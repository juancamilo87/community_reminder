package fi.oulu.acp.communityreminder.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.oulu.acp.communityreminder.Contact;
import fi.oulu.acp.communityreminder.db.ContactsDataSource;
import fi.oulu.acp.communityreminder.db.FriendsDataSource;

/**
 * Created by JuanCamilo on 3/29/2015.
 */
public class AddFriendTask extends AsyncTask<Object, Void, HttpResponse> {

    private Context context;
    private List<String> contactsIndex;
    String friend_id;

    protected HttpResponse doInBackground(Object... objects) {
        context = (Context) objects[0];
        String user_id = (String) objects[1];
        friend_id = (String) objects[2];
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
        nameValuePairs.add(new BasicNameValuePair("friend_id", friend_id));

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://pan0166.panoulu.net/community/backend/registerFriend.php");
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
                Log.e("RegisterFriend", "Unknown error");
            }
            else if(responseCode.equals("201"))
            {
                ContactsDataSource ds = new ContactsDataSource(context);
                ds.open();
                ds.makeFriend(friend_id);
                Contact contact = ds.getContact(friend_id);
                ds.close();
                FriendsDataSource fds = new FriendsDataSource(context);
                fds.open();
                byte[] bArray = null;
                if(contact.getPicture()!=null)
                {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    contact.getPicture().compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bArray = bos.toByteArray();
                }
                fds.addFriendsData(bArray,contact.getName(),contact.getPhones().get(0),null,0);
                fds.close();
                Log.d("RegisterFriend","Friend added");
            }
            else if(responseCode.equals("202"))
            {
                Log.d("RegisterFriend","Friend already added");
            }
            else
            {
                Log.e("RegisterFriend","Unknown error");
            }
        }
        else
        {
            Log.e("RegisterFriend","Unknown error");
        }

    }


    private void addContactsToDB(ArrayList<Contact> contactsToAdd){
        Collections.sort(contactsToAdd, new CustomComparator());
        ContactsDataSource ds = new ContactsDataSource(context);
        ds.open();
        ds.recreateTable();
        for(int i = 0; i<contactsToAdd.size(); i++)
        {
            Bitmap photo = contactsToAdd.get(i).getPicture();
            byte[] bArray = null;
            if(photo!=null)
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
                bArray = bos.toByteArray();
            }

            String name = contactsToAdd.get(i).getName();
            String phone = contactsToAdd.get(i).getPhones().get(0);

            ds.addContactsData(bArray, name, phone, 0);
        }
        ds.close();
    }

    public class CustomComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact o1, Contact o2) {
            return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
        }
    }
}
