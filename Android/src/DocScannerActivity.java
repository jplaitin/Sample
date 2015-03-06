package com.example.example;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.apps.analytics.easytracking.EasyTracker;
import com.haave.docscanner.db.DataBaseAccessor;
import com.haave.docscanner.db.DocumentPage;
import com.haave.docscanner.evernote.EvernoteSender;

public abstract class DocScannerActivity extends Activity implements EvernoteSender.EvernoteCallback {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EasyTracker.getTracker().setContext(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getTracker().trackActivityStart(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getTracker().trackActivityStop(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		EvernoteSender.addCallback(this);
		GlobalVariables.loadNotebooks(this);
		GlobalVariables.reCheckEvernoteStatus(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		EvernoteSender.removeCallback(this);
	}

	@Override
	public void onEvernoteAccountValid(String userName, String passWD) {}

	@Override
	public void onEvernoteFinishedSending(String file_name, int doc_id) {
//		Log.i("debug", "DocScannerActivity, onEvernoteFinishedSending");
		final DataBaseAccessor dbAcces = new DataBaseAccessor(this);
		dbAcces.changeDocumentEvernoteStatus(doc_id, DocumentPage.EVERNOTE_STATUS_SENT);
		dbAcces.close();
	}

	@Override
	public void onEvernoteNotebooks(String[] notebooks) {
//		Log.e("debug", "onEvernoteNotebooks 1");
		GlobalVariables.notebooks = notebooks;
//		Log.e("debug", "onEvernoteNotebooks 2");
	}

	@Override
	public void onEvernoteError(String file_name, int doc_id, String error) {}
	
	/**
	 * This method is deprecated in Android 3.0 (Honeycomb) and later, but GoogleAnalytics support goes back to Android 1.5 and therefore cannot use
	 * the Fragment API.
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		Object o = super.onRetainNonConfigurationInstance();
		EasyTracker.getTracker().trackActivityRetainNonConfigurationInstance();
		return o;
	}
}
