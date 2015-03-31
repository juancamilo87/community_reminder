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
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fi.oulu.acp.communityreminder.db.ContactsDataSource;
import fi.oulu.acp.communityreminder.db.FriendsDataSource;
import fi.oulu.acp.communityreminder.services.TemperatureService;
import fi.oulu.acp.communityreminder.tasks.VerifyContactsTask;

/**
 * Created by JuanCamilo on 3/29/2015.
 */
public class HomeScreenActivity extends Activity {

    private static final String[] PHOTO_BITMAP_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Photo.PHOTO
    };


    private Context context;
    private ImageButton btnEmergency;
    private int emergencyTaps;
    private Toast toast;
    private ImageButton btnProblem;
    private int problemTaps;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        context = this;
        toast = Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT);
        //Contacts
        ImageButton contactsBtn = (ImageButton) findViewById(R.id.ContactsButton);
        contactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ContactListActivity.class);
                startActivity(intent);
            }
        });

        //Emergency buttons
        //First
        initializeEmergencyButton();
        initializeProblemButton();
        initializeContacts();
        initializeTempAlerts();

        //Second

    }

    private void initializeTempAlerts() {
        Intent intent = new Intent(this, TemperatureService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initializeContacts() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                ArrayList<Contact> thisContacts = getAllContacts();
                new VerifyContactsTask().execute(context, thisContacts);
                new GetFriendsTask().execute(context);
            }
        }).start();

    }

    private void initializeProblemButton() {
        problemTaps = 0;
        final Handler handler = new Handler();

        final Runnable restartTapCount = new Runnable() {
            @Override
            public void run() {
                problemTaps = 0;
                Log.d("DEBUG", "problemTap resetted");
            }
        };

        final int millisecondsPerTap = (getResources().getInteger(R.integer.seconds_for_emergency)*1000)/getResources().getInteger(R.integer.taps_for_emergency);
        Log.d("DEBUG", "time between: "+millisecondsPerTap);
        final int tapsForEmergency = getResources().getInteger(R.integer.taps_for_emergency);


        btnProblem = (ImageButton) findViewById(R.id.LifeProblemButton);

        btnProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(restartTapCount);
                problemTaps++;
                if (problemTaps == tapsForEmergency) {
                    toast.cancel();
                    Toast.makeText(getApplicationContext(), "Life problem alert sent!", Toast.LENGTH_SHORT).show();
                    problemTaps = 0;
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    vibrator.vibrate(600);

                } else {
                    toast.setText("Tap " + (tapsForEmergency - problemTaps) + " more times to send life problem alert.");
                    toast.show();
                    handler.postDelayed(restartTapCount, millisecondsPerTap);
                }
            }
        });

        btnProblem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast.setText("Tap " + tapsForEmergency + " times to send a life problem alert!");
                toast.show();
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                vibrator.vibrate(300);
                return true;
            }
        });
    }

    private void initializeEmergencyButton() {
        emergencyTaps = 0;
        final Handler handler = new Handler();

        final Runnable restartTapCount = new Runnable() {
            @Override
            public void run() {
                emergencyTaps = 0;
                Log.d("DEBUG", "emergencyTap resetted");
            }
        };

        final int millisecondsPerTap = (getResources().getInteger(R.integer.seconds_for_emergency)*1000)/getResources().getInteger(R.integer.taps_for_emergency);
        Log.d("DEBUG", "time between: "+millisecondsPerTap);
        final int tapsForEmergency = getResources().getInteger(R.integer.taps_for_emergency);


        btnEmergency = (ImageButton) findViewById(R.id.HealthEmergencyButton);

        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(restartTapCount);
                emergencyTaps++;
                if(emergencyTaps==tapsForEmergency)
                {
                    toast.cancel();
                    Toast.makeText(getApplicationContext(),"Emergency alert sent!",Toast.LENGTH_SHORT).show();
                    emergencyTaps = 0;
                    Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

                    vibrator.vibrate(600);

                }
                else
                {
                    toast.setText("Tap " + (tapsForEmergency-emergencyTaps) + " more times to send an alert.");
                    toast.show();
                    handler.postDelayed(restartTapCount, millisecondsPerTap);
                }
            }
        });

        btnEmergency.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast.setText("Tap " + tapsForEmergency + " times to send an emergency alert!");
                toast.show();
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                vibrator.vibrate(300);
                return true;
            }
        });
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

}
