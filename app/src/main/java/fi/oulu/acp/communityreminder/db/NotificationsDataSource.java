package fi.oulu.acp.communityreminder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fi.oulu.acp.communityreminder.Contact;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class NotificationsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_NOTIFICATION_ID,
            MySQLiteHelper.COLUMN_NOTIFICATION_PHONE, MySQLiteHelper.COLUMN_NOTIFICATION_TITLE,
            MySQLiteHelper.COLUMN_NOTIFICATION_MESSAGE, MySQLiteHelper.COLUMN_NOTIFICATION_TIMESTAMP};

    public NotificationsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public int addNotificationsData(String phoneNumber, String title, String message) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_NOTIFICATION_PHONE, phoneNumber);
        values.put(MySQLiteHelper.COLUMN_NOTIFICATION_TITLE, title);
        values.put(MySQLiteHelper.COLUMN_NOTIFICATION_MESSAGE, message);
        values.put(MySQLiteHelper.COLUMN_NOTIFICATION_TIMESTAMP, System.currentTimeMillis());
        long insertId = database.insert(MySQLiteHelper.TABLE_NOTIFICATIONS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS,
                allColumns, MySQLiteHelper.COLUMN_FRIEND_ID+ " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(0);

    }

    public Cursor getAllNotificationsCursor(){
        return database.query(MySQLiteHelper.TABLE_NOTIFICATIONS, allColumns, null, null, null, null, MySQLiteHelper.COLUMN_NOTIFICATION_TIMESTAMP + " DESC");

    }

    public Cursor getNotificationsFromContact(String phoneNumber){
        return database.query(MySQLiteHelper.TABLE_NOTIFICATIONS, allColumns, MySQLiteHelper.COLUMN_CONTACTS_PHONE, new String[]{phoneNumber}, null, null, MySQLiteHelper.COLUMN_NOTIFICATION_TIMESTAMP + " DESC");
    }

}
