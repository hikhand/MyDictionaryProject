package ir.khaled.mydictionary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "database_items";
    private static final String TABLE_NAME    = "words";
    private static final String KEY_ID     = "_id";
    private static final String KEY_WORD   = "word";
    private static final String KEY_MEANING  = "meaning";
    private static final String KEY_DATE  = "date";
    private static final String KEY_COUNT  = "count";

    private static final String CREATE_TABLE  = "CREATE TABLE " + TABLE_NAME + " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_WORD + " TEXT," +
            KEY_MEANING + " TEXT" +
            KEY_DATE + " TEXT" +
            KEY_COUNT + " INTEGER" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

        Log.i(TAG, "DATABASE created.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("Drop table if exists " + TABLE_NAME);
        onCreate(db);
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public String getRowIdName() {
        return KEY_ID;
    }
}