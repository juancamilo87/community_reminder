package fi.oulu.acp.communityreminder;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import fi.oulu.acp.communityreminder.db.ContactsDataSource;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver{
    private Context context;
    private ContactsDataSource source = new ContactsDataSource(context);

    @Override
    public void onReceive(Context context, Intent intent){
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                NotificationService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle extras = intent.getExtras();
        String tit = extras.getString("Title", "");
        String msg = extras.getString("Message", "");
        String num = extras.getString("PhoneNumber", "");

        Toast.makeText(context, tit + msg + num, Toast.LENGTH_LONG).show();
        Notification notification = new Notification(R.drawable.app_icon_notificaton, source.getName(num) + "\n"
                + tit + "\n" + msg, System.currentTimeMillis());
        notification.setLatestEventInfo(context, tit, msg, null);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(1, notification);

        if (tit.equals("pedometerGoal")){
            SharedPreferences.Editor editor = context.getSharedPreferences("fi.oulu.acp.communityreminder", Context.MODE_PRIVATE).edit();
            editor.putInt("yourGoal", Integer.parseInt(msg));
            editor.commit();
        }
    }

    private void writeToDatabase(Context context){

    }
}
