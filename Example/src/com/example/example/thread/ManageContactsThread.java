package com.example.example.thread;

import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;

import com.haave.contactstash.db.DataBaseAccessor;
import com.haave.contactstash.http.ContactStashConnections;
import com.haave.contactstash.http.ContactStashConnections.ContactStashCallback;
import com.haave.contactstash.http.DataContainerContact;
import com.haave.contactstash.http.JSONObjectWrapper;
import com.norfello.android.utils.DebugLog;

public class ManageContactsThread extends Thread implements ContactStashCallback {
	private static final DebugLog l = new DebugLog(ManageContactsThread.class.getSimpleName());
	private volatile Thread runner;
	private long lastUpdate = 0; 
	private ManageContactListener listener;

	/**
	 * Time that thread waits until checks if there is more work to do This value is in seconds. Remember c2dm service stops waiting. 10 min
	 */
	private static final int WAIT_SHORT = 15;// 600;
	/**
	 * Time that thread waits until checks if there is more work to do This value is in seconds. Remember c2dm service stops waiting. 30 min
	 */
	private static final int WAIT_LONG = 1800;
	/**
	 * Update time 10 min
	 */
	private static final long WAIT_UPDATE_INTERVAL = 1000*60; //*10; 

	/**
	 * When no work to be done thread goes to sleep
	 */
	private static final int WAIT_MODE_NORMAL = 1;
	/**
	 * After upload url is received
	 */
	private static final int WAIT_MODE_URLOAD = 2;
	/**
	 * Waiting for scan result to get back to work
	 */
	private static final int WAIT_MODE_SCANRESULT = 3;
	/**
	 * Waiting for back end to start to work so that thread can get back to work :)
	 */
	private static final int WAIT_MODE_BACKEND_DOWN = 4;

	private Vector<DataContainerContact> contactList;
	private ContactStashConnections contactStashCon;
	private DataBaseAccessor dbAccessor;
	private int waitMode;
	private boolean isPaused 		= false;
	private boolean updateRound 	= true;

	private final ReentrantLock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private String nextScanUploadUrl;
	private String nextScanAuth;

	public ManageContactsThread(ContactStashConnections contactStashCon) {
		super();
		waitMode = WAIT_MODE_NORMAL;
		this.contactStashCon = contactStashCon;
		if (contactStashCon != null) {
			synchronized (contactStashCon) {
				ContactStashConnections.addCallback(this);
			}
		}
		// l.turnOff();
		// l.i("MyApplication onCreate - App APID: " + pushId);
		// l.e(uploader.getUserAgent());
	}

	public void setDataBaseAccessor(DataBaseAccessor dbAccessor) {
//		l.i("setDataBaseAccessor: "+ dbAccessor);
		this.dbAccessor = dbAccessor;
	}

	public synchronized void startThread() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public synchronized void stopThread() {
		if (runner != null) {
			if (contactStashCon != null) {
				synchronized (contactStashCon) {
					ContactStashConnections.removeCallback(this);
				}
			}
			Thread moribund = runner;
			runner = null;
			moribund.interrupt();
		}
	}

	@Override
	public void run() {
//		 l.i("thread started");
		while (Thread.currentThread() == runner) {
			lock.lock();
			if (dbAccessor != null) {
				contactList = dbAccessor.getContactsData();
			} else {
				contactList = null;
			}
			lock.unlock();
			while (!allCardsFinished() && Thread.currentThread() == runner) {
				// l.i("cardsNotFinished looping");
				lock.lock();
				if (dbAccessor != null) {
					contactList = dbAccessor.getContactsData();
				} else {
					contactList = null;
					break;
				}
				lock.unlock();
				for (DataContainerContact item : contactList) {
					// l.i("contactlist copy looping");
					if (Thread.currentThread() != runner) {
						break;
					}
					if (item.data.processing_status.equals(DataBaseAccessor.STATUS_SEND_IMAGE)) {
						l.v("run->sentToServerForScan(item): ");
						waitMode = WAIT_MODE_SCANRESULT;
						sentToServerForScan(item); //TODO
					} else if(item.data.processing_status.equals(DataBaseAccessor.STATUS_UPDATE) ) {
						l.d("run->updateContactInformation(item) CID: "+ item.contactstash_id);
						waitMode = WAIT_MODE_SCANRESULT;
						updateContactInformation(item);
					} else if(item.data.processing_status.equals(DataBaseAccessor.STATUS_DELETE) ) {
						l.d("run->updateContactInformation(item) CID: "+ item.contactstash_id);
						waitMode = WAIT_MODE_SCANRESULT;
						deleteContactInformation(item);
					} else if (!(item.data.processing_status.equals(DataBaseAccessor.STATUS_RECOGNIZED) || item.data.processing_status.equals(DataBaseAccessor.STATUS_UNRECOGNIZABLE)) ) {
						l.v("run->getContactInfromation(item)  CID: "+ item.contactstash_id);
						if(item.contactstash_id != 0) {
							getContactInfromation(item);
							waitingShortTime();
						}
						else if(item.contactstash_id == 0){
							item.deleteFromDB(dbAccessor);
							if(listener != null) {
								listener.contactsChanged();
							}
						}
					} else if(updateRound) {
//						l.v("UPDATEROUND run->getContactInfromation(item) RETRIEVE_STATUS");
						if(item.contactstash_id != 0) {
							getContactInfromation(item);
							waitingShortTime();
						}
					}
				}
				lock.lock();
//				l.i("thread loop ended waitMode: "+ waitMode);
				try {
					if (waitMode == WAIT_MODE_SCANRESULT) {
						waitMode = WAIT_MODE_NORMAL;
//						l.i("thread is waiting " + WAIT_SHORT + " seconds");
						condition.await(WAIT_SHORT, TimeUnit.SECONDS);
					} else if (waitMode == WAIT_MODE_BACKEND_DOWN) {
						waitMode = WAIT_MODE_NORMAL;
//						l.e("BACKEND IS DOWN WAITING FOR " + WAIT_LONG + "seconds");
						condition.await(WAIT_LONG, TimeUnit.SECONDS);
					} 
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					lock.unlock();
				}
			}
			lock.lock();
			try {
//				l.i("thread is waiting indefinitely");
				condition.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				lock.unlock();
			}
		}
//		l.i("thread run loop finished");
	}


	private void waitingShortTime() {
		lock.lock();
		try {
			condition.await(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} // prevent httprequest from getting to crowded
		lock.unlock();
	}

	/**
	 * If One of the List's ContactListItem statuses is SEND_STATUS, RETRIEVE_STATUS or RECEIVED_STATUS and application has not recently (in 30
	 * seconds hard coded) asked scan results
	 * 
	 * @return
	 */
	private synchronized boolean allCardsFinished() {
		if (contactList == null) {
			waitMode = WAIT_MODE_SCANRESULT;
			return true;
		}
		for (DataContainerContact item : contactList) {
			if (isPaused) {
//				l.v("thread is paused");
				waitMode 		= WAIT_MODE_NORMAL;
				break;
			}
			String status 		= item.data.processing_status;
//			l.v("ID: "+ item.id +" cID: "+ item.contactstash_id +" status: "+ item.data.processing_status);
			Long timeDelta 		= lastUpdate - System.currentTimeMillis();
			updateRound 		=  timeDelta > WAIT_UPDATE_INTERVAL;
			if(updateRound) {
				return false;
			}
			
			if (!(status.equals(DataBaseAccessor.STATUS_RECOGNIZED) || status.equals(DataBaseAccessor.STATUS_UNRECOGNIZABLE))) { //last to escape loop and load new contact from database
				return false;
			}
		}
		return true;
	}

	private synchronized void sentToServerForScan(final DataContainerContact item) {
		if (nextScanUploadUrl == null || nextScanAuth == null) {
//			l.i("getUploadUrl");
			String jsonString = contactStashCon.getImgUploadUrlNoThread();
			if(jsonString == null){
				return;
			}
			try {
				JSONObjectWrapper jObject = new JSONObjectWrapper(jsonString);
//				lock.lock();
				nextScanUploadUrl = jObject.getString("upload_url", null);
				nextScanAuth = jObject.getString("auth", null);
//				l.i("scanUploadUrlReceiced");
//				lock.unlock();
			} catch (JSONException e) {
//				l.e(e);
				return; //failed to get upload url
			}
		} 
		
		String uploadUrl = nextScanUploadUrl;
		nextScanUploadUrl = null;
		String scanAuth = nextScanAuth;
		nextScanAuth = null;
		String jsonString = contactStashCon.uploadScannedImage(item, uploadUrl, scanAuth);
		item.writeToDB(dbAccessor);
//		l.e("scanUploadFinished");

		
		waitMode = WAIT_MODE_SCANRESULT;
	}

	private synchronized void getContactInfromation(DataContainerContact item) {
		if (dbAccessor != null) {
//			l.i("ARE ANY NULL item: " + item + " contactStashCon: " + contactStashCon);
			contactStashCon.getContact(Integer.toString(item.contactstash_id), dbAccessor);
		}
		waitMode = WAIT_MODE_SCANRESULT;
	}
	
	private void updateContactInformation(DataContainerContact item) {
//		l.i("updateContactInformation");
		DataContainerContact newData = contactStashCon.updateContactNoThread(item);
		waitMode = WAIT_MODE_SCANRESULT;
	}
	
	private void deleteContactInformation(DataContainerContact item) {
		boolean deleteSuccess = contactStashCon.deleteContactNoThread(item);
		if(deleteSuccess) {
			item.deleteFromDB(dbAccessor);
			if(listener != null) {
//				l.i("Notify contact change onDelete");
				listener.contactsChanged();
			}
		}
	}

	public void resumeWorking() {
//		l.i("resumeWorking");
		isPaused = false;
		lock.lock();
		condition.signal();
		lock.unlock();
	}

	public void pauseWork() {
		isPaused = true;
		if (dbAccessor != null) {
			synchronized (dbAccessor) {
				dbAccessor.close();
			}
		}
	}

	public void sosialMediaServiceReplyStatus(String socialmedia, String jsonString) {
	}
	public void noSocialMediaServiceObject() {
	}
	public void sosialMediaServiceReplyConnect(String socialmedia, String jsonString) {
	}
	public void sosialMediaServiceReplyDisConnnect(String socialmedia, String jsonString) {
	}
	public void sosialMediaServiceReplyImport(String serviceName, String responceTxt) {
	}
	public void sosialMediaServiceErrorImport(String serviceName) {
	}
	public void serviceConnectionSuccess(String pushId) {}
	public void contactQuerySuccess() {}
	public void onContactInformationReceived(DataContainerContact newCData) {}
	public void scanUploadUrlReceiced(String jsonString) {}

	public void scanUploadFinished(DataContainerContact cData, String jsonString) {}
	public void gotContactInformation(DataContainerContact dc) {}
	
	public void setManageContactListener(ManageContactListener li) {
		listener = li;
	}
	
	public interface ManageContactListener{
		public void contactsChanged();
	}

	public void deleteContactInformation(String contactStashID) {
		DataContainerContact cData = dbAccessor.getContactDataWithContactStashID(Integer.parseInt(contactStashID));
		cData.deleteFromDB(dbAccessor);
		if(listener != null) {
//			l.i("Notify contact change onDelete");
			listener.contactsChanged();
		}
	}
}
