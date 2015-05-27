package fi.oulu.acp.communityreminder.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by JuanCamilo on 4/6/2015.
 */
public class GetTemperatureTimesTask extends AsyncTask<Object, Void, HttpResponse> {


    private Context context;


    protected HttpResponse doInBackground(Object... objects) {
        context = (Context) objects[0];

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userId = prefs.getString("phoneNumber","");

        try {
            HttpClient httpclient = new DefaultHttpClient();
            String url = "http://pan0166.panoulu.net/community/backend/getTemperatures.php";
            url+="?user_id="+userId;
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
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
                HttpEntity entity = feed.getEntity();
                try {
                    if (entity != null) {
                        InputStream ips = entity.getContent();
                        BufferedReader buf = new BufferedReader(new InputStreamReader(ips, "UTF-8"));
                        StringBuilder sb = new StringBuilder();
                        String s;
                        while (true) {
                            s = buf.readLine();
                            if (s == null || s.length() == 0)
                                break;
                            sb.append(s);

                        }
                        buf.close();
                        ips.close();
                        String answer = sb.toString();
                        Log.d("Tag", answer);
                        JSONArray jsonArray = new JSONArray(answer);

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putInt("timeZero",Integer.parseInt((String)jsonArray.get(0)));
                        editor.putInt("timeFive",Integer.parseInt((String)jsonArray.get(1)));
                        editor.putInt("timeTen",Integer.parseInt((String)jsonArray.get(2)));
                        editor.putInt("timeFifteen",Integer.parseInt((String)jsonArray.get(3)));
                        editor.putInt("timeTwenty",Integer.parseInt((String)jsonArray.get(4)));
                        editor.putInt("timeOther",Integer.parseInt((String)jsonArray.get(5)));
                        editor.apply();
                        //Toast.makeText(context, prefs.getInt("timeZero", 0), Toast.LENGTH_LONG).show();

                    }
                }catch(Exception e){}


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
            //Log.e("RegisterFriend","Unknown error");
        }

    }
}
