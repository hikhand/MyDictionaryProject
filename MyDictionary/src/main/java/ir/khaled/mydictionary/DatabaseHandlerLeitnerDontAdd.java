//package ir.khaled.mydictionary;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by khaled on 4/07/13 at 2:12 AM.
// */
//class DatabaseHandlerLeitnerDontAdd extends SQLiteOpenHelper {
//
//    // All Static variables
//    // Database Version
//    private static final int DATABASE_VERSION = 1;
//
//    // Database Name
//    public static final String DATABASE_NAME = "leitner.db";
//
//    // Contacts table name
//    private static final String TABLE_ITEMS = "dontAdd";
//
//    // Contacts Table Columns names
//    private static final String KEY_ID = "_id";
//    private static final String KEY_NAME = "name";
//    private static final String KEY_MEANING = "meaning";
//    private static final String KEY_ADD_DATE = "addDate";
//    private static final String KEY_lAST_DATE = "lastCheckDate";
//    private static final String KEY_lAST_DAY = "lastCheckDay";
//    private static final String KEY_DECK = "deck";
//    private static final String KEY_INDEX = "index1";
//    private static final String KEY_COUNT_CORRECT = "countCorrect";
//    private static final String KEY_COUNT_INCORRECT = "countInCorrect";
//    private static final String KEY_COUNT = "count";
//
//    public DatabaseHandlerLeitnerDontAdd(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
////        DATABASE_NAME = databaseName;
//    }
//
//    // Creating Tables
//    @Override
//    public void onCreate(SQLiteDatabase db) {
////        String sql = String.format("CREATE TABLE %s " + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s TEXT," +
////                " %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER)",
////                TABLE_ITEMS, KEY_ID, KEY_NAME, KEY_MEANING, KEY_ADD_DATE, KEY_lAST_DATE,
////                KEY_lAST_DAY, KEY_DECK, KEY_INDEX, KEY_COUNT_CORRECT, KEY_COUNT_INCORRECT, KEY_COUNT);
////        Log.d("myTag!!", "onCreate with SQL: "+sql);
////        db.execSQL(sql);
//    }
//
//    // Upgrading database
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
////        // Drop older table if existed
//////        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
////        db.execSQL("drop if exists" + TABLE_ITEMS);
////
////        // Create tables again
////        onCreate(db);
//    }
//
//
//    // Adding new item
//    public void addItem(Item item, String tableName) {
//        SQLiteDatabase db = this.getWritableDatabase();
////        assert db != null;
//
//        if (tableName == "leitner" || tableName == "dontAdd") {
//            ContentValues values = new ContentValues();
//            values.put(KEY_NAME, item.getName());
//            values.put(KEY_MEANING, item.getMeaning());
//            values.put(KEY_ADD_DATE, item.getAddDate());
//            values.put(KEY_lAST_DATE, item.getLastCheckDate());
//            values.put(KEY_lAST_DAY, item.getLastCheckDay());
//            values.put(KEY_DECK, item.getDeck());
//            values.put(KEY_INDEX, item.getIndex());
//            values.put(KEY_COUNT_CORRECT, item.getCountCorrect());
//            values.put(KEY_COUNT_INCORRECT, item.getCountInCorrect());
//            values.put(KEY_COUNT, item.getCount());
//
//            Log.d("id value : ", Integer.toString(item.getId()));
//
//            db.insert(tableName, null, values);
//            db.close();
//        }
//    }
//
//
//    // Getting single Item
//    public Item getItem(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_ITEMS, new String[]{KEY_ID,
//                KEY_NAME, KEY_MEANING, KEY_ADD_DATE, KEY_lAST_DATE,
//                KEY_lAST_DAY, KEY_DECK, KEY_INDEX, KEY_COUNT_CORRECT, KEY_COUNT_INCORRECT, KEY_COUNT}, KEY_ID + "=?",
//                new String[]{String.valueOf(id)}, null, null, null, null);
//
//        cursor.moveToFirst();
//
//        Item item = null;
//        if (cursor.getCount() > 0 && cursor != null) {
//            item = new Item(
//                    Integer.parseInt(cursor.getString(0)),//id
//                    cursor.getString(1), //name
//                    cursor.getString(2), //meaning
//                    cursor.getString(3), //add date
//                    cursor.getString(4), //last check date
//
//                    Integer.parseInt(cursor.getString(5)),//last check day
//                    Integer.parseInt(cursor.getString(6)),//deck
//                    Integer.parseInt(cursor.getString(7)),//index
//                    Integer.parseInt(cursor.getString(8)),//count correct
//                    Integer.parseInt(cursor.getString(9)),//count incorrect
//                    Integer.parseInt(cursor.getString(10)));//count
//        }
//        cursor.close();
//        db.close();
//        // return item
//        return item;
//    }
//
//
//
//    // Getting single Item's word
//    public int getItemId(String word, String meaning) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_ITEMS, new String[] { KEY_ID,
//                KEY_NAME, KEY_MEANING, KEY_ADD_DATE, KEY_lAST_DATE,
//                KEY_lAST_DAY, KEY_DECK, KEY_INDEX, KEY_COUNT_CORRECT, KEY_COUNT_INCORRECT, KEY_COUNT}, KEY_NAME +"=? AND " + KEY_MEANING +"=?",
//                new String[] {word, meaning}, null, null, null, null);
//        if( cursor != null && cursor.moveToFirst()  ){
//            Log.i("getItem's Id", "word: " + word + "  meaning: " + meaning + " = " + Integer.toString(cursor.getInt(0)));
//            return cursor.getInt(0);
//        }
//        return 0;
//    }
//
//
//    // Getting All items
//    public List<Item> getAllItems() {
//        List<Item> itemsList = new ArrayList<Item>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_ITEMS;
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                Item item = new Item(
//                        Integer.parseInt(cursor.getString(0)),//id
//                        cursor.getString(1), //name
//                        cursor.getString(2), //meaning
//                        cursor.getString(3), //add date
//                        cursor.getString(4), //last check date
//
//                        Integer.parseInt(cursor.getString(5)),//LAST CHECK DAY
//                        Integer.parseInt(cursor.getString(6)),//deck
//                        Integer.parseInt(cursor.getString(7)),//index
//                        Integer.parseInt(cursor.getString(8)),//count correct
//                        Integer.parseInt(cursor.getString(9)),//count incorrect
//                        Integer.parseInt(cursor.getString(10)));//count);
//
//                itemsList.add(item);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        // return contact list
//        return itemsList;
//    }
//
//
//    // Getting contacts Count
//    public int getItemsCount() {
//        String countQuery = "SELECT  * FROM " + TABLE_ITEMS;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(countQuery, null);
//        int count = cursor.getCount();
//        cursor.close();
//
//        Log.d("myTag2", Integer.toString(count));
//        // return count
//        return count;
//    }
//
//    // Updating single contact
//    public int updateItem(Item item) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_NAME, item.getName());
//        values.put(KEY_MEANING, item.getMeaning());
//        values.put(KEY_ADD_DATE, item.getAddDate());
//        values.put(KEY_lAST_DATE, item.getLastCheckDate());
//        values.put(KEY_lAST_DAY, item.getLastCheckDay());
//        values.put(KEY_DECK, item.getDeck());
//        values.put(KEY_INDEX, item.getIndex());
//        values.put(KEY_COUNT_CORRECT, item.getCountCorrect());
//        values.put(KEY_COUNT_INCORRECT, item.getCountInCorrect());
//        values.put(KEY_COUNT, item.getCount());
//
//        Log.i("in update method", "id: " + item.getId());
//        return db.update(TABLE_ITEMS, values, KEY_ID + "=" + item.getId(), null);
//    }
//
//
//
//    // Deleting single contact
//    public void deleteItem(int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
////        db.delete(TABLE_WORDS, KEY_ID + " = ?",
////                new String[] { String.valueOf(id) });
//
//        try {db.delete(TABLE_ITEMS, KEY_ID + "=" + Integer.toString(id), null);}
//        catch (Exception e)
//        {
//            Log.e("DB ERROR", e.toString());
//            e.printStackTrace();
//        }
//        db.close();
//    }
//
//
//    public void clearTable() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_ITEMS, null, null);
//    }
//
//
//
//}