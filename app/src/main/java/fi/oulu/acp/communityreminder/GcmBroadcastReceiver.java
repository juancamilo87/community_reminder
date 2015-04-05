package fi.oulu.acp.communityreminder;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                NotificationService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
        Toast.makeText(context, intent.getExtras().toString(), Toast.LENGTH_SHORT).show();
        Log.e("++++++++", intent.getExtras().toString());
    }
}
