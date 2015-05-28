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
import android.widget.RemoteViews;

import com.flurry.android.FlurryAgent;

import java.util.Calendar;
import java.util.GregorianCalendar;
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
        long startTime = GregorianCalendar.getInstance().getTimeInMillis();
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
        SharedPreferences prefs = context.getSharedPreferences("fi.oulu.acp.communityreminder",Context.MODE_PRIVATE);

        int notificationId = prefs.getInt("notificationId",0)+1;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("notificationId", notificationId);
        editor.commit();
        Intent newIntent = new Intent(context, DismissReceiver.class);
        newIntent.setAction("LAUNCH");
        newIntent.putExtra("startTime", startTime);
        newIntent.putExtra("notificationId",notificationId);

        PendingIntent pintentNotification = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissNotification = new Intent(context, DismissReceiver.class);
        dismissNotification.setAction("DISMISS");
        dismissNotification.putExtra("startTime", startTime);
        dismissNotification.putExtra("notificationId",notificationId);
        PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, 0, dismissNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent callIntent = new Intent(context, DismissReceiver.class);
        //callIntent.setData(Uri.parse("tel:" + num));
        callIntent.putExtra("startTime",startTime);
        callIntent.putExtra("Number", num);
        callIntent.putExtra("notificationId",notificationId);
        callIntent.setAction("CALL");
        PendingIntent piCall = PendingIntent.getBroadcast(context, 0, callIntent, 0);

        if(tit.equals("Temperatures"))
        {
            String time_zero = extras.getString("Time_zero", "");
            String time_five = extras.getString("Time_five", "");
            String time_ten = extras.getString("Time_ten", "");
            String time_fifteen = extras.getString("Time_fifteen", "");
            String time_twenty = extras.getString("Time_twenty", "");
            String time_other = extras.getString("Time_other", "");
            RemoteViews notiViewNot = new RemoteViews(context.getPackageName(),R.layout.temperature_notification);
            notiViewNot.setImageViewResource(R.id.temp_noti_not_image, R.drawable.app_icon_notification);
            Calendar calendar = new GregorianCalendar();
            String time;
            if(calendar.get(Calendar.HOUR_OF_DAY)<10)
            {
                time = "0"+calendar.get(Calendar.HOUR_OF_DAY)+":";
            }
            else
            {
                time = calendar.get(Calendar.HOUR_OF_DAY)+":";
            }
            if(calendar.get(Calendar.MINUTE)<10)
            {
                time = time+"0"+calendar.get(Calendar.MINUTE);
            }
            else
            {
                time = time+calendar.get(Calendar.MINUTE);
            }
            notiViewNot.setTextViewText(R.id.temp_noti_not_time, time);
            notiViewNot.setTextViewText(R.id.temp_noti_not_title, source.getName(num));
            notiViewNot.setTextViewText(R.id.temp_noti_not_message, tit);
            notiViewNot.setTextViewText(R.id.temp_noti_not_sender, msg);
            RemoteViews notiView = new RemoteViews(context.getPackageName(),R.layout.temperature_notification_expanded);
            notiView.setImageViewResource(R.id.temp_noti_image, R.drawable.app_icon_notification);
            notiView.setImageViewResource(R.id.temp_noti_image_call, R.mipmap.telephone);
            notiView.setTextViewText(R.id.temp_noti_time, time);
            notiView.setTextViewText(R.id.temp_noti_title, tit);
            notiView.setTextViewText(R.id.temp_noti_message, msg);
            notiView.setTextViewText(R.id.temp_zero_right, time_zero + " minutes");
            notiView.setTextViewText(R.id.temp_five_right, time_five + " minutes");
            notiView.setTextViewText(R.id.temp_ten_right, time_ten + " minutes");
            notiView.setTextViewText(R.id.temp_fifteen_right, time_fifteen + " minutes");
            notiView.setTextViewText(R.id.temp_twenty_right, time_twenty + " minutes");
            notiView.setTextViewText(R.id.temp_other_right, time_other + " minutes");
            notiView.setOnClickPendingIntent(R.id.temp_noti_linear, piCall);
            notiView.setTextViewText(R.id.temp_noti_name,source.getName(num));
            android.app.Notification.Builder noti = new android.app.Notification.Builder(context)
                    .setContent(notiViewNot)
                    .setSmallIcon(R.drawable.app_icon_notification)
                    .setVibrate(new long[]{100, 500, 300, 500})
                    .setAutoCancel(true)
                    .setDeleteIntent(pendingDismiss)
                    ;
            noti.setContentIntent(pintentNotification);
            Notification notif = noti.build();
            notif.bigContentView = notiView;
            nm.notify(notificationId, notif);
        }
        else
        {
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
            nm.notify(notificationId, noti.build());
        }

        source.close();

        if (tit.equals("pedometerGoal")){
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
                long totalTime = GregorianCalendar.getInstance().getTimeInMillis() - intent.getLongExtra("startTime",GregorianCalendar.getInstance().getTimeInMillis());
                HashMap<String, String> eventParams = new HashMap<>();
                eventParams.put("Duration", totalTime+" ms");
                eventParams.put("Response","Dismiss");
                FlurryAgent.endTimedEvent("Notification",eventParams);
                //Toast.makeText(context, "Dismissed", Toast.LENGTH_SHORT).show();
            }

            if (intent.getAction().equals("CALL")){
                long totalTime = GregorianCalendar.getInstance().getTimeInMillis() - intent.getLongExtra("startTime",GregorianCalendar.getInstance().getTimeInMillis());
                HashMap<String, String> eventParams = new HashMap<>();
                eventParams.put("Duration", totalTime+" ms");
                eventParams.put("Response","Call");
                FlurryAgent.endTimedEvent("Notification",eventParams);
                String num = intent.getExtras().getString("Number", "");
                //Toast.makeText(context, "CALL", Toast.LENGTH_SHORT).show();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + num));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(callIntent);

                SharedPreferences prefs = context.getSharedPreferences("fi.oulu.acp.communityreminder",Context.MODE_PRIVATE);

                int notificationId = prefs.getInt("notificationId",0);

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(notificationId);
            }

            if (intent.getAction().equals("LAUNCH")){
                long totalTime = GregorianCalendar.getInstance().getTimeInMillis() - intent.getLongExtra("startTime",GregorianCalendar.getInstance().getTimeInMillis());
                HashMap<String, String> eventParams = new HashMap<>();
                eventParams.put("Duration", totalTime + " ms");
                eventParams.put("Response","Launch");
                FlurryAgent.endTimedEvent("Notification",eventParams);
                //Toast.makeText(context, "onClick", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(context, NotificationActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }

    }
}
