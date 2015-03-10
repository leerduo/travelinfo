package com.ustc.dystu.dandelion.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DandDB extends SQLiteOpenHelper {

	private static final String TAG = DandDB.class.getSimpleName();

	private static Context sContext;

	public final static String DB_NAME = "danddb";

	private final static int VERSION = 1;

	private static DandDB instance = null;

	private SQLiteDatabase db = null;

	// cache table
	private final static String CACHE_TABLE = "cache_table";
	public final static String CACHE_KEY = "id";
	public final static String CACHE_VALUE = "value";
	public final static String CACHE_TIME = "time";

	private DandDB(Context context) {
		super(context, DB_NAME, null, VERSION);
		sContext = context;

		try {
			db = this.getWritableDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static synchronized DandDB getInstance(Context ctx) {
		if (instance == null) {
			return instance = new DandDB(ctx);
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String cache_sql = "CREATE TABLE IF NOT EXISTS " + CACHE_TABLE + " ("
				+ CACHE_KEY + " Varchar(255) UNIQUE, " + CACHE_VALUE + " TEXT, "
				+ CACHE_TIME + " Varchar(255) )";

		db.execSQL(cache_sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	public boolean updateCache(String key, String value) {
		try {
			ContentValues c = new ContentValues();
			c.put(CACHE_KEY, key);
			c.put(CACHE_VALUE, value);
			c.put(CACHE_TIME, System.currentTimeMillis() + "");

			long i = db.replace(CACHE_TABLE, null, c);

			if (i > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public Object[] getCache(String key) {

		Cursor cursor = null;
		try {
			Object[] obj = new Object[2];

			String sql = "select * from " + CACHE_TABLE + " where " + CACHE_KEY
					+ " = ?";

			cursor = db.rawQuery(sql, new String[] { key });

			if (cursor.moveToFirst()) {

				String value = cursor.getString(cursor
						.getColumnIndex(CACHE_VALUE));
				String time = cursor.getString(cursor
						.getColumnIndex(CACHE_TIME));

				obj[0] = value;
				obj[1] = time;

				return obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return null;
	}

}
