package fi.oulu.acp.communityreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import fi.oulu.acp.communityreminder.ServerUtilities;

/**
 * Created by JuanCamilo on 4/6/2015.
 */
public class BatteryLevelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("+++++","Low battery");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String phone = prefs.getString("phoneNumber","");
            ServerUtilities.sendMessage(phone,"Low Battery","The battery of my smartphone is running out soon!");
    }
}