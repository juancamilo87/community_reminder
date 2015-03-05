package fi.oulu.acp.communityreminder;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by JuanCamilo on 3/5/2015.
 */
public class TemperatureService extends Service {


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.d("Testing", "Service got created");
        Toast.makeText(this, "ServiceClass.onCreate()", Toast.LENGTH_LONG).show();


        //this.onDestroy();



    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Toast.makeText(this, "ServiceClass.onDestroy()", Toast.LENGTH_LONG).show();
        Log.d("Testing", "Service got destroyed");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);

        Log.d("Testing", "Service got started");
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        Log.d("Status:", ""+status);

        int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        double temp = (double) temperature/(double) 10;
        Log.d("Temp:", ""+temp);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;
        Toast.makeText(this, "Temp: "+temp+"  -  Level: "+ batteryPct, Toast.LENGTH_SHORT).show();
        Log.d("Level: ", ""+batteryPct);
    }
}
