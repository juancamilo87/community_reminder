package fi.oulu.acp.communityreminder.tasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;

import fi.oulu.acp.communityreminder.Contact;
import fi.oulu.acp.communityreminder.ServerUtilities;
import fi.oulu.acp.communityreminder.db.FriendsDataSource;

public class CheckBirthdayTask extends BroadcastReceiver{

    private Calendar calendar = Calendar.getInstance();
    private AlarmManager alarmManager;

    public void setAlarm(Context context){
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CheckBirthdayTask.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 30);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() ,AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    public void cancellAlarm(Context context){
        Intent intent = new Intent(context, CheckBirthdayTask.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FriendsDataSource fds = new FriendsDataSource(context);
        fds.open();
        ArrayList<Contact> friends = new ArrayList<>();
        friends = fds.getAllFriends();
        if (friends.size() != 0){
            for (Contact contact : friends){
                String birthday = contact.getBirthday();
                String[] parts = birthday.split("-");

                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
              
                int currMonth = calendar.get(Calendar.MONTH);
                int currDay = calendar.get(Calendar.DAY_OF_MONTH);

                if (month == currMonth && day == currDay){
                    ServerUtilities.sendMessage(contact.getPhones().get(0), "Birthday", "Today is " + contact.getName() + "'s birthday");
                }

                if (month == currMonth && day - currDay == 1){
                    ServerUtilities.sendMessage(contact.getPhones().get(0), "Birthday", "Tomorrow is " + contact.getName() + "'s birthday");
                }
            }
        }
        fds.close();
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
