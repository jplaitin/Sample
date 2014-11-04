package com.example.example.opengl;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.haave.docscanner.DocScannerPreferences;
import com.haave.docscanner.GlobalVariables;
import com.haave.docscanner.camera.ImageSaver;
import com.haave.docscanner.camera.controllers.ViewCameraManualController.OnCornerTouchListener;
import com.haave.docscanner.jni.WhiteMethod;
import com.haave.docscanner.opengl.CustomGLSurfaceView.Renderer;
import com.haave.docscanner.opengl.GLDrawerDocumentEdge.TextureWrap;
import com.haave.docscanner.perspective.DocumentPiecer;
import com.haave.docscanner.perspective.Rectangle3D;
import com.haave.docscanner.recognize.CornerPoint;
import com.haave.docscanner.recognize.DocumentCornerManager;
import com.haave.docscanner.recognize.documentpresets.PresetManager;
import com.norfello.android.graphics.Dim2D;



public class CameraGLSurfaceView extends CustomGLSurfaceView implements SurfaceHolder.Callback, Renderer, 
	android.hardware.Camera.PreviewCallback, IRenderFrame {
//	private static final DebugLog l = new DebugLog(CameraGLSurfaceView.class.getSimpleName());
	private int[] textureIDs;
	
	private static final int state_clear_gl = -1;
	private static final int state_pause = 0;
	private static final int state_preview = 1;
	private static final int state_capture = 2;
	private static final int state_captute_manual = 3;
	private static final int state_scan_pers = 4;
	private static final int state_enhance = 5;
	
	private int state;
	private boolean isEglConfigApha = false;
	
	private int edgeRecognizeMode = GlobalVariables.EDGE_MODE_NONE;
	private int enhanceMode = GlobalVariables.ENHANCE_MODE_NONE;
	
	private ArrayList<OnScanListener> listeners = new ArrayList<OnScanListener>();

	private GLDrawerDocument docDrawer = null;
	private GLDrawerCameraPreview cameraPreviewDrawer = null;
	private GLDrawerDocumentEdge drawDocumentEdges = null;
	private ImageSaver imageSaver = null;
	private Thread prepareReceivingImage = null;
	private Thread enhanceAndSave = null;
	private boolean hasSurface = false;

	private PresetManager presetManager = null;
	
	public CameraGLSurfaceView(Context c) {
		super(c);
	}
	public CameraGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		state = state_pause;
		setEGLConfig();
	}
	
	private void setEGLConfig() {
		boolean changeEGLConfig = false;
		if(state > state_preview && isEglConfigApha) {
			this.setEGLConfigChooser(5, 6, 5, 0, 0, 0);
			isEglConfigApha = false;
			changeEGLConfig = true;
			
		}
		else if(!isEglConfigApha){
			this.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
			isEglConfigApha = true;
			changeEGLConfig = true;
		}
		if(changeEGLConfig) {
			this.setRenderer(this);
			this.setRenderMode(RENDERMODE_WHEN_DIRTY);
			this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
			if(hasSurface) {
				initEGL();
			}
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		state = state_pause; 
	}
	
	@Override
	public void onResume() {
		super.onResume();
		state = state_preview;
	}
	
	public void clear() { 
		removeAllListeners();
		if(imageSaver != null) {
			imageSaver.finish();
			imageSaver = null;
		}	
		presetManager = null;
		clearLastScan();
	}
	
	public void clearLastScan() {
		if(enhanceAndSave != null){
			final Thread temp = enhanceAndSave;
			enhanceAndSave = null;
			try {
				temp.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(docDrawer != null){
			docDrawer.setRenderCallback(null);
			docDrawer.clear();
			docDrawer = null;
		}
		if(cameraPreviewDrawer != null) {
			cameraPreviewDrawer.setRenderCallback(null);
			cameraPreviewDrawer.clear();
			cameraPreviewDrawer = null;
		}
		if(prepareReceivingImage != null){
			final Thread temp = prepareReceivingImage;
			prepareReceivingImage = null;
			try {
				temp.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(drawDocumentEdges != null) {
			drawDocumentEdges.setRenderCallback(null);
			drawDocumentEdges.clear();
			drawDocumentEdges = null;
		}
		clearOpenGL();
	}
	
	public void clearOpenGL() {
		state = state_clear_gl;
		renderFrame();
	}
	
	@Override
	public void renderFrame(){
		requestRender();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
//		l.i("onDrawFrame");
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		if(state == state_pause) return;
		gl.glLoadIdentity();
		
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, OpenGLUtils.makeCopyFloatBuffer( new float[] {  //Draw surface
				0.0f, 0.0f,		// V1 - bottom left 
				0.0f, 1.0f,		// V2 - bottom right
				1.0f, 0.0f,		// V3 - top left
				1.0f, 1.0f		// V4 - top right
		}));	
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		if(state == state_preview){
			cameraPreviewDrawer.drawPreview(gl, textureIDs);
		}
		else if(state == state_scan_pers) {	
			docDrawer.drawData(gl, textureIDs);
			int progress = docDrawer.getProgress();
			for (OnScanListener l : listeners) {
				l.onClipProgress(progress);
			}
			
			cameraPreviewDrawer.showScanProgress(progress);
			cameraPreviewDrawer.drawAnimatedScan(gl, textureIDs);
			if(progress == 100) {
				state = state_pause;
				documentDrawn();
			}
		}
		else if(state == state_captute_manual) {
			drawDocumentEdges.drawCornersManual(gl, textureIDs);
		}
		else if(state == state_capture){
//			gl.glDeleteTextures(1, textureIDs, GLPreviewEnhanceThread.TEXTURE_INDEX_GLENHANCEDFRAME);
			cameraPreviewDrawer.drawCaptureFrame(gl, textureIDs);
			renderFrame();
		}
		else if(state == state_clear_gl){
//			l.i("glTexture Cleared");
			int count = textureIDs.length;
			for(int i=0; i<count; i++){
				if(textureIDs[i] != 0) {
					gl.glDeleteTextures(1, textureIDs, i);
					textureIDs[i] = 0;
				}
			}
			gl.glFlush();
			System.gc();
		}
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
	
	public void documentDrawn() {
//		cameraPreviewDrawer.clear();
//		state = state_clear_gl;
//		renderFrame();
		System.gc();
		final Bitmap docImage = docDrawer.getDocumentImage();
		docDrawer.setRenderCallback(null);
//		docDrawer.clear();
//		docDrawer = null;
		
		state = state_enhance;
		System.gc();
		if(enhanceMode != GlobalVariables.ENHANCE_MODE_NONE){
			enhanceAndSave = new Thread(new Runnable() {
				@Override
				public void run() {
					WhiteMethod whiteMethod = new WhiteMethod();
					for (OnScanListener l : listeners) {
						whiteMethod.addOnOnScanListener(l);
					}
					whiteMethod.imageWhiteMethod(docImage);
					for (OnScanListener l : listeners) {
						whiteMethod.removeOnOnScanListener(l);
						l.onScanCompleted();
					}
					imageSaver.addImage(docImage, 256); 
				}
			});
			enhanceAndSave.start();
		}
		else{
			for (OnScanListener l : listeners) {
				l.onScanCompleted();
			}
			imageSaver.addImage(docImage, 256); 
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (height == 0) { // Prevent A Divide By Zero By
			height = 1; // Making Height Equal One
		}
		
//		l.i("view width: "+ width +" view height: "+ height);
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, 1.0f, 0.0f, 1.0f, -1.0f, 1.0f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		int count = textureIDs.length;
		boolean shouldFlush = false;
		for(int i=0; i<count; i++){
			if(textureIDs[i] != 0) {
				gl.glDeleteTextures(1, textureIDs, i);
				shouldFlush = true;
			}
		}
		
		if(shouldFlush) {
			gl.glFlush();
			System.gc();
		}
		//document drawer deletes textures after each draw
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
 
		gl.glFrontFace(GL10.GL_CW);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glCullFace(GL10.GL_BACK); 
		
		gl.glShadeModel(GL10.GL_SMOOTH);
		
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_DITHER);
		
		textureIDs = new int[6]; //previewFrame, piece for clipping, previewFrameEnhanced, clippedImage, documentcornerpointicon, zoomFrame
		hasSurface = true;
	}
		
	public void applyPreferences(DocScannerPreferences preference) {
//		l.i("TARGET applyPreferences()");
		enhanceMode = preference.getEnhanceModeIndex();
		edgeRecognizeMode = preference.getEdgeRecognizeModeIndex();
		if(cameraPreviewDrawer != null) {
			cameraPreviewDrawer.setEdgeRecognizeMode(edgeRecognizeMode);
			cameraPreviewDrawer.setEnhanceMode(enhanceMode);
			if(edgeRecognizeMode != GlobalVariables.EDGE_MODE_AUTO) {
				state = state_clear_gl;
				renderFrame();
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
				  @Override
				public void run() {
					  state = state_preview;
				  }
				}, 100);

			}
			else {
				state = state_preview;
			}
		}
	}

	public void startDrawPreview(Dim2D previewS, Rect visiblePreview, int cameraDisplayOrientation) {
		state = state_pause;
		if(cameraPreviewDrawer == null) {
			cameraPreviewDrawer = new GLDrawerCameraPreview(previewS, visiblePreview, getWidth(), getHeight(), cameraDisplayOrientation);
			cameraPreviewDrawer.setRenderCallback(this);
			cameraPreviewDrawer.setEdgeRecognizeMode(edgeRecognizeMode);
			cameraPreviewDrawer.setEnhanceMode(enhanceMode);
			cameraPreviewDrawer.setPresetManager(presetManager);
		}
		state = state_preview;
		setEGLConfig();
	}

	public void setImageSaver(ImageSaver mImageSaver) {
		imageSaver = mImageSaver;
	}

	/**
	 * This method is called if a new image from the camera arrived. The camera delivers images in a yuv color format. It is converted to a black and
	 * white image with a size of 256x256 pixels (only a fraction of the resulting image is used). Afterwards Rendering the frame (in the main loop
	 * thread) is started by setting the newFrameLock to true.
	 */
	@Override
	public void onPreviewFrame(byte[] yuvs, Camera camera) {
		if(cameraPreviewDrawer != null)
			cameraPreviewDrawer.onPreview(yuvs);
	}
	
	public void prepareToTakePicture(final Dim2D size, final int imageRotation) { //TODO
//		l.i("prepareToTakePicture");
		if(edgeRecognizeMode == GlobalVariables.EDGE_MODE_AUTO) {
//			l.i("EDGE_MODE_AUTO"); 
			final Rectangle3D documentForm 	= cameraPreviewDrawer.prepareToCapturePicture(size, imageRotation); //need this to create glTexture from preview
			prepareReceivingImage = new Thread( new Runnable() {
				@Override
				public void run() {
					final int picWidth = size.width;
					final int picHeight = size.height;
					
					RectF visDocRect 			= DocumentCornerManager.minFitRect(cameraPreviewDrawer.getCorners());
					float visDocRatio 			= visDocRect.width()*visDocRect.height();
					DocumentPiecer piecer 		= new DocumentPiecer(picWidth, picHeight, presetManager, documentForm, visDocRatio);
					piecer.pieceDocumentForm(documentForm); 
					
					DocumentClipOrganizer textureOrganizer = new DocumentClipOrganizer(piecer.getPieces(), piecer.getTextureDimensions(), 
							picWidth, picHeight, piecer.documentWidth(), piecer.documentHeight());
					docDrawer 					= new GLDrawerDocument(textureOrganizer);
					docDrawer.setRenderCallback(CameraGLSurfaceView.this);	
				}
			});
			prepareReceivingImage.start();
			state = state_capture;
			setEGLConfig();
		}
		else if(edgeRecognizeMode == GlobalVariables.EDGE_MODE_MANUAL) {
//			l.i("EDGE_MODE_MANUAL");
			state = state_preview;
			prepareReceivingImage = new Thread( new Runnable() {
				@Override
				public void run() {
					cameraPreviewDrawer.prepareToChooseCorners(size);
				}
			});
			prepareReceivingImage.start();
			renderFrame();
		}
	}
	
	public void createGlEdgeDrawer(int rotation) {
		drawDocumentEdges = new GLDrawerDocumentEdge(getContext(), getWidth(), getHeight(), rotation);
	}
	
	public void scanImage(byte[] jpegData, int picWidth, int picHeight, final int imageRotation) {
//		long start = System.currentTimeMillis();
//		l.i("scanImage( cameraParameters, "+ picWidth +", "+ picHeight +", "+ imageRotation +" )");		
		if(prepareReceivingImage != null) {
			try {
				prepareReceivingImage.join();
				final Thread temp = prepareReceivingImage;
				prepareReceivingImage = null;
				temp.interrupt();			
			} catch (InterruptedException e) {
//				l.e("", e);
			}
		}
		
		if(edgeRecognizeMode == GlobalVariables.EDGE_MODE_AUTO) {
			state = state_scan_pers;
			docDrawer.setSourceImage(jpegData);
			cameraPreviewDrawer.setBlackBackGround();
			cameraPreviewDrawer.releasePreviewData();
			System.gc();
		}
		else {	
			if(edgeRecognizeMode == GlobalVariables.EDGE_MODE_MANUAL) {
				state = state_clear_gl;
				renderFrame();
				drawDocumentEdges.setPresetManager(presetManager);
				drawDocumentEdges.setCornerPoints(cameraPreviewDrawer.getCorners());
				drawDocumentEdges.setImageData(jpegData, picWidth, picHeight);
				drawDocumentEdges.setEnhanceMode(enhanceMode);
				drawDocumentEdges.setRenderCallback(new IRenderFrame() {
					@Override
					public void renderFrame() {
						requestRender();
					}
				});
				state = state_captute_manual;
				setEGLConfig();
				renderFrame();	
			}
			else {
				state = state_clear_gl;
				renderFrame();	
				if(enhanceMode == GlobalVariables.ENHANCE_MODE_NONE) {
					imageSaver.addImage(jpegData, picWidth, picHeight, 256, imageRotation);
					for (OnScanListener li : listeners) {
						li.onScanCompleted();
					}
				}
				else {
					final BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inScaled 		= false;
					opts.inDither		= false;           
					opts.inTempStorage	= new byte[32 * 1024]; 
					opts.inSampleSize 	= 1;
					opts.inPreferQualityOverSpeed = true;
					opts.inPreferredConfig = Config.RGB_565;
					final Bitmap img = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, opts);
					if(enhanceMode != GlobalVariables.ENHANCE_MODE_NONE){
						enhanceAndSave = new Thread(new Runnable() {
							@Override
							public void run() {
//								l.i("enhanceAndSave thread running");
								WhiteMethod whiteMethod = new WhiteMethod();
								for (OnScanListener l : listeners) {
									whiteMethod.addOnOnScanListener(l);
								}
								whiteMethod.imageWhiteMethod(img);
								for (OnScanListener li : listeners) {
									whiteMethod.removeOnOnScanListener(li);
									li.onScanCompleted();
								}
//								l.i("imageWhiteMethod ended and imageSaving should start");
								imageSaver.addImage(img, 256, imageRotation); 
							}
						});
						enhanceAndSave.start();
					}
				}
			}
		}

		renderFrame();
//		long duration = System.currentTimeMillis() - start;
//		l.i("image capture process took :"+ duration +"ms.");
//		l.e(SystemUtils.getMemoryInfo());
	}
	
	public void scanImage() {
		if(edgeRecognizeMode == GlobalVariables.EDGE_MODE_MANUAL) {
			int picWidth 				= drawDocumentEdges.getOrigWidth();
			int picHeight 				= drawDocumentEdges.getOrigHeight();
//			int imageRotation 			= drawDocumentEdges.getOrigRotation();
			byte[] jpegData 			= drawDocumentEdges.getPictureData();
			ArrayList<CornerPoint> cps 	= drawDocumentEdges.getCorners();
			TextureWrap tw 				= drawDocumentEdges.getCapturedFrame();	
//			l.i("TARGET: EDGE_MODE_MANUAL start scanning imageRotation: "+ imageRotation);		
//			l.i("cameraPreviewDrawer.prepareManualToScan before");
			Rectangle3D documentForm 	= cameraPreviewDrawer.prepareManualToScan(jpegData, picWidth, picHeight, cps, tw);
//			l.i("cameraPreviewDrawer.prepareManualToScan after");
			
			RectF visDocRect 			= DocumentCornerManager.minFitRect(cps);
			float visDocRatio 			= visDocRect.width()*visDocRect.height();
			DocumentPiecer piecer 		= new DocumentPiecer(picWidth, picHeight, presetManager, documentForm, visDocRatio); 
			piecer.pieceDocumentForm(documentForm);
			
			DocumentClipOrganizer textureOrganizer = new DocumentClipOrganizer(piecer.getPieces(), piecer.getTextureDimensions(), 
					picWidth, picHeight, piecer.documentWidth(), piecer.documentHeight());
			docDrawer 					= new GLDrawerDocument(textureOrganizer);
			docDrawer.setRenderCallback(CameraGLSurfaceView.this);	
			docDrawer.setSourceImage(jpegData);
//			l.i("docDrawer.setSourceImage after");
			
			cameraPreviewDrawer.setBlackBackGround();
			cameraPreviewDrawer.releasePreviewData();
	
			System.gc();
			state = state_scan_pers;
//			l.i("state = state_scan_pers");
//			setEGLConfig();
			renderFrame();
		}
	}
	
	public void addScanListener(OnScanListener l){
		listeners.add(l);
	}	
	public void removeScanListener(OnScanListener l){
		listeners.remove(l);
	}
	public void removeAllListeners(){
		listeners.clear();
	}
	
	public void showDocumentCreatedAnimation() {}
	
	public void resumePreview(Dim2D ps, Rect visiblePreviewRect, int orientation) {
		clearLastScan();
		startDrawPreview(ps, visiblePreviewRect, orientation);
		cameraPreviewDrawer.resumePreview(ps, visiblePreviewRect);
		state = state_preview;
		setEGLConfig();
	}
	
	public Rect getFocusArea(){
		return cameraPreviewDrawer.getFocusArea();
	}
	
	public void setDocumentEdgeListener(OnDocumentEdgesListener li){
		if(cameraPreviewDrawer != null){
			cameraPreviewDrawer.setDocumentEdgeListener(li);
		}
		else {
//			l.e("TARGET ERROR NO DRAWER TO CONNECT DOCUMENTEDGELISTENER");
		}
	}

	public void setPresetManager(PresetManager presetManager) {
		this.presetManager  = presetManager;
		if(drawDocumentEdges != null) {
			drawDocumentEdges.setPresetManager(presetManager);
		}
		if(cameraPreviewDrawer != null) {
			cameraPreviewDrawer.setPresetManager(presetManager);
		}
	}
	public void setCameraPreviewOrientation(int captureImageRotation) {
		if(cameraPreviewDrawer != null) {
			cameraPreviewDrawer.setCameraPreviewOrientation(captureImageRotation);
		}
	}
	public OnCornerTouchListener getOnCornerTouchListener() {
		return drawDocumentEdges;
	}
	
}