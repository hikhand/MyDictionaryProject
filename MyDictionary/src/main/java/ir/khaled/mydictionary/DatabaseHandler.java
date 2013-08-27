package ir.khaled.mydictionary;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.net.PortUnreachableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by khaled on 6/28/13.
 */
class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    public static final String DATABASE_NAME = "items.db";

    // Contacts table name
    private static final String TABLE_WORDS = "words";
    private static final String TABLE_TAGS = "tags";

    private static final String KEY_TAG = "tagName";

    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_WORD = "word";
    private static final String KEY_MEANING = "meaning";
    private static final String KEY_EXAMPLE = "examples";
    private static final String KEY_DATE = "date";
    private static final String KEY_DATE_LAST = "lastDate";
    private static final String KEY_COUNT = "count";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s " + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER)",
                TABLE_WORDS, KEY_ID, KEY_WORD, KEY_MEANING, KEY_EXAMPLE, KEY_TAG, KEY_DATE, KEY_DATE_LAST, KEY_COUNT);
        db.execSQL(sql);
    }

    // Upgrading databaseMain
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        List<Custom> itemsList = new ArrayList<Custom>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_WORDS;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Custom item = new Custom(cursor.getInt(0), cursor.getString(1), cursor.getString(2), "", "", cursor.getString(3), cursor.getString(3), cursor.getInt(4));
                itemsList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();


        db.execSQL("drop table if exists '" + TABLE_WORDS+"'");

        // Create tables again
        onCreate(db);

        ContentValues values = new ContentValues();
        for (Custom item : itemsList) {
            values.put(KEY_WORD, item.getWord());
            values.put(KEY_MEANING, item.getMeaning());
            values.put(KEY_EXAMPLE, item.getExample());
            values.put(KEY_TAG, item.getTags());
            values.put(KEY_DATE, item.getDate());
            values.put(KEY_DATE_LAST, item.getLastDate());
            values.put(KEY_COUNT, item.getCount());
            db.insert(TABLE_WORDS, null, values);
        }
    }



    // Adding new item
    public void addItem(Custom item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_WORD, item.getWord());
        values.put(KEY_MEANING, item.getMeaning());
        values.put(KEY_EXAMPLE, item.getExample());
        values.put(KEY_TAG, item.getTags());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_DATE_LAST, item.getLastDate());
        values.put(KEY_COUNT, item.getCount());

        db.insert(TABLE_WORDS, null, values);
        db.close();

    }


    // Getting single Item
    public Custom getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_WORDS, new String[]{KEY_ID,
                KEY_WORD, KEY_MEANING, KEY_EXAMPLE, KEY_TAG, KEY_DATE, KEY_DATE_LAST, KEY_COUNT}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        cursor.moveToFirst();

        Custom item = null;
        if (cursor.getCount() > 0 && cursor != null) {
            item = new Custom(
                    Integer.parseInt(cursor.getString(0)),//id
                    cursor.getString(1), //word
                    cursor.getString(2), //meaning
                    cursor.getString(3), //meaning
                    cursor.getString(4), //meaning
                    cursor.getString(5), //date
                    cursor.getString(6), //date
                    cursor.getInt(7));
        }
        cursor.close();
        db.close();
        // return item
        return item;
    }


//    // Getting single Item's word
//    public String getItemWord(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_WORDS, new String[] { KEY_ID,
//                KEY_WORD, KEY_MEANING, KEY_DATE, KEY_COUNT}, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//
//        if (cursor != null)
//            cursor.moveToFirst();
//
//
//        // return item's word
//        return cursor.getString(1);
//    }
//
//    // Getting single Item's word
//    public String getItemMeaning(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_WORDS, new String[] { KEY_ID,
//                KEY_WORD, KEY_MEANING, KEY_DATE, KEY_COUNT}, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//
//        if (cursor != null)
//            cursor.moveToFirst();
//
//
//        // return item's word
//        return cursor.getString(2);
//    }
//
//    // Getting single Item's word
//    public String getItemDate(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_WORDS, new String[] { KEY_ID,
//                KEY_WORD, KEY_MEANING, KEY_DATE, KEY_COUNT}, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//
//        if (cursor != null)
//            cursor.moveToFirst();
//
//
//        // return item's word
//        return cursor.getString(3);
//    }
//
//    // Getting single Item's word
//    public int getItemCount(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_WORDS, new String[] { KEY_ID,
//                KEY_WORD, KEY_MEANING, KEY_DATE, KEY_COUNT}, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//
//        if (cursor != null)
//            cursor.moveToFirst();
//
//
//        // return item's word
//        return cursor.getInt(4);
//    }
//

    // Getting single Item's word
    public int getItemId(String word, String meaning) {
        SQLiteDatabase db = this.getReadableDatabase();



        Cursor cursor = db.query(TABLE_WORDS, new String[] { KEY_ID,
                KEY_WORD, KEY_MEANING, KEY_EXAMPLE, KEY_TAG, KEY_DATE, KEY_DATE_LAST, KEY_COUNT}, KEY_WORD +"=? AND " + KEY_MEANING +"=?",
                new String[] {word, meaning}, null, null, null, null);
        if( cursor != null && cursor.moveToFirst()  ){
            Log.i("getItem's Id", "word: " + word + "  meaning: " + meaning + " = " + Integer.toString(cursor.getInt(0)));
            return cursor.getInt(0);
        }

        // return item's word
        return 123456;
    }


    // Getting All items
    public ArrayList<Custom> getAllItems() {
        ArrayList<Custom> itemsList = new ArrayList<Custom>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_WORDS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Custom item = new Custom(
                        Integer.parseInt(cursor.getString(0)),//id
                        cursor.getString(1), //word
                        cursor.getString(2), //meaning
                        cursor.getString(3), //meaning
                        cursor.getString(4), //meaning
                        cursor.getString(5), //date
                        cursor.getString(6), //date
                        cursor.getInt(7));
                itemsList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // return contact list
        return itemsList;
    }


    // Getting contacts Count
    public int getItemsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_WORDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        Log.d("myTag2", Integer.toString(count));
        // return count
        return count;
    }

    // Updating single contact
    public int updateItem(Custom item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WORD, item.getWord());
        values.put(KEY_MEANING, item.getMeaning());
        values.put(KEY_EXAMPLE, item.getExample());
        values.put(KEY_TAG, item.getTags());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_DATE_LAST, item.getLastDate());
        values.put(KEY_COUNT, item.getCount());

        Log.i("in update method", "id: " + item.getId());
        return db.update(TABLE_WORDS, values, KEY_ID + "=" + item.getId(), null);
    }



    // Deleting single contact
    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_WORDS, KEY_ID + " = ?",
//                new String[] { String.valueOf(id) });

        try {db.delete(TABLE_WORDS, KEY_ID + "=" + Integer.toString(id), null);}
        catch (Exception e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
        db.close();
    }


    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORDS, null, null);
    }


    public ArrayList<String> getTags(boolean first) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s " + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT)",
                TABLE_TAGS, KEY_ID, KEY_TAG);

        db.execSQL(sql);



        String itemI = null;
        try {
            Cursor cursor = db.query(TABLE_TAGS, new String[]{KEY_ID, KEY_TAG}, KEY_ID + "=?",
                    new String[]{String.valueOf(1)}, null, null, null, null);

            cursor.moveToFirst();
            if (cursor.getCount() > 0 && cursor != null) {
                itemI = cursor.getString(1);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (itemI == null) {
            ContentValues values = new ContentValues();
                values.put(KEY_TAG, "default");
                db.insert(TABLE_TAGS, null, values);
            }

        ArrayList<String> itemsList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TAGS;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String item = cursor.getString(1); //name
                itemsList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return contact list
        return itemsList;
    }

    public void addTag (String tagName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();


        values.put(KEY_TAG, tagName);

        db.insert(TABLE_TAGS, null, values);
    }

    public int updateTag(int id, String tagName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TAG, tagName);

        return db.update(TABLE_TAGS, values, KEY_ID + "=" + id, null);
    }

    public void deleteTag(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {db.delete(TABLE_TAGS, KEY_ID + "=" + Integer.valueOf(id), null);}
        catch (Exception e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public int getTagId(String tagName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TAGS, new String[] { KEY_ID,
                KEY_TAG}, KEY_TAG +"=?",
                new String[] {tagName}, null, null, null, null);
        if( cursor != null && cursor.moveToFirst()  ){
            return cursor.getInt(0);
        }
        // return item's word
        return -1;
    }


}