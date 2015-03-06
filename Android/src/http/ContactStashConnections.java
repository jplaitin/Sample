
package com.example.example.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;
import android.preference.PreferenceManager;

import com.haave.contactstash.ContactStashApplication;
import com.haave.contactstash.ContactStashPreferences;
import com.haave.contactstash.GlobalVariables;
import com.haave.contactstash.camera.FileManager;
import com.haave.contactstash.db.DataBaseAccessor;
import com.haave.contactstash.http.GetContact.GetContactThreadCallBack;
import com.haave.contactstash.oauth.OAuthTaskManagerContactStash;
import com.norfello.android.utils.DebugLog;
import com.norfello.android.utils.StringUtils;
import com.norfello.android.utils.SystemUtils;

public class ContactStashConnections {

	private static DebugLog l = new DebugLog(ContactStashConnections.class.getSimpleName());
	
	private static final String JSON_TAG_IDS 				= "ids";
	private static final String JSON_TAG_NEXT 				= "next";
	private static final String HTTP_PARAM_KEY_LIMIT 		= "limit";
	private static final String HTTP_PARAM_KEY_SINCE 		= "since";
	private final long NEXT_UPLOAD_LIMIT	 				= 1000 * 60 * 10;
	private final long TOO_LONG_SINCE_LAST 					= 1000 * 60 * 60 * 24 * 14; //two weeks 
	private final SimpleDateFormat dateFormat 				= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private static final int SOCKET_OPERATION_TIMEOUT 		= 15 * 1000;
	private static final int CONNECTION_TIMEOUT				= 15 * 1000;
	
//	public static final String URL_BASE 					= "http://dev11a.latest.connstash.appspot.com/";
	public static final String URL_BASE 					= "https://connstash.appspot.com/";
	public static final String URL_CONTACTS 				= URL_BASE +"api/v1/contact/";
	public static final String URL_UPLOAD 					= URL_BASE +"api/v1/avatar/";
	public static final String URL_SETTINGS					= URL_BASE +"api/v1/settings/";
	public static final String URL_UA_DEVICE				= URL_BASE +"api/v1/device/";
	public static final String URL_UPLOAD_IMG				= URL_BASE +"api/v1/upload/";
	public static final String URL_OAUT_TEST				= URL_BASE +"oauthtest/";

	private CommonsHttpOAuthConsumer consumer 				= null;
	private SSLSessionCache sessionCache	  				= null;
	
	private boolean isGettingContacts 						= false;

	private DefaultHttpClient multiHttpClient				= null;
	
	private static final Vector<ContactStashCallback> callbacks = new Vector<ContactStashCallback>();

	public ContactStashConnections(Context context, CommonsHttpOAuthConsumer c) {
		consumer = c;
		sessionCache = context == null ? null : new SSLSessionCache(context);
	}
	
	public static ContactStashConnections getContactStashConnections(Context context) {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String token = pref.getString(ContactStashPreferences.PREF_KEY_ACCESS_TOKEN, null);
		String tokenSecret = pref.getString(ContactStashPreferences.PREF_KEY_ACCESS_TOKEN_SECRET, null);
//		l.i("Token: "+ token +" Token secret: "+ tokenSecret);
		if (token != null && tokenSecret != null) {
			if (SystemUtils.haveNetworkConnection(context)) {  //CHECK CONNECTION
				try {
					CommonsHttpOAuthConsumer c = new CommonsHttpOAuthConsumer(OAuthTaskManagerContactStash.CONSUMER_KEY,
							OAuthTaskManagerContactStash.CONSUMER_SECRET);
					c.setTokenWithSecret(token, tokenSecret);
					CommonsHttpOAuthProvider p = new CommonsHttpOAuthProvider(OAuthTaskManagerContactStash.REQUEST_URL + "?scope="
								+ URLEncoder.encode(OAuthTaskManagerContactStash.SCOPE, OAuthTaskManagerContactStash.ENCODING),
								OAuthTaskManagerContactStash.ACCESS_URL, OAuthTaskManagerContactStash.AUTHORIZE_URL);
					ContactStashConnections con = new ContactStashConnections(context, c); //TODO
					return con;
					//con.testConnection();
					 
				} catch (UnsupportedEncodingException e) {
//					l.e(e);
					return null;
				}
			}
			else{
//				l.e("no network connection when trying to create contactstashconnection");
			}
		}
		else{
//			l.e("no access token yet so can't implement ContactStashConnection");
		}
		return null;
	}

	private void testConnection() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				HttpClient httpClient = createDefaultHttpClient();
				   
				String url = URL_OAUT_TEST;
				HttpGet request = new HttpGet(url);
				setDefaultHeaders(request);
			    
				try {
					consumer.sign(request);
					HttpResponse response 	= httpClient.execute(request);
					String responceTxt 		= StringUtils.toString(response);
					l.e(response.getStatusLine().toString());
					l.i(responceTxt);
				} catch (Exception e) {
//					l.e(e);
				}
			}
		});
		t.start();
		
	}

	public void getContactsAuto(final DataBaseAccessor dbAccessor) {
		ContactStashPreferences pref = ContactStashApplication.getApplicationPreferences();		
		final long lastUpdate = pref.getLastContactUpdate();
		final long sinceLastUpdate = System.currentTimeMillis() - lastUpdate;
		if(sinceLastUpdate > NEXT_UPLOAD_LIMIT) {
			getContacts(dbAccessor, lastUpdate);
		}
		else {
			l.e("not yet 10 minutes since last");
		}	
	}
	
	public void getContacts(final DataBaseAccessor dbAccessor, final long lastUpdate) {
		if(isGettingContacts) return;
		isGettingContacts = true;
		
		Thread t = new Thread(new Runnable() {
			private final ReentrantLock lock = new ReentrantLock(); 
			private final Condition condition = lock.newCondition();
			private ArrayList<GetContact> threads		= null;
			
			public void run() {
				// create an HTTP request to a protected resource
				try {
					// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
					String nextVal = null;
					ArrayList<String> idList = new ArrayList<String>();
					HttpClient httpClient = createDefaultHttpClient();
					String sinceLastUpdate = null;
					if( (System.currentTimeMillis() - lastUpdate) < TOO_LONG_SINCE_LAST) {
						Date d = new Date(lastUpdate);
						sinceLastUpdate = dateFormat.format(d);
//						l.e("contactstash update sinceLastUpdate: "+ sinceLastUpdate);
					}
					
					do {
						String url = URL_CONTACTS + "?";
						List<NameValuePair> params = new LinkedList<NameValuePair>();
						params.add(new BasicNameValuePair(HTTP_PARAM_KEY_SINCE, sinceLastUpdate)); //this value cant change
						params.add(new BasicNameValuePair(HTTP_PARAM_KEY_LIMIT, "50"));
						if (nextVal != null) {
							params.add(new BasicNameValuePair(JSON_TAG_NEXT, nextVal));
						}
						url += URLEncodedUtils.format(params, OAuthTaskManagerContactStash.ENCODING);
						nextVal = null;
						HttpGet request = new HttpGet(url);
						setDefaultHeaders(request);
						consumer.sign(request);
						
						ContactStashApplication.getApplicationPreferences().setLastContactUpdate(System.currentTimeMillis());
						
						HttpResponse response 	= httpClient.execute(request);
						String responceTxt 		= StringUtils.toString(response);
						
//						l.e("url: " + url.toString());
//						l.e("status: " + response.getStatusLine());
//						l.e("responce: " + responceTxt);
						JSONObjectWrapper jObj	= new JSONObjectWrapper(new JSONObject(responceTxt));

						JSONArray jArr			= jObj.getArray(JSON_TAG_IDS);
						if(jArr == null) {
							break;
						}
						int count = jArr.length();
						for(int i=0; i<count; i++) {
							idList.add(jArr.getString(i));
						}	
						nextVal = jObj.getString(JSON_TAG_NEXT, null);
					} while (nextVal != null);
//					l.i("idList: " + idList.toString());
					
					HttpParams params = new BasicHttpParams();
					ConnManagerParams.setMaxTotalConnections(params, 4);
					HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); //!!!!!!!
					// Turn off stale checking.  Our connections break all the time anyway,
					// and it's not worth it to pay the penalty of checking every time.
					HttpConnectionParams.setStaleCheckingEnabled(params, false);
					HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
					HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
					HttpConnectionParams.setSocketBufferSize(params, 8192);

					// Don't handle redirects -- return them to the caller.  Our code
					// often wants to re-POST after a redirect, which we must do ourselves.
					HttpClientParams.setRedirecting(params, false);

					// Use a session cache for SSL sockets
					SchemeRegistry schemeRegistry = new SchemeRegistry();
					schemeRegistry.register(new Scheme("http",
					        PlainSocketFactory.getSocketFactory(), 80));
					schemeRegistry.register(new Scheme("https",
					        SSLCertificateSocketFactory.getHttpSocketFactory(
					        SOCKET_OPERATION_TIMEOUT, sessionCache), 443));

					ClientConnectionManager manager =
					        new ThreadSafeClientConnManager(params, schemeRegistry);

					int concurrentGETmax 			= 3;
					threads 						= new ArrayList<GetContact>();
					long start 						= System.currentTimeMillis();
					if (idList.size() > 0) {							
						final DefaultHttpClient clientHttpSingeCont = new DefaultHttpClient(manager, params);
						for (final String id : idList) {
							if(threads.size() < concurrentGETmax) {
								lock.lock();
								GetContact thread = new GetContact(id, consumer, clientHttpSingeCont);
								thread.setDataBaseAccessor(dbAccessor);
								thread.setContactHandlerListener(new OnContactThreadHandlerListener() {
									public void onContactHandled(GetContact getContactThread) {
										lock.lock();
										threads.remove(getContactThread);
										condition.signal();
										lock.unlock();
										synchronized(ContactStashConnections.this){
											for (ContactStashCallback cb : callbacks) {
												cb.contactQuerySuccess();
											}
										}
									}

									public void onContactDeleted(GetContact getContactThread) {
										lock.lock();
										threads.remove(getContactThread);
										condition.signal();
										lock.unlock();
										synchronized(ContactStashConnections.this){
											for (ContactStashCallback cb : callbacks) {
												cb.deleteContactInformation(id);
											}
										}
									}
								});
								threads.add( thread );
								lock.unlock();
								thread.start();
//							l.i("responce: " + responceTxt);
							}
							else {
								lock.lock();
								condition.await();
								lock.unlock();
							}
						}
					}
					long duration = (System.currentTimeMillis() - start)/1000;
//					l.i("duration: "+ duration);
				} catch (Exception e) {
//					l.e(e);
				}
				isGettingContacts = false;
			}
		});
		t.start();
	}
	
	private void createJsontFieldsToFile(HttpClient httpClient, int contactstash_contact_id) {
		String getSingleContactUrl = ContactStashConnections.URL_CONTACTS + contactstash_contact_id + "/";
		HttpOptions opReq = new HttpOptions(getSingleContactUrl);
		try {
			consumer.sign(opReq);
			HttpResponse opResp 			= httpClient.execute(opReq);
			String responceTxt 				= StringUtils.toString(opResp);
			File test 						= new File(GlobalVariables.appFolder);
			File textFile					= new File(test, "contactjson.txt");
			textFile.createNewFile();
			FileOutputStream fOut 			= new FileOutputStream(textFile);
			OutputStreamWriter myOutWriter 	= new OutputStreamWriter(fOut);
			myOutWriter.append(responceTxt);
			myOutWriter.close();
			fOut.close();
//			l.d("Options responce :\n"+ responceTxt);
		} catch (Exception e) { /*l.e( e ); */ } 
	}

	public boolean getServiceStatus(final String serviceCallName) {
		Thread t = new Thread(new Runnable() {
			private final ReentrantLock lock = new ReentrantLock(); 
			private final Condition condition = lock.newCondition();
			
			public void run() {
				// create an HTTP request to a protected resource
				try {
					HttpClient httpClient = createDefaultHttpClient();
					   
					String url = URL_SETTINGS+ serviceCallName +"/";
					HttpGet request = new HttpGet(url);
					setDefaultHeaders(request);
				    
					consumer.sign(request);
					
					HttpResponse response 	= httpClient.execute(request);
					String responceTxt 		= StringUtils.toString(response);
//					l.e("status: " + response.getStatusLine());
					int status = response.getStatusLine().getStatusCode();
//					l.e("test responce: " + responceTxt);
					synchronized(ContactStashConnections.this){
						for (ContactStashCallback cb : callbacks) {
							if(status == 200 ) {
								cb.sosialMediaServiceReplyStatus(serviceCallName, responceTxt);
							}
							else {
								cb.noSocialMediaServiceObject();
							}
						}
					}
//					JSONObject jObject 		= new JSONObject(responceTxt);
				} catch (Exception e) {
//					l.e(e);
				}
			}
		});
		t.start();
		return false;
	}

	public void disconnectSocialNetwork(final String serviceName) {
		Thread t = new Thread(new Runnable() {			
			public void run() {
				// create an HTTP request to a protected resource
				try {
					// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
				    HttpClient httpClient = createDefaultHttpClient();

				    String url = URL_SETTINGS+ serviceName +"/";
					HttpDelete request = new HttpDelete(url);
					consumer.sign(request);
					
					HttpResponse response 	= httpClient.execute(request);
					String responceTxt 		= StringUtils.toString(response);
//					l.e("DELETE status: " + response.getStatusLine());
//					l.e("test responce: " + responceTxt);
					synchronized(ContactStashConnections.this){
						for (ContactStashCallback cb : callbacks) {
							cb.sosialMediaServiceReplyDisConnnect( serviceName, responceTxt );
						}
					}
//					JSONObject jObject 		= new JSONObject(responceTxt);
				} catch (Exception e) {
//					l.e(e);
				}
			}
		});
		t.start();
	}
	
	public void connectSocialNetwork(final String serviceName) {
		Thread t = new Thread(new Runnable() {			
			public void run() {
				// create an HTTP request to a protected resource
				try {
					// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
					HttpClient httpClient = createDefaultHttpClient();
					   
					String url = URL_SETTINGS+ serviceName +"/";
					HttpPost request = new HttpPost(url);
					setDefaultHeaders(request);
				    
					consumer.sign(request);
					
					HttpResponse response 	= httpClient.execute(request);
					String responceTxt 		= StringUtils.toString(response);
//					l.e("status: " + response.getStatusLine());
//					l.e("test responce: " + responceTxt);
					synchronized(ContactStashConnections.this){
						for (ContactStashCallback cb : callbacks) {
							cb.sosialMediaServiceReplyConnect( serviceName, responceTxt );
						}
					}
//					JSONObject jObject 		= new JSONObject(responceTxt);
				} catch (Exception e) {
//					l.e(e);
				}
			}
		});
		t.start();
	}
	
	public void importContacts(final String serviceName) {
		Thread t = new Thread(new Runnable() {			
			public void run() {
				// create an HTTP request to a protected resource
				try { 
					HttpClient httpClient = createDefaultHttpClient();
		   
					String url = URL_SETTINGS+ serviceName +"/";
					HttpPut request = new HttpPut( url);
					setDefaultHeaders(request);
					
					ArrayList<BasicNameValuePair> extra = new ArrayList<BasicNameValuePair>();
					extra.add(new BasicNameValuePair("synchronize", "true"));
					setUpParameters(request, extra);
										
					consumer.sign(request);
					
					HttpResponse response 	= httpClient.execute(request);
					String responceTxt 		= StringUtils.toString(response);
					l.v("status: " + response.getStatusLine());
					l.v("test responce: " + responceTxt);
					
//					l.e("status: " + response.getStatusLine());
					int status = response.getStatusLine().getStatusCode();
					synchronized(ContactStashConnections.this){
						for (ContactStashCallback cb : callbacks) {
							if(status == 200 ) {
								cb.sosialMediaServiceReplyImport(serviceName, responceTxt);
							}
							else {
								cb.sosialMediaServiceErrorImport(serviceName);
							}
						}
					}
				} catch (Exception e) {
//					l.e(e);
				}
			}
		});
		t.start();
	}
	
	public void connectUAtoDevice(final String ua_pushId, final String deviceUuid) {
//		URL_UA_DEVICE
//		device_id
//		(header)user-agent: android
		Thread t = new Thread(new Runnable() {			
			public void run() {
				// create an HTTP request to a protected resource
				try { 
					// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
					HttpClient httpClient = createDefaultHttpClient();
//					params.add(new BasicNameValuePair("synchronize", "true"));
		   			   
					HttpPost request = new HttpPost(URL_UA_DEVICE);
					setDefaultHeaders(request);
					
					ArrayList<BasicNameValuePair> extra = new ArrayList<BasicNameValuePair>();
					extra.add(new BasicNameValuePair("device_id", deviceUuid));
					extra.add(new BasicNameValuePair("device_token", ua_pushId));
					setUpParameters(request, extra);
					
					consumer.sign(request);
					
					HttpResponse response 	= httpClient.execute(request);
					String responceTxt 		= StringUtils.toString(response);
//					l.i("connectUAtoDevice responceText: "+ responceTxt);
					l.v(response.getStatusLine().toString());
					if(response.getStatusLine().getStatusCode() == 200) {
						
					}
//					JSONObject jObject 		= new JSONObject(responceTxt);
				} catch (Exception e) {
//					l.e(e);
				}
			}
		});
		t.start();
	}
	
	public String getImgUploadUrlNoThread() {
		try { 
			// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
			HttpClient httpClient = createDefaultHttpClient();
//			params.add(new BasicNameValuePair("synchronize", "true"));
   			   
			HttpGet request = new HttpGet(URL_UPLOAD_IMG);
			setDefaultHeaders(request);
		
			consumer.sign(request);
			
			HttpResponse response 	= httpClient.execute(request);
			String responceTxt 		= StringUtils.toString(response);
			int status 				= response.getStatusLine().getStatusCode();
//			l.i("getImgUploadUrl responceText: "+ responceTxt +"\nstatusCode: "+ status);
			if(status == HttpStatus.SC_OK) {
				return responceTxt;
			}
		} catch (Exception e) {
//			l.e(e);
		}
		return null;
	}
	
	public void getImgUploadUrl() {
		Thread t = new Thread(new Runnable() {			
			public void run() {
				String responceTxt = getImgUploadUrlNoThread();
				if(responceTxt == null) {
					return;
				}
				synchronized(ContactStashConnections.this){
					for (ContactStashCallback cb : callbacks) {
						cb.scanUploadUrlReceiced(responceTxt);
					}
				}
			}
		});
		t.start();
	}
	
	
	public synchronized String uploadScannedImage(final DataContainerContact cData, final String nextScanUploadUrl, final String nextScanAuth) {
		try { 
			// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
			HttpClient httpClient		= createDefaultHttpClient();
			HttpPost request 			= new HttpPost(nextScanUploadUrl);

			request.addHeader("User-Agent", GlobalVariables.userAgent);
			request.addHeader("X-Requested-With", "XmlHttpRequest");

//			l.i("uploading image : "+ cData.id);
			File file = new File(cData.cardImg.path);
			boolean ex = file.exists();
			boolean canR = file.canRead();
			
		    MultipartEntity mpEntity  	= new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		    ContentBody cbFile       	= new FileBody( file, StringUtils.getMimeType(file.getName()));
		    mpEntity.addPart( "image_data",       cbFile        );        
		    mpEntity.addPart( "auth", new StringBody(nextScanAuth));
		    mpEntity.addPart( "cropped", new StringBody("true"));
		    request.setEntity( mpEntity );

		    try {
		    	HttpResponse response 		= httpClient.execute(request);
		    	String responceTxt 			= StringUtils.toString(response);
		    	l.i("uploadScannedImage responceText: "+ responceTxt); //TODO 
		    	try {
					JSONObjectWrapper jObject 		= new JSONObjectWrapper(responceTxt);
					cData.contactstash_id			= jObject.getInt("id", 0);
					if(cData.contactstash_id != 0) {
						cData.data.processing_status		= DataBaseAccessor.STATUS_UNPROCESSED;
					}

				} catch (JSONException e) {
//					l.e(e);
				}
		    	synchronized(ContactStashConnections.this){
					for (ContactStashCallback cb : callbacks) {
						cb.scanUploadFinished(cData, responceTxt);
					}
				}
		    	return responceTxt;
		    }
		    catch(Exception e) {
		    	request.abort();
//		    	l.e(e);
		    }
			
		} catch (Exception e) {
//			l.e(e);
		}
		return null;
	}
	
	public void getContact(String cID, DataBaseAccessor dbAccessor) {
		createMultiThreadHttpClient();
		GetContact thread = new GetContact(cID, consumer, multiHttpClient);
		thread.setDataBaseAccessor( dbAccessor );
		thread.setGetContactCallback( new GetContactThreadCallBack() {
			public void gotContactInformation(DataContainerContact cData) {
				synchronized(ContactStashConnections.this){
					for (ContactStashCallback cb : callbacks) {
						cb.gotContactInformation(cData);
					}
				}
			}
			public void deleteContactInformation(String contactStashID) {
				synchronized(ContactStashConnections.this){
					for (ContactStashCallback cb : callbacks) {
						cb.deleteContactInformation(contactStashID);
					}
				}
			}
		}  );
		thread.start();
	}
	
	private void createMultiThreadHttpClient() {
		if(multiHttpClient == null) {
			HttpParams params = new BasicHttpParams();
			ConnManagerParams.setMaxTotalConnections(params, 4);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); //!!!!!!!
			// Turn off stale checking.  Our connections break all the time anyway,
			// and it's not worth it to pay the penalty of checking every time.
			HttpConnectionParams.setStaleCheckingEnabled(params, false);
			HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
	
			// Don't handle redirects -- return them to the caller.  Our code
			// often wants to re-POST after a redirect, which we must do ourselves.
			HttpClientParams.setRedirecting(params, false);
	
			// Use a session cache for SSL sockets
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http",
			        PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https",
			        SSLCertificateSocketFactory.getHttpSocketFactory(
			        SOCKET_OPERATION_TIMEOUT, sessionCache), 443));
	
			ClientConnectionManager manager =
			        new ThreadSafeClientConnManager(params, schemeRegistry);
			
			multiHttpClient = new DefaultHttpClient(manager, params);
		} 
	}
	
	public void getContactShort(final String cID, final DataBaseAccessor dbAccessor) {
		Thread t = new Thread(new Runnable() {			 
			public void run() {  
				HttpGet request = null;
				try { 
					// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
					HttpClient httpClient		= createDefaultHttpClient();
					String getSingleContactUrl = ContactStashConnections.URL_CONTACTS + cID + "/";
					request 			= new HttpGet(getSingleContactUrl);
					setDefaultHeaders(request);

					consumer.sign(request);
					
					HttpResponse response 	= httpClient.execute(request);
					String responceTxt 		= StringUtils.toString(response);
					
					l.v("responceTxt: " + responceTxt);
					l.v("response.getStatusLine(): "+ response.getStatusLine());

					JSONObject jObject = new JSONObject(responceTxt);
					DataContainerContact cData = DataContainerContact.readContactDataObject(jObject);
					
					cData.combineDataBaseAndJson(dbAccessor);
					FileManager fm = new FileManager();
					if (cData.data.avatars != null) {
						for (DataContainerContactImage img: cData.avatars) {
							String url				= ContactStashConnections.URL_UPLOAD + img.contact_stash_avatar_id + "/";
							request 				= new HttpGet(url);
							if(img.md5_checksum != null) {
								request.addHeader("if-none-match", img.md5_checksum);
							}
							request.addHeader("User-Agent", GlobalVariables.userAgent);
							request.addHeader("X-Requested-With", "XmlHttpRequest");
							
							consumer.sign(request);
							HttpResponse response2 	= httpClient.execute(request);
							if(response2.getStatusLine().getStatusCode() == 200) {
								Header etagH 		= response2.getFirstHeader("Etag");
								String etagVal 		= null;
								String avatarPath 	= null;
								if(etagH != null) {
									etagVal 		= etagH.getValue();
								}
								
								HttpEntity entity = response2.getEntity();
								BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
								final long contentLength = bufHttpEntity.getContentLength();
								if (contentLength >= 0) { 
									InputStream is = bufHttpEntity.getContent();
									if(img.path == null) { //write new avatar img
										String nameClip	 	= cData.data.person_first_names;
										avatarPath 			= fm.savePortrait(is, System.currentTimeMillis(), nameClip);
										img.path 			= avatarPath;
										img.md5_checksum	= etagVal;
									}
									else { //over write old avatar img
										avatarPath 			= fm.savePortrait(is, img.path );
										img.md5_checksum	= etagVal;
									}
								}
								
							}	
						}
					}
					cData.writeToDB(dbAccessor);
					synchronized(ContactStashConnections.this){
						for (ContactStashCallback cb : callbacks) {
							cb.gotContactInformation(cData);
						}
					}
				}
				catch(Exception e) {
					request.abort();
//				    	l.e(e);
				 }
			
			}
		});
		t.start();
	}
	
	public void updateContact(final DataContainerContact cData) {
		Thread t = new Thread(new Runnable() {			
			public void run() {
				HttpRequestBase request;
				if(cData.contactstash_id == 0) {
					request = new HttpPost(URL_CONTACTS);
					ArrayList<BasicNameValuePair> extra = cData.data.getDataAsHttpParameters();
					setUpParameters((HttpPost)request, extra);
				}
				else {
					String url = URL_CONTACTS + Integer.toString(cData.contactstash_id) +"/";
//					l.i("Update url: "+ url);
					request = new HttpPut(url);
					ArrayList<BasicNameValuePair> extra = cData.data.getDataAsHttpParameters();
					setUpParameters((HttpPut)request, extra);
				}
				
				// create an HTTP request to a protected resource
				try {
					// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
					HttpClient httpClient = createDefaultHttpClient();
					setDefaultHeaders(request);
					consumer.sign(request);
					
					HttpResponse response 	= httpClient.execute(request);
					String responceTxt 		= StringUtils.toString(response);
					l.e("status: " + response.getStatusLine());
					l.e("test responce: " + responceTxt);
					JSONObject jObject = new JSONObject(responceTxt);
					DataContainerContact newData = DataContainerContact.readContactDataObject(jObject);
					newData.id = cData.id;
					synchronized(ContactStashConnections.this){
						for (ContactStashCallback cb : callbacks) {
							cb.onContactInformationReceived(newData);
						}
					}
					
//					if( socialMediacallback != null ) {
//						socialMediacallback.sosialMediaServiceReplyConnect( serviceName, responceTxt );
//					}
//					JSONObject jObject 		= new JSONObject(responceTxt);
				} catch (Exception e) {
//					l.e(e);
				}
			}

			
		});
		t.start();
	}
	
	public DataContainerContact updateContactNoThread(final DataContainerContact cData) {
		HttpRequestBase request;
		if(cData.contactstash_id == 0) {
			request = new HttpPost(URL_CONTACTS);
			ArrayList<BasicNameValuePair> extra = cData.data.getDataAsHttpParameters();
			setUpParameters((HttpPost)request, extra);
		}
		else {
			String url = URL_CONTACTS + Integer.toString(cData.contactstash_id) +"/";
			request = new HttpPut(url);
			ArrayList<BasicNameValuePair> extra = cData.data.getDataAsHttpParameters();
			setUpParameters((HttpPut)request, extra);
		}
		
		// create an HTTP request to a protected resource
		try {
			// HttpOptions request = new HttpOptions("https://connstash.appspot.com/api/v1/profile/me/");
			HttpClient httpClient = createDefaultHttpClient();
			setDefaultHeaders(request);
			consumer.sign(request);
			
			HttpResponse response 	= httpClient.execute(request);
			String responceTxt 		= StringUtils.toString(response);
			l.e("status: " + response.getStatusLine());
			l.e("test responce: " + responceTxt);
			JSONObject jObject 				= new JSONObject(responceTxt);
			DataContainerContact newData 	= DataContainerContact.readContactDataObject(jObject);
			newData.id 						= cData.id;
			newData.data.processing_status 	= DataBaseAccessor.STATUS_UPDATED;
			synchronized(ContactStashConnections.this){
				for (ContactStashCallback cb : callbacks) {
					cb.onContactInformationReceived(newData);
				}
			}
			return cData;
		} catch (Exception e) {
//			l.e(e);
		}
		return null;
	}
	
	public boolean deleteContactNoThread(final DataContainerContact cData) {
		if(cData.contactstash_id == 0) {
//			l.e("Couldn't delete .. No ContactStash id!!!" );
			return false;
		}
		
		String url = URL_CONTACTS + Integer.toString(cData.contactstash_id) +"/";
		HttpClient httpClient = createDefaultHttpClient();
		HttpDelete request = new HttpDelete(url);
		setDefaultHeaders(request);
		try {
			consumer.sign(request);
			HttpResponse response 	= httpClient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if( statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_GONE) {
				return true;
			}
			else {
				return false;
			}		
//			String responceTxt 		= StringUtils.toString(response);
//			l.i(response.getStatusLine().toString());
//			l.i(responceTxt);
		} catch (Exception e) {
			return false;
		} 
	}
	
	private HttpClient createDefaultHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		return httpClient;
	}
	
	private void setUpParameters(HttpEntityEnclosingRequestBase request, ArrayList<BasicNameValuePair> extra) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		if(extra != null)
			params.addAll(extra);
		try {
			request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}  
	}
	
	public static void setDefaultHeaders(AbstractHttpMessage request) { 
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("User-Agent", GlobalVariables.userAgent);
		request.addHeader("X-Requested-With", "XmlHttpRequest");
	}
	
	public static void addCallback(ContactStashCallback cb)
	{
		callbacks.add(cb);
	}
	
	public static void removeCallback(ContactStashCallback cb)
	{
		callbacks.remove(cb);	
	}
	
	public interface ContactStashCallback{
		public void sosialMediaServiceReplyStatus(String socialmedia, String jsonString );
		public void deleteContactInformation(String contactStashID);
		public void noSocialMediaServiceObject();
		public void sosialMediaServiceReplyConnect(String socialmedia, String jsonString );
		public void sosialMediaServiceReplyDisConnnect(String socialmedia, String jsonString );
		public void sosialMediaServiceReplyImport(String serviceName, String responceTxt);
		public void sosialMediaServiceErrorImport(String serviceName);
		
		public void scanUploadUrlReceiced(String jsonString);
		public void scanUploadFinished(DataContainerContact cData, String jsonString);
		
		public void serviceConnectionSuccess(String pushId);
		
		public void contactQuerySuccess();
		
		public void onContactInformationReceived(DataContainerContact newCData);
		
		public void gotContactInformation(DataContainerContact dc);
		
	}
}
