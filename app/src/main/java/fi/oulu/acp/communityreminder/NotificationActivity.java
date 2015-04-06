package fi.oulu.acp.communityreminder;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import fi.oulu.acp.communityreminder.db.MySQLiteHelper;
import fi.oulu.acp.communityreminder.db.NotificationsDataSource;


public class NotificationActivity extends ActionBarActivity {
    private ListView lstViewNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_screen);
        lstViewNotif = (ListView) findViewById(R.id.lstViewNotif);
        NotificationAdapter adapter = new NotificationAdapter(this, R.layout.notification_row, getNotifications());
        lstViewNotif.setAdapter(adapter);
    }

    private ArrayList<Notification> getNotifications(){
        ArrayList<Notification> list = new ArrayList<>();
        NotificationsDataSource ds = new NotificationsDataSource(getApplicationContext());
        ds.open();
        Cursor cursor = ds.getAllNotificationsCursor();
        if (cursor != null){
            try{
                while (cursor.moveToNext()){
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NOTIFICATION_PHONE));
                    String title = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NOTIFICATION_TITLE));
                    String message = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NOTIFICATION_MESSAGE));
                    Notification notification = new Notification(phoneNumber, title, message);
                    list.add(notification);
                }

            } finally {
                cursor.close();
                ds.close();
            }
        }

        return list;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification, menu);
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
