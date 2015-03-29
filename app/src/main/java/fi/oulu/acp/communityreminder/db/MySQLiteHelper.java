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

    public static final String COLUMN_TEMP_ID = "_id";
    public static final String COLUMN_TEMP_SESSION = "session";
    public static final String COLUMN_TEMP_PERFORMANCE = "performance";
    public static final String COLUMN_TEMP_ENV_TEMP = "env_temperature";
    public static final String COLUMN_TEMP_TEMP = "temperature";

    public static final String TABLE_CONTACTS_DATA = "contacts";

    public static final String COLUMN_CONTACTS_ID = "_id";
    public static final String COLUMN_CONTACTS_BITMAP = "bitmap";
    public static final String COLUMN_CONTACTS_NAME = "name";
    public static final String COLUMN_CONTACTS_PHONE = "phone";
    public static final String COLUMN_CONTACTS_FRIEND = "friend";

    public static final String TABLE_FRIENDS_DATA = "friends";

    public static final String COLUMN_FRIEND_ID = "_id";
    public static final String COLUMN_FRIEND_BITMAP = "bitmap";
    public static final String COLUMN_FRIEND_NAME = "name";
    public static final String COLUMN_FRIEND_PHONE = "phone";
    public static final String COLUMN_FRIEND_BIRTHDAY = "birthday";
    public static final String COLUMN_FRIEND_STEPGOAL = "step_goal";

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 9;

    // Database creation sql statement
    private static final String DATABASE_CREATE_TEMP = "create table "
            + TABLE_TEMP_DATA + "(" + COLUMN_TEMP_ID
            + " integer primary key autoincrement, " + COLUMN_TEMP_SESSION
            + " integer not null, " + COLUMN_TEMP_PERFORMANCE
            + " text not null, " + COLUMN_TEMP_ENV_TEMP
            + " text not null, " + COLUMN_TEMP_TEMP
            + " real not null);";

    public static final String DATABASE_CREATE_CONTACTS = "create table "
            + TABLE_CONTACTS_DATA + "(" + COLUMN_CONTACTS_ID
            + " integer primary key autoincrement, " + COLUMN_CONTACTS_BITMAP
            + " BLOB, " + COLUMN_CONTACTS_NAME
            + " text not null, " + COLUMN_CONTACTS_PHONE
            + " text not null, " + COLUMN_CONTACTS_FRIEND
            + " int not null);";

    public static final String DATABASE_CREATE_FRIENDS = "create table "
            + TABLE_FRIENDS_DATA + "(" + COLUMN_FRIEND_ID
            + " integer primary key autoincrement, " + COLUMN_FRIEND_BITMAP
            + " BLOB, " + COLUMN_FRIEND_NAME
            + " text not null, " + COLUMN_FRIEND_PHONE
            + " text not null, " + COLUMN_FRIEND_BIRTHDAY
            + " text, " + COLUMN_FRIEND_STEPGOAL
            + " integer);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_TEMP);
        database.execSQL(DATABASE_CREATE_CONTACTS);
        database.execSQL(DATABASE_CREATE_FRIENDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMP_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS_DATA);
        onCreate(db);
    }


}
