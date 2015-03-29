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
public class FriendsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_FRIEND_ID,
            MySQLiteHelper.COLUMN_FRIEND_BITMAP, MySQLiteHelper.COLUMN_FRIEND_NAME,
            MySQLiteHelper.COLUMN_FRIEND_PHONE, MySQLiteHelper.COLUMN_FRIEND_BIRTHDAY,
            MySQLiteHelper.COLUMN_FRIEND_STEPGOAL};

    public FriendsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public int addFriendsData(byte[] image, String name, String phone, String birthday, int stepGoal) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_FRIEND_BITMAP, image);
        values.put(MySQLiteHelper.COLUMN_FRIEND_NAME, name);
        values.put(MySQLiteHelper.COLUMN_FRIEND_PHONE, phone);
        values.put(MySQLiteHelper.COLUMN_FRIEND_BIRTHDAY, birthday);
        values.put(MySQLiteHelper.COLUMN_FRIEND_STEPGOAL, stepGoal);
        long insertId = database.insert(MySQLiteHelper.TABLE_FRIENDS_DATA, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_FRIENDS_DATA,
                allColumns, MySQLiteHelper.COLUMN_FRIEND_ID+ " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(0);

    }

    public void recreateTable(){
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteHelper.TABLE_FRIENDS_DATA);
        database.execSQL(MySQLiteHelper.DATABASE_CREATE_FRIENDS);
    }

    public ArrayList<Contact> getAllFriends(){
        ArrayList<Contact> result = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_FRIENDS_DATA, allColumns, null, null, null, null, null);
        int i = 1;
        while (cursor.moveToNext()) {
            byte[] pictureBytes = cursor.getBlob(1);
            Bitmap picture = null;
            if(pictureBytes!=null)
            {
                picture = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length);
            }

            String name = cursor.getString(2);
            String phone = cursor.getString(3);
            ArrayList<String> phones = new ArrayList<>();
            phones.add(phone);
            Contact newContact = new Contact(i+"",phones,name, picture);
            newContact.setBirthday(cursor.getString(4));
            newContact.setStepGoals(cursor.getInt(5));
            result.add(newContact);
        }

        Collections.sort(result,new CustomComparator());
        return result;
    }

    public class CustomComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact o1, Contact o2) {
            return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
        }
    }

}
