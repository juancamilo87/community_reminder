package fi.oulu.acp.communityreminder.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
import java.util.List;

import fi.oulu.acp.communityreminder.Contact;
import fi.oulu.acp.communityreminder.db.ContactsDataSource;

/**
 * Created by JuanCamilo on 3/29/2015.
 */
public class VerifyContactsTask extends AsyncTask<Object, Void, HttpResponse> {

    private Exception exception;
    private Context context;
    private InputStream is;
    private StringBuilder sb;
    private List<String> contactsIndex;
    private ArrayList<Contact> theContacts;


    protected HttpResponse doInBackground(Object... objects) {
        context = (Context) objects[0];
        theContacts = (ArrayList) objects[1];
        try {
            String json = "{\"phones\":[";
            for (int i = 0; i < theContacts.size(); i++)
            {
                String thisPhone = theContacts.get(i).getPhones().get(0);
                thisPhone = thisPhone.replaceAll("[^0-9+]", "");
                json += "\""+thisPhone + "\",";
            }
            json = json.substring(0,json.length()-1);
            json += "]}";
            StringEntity params = new StringEntity(json);

            //http post

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://pan0166.panoulu.net/community/backend/verifyNumbers.php?key=fdsjfkiajl3ir3f");

            httppost.addHeader("content-type", "application/x-www-form-urlencoded");
            httppost.setEntity(params);
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
                Log.e("Contacts", "Unkown error");
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
                        contactsIndex = new ArrayList<String>();
                        JSONObject parentObject = new JSONObject(answer);
                        JSONArray jsonArray = parentObject.getJSONArray("result");

                        if (jsonArray != null) {
                            int len = jsonArray.length();
                            for (int i=0;i<len;i++){
                                contactsIndex.add(jsonArray.get(i).toString());
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                ArrayList<Contact> contactsInApp = new ArrayList<>();
                for(int i = 0; i< contactsIndex.size(); i++)
                {
                    int index = Integer.parseInt(contactsIndex.get(i));
                    contactsInApp.add(theContacts.get(index));
                }
                if(contactsInApp.size()>0)
                    addContactsToDB(contactsInApp);
                Log.d("Contacts","Contacts loaded");
            }
            else
            {
                Log.e("Contacts","Unkown error");
            }
        }
        else
        {
            Log.e("Contacts", "Unkown error");
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
