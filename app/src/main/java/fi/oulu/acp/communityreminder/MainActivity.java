package fi.oulu.acp.communityreminder;

import android.app.*;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends ActionBarActivity {

    static final String TAG = "MainActivity";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static String PROPERTY_APP_VERSION = "appVersion";
    private final static String PROPERTY_REG_ID = "registration_id";

    String regid;
    SharedPreferences prefs;
    Context context;
    AtomicInteger msgId = new AtomicInteger();

    GoogleCloudMessaging gcm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
//
//        if (checkPlayServices()) {
//            gcm = GoogleCloudMessaging.getInstance(this);
//            regid = getRegistrationId(context);
//
//            if (regid.isEmpty()) {
//                registerInBackground();
//            }
//        } else {
//            Log.i(TAG, "No valid Google Play Services APK found.");
//        }

    }

    private String getRegistrationId(Context context){
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()){
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion){
            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context){
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context){
        try{
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e){
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground(){
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... params){
                String msg = "";
                try{
                    if (gcm == null)
                        gcm = GoogleCloudMessaging.getInstance(context);

                    regid = gcm.register(Config.GOOGLE_SENDER_ID);
                    msg = "Device registered, registration ID =" + regid;

                    ServerUtilities.register(context, regid);
                    storeRegistrationId(context, regid);

                } catch (IOException e){
                    msg = "Error :" + e.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg){

            }
        }.execute(null, null, null);
    }

    public void sendMessage(){
        new AsyncTask<Object, Void, HttpResponse>(){
            @Override
            protected HttpResponse doInBackground(Object... params){
                String msg = "";
                try {
                    /*Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action",
                            "fi.oulu.acp.communityreminder.ECHO_NOW");
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(Config.GOOGLE_SENDER_ID + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";*/
                    String url = "http://pan0166.panoulu.net/community/backend/broadcastAlert.php";
                    //Map<String, String> par = new HashMap<>();
                    //par.put("regId", regid);
                    //par.put("message", "Hey!");
                    //ServerUtilities.post(url, par);
                    url += "?user_id=" + "09876543210" + "&message=" + "BLA" + "&key=" + "";
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);

                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    return httpResponse;
                } catch (IOException ex) {
                    Log.e("++++++++", "BLA");
                    return null;
                }
                //return msg;
            }
            @Override
            protected void onPostExecute(HttpResponse msg){
                Log.e("++++++++", msg.toString());
                try{
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(msg.getEntity().getContent(),"UTF-8"));
                    String fhg = bufferedReader.readLine();
                    Log.e("--------", fhg);
                } catch (Exception e){

                }

            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId){
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationToBackend(){

    }

    @Override
    protected void onResume(){
        super.onResume();
        //checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToTemperature(View view)
    {




//        RemoteViews notiViewNot = new RemoteViews(context.getPackageName(),R.layout.temperature_notification);
//        notiViewNot.setImageViewResource(R.id.temp_noti_not_image, R.drawable.app_icon_notification);
//        Calendar calendar = new GregorianCalendar();
//        String time;
//        if(calendar.get(Calendar.HOUR_OF_DAY)<10)
//        {
//            time = "0"+calendar.get(Calendar.HOUR_OF_DAY)+":";
//        }
//        else
//        {
//            time = calendar.get(Calendar.HOUR_OF_DAY)+":";
//        }
//        if(calendar.get(Calendar.MINUTE)<10)
//        {
//            time = time+"0"+calendar.get(Calendar.MINUTE);
//        }
//        else
//        {
//            time = time+calendar.get(Calendar.MINUTE);
//        }
//        notiViewNot.setTextViewText(R.id.temp_noti_not_time, time);
//        notiViewNot.setTextViewText(R.id.temp_noti_not_title, "This is the Title");
//        notiViewNot.setTextViewText(R.id.temp_noti_not_message, "This is the message");
//        RemoteViews notiView = new RemoteViews(context.getPackageName(),R.layout.temperature_notification_expanded);
//        notiView.setImageViewResource(R.id.temp_noti_image, R.drawable.app_icon_notification);
//        notiView.setImageViewResource(R.id.temp_noti_image_call, R.mipmap.telephone);
//        notiView.setTextViewText(R.id.temp_noti_time, time);
//        notiView.setTextViewText(R.id.temp_noti_title, "This is the Title");
//        notiView.setTextViewText(R.id.temp_noti_message, "This is the message");
//        notiView.setTextViewText(R.id.temp_zero_right, time_zero + " minutes");
//        notiView.setTextViewText(R.id.temp_five_right, time_five + " minutes");
//        notiView.setTextViewText(R.id.temp_ten_right, time_ten + " minutes");
//        notiView.setTextViewText(R.id.temp_fifteen_right, time_fifteen + " minutes");
//        notiView.setTextViewText(R.id.temp_twenty_right, time_twenty + " minutes");
//        notiView.setTextViewText(R.id.temp_other_right, time_other + " minutes");
//        notiView.setOnClickPendingIntent(R.id.temp_noti_linear,piCall);
//        notiView.setTextViewText(R.id.temp_noti_name,"Camilo");
//        android.app.Notification.Builder noti = new android.app.Notification.Builder(context)
//                .setContent(notiViewNot)
//                .setSmallIcon(R.drawable.app_icon_notification)
//                .setVibrate(new long[]{100, 500, 300, 500})
//
//                .setAutoCancel(true)
//                //.setDeleteIntent(pendingDismiss)
//                ;
//        noti.setContentIntent(pintentNotification);
//        Notification notif = noti.build();
//        notif.bigContentView = notiView;
//        nm.notify(notificationId, notif);
    }

    public void goToSettingsActivity(View view){
        Intent intent = new Intent (this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goToPedometerActivity(View view){
        Intent intent = new Intent(this, PedometerActivity.class);
        startActivity(intent);
        //sendMessage();
    }

    public void goToSignIn(View view){
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    public void goToNotifications(View v){
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }
}
