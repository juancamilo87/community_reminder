package fi.oulu.acp.communityreminder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class PedometerActivity extends ActionBarActivity {
    private TextView stepValues;
    private int steps;
    private StepService stepService;
    private static final int STEP_MSG = 1;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);
        steps = 0;
        stepValues = (TextView) findViewById(R.id.step_value);
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
                    stepValues.setText("" + steps);
                    break;
                default: super.handleMessage(msg);
            }
        }
    };
}
