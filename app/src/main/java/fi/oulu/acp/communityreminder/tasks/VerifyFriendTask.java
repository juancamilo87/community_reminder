package fi.oulu.acp.communityreminder.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import fi.oulu.acp.communityreminder.ContactListActivity;
import fi.oulu.acp.communityreminder.db.FriendsDataSource;

/**
 * Created by JuanCamilo on 3/29/2015.
 */
public class VerifyFriendTask extends AsyncTask<Object, Void, HttpResponse> {

    private ContactListActivity context;
    private String friend_id;

    protected HttpResponse doInBackground(Object... objects) {
        context = (ContactListActivity) objects[0];
        String user_id = (String) objects[1];
        friend_id = (String) objects[2];
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
        nameValuePairs.add(new BasicNameValuePair("friend_id", friend_id.replaceAll("[^0-9+]", "")));

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://pan0166.panoulu.net/community/backend/verifyFriend.php?key=fdsjfkiajl3ir3f");
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

                Log.e("VerifyFriend", "Unknown error");
            }
            else if(responseCode.equals("201"))
            {
                FriendsDataSource ds = new FriendsDataSource(context);
                ds.open();
                ds.verifyFriend(friend_id);
                context.reloadList(ds.getAllFriends());
                ds.close();

                Log.d("VerifyFriend","Friend accepted");
            }
            else if(responseCode.equals("202"))
            {
                Log.d("VerifyFriend","Friend already added");
            }
            else
            {
                Log.e("VerifyFriend","Unknown error");
            }
        }
        else
        {
            Log.e("VerifyFriend","Unknown error");
        }

    }
}
