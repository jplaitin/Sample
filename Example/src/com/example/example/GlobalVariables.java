package com.example.example;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;

import com.haave.docscanner.db.DataBaseAccessor;
import com.haave.docscanner.db.DocumentFolder;
import com.haave.docscanner.db.DocumentPage;
import com.haave.docscanner.evernote.EvernoteSender;
import com.haave.docscanner.recognize.documentpresets.PresetManager;


public class GlobalVariables {

	public static String[] notebooks = null;
	private static final long NOTEBOOK_REFRESH_LIMIT = 1000 * 60 * 15; //15min
	private static long lastNotebookQuery = 0;
	private static final long EVERNOTE_REFRESH_LIMIT = 1000 * 60 * 5; //5min
	private static long lastEvernoteCheck = 0;
	
	public static String sourceImgFolderPath = null;
	public static String thumbNailForlderPath = null;
	public static String appFolder = null;
	public static String tempFolder = null;
	public static float densityScale = 1.0f;
	public static int currentFolderID = DocumentFolder.ROOT_FOLDER;
	public static int currentPresetType = PresetManager.PRESET_MISC;
	public static int lastPageID = DocumentPage.NEW_DOCUMENT_PAGE;

	/**
	 * Used to define edge recognize mode in class {@link DocScannerPreferences} 
	 */
	public static final int EDGE_MODE_AUTO = 0;
	public static final int EDGE_MODE_MANUAL = 1;
	public static final int EDGE_MODE_NONE = 2;
	
	public static final int ENHANCE_MODE_ENHANCE = 0;
	public static final int ENHANCE_MODE_NONE = 1;
	
	/**
	 * Remember to call this only from activity that 
	 * @param activity
	 */
	public static void loadNotebooks(Activity activity) {
		boolean doQuery = false;
		if(lastNotebookQuery == 0) {
			lastNotebookQuery = System.currentTimeMillis();
			doQuery = true;
		}
		else if(lastNotebookQuery - System.currentTimeMillis() > NOTEBOOK_REFRESH_LIMIT){
			lastNotebookQuery = 0;
		}
		if(!doQuery) return;
		
		DocScannerPreferences mPref = ((DocScannerApplication)activity.getApplication()).getApplicationPreferences();
		String evUser 	= mPref.getEvernoteUserName();
		String evPasswd = mPref.getEvernotePassWD();
		if(evUser != null && evPasswd != null) {
			EvernoteSender.setAccount(evUser, evPasswd);
	    	EvernoteSender.queryNotebooks();
		}
	}
	
	public static void reCheckEvernoteStatus(final Activity activity) {
		boolean doQuery = false;
		if(lastEvernoteCheck == 0) {
			lastEvernoteCheck = System.currentTimeMillis();
			doQuery = true;
		}
		else if(lastEvernoteCheck - System.currentTimeMillis() > EVERNOTE_REFRESH_LIMIT){
			lastEvernoteCheck = 0;
		}
		if(!doQuery) return;
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				PresetManager pm = new PresetManager(activity);
				DataBaseAccessor dbAccessor = new DataBaseAccessor(activity);
				Cursor c = dbAccessor.getFailedEvernoteSendDocuments();
				Log.i("debug", "GlobalVariables: reCheckEvernoteStatus column count : "+ c.getCount() );
				
				while(c.moveToNext()) {
					int preset = c.getInt(c.getColumnIndex(DataBaseAccessor.DOC_C_PRESET));
					String notebook = pm.getNoteBook(preset);
					String presetName = pm.getPresetName(preset);
					int docId = c.getInt(c.getColumnIndex(DataBaseAccessor.DOC_C_ID));
					String path = c.getString(c.getColumnIndex(DataBaseAccessor.DOC_C_DOC_PATH));
										
					EvernoteSender.sendFile(path, docId, notebook, presetName); 

				}
				c.close();
				dbAccessor.close();
			}
		});
		t.start();
	}
	
}
