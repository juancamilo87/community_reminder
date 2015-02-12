package fi.oulu.acp.communityreminder;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class EmergencyButton extends ActionBarActivity {

    private Button btnEmergency;
    private int emergencyTaps;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_button);
        emergencyTaps = 0;

        final Handler handler = new Handler();

        final Runnable restartTapCount = new Runnable() {
            @Override
            public void run() {
                emergencyTaps = 0;
                Log.d("DEBUG", "emergencyTap resetted");
            }
        };

        final int millisecondsPerTap = (getResources().getInteger(R.integer.seconds_for_emergency)*1000)/getResources().getInteger(R.integer.taps_for_emergency);
        Log.d("DEBUG", "time between: "+millisecondsPerTap);
        final int tapsForEmergency = getResources().getInteger(R.integer.taps_for_emergency);


        btnEmergency = (Button) findViewById(R.id.btn_emg_but);
        toast = Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT);
        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(restartTapCount);
                emergencyTaps++;
                if(emergencyTaps==tapsForEmergency)
                {
                    toast.cancel();
                    Toast.makeText(getApplicationContext(),"Emergency alert sent!",Toast.LENGTH_SHORT).show();
                    emergencyTaps = 0;
                    Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

                    vibrator.vibrate(600);

                }
                else
                {
                    toast.setText("Tap " + (tapsForEmergency-emergencyTaps) + " more times to send an alert.");
                    toast.show();
                    handler.postDelayed(restartTapCount, millisecondsPerTap);
                }
            }
        });

        btnEmergency.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast.setText("Tap " + tapsForEmergency + " times to send an emergency alert!");
                toast.show();
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

                vibrator.vibrate(300);
                return true;
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_emergency_button, menu);
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
}
