package fi.oulu.acp.communityreminder.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import fi.oulu.acp.communityreminder.Contact;
import fi.oulu.acp.communityreminder.db.ContactsDataSource;
import fi.oulu.acp.communityreminder.db.FriendsDataSource;

/**
 * Created by JuanCamilo on 4/6/2015.
 */
public class ChangeTemperatureTimesTask  extends AsyncTask<Object, Void, HttpResponse> {


    private Context context;


    protected HttpResponse doInBackground(Object... objects) {
        context = (Context) objects[1];

        String[] timeTemp = (String[]) objects[2];

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userId = prefs.getString("phoneNumber","");
        String phone = (String) objects[0];

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id", userId));
        nameValuePairs.add(new BasicNameValuePair("phoneNumber", phone));
        nameValuePairs.add(new BasicNameValuePair("time_zero", timeTemp[0]));
        nameValuePairs.add(new BasicNameValuePair("time_five", timeTemp[1]));
        nameValuePairs.add(new BasicNameValuePair("time_ten", timeTemp[2]));
        nameValuePairs.add(new BasicNameValuePair("time_fifteen", timeTemp[3]));
        nameValuePairs.add(new BasicNameValuePair("time_twenty", timeTemp[4]));
        nameValuePairs.add(new BasicNameValuePair("time_other", timeTemp[5]));


        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://pan0166.panoulu.net/community/backend/changeTemperatures.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(HttpResponse feed) {
        Header[] headers = feed.getAllHeaders();
        if(headers.length>3)
        {
            String responseCode = headers[3].getValue();
            if(responseCode.equals("409")){

                //Log.e("Error updating temp times", "Unknown error");
            }
            else if(responseCode.equals("201"))
            {
                Toast.makeText(context,"Temperature times changed",Toast.LENGTH_SHORT).show();
            }
            else if(responseCode.equals("202"))
            {
                //Log.d("RegisterFriend","Friend already added");
            }
            else
            {
                //Log.e("RegisterFriend","Unknown error");
            }
        }
        else
        {
            Log.e("RegisterFriend","Unknown error");
        }

    }
}
