package fi.oulu.acp.communityreminder.services;


import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import fi.oulu.acp.communityreminder.ServerUtilities;


public class TemperatureService extends IntentService {

    private static String KEY = "5c76a5f28408e8aae498bd15b24d32cc";
    private static String CITY_ID = "643492";

    private int timeZero = 40;
    private int timeFive = 20;
    private int timeTen = 15;
    private int timeFifteen = 12;
    private int timeTwenty = 10;
    private int timeOther = 8;
    private String userId;


    private long startDownTemperature;
    private float prevTemp;
    private float ambientTemp;
    private float highestTemp;


    Handler _handler = new Handler();


    Runnable _runnable = new Runnable() {
        @Override
        public void run() {

            int maxTime = 0;
            if(ambientTemp>0)
            {
                maxTime = timeZero;
            }
            else if(ambientTemp>-5)
            {
                maxTime = timeFive;
            }
            else if(ambientTemp>-10)
            {
                maxTime = timeTen;
            }
            else if(ambientTemp>-15)
            {
                maxTime = timeFifteen;
            }
            else if(ambientTemp>-20)
            {
                maxTime = timeTwenty;
            }
            else
            {
                maxTime = timeOther;
            }
            Log.d("maxTimetemp",maxTime+"");

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getApplicationContext().registerReceiver(null, iFilter);

            int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            float temp = (float) temperature/(float) 10;
            Toast.makeText(getApplicationContext(),"Battery temp: "+temp,Toast.LENGTH_LONG).show();
            if(temp>prevTemp)
            {
                Log.d("TEmp","higher temp");
                startDownTemperature = System.currentTimeMillis();
                highestTemp = temp;
            }
            else
            {
                Log.d("Temp","lower temp");
                long elapsedTime = System.currentTimeMillis() - startDownTemperature;
                elapsedTime /= 1000;
                Log.d("ElapsedTemp",""+elapsedTime);
                if(highestTemp-temp>5)
                {
                    Log.d("temp","more than 5 difference");
                    if(elapsedTime>maxTime*60&&ambientTemp<5)
                    {
                        Log.d("temp","time to send alert");
                        ServerUtilities.sendMessage(userId,"Temperature alarm","He has been for more than "+ Math.ceil(elapsedTime/60) + " minutes at "+ (Math.round(ambientTemp*10)/10.0) + "\\u00B0C.");
                        Log.d("Alert","Too long outside");
                    }
                }
            }

            prevTemp = temp;

            Log.d("Temp", ""+temp);

            callService(maxTime/3);
        }
    };

    Handler _handlerAmb = new Handler();


    Runnable _runnableAmb = new Runnable() {
        @Override
        public void run() {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        HttpClient httpclient = new DefaultHttpClient();
                        String url = "http://api.openweathermap.org/data/2.5/weather?";
                        url += "id="+CITY_ID;
                        url += "&APPID="+KEY;
                        HttpGet httpget = new HttpGet(url);
                        HttpResponse response = httpclient.execute(httpget);
                        HttpEntity entity = response.getEntity();
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
                        Log.d("Answer",answer);
                        JSONObject parentObject = new JSONObject(answer);
                        JSONObject main = parentObject.getJSONObject("main");
                        ambientTemp = (float)(main.getDouble("temp")-273.15);
                        Log.d("AmbTemp",""+ambientTemp);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    callServiceAmb();
                }
            }).start();
        }
    };

    public TemperatureService(){
        super(TemperatureService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Testing", "Service got started");
        startDownTemperature = System.currentTimeMillis();
        prevTemp = -50;
        ambientTemp = 20;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userId = prefs.getString("phoneNumber","");
        startService();
        return START_STICKY;
    }

    private void callService(int time) {
        updateValues();
        _handler.removeCallbacks(_runnable);
        _handler.postDelayed(_runnable, time*60*1000);
    }

    private void callServiceAmb() {
        _handlerAmb.removeCallbacks(_runnableAmb);
        _handlerAmb.postDelayed(_runnableAmb, 60*60*1000);
    }

    private void startService(){
        updateValues();
        _handler.removeCallbacks(_runnable);
        _handler.post(_runnable);

        _handlerAmb.removeCallbacks(_runnableAmb);
        _handlerAmb.post(_runnableAmb);
    }

    private void updateValues(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        timeZero = prefs.getInt("timeZero",40);
        timeFive = prefs.getInt("timeFive",20);
        timeTen = prefs.getInt("timeTen",15);
        timeFifteen = prefs.getInt("timeFifteen",12);
        timeTwenty = prefs.getInt("timeTwenty",10);
        timeOther = prefs.getInt("timeOther",8);
    }
}