package fi.oulu.acp.communityreminder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import static fi.oulu.acp.communityreminder.Config.SERVER_URL;

/**
 * Created by alex on 14.3.2015.
 */
public class ServerUtilities {
    private static final int MAX_ATTEMPTS = 1;
    private static final String TAG = "ServerUtilities";
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    public static void register(final Context context, final String regId){
        Log.i(TAG, "registering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL;
        Map<String, String> params = new HashMap<>();
        params.put("regId", regId);
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 0; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
                //displayMessage(context, context.getString(
                //        R.string.server_registering, i, MAX_ATTEMPTS));
                post(serverUrl, params);
                //GCMRegistrar.setRegisteredOnServer(context, true);
                //String message = context.getString(R.string.server_registered);
                //CommonUtilities.displayMessage(context, message);
                return;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
    }

    public static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    public static void sendMessage(final String uid, final String title, final String message){
        Map<String, String> notificationParams = new HashMap<String, String>();

        notificationParams.put("Title", title);
        notificationParams.put("Message", message);
        DateFormat datef = new SimpleDateFormat("yyyy.MM.dd");
        DateFormat dayf = new SimpleDateFormat("EEE");
        DateFormat timef = new SimpleDateFormat("HH:mm Z");
        String date = datef.format(Calendar.getInstance().getTime());
        String day = dayf.format(Calendar.getInstance().getTime());
        String time = timef.format(Calendar.getInstance().getTime());
        notificationParams.put("Date", date);
        notificationParams.put("Day", day);
        notificationParams.put("Time", time);
        FlurryAgent.logEvent("Notification_sent", notificationParams);
        new AsyncTask<Object, Void, HttpResponse>(){
            @Override
            protected HttpResponse doInBackground(Object... objects){
                try {
                    String json = "{\"user_id\":\""+uid+"\"";
                    json+= ", \"message\":\""+message+"\"";
                    json+= ", \"title\":\""+title+"\"";
                    json += "}";
                    StringEntity params = new StringEntity(json);


                    String url = "http://pan0166.panoulu.net/community/backend/broadcastAlert.php?key=fdsjfkiajl3ir3f";

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.addHeader("content-type", "application/x-www-form-urlencoded");
                    httpPost.setEntity(params);

                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    return httpResponse;
                } catch (IOException ex) {
                    Log.e("++++++++", "BLA");
                    return null;
                }
                //return msg;
            }
            @Override
            protected void onPostExecute(HttpResponse msg){
                Log.e(TAG, msg.toString());
                try{
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(msg.getEntity().getContent(),"UTF-8"));
                    String fhg = bufferedReader.readLine();
                    Log.e(TAG, fhg);
                } catch (Exception e){
                    Log.e(TAG, e.toString());
                }

            }
        }.execute(null, null, null);
    }


}
