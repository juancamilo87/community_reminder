package fi.oulu.acp.communityreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ActionMenuView;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;


public class TemperatureActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mTemp;

    private BroadcastReceiver mBatInfoReceiver;
    private LinearLayout temperatureList;
    private long startTime;
    private AlarmManager alarm;
    private PendingIntent pintent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        temperatureList = (LinearLayout) findViewById(R.id.linear_layout_temps);
        startTime = System.currentTimeMillis();
        Intent intent = new Intent(this, TemperatureService.class);
        pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_temperature, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if(mTemp == null)
        {
            Log.d("Sensor", "No ambient temperature sensor");
            mTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        }
        if(mTemp == null)
        {
            Calendar cal = Calendar.getInstance();




            alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            // schedule for every 5 seconds
            alarm.cancel(pintent);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60 * 1000, pintent);


            Log.d("Sensor", "No internal temperature sensor");

        }
        else
        {
            mSensorManager.registerListener(this,
                    mTemp,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        //mSensorManager.registerListener(this, mTemp);



    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alarm.cancel(pintent);
    }

    private void updateTemperature(double temp)
    {
        GridLayout newGridLayout = new GridLayout(this);
        TextView time = new TextView(this);
        TextView txtTemp = new TextView(this);
        long newTime = System.currentTimeMillis()-startTime;
        time.setText(""+newTime+"    -     ");
        txtTemp.setText("      "+temp);
        newGridLayout.setColumnCount(2);
        txtTemp.setTypeface(null, Typeface.BOLD);

        newGridLayout.addView(time);
        newGridLayout.addView(txtTemp);
        temperatureList.addView(newGridLayout);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor mSensor = event.sensor;
        float temperature[] = event.values;
    }
}
