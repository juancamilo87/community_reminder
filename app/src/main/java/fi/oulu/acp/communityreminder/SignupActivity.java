package fi.oulu.acp.communityreminder;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import fi.oulu.acp.communityreminder.tasks.VerifyContactsTask;


public class SignupActivity extends Activity {

    private String uid;
    private BroadcastReceiver smsReceiver;
    private String finalPhoneNumber;
    private String name;
    private TextWatcher textWatcher;
    private String secretMessage;
    private boolean smsReceived;
    private Context sContext;
    private ProgressBar loginLoading;
    private TextView loginText;
    private EditText edTName;
    private EditText edTPhone;
    private RelativeLayout verifyPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_screen);
        sContext = this;
        verifyPhone = (RelativeLayout) findViewById(R.id.verify_button);
        loginLoading = (ProgressBar) findViewById(R.id.login_loading);
        loginText = (TextView) findViewById(R.id.login_text);
        edTName = (EditText) findViewById(R.id.editName);
        edTPhone = (EditText) findViewById(R.id.editPhoneNumber);
        smsReceived = false;
        verifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPhone();
            }
        });
        TelephonyManager manager =(TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = manager.getLine1Number();
        if(mPhoneNumber!=null){
            ((EditText) findViewById(R.id.editPhoneNumber)).setText(mPhoneNumber);
        }
        uid = manager.getDeviceId();


    }

    @Override
    protected void onDestroy() {
        try{
            unregisterReceiver(smsReceiver);
        }catch(Exception e)
        {
        }
        try{
            unregisterReceiver(smsSentReceiver);
        }catch(Exception e){}

        super.onDestroy();
    }

    private void verifyPhone()
    {

        disable();
        name = edTName.getText().toString();
        finalPhoneNumber = edTPhone.getText().toString();
        finalPhoneNumber = finalPhoneNumber.replaceAll(" ", "");




        try
        {
            Integer.parseInt(finalPhoneNumber);
            if(!finalPhoneNumber.equals("")&&!name.equals(""))
            {
                smsReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Object[] pdus=(Object[])intent.getExtras().get("pdus");
                        SmsMessage shortMessage=SmsMessage.createFromPdu((byte[]) pdus[0]);

                        String sender = shortMessage.getOriginatingAddress();
                        String message = shortMessage.getDisplayMessageBody();
                        Log.d("Tag", "Message received from sender: "+ sender + " with message: " + message);
                        String newNumber = finalPhoneNumber;
                        if(finalPhoneNumber.startsWith("0"))
                        {
                            newNumber = finalPhoneNumber.replaceFirst("0","");
                        }
                        if(sender.contains(newNumber))
                        {
                            smsReceived = true;
                            verifyPhoneNumber(sender, message);
                            try{
                                context.unregisterReceiver(smsReceiver);
                            }catch(Exception e){}
                        }

                    }};

                IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                registerReceiver(smsReceiver, intentFilter);
                Log.d("Tag", "Register SMS receiver");

                SmsManager sms = SmsManager.getDefault();
                try
                {
                    Log.d("Tag", "Sending sms...");
                    secretMessage = random();

                    IntentFilter smsIntentFilter = new IntentFilter();
                    smsIntentFilter.addAction("Message sent");
                    registerReceiver(smsSentReceiver,smsIntentFilter);

                    Intent intent = new Intent();
                    intent.setAction("Message sent");
                    PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    sms.sendTextMessage(finalPhoneNumber, null, secretMessage, pIntent, null);


                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if(!smsReceived)
                            {
                                enable();
                                Toast.makeText(sContext, "Error verifying phone number", Toast.LENGTH_SHORT);

                                try{
                                    sContext.unregisterReceiver(smsReceiver);
                                }catch(Exception e){}

                                final EditText editPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);

                                textWatcher = new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        editPhoneNumber.setError(null);
                                        editPhoneNumber.removeTextChangedListener(textWatcher);
                                    }
                                };

                                editPhoneNumber.setError("Verify phone number");
                                editPhoneNumber.addTextChangedListener(textWatcher);
                            }
                        }
                    };
                    handler.postDelayed(runnable,60000);
                    Log.d("Tag", "SMS broadcast sent");
                }
                catch(IllegalArgumentException e)
                {
                    enable();
                }

            }
            else
            {
                enable();
                Toast.makeText(this,"Please insert all data.",Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e)
        {
            enable();
            Toast.makeText(this,"Please only input numbers for the phone", Toast.LENGTH_SHORT).show();
        }


    }

    public void verifyPhoneNumber(String address, String message)
    {
        Log.d("Tag", "Received phone number");
        if(secretMessage.equals(message))
        {
            registerUserPhp();

        }
    }


    private void registerUserPhp(){
        new HTTPPost().execute(this);

    }

    public static String random() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int length = 6;
        char tempChar;
        for (int i = 0; i < length; i++){
            tempChar = chars[generator.nextInt(chars.length)];
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private class HTTPPost extends AsyncTask<Object, Void, HttpResponse> {

        private Exception exception;
        private Context context;
        private InputStream is;
        private StringBuilder sb;


        protected HttpResponse doInBackground(Object... objects) {
            context = (Context) objects[0];
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("name", name));
            nameValuePairs.add(new BasicNameValuePair("uid", uid));
            nameValuePairs.add(new BasicNameValuePair("phoneNumber", finalPhoneNumber));
            //http post
            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://pan0166.panoulu.net/community/backend/registerUser.php");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                return response;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(HttpResponse feed) {
            Header[] headers = feed.getAllHeaders();
            enable();
            if(headers.length>3)
            {
                String responseCode = headers[3].getValue();
                if(responseCode.equals("409")){
                    Log.e("Sign Up","Unknown error");
                }
                else if(responseCode.equals("201"))
                {
                    storeLogin();
                }
                else
                {
                    Log.e("Sign Up", "Unknown error");
                }
            }
            else
            {
                Log.e("Sign Up", "Unknown error");
            }

        }
    }

    private void storeLogin(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("phoneNumber", finalPhoneNumber);
        editor.putBoolean("signedIn", true);
        editor.putString("name", name);
        editor.putString("uid",uid);
        editor.apply();

        Intent intent = new Intent(this, HomeScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void disable(){
        loginText.setText("Logging In...");
        ViewGroup.LayoutParams params = loginText.getLayoutParams();
        params.resolveLayoutDirection(RelativeLayout.ALIGN_PARENT_RIGHT);
        loginText.setLayoutParams(params);
        loginLoading.setVisibility(View.VISIBLE);
        edTName.setEnabled(false);
        edTPhone.setEnabled(false);
        verifyPhone.setEnabled(false);
        verifyPhone.setClickable(false);
    }

    private void enable(){
        loginText.setText("Verify your phone number");
        ViewGroup.LayoutParams params = loginText.getLayoutParams();
        params.resolveLayoutDirection(RelativeLayout.CENTER_IN_PARENT);
        loginText.setLayoutParams(params);
        loginLoading.setVisibility(View.GONE);
        edTName.setEnabled(true);
        edTPhone.setEnabled(true);
        verifyPhone.setEnabled(true);
        verifyPhone.setClickable(true);
    }

    private SMSSent smsSentReceiver = new SMSSent();
    public class SMSSent extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Tag:","Message sent");
            try{
                context.unregisterReceiver(smsSentReceiver);
            }catch(Exception e)
            {

            }
        }
    }
}
