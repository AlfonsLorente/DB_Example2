package com.example.db_example2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.nio.charset.CoderMalfunctionError;

public class WordListOpenHelper extends SQLiteOpenHelper {

    // It's a good idea to always define a log tag like this.
    private static final String TAG = WordListOpenHelper.class.getSimpleName();

    // has to be 1 first time or app will crash
    private static final int DATABASE_VERSION = 1;
    private static final String WORD_LIST_TABLE = "word_entries";
    private static final String DATABASE_NAME = "wordlist";

    // Column names...
    public static final String COL_ID = "_id";
    public static final String COL_WORD = "word";

    // ... and a string array of columns.
    private static final String[] COLUMNS = { COL_ID, COL_WORD };

    // Build the SQL query that creates the table.
    private static final String WORD_LIST_TABLE_CREATE =
            "CREATE TABLE " + WORD_LIST_TABLE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY, " + // id will auto-increment if no value passed
                    COL_WORD + " TEXT );";


    private SQLiteDatabase mWritableDB;
    private SQLiteDatabase mReadableDB;


    public WordListOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WORD_LIST_TABLE_CREATE);
        fillDatabaseWithData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(WordListOpenHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + WORD_LIST_TABLE);
        onCreate(db);
    }

    //fill the database with hardcoded data
    private void fillDatabaseWithData(SQLiteDatabase db){
        String[] words = {"Android", "Adapter", "ListView", "AsyncTask",
                "Android Studio", "SQLiteDatabase", "SQLOpenHelper",
                "Data model", "ViewHolder","Android Performance",
                "OnClickListener"};
        // Create a container for the data.
        ContentValues values = new ContentValues();
        for (int i=0; i < words.length; i++) {
            // Put column/value pairs into the container.
            // put() overrides existing values.
            values.put(COL_WORD, words[i]);
            db.insert(WORD_LIST_TABLE, null, values);
        }

    }

    public WordItem query(int position) {
        String query = "SELECT  * FROM " + WORD_LIST_TABLE +
                " ORDER BY " + COL_WORD + " ASC " +
                "LIMIT " + position + ",1";
        Cursor cursor = null;
        WordItem entry = new WordItem();

        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }
            cursor = mReadableDB.rawQuery(query, null);
            cursor.moveToFirst();
            entry.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
            entry.setWord(cursor.getString(cursor.getColumnIndexOrThrow(COL_WORD)));



        } catch (Exception e) {
            Log.d(TAG, "QUERY EXCEPTION: " + e.getMessage());

        } finally {
            cursor.close();
            return entry;
        }
    }

    public long insert(String word){
        long newId = 0;
        ContentValues values = new ContentValues();
        values.put(COL_WORD, word);
        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }
            newId = mWritableDB.insert(WORD_LIST_TABLE, null, values);
        } catch (Exception e) {
            Log.d(TAG, "INSERT EXCEPTION! " + e.getMessage());
        }
        return newId;
    }


    public long count(){
        if (mReadableDB == null) {
            mReadableDB = getReadableDatabase();
        }
        return DatabaseUtils.queryNumEntries(mReadableDB, WORD_LIST_TABLE);
    }


    public int delete(int id) {
        int deleted = 0;
        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }
            String[] args = new String[]{String.valueOf(id)};
            deleted = mWritableDB.delete(WORD_LIST_TABLE, //table name
                    COL_ID + " =? ", args);
        } catch (Exception e) {
            Log.d (TAG, "DELETE EXCEPTION! " + e.getMessage());
        }
        return deleted;
    }

    public int update(int id, String word) {
        int mNumberOfRowsUpdated = -1;
        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }
            ContentValues values = new ContentValues();
            values.put(COL_WORD, word);
            mNumberOfRowsUpdated = mWritableDB.update(WORD_LIST_TABLE,
                    values,
                    COL_ID + " = ?",
                    new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.d (TAG, "UPDATE EXCEPTION! " + e.getMessage());
        }
        return mNumberOfRowsUpdated;
    }


    public Cursor search (String searchString) {
        String[] columns = new String[]{COL_WORD};
        searchString = "%" + searchString + "%";
        String where = COL_WORD + " LIKE ?";
        String[]whereArgs = new String[]{searchString};
        String orderBy = COL_WORD + " ASC";

        Cursor cursor = null;

        try {
            if (mReadableDB == null) mReadableDB = getReadableDatabase();
            cursor = mReadableDB.query(WORD_LIST_TABLE, columns, where, whereArgs,
                    null, null, orderBy);
        } catch (Exception e) {
            Log.d(TAG, "SEARCH EXCEPTION! " + e);
        }

        return cursor;
    }
}
