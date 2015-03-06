package com.example.example;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;

import com.haave.docscanner.recognize.documentpresets.PresetManager;
import com.norfello.android.graphics.Dim2D;

public class DocScannerPreferences {
//	private static DebugLog l = new DebugLog(DocScannerPreferences.class.getSimpleName());
	private final String APP_PREF_KEY = "com.haave.docscanner.preferences";
	private final String PREF_KEY_FIRST_RUN = "firstrun";
	private final String PREF_KEY_FOLDER_SIMG_PATH 	= "source_image_folder_path";
	private final String PREF_KEY_FOLDER_THUMB_PATH = "thumbnails_folder_path";
	private final String PREF_KEY_APP_FOLDER_PATH	= "application_folder_path";
	private final String PREF_KEY_FOLDER_TEMP_PATH	= "temp_folder_path";
	
	public static final int CAMERA_NO_SUPPORT = -1;
	
	private final String PREF_KEY_FIRST_RUN_CAMERA = "firstruncamera";
	private final String PREF_KEY_CAMERA_EDGEMODES_ = "camera_edge_mode_";
	private final String PREF_KEY_CAMERA_EDGEMODES_COUNT = "camera_edge_count";
	private final String PREF_KEY_CAMERA_EDGEMODES_USED = "camera_edge_used";
	private final String PREF_KEY_CAMERA_ENHANCE_ = "enhance_mode_";
	private final String PREF_KEY_CAMERA_ENHANCE_COUNT = "enhance_mode_count";
	private final String PREF_KEY_CAMERA_ENHANCE_USED = "enhance_used";
	private final String PREF_KEY_CAMERA_FLASHMODE = "camera_flashmode";
	private final String PREF_KEY_CAMERA_SCANPROFILE_ID = "camera_scanprofile_id";
	private final String PREF_KEY_CAMERA_SIZE_WIDTH = "camera_preview_width";
	private final String PREF_KEY_CAMERA_SIZE_HEIGHT = "camera_preview_height";
	private final String PREF_KEY_CAMERA_COLOREFFECT_ = "camera_coloreffect_";
	private final String PREF_KEY_CAMERA_COLOREFFECT_COUNT = "camera_coloreffect_count";
	private final String PREF_KEY_CAMERA_COLOREFFECT_USED = "camera_coloreffect_used";
	private final String PREF_KEY_CAMERA_FLASHMODE_ = "camera_flashmodes_";
	private final String PREF_KEY_CAMERA_FLASHMODE_COUNT = "camera_flashmodes_count";
	private final String PREF_KEY_CAMERA_FLASHMODE_USED = "camera_flashmode_used";
	private final String PREF_KEY_CAMERA_FOCUSMODE_USED = "camera_focusmode_used";
	private final String PREF_KEY_CAMERA_PICTUREFORMAT_USED = "camera_pictureformat_used";
	
	private final String PREF_KEY_SHARE_APP_DATA_QUICK1 = "share_app_data_quick1";
	private final String PREF_KEY_SHARE_APP_DATA_QUICK2 = "share_app_data_quick2";
	private final String PREF_KEY_SHARE_APP_DATA_QUICK3 = "share_app_data_quick3";
	
	private final String PREF_KEY_EVERNOTE_USERNAME = "evernote_username";
	private final String PREF_KEY_EVERNOTE_PASSWORD = "evernote_password";
	private final String PREF_KEY_DOCUMENT_EVERNOTE_NOTEBOOK = "document_evernote_notebook";
	private final String PREF_KEY_BUSINESS_EVERNOTE_NOTEBOOK = "business_evernote_notebook";
	private final String PREF_KEY_RECEIPT_EVERNOTE_NOTEBOOK = "receipt_evernote_notebook";
	private final String PREF_KEY_WHITEBOARD_EVERNOTE_NOTEBOOK = "whiteboard_evernote_notebook";
	private final String PREF_KEY_MISC_EVERNOTE_NOTEBOOK = "misc_evernote_notebook";
	
	private final String PREF_KEY_LAST_DOC_PAGE_ID			= "last_document_page";
	private final String PREF_KEY_LAST_PRESET_ID		= "last_preset";
	
	private SharedPreferences preferences;
	private Editor prefEditor;
	
	public DocScannerPreferences(Context context) {
		preferences = context.getSharedPreferences(APP_PREF_KEY, Context.MODE_PRIVATE);
		prefEditor = preferences.edit();
	}
	
	public boolean isFirstRun() {
		return preferences.getBoolean(PREF_KEY_FIRST_RUN, true);
	}
	public void setFirstRun() {
		prefEditor.putBoolean(PREF_KEY_FIRST_RUN, false);
		prefEditor.commit();
	}
	

	public void setAppFolderPaths(String sourceImgFolderPath, String thumbNailFolderPath, String appFolderPath, String tempFolderPath) {
		prefEditor.putString(PREF_KEY_FOLDER_SIMG_PATH, sourceImgFolderPath);
		prefEditor.putString(PREF_KEY_FOLDER_THUMB_PATH, thumbNailFolderPath);
		prefEditor.putString(PREF_KEY_FOLDER_TEMP_PATH, tempFolderPath);
		prefEditor.putString(PREF_KEY_APP_FOLDER_PATH, appFolderPath);
		prefEditor.commit();
		GlobalVariables.sourceImgFolderPath 	= sourceImgFolderPath;
		GlobalVariables.thumbNailForlderPath 	= thumbNailFolderPath;
		GlobalVariables.tempFolder				= tempFolderPath;
		GlobalVariables.appFolder				= appFolderPath;
	}
	
	public String getSourceImgFolderPath() {
		return preferences.getString(PREF_KEY_FOLDER_SIMG_PATH, "");
	}
	public String getThumbnailsFolderPath() {
		return preferences.getString(PREF_KEY_FOLDER_THUMB_PATH, "");
	}
	public String getApplicationFolderPath() {
		return preferences.getString(PREF_KEY_APP_FOLDER_PATH, "");
	}
	public String getTempFolderPath() {
		return preferences.getString(PREF_KEY_FOLDER_TEMP_PATH, "");
	}
	
	/**
	 * Returns edge mode index from all possible edge modes. 
	 * return values can be interpreted with class  {@link #GlobalVariables}
	 * <br/><br/>
	 * EDGE_MODE_AUTO = 0;<br/>
	 * EDGE_MODE_MANUAL = 1;<br/>
	 * EDGE_MODE_NONE = 2;<br/>
	 * @return
	 */
	public int getEdgeRecognizeModeIndex(){
		return preferences.getInt(PREF_KEY_CAMERA_EDGEMODES_USED, 0);
	}
	/**
	 * set used edge mode index from all possible edge modes. 
	 * set parameter can be interpreted with class  {@link #GlobalVariables}
	 * <br/><br/>
	 * EDGE_MODE_AUTO = 0;<br/>
	 * EDGE_MODE_MANUAL = 1;<br/>
	 * EDGE_MODE_NONE = 2;<br/>
	 * @return
	 */
	public void setUsedEdgeRecognizeModeIndex(int mode){
		prefEditor.putInt(PREF_KEY_CAMERA_EDGEMODES_USED, mode);
		prefEditor.commit();
	}
	public void createEdgeRecognizeModes(){ //corresponding values in GlobalVariables
		prefEditor.putString(PREF_KEY_CAMERA_EDGEMODES_+ 0, "auto");
		prefEditor.putString(PREF_KEY_CAMERA_EDGEMODES_+ 1, "manual");
		prefEditor.putString(PREF_KEY_CAMERA_EDGEMODES_+ 2, "none");
		prefEditor.putInt(PREF_KEY_CAMERA_EDGEMODES_COUNT, 3);
		prefEditor.commit();
	}
	
	/**
	 * Returns enhance mode index from all possible enhance modes. 
	 * return values can be interpreted with class  {@link #GlobalVariables}
	 * <br/><br/>
	 * ENHANCE = 0 <br/>
	 * ENHANCE_NONE = 1 <br/>
	 * @return
	 */
	public int getEnhanceModeIndex(){
		return preferences.getInt(PREF_KEY_CAMERA_ENHANCE_USED, 0);
	}
	/**
	 * set used enhance mode index from all possible enhance modes. 
	 * set parameter can be interpreted with class  {@link #GlobalVariables}
	 * <br/><br/>
	 * ENHANCE = 0 <br/>
	 * ENHANCE_NONE = 1 <br/>
	 * @return
	 */
	public void setUsedEnhanceModeIndex(int mode){
		prefEditor.putInt(PREF_KEY_CAMERA_ENHANCE_USED, mode);
		prefEditor.commit();
	}
	public void createEnhanceModes(){ //corresponding values in GlobalVariables
		prefEditor.putString(PREF_KEY_CAMERA_ENHANCE_+ 0, "enhance");
		prefEditor.putString(PREF_KEY_CAMERA_ENHANCE_+ 1, "none");
		prefEditor.putInt(PREF_KEY_CAMERA_ENHANCE_COUNT, 2);
		prefEditor.commit();
	}
	
	public int getCameraFlashMode() {
		int flashmode = preferences.getInt(PREF_KEY_CAMERA_FLASHMODE, 1);
//		l.i("getCameraFlashMode()-> "+ flashmode);
		return flashmode;
	}
	public void setCameraFlashMode(int value) {
//		l.i("setCameraFlashMode( "+ value +")");
		prefEditor.putInt(PREF_KEY_CAMERA_FLASHMODE, value);
		prefEditor.commit();
	}

	public int getScanProfileID() {
		int scanProfileID = preferences.getInt(PREF_KEY_CAMERA_SCANPROFILE_ID, -1); //-1 means no profile aka custom profile
//		l.i("getScanProfileID()-> "+ scanProfileID);
		return scanProfileID;
	}
	
	public void setScanProfileID(int id) {
//		l.i("setScanProfileID( "+ id +")");
		prefEditor.putInt(PREF_KEY_CAMERA_SCANPROFILE_ID, id);
		prefEditor.commit();
	}

	public boolean isFirstRunCamera() {
		return preferences.getBoolean(PREF_KEY_FIRST_RUN_CAMERA, true);
	}
	public void setFirstRunCamera() {
		prefEditor.putBoolean(PREF_KEY_FIRST_RUN_CAMERA, false);
		prefEditor.commit();
	}

	public void setPreviewSize(Dim2D size) {
		prefEditor.putInt(PREF_KEY_CAMERA_SIZE_WIDTH, size.width);
		prefEditor.putInt(PREF_KEY_CAMERA_SIZE_HEIGHT, size.height);
		prefEditor.commit();
	}
	
	public Dim2D getPreviewSize() {
		int w = preferences.getInt(PREF_KEY_CAMERA_SIZE_WIDTH, 0);
		int h = preferences.getInt(PREF_KEY_CAMERA_SIZE_HEIGHT, 0);
		return new Dim2D(w, h);
	}

	public void setColorEffects(List<String> colorEffects) {
//		l.i("setColorEffects() count: "+ colorEffects.size());
		int index = 0;
		for (String effect : colorEffects) {
			if(effect.equals(Camera.Parameters.EFFECT_MONO)) {
				index = 1;
			}
			else if(effect.equals(Camera.Parameters.EFFECT_NEGATIVE)) {
				index = 2;
			}
			else if(effect.equals(Camera.Parameters.EFFECT_NONE)) {
				index = 0;
			}
			prefEditor.putString(PREF_KEY_CAMERA_COLOREFFECT_+index, effect);
		}
//		l.i("setColorEffects() index: "+ index);
		prefEditor.putInt(PREF_KEY_CAMERA_COLOREFFECT_COUNT, index);
		prefEditor.commit();
	}
	
	/**
	 *-1 - NO CAMERA EFFECTS SUPPORTED!!!<br/>
	 * 0 - Camera.Parameters.EFFECT_NONE<br/>
	 * 1 - Camera.Parameters.EFFECT_MONO (black and white)<br/>
	 * 2 - Camera.Parameters.EFFECT_NEGATIVE<br/>
	 * @param index
	 */
	public void setUsedColorEffect(int index) {
//		l.i("setUsedColorEffect("+ index +")");
		prefEditor.putInt(PREF_KEY_CAMERA_COLOREFFECT_USED, index);
		prefEditor.commit();
	}
	/**
	 * Uses camera these camera color effects index points to one of these
	 * Color effects
	 * <br/><br/>
	 *-1 - NO CAMERA EFFECTS SUPPORTED!!!<br/>
	 * 0 - Camera.Parameters.EFFECT_NONE<br/>
	 * 1 - Camera.Parameters.EFFECT_MONO (black and white)<br/>
	 * 2 - Camera.Parameters.EFFECT_NEGATIVE<br/>
	 * @return index
	 */
	public int getUsedColorEffectIndex() {
		return preferences.getInt(PREF_KEY_CAMERA_COLOREFFECT_USED, 0);
	}
	/**
	 * Returns used camera color effects name
	 * <br/><br/>
	 *-1 - NO CAMERA EFFECTS SUPPORTED!!!<br/>
	 * 0 - Camera.Parameters.EFFECT_NONE<br/>
	 * 1 - Camera.Parameters.EFFECT_MONO (black and white)<br/>
	 * 2 - Camera.Parameters.EFFECT_NEGATIVE<br/>
	 * @return index
	 */
	public String getColorEffect() {
		int index = getUsedColorEffectIndex();
		if(index == -1) {
			return null;
		}
		return preferences.getString(PREF_KEY_CAMERA_COLOREFFECT_+ index, Camera.Parameters.EFFECT_NONE);
	}

	public void setFlashModes(List<String> flashModes) {
		for (String flashMode : flashModes) {
			if(flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
				prefEditor.putString(PREF_KEY_CAMERA_FLASHMODE_+ 0, flashMode);
			}
			else if(flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
				prefEditor.putString(PREF_KEY_CAMERA_FLASHMODE_+ 1, flashMode);
			}
			else if(flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
				prefEditor.putString(PREF_KEY_CAMERA_FLASHMODE_+ 2, flashMode);
			}
		}
		prefEditor.putInt(PREF_KEY_CAMERA_FLASHMODE_COUNT, 3);
		prefEditor.commit();
	}
	
	public void setUsedFlashMode(int usedFlashMode) {
		prefEditor.putInt(PREF_KEY_CAMERA_FLASHMODE_USED, usedFlashMode);
		prefEditor.commit();
	}
	public int getUsedFlashModeIndex() {
		return preferences.getInt(PREF_KEY_CAMERA_FLASHMODE_USED, 0);
	}
	public String getFlashMode() {
		int index = getUsedFlashModeIndex();
		if(index == -1) {
			return null;
		}
		return preferences.getString(PREF_KEY_CAMERA_FLASHMODE_+ index, Camera.Parameters.FLASH_MODE_OFF);
	}
	
	public void setFocusMode(String focusMode) {
		prefEditor.putString(PREF_KEY_CAMERA_FOCUSMODE_USED, focusMode);
		prefEditor.commit();
	}
	public String getFocusMode() {
		return preferences.getString(PREF_KEY_CAMERA_FOCUSMODE_USED, "");
	}
	
	
	public void setPictureFormat(int pictureFormat) {
		prefEditor.putInt(PREF_KEY_CAMERA_PICTUREFORMAT_USED, pictureFormat);
		prefEditor.commit();
	}	
	public int getPictureFormat() {
		return preferences.getInt(PREF_KEY_CAMERA_PICTUREFORMAT_USED, 0);
	}

	public String getSendOptionQuickSlot1ClassName() {
		return preferences.getString(PREF_KEY_SHARE_APP_DATA_QUICK1, null);
	}
	public String getSendOptionQuickSlot2ClassName() {
		return preferences.getString(PREF_KEY_SHARE_APP_DATA_QUICK2, null);
	}
	public String getSendOptionQuickSlot3ClassName() {
		return preferences.getString(PREF_KEY_SHARE_APP_DATA_QUICK3, null);
	}
	
	public void setSendOptionQuickSlot1ClassName(String className) {
		prefEditor.putString(PREF_KEY_SHARE_APP_DATA_QUICK1, className);
		prefEditor.commit();
	}
	public void setSendOptionQuickSlot2ClassName(String className) {
		prefEditor.putString(PREF_KEY_SHARE_APP_DATA_QUICK2, className);
		prefEditor.commit();
	}
	public void setSendOptionQuickSlot3ClassName(String className) {
		prefEditor.putString(PREF_KEY_SHARE_APP_DATA_QUICK3, className);
		prefEditor.commit();
	}

	public String getEvernoteUserName() {
		return preferences.getString(PREF_KEY_EVERNOTE_USERNAME, null);
	}
	public void setEvernoteUserName(String userName) {
		prefEditor.putString(PREF_KEY_EVERNOTE_USERNAME, userName);
		prefEditor.commit();
	}

	public String getEvernotePassWD() {
		return preferences.getString(PREF_KEY_EVERNOTE_PASSWORD, null);
	}
	public void setEvernotePassWord(String passWord) {
		prefEditor.putString(PREF_KEY_EVERNOTE_PASSWORD, passWord);
		prefEditor.commit();
	}
	
	public String getEvernoteDocumentSendNoteBook() {
		return preferences.getString(PREF_KEY_DOCUMENT_EVERNOTE_NOTEBOOK, null);
	}
	public void setEvernoteDocumentSendNoteBook(String notebook) {
		prefEditor.putString(PREF_KEY_DOCUMENT_EVERNOTE_NOTEBOOK, notebook);
		prefEditor.commit();
	}
	public String getEvernoteBusinessSendNoteBook() {
		return preferences.getString(PREF_KEY_BUSINESS_EVERNOTE_NOTEBOOK, null);
	}
	public void setEvernoteBusinessSendNoteBook(String notebook) {
		prefEditor.putString(PREF_KEY_BUSINESS_EVERNOTE_NOTEBOOK, notebook);
		prefEditor.commit();
	}
	public String getEvernoteReceiptSendNoteBook() {
		return preferences.getString(PREF_KEY_RECEIPT_EVERNOTE_NOTEBOOK, null);
	}
	public void setEvernoteReceiptSendNoteBook(String notebook) {
		prefEditor.putString(PREF_KEY_RECEIPT_EVERNOTE_NOTEBOOK, notebook);
		prefEditor.commit();
	}
	public String getEvernoteWhiteboardSendNoteBook() {
		return preferences.getString(PREF_KEY_WHITEBOARD_EVERNOTE_NOTEBOOK, null);
	}
	public void setEvernoteWhiteboardSendNoteBook(String notebook) {
		prefEditor.putString(PREF_KEY_WHITEBOARD_EVERNOTE_NOTEBOOK, notebook);
		prefEditor.commit();
	}
	public String getEvernoteMiscSendNoteBook() {
		return preferences.getString(PREF_KEY_MISC_EVERNOTE_NOTEBOOK, null);
	}
	public void setEvernoteMiscSendNoteBook(String notebook) {
		prefEditor.putString(PREF_KEY_MISC_EVERNOTE_NOTEBOOK, notebook);
		prefEditor.commit();
	}
	
	public int getLastDocumentPageID() {
		return preferences.getInt(PREF_KEY_LAST_DOC_PAGE_ID, -1);
	}
	public void setLastDocumentPageID(int pageID) {
		prefEditor.putInt(PREF_KEY_LAST_DOC_PAGE_ID, pageID);
		prefEditor.commit();
	}
	public int getLastPreset() {
		return preferences.getInt(PREF_KEY_LAST_PRESET_ID, PresetManager.PRESET_DOCUMENT);
	}
	public void setLastPreset(int pageID) {
		prefEditor.putInt(PREF_KEY_LAST_PRESET_ID, pageID);
		prefEditor.commit();
	}
}