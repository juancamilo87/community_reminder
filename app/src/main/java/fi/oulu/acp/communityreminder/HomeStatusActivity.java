package fi.oulu.acp.communityreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class HomeStatusActivity extends ActionBarActivity {

    private WifiManager wifiManager;
    private WifiScanReceiver scanReceiver;
    private ListView lstWifi;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_status);

        //Setup ListView and its Adapter to display WiFi discoveries
        lstWifi = (ListView) findViewById(R.id.lstWifi);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lstWifi.setAdapter(adapter);
        lstWifi.setOnItemClickListener(deviceClickListener);

        //Perform discovery
        doDiscovery();
    }

    private void doDiscovery(){
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        scanReceiver = new WifiScanReceiver();
        registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class WifiScanReceiver extends BroadcastReceiver {
        public WifiScanReceiver(){
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                List<ScanResult> wifiScanList = wifiManager.getScanResults();
                for(int i = 0; i < wifiScanList.size(); i++){
                    adapter.add(wifiScanList.get(i).SSID + "\n" + wifiScanList.get(i).BSSID);
                }
            }
        }
    }

    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            //Get the device MAC address which is 17 last characters in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            //Save MAC to preferences
            if (!address.equals("")){
                SharedPreferences.Editor editor = getSharedPreferences("fi.oulu.acp.communityreminder", MODE_PRIVATE).edit();
                editor.putString("MAC", address);
                editor.commit();
                Toast.makeText(getApplicationContext(), "Your Home WiFi Has Been Chosen", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(scanReceiver);
    }
}
