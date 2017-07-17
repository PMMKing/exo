package com.haolb.client.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.haolb.client.app.MainApplication;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库操作辅助类 单例模式
 * 
 * @author zexu.ge
 * 
 */
public class DbHelper extends SQLiteOpenHelper {

	private AtomicInteger friendId;
	private AtomicInteger sessionId;
	private AtomicInteger messageId;
//	private AtomicInteger fileFragmentId;

	private static final String DATABASE_NAME = "mixin_db";
	private final static int DATABASE_VERSION = 2;

	public final static String FRIEND_TABLE_NAME = "Friend";
	public final static String SESSION_TABLE_NAME = "Session";
	public final static String MESSAGE_TABLE_NAME = "Message";
//	public final static String FILEFRAGMENT_TABLE_NAME = "FileFragment";
	

	private static final String initSql1 = "create table " + FRIEND_TABLE_NAME
			+ "(_id integer primary key ,"//
			+ "account text,"//
			+ "userId integer,"//
			+ "nickname text,"//
			+ "portrait text,"//
			+ "sex integer,"//
			+ "address text,"//
			+ "firLetter text," +
			"myId integer);";
	
	private static final String initSql2 = "create table " + SESSION_TABLE_NAME
			+ "(_id integer primary key ,"//
			+ "lastTime text,"//
			+ "lastMessage text,"//
			+ "userId integer," +
			"myId integer);";
	
	private static final String initSql3 = "create table " + MESSAGE_TABLE_NAME
			+ "(_id integer primary key ,"//
			+ "content text,"//
			+ "time long,"//
			+ "category integer,"//
			+ "type integer,"//
			+ "status integer,"//
			+ "userId integer," //
			+ "uuid text," //
			+ "filePath text," //
			+ "fileName text," //
			+ "fileSize integer," //
			+ "receiverBytesCount integer," //
			+ "filePercent integer," +
			"myId integer);";
	
	/**
	 * 
	public int _id;
	public int messageId;
	public String filePath;
	public int status;//0未完成,1已完成
	 */
//	private static final String initSql4 = "create table " + FILEFRAGMENT_TABLE_NAME
//			+ "(_id integer primary key ,"//
//			+ "messageId integer,"//
//			+ "filePath text," //
//			+ "status integer);";

	private static DbHelper instance;

	private DbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public static DbHelper getInstance() {
		if (null == instance) {
			synchronized (DbHelper.class) {
				if (null == instance) {
					instance = new DbHelper(MainApplication.getInstance(),
							DATABASE_NAME, null, DATABASE_VERSION);
					instance.initFriendKey();
					instance.initSessionKey();
					instance.initMessageKey();
//					instance.initFileFragmentKey();
				}
			}
		}
		return instance;
	}
	/**
	 * 初始化数据库主键
	 */
	public void initFriendKey() {
		String sql = "select max(_id) from " + FRIEND_TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		int max = 0;
		if (cursor.moveToFirst()) {
			max = cursor.getInt(0);
		}
		cursor.close();
		friendId = new AtomicInteger(max);
	}
	
	/**
	 * 初始化数据库主键
	 */
	public void initSessionKey() {
		String sql = "select max(_id) from " + SESSION_TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		int max = 0;
		if (cursor.moveToFirst()) {
			max = cursor.getInt(0);
		}
		cursor.close();
		sessionId = new AtomicInteger(max);
	}
	
	/**
	 * 初始化数据库主键
	 */
	public void initMessageKey() {
		String sql = "select max(_id) from " + MESSAGE_TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		int max = 0;
		if (cursor.moveToFirst()) {
			max = cursor.getInt(0);
		}
		cursor.close();
		messageId = new AtomicInteger(max);
	}
	
	/**
	 * 初始化数据库主键
	 */
//	public void initFileFragmentKey() {
//		String sql = "select max(_id) from " + FILEFRAGMENT_TABLE_NAME;
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(sql, null);
//		int max = 0;
//		if (cursor.moveToFirst()) {
//			max = cursor.getInt(0);
//		}
//		cursor.close();
//		fileFragmentId = new AtomicInteger(max);
//	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
			db.execSQL(initSql1);
			db.execSQL(initSql2);
			db.execSQL(initSql3);
//			db.execSQL(initSql4);
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String 
		sql = " DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME;
		db.execSQL(sql);
		sql = " DROP TABLE IF EXISTS " + SESSION_TABLE_NAME;
		db.execSQL(sql);
		sql = " DROP TABLE IF EXISTS " + FRIEND_TABLE_NAME;
		db.execSQL(sql);
//		sql = " DROP TABLE IF EXISTS " + FILEFRAGMENT_TABLE_NAME;
//		db.execSQL(sql);
		onCreate(db);
	}

	public AtomicInteger getFriendId() {
		return friendId;
	}
	
	public AtomicInteger getSessionId() {
		return sessionId;
	}
	
	public AtomicInteger getMessageId() {
		return messageId;
	}
	
//	public AtomicInteger getFileFragmentId() {
//		return fileFragmentId;
//	}

}
