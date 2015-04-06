package fi.oulu.acp.communityreminder;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;


public class PedometerActivity extends ActionBarActivity implements View.OnClickListener{
    //private TextView stepValues;
    private int steps;
    private int stepsGoal;
    private StepService stepService;
    private static final int STEP_MSG = 1;
    private boolean isRunning;
    private Button btnYourGoal;
    private Button btnFamilyGoal;
    private ProgressBar stepsBar;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer_screen);

        btnYourGoal = (Button) findViewById(R.id.resetgoal);
        btnFamilyGoal = (Button) findViewById(R.id.setgoalforfamily);
        btnYourGoal.setOnClickListener(this);
        btnFamilyGoal.setOnClickListener(this);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("fi.oulu.acp.communityreminder", Context.MODE_PRIVATE);
        stepsGoal = prefs.getInt("yourGoal", 0);

        stepsBar = (ProgressBar) findViewById(R.id.progress_id);
        stepsBar.setProgress(0);
        stepsBar.setMax(stepsGoal);

        steps = 0;
        //stepValues = (TextView) findViewById(R.id.step_value);
        startStepService();
        bindStepService();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();

        /*if (!isRunning){
            startStepService();
            bindStepService();
        }
        else if (isRunning){
            bindStepService();
        }*/
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pedometer, menu);
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

    private ServiceConnection sConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            stepService = ((StepService.StepBinder)service).getService();
            stepService.registerCallBack(callBack);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stepService = null;
        }
    };

    private void startStepService(){
        startService(new Intent(PedometerActivity.this, StepService.class));
    }

    private void bindStepService(){
        bindService(new Intent(PedometerActivity.this,
                StepService.class), sConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindStepService(){
        unbindService(sConnection);
    }

    private void stopService(){
        if (stepService != null){
            stopService(new Intent(PedometerActivity.this,
                    StepService.class));
        }
        isRunning = false;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopService();
        unbindStepService();
    }

    private StepService.ICallBack callBack = new StepService.ICallBack() {
        @Override
        public void stepChanged(int value) {
            handler.sendMessage(handler.obtainMessage(STEP_MSG, value, 0));
        }
    };

    private Handler handler = new Handler() {
        public void handleMessage(Message msg){
            switch (msg.what){
                case STEP_MSG:
                    steps = msg.arg1;
                    stepsBar.setProgress(steps);
                    //stepValues.setText("" + steps);
                    break;
                default: super.handleMessage(msg);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.resetgoal:
                //isRunning = false;
                final Dialog dialog = new Dialog(PedometerActivity.this);
                dialog.setContentView(R.layout.dialog_yourgoal);
                dialog.setTitle("Set your goal");
                Button btnOk = (Button) dialog.findViewById(R.id.btn_goal_ok);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText goal = (EditText) dialog.findViewById(R.id.text_goal);
                        if (goal.getText() != null){
                            SharedPreferences.Editor editor = getSharedPreferences("fi.oulu.acp.communityreminder", MODE_PRIVATE).edit();
                            Log.e("++++++++++++++", goal.getText().toString());
                            editor.putInt("yourGoal", Integer.parseInt(goal.getText().toString()));
                            editor.commit();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
                Button btnCancel = (Button) dialog.findViewById(R.id.btn_goal_cancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case R.id.setgoalforfamily:
                //isRunning = false;
                final Dialog dialog1 = new Dialog(PedometerActivity.this);
                dialog1.setContentView(R.layout.dialog_yourgoal);
                dialog1.setTitle("Set your for your family");
                Button btnOK = (Button) dialog1.findViewById(R.id.btn_goal_ok);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText goalRemote = (EditText) dialog1.findViewById(R.id.text_goal);
                        String g = goalRemote.getText().toString();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String phoneNumber = prefs.getString("phoneNumber", "");
                        ServerUtilities.sendMessage(phoneNumber, "pedometerGoal", g);
                        dialog1.dismiss();
                    }
                });
                dialog1.show();
                Button btnCANCEL = (Button) dialog1.findViewById(R.id.btn_goal_cancel);
                btnCANCEL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog1.dismiss();
                    }
                });
                break;
        }
    }
}
