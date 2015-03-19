package fi.oulu.acp.communityreminder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class ChangeContactService extends Service {
    //EDIT
    private static final String ACTION="android.intent.action.EDIT";
    private static final String CATEGORY="android.intent.category.DEFAULT";
    private static final String MIMETYPE="vnd.android.cursor.item/person";
    private static final String HOST="contacts";
    private static final String MIMETYPE2="vnd.android.cursor.item/contact";
    private static final String HOST2="com.android.contacts";
    private static final String MIMETYPE3="vnd.android.cursor.item/raw_contact";
    private static final String HOST3="com.android.contacts";

    //ADD
    private static final String ACTION2="android.intent.action.INSERT";
    private static final String MIMETYPE4="vnd.android.cursor.dir/person";
    private static final String MIMETYPE5="vnd.android.cursor.dir/contact";
    private static final String MIMETYPE6="vnd.android.cursor.dir/raw_contact";


    private BroadcastReceiver contactReceiver;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final IntentFilter editFilter = new IntentFilter();
        final IntentFilter addFilter = new IntentFilter();
        editFilter.addAction(ACTION);
        addFilter.addAction(ACTION2);
        editFilter.addCategory(CATEGORY);
        addFilter.addCategory(CATEGORY);
        editFilter.addDataAuthority(HOST, null);
        editFilter.addDataAuthority(HOST2, null);
        editFilter.addDataAuthority(HOST3, null);
        try {
            editFilter.addDataType(MIMETYPE);
            editFilter.addDataType(MIMETYPE2);
            editFilter.addDataType(MIMETYPE3);
            addFilter.addDataType(MIMETYPE4);
            addFilter.addDataType(MIMETYPE5);
            addFilter.addDataType(MIMETYPE6);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        this.contactReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Do whatever you need it to do when it receives the broadcast
                // Example show a Toast message...
                checkContacts(context);
            }
        };
        // Registers the receiver so that your service will listen for
        // broadcasts
        this.registerReceiver(this.contactReceiver, editFilter);
        this.registerReceiver(this.contactReceiver, addFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Do not forget to unregister the receiver!!!
        this.unregisterReceiver(this.contactReceiver);
    }

    public void checkContacts(Context context){
        ArrayList<Contact> contacts = getAllContacts();
        //Todo compare with DB online
    }


    public ArrayList<Contact> getAllContacts()
    {
        ArrayList<Contact> contacts = new ArrayList<>();
        ContentResolver cr = this.getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst())
        {
            ArrayList<String> alContacts = new ArrayList<String>();
            do
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                //String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
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
                    Contact currentContact = new Contact(id, phones);
                    contacts.add(currentContact);
                }

            } while (cursor.moveToNext()) ;
        }
        return contacts;
    }


}
