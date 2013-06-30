package ir.khaled.mydictionary;

/**
 * Created by khaled on 6/30/13.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseHandler1 {

    private final String TAG 	 = "DatabaseHandler";
    private static final String KEY_WORD   = "word";
    private static final String KEY_MEANING  = "meaning";
    private static final String KEY_DATE  = "date";
    private static final String KEY_COUNT  = "count";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseHandler1(Context context) {
        dbHelper = new DatabaseHelper(context);
        Log.i(TAG, "Object created.");
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void insertContact(Custom contact) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_WORD,  contact.getWord());
        cv.put(KEY_MEANING,  contact.getMeaning());
        cv.put(KEY_DATE,  contact.getDate());
        cv.put(KEY_COUNT, contact.getCount());
        database.insert(dbHelper.getTableName(), KEY_WORD, cv);

        Log.i(TAG, "Contact added successfully.");
    }

    public void deleteContact(long id) {
        database.delete(dbHelper.getTableName(), dbHelper.getRowIdName() + "=" + id, null);
    }

    public void updateContact(Custom item) {
        ContentValues cv = new ContentValues();

        ContentValues values = new ContentValues();
        values.put(KEY_WORD, item.getWord());
        values.put(KEY_MEANING, item.getMeaning());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_COUNT, item.getCount());

        database.update(dbHelper.getTableName(), cv, dbHelper.getRowIdName() + "=" + item.getId(), null);
    }

    public List<Custom> getAllContacts() {
        List<Custom> contacts = new ArrayList<Custom>();

        Cursor cursor = database.query(dbHelper.getTableName(),	null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Custom contact = cursorToContact(cursor);
            contacts.add(contact);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();

        return contacts;
    }

    public void clearTable() {
        database.delete(dbHelper.getTableName(), null, null);
    }


    private Custom cursorToContact(Cursor cursor) {
        Custom item = new Custom();

//        item.setId(cursor.getLong(0));
        item.setWord(cursor.getString(1));
        item.setMeaning(cursor.getString(2));
        item.setDate(cursor.getString(3));
        item.setCount(cursor.getInt(4));

        return item;
    }
}