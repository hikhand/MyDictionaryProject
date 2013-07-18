package ir.khaled.mydictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khaled on 4/07/13 at 2:12 AM.
 */
class DatabaseHandlerLeitnerIndexesStatus extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "leitner.db";

    // Contacts table name
    private static final String TABLE_ITEMS = "indexesStatus";

    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_LAST_DAY = "status";

    public DatabaseHandlerLeitnerIndexesStatus(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        DATABASE_NAME = databaseName;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s " + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER)",
                TABLE_ITEMS, KEY_ID, KEY_LAST_DAY);
        db.execSQL(sql);

        for (int i = 0; i < 31; i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_LAST_DAY, "false");
            db.insert(TABLE_ITEMS, null, values);
        }

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("drop if exists" + TABLE_ITEMS);

        // Create tables again
        onCreate(db);
    }



    // Getting single Item
    public boolean getStatus(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ITEMS, new String[]{KEY_ID,
                KEY_LAST_DAY}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        cursor.moveToFirst();

        boolean item = false;
        if (cursor.getCount() > 0 && cursor != null) {
            item = Boolean.parseBoolean(cursor.getString(1));
        }
        cursor.close();
        db.close();
        // return item
        return item;
    }



    // Getting All items
    public ArrayList<Boolean> getAllItems() {
        ArrayList<Boolean> itemsList = new ArrayList<Boolean>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ITEMS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                boolean item = Boolean.parseBoolean(cursor.getString(1)); //name
                itemsList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return contact list
        return itemsList;
    }


    // Updating single contact
    public int updateItem(int id, boolean newLastDay) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LAST_DAY, newLastDay);


        return db.update(TABLE_ITEMS, values, KEY_ID + "=" + id, null);
    }


    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, null, null);
    }



}