package fi.oulu.acp.communityreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service {
    private NotificationManager nm;


    @Override
    public void onCreate(){
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotif(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotif(Intent intent){
        int state = intent.getIntExtra("change", 0);
        if (state == 1) {
            Notification notif = new Notification(R.drawable.abc_ab_share_pack_holo_light, "INSIDE",
                    System.currentTimeMillis());
            notif.setLatestEventInfo(this, "Home Status", "Inside", null);
            notif.flags |= Notification.FLAG_AUTO_CANCEL;
            nm.notify(1, notif);
            stopSelf();
        }
        else if (state == 0){
            Notification notif = new Notification(R.drawable.abc_ab_share_pack_holo_light, "OUTSiDE",
                    System.currentTimeMillis());
            notif.setLatestEventInfo(this, "Home Status", "Outside", null);
            notif.flags |= Notification.FLAG_AUTO_CANCEL;
            nm.notify(1, notif);
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
