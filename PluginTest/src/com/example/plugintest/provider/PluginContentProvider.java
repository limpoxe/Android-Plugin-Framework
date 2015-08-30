package com.example.plugintest.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import com.example.plugintest.provider.PluginDbTables.PluginFirstTable;

public class PluginContentProvider extends ContentProvider {

	private static final String DATABASE_NAME = "plugin_test.db";
	private static final int DATABASE_VERSION = 2;

	private static final String TEST_FIRST_TABLENAME = "first_table";
	private static final int TEST_FIRST = 101;
	private static final int TEST_FIRST_ID = 201;
	private static final String[] sTestFirstProjection;

	private static final UriMatcher sUriMatcher;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TEST_FIRST_TABLENAME + "("
					+ PluginFirstTable._ID + " INTEGER PRIMARY KEY, "
					+ PluginFirstTable.MY_FIRST_PLUGIN_NAME + " TEXT, "
					+ PluginFirstTable.CREATED_DATE + " INTEGER, "
					+ PluginFirstTable.MODIFIED_DATE + " INTEGER );");
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TEST_FIRST_TABLENAME + "("
					+ PluginFirstTable._ID + " INTEGER PRIMARY KEY, "
					+ PluginFirstTable.MY_FIRST_PLUGIN_NAME + " TEXT, "
					+ PluginFirstTable.CREATED_DATE + " INTEGER, "
					+ PluginFirstTable.MODIFIED_DATE + " INTEGER );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + TEST_FIRST_TABLENAME);
			onCreate(db);
		}
	}

	private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		String TABLE_NAME;
		switch (sUriMatcher.match(uri)) {
		case TEST_FIRST:
			TABLE_NAME = TEST_FIRST_TABLENAME;
			if (projection == null || projection.length == 0) {
				projection = sTestFirstProjection;
			}
			if (TextUtils.isEmpty(sortOrder)) {
				sortOrder = PluginFirstTable.DEFAULT_SORT_ORDER;
			}
			break;

		case TEST_FIRST_ID:
			TABLE_NAME = TEST_FIRST_TABLENAME;
			if (projection == null || projection.length == 0) {
				projection = sTestFirstProjection;
			}
			if (TextUtils.isEmpty(sortOrder)) {
				sortOrder = PluginFirstTable.DEFAULT_SORT_ORDER;
			}
			if (TextUtils.isEmpty(selection)) {
				selection = PluginFirstTable._ID + " = " + uri.getPathSegments().get(1);
			} else {
				selection += " AND " + PluginFirstTable._ID + " = " + uri.getPathSegments().get(1);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, projection, selection,
			selectionArgs, null, null, sortOrder);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		String TABLE_NAME;
		String nullColumnHack;
		Resources r = Resources.getSystem();
		if (values == null) {
			values = new ContentValues();
		}

		Long now = Long.valueOf(System.currentTimeMillis());

		switch (sUriMatcher.match(uri)) {
		case TEST_FIRST:
			TABLE_NAME = TEST_FIRST_TABLENAME;
			nullColumnHack = PluginFirstTable.MY_FIRST_PLUGIN_NAME;
			if (values.containsKey(PluginFirstTable.MY_FIRST_PLUGIN_NAME) == false) {
				values.put(PluginFirstTable.MY_FIRST_PLUGIN_NAME, r.getString(android.R.string.untitled));
			}
			if (values.containsKey(PluginFirstTable.CREATED_DATE) == false) {
				values.put(PluginFirstTable.CREATED_DATE, now);
			}
			if (values.containsKey(PluginFirstTable.MODIFIED_DATE) == false) {
				values.put(PluginFirstTable.MODIFIED_DATE, now);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(TABLE_NAME, nullColumnHack, values);
		if (rowId > 0) {
			Uri returnUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(returnUri, null);
			return returnUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String TABLE_NAME;
		int count;

		switch (sUriMatcher.match(uri)) {
		case TEST_FIRST:
			TABLE_NAME = TEST_FIRST_TABLENAME;
			break;

		case TEST_FIRST_ID:
			TABLE_NAME = TEST_FIRST_TABLENAME;
			selection = PluginFirstTable._ID + "=" + uri.getPathSegments().get(1)
				+ (!TextUtils.isEmpty(selection) ? " AND (" + selection	+ ')' : "");
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		count = db.delete(TABLE_NAME, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Long now = Long.valueOf(System.currentTimeMillis());

		String TABLE_NAME;
		int count;

		switch (sUriMatcher.match(uri)) {
		case TEST_FIRST:
			TABLE_NAME = TEST_FIRST_TABLENAME;
			if (values.containsKey(PluginFirstTable.MODIFIED_DATE) == false) {
				values.put(PluginFirstTable.MODIFIED_DATE, now);
			}
			break;

		case TEST_FIRST_ID:
			TABLE_NAME = TEST_FIRST_TABLENAME;
			if (values.containsKey(PluginFirstTable.MODIFIED_DATE) == false) {
				values.put(PluginFirstTable.MODIFIED_DATE, now);
			}
			selection = PluginFirstTable._ID + "=" + uri.getPathSegments().get(1)
				+ (!TextUtils.isEmpty(selection) ? " AND (" + selection	+ ')' : "");
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		count = db.update(TABLE_NAME, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(PluginDbTables.AUTHORITY, "pluginfirst", TEST_FIRST);
		sUriMatcher.addURI(PluginDbTables.AUTHORITY, "pluginfirst/#", TEST_FIRST_ID);

		sTestFirstProjection = new String[] { PluginFirstTable._ID,
				PluginFirstTable.MY_FIRST_PLUGIN_NAME,
				PluginFirstTable.CREATED_DATE, PluginFirstTable.MODIFIED_DATE };
	}

}