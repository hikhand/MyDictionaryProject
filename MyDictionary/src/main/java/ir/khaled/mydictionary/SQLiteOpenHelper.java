package ir.khaled.mydictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
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
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "itemsManager";

    // Contacts table name
    private static final String TABLE_WORDS = "words";

    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_WORD = "word";
    private static final String KEY_MEANING = "meaning";
    private static final String KEY_DATE = "date";
    private static final String KEY_COUNT = "count";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s " + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER)",
                TABLE_WORDS, KEY_ID, KEY_WORD, KEY_MEANING, KEY_DATE, KEY_COUNT);
        Log.d("myTag!!", "onCreate with SQL: "+sql);
//        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_WORDS + "("
//                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_WORD + " TEXT,"
//                + KEY_MEANING + " TEXT," + KEY_DATE + " TEXT," + KEY_COUNT + " INTEGER" + ")";

//        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(sql);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("drop if exists" + TABLE_WORDS);

        // Create tables again
        onCreate(db);
    }


    // Adding new item
    public void addItem(Custom item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

//        values.put(KEY_ID, item.getId());
        values.put(KEY_WORD, item.getWord());
        values.put(KEY_MEANING, item.getMeaning());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_COUNT, item.getCount());


        Log.d("id value : ", Integer.toString(item.getId()));

//        db.insert(TABLE_WORDS, null, values);
        db.insert(TABLE_WORDS, null, values);
//        db.insertWithOnConflict(TABLE_WORDS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();

//        ContentValues values = new ContentValues();
//        values.put(KEY_WORD, item.getWord()); // Contact Phone Number
//        values.put(KEY_MEANING, item.getMeaning()); // Contact Phone Number
//        values.put(KEY_DATE, item.getDate()); // Contact Phone Number
//        values.put(KEY_COUNT, item.getCount());
//        // Inserting Row
//        db.insert(TABLE_WORDS, null, values);
//        db.close(); // Closing database connection
    }


    // Getting single Item
    public Custom getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_WORDS, new String[]{KEY_ID,
                KEY_WORD, KEY_MEANING, KEY_DATE, KEY_COUNT}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        cursor.moveToFirst();
        if (cursor == null ){

        }
        Log.d("cursor word's text", cursor.getString(1));


        Custom item = null;
        if (cursor.getCount() > 0 && cursor != null) {
            item = new Custom(
//                    Integer.parseInt(cursor.getString(0)),//id
                    cursor.getString(1), //word
                    cursor.getString(2), //meaning
                    cursor.getString(3), //date
                    cursor.getInt(4));
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
                KEY_WORD, KEY_MEANING, KEY_DATE, KEY_COUNT}, KEY_WORD +"=? AND " + KEY_MEANING +"=?",
                new String[] {word, meaning}, null, null, null, null);
        if( cursor != null && cursor.moveToFirst()  ){
            Log.i("getItem's Id", "word: " + word + "  meaning: " + meaning + " = " + Integer.toString(cursor.getInt(0)));
            return cursor.getInt(0);
        }

        // return item's word
        return 123456;
    }


    // Getting All items
    public List<Custom> getAllItems() {
        List<Custom> itemsList = new ArrayList<Custom>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_WORDS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Custom item = new Custom(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
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
        values.put(KEY_DATE, item.getDate());
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



}