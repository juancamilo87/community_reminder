package fi.oulu.acp.communityreminder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fi.oulu.acp.communityreminder.db.ContactsDataSource;
import fi.oulu.acp.communityreminder.db.FriendsDataSource;
import fi.oulu.acp.communityreminder.tasks.VerifyContactsTask;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class ContactListActivity extends Activity {

    private static final String[] PHOTO_BITMAP_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Photo.PHOTO
    };

    private FriendsDataSource fds;
    private ContactsDataSource cds;
    private ListView lv;
    private ArrayList<Contact> friends;
    private FriendListAdapter friendListAdapter;
    private Context context;
    private ImageButton addContactBtn;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_screen);
        context = this;
        addContactBtn = (ImageButton) findViewById(R.id.addcontacts);
        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,AddContactListActivity.class);
                startActivity(intent);
            }
        });
        linearLayout = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        lv = (ListView) findViewById(R.id.contactsname);
        cds = new ContactsDataSource(context);
        fds = new FriendsDataSource(context);
        fds.open();
        friends = fds.getAllFriends();
        fds.close();
        if(friends.size()==0)
        {
            linearLayout.setVisibility(View.VISIBLE);
        }

        friendListAdapter = new FriendListAdapter(this, R.layout.contact_row_add, addHeaders(friends));
        lv.setAdapter(friendListAdapter);
        lv.setEmptyView(findViewById(R.id.no_contacts_txt));

        new Thread(new Runnable() {
            @Override
            public void run() {

                ArrayList<Contact> thisContacts = getAllContacts();
                new VerifyContactsTask().execute(context, thisContacts);

                cds.open();

                new GetFriendsTask().execute(context);

                cds.close();
            }
        }).start();
    }

    private class GetFriendsTask extends AsyncTask<Object, Void, HttpResponse>
    {
        private Context context;

        private ArrayList<String> friendsNumbers;
        private ArrayList<String> pendingNumbers;
        private ArrayList<String> requestNumbers;

        @Override
        protected HttpResponse doInBackground(Object... objects) {
            context = (Context) objects[0];
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String phoneNumber = prefs.getString("phoneNumber","none");
            //http get
            try{
                HttpClient httpclient = new DefaultHttpClient();
                String url = "http://pan0166.panoulu.net/community/backend/getFriends.php";
                url += "?user_id="+phoneNumber;
                HttpGet httpget = new HttpGet(url);
                HttpResponse response = httpclient.execute(httpget);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(HttpResponse feed) {
            Header[] headers = feed.getAllHeaders();
            if(headers.length>3)
            {
                String responseCode = headers[3].getValue();
                if(responseCode.equals("409")){
                    Log.e("Friends", "Unknown error");
                }
                else if(responseCode.equals("201"))
                {
                    HttpEntity entity = feed.getEntity();
                    try {
                        if (entity != null) {
                            InputStream ips  = entity.getContent();
                            BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));
                            StringBuilder sb = new StringBuilder();
                            String s;
                            while(true )
                            {
                                s = buf.readLine();
                                if(s==null || s.length()==0)
                                    break;
                                sb.append(s);

                            }
                            buf.close();
                            ips.close();
                            String answer = sb.toString();
                            Log.d("Tag",answer);
                            friendsNumbers = new ArrayList<String>();
                            pendingNumbers = new ArrayList<String>();
                            requestNumbers = new ArrayList<String>();
                            JSONObject parentObject = new JSONObject(answer);
                            JSONArray jsonArray = parentObject.getJSONArray("result");
                            JSONObject friends = jsonArray.getJSONObject(0);
                            JSONObject pendingFriends = jsonArray.getJSONObject(1);
                            JSONObject requestFriends = jsonArray.getJSONObject(2);

                            JSONArray friendsArray = friends.getJSONArray("friends");
                            JSONArray pendingFriendsArray = pendingFriends.getJSONArray("pending_friends");
                            JSONArray requestFriendsArray = requestFriends.getJSONArray("request_friends");

                            if (friendsArray != null) {
                                int len = friendsArray.length();
                                for (int i=0;i<len;i++){
                                    friendsNumbers.add(friendsArray.get(i).toString());
                                }
                            }
                            if (pendingFriendsArray!= null) {
                                int len = pendingFriendsArray.length();
                                for (int i=0;i<len;i++){
                                    pendingNumbers.add(pendingFriendsArray.get(i).toString());
                                }
                            }
                            if (requestFriendsArray != null) {
                                int len = requestFriendsArray.length();
                                for (int i=0;i<len;i++){
                                    requestNumbers.add(requestFriendsArray.get(i).toString());
                                }
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    ArrayList<Contact> newFriends = new ArrayList<>();
                    ContactsDataSource ds = new ContactsDataSource(context);
                    ds.open();
                    ArrayList<Contact> possibleFriends = ds.getAllContactsAvailable();

                    for(int i = 0; i< friendsNumbers.size(); i++)
                    {
                        for(int j = 0; j< possibleFriends.size(); j++)
                        {
                            if(possibleFriends.get(j).getPhones().get(0).equals(friendsNumbers.get(i)))
                            {
                                Contact newContact = possibleFriends.get(j);
                                newFriends.add(newContact);
                                ds.makeFriend(friendsNumbers.get(i));
                            }
                        }
                    }
                    for(int i = 0; i< pendingNumbers.size(); i++)
                    {
                        for(int j = 0; j< possibleFriends.size(); j++)
                        {
                            if(possibleFriends.get(j).getPhones().get(0).equals(pendingNumbers.get(i)))
                            {
                                Contact newContact = possibleFriends.get(j);
                                newContact.setPending();
                                newFriends.add(newContact);
                                ds.makeFriend(pendingNumbers.get(i));
                            }
                        }
                    }
                    for(int i = 0; i< requestNumbers.size(); i++)
                    {
                        for(int j = 0; j< possibleFriends.size(); j++)
                        {
                            if(possibleFriends.get(j).getPhones().get(0).equals(requestNumbers.get(i)))
                            {
                                Contact newContact = possibleFriends.get(j);
                                newContact.setRequested();
                                newFriends.add(newContact);
                                ds.makeFriend(requestNumbers.get(i));
                            }
                        }
                    }
                    FriendsDataSource fds = new FriendsDataSource(context);
                    fds.open();
                    fds.recreateTable();
                    Collections.sort(newFriends,new CustomComparator());
                    for(int i = 0; i<newFriends.size();i++){
                        Contact friend = newFriends.get(i);
                        Bitmap photo = friend.getPicture();
                        byte[] bArray = null;
                        if(photo!=null)
                        {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
                            bArray = bos.toByteArray();
                        }


                        fds.addFriendsData(bArray,friend.getName(),friend.getPhones().get(0),null,0,friend.getStatus());
                    }
                    fds.close();
                    ds.close();

                    linearLayout.setVisibility(View.GONE);
                    friendListAdapter = new FriendListAdapter(context,R.layout.contact_row_add,addHeaders(newFriends));
                    lv.setAdapter(friendListAdapter);
                    Log.d("Friends","Friends loaded");
                }
                else
                {
                    Log.e("Friends","Unknown error");
                }
            }
            else
            {
                Log.e("Friends","Unknown error");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fds = new FriendsDataSource(context);
        fds.open();
        friends = fds.getAllFriends();
        fds.close();
        friendListAdapter = new FriendListAdapter(this, R.layout.contact_row_add, addHeaders(friends));
        lv.setAdapter(friendListAdapter);
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
                            phones.add(contactNumber.replaceAll("[^0-9+]", ""));
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
        Collections.sort(result, new CustomComparator());
        return result;
    }

    public class CustomComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact o1, Contact o2) {
            int s1 = o1.getStatus();
            int s2 = o2.getStatus();
            if(s1==s2){
                return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
            }
            else if(s1==0)
            {
                return 1;
            }
            else if(s2==0)
            {
                return -1;
            }
            else if(s1==2)
            {
                return -1;
            }
            else
            {
                return 1;
            }


        }
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

    public ArrayList<Contact> addHeaders(ArrayList<Contact> items){
        ArrayList<Contact> realItems = new ArrayList<>();
        boolean request = false;
        boolean pending = false;
        boolean friends = false;
        for(int i = 0; i<items.size();i++)
        {
            Contact thisContact = items.get(i);
            switch (thisContact.getStatus()){
                case 0:
                    if(!friends)
                    {
                        Contact newContact = new Contact("0",new ArrayList<String>(),"Friends");
                        newContact.setStatus(3);
                        realItems.add(newContact);
                        friends = true;
                    }
                    break;
                case 1:
                    if(!pending)
                    {
                        Contact newContact = new Contact("0",new ArrayList<String>(),"Awaiting confirmation");
                        newContact.setStatus(3);
                        realItems.add(newContact);
                        pending = true;
                    }
                    break;
                case 2:
                    if(!request)
                    {
                        Contact newContact = new Contact("0",new ArrayList<String>(),"Requests");
                        newContact.setStatus(3);
                        realItems.add(newContact);
                        request = true;
                    }
                    break;
            }
            realItems.add(thisContact);

        }
        return realItems;
    }

}
