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
import android.view.ViewGroup;
import android.widget.Button;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fi.oulu.acp.communityreminder.tasks.VerifyContactsTask;

/**
 * Created by JuanCamilo on 3/28/2015.
 */
public class SignInActivity extends Activity {

    private String name;
    private String phoneNumber;
    private String device_id;
    private TextView txtSignIn;
    private ProgressBar signInProgress;
    private RelativeLayout signInLayOut;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);
        context = this;
        signInLayOut = (RelativeLayout) findViewById(R.id.sign_in_button);

        signInLayOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignUp();
            }
        });

        txtSignIn = (TextView) findViewById(R.id.sign_up_text);
        signInProgress = (ProgressBar) findViewById(R.id.sign_in_loading);
        disable();
        verifyUser();




    }

    private void verifyUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("signedIn",false))
        {
            name = prefs.getString("name","none");
            phoneNumber = prefs.getString("phoneNumber","none");
            device_id = prefs.getString("uid","none");
            new HTTPGet().execute(this);
        }
        else
        {
            enable();
        }
    }





    public void goToSignUp(){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    public void goToMain(){

        Intent intent = new Intent(this, HomeScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private class HTTPGet extends AsyncTask<Object, Void, HttpResponse> {

        private Exception exception;
        private Context context;
        private InputStream is;
        private StringBuilder sb;


        protected HttpResponse doInBackground(Object... objects) {
            context = (Context) objects[0];
            //http post
            try{
                HttpClient httpclient = new DefaultHttpClient();
                String url = "http://pan0166.panoulu.net/community/backend/verifyUser.php";
                url += "?uid="+device_id+"&phoneNumber="+phoneNumber;
                HttpGet httpget = new HttpGet(url);
                HttpResponse response = httpclient.execute(httpget);
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
                    Toast.makeText(context, "Couldn't log in automatically", Toast.LENGTH_SHORT).show();
                    enable();
                }
                else if(responseCode.equals("201"))
                {
                    HttpEntity entity = feed.getEntity();
                    try{
                        if (entity != null) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(
                                    entity.getContent()));

                            // NEW CODE
                            String answer = in.readLine();
                            answer = answer.trim();
                            in.close();

                            if(answer.equals("true"))
                            {
                                Toast.makeText(context,"Logged in",Toast.LENGTH_SHORT).show();
                                goToMain();
                            }
                            else
                            {
                                Toast.makeText(context,"The account has changed, please login again",Toast.LENGTH_SHORT).show();
                                enable();
                            }

                        }
                    }catch(Exception e){
                        Toast.makeText(context, "Couldn't log in automatically", Toast.LENGTH_SHORT).show();
                        enable();
                    }




                }
                else
                {
                    Toast.makeText(context, "Couldn't log in automatically", Toast.LENGTH_SHORT).show();
                    enable();
                }
            }
            else
            {
                Toast.makeText(context, "Couldn't log in automatically", Toast.LENGTH_SHORT).show();
                enable();
            }

        }
    }

    private void disable(){
        txtSignIn.setText("Loading...");
        ViewGroup.LayoutParams params = txtSignIn.getLayoutParams();
        params.resolveLayoutDirection(RelativeLayout.ALIGN_PARENT_RIGHT);
        txtSignIn.setLayoutParams(params);
        signInProgress.setVisibility(View.VISIBLE);
        signInLayOut.setEnabled(false);
        signInLayOut.setClickable(false);
    }

    private void enable(){
        txtSignIn.setText("Sign in with phone number");
        ViewGroup.LayoutParams params = txtSignIn.getLayoutParams();
        params.resolveLayoutDirection(RelativeLayout.CENTER_IN_PARENT);
        txtSignIn.setLayoutParams(params);
        signInProgress.setVisibility(View.GONE);
        signInLayOut.setEnabled(true);
        signInLayOut.setClickable(true);
    }

}
