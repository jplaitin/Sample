package com.example.example.db;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.haave.contactstash.http.DataContainerContact;
import com.haave.contactstash.http.DataContainerContactBox;
import com.haave.contactstash.http.DataContainerContactData;
import com.haave.contactstash.http.DataContainerContactImage;
import com.haave.contactstash.http.DataContainerSocialNetwork;

public class DataBaseAccessor extends SQLiteOpenHelper {
//	private static final DebugLog l = new DebugLog(DataBaseAccessor.class.getSimpleName());
	
	public static final SimpleDateFormat dateFormat 				= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	public static final int NEW_INSTANCE = 0;
	public static Object lock = null;
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATA_BASE_NAME 			= "contactstashdb";

	public static final String CONTACT 						= "contacts";
	public static final String CONTACT_C_ID 				= "_id";
	public static final String CONTACT_C_CONTACTSTASH_ID 	= "contactstash_id";
	public static final String CONTACT_C_OWNER 				= "owner";
	public static final String CONTACT_C_IMAGE_ID			= "images_id"; 
	public static final String CONTACT_C_CHECKED			= "checked";
	
	public static String createContactTableString() {
		StringBuilder createString = new StringBuilder();
		createString.append("CREATE TABLE IF NOT EXISTS ").append(DataBaseAccessor.CONTACT).append(" (");
		createString.append(DataBaseAccessor.CONTACT_C_ID).append(" INTEGER primary key autoincrement, ");
		createString.append(DataBaseAccessor.CONTACT_C_CONTACTSTASH_ID).append(" INTEGER, ");
		createString.append(DataBaseAccessor.CONTACT_C_OWNER).append(" INTEGER, ");
		createString.append(DataBaseAccessor.CONTACT_C_IMAGE_ID).append(" INTEGER, ");
		createString.append(DataBaseAccessor.CONTACT_C_CHECKED).append(" INT(1), ");
		
		createString.append("FOREIGN KEY(").append(DataBaseAccessor.CONTACT_C_IMAGE_ID).append(") ");
		createString.append("REFERENCES ").append(DataBaseAccessor.IMAGES).append("(").append(DataBaseAccessor.IMAGES_C_ID).append(")");
		
		createString.append(");");
		return createString.toString();
	}
	
	public static final String IMAGES 					= "images";
	public static final String IMAGES_C_ID				= "_id";
	public static final String IMAGES_C_CONTACTSTASH_IMG_ID	= "contact_stash_avatar_id";
	public static final String IMAGES_C_MD5CHECK		= "md5checksum";
	public static final String IMAGES_C_PATH			= "path";
	public static final String IMAGES_C_GROUP			= "image_group";
	
	public static String createImageTableString() {
		StringBuilder createString = new StringBuilder();
		createString.append("CREATE TABLE IF NOT EXISTS ").append(IMAGES).append(" (");
		createString.append(IMAGES_C_ID).append(" INTEGER primary key autoincrement, ");
		createString.append(IMAGES_C_CONTACTSTASH_IMG_ID).append(" INTEGER, "); 
		createString.append(IMAGES_C_MD5CHECK).append(" VARCHAR, "); 
		createString.append(IMAGES_C_PATH).append(" VARCHAR, ");
		createString.append(IMAGES_C_GROUP).append(" INTEGER");
		createString.append(");");
		
		return createString.toString();
	}

	public static final String DATAFIELD					= "datafield";
	public static final String DATAFIELD_C_CONTACT_ID		= "contact_id";
	public static final String DATAFIELD_C_FIELD_TYPE		= "field_type";
	public static final String DATAFIELD_C_VALUE			= "value";
	public static final String DATAFIELD_C_ID				= "_id";
	public static final String DATAFIELD_C_X				= "x";
	public static final String DATAFIELD_C_Y				= "y";
	public static final String DATAFIELD_C_WIDTH			= "width";
	public static final String DATAFIELD_C_HEIGHT			= "height";
	
	public static final String STATUS_SEND_IMAGE		= "send_image";
	public static final String STATUS_PROCESSED 		= "processed";
	public static final String STATUS_UNPROCESSED 		= "unprocessed";
	public static final String STATUS_RECOGNIZED 		= "recognized";
	public static final String STATUS_DELETE			= "delete";
	public static final String STATUS_UPDATE	 		= "update";
	public static final String STATUS_UPDATED	 		= "updated"; //contact updloaded (no image) and get updated contact
	public static final String STATUS_UNRECOGNIZABLE 	= "unrecognizable";
	
	public static String createBoxesTableString() {
		StringBuilder createString 	= new StringBuilder();
		createString.append("CREATE TABLE IF NOT EXISTS ").append(DataBaseAccessor.DATAFIELD).append(" (");
		createString.append(DataBaseAccessor.DATAFIELD_C_ID).append(" INTEGER primary key autoincrement, ");
		createString.append(DataBaseAccessor.DATAFIELD_C_CONTACT_ID).append(" INTEGER, ");
		createString.append(DataBaseAccessor.DATAFIELD_C_FIELD_TYPE).append(" VARCHAR, ");
		createString.append(DataBaseAccessor.DATAFIELD_C_VALUE).append(" VARCHAR, ");
		createString.append(DataBaseAccessor.DATAFIELD_C_X).append(" REAL, ");
		createString.append(DataBaseAccessor.DATAFIELD_C_Y).append(" REAL, ");
		createString.append(DataBaseAccessor.DATAFIELD_C_WIDTH).append(" REAL, ");
		createString.append(DataBaseAccessor.DATAFIELD_C_HEIGHT).append(" REAL, ");
		
		createString.append("FOREIGN KEY(").append(DataBaseAccessor.DATAFIELD_C_CONTACT_ID).append(") ");
		createString.append("REFERENCES ").append(DataBaseAccessor.CONTACT).append("(").append(DataBaseAccessor.CONTACT_C_ID).append(")");
		
		createString.append(");");
		return createString.toString();
	}
	
	public static final String SOCIAL_NETWORK 					= "sosialnetworks";
	public static final String SOCIAL_NETWORK_C_ID 				= "_id";
	public static final String SOCIAL_NETWORK_C_OWNER	 		= "owner";
	public static final String SOCIAL_NETWORK_C_API_ID			= "api_id";
	public static final String SOCIAL_NETWORK_C_CREATED			= "created";
	public static final String SOCIAL_NETWORK_C_MODIFIED		= "modified";
	public static final String SOCIAL_NETWORK_C_SYNCHRONIZED	= "synchronize";
	public static final String SOCIAL_NETWORK_C_CONNECTED		= "connected";
	public static final String SOCIAL_NETWORK_C_LAST_IMPORT		= "last_import";

	
	public static String createSocialNetworkTableString() {
		StringBuilder createString = new StringBuilder();
		createString.append("CREATE TABLE IF NOT EXISTS ").append(DataBaseAccessor.SOCIAL_NETWORK).append(" (");
		createString.append(DataBaseAccessor.SOCIAL_NETWORK_C_ID).append(" VARCHAR primary key, ");
		createString.append(DataBaseAccessor.SOCIAL_NETWORK_C_OWNER).append(" INTEGER, ");
		createString.append(DataBaseAccessor.SOCIAL_NETWORK_C_API_ID).append(" INTEGER, ");
		createString.append(DataBaseAccessor.SOCIAL_NETWORK_C_CREATED).append(" DATE, "); 
		createString.append(DataBaseAccessor.SOCIAL_NETWORK_C_MODIFIED).append(" DATE, ");
		createString.append(DataBaseAccessor.SOCIAL_NETWORK_C_SYNCHRONIZED).append(" INT(1), "); 
		createString.append(DataBaseAccessor.SOCIAL_NETWORK_C_CONNECTED).append(" INT(1), ");
		createString.append(DataBaseAccessor.SOCIAL_NETWORK_C_LAST_IMPORT).append(" DATE ");
		createString.append(");");
		return createString.toString();
	}
	
	public DataBaseAccessor(Context context) {
		super(context, DATA_BASE_NAME, null, DATABASE_VERSION);
		if(lock == null) {
			lock = new Object();
		}
		// l.turnOff();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String c1 = createContactTableString();
		String c3 = createBoxesTableString();
		String c4 = createImageTableString();
		String c5 = createSocialNetworkTableString();
		
//		l.d("test: "+c1);
//		l.d("test: "+c3);
//		l.d("test: "+c4);
//		l.d("test: "+c5);
		
		db.execSQL(c1);
		db.execSQL(c3);
		db.execSQL(c4);
		db.execSQL(c5);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
	
	public ContentValues getImageDataContentValues(DataContainerContactImage img) {
		ContentValues imgVal = new ContentValues();
		if (img.id != NEW_INSTANCE) {
			imgVal.put(DataBaseAccessor.IMAGES_C_ID, 						img.id);
		}
		imgVal.put(DataBaseAccessor.IMAGES_C_CONTACTSTASH_IMG_ID, 			img.contact_stash_avatar_id);
		imgVal.put(DataBaseAccessor.IMAGES_C_PATH, 							img.path);
		imgVal.put(DataBaseAccessor.IMAGES_C_MD5CHECK,						img.md5_checksum);
		imgVal.put(DataBaseAccessor.IMAGES_C_GROUP, 						img.image_group);
		return imgVal;
	}

	public ContentValues getSosialDataContentValues(DataContainerSocialNetwork socialnetwork) {
		int con			= socialnetwork.connected ? 1 : 0;
		int synch		= socialnetwork.synchronize ? 1 : 0;
		ContentValues socialVal = new ContentValues();
		socialVal.put(DataBaseAccessor.SOCIAL_NETWORK_C_ID, 						socialnetwork.id);
		socialVal.put(DataBaseAccessor.SOCIAL_NETWORK_C_API_ID, 					socialnetwork.api_id);
		socialVal.put(DataBaseAccessor.SOCIAL_NETWORK_C_OWNER, 						socialnetwork.owner);
		if(socialnetwork.created != null) {
			socialVal.put(DataBaseAccessor.SOCIAL_NETWORK_C_CREATED,					dateFormat.format( socialnetwork.created ));
		}
		if(socialnetwork.modified != null) {
			socialVal.put(DataBaseAccessor.SOCIAL_NETWORK_C_MODIFIED,					dateFormat.format( socialnetwork.modified ));
		}
		if(socialnetwork.last_imported != null) {
			socialVal.put(DataBaseAccessor.SOCIAL_NETWORK_C_LAST_IMPORT,				dateFormat.format( socialnetwork.last_imported ));
		}
		socialVal.put(DataBaseAccessor.SOCIAL_NETWORK_C_CONNECTED,					con);
		socialVal.put(DataBaseAccessor.SOCIAL_NETWORK_C_SYNCHRONIZED, 				synch);
		return socialVal;
	}
	public void insertSocialMediaData(DataContainerSocialNetwork dataSocial) {
		synchronized (this) {
			SQLiteDatabase db = getWritableDatabase();
			
			String[] whereArgs = new String[] { dataSocial.id };
			String[] projection = new String[] { SOCIAL_NETWORK_C_ID };
			String whereClause = SOCIAL_NETWORK_C_ID+ "=?";
	
			Cursor cursor = db.query(SOCIAL_NETWORK, projection, whereClause, whereArgs, null, null, null);
			int count = cursor.getCount();
			cursor.close();
			if(count > 0) {
				int number = db.update(SOCIAL_NETWORK, getSosialDataContentValues(dataSocial), whereClause, whereArgs);
			}
			else {
				int number = (int)db.insert(SOCIAL_NETWORK, null, getSosialDataContentValues(dataSocial));
			}
			
			db.close();
		}
	}	
	
	public ArrayList<DataContainerSocialNetwork> getAllSocialNetworkData() {
		synchronized (this) {
			SQLiteDatabase db 										= getReadableDatabase();
			ArrayList<DataContainerSocialNetwork> socialDataList 	= new  ArrayList<DataContainerSocialNetwork>();
			
			String[] whereArgs = null;
			String[] projection = null;
			String whereClause = null;
	
			Cursor cursor = db.query(SOCIAL_NETWORK, projection, whereClause, whereArgs, null, null, null);
	//		Cursor cursor 											= db.query(SOCIAL_NETWORK, null, null, null, null, null, null);
	//		l.i("search hits from "+ cursor.getCount());
			while(cursor.moveToNext()) {
				DataContainerSocialNetwork socialData 				= new DataContainerSocialNetwork();
				socialData.id										= cursor.getString(cursor.getColumnIndex(SOCIAL_NETWORK_C_ID));
				socialData.api_id									= cursor.getString(cursor.getColumnIndex(SOCIAL_NETWORK_C_API_ID));
				socialData.owner									= cursor.getInt(cursor.getColumnIndex(SOCIAL_NETWORK_C_OWNER));
				socialData.connected								= (cursor.getInt(cursor.getColumnIndex(SOCIAL_NETWORK_C_CONNECTED)) == 1);
				socialData.synchronize								= (cursor.getInt(cursor.getColumnIndex(SOCIAL_NETWORK_C_SYNCHRONIZED)) == 1);
				try {
					String mod 										= cursor.getString(cursor.getColumnIndex(SOCIAL_NETWORK_C_MODIFIED));
					if(mod != null)
						socialData.modified							= dateFormat.parse(mod);
					String cre 										= cursor.getString(cursor.getColumnIndex(SOCIAL_NETWORK_C_CREATED));
					if(cre != null)
						socialData.created							= dateFormat.parse(cre);
					String imp										= cursor.getString(cursor.getColumnIndex(SOCIAL_NETWORK_C_LAST_IMPORT));
					if(imp != null)
						socialData.last_imported						= dateFormat.parse(imp);
				} catch (ParseException e) { /*l.e(e);*/ }
				socialDataList.add(socialData);
			}
			cursor.close();
			db.close();
			return socialDataList;
		}
	}  //TODO
	
	
	public boolean insertImageData(DataContainerContactImage img) {
//		l.i("insertImageData : "+ img.toString());
		synchronized (this) {
			if(img.id == NEW_INSTANCE) {
				img.id = insertData(IMAGES, IMAGES_C_ID, getImageDataContentValues(img));
			}
			else {
				updateData(img.id, IMAGES, IMAGES_C_ID, getImageDataContentValues(img));
			}
			if(img.id == NEW_INSTANCE) {
				return false;
			}
			// l.i("new document id "+ id);
			return true;
		}
	}
	
	private ContentValues getContactDataContentValues(DataContainerContact cData) {
		ContentValues val = new ContentValues();
		if (cData.id != NEW_INSTANCE) {
			val.put(DataBaseAccessor.CONTACT_C_ID, cData.id);
		}
		if(cData.cardImg != null && cData.cardImg.id != NEW_INSTANCE) {
			val.put(DataBaseAccessor.CONTACT_C_IMAGE_ID, cData.cardImg.id);
		}
		if(cData.contactstash_id != NEW_INSTANCE) {
			val.put(DataBaseAccessor.CONTACT_C_CONTACTSTASH_ID, cData.contactstash_id);
		}
		val.put(DataBaseAccessor.CONTACT_C_OWNER, cData.owner);
		if(cData.checked) {
			val.put(DataBaseAccessor.CONTACT_C_CHECKED, 1);
		} 
		else {
			val.put(DataBaseAccessor.CONTACT_C_CHECKED, 0);
		}
		return val;
	}
	public boolean insertContactData(DataContainerContact cData) {
		synchronized (this) {
			if(cData.id == NEW_INSTANCE) {
				cData.id = insertData(CONTACT, CONTACT_C_ID, getContactDataContentValues(cData));
			}
			else {
				updateData(cData.id, CONTACT, CONTACT_C_ID, getContactDataContentValues(cData));
			}
			if(cData.id == NEW_INSTANCE) {
				return false;
			}
			// l.i("new document id "+ id);
			return true;
		}
	}
	
	private ArrayList<ContentValues> getContactDataFieldContentValues(DataContainerContact cData) {
		//this is tricky tricky tricky
		//data from server doesn't contain datafield tables id values so
		//data can't be writen directly over right fields. All fields type and value ... values
		//must be checked and compared to write server data to right place.
		//I don't wanna do that so I do the next best thing (perhaps little faster)
		//delete all contacs data fields and write them again to datafield table
		
		ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
		Field[] fields = DataContainerContactData.class.getDeclaredFields();
		for (Field field : fields) {
			String fieldName	= field.getName();
			try {
				if(field.getType().equals(String[].class)) {
					String[] fieldValues 	= (String[])field.get(cData.data);
					int index = 0;
					for (String fieldValue : fieldValues) {
						ContentValues arrayVal = new ContentValues();
						arrayVal.put(DataBaseAccessor.DATAFIELD_C_CONTACT_ID, 	cData.id);
						arrayVal.put(DataBaseAccessor.DATAFIELD_C_FIELD_TYPE, 	fieldName);
						arrayVal.put(DataBaseAccessor.DATAFIELD_C_VALUE, 		fieldValue);
						try {
							Field coordField = cData.boxes.getClass().getField(fieldName);
							float[][] coordsArr = (float[][])coordField.get(cData.boxes);
							if(coordsArr[index] != null) {
								arrayVal.put(DataBaseAccessor.DATAFIELD_C_X, 			coordsArr[index][0]);
								arrayVal.put(DataBaseAccessor.DATAFIELD_C_Y, 			coordsArr[index][1]);
								arrayVal.put(DataBaseAccessor.DATAFIELD_C_WIDTH, 		coordsArr[index][2]);
								arrayVal.put(DataBaseAccessor.DATAFIELD_C_HEIGHT, 		coordsArr[index][3]);
							}
						}catch (Exception e) {}
						index++;
						contentValues.add(arrayVal);
					}
				}
				else if(field.getType().equals(String.class)){
					ContentValues val = new ContentValues(); //always inserting data always new rows
					val.put(DataBaseAccessor.DATAFIELD_C_CONTACT_ID, 	cData.id);
					val.put(DataBaseAccessor.DATAFIELD_C_FIELD_TYPE, 	fieldName);
					val.put(DataBaseAccessor.DATAFIELD_C_VALUE, 		(String)field.get(cData.data));
					try {
						Field coordField = cData.boxes.getClass().getField(fieldName);
						float[] coordsArr = (float[])coordField.get(cData.boxes);
						if(coordsArr != null) {
							val.put(DataBaseAccessor.DATAFIELD_C_X, 			coordsArr[0]);
							val.put(DataBaseAccessor.DATAFIELD_C_Y, 			coordsArr[1]);
							val.put(DataBaseAccessor.DATAFIELD_C_WIDTH, 		coordsArr[2]);
							val.put(DataBaseAccessor.DATAFIELD_C_HEIGHT, 		coordsArr[3]);
						}
					}catch (Exception e) {}
					contentValues.add(val);
				}
				else if(field.getType().equals(double.class)) {
					ContentValues val = new ContentValues(); //always inserting data always new rows
					val.put(DataBaseAccessor.DATAFIELD_C_CONTACT_ID, 	cData.id);
					val.put(DataBaseAccessor.DATAFIELD_C_FIELD_TYPE, 	fieldName);
					val.put(DataBaseAccessor.DATAFIELD_C_VALUE, 		field.getDouble(cData.data));
					contentValues.add(val);
				}
				else if(field.getType().equals(Date.class)) {
					ContentValues val = new ContentValues(); //always inserting data always new rows
					val.put(DataBaseAccessor.DATAFIELD_C_CONTACT_ID, 	cData.id);
					val.put(DataBaseAccessor.DATAFIELD_C_FIELD_TYPE, 	fieldName);
					val.put(DataBaseAccessor.DATAFIELD_C_VALUE, 		DataContainerContact.dateFormat.format((Date)field.get(cData.data)));
					contentValues.add(val);
				}
				else if(field.getType().equals(int.class)) {
					ContentValues val = new ContentValues(); //always inserting data always new rows
					val.put(DataBaseAccessor.DATAFIELD_C_CONTACT_ID, 	cData.id);
					val.put(DataBaseAccessor.DATAFIELD_C_FIELD_TYPE, 	fieldName);
					val.put(DataBaseAccessor.DATAFIELD_C_VALUE, 		field.getInt(cData.data));
					contentValues.add(val);
				}
			}
			catch (Exception e) {}
		}
		return contentValues;
	}
	public boolean insertContactDataFieldData(DataContainerContact cData) {
		synchronized (this) {
			SQLiteDatabase db = getWritableDatabase();
			removeContactDataFieldData(cData, db);
			boolean isSuccess = true;
			int rowId = NEW_INSTANCE;
			
			ArrayList<ContentValues> contentValues = getContactDataFieldContentValues(cData);
			db.beginTransaction();
			for (ContentValues cVal : contentValues) {
				rowId = insertData(db, DATAFIELD, DATAFIELD_C_ID, cVal);
				if(rowId == NEW_INSTANCE) {
//					l.e("ContentValue insert failed. field: "+ cVal.getAsString(DataBaseAccessor.DATAFIELD_C_FIELD_TYPE));
					isSuccess = false;
				}
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			return isSuccess;
		}
	}

	public int insertData(String toTable, String idColumn, ContentValues values) {
		synchronized (this) {
			SQLiteDatabase db = getWritableDatabase();
	
			long insertIndx = db.insert(toTable, null, values);
			String[] whereArgs = new String[] { Long.toString(insertIndx) };
			String[] projection = new String[] { idColumn };
			String whereClause = "ROWID=?";
	
			int id = NEW_INSTANCE;
			Cursor cursor = db.query(toTable, projection, whereClause, whereArgs, null, null, null);
			if (cursor.getCount() != 0) {
				cursor.moveToFirst();
				id = cursor.getInt(cursor.getColumnIndex(idColumn));
			} 
			cursor.close();
			db.close();
			return id;
		}
	}
	
	private int updateData(int contactID, String toTable, String idColumn, ContentValues values) {
		SQLiteDatabase db 		= getWritableDatabase();
		
		String[] whereArgs 		= new String[] { Long.toString(contactID) };
		String whereClause 		= idColumn +"=?";
		long insertIndx 		= db.update(toTable, values, whereClause, whereArgs);
		
		whereArgs 				= new String[] { Long.toString(insertIndx) };
		String[] projection 	= new String[] { idColumn };
		whereClause 			= "ROWID=?";

		int id 					= NEW_INSTANCE;
		Cursor cursor 			= db.query(toTable, projection, whereClause, whereArgs, null, null, null);
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			id = cursor.getInt(cursor.getColumnIndex(idColumn));
		} 
		cursor.close();
		db.close();
		return id;
	}
	
	private int insertData(SQLiteDatabase db, String toTable, String idColumn, ContentValues values) {
		long insertIndx = db.insert(toTable, null, values);
		String[] whereArgs = new String[] { Long.toString(insertIndx) };
		String[] projection = new String[] { idColumn };
		String whereClause = "ROWID=?";

		int id = NEW_INSTANCE;
		Cursor cursor = db.query(toTable, projection, whereClause, whereArgs, null, null, null);
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			id = cursor.getInt(cursor.getColumnIndex(idColumn));
		} 
		cursor.close();
		return id;
	}
	
	public DataContainerContact getContactData(int id) {
		DataContainerContact dc 		= null;
		synchronized (this) {
			SQLiteDatabase db 			= getReadableDatabase();
			dc 	= getContactData(id, db);
			db.close();
		}
		return dc;
	}
	
	public DataContainerContact getContactDataWithContactStashID(int contactstash_id) {
		DataContainerContact dc 		= null;
		synchronized (this) {
			SQLiteDatabase db 			= getReadableDatabase();
			dc 	= getContactDataWithContactStashID(contactstash_id, db);
			db.close();
		}
		return dc;
	}
	
	private DataContainerContact getContactDataWithContactStashID(int contactstash_id, SQLiteDatabase db) {
		String[] whereArgs = new String[] { Long.toString(contactstash_id) };
		String[] projection = null;
		String whereClause = CONTACT_C_CONTACTSTASH_ID+"=?";
		
		Cursor cursor = db.query(CONTACT, projection, whereClause, whereArgs, null, null, null);
		return combineContactData(cursor, db);
	}
	
	private DataContainerContact getContactData(int id, SQLiteDatabase db) {
		String[] whereArgs = new String[] { Long.toString(id) };
		String[] projection = null;
		String whereClause = CONTACT_C_ID+"=?";
		
		Cursor cursor = db.query(CONTACT, projection, whereClause, whereArgs, null, null, null);
	
		return combineContactData(cursor, db);
	}
	
	private DataContainerContact combineContactData(Cursor cursor, SQLiteDatabase db) {
		DataContainerContact cData = new DataContainerContact();
		int cardImageID = 0;
		if(cursor.moveToFirst()) {
			cData.id 				= cursor.getInt(cursor.getColumnIndex(CONTACT_C_ID));
			cData.contactstash_id	= cursor.getInt(cursor.getColumnIndex(CONTACT_C_CONTACTSTASH_ID));
			cData.owner				= cursor.getInt(cursor.getColumnIndex(CONTACT_C_OWNER));
			int checked 			= cursor.getInt(cursor.getColumnIndex(CONTACT_C_CHECKED));
			cData.checked 			= (checked == 1);
			cardImageID				= cursor.getInt(cursor.getColumnIndex(CONTACT_C_IMAGE_ID));
			cursor.close();
		}
		else {
			cursor.close();
			return null;
		}
		if(cardImageID != 0) {
			String[] whereArgs 				= new String[] { Long.toString(cardImageID) };
			String[] projection 			= null;
			String whereClause 				= IMAGES_C_ID+"=?";
			
			Cursor cImg = db.query(IMAGES, projection, whereClause, whereArgs, null, null, null);
			if(cImg.moveToFirst()) {
				int img_id			= cImg.getInt(		cImg.getColumnIndex(IMAGES_C_ID));
				int contact_stash_img_id = cImg.getInt(	cImg.getColumnIndex(IMAGES_C_CONTACTSTASH_IMG_ID));
				String path 		= cImg.getString(	cImg.getColumnIndex(IMAGES_C_PATH));
				String md5check 	= cImg.getString(	cImg.getColumnIndex(IMAGES_C_MD5CHECK));
				int group			= cImg.getInt(		cImg.getColumnIndex(IMAGES_C_GROUP));
				
				DataContainerContactImage img 	= new DataContainerContactImage(cardImageID, path, md5check);
				img.image_group					= group;
				img.id							= img_id;
				img.contact_stash_avatar_id		= contact_stash_img_id;
				cData.cardImg = img;
			}
		}
		
		String[] whereArgs  					= new String[] { Long.toString(cData.id) };
		String[] projection 					= null;
		String whereClause  					= IMAGES_C_GROUP+"=?";
		
		Cursor cImg = db.query(IMAGES, projection, whereClause, whereArgs, null, null, null);
		while(cImg.moveToNext()) {
			int img_id				= cImg.getInt(		cImg.getColumnIndex(IMAGES_C_ID));
			int contact_stash_img_id = cImg.getInt(	cImg.getColumnIndex(IMAGES_C_CONTACTSTASH_IMG_ID));
			String path 			= cImg.getString(	cImg.getColumnIndex(IMAGES_C_PATH));
			String md5check 		= cImg.getString(	cImg.getColumnIndex(IMAGES_C_MD5CHECK));
			int group				= cImg.getInt(		cImg.getColumnIndex(IMAGES_C_GROUP));
			
			DataContainerContactImage img 	= new DataContainerContactImage(cardImageID, path, md5check);
			img.image_group					= group;
			img.id							= img_id;
			img.contact_stash_avatar_id		= contact_stash_img_id;
			cData.avatars.add(img);
		}
		cImg.close();
		
		DataContainerContactData 	data 	= new DataContainerContactData();
		int count 							= cData.avatars.size(); 
		data.avatars						= new int[count];
		for (int i=0; i<count; i++) {
			data.avatars[i] 				= cData.avatars.get(i).contact_stash_avatar_id;
		}
		
		whereArgs = new String[] { Long.toString(cData.id) };
		projection = null;
		whereClause = DATAFIELD_C_CONTACT_ID+"=?";
		
		Cursor dataCursor = db.query(DATAFIELD, projection, whereClause, whereArgs, null, null, null);
		DataContainerContactBox		box	 = new DataContainerContactBox();
		ArrayList<DataFieldStorage> stored = new ArrayList<DataBaseAccessor.DataFieldStorage>();
		while(dataCursor.moveToNext()) {
			DataFieldStorage ds = new DataFieldStorage();
			ds.fName 	= dataCursor.getString(dataCursor.getColumnIndex(DATAFIELD_C_FIELD_TYPE));
			ds.fValue 	= dataCursor.getString(dataCursor.getColumnIndex(DATAFIELD_C_VALUE));
			ds.x 		= dataCursor.getFloat(dataCursor.getColumnIndex(DATAFIELD_C_X));
			ds.y 		= dataCursor.getFloat(dataCursor.getColumnIndex(DATAFIELD_C_Y));
			ds.width	= dataCursor.getFloat(dataCursor.getColumnIndex(DATAFIELD_C_WIDTH));
			ds.height	= dataCursor.getFloat(dataCursor.getColumnIndex(DATAFIELD_C_HEIGHT));
			try {
				Field field 	= data.getClass().getDeclaredField(ds.fName);
				if(field.getType().equals(String[].class)) {
					stored.add(ds); //store data that will be inserted to arrays
				}
				else if(field.getType().equals(int.class)) {
					field.setInt(data, Integer.valueOf(ds.fValue));
				}
				else if(field.getType().equals(double.class)){
					field.setDouble(data, Double.valueOf(ds.fValue));
				}
				else if(field.getType().equals(String.class)){
					field.set(data, ds.fValue);
//					if(ds.fName.equals("person_facebook")) {
//						l.i("field name : "+ ds.fName +" value: "+ ds.fValue);
//					}
					try {
						Field boxfield 	= box.getClass().getDeclaredField(ds.fName);
						boxfield.set(box, new float[]{ds.x, ds.y, ds.width, ds.height});
					} catch (Exception e) {}
				}
				else if(field.getType().equals(Date.class)){
					Date created = DataContainerContact.dateFormat.parse(ds.fValue);
					field.set(data, created); 
				}
				else {
					
				}
			} catch (Exception e) { /*l.e(e);*/ }
		}
		dataCursor.close();

		while(!stored.isEmpty()) {
			ArrayList<DataFieldStorage> fieldStorage 	= new ArrayList<DataBaseAccessor.DataFieldStorage>();
			String fieldName 							= stored.get(0).fName;
//			l.d("grouping fieldName: "+ fieldName);
			for (DataFieldStorage df : stored) {
				if(df.fName.equals(fieldName)) {
					fieldStorage.add(df);
				}
			}
			String[] 	dfValues = new String[fieldStorage.size()];
			float[][] 	bfValues = new float[fieldStorage.size()][4];
			try {
				Field dataField 	= data.getClass().getDeclaredField(fieldStorage.get(0).fName);
				dataField.set(data, dfValues);
				Field boxField		= box.getClass().getDeclaredField(fieldStorage.get(0).fName);
				boxField.set(box, bfValues);
				int index = 0;
				for (DataFieldStorage df : fieldStorage) {
					dfValues[index] = df.fValue;
					bfValues[index] = new float[] {df.x, df.y, df.width, df.height};
					index++;
					stored.remove(df);
					
				}
			} catch (Exception e) { 
//				l.e(e);
				for (DataFieldStorage df : fieldStorage) {
					stored.remove(df);
				}
			}
		}
		cData.boxes 	= box;
		cData.data 		= data;
		return cData;
	}

	public Vector<DataContainerContact> getContactsDataFiltered() { //TODO do not use threads int databaseaccessor
		synchronized (this) {
			SQLiteDatabase db 								= getReadableDatabase();
			Vector<DataContainerContact> contactsData 		= new  Vector<DataContainerContact>();
			
			String[] projection 							= new String[] { CONTACT_C_ID };
			Cursor cursor 									= db.query(CONTACT, projection, null, null, null, null, null);
			while(cursor.moveToNext()) {
				int id 										= cursor.getInt(cursor.getColumnIndex(CONTACT_C_ID));
				DataContainerContact cData 					= getContactData(id, db);
				if(cData.data.processing_status.equals(STATUS_RECOGNIZED) || cData.data.processing_status.equals(STATUS_UPDATE) || cData.data.processing_status.equals(STATUS_UPDATED)) {
					contactsData.add(cData);
				}
			}
			db.close();
			return contactsData;
		}
	}
	
	public Vector<DataContainerContact> getContactsData() {
		synchronized (this) {
			SQLiteDatabase db 								= getReadableDatabase();
			Vector<DataContainerContact> contactsData 		= new  Vector<DataContainerContact>();
			
			String[] projection 							= new String[] { CONTACT_C_ID };
			Cursor cursor 									= db.query(CONTACT, projection, null, null, null, null, null);
			while(cursor.moveToNext()) {
				int id 										= cursor.getInt(cursor.getColumnIndex(CONTACT_C_ID));
				DataContainerContact cData 					= getContactData(id, db);
				contactsData.add(cData);
			}
			db.close();
			return contactsData;
		}
		
	}
	
	private class DataFieldStorage{
		public String fName, fValue;
		public float x, y, width, height;
	}

	public void removeImageData(int contactstash_img_id) {
		synchronized (this) {
			SQLiteDatabase db 								= getWritableDatabase();
			removeImageData(contactstash_img_id, db);
			db.close();
		}
	}
	
	private void removeImageData(int contactstash_img_id, SQLiteDatabase db) {
		String[] whereArgs  					= new String[] { Long.toString(contactstash_img_id) };
		String whereClause  					= IMAGES_C_CONTACTSTASH_IMG_ID +"=?";
		int deletedRows 						= db.delete(IMAGES, whereClause, whereArgs);
	}

	public void deleteSocialMediaData(String socialmedia) {
		String[] whereArgs  					= new String[] { socialmedia };
		String whereClause  					= SOCIAL_NETWORK_C_ID +"=?";
		synchronized (this) {
			SQLiteDatabase db 						= getWritableDatabase();
			int deletedRows 						= db.delete(SOCIAL_NETWORK, whereClause, whereArgs);
	//		l.d("deletedRows: "+ deletedRows);
			db.close();
		}
	}

	public int[] getUnFinishedScanIDs() {
//		l.i("test: getUnFinishedScanIDs");
		synchronized (this) {
			SQLiteDatabase db 						= getReadableDatabase();
			String[] whereArgs = new String[] { "status", STATUS_UNPROCESSED, STATUS_PROCESSED }; //
			String[] projection = new String[] { DATAFIELD_C_CONTACT_ID };
			String whereClause = DATAFIELD_C_FIELD_TYPE+"=? AND ("+ DATAFIELD_C_VALUE +"=? OR "+ DATAFIELD_C_VALUE +"=?)";
	
			Cursor cursor = db.query(DATAFIELD, projection, whereClause, whereArgs, null, null, null);
			int count = cursor.getCount();
	//		l.i("test found "+ count +" unfinished contacts");
			int[] ids = new int[count];
			int index = 0;
			int colIndex = cursor.getColumnIndex(DATAFIELD_C_CONTACT_ID);
			while(cursor.moveToNext()) {
				ids[index] = cursor.getInt(colIndex);
				index++;
			}
			return ids;
		}
	}
	
	public interface DataBaseListener{
		public void onDataChange();
	}

	public int getUnfinishedScanCount() {
		synchronized (this) {
			SQLiteDatabase db 								= getReadableDatabase();
//			l.i("after get db: "+ db);
			ArrayList<DataContainerContact> unfinishedCData = new  ArrayList<DataContainerContact>();
			String[] projection 							= new String[] { CONTACT_C_ID };
			Cursor cursor 									= db.query(CONTACT, projection, null, null, null, null, null);
			while(cursor.moveToNext()) {
				int id 										= cursor.getInt(cursor.getColumnIndex(CONTACT_C_ID));
				DataContainerContact dc 					= getContactData(id, db);
				if(dc == null || dc.data.processing_status == null) {
					break;
				}
				if(!(dc.data.processing_status.equals(STATUS_RECOGNIZED) || dc.data.processing_status.equals(STATUS_UNRECOGNIZABLE))) {
//					l.i("Test getUnfinishedScanCount cData :\n"+ dc.toString());
					unfinishedCData.add(getContactData(id, db));	
				}
			}
			try {
//				l.i("before close db: "+ db);
				db.close();
			}catch (NullPointerException e) {
				// fucking synch error
				return -1;
			}
			return unfinishedCData.size();
		}
	}

	public void removeContactData(DataContainerContact cData) {
		synchronized (this) {
			SQLiteDatabase db = getWritableDatabase();
			removeContactData(cData, db);
			db.close();
		}
	}
	
	public void removeContactData(DataContainerContact cData, SQLiteDatabase db) {
		String whereClause = CONTACT_C_ID +"=?";
		String[] whereArgs = new String[] { Integer.toString(cData.id) };
		synchronized (this) {
			db.delete(CONTACT, whereClause, whereArgs);
			if(cData.cardImg != null) {
				removeImageData(cData.cardImg.id, db);
			}
			for (DataContainerContactImage img : cData.avatars) {
				removeImageData(img.id, db);
			}
			removeContactDataFieldData(cData, db);
		}
	}
	
	public void removeContactDataFieldData(DataContainerContact cData) {
		synchronized (this) {
			SQLiteDatabase db = getWritableDatabase();
			removeContactDataFieldData(cData, db);
			db.close();
		}
	}
	
	private void removeContactDataFieldData(DataContainerContact cData, SQLiteDatabase db) {
		String whereClause = DATAFIELD_C_CONTACT_ID +"=?";
		String[] whereArgs = new String[] { Integer.toString(cData.id) };
		db.delete(DATAFIELD, whereClause, whereArgs);
	}
}