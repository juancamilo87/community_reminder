package fi.oulu.acp.communityreminder;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class StepService extends Service implements StepListener{
    private StepDetector stepDetector;
    private int stepCount = 0;
    private Sensor sensor;
    private SensorManager sensorManager;
    private ICallBack callBack;
    private IBinder binder = new StepBinder();

    public StepService() {
    }

    public class StepBinder extends Binder {
        StepService getService(){
            return StepService.this;
        }
    }

    @Override
    public void onCreate(){
        Log.i("STEPservice", "[SERVICE] onCreate");
        super.onCreate();
        stepDetector = new StepDetector();
        stepDetector.addStepListener(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return binder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterDetector();
    }

    private void registerDetector(){
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterDetector(){
        sensorManager.unregisterListener(stepDetector);
    }

    public interface ICallBack{
        public void stepChanged(int value);
    }

    public void registerCallBack(ICallBack cb){
        callBack = cb;
    }

    @Override
    public void onStep(){
        stepCount++;
        callBack.stepChanged(stepCount);
        //notifyListener();
    }
}