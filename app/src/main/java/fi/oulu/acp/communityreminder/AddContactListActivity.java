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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fi.oulu.acp.communityreminder.db.ContactsDataSource;
import fi.oulu.acp.communityreminder.db.FriendsDataSource;
import fi.oulu.acp.communityreminder.tasks.VerifyContactsTask;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class AddContactListActivity extends Activity {




    private Context context;


    private ContactsDataSource ds;
    private ListView lv;
    private ContactListAdapter contactListAdapter;
    private ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlurryAgent.logEvent("AddContactListActivity");
        context = this;
        setContentView(R.layout.activity_gotophonecontacts);
        lv = (ListView) findViewById(R.id.phonecontacts);

        ds = new ContactsDataSource(context);
        ds.open();
        contacts = ds.getAllContactsAvailableToAdd();
        ds.close();
        contactListAdapter = new ContactListAdapter(this, R.layout.contact_row_add, contacts);
        lv.setAdapter(contactListAdapter);
        lv.setEmptyView(findViewById(R.id.no_contacts_txt));


    }

    @Override
    protected void onResume() {
        super.onResume();
        ds = new ContactsDataSource(context);
        ds.open();
        contacts = ds.getAllContactsAvailableToAdd();
        ds.close();
        contactListAdapter = new ContactListAdapter(this, R.layout.contact_row_add, contacts);
        lv.setAdapter(contactListAdapter);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }



}
