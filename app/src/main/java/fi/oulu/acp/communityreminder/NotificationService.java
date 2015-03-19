package fi.oulu.acp.communityreminder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class NotificationService extends IntentService {
    private NotificationManager nm;
    public static final int NOTIFICATION_ID = 1;
    public static final String TAG = "Notification Service";

    public NotificationService(){
        super("NotofocationService");
    }
    @Override
    public void onCreate(){
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i("++++", "[SERVICE] onStart");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotif(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()){
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
                sendNotif(intent);
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)){
                Toast.makeText(getApplicationContext(), "Send Error", Toast.LENGTH_SHORT).show();
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)){
                Toast.makeText(getApplicationContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    private void sendNotif(Intent intent){
        if (intent.getAction().equals(HomeStatusActivity.HOME_STATUS_ACTION)){
            int state = intent.getIntExtra("change", 0);
            if (state == 1) {
                Notification notif = new Notification(R.drawable.abc_ab_share_pack_holo_light, "INSIDE",
                        System.currentTimeMillis());
                notif.setLatestEventInfo(this, "Home Status", "Inside", null);
                notif.flags |= Notification.FLAG_AUTO_CANCEL;
                nm.notify(1, notif);
            }
            else if (state == 0){
                Notification notif = new Notification(R.drawable.abc_ab_share_pack_holo_light, "OUTSIDE",
                        System.currentTimeMillis());
                notif.setLatestEventInfo(this, "Home Status", "Outside", null);
                notif.flags |= Notification.FLAG_AUTO_CANCEL;
                nm.notify(1, notif);
            }
        }
        stopSelf();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
