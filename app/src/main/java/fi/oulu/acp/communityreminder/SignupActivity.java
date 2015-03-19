package fi.oulu.acp.communityreminder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Random;


public class SignupActivity extends Activity {


    private String uid;
    private BroadcastReceiver smsReceiver;
    private String finalPhoneNumber;
    private String name;
    private TextWatcher textWatcher;
    private String secretMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_screen);
        final Button verifyPhone = (Button) findViewById(R.id.verify_button);
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
        unregisterReceiver(smsReceiver);
        super.onDestroy();
    }

    private void verifyPhone()
    {
        //Todo add thinking part of UI and block it
        //Todo DB adjustments
        //Todo Store sharedPreferences that user is already logged in
        name = ((EditText) findViewById(R.id.editName)).getText().toString();
        finalPhoneNumber = ((EditText)findViewById(R.id.editPhoneNumber)).getText().toString();
        finalPhoneNumber = finalPhoneNumber.replaceAll(" ", "");
        try
        {
            Integer.parseInt(finalPhoneNumber);
        }catch (Exception e)
        {
            Toast.makeText(this,"Please only input numbers for the phone", Toast.LENGTH_SHORT).show();
        }
        if(!finalPhoneNumber.equals("")&&!name.equals(""))
        {
            smsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Object[] pdus=(Object[])intent.getExtras().get("pdus");
                    SmsMessage shortMessage=SmsMessage.createFromPdu((byte[]) pdus[0]);

                    String sender = shortMessage.getOriginatingAddress();
                    String message = shortMessage.getDisplayMessageBody();
                    verifyPhoneNumber(sender, message);
                    context.unregisterReceiver(smsReceiver);
                }};

            IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(smsReceiver, intentFilter);
            Log.d("Tag", "Register SMS receiver");

            SmsManager sms = SmsManager.getDefault();
            try
            {
                Log.d("Tag", "Sending sms...");
                secretMessage = random();
                sms.sendTextMessage(finalPhoneNumber, null, secretMessage, null, null);
            }
            catch(IllegalArgumentException e)
            {

            }

        }
        else
        {
            Toast.makeText(this,"Please insert all data.",Toast.LENGTH_SHORT).show();
        }

    }

    public void verifyPhoneNumber(String address, String message)
    {
        Log.d("Tag", "Received phone number");
        if(secretMessage.equals(message))
        {
            registerUserPhp();

        }
        else
        {
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

            editPhoneNumber.setError("Wrong phone number");
            editPhoneNumber.addTextChangedListener(textWatcher);

        }
    }


    private void registerUserPhp(){
        new HTTPPost().execute(this);

    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(6);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
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
            if(headers.length>3)
            {
                String responseCode = headers[3].getValue();
                if(responseCode.equals("409")){
                    Toast.makeText(context,"Error creating account",Toast.LENGTH_SHORT).show();
                }
                else if(responseCode.equals("201"))
                {
                    Toast.makeText(context,"Account created",Toast.LENGTH_SHORT).show();
                    storeLogin();
                }
                else
                {
                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
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
        onDestroy();

    }
}
