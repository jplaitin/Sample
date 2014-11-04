package com.example.example.camera;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.haave.docscanner.DocScannerApplication;
import com.haave.docscanner.DocScannerPreferences;
import com.haave.docscanner.opengl.CameraGLSurfaceView;
import com.haave.docscanner.view.IRotatable;
import com.norfello.android.graphics.Dim2D;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, IRotatable{
//	private static DebugLog l = new DebugLog(CameraSurfaceView.class.getSimpleName());
	public static final Double ASPECT_TOLERANCE = 0.1; 
	private SurfaceHolder holder;
	private Camera camera;
	private Camera.Parameters cameraParameters;
	
	private String focusMode = null; 
	private int pictureFormat = -1;
	
	private Dim2D previewSize;
	private Dim2D imageSize;
	private Rect previewRect;
	private CameraGLSurfaceView previewSurface;
	private int cameraDisplayOrientation;
	private int facingBackCameraID;
	private boolean previewOn;
	private boolean isSurface = false;
	private int dx, dy;
	
	private ArrayList<OnCameraActionListener> cameraActionListeners = new ArrayList<OnCameraActionListener>();
	public CameraSurfaceView(Context context) {
		super(context);
		initHolder(context);
	}

	public CameraSurfaceView(Context context, AttributeSet set) {
		super(context, set);
		initHolder(context);
	}

	private void initHolder(Context context) {
		// Initiate the Surface Holder properly
		camera 			= null;
		previewSurface 	= null;
		previewSize 	= null;
		cameraDisplayOrientation = 0;
		previewOn = false;
		//getCameraInfo(); Uses only back wards facing camera
		this.holder = this.getHolder();
		this.holder.addCallback(this);
		this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		setWillNotDraw(false);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		if (camera == null) return;
		stopPreview();
		
		initPreferences(width, height);
		
		startPreview();
	}

	public void stopPreview() {
		if(previewOn) {
			previewOn = false;
			for (OnCameraActionListener l : cameraActionListeners) {
				l.onStopPreview();
			}
			camera.stopPreview();
//			l.e("preview Stopped");
		}
		if(camera != null){
			camera.setPreviewCallback(null);
		}
	}
	
	public void startPreview() {
	//	l.i("startPreview() preview call back: "+ previewSurface);
		if(!isSurface) return;
		camera.setParameters(cameraParameters);
		camera.startPreview();
		camera.setPreviewCallback(previewSurface);
		if(previewSurface != null) {
			previewSurface.startDrawPreview(previewSize, previewRect, cameraDisplayOrientation);
		}
		for (OnCameraActionListener l : cameraActionListeners) {
			l.onStartPreview();
		}
//		l.e("preview Running");
		previewOn = true;
	}
	
	public void clear(){
		if(cameraActionListeners != null){
			cameraActionListeners.clear();
			cameraActionListeners = null;
		}
		previewSurface = null;
		previewSize = null;
		imageSize = null;	
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
//		l.i("surfaceCreated");
		try {
			if(camera == null) {
				camera = Camera.open();
			}
			camera.setPreviewDisplay(this.holder);
			cameraParameters = camera.getParameters();

		} catch (IOException e) {
//			l.e("IOException caused by setPreviewDisplay()", e);
		}
		isSurface = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		l.i("surfaceDestroyed");
		isSurface = false;
		// Surface will be destroyed when we return, so stop the preview.
		if (camera != null) {
			stopPreview();
			camera.release();
			camera = null;
		}
	}

	private Dim2D getOptimalPreviewSize(List<Size> sizes, Dim2D imageSize) {
	//	l.i("getOptimalPreviewSize( list<Size>, "+ w +", "+ h +" )");
		double targetRatio = (double) imageSize.width / imageSize.height;
		if (sizes == null)
			return null;

//		l.i("targetRatio: "+ targetRatio);
		Size bestFitSize = null;
		Size optimalSize = null;
		int maxSize = 0;
		double minScaleDiff = Double.MAX_VALUE;
		double scaleDiff = 0.0;
		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			scaleDiff = Math.abs(targetRatio - ratio);
			if(scaleDiff == 0) {
				if(maxSize < size.width*size.height) {
					bestFitSize = size;
					maxSize = size.width*size.height;
				}
			}
			else if(bestFitSize == null) {
				if(scaleDiff < minScaleDiff) {
					optimalSize = size;
				}
				else if((optimalSize.width*optimalSize.height) < (size.width*size.height)) {
					optimalSize = size;
				}
			}
		}
		if(bestFitSize == null) {
			bestFitSize = optimalSize;
		}
		return new Dim2D(bestFitSize.width, bestFitSize.height);
	}

	public void setPreviewCallback(CameraGLSurfaceView cb) {
	//	l.i("setPreviewCallback( "+ cb +")");
		this.previewSurface = cb;
		if(camera != null) {
			camera.setPreviewCallback(cb);
		}
	}
	
	public int getCameraDisplayOrientation() {
		return cameraDisplayOrientation;
	}
	public Dim2D getImageSize(){
		return imageSize;
	}	
	public Dim2D getPreviewSize() {
		return previewSize;
	}
	public void setPreviewSize(Dim2D previewSize) {
		this.previewSize = previewSize;
	}
	
	public void setCameraDisplayOrientation( android.hardware.Camera camera ) { //did this a long way around so xml layout editor doesn't give error messages
		 android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(facingBackCameraID, info);
	     int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0;
	         						break;
	         case Surface.ROTATION_90: degrees = 90;
	         						break;
	         case Surface.ROTATION_180: degrees = 180;
	         						break;
	         case Surface.ROTATION_270: degrees = 270;
	         						break;
	     }
	    
	     degrees = (degrees + 45) / 90 * 90;
	     if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
	         rotation = (info.orientation - degrees + 360) % 360;
	     } else {  // back-facing camera
	         rotation = (info.orientation + degrees) % 360;
	     }
	    
	     cameraDisplayOrientation = rotation;
//	     l.i("cameraDisplayOrientation: "+ cameraDisplayOrientation);
	 }
	
	public void getCameraInfo() {
		// Look for front-facing camera, using the Gingerbread API.
	    // Java reflection is used for backwards compatibility with pre-Gingerbread APIs.
	    try {
	        Class<?> cameraClass = Class.forName("android.hardware.Camera");
	        Object cameraInfo = null;
	        Field field = null;
	        int cameraCount = 0;
	        Method getNumberOfCamerasMethod = cameraClass.getMethod( "getNumberOfCameras" );
	        if ( getNumberOfCamerasMethod != null ) {
	            cameraCount = (Integer) getNumberOfCamerasMethod.invoke( null, (Object[]) null );
	        }
	        Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
	        if ( cameraInfoClass != null ) {
	            cameraInfo = cameraInfoClass.newInstance();
	        }
	        if ( cameraInfo != null ) {
	            field = cameraInfo.getClass().getField( "facing" );
	        }
	        Method getCameraInfoMethod = cameraClass.getMethod( "getCameraInfo", Integer.TYPE, cameraInfoClass );
	        if ( getCameraInfoMethod != null && cameraInfoClass != null && field != null ) {
	            for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
	                getCameraInfoMethod.invoke( null, camIdx, cameraInfo );
	                int facing = field.getInt( cameraInfo );
	                if ( facing == CameraInfo.CAMERA_FACING_BACK ) { // Camera.CameraInfo.CAMERA_FACING_FRONT
	                	facingBackCameraID = camIdx;
	                }
	            }
	        }
	    }
	    // Ignore the bevy of checked exceptions the Java Reflection API throws - if it fails, who cares.
	    catch ( Exception e        ) {}
	}
	
	public void applyPreferences(DocScannerPreferences preference) {
//		l.i("applyPreferences()");
		if(!isSurface) return;
		
		if(previewOn) {	
			stopPreview();
		}	
		changingPreferences(preference);
		startPreview();
	}

	public void autoFocus(AutoFocusCallback autoFocusCallback) {
//		l.i("autoFocus previewOn: "+ previewOn);
		if(previewOn) {
			camera.autoFocus(autoFocusCallback);
		}
	}

	public Camera getCamera() {
		return camera;
	}

	public boolean usesAutoFocus() {
		if(focusMode == null){
			return false;
		}
		boolean usesAutoFocus = focusMode.equals(Parameters.FOCUS_MODE_AUTO);
	//	l.i("usesAutoFocus() -> "+ usesAutoFocus);
		return usesAutoFocus;
	}

	public void addCameraActionListener( OnCameraActionListener l) {
		cameraActionListeners.add(l);
	}
	public void removeCameraActionListener( OnCameraActionListener l) {
		cameraActionListeners.remove(l);
	}
	public void clearCameraActionListeners() {
		cameraActionListeners.clear();
	}

	@Override
	public void setOrientation(int orientation) {
	}
		
	public boolean isPreviewRunning() {
		return previewOn;
	}

	public void releaseCamera() {
		if(camera != null) {
			camera.release();
		}
	}

	private DocScannerPreferences getAppicationPreferences() {
		return ((DocScannerApplication)((ActivityDocScannerCamera)getContext()).getApplication()).getApplicationPreferences();
	}
	
	//TODO
	private void initPreferences(int width, int height) { 
//		l.i("TARGET : w: "+ width +" h: "+ height);
		DocScannerPreferences preferences = getAppicationPreferences();
		cameraParameters = camera.getParameters();		
		setCameraDisplayOrientation(camera);
		
		if(preferences.isFirstRunCamera()) {		
			ArrayList<Camera.Size> previewSizes = (ArrayList<Camera.Size>)cameraParameters.getSupportedPreviewSizes();
			imageSize = getCameraCaptureImageSize();
			previewSize = getOptimalPreviewSize(previewSizes, imageSize);
			preferences.setPreviewSize(previewSize);
			setCameraOrientation();
			positionSurfaceView();
			
			List<String> tempColorEffects = cameraParameters.getSupportedColorEffects();
			List<String> colorEffects 	  = new ArrayList<String>();
			if(tempColorEffects != null) {
				for (String effect : tempColorEffects) {
	//				l.i("ColorEffect effect: "+ effect);
					if(effect.equals(Camera.Parameters.EFFECT_MONO)) {
						colorEffects.add(effect);
					}
					else if(effect.equals(Camera.Parameters.EFFECT_NEGATIVE)) {
						colorEffects.add(effect);
					}
					else if(effect.equals(Camera.Parameters.EFFECT_NONE)) {
						colorEffects.add(effect);
					}
				}
				preferences.setColorEffects(colorEffects);
				preferences.setUsedColorEffect(0);
			}
			else {
				preferences.setUsedColorEffect(DocScannerPreferences.CAMERA_NO_SUPPORT);
			}
			
			List<String> flashModes	= cameraParameters.getSupportedFlashModes();
			List<String> filtFlashModes = new ArrayList<String>();
			if(flashModes != null) {
				for (String flashMode : flashModes) {
					if(flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
						filtFlashModes.add(flashMode);
					}
					else if(flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
						filtFlashModes.add(flashMode);
					}
					else if(flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
						filtFlashModes.add(flashMode);
					}
				}
				preferences.setFlashModes(filtFlashModes);
				preferences.setUsedFlashMode(2);
			}
			else {
				preferences.setUsedFlashMode(DocScannerPreferences.CAMERA_NO_SUPPORT);
			}
			
			List<String> focusModes = cameraParameters.getSupportedFocusModes();
			if (focusModes != null) {
				if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
					focusMode = Camera.Parameters.FOCUS_MODE_AUTO;
				}else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)){
					focusMode = Camera.Parameters.FOCUS_MODE_MACRO; //Terrible
				}
				else {
					focusMode = focusModes.get(0);
				}
			}
			preferences.setFocusMode(focusMode);
			
			List<Integer> pictureFormats = cameraParameters.getSupportedPictureFormats();
			if (pictureFormats != null) {
				if(pictureFormats.contains(PixelFormat.RGB_565)){
					pictureFormat = PixelFormat.RGB_565;
				}
				else{
					pictureFormat = PixelFormat.JPEG;
				}
			}
			preferences.setPictureFormat(pictureFormat);
	
//			l.i("TARGET imageSize: w: "+ imageSize.width +" h: "+ imageSize.height);

//			cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
			cameraParameters.setPictureSize(imageSize.width, imageSize.height);
			cameraParameters.setPictureFormat(pictureFormat);
			cameraParameters.setJpegQuality(100);
			cameraParameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
			cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			cameraParameters.setFocusMode(focusMode);
			
			camera.setParameters(cameraParameters);
			
			preferences.setFirstRunCamera();
		}
		else {
			changingPreferences(preferences);
			
			previewSize = preferences.getPreviewSize();
//			l.i("TARGET previewSize: w: "+ previewSize.width +" h: "+ previewSize.height);
			setCameraOrientation();
			positionSurfaceView();
	        
			imageSize = getCameraCaptureImageSize();
			cameraParameters.setPictureSize(imageSize.width, imageSize.height);
			
			pictureFormat = preferences.getPictureFormat();
			focusMode = preferences.getFocusMode();
			if(focusMode == null) {
				List<String> focusModes = cameraParameters.getSupportedFocusModes();
				if (focusModes != null) {
					if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
						focusMode = Camera.Parameters.FOCUS_MODE_AUTO;
					}else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)){
						focusMode = Camera.Parameters.FOCUS_MODE_MACRO; //Terrible
					}
					else {
						focusMode = focusModes.get(0);
					}
				}
				preferences.setFocusMode(focusMode);
			}
			cameraParameters.setJpegQuality(100);
			cameraParameters.setPictureFormat(pictureFormat);
			cameraParameters.setFocusMode(focusMode);
		}
	}
	
	private Dim2D getCameraCaptureImageSize() {
		List<Camera.Size> imageSizes = cameraParameters.getSupportedPictureSizes();
    	int maxSize = 0;
    	int sizeIndex = 0;
    	int maxIndex = 0;
    	for (Size size : imageSizes) {
//    		l.i("Camera image size w: "+ size.width +" h: "+ size.height );
			if(size.width*size.height > maxSize) {
				maxSize = size.width*size.height;
				maxIndex = sizeIndex;
			}
			sizeIndex++;
		}
		return new Dim2D(imageSizes.get(maxIndex).width, imageSizes.get(maxIndex).height);	
	}
	
	private void changingPreferences(DocScannerPreferences preferences) {
//		l.i("changingPreferences() applied");
		String colorEff = preferences.getColorEffect();
		if(colorEff != null) {
			cameraParameters.setColorEffect(colorEff);
		}
		String flashMo = preferences.getFlashMode();
		if(flashMo != null) {
			cameraParameters.setFlashMode(flashMo);
		}
	}
	
	private void setCameraOrientation() {
//		l.i("setCameraOrientation() cameraDisplayOrientation: "+ cameraDisplayOrientation);
		if(cameraDisplayOrientation == 0)
        {
        	camera.setDisplayOrientation(180);
        }
		else if(cameraDisplayOrientation == 90)
        {                         
            camera.setDisplayOrientation(90);                           
        }
		cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
	}
	
	public void setFocusArea(Rect r) {
		if(r == null || !previewOn) return;
		if(android.os.Build.VERSION.SDK_INT < 14 || cameraParameters.getMaxNumFocusAreas() == 0) return;
		
		List<Camera.Area> focusList = new ArrayList<Camera.Area>();
		focusList.add(new Camera.Area(r, 1000));
		
		if(cameraParameters.getMaxNumFocusAreas() > 0){
			camera.getParameters().setFocusAreas(focusList);
		}
		if(cameraParameters.getMaxNumMeteringAreas() > 0){
			camera.getParameters().setMeteringAreas(focusList);
		}
	}
	
	private void positionSurfaceView() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displaymetrics);

        int screenH = displaymetrics.heightPixels;
        int screenW = displaymetrics.widthPixels;
        int prevW 	= previewSize.width;
    	int prevH 	= previewSize.height;
        if(cameraDisplayOrientation == 90 || cameraDisplayOrientation == 270)
        {
        	prevW = previewSize.height;
        	prevH = previewSize.width;
        }
		
        float screenRatio = (float)screenW / screenH;
		float previewRatio = (float) prevW/ prevH;	
//		l.i("screenW: "+ screenW +" screenH: "+ screenH +" prevW: "+ prevW +" prevH: "+ prevH);
		
		float previewScale = 1.0f;
		int cameraSurfaceWidth = 0;
		int cameraSurfaceHeight = 0;
		if(previewRatio < screenRatio) { //extra height scale using width
			previewScale = (float)screenW/prevW;
			dx = 0;
			dy = (int)((previewScale*prevH-screenH)/2.0f);
//			l.i("dy: "+ dy);
			int top = (int)(dy/previewScale);
			int bottom = prevH - top;
			previewRect = new Rect(0, top, prevW, bottom);
			cameraSurfaceWidth = screenW;
			cameraSurfaceHeight = (int) (previewScale * prevH);
		}
		else {
			previewScale = (float)screenH/prevH;
			dx = (int)((previewScale*prevW-screenW)/2.0f);
			dy = 0;
//			l.i("dx: "+ dx);
			int left = (int)(dx/previewScale);
			int right = prevW - left;
			previewRect = new Rect(left, 0, right, prevH);
			cameraSurfaceWidth = (int) (previewScale * prevW);
			cameraSurfaceHeight = screenH;
		}
//		l.i("cameraSurfaceWidth: " + cameraSurfaceWidth + " cameraSurfaceHeight: " + cameraSurfaceHeight);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(cameraSurfaceWidth, cameraSurfaceHeight);
		lp.setMargins(-dx, -dy, 0, 0);
		lp.gravity = Gravity.TOP | Gravity.LEFT; 
		//Older android version doesn't change margin unless gravity is also set
		//http://stackoverflow.com/a/6123374/1471489 Thank you Andrei
		setLayoutParams(lp);
		requestLayout();
		invalidate();
		
//		l.i("previewRect: "+ previewRect.toString());
//		l.i("CameraSurface w: "+ getWidth() +" h: "+ getHeight() +" top: "+ getTop() +" left: "+ getLeft());
	}
	
	public Rect getPreviewVisibleRect() {
		return previewRect;
	}
}
