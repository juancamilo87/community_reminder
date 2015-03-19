package fi.oulu.acp.communityreminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Calendar;


public class TemperatureActivity extends Activity {


    private AlarmManager alarm;
    private PendingIntent pintent;
    private EditText environmentTemperature;
    private RadioGroup performance;
    private SharedPreferences prefs;

    public static final int NOTIFICATION_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        environmentTemperature = (EditText) findViewById(R.id.ttemp);
        performance = (RadioGroup) findViewById(R.id.rtemp);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean gathering = prefs.getBoolean("gathering-data", false);
        if(gathering){
            disableUI();
        }
        Button btnGather = (Button) findViewById(R.id.collect_button);
        btnGather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDataCollection();
            }
        });
//        Intent intent = new Intent(this, TemperatureService.class);
//        pintent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.remove("tempHistory");
//        editor.commit();

    }


    private void disableUI(){
        environmentTemperature.setEnabled(false);
        for(int i = 0; i < performance.getChildCount(); i++){
            ((RadioButton)performance.getChildAt(i)).setEnabled(false);
        }
        Button btn = (Button)findViewById(R.id.collect_button);
        btn.setEnabled(false);
        btn.setText("Gathering data...");
    }
    private void enableUI(){
        environmentTemperature.setEnabled(true);
        for(int i = 0; i < performance.getChildCount(); i++){
            ((RadioButton)performance.getChildAt(i)).setEnabled(true);
        }
        Button btn = (Button)findViewById(R.id.collect_button);
        btn.setEnabled(true);
        btn.setText("Start data collection");
    }


    @Override
    protected void onResume() {
        super.onResume();

        boolean gathering = prefs.getBoolean("gathering-data", false);
        if(gathering){
            disableUI();
        }
        else
        {
            enableUI();
        }
//        Calendar cal = Calendar.getInstance();
//        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarm.cancel(pintent);
//        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 3 * 1000, pintent);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.remove("tempHistory");
//        editor.commit();
//        alarm.cancel(pintent);
    }

    public void startDataCollection()
    {
        int selectedId = performance.getCheckedRadioButtonId();
        String envTemp = environmentTemperature.getText().toString();
        if(envTemp==null||envTemp.equals("")||selectedId==-1)
        {
            Toast.makeText(this,"Please fill in all the information",Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Start gathering data
            int currentId = prefs.getInt("currentId", 0);
            currentId++;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("currentId", currentId);
            editor.commit();

            RadioButton selectedPerformance = (RadioButton) findViewById(selectedId);
            String perf = selectedPerformance.getText().toString();

            Intent intent = new Intent(this, TemperatureBroadcastReceiver.class);
            intent.putExtra("temperature", envTemp);
            intent.putExtra("performance", perf);
            intent.putExtra("currentId",currentId);

            pintent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar cal = Calendar.getInstance();
            alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pintent);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 3 * 1000, pintent);
            editor.putBoolean("gathering-data", true);
            disableUI();

            //Create permanent notification

            Intent newIntent = new Intent(this, TemperatureNotificationManager.class);

            PendingIntent pintentNotification = PendingIntent.getBroadcast(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification noti = new Notification.Builder(this)
                    .setContentTitle("Temperature Module")
                    .setContentText("Gathering data...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true) // Again, THIS is the important line
                    .addAction(R.mipmap.ic_launcher,"Stop", pintentNotification)
                    .build();

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, noti);
        }

    }

}
