package fi.oulu.acp.communityreminder;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import java.util.HashMap;

import fi.oulu.acp.communityreminder.db.ContactsDataSource;
import fi.oulu.acp.communityreminder.db.NotificationsDataSource;
import fi.oulu.acp.communityreminder.tasks.GetTemperatureTimesTask;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver{
    private Context context;


    @Override
    public void onReceive(Context context, Intent intent){
        ContactsDataSource source = new ContactsDataSource(context);
        // Explicitly specify that GcmIntentService will handle the intent.
        //ComponentName comp = new ComponentName(context.getPackageName(),
        //        NotificationService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        //startWakefulService(context, (intent.setComponent(comp)));
        //setResultCode(Activity.RESULT_OK);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        source.open();

        Bundle extras = intent.getExtras();
        String tit = extras.getString("Title", "");
        String msg = extras.getString("Message", "");
        String num = extras.getString("PhoneNumber", "");

        //Flurry
        HashMap<String, String> eventParams = new HashMap<>();
        eventParams.put("Title", tit);
        eventParams.put("Message", msg);
        eventParams.put("PhoneNumber", num);

        FlurryAgent.logEvent("Notification", eventParams, true);

        if (tit.equals("Temperatures")){
            new GetTemperatureTimesTask().execute(context);
        }

        NotificationsDataSource ds = new NotificationsDataSource(context);
        ds.open();
        Log.e("++++++++++", "RRRRRRRRRRRRRRRRRRR");
        ds.addNotificationsData(source.getName(num), tit, msg);
        ds.close();


        //Toast.makeText(context, tit + msg + num, Toast.LENGTH_LONG).show();

        /*Notification notification = new Notification(R.drawable.app_icon_notificaton, source.getName(num) + "\n"
                + tit + "\n" + msg, System.currentTimeMillis());
        notification.setLatestEventInfo(context, tit, msg, null);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(1, notification);*/

        Intent newIntent = new Intent(context, DismissReceiver.class);
        newIntent.setAction("LAUNCH");


        PendingIntent pintentNotification = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissNotification = new Intent(context, DismissReceiver.class);
        dismissNotification.setAction("DISMISS");
        PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, 0, dismissNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent callIntent = new Intent(context, DismissReceiver.class);
        //callIntent.setData(Uri.parse("tel:" + num));
        callIntent.putExtra("Number", num);
        callIntent.setAction("CALL");
        PendingIntent piCall = PendingIntent.getBroadcast(context, 0, callIntent, 0);


        Notification.Builder noti = new Notification.Builder(context)
                .setContentTitle(source.getName(num))
                .setContentText(tit)
                .setSubText(msg)
                .setSmallIcon(R.drawable.app_icon_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.app_icon_notification))
                .setVibrate(new long[]{100, 500, 300, 500})
                .setStyle(new Notification.BigTextStyle()
                        .setBigContentTitle(tit)
                        .bigText(msg)
                        .setSummaryText(source.getName(num)))
                .addAction(R.mipmap.telephone,
                        "Call", piCall)
                .setAutoCancel(true)
                .setDeleteIntent(pendingDismiss)
                ;
        noti.setContentIntent(pintentNotification);
        nm.notify(1, noti.build());
        source.close();

        if (tit.equals("pedometerGoal")){
            SharedPreferences.Editor editor = context.getSharedPreferences("fi.oulu.acp.communityreminder", Context.MODE_PRIVATE).edit();
            editor.putInt("yourGoal", Integer.parseInt(msg));
            editor.commit();
        }
    }

    private void writeToDatabase(Context context){

    }

    public static class DismissReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("DISMISS")){
                FlurryAgent.endTimedEvent("Notification");
                //Toast.makeText(context, "Dismissed", Toast.LENGTH_SHORT).show();
            }

            if (intent.getAction().equals("CALL")){
                String num = intent.getExtras().getString("Number", "");
                FlurryAgent.endTimedEvent("Notification");
                //Toast.makeText(context, "CALL", Toast.LENGTH_SHORT).show();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + num));
                context.startActivity(callIntent);
            }

            if (intent.getAction().equals("LAUNCH")){
                FlurryAgent.endTimedEvent("Notification");
                //Toast.makeText(context, "onClick", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(context, NotificationActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }

    }
}
