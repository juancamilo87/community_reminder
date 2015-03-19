package fi.oulu.acp.communityreminder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {


    public static final String TABLE_TEMP_DATA = "temperatures";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SESSION = "session";
    public static final String COLUMN_PERFORMANCE = "performance";
    public static final String COLUMN_ENV_TEMP = "env_temperature";
    public static final String COLUMN_TEMP = "temperature";

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_TEMP_DATA + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_SESSION
            + " integer not null, " + COLUMN_PERFORMANCE
            + " text not null, " + COLUMN_ENV_TEMP
            + " text not null, " + COLUMN_TEMP
            + " real not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMP_DATA);
        onCreate(db);
    }


}
