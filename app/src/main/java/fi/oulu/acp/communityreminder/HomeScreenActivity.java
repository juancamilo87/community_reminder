package fi.oulu.acp.communityreminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by JuanCamilo on 3/29/2015.
 */
public class HomeScreenActivity extends Activity {

    private Context context;
    private ImageButton btnEmergency;
    private int emergencyTaps;
    private Toast toast;
    private ImageButton btnProblem;
    private int problemTaps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        context = this;
        toast = Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT);
        //Contacts
        ImageButton contactsBtn = (ImageButton) findViewById(R.id.ContactsButton);
        contactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ContactListActivity.class);
                startActivity(intent);
            }
        });

        //Emergency buttons
        //First
        initializeEmergencyButton();
        initializeProblemButton();

        //Second

    }

    private void initializeProblemButton() {
        problemTaps = 0;
        final Handler handler = new Handler();

        final Runnable restartTapCount = new Runnable() {
            @Override
            public void run() {
                problemTaps = 0;
                Log.d("DEBUG", "problemTap resetted");
            }
        };

        final int millisecondsPerTap = (getResources().getInteger(R.integer.seconds_for_emergency)*1000)/getResources().getInteger(R.integer.taps_for_emergency);
        Log.d("DEBUG", "time between: "+millisecondsPerTap);
        final int tapsForEmergency = getResources().getInteger(R.integer.taps_for_emergency);


        btnProblem = (ImageButton) findViewById(R.id.LifeProblemButton);

        btnProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(restartTapCount);
                problemTaps++;
                if (problemTaps == tapsForEmergency) {
                    toast.cancel();
                    Toast.makeText(getApplicationContext(), "Life problem alert sent!", Toast.LENGTH_SHORT).show();
                    problemTaps = 0;
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    vibrator.vibrate(600);

                } else {
                    toast.setText("Tap " + (tapsForEmergency - problemTaps) + " more times to send life problem alert.");
                    toast.show();
                    handler.postDelayed(restartTapCount, millisecondsPerTap);
                }
            }
        });

        btnProblem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast.setText("Tap " + tapsForEmergency + " times to send a life problem alert!");
                toast.show();
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                vibrator.vibrate(300);
                return true;
            }
        });
    }

    private void initializeEmergencyButton() {
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


        btnEmergency = (ImageButton) findViewById(R.id.HealthEmergencyButton);

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
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                vibrator.vibrate(300);
                return true;
            }
        });
    }
}
