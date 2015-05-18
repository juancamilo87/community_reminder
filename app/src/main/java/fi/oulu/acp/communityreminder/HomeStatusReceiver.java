package fi.oulu.acp.communityreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;


public class HomeStatusReceiver extends BroadcastReceiver{
    WifiManager wifiManager;
    private static boolean wasConnected;

    public HomeStatusReceiver(){

    }

    public void onReceive(Context context, Intent intent){
        //Get WifiManager and retrieve MAC from shared preferences
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        SharedPreferences sharedPreferences = context.getSharedPreferences("fi.oulu.acp.communityreminder", Context.MODE_PRIVATE);
        String addr = sharedPreferences.getString("MAC", "");

        //Check if we are connected and our home MAC matches the connected AP MAC
        //However some issues to be fixed (when choosing another home AP from settings does not give notification)
        Intent service = new Intent(context, NotificationService.class);
        service.setAction(HomeStatusActivity.HOME_STATUS_ACTION);

        if (wifiManager.isWifiEnabled() &&
                wifiManager.getConnectionInfo().getBSSID().equals(addr)&&!wasConnected){
            context.startService(service.putExtra("change", 1));
            wasConnected = true;
        }
        else if ((!wifiManager.isWifiEnabled() && wasConnected) ||
                (wifiManager.isWifiEnabled() && !wifiManager.getConnectionInfo().getBSSID().equals(addr) && wasConnected)){
            context.startService(service.putExtra("change", 0));
            wasConnected = false;
        }
    }
}
