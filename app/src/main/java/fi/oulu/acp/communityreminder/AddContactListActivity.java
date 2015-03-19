package fi.oulu.acp.communityreminder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class AddContactListActivity extends Activity {

    private Context context;

    private static final String[] PHOTO_BITMAP_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Photo.PHOTO
    };

    private ListView lv;
    private ContactListAdapter contactListAdapter;
    private ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_gotophonecontacts);
        contacts = new ArrayList<>();
        lv = (ListView) findViewById(R.id.phonecontacts);
        contactListAdapter = new ContactListAdapter(this, R.layout.contact_row, contacts);
        lv.setAdapter(contactListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            public void run() {
                contacts = getAllContacts();
                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        contactListAdapter = new ContactListAdapter(context, R.layout.contact_row, contacts);
                        lv.setAdapter(contactListAdapter);
                    }
                };
                mainHandler.post(myRunnable);

//
            }
        }).start();


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    public ArrayList<Contact> getAllContacts() {
        ArrayList<Contact> result = new ArrayList<>();
        ContentResolver cr = this.getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if(cursor.moveToFirst())
        {
            do
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Integer bitmapId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                Bitmap photo = fetchThumbnail(bitmapId);
                ArrayList<String> phones = new ArrayList<>();

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                    while (pCur.moveToNext())
                    {
                        int phoneType = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        if(phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE||phoneType== ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE)
                        {
                            String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phones.add(contactNumber);
                        }

                    }
                    pCur.close();
                }

                if(phones.size()>0)
                {
                    Contact currentContact = new Contact(id, phones, name, photo);
                    result.add(currentContact);
                }

            } while (cursor.moveToNext()) ;
        }
        cursor.close();
        return result;
    }

    final Bitmap fetchThumbnail(final int thumbnailId) {

        final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId);
        final Cursor cursor = this.getContentResolver().query(uri, PHOTO_BITMAP_PROJECTION, null, null, null);

        try {
            Bitmap thumbnail = null;
            if (cursor.moveToFirst()) {
                final byte[] thumbnailBytes = cursor.getBlob(0);
                if (thumbnailBytes != null) {
                    thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
                }
            }
            return thumbnail;
        }
        finally {
            cursor.close();
        }

    }

}
