package fi.oulu.acp.communityreminder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fi.oulu.acp.communityreminder.Contact;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class ContactsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_CONTACTS_ID,
            MySQLiteHelper.COLUMN_CONTACTS_BITMAP, MySQLiteHelper.COLUMN_CONTACTS_NAME,
            MySQLiteHelper.COLUMN_CONTACTS_PHONE};

    public ContactsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public int addContactsData(byte[] image, String name, String phone, int friend) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CONTACTS_BITMAP, image);
        values.put(MySQLiteHelper.COLUMN_CONTACTS_NAME, name);
        values.put(MySQLiteHelper.COLUMN_CONTACTS_PHONE, phone);
        values.put(MySQLiteHelper.COLUMN_CONTACTS_FRIEND, friend);
        long insertId = database.insert(MySQLiteHelper.TABLE_CONTACTS_DATA, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS_DATA,
                allColumns, MySQLiteHelper.COLUMN_CONTACTS_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(0);

    }

    public void recreateTable(){
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteHelper.TABLE_CONTACTS_DATA);
        database.execSQL(MySQLiteHelper.DATABASE_CREATE_CONTACTS);
    }

    public ArrayList<Contact> getAllContactsAvailable(){
        ArrayList<Contact> result = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS_DATA, allColumns, null, null, null, null, null);
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
            result.add(newContact);
        }
        Collections.sort(result, new CustomComparator());
        return result;
    }

    public ArrayList<Contact> getAllContactsAvailableToAdd(){
        ArrayList<Contact> result = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS_DATA, allColumns,
                MySQLiteHelper.COLUMN_CONTACTS_FRIEND + " = 0", null, null, null, null);
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
            result.add(newContact);
        }

        Collections.sort(result, new CustomComparator());

        return result;
    }

    public void makeFriend(String phone){
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN_CONTACTS_FRIEND,1);
        database.update(MySQLiteHelper.TABLE_CONTACTS_DATA,cv,MySQLiteHelper.COLUMN_CONTACTS_PHONE + " = '" + phone+ "'",null);
    }

    public Contact getContact(String phone){
        Contact contact = null;

        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS_DATA,null,MySQLiteHelper.COLUMN_CONTACTS_PHONE + " = '" + phone
                + "'",null,null,null,null);

        if(cursor.moveToFirst())
        {
            byte[] pictureBytes = cursor.getBlob(1);
            Bitmap picture = null;
            if(pictureBytes!=null)
            {
                picture = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length);
            }

            String name = cursor.getString(2);
            String thePhone = cursor.getString(3);
            ArrayList<String> phones = new ArrayList<>();
            phones.add(thePhone);
            contact = new Contact("1",phones, name, picture);
        }
        return contact;
    }

    public String getName(String phone)
    {

        Contact contact = getContact(phone);
        return contact.getName();
    }

    public class CustomComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact o1, Contact o2) {
            return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
        }
    }
}
