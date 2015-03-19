package fi.oulu.acp.communityreminder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import fi.oulu.acp.communityreminder.db.TemperatureDataSource;

/**
 * Created by JuanCamilo on 3/5/2015.
 */
public class TemperatureBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Testing", "Service got started");
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        float temp = (float) temperature/(float) 10;
        Log.d("Temp:", ""+temp);

        int session = intent.getIntExtra("currentId", 0);
        String envTemp = intent.getStringExtra("temperature");
        String performance = intent.getStringExtra("performance");

        TemperatureDataSource ds = new TemperatureDataSource(context);
        ds.open();
        ds.addTempData(session, performance, envTemp, temp);

        //GATHER DATA

//        ArrayList<Float> list;
//        list = getStringArrayPref(context, "tempHistory");
//        Float[] temperatures = new Float[list.size()];
//        temperatures = list.toArray(temperatures);
//        float previousAverage = average(temperatures);
//        Queue<Float> tempHistory = new LinkedList<Float>(Arrays.asList(temperatures));
//
//        if(tempHistory.size()==10)
//        {
//            float number = tempHistory.remove();
//        }
//        tempHistory.add(temp);
//        Log.d("tag","Temp added: "+(temp));
//
//        float currentAverage = average(tempHistory);
//        Log.d("tag","PreviousAverage: "+previousAverage);
//        Log.d("tag","CurrentAverage: "+currentAverage);
//
//        list = new ArrayList<Float>(tempHistory);
//        setStringArrayPref(context, "tempHistory", list);


    }

    public static void setStringArrayPref(Context context, String key, ArrayList<Float> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }

    public static ArrayList<Float> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<Float> results = new ArrayList<Float>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    Float result = (float) a.optDouble(i);
                    results.add(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public static float average(Float[] values){
        float total = 0;
        for(int i = 0; i<values.length-1;i++)
        {
            total+= values[i];
        }
        return total/(float)values.length;
    }

    public static float average(Queue<Float> values){
        float total = 0;
        int size = 0;
        Iterator iterator = values.iterator();
        while(iterator.hasNext()){
            size++;
            total +=(float) iterator.next();
        }
        return total/(float)size;

    }
}
