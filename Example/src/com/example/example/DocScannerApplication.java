package com.example.example;

import java.io.File;

import android.app.Application;

import com.norfello.android.exceptions.FileCreationException;
import com.norfello.android.utils.FileUtils;

public class DocScannerApplication extends Application {
//	private final DebugLog l = new DebugLog(DocScannerApplication.class.getSimpleName());
	private DocScannerPreferences preferenceM;
	
	@Override
	public void onCreate() {
//		l.i("onCreate()");
		super.onCreate();
		initApplication();
//		l.i("onCreate() finished");
	}


	/**
	 * Do all application initialization here. If initialization fails then you can call it from somewhere else
	 * in application so that before launch activity is created application init is always possible to execute 
	 */
	public void initApplication() {
//		l.i("initApplication()");
		preferenceM = new DocScannerPreferences(getApplicationContext());
		GlobalVariables.densityScale = getResources().getDisplayMetrics().density;
		if(preferenceM.isFirstRun()) {
//			l.i("is FIRST RUN");
			try {
				firstStartApplicationInit();
				preferenceM.setFirstRun();//first time application has run
			} catch (FileCreationException e) {
//				l.e("APPLICATION FIRST RUN FAILED", e);
			}
		}
		else {
			GlobalVariables.sourceImgFolderPath 	= preferenceM.getSourceImgFolderPath();
			GlobalVariables.thumbNailForlderPath 	= preferenceM.getThumbnailsFolderPath();
			GlobalVariables.tempFolder 				= preferenceM.getTempFolderPath();
			GlobalVariables.appFolder			 	= preferenceM.getApplicationFolderPath();
			
			Thread cleanUp = new Thread(new Runnable() {
				@Override
				public void run() {
					File tempFolder = new File(GlobalVariables.tempFolder);
					String[] filePaths = tempFolder.list();
					int count = filePaths.length;
					if(count > 0) {
//						l.d("Deleting temp folder files");
						for (int i = 0; i < count; i++) {
							new File(tempFolder, filePaths[i]).delete();
						}
					}
				}
			});
			cleanUp.start();
		}
	}


	/**
	 * is used only once when application is started for the first time. 
	 * Creates folder structures etc.
	 * @throws FileCreationException 
	 */
	private void firstStartApplicationInit() throws FileCreationException{
//		l.i("firstStartApplicationInit()");
		File appFolder 			= FileUtils.createAppFolder(getApplicationContext());
		File tempFolder			= FileUtils.createPrivateTempFileFolder(getApplicationContext());
		File sourceImgFolder 	= FileUtils.createPrivateImageFolder(getApplicationContext(), "sourceImages");
		File thumbNailForlder 	= FileUtils.createPrivateImageFolder(getApplicationContext(), "thumbNails");
		
		GlobalVariables.sourceImgFolderPath 	= sourceImgFolder.getAbsolutePath();
		GlobalVariables.thumbNailForlderPath 	= thumbNailForlder.getAbsolutePath();
		GlobalVariables.appFolder				= appFolder.getAbsolutePath();
		GlobalVariables.tempFolder				= tempFolder.getAbsolutePath();
		
		preferenceM.setAppFolderPaths(GlobalVariables.sourceImgFolderPath, GlobalVariables.thumbNailForlderPath, GlobalVariables.appFolder, GlobalVariables.tempFolder);
		preferenceM.createEdgeRecognizeModes();
		preferenceM.createEnhanceModes();
		
	//	folder structure
	//	loading screen
	//	images storage
	//	thumb nail creation
		
	}
	
	public DocScannerPreferences getApplicationPreferences() {
		return preferenceM;
	}	
}