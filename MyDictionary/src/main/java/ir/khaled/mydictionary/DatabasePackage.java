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
class DatabasePackage extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;

    public static String DATABASE_NAME;

    // Contacts table name
//    private static final String TABLE_LEITNER = "leitner";

    private static final String TABLE_ARCHIVE = "archive";




    Names v;

    public DatabasePackage(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
        v = new Names();
        DATABASE_NAME = databaseName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }


    public void addItem(ItemPackage item, String table) {
        SQLiteDatabase db = this.getWritableDatabase();
//        assert db != null;

        ContentValues values = new ContentValues();

        values.put(v.KEY_NAME, item.getName());
        values.put(v.KEY_MEANING_EN, item.getMeaningEn());
        values.put(v.KEY_MEANING_FA, item.getMeaningFa());
        values.put(v.KEY_EXAMPLES_EN, item.getExamplesEn());
        values.put(v.KEY_EXAMPLES_FA, item.getExamplesFa());
        values.put(v.KEY_lAST_DATE, item.getLastCheckDate());
        values.put(v.KEY_lAST_DAY, item.getLastCheckDay());
        values.put(v.KEY_DECK, item.getDeck());
        values.put(v.KEY_INDEX, item.getIndex());
        values.put(v.KEY_COUNT_CORRECT, item.getCountCorrect());
        values.put(v.KEY_COUNT_INCORRECT, item.getCountInCorrect());
        values.put(v.KEY_COUNT, item.getCount());

        Log.d("id value : ", Integer.toString(item.getId()));

        db.insert(table, null, values);
        db.close();
    }

    public int updateItem(ItemPackage item, String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(v.KEY_NAME, item.getName());
        values.put(v.KEY_MEANING_EN, item.getMeaningEn());
        values.put(v.KEY_MEANING_FA, item.getMeaningFa());
        values.put(v.KEY_EXAMPLES_EN, item.getExamplesEn());
        values.put(v.KEY_EXAMPLES_FA, item.getExamplesFa());
        values.put(v.KEY_lAST_DATE, item.getLastCheckDate());
        values.put(v.KEY_lAST_DAY, item.getLastCheckDay());
        values.put(v.KEY_DECK, item.getDeck());
        values.put(v.KEY_INDEX, item.getIndex());
        values.put(v.KEY_COUNT_CORRECT, item.getCountCorrect());
        values.put(v.KEY_COUNT_INCORRECT, item.getCountInCorrect());
        values.put(v.KEY_COUNT, item.getCount());

        Log.i("in update method", "id: " + item.getId());
        return db.update(table, values, v.KEY_ID + "=" + item.getId(), null);
    }


    public ItemPackage getItem(int id, String table) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(table, new String[]{v.KEY_ID,
                v.KEY_NAME, v.KEY_MEANING_EN, v.KEY_MEANING_FA, v.KEY_EXAMPLES_EN, v.KEY_EXAMPLES_FA, v.KEY_lAST_DATE,
                v.KEY_lAST_DAY, v.KEY_DECK, v.KEY_INDEX, v.KEY_COUNT_CORRECT, v.KEY_COUNT_INCORRECT, v.KEY_COUNT}, v.KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        cursor.moveToFirst();

        ItemPackage item = null;
        if (cursor.getCount() > 0 && cursor != null) {
            item = new ItemPackage(
                    Integer.parseInt(cursor.getString(0)),//id
                    cursor.getString(1), //name
                    cursor.getString(2), //meaningEn
                    cursor.getString(3), //meaningFa
                    cursor.getString(4), //meaningEn
                    cursor.getString(5), //meaningFa
                    cursor.getString(6), //last check date

                    Integer.parseInt(cursor.getString(7)),//last check day
                    Integer.parseInt(cursor.getString(8)),//deck
                    Integer.parseInt(cursor.getString(9)),//index
                    Integer.parseInt(cursor.getString(10)),//count correct
                    Integer.parseInt(cursor.getString(11)),//count incorrect
                    Integer.parseInt(cursor.getString(12)));//count
        }
        cursor.close();
        db.close();
        return item;
    }


    public ArrayList<ItemPackage> getAllItems(String table) {
        ArrayList<ItemPackage> itemsList = new ArrayList<ItemPackage>();
        String selectQuery = "SELECT  * FROM " + table;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ItemPackage item = new ItemPackage(
                        Integer.parseInt(cursor.getString(0)),//id
                        cursor.getString(1), //name
                        cursor.getString(2), //meaningEn
                        cursor.getString(3), //meaningFa
                        cursor.getString(4), //meaningEn
                        cursor.getString(5), //meaningFa
                        cursor.getString(6), //last check date

                        Integer.parseInt(cursor.getString(7)),//last check day
                        Integer.parseInt(cursor.getString(8)),//deck
                        Integer.parseInt(cursor.getString(9)),//index
                        Integer.parseInt(cursor.getString(10)),//count correct
                        Integer.parseInt(cursor.getString(11)),//count incorrect
                        Integer.parseInt(cursor.getString(12)));//count

                itemsList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return itemsList;
    }


    public int getItemsCount(String table) {
        String countQuery = "SELECT  * FROM " + table;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        Log.d("myTag2", Integer.toString(count));
        // return count
        return count;
    }


    public int updatePosition(int id, int deck, int index) {
        v = new Names();
        ItemPackage item = getItem(id, v.TABLE_LEITNER);

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(v.KEY_NAME, item.getName());
        values.put(v.KEY_MEANING_EN, item.getMeaningEn());
        values.put(v.KEY_MEANING_FA, item.getMeaningFa());
        values.put(v.KEY_EXAMPLES_EN, item.getExamplesEn());
        values.put(v.KEY_EXAMPLES_FA, item.getExamplesFa());
        values.put(v.KEY_lAST_DATE, item.getLastCheckDate());
        values.put(v.KEY_lAST_DAY, item.getLastCheckDay());
        values.put(v.KEY_DECK, deck);
        values.put(v.KEY_INDEX, index);
        values.put(v.KEY_COUNT_CORRECT, item.getCountCorrect());
        values.put(v.KEY_COUNT_INCORRECT, item.getCountInCorrect());
        values.put(v.KEY_COUNT, item.getCount());

        return db.update(v.TABLE_LEITNER, values, v.KEY_ID + "=" + item.getId(), null);
    }



    public void clearTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, null, null);
    }



    public String getLastDate() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(v.TABLE_MAIN, new String[]{v.KEY_ID,
                v.KEY_MAIN_LAST_DATE, v.KEY_MAIN_LAST_DAY}, v.KEY_ID + "=?",
                new String[]{String.valueOf(1)}, null, null, null, null);

        cursor.moveToFirst();

        String item = "today";
        if (cursor.getCount() > 0 && cursor != null) {
            item = cursor.getString(1);
        }
        cursor.close();
        db.close();
        return item;
    }

    public int getLastDay() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(v.TABLE_MAIN, new String[]{v.KEY_ID,
                v.KEY_MAIN_LAST_DATE, v.KEY_MAIN_LAST_DAY}, v.KEY_ID + "=?",
                new String[]{String.valueOf(1)}, null, null, null, null);

        cursor.moveToFirst();

        int item = 0;
        if (cursor.getCount() > 0 && cursor != null) {
            item = Integer.parseInt(cursor.getString(2));
        }
        cursor.close();
        db.close();
        return item;
    }

    public int updateLastDate(String newLastDate) {

        ContentValues values = new ContentValues();
        values.put(v.KEY_MAIN_LAST_DATE, newLastDate);
        values.put(v.KEY_MAIN_LAST_DAY, getLastDay());
        SQLiteDatabase db = this.getWritableDatabase();

        return db.update(v.TABLE_MAIN, values, v.KEY_ID + "=" + 1, null);
    }

    public int updateLastDay(int newLastDay) {

        ContentValues values = new ContentValues();
        values.put(v.KEY_MAIN_LAST_DATE, getLastDate());
        values.put(v.KEY_MAIN_LAST_DAY, newLastDay);
        SQLiteDatabase db = this.getWritableDatabase();

        return db.update(v.TABLE_MAIN, values, v.KEY_ID + "=" + 1, null);
    }

    public String getLastDayDate(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(v.TABLE_LAST_CHECK_DAY_DATE, new String[]{v.KEY_ID,
                v.KEY_LAST_DAY_DATE}, v.KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        cursor.moveToFirst();

        String item = "today";
        if (cursor.getCount() > 0 && cursor != null) {
            item = cursor.getString(1);
        }
        cursor.close();
        db.close();
        return item;
    }

    public ArrayList<Integer> getAllItemsLastDay() {
        ArrayList<Integer> itemsList = new ArrayList<Integer>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + v.TABLE_LAST_CHECK_DAY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                int item = Integer.parseInt(cursor.getString(1)); //name
                itemsList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        db.close();
        // return contact list
        return itemsList;
    }

    public ArrayList<String> getAllItemsLastDayDate() {
        ArrayList<String> itemsList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + v.TABLE_LAST_CHECK_DAY_DATE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String item = cursor.getString(1); //name
                itemsList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        db.close();
        // return contact list
        return itemsList;
    }

    public void updateItemLastDays(int id, int newLastDay) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(v.KEY_LAST_DAY, newLastDay);

        int x = db.update(v.TABLE_LAST_CHECK_DAY, values, v.KEY_ID + "=" + id, null);
//        db.close();
    }

    public void updateItemLastDaysDate(int id, String newLastDay) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(v.KEY_LAST_DAY_DATE, newLastDay);

        db.update(v.TABLE_LAST_CHECK_DAY_DATE, values, v.KEY_ID + "=" + id, null);
//        db.close();
    }



    public void updateItemPerDay(int id, String name) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        SQLiteDatabase db = this.getWritableDatabase();

        db.update(v.TABLE_PER_DAY_TODAY, values, v.KEY_ID + "=" + id, null);
    }

    public void updateItemsPerDay(String[] names) {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();

        for (int i = 1; i <= names.length; i++) {
            String name = names[i];
            values.put("name", name);
            db.update(v.TABLE_PER_DAY_TODAY, values, v.KEY_ID + "=" + i, null);
        }
    }

    public ArrayList<String> getItemsPerDay() {
        ArrayList<String> itemsList = new ArrayList<String>();
        String selectQuery = "SELECT  * FROM " + v.TABLE_PER_DAY_TODAY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                itemsList.add(cursor.getString(1));//name
            } while (cursor.moveToNext());
        }
        cursor.close();

        return itemsList;
    }





}