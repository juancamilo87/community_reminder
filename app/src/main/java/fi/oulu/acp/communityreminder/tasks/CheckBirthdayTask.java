package fi.oulu.acp.communityreminder.tasks;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import fi.oulu.acp.communityreminder.Contact;
import fi.oulu.acp.communityreminder.GcmBroadcastReceiver;
import fi.oulu.acp.communityreminder.R;
import fi.oulu.acp.communityreminder.db.ContactsDataSource;
import fi.oulu.acp.communityreminder.db.FriendsDataSource;
import fi.oulu.acp.communityreminder.db.NotificationsDataSource;
import fi.oulu.acp.communityreminder.services.ResetValues;

public class CheckBirthdayTask extends BroadcastReceiver{

    public static String BIRTHDAY_ALARM = "fi.oulu.acp.communityreminder.tasks.BIRTHDAY_ALARM";
    private Calendar calendar = Calendar.getInstance();
    private AlarmManager alarmManager;
    private ArrayList<Contact> friends;

    public void setAlarm(Context context){
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 9);
        long currTime = calendar.getTimeInMillis();
        long intendedTime = c.getTimeInMillis();
        if (intendedTime >= currTime){
            Intent intent = new Intent(context, CheckBirthdayTask.class);
            intent.setAction(BIRTHDAY_ALARM);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            //calendar.setTimeInMillis(System.currentTimeMillis());
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() ,AlarmManager.INTERVAL_DAY, pendingIntent);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 1);
        long intendedTime1 = cal.getTimeInMillis();
        if (intendedTime1 >= currTime){
            Intent service = new Intent(context, CheckBirthdayTask.class);
            service.setAction("ResetValues");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, service, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }
    }

    public void cancellAlarm(Context context){
        Intent intent = new Intent(context, CheckBirthdayTask.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BIRTHDAY_ALARM)){
            FriendsDataSource fds = new FriendsDataSource(context);
            fds.open();
            friends = new ArrayList<>();
            friends = fds.getAllFriends();
            fds.close();
            Log.e("+++", "BEFORE");
            if (friends.size() != 0){
                Log.e("+++", friends.toString());
                for (Contact contact : friends){
                    String birthday = contact.getBirthday();
                    String[] parts = birthday.split("-");
                    Log.e("+++", contact.getPhones().toString());
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    int year = Integer.parseInt(parts[0]);
                    Log.e("----", month + "///" + year );

                    if (year != 0 && day != 0 && month != 0){
                        Calendar friendBirth = Calendar.getInstance();
                        friendBirth.set(Calendar.YEAR, month - 1, day);

                        int friendDay = friendBirth.get(Calendar.DAY_OF_YEAR);
                        int currDay = calendar.get(Calendar.DAY_OF_YEAR);

                        Log.e("----", "FR "  + friendDay );
                        Log.e("----", "CR "  + currDay);

                        if (friendDay == currDay){
                            onNotify(context, contact.getPhones().get(0), "Birthday", "Today is " + contact.getName() + "'s birthday") ;
                        }

                        if (friendDay - currDay == 1){
                            onNotify(context, contact.getPhones().get(0), "Birthday", "Tomorrow is " + contact.getName() + "'s birthday");
                        }
                    }
                }
            }

        }
        if (intent.getAction().equals("ResetValues")){
            Intent service = new Intent(context, ResetValues.class);
            context.startService(service);
            Log.e("****", "RESET!!!");
        }

    }

    public static void onNotify(Context context, String num, String tit, String msg){
        SharedPreferences prefs = context.getSharedPreferences("fi.oulu.acp.communityreminder",Context.MODE_PRIVATE);

        int notificationId = prefs.getInt("notificationId",0)+1;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("notificationId", notificationId);
        editor.commit();

        Intent newIntent = new Intent(context, GcmBroadcastReceiver.DismissReceiver.class);
        newIntent.setAction("LAUNCH");
        newIntent.putExtra("notificationId",notificationId);

        PendingIntent pintentNotification = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissNotification = new Intent(context, GcmBroadcastReceiver.DismissReceiver.class);
        dismissNotification.setAction("DISMISS");
        dismissNotification.putExtra("notificationId",notificationId);
        PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, 0, dismissNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent callIntent = new Intent(context, GcmBroadcastReceiver.DismissReceiver.class);
        //callIntent.setData(Uri.parse("tel:" + num));
        callIntent.putExtra("Number", num);
        callIntent.putExtra("notificationId",notificationId);
        callIntent.setAction("CALL");
        PendingIntent piCall = PendingIntent.getBroadcast(context, 0, callIntent, 0);

        ContactsDataSource source = new ContactsDataSource(context);
        source.open();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

        NotificationsDataSource ds = new NotificationsDataSource(context);
        ds.open();
        ds.addNotificationsData(source.getName(num), tit, msg);
        ds.close();
        source.close();
    }

    public static class AutoStart extends BroadcastReceiver{
        CheckBirthdayTask task = new CheckBirthdayTask();
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                task.setAlarm(context);
            }
        }
    }

}
