package fi.oulu.acp.communityreminder.services;


import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

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


public class TemperatureService extends IntentService {

    private static String KEY = "5c76a5f28408e8aae498bd15b24d32cc";
    private static String CITY_ID = "643492";

    private static int timeZero = 40;
    private static int timeFive = 20;
    private static int timeTen = 15;
    private static int timeFifteen = 12;
    private static int timeTwenty = 10;
    private static int timeOther = 8;


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

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getApplicationContext().registerReceiver(null, iFilter);

            int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            float temp = (float) temperature/(float) 10;
            if(temp>=prevTemp)
            {
                startDownTemperature = System.currentTimeMillis();
                highestTemp = temp;
            }
            else
            {
                long elapsedTime = System.currentTimeMillis() - startDownTemperature;
                elapsedTime /= 1000;
                Log.d("Elapsed",""+elapsedTime);
                if(highestTemp-temp>10)
                {
                    if(elapsedTime>maxTime*60&&ambientTemp<5)
                    {
                        //TODO notification
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
        startService();
        return START_STICKY;
    }

    private void callService(int time) {
        _handler.removeCallbacks(_runnable);
        _handler.postDelayed(_runnable, time*60*1000);
    }

    private void callServiceAmb() {
        _handlerAmb.removeCallbacks(_runnableAmb);
        _handlerAmb.postDelayed(_runnableAmb, 60*60*1000);
    }

    private void startService(){
        _handler.removeCallbacks(_runnable);
        _handler.post(_runnable);

        _handlerAmb.removeCallbacks(_runnableAmb);
        _handlerAmb.post(_runnableAmb);
    }
}