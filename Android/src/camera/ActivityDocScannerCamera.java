package com.example.example.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.apps.analytics.easytracking.EasyTracker;
import com.haave.docscanner.DocScannerActivity;
import com.haave.docscanner.DocScannerApplication;
import com.haave.docscanner.DocScannerPreferences;
import com.haave.docscanner.GlobalVariables;
import com.haave.docscanner.R;
import com.haave.docscanner.analytics.GlobalAnalyticsParameters;
import com.haave.docscanner.camera.controllers.CameraControllerManager;
import com.haave.docscanner.camera.controllers.ManualControllerClickListener;
import com.haave.docscanner.camera.controllers.OnCameraPreferenceChangeListener;
import com.haave.docscanner.db.DataBaseAccessor;
import com.haave.docscanner.documents.ActivityDocumentPages;
import com.haave.docscanner.documents.SimpleGestureFilter;
import com.haave.docscanner.documents.SimpleGestureFilter.SimpleGestureListener;
import com.haave.docscanner.evernote.EvernoteSender;
import com.haave.docscanner.opengl.OnScanListener;
import com.haave.docscanner.recognize.documentpresets.OnPresetChangeListener;
import com.haave.docscanner.recognize.documentpresets.Preset;
import com.haave.docscanner.recognize.documentpresets.PresetManager;
import com.haave.docscanner.view.CameraLayers;
import com.haave.docscanner.view.IRotatable;
import com.norfello.android.utils.UiUtils;

public class ActivityDocScannerCamera extends DocScannerActivity implements IImageSaverCallback{
//	private final DebugLog l = new DebugLog(ActivityDocScannerCamera.class.getSimpleName());
	public static final String EXTRA_KEY_IMAGE_PATHS = "import_camera_captured_image_paths";
	public static final String EXTRA_KEY_THUMB_PATHS = "import_camera_captured_thumb_paths";
	public static final String EXTRA_KEY_SNAP_SHOT = "import_camera_use_snap_shot";

	// GUI elements
	private CameraLayers cameraViewLayers;
	private CameraControllerManager controllerLayer;
	private SimpleGestureFilter detector;
	
	private boolean isInitialized;
	private boolean firstOnResume = true;
	private ImageSaver mImageSaver;
	private FileManager fileManager;
	private PresetManager presetManager;
	private boolean capturingImage = false;

	// orientation
	private MyOrientationEventListener orientationListener;
	private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	// The orientation compensation for icons and thumbnails. Ex: if the value
	// is 90, the UI components should be rotated 90 degrees counter-clockwise.
	private int mOrientationCompensation = 0;	
	private DataBaseAccessor dbAccessor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		//l.turnOff();
		
		setContentView(R.layout.activity_camera);
		Thread initThread = new Thread(new Runnable() {
			@Override
			public void run() {
				initialize();
			}
		});
		initThread.run();
	}

	@Override
	public synchronized void capturedImageAndThumbSaved(final String imagePath, final String thumbPath) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				controllerLayer.setScannedThumb(thumbPath);
				DocScannerPreferences pref = ((DocScannerApplication) ActivityDocScannerCamera.this.getApplication()).getApplicationPreferences();
				pref.setLastPreset(GlobalVariables.currentPresetType);
				
				String trackerL = GlobalAnalyticsParameters.createScanLabel(pref, presetManager.getPresetName(GlobalVariables.currentPresetType));
//				l.e(trackerL);
				EasyTracker.getTracker().trackEvent(GlobalAnalyticsParameters.TRACK_C_CAMERA, 
						GlobalAnalyticsParameters.TRACK_A_CAPTURE, trackerL, 0);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// mSensorManager.registerListener(DocScannerCameraActivity.this, mLight, SensorManager.SENSOR_DELAY_FASTEST);
//		l.i("onResume :"+ SystemUtils.getMemoryInfo());	
		if (firstOnResume) { //run on create
			firstOnResume = false;
			return;
		}
		orientationListener.enable();
		if(cameraViewLayers != null) {
			cameraViewLayers.onResume();
		}
		if(mImageSaver != null){
			mImageSaver.setImageSaverCallback(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		orientationListener.disable();
		cameraViewLayers.onPause();
		mImageSaver.setImageSaverCallback(null);
		
//		l.i("onPause :"+ SystemUtils.getMemoryInfo());	
		// mSensorManager.unregisterListener(DocScannerCameraActivity.this, mLight);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isInitialized) {
			if (mImageSaver != null) {
				mImageSaver.setPresetManager(null);
				mImageSaver.finish();
				mImageSaver = null;
				cameraViewLayers.setImageSaver(mImageSaver);
			}
			if (dbAccessor != null) {
	        	dbAccessor.close();
			}
			if(cameraViewLayers != null) {
				cameraViewLayers.releaseCamera();
				cameraViewLayers.clear();
			}
		}
//		l.e("onDestroy()");
	}
	
	private void initialize() {
		// init config
		if (isInitialized)
			return;
		
		orientationListener = new MyOrientationEventListener(ActivityDocScannerCamera.this);
		orientationListener.enable();
		
		dbAccessor 		= new DataBaseAccessor(this);
		fileManager 	= new FileManager();
		presetManager	= new PresetManager(ActivityDocScannerCamera.this);
		presetManager.setOnChangeListener(new OnPresetChangeListener() {
			@Override
			public void presetChanged(Preset p) {
				controllerLayer.setPresetInfo(presetManager.getPresetInfo(), presetManager.getLockedPresetType());
			}
		});
		
		mImageSaver = new ImageSaver(fileManager, dbAccessor);
		mImageSaver.setImageSaverCallback(this);
		mImageSaver.setPresetManager(presetManager);
		DocScannerPreferences pref = ((DocScannerApplication)ActivityDocScannerCamera.this.getApplication()).getApplicationPreferences();
		EvernoteSender.setAccount(pref.getEvernoteUserName(), pref.getEvernotePassWD());
		
		FrameLayout cameraFrameLayout 	= (FrameLayout) findViewById(R.id.cameraFrameLayout); 
		cameraViewLayers 				= (CameraLayers) findViewById(R.id.cameraPreview);		
		cameraViewLayers.setImageSaver(mImageSaver);
		cameraViewLayers.setPresetManager(presetManager);
		cameraViewLayers.addScanListener(new OnScanListener() {
			@Override
			public void onScanCompleted() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int edgeMode = ((DocScannerApplication)ActivityDocScannerCamera.this.getApplication()).getApplicationPreferences().getEdgeRecognizeModeIndex();
						if(capturingImage && edgeMode == GlobalVariables.EDGE_MODE_NONE) {
							int enhanceMode = ((DocScannerApplication)ActivityDocScannerCamera.this.getApplication()).getApplicationPreferences().getEnhanceModeIndex();
							if(enhanceMode == GlobalVariables.ENHANCE_MODE_NONE) {
								cameraViewLayers.showDocumentCreatedAnimation();
							}
						}
						controllerLayer.resumePreview();
						initPreviewControllerListeners();
						cameraViewLayers.resumePreview();
						cameraViewLayers.setPresetManager(presetManager);
						controllerLayer.setPresetInfo(presetManager.getPresetInfo(), presetManager.getLockedPresetType());
						capturingImage = false;
					}
				});
			}
			@Override
			public void onEnchanceProgress(final int progress) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						controllerLayer.enchanceProgress(progress);
					}
				});
			}
			@Override
			public void onClipProgress(final int progress) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						controllerLayer.clipProgress(progress);
					}
				});
			}
		});
		cameraViewLayers.addOnShakeGestureListener(new OnShakeGestureListener() {
			@Override
			public void onShakeGesture() {
				DocScannerPreferences prefer = ((DocScannerApplication)ActivityDocScannerCamera.this.getApplication()).getApplicationPreferences();
				if(prefer.getEdgeRecognizeModeIndex() == GlobalVariables.EDGE_MODE_AUTO) {
					presetManager.lockPreset(PresetManager.PRESET_NONE);
					controllerLayer.setPresetInfo(presetManager.getPresetInfo(), presetManager.getLockedPresetType());
				}
			}
		});
		cameraViewLayers.applyPreferences(((DocScannerApplication)ActivityDocScannerCamera.this.getApplication()).getApplicationPreferences());
		
		controllerLayer		= new CameraControllerManager(cameraFrameLayout, this);
		initPreviewControllerListeners();
		
		detector = new SimpleGestureFilter(this, new SimpleGestureListener() {
			@Override
			public void onSwipe(int direction) {
//				String str = "";
				switch (direction) {
				case SimpleGestureFilter.SWIPE_LEFT:
				{
					presetManager.lockPreviousPreset();
//					str = "Swipe left";
					break;
				}
				case SimpleGestureFilter.SWIPE_RIGHT:
				{
					presetManager.lockNextPreset();
//					str = "Swipe right";
					break;
				}
				case SimpleGestureFilter.SWIPE_DOWN:
//					str = "Swipe Down";
					break;
				case SimpleGestureFilter.SWIPE_UP:
//					str = "Swipe Up";
					break;
				}
//				Log.i("debug", str);
//				Toast.makeText(ActivityDocumentPages.this, str, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onDoubleTap() {
			}
		});
		isInitialized = true;
	}
	
	private void initPreviewControllerListeners() {
		controllerLayer.setCameraClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(!capturingImage) { 
							capturingImage = cameraViewLayers.captureImage();
							int edgeMode = ((DocScannerApplication)ActivityDocScannerCamera.this.getApplication()).getApplicationPreferences().getEdgeRecognizeModeIndex();
							if(capturingImage && edgeMode == GlobalVariables.EDGE_MODE_AUTO) {
								controllerLayer.showScanProgressView();
							}
							if(capturingImage && edgeMode == GlobalVariables.EDGE_MODE_MANUAL) {
								controllerLayer.showScanManualEdges();
								controllerLayer.setPresetInfo(presetManager.getPresetInfo(), presetManager.getLockedPresetType());
								controllerLayer.setManualControllerClickListener(new ManualControllerClickListener() {
									@Override
									public void scanImage() {
										controllerLayer.showScanProgressView();										
										cameraViewLayers.scanImage();
									}				
									@Override
									public void resumePreview() {
										controllerLayer.resumePreview();
										initPreviewControllerListeners();
										cameraViewLayers.resumePreview();
										controllerLayer.setPresetInfo(presetManager.getPresetInfo(), presetManager.getLockedPresetType());
										capturingImage = false;
									}
								});
								controllerLayer.setManualControlOnTouchListener(cameraViewLayers.getOnCornerTouchListener());
							}
						}
					}
				});
			}
		});
		controllerLayer.setPresetInfo(presetManager.getPresetInfo(), presetManager.getLockedPresetType());
		controllerLayer.setOnPreferenceChangeListener(new OnCameraPreferenceChangeListener() {
			@Override
			public void preferenceChanged() {
				ActivityDocScannerCamera.this.cameraViewLayers.applyPreferences(
						((DocScannerApplication)ActivityDocScannerCamera.this.getApplication()).getApplicationPreferences());
			}
		});
		controllerLayer.setThumbClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}
	
	private class MyOrientationEventListener extends OrientationEventListener {
		public MyOrientationEventListener(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			// We keep the last known orientation. So if the user first orient
			// the camera then point the camera to floor or sky, we still have
			// the correct orientation.
			if (orientation == ORIENTATION_UNKNOWN)
				return;
			mOrientation = UiUtils.roundOrientation(orientation, mOrientation);
			// When the screen is unlocked, display rotation may change. Always
			// calculate the up-to-date orientationCompensation.
			int orientationCompensation = mOrientation + UiUtils.getDisplayRotation(ActivityDocScannerCamera.this);
			if (mOrientationCompensation != orientationCompensation) {
				mOrientationCompensation = orientationCompensation;
				setOrientationIndicator(mOrientationCompensation);
			}
			// l.i("orientation: "+ orientation +" mOrientation: "+ mOrientation +" mOrientationCompensation: "+ mOrientationCompensation);
		}
	}

	private void setOrientationIndicator(int orientation) {
//		l.i("setOrientationIndicator( " + orientation + " )");
		IRotatable[] indicators = { cameraViewLayers, controllerLayer };
		for (IRotatable indicator : indicators) {
			if (indicator != null)
				indicator.setOrientation(orientation);
		}
	}
	
	
	@Override
	public void onBackPressed() {
		if(controllerLayer.isCameraSettingsVisible()) {
			controllerLayer.hideCameraSettings();
		}
		else {
			Intent i = new Intent(ActivityDocScannerCamera.this, ActivityDocumentPages.class);
			i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			ActivityDocScannerCamera.this.startActivity(i);
			ActivityDocScannerCamera.this.finish();
		}
	}

	@Override
	public void onEvernoteFinishedSending(String file_name, int doc_id) {
		super.onEvernoteFinishedSending(file_name, doc_id);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ActivityDocScannerCamera.this, "Image sent to evernote", Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	@Override
	public void onEvernoteError(String file_name, int doc_id, final String error) {
		super.onEvernoteError(file_name, doc_id, error);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ActivityDocScannerCamera.this, "Failed to send image to evernote : \n"+ error, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onEvernoteNotebooks(String[] notebooks) {
		super.onEvernoteNotebooks(notebooks);
	}

	@Override
	public void onEvernoteAccountValid(String userName, String passWD) {
		super.onEvernoteAccountValid(userName, passWD);
		// TODO Auto-generated method stub
	}
}
