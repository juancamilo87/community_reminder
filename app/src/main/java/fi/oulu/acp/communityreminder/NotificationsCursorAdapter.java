package fi.oulu.acp.communityreminder;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import fi.oulu.acp.communityreminder.db.MySQLiteHelper;

/**
 * Created by JuanCamilo on 4/6/2015.
 */
public class NotificationsCursorAdapter extends ResourceCursorAdapter {

    public NotificationsCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.txt_phoneNumber);
        name.setText(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NOTIFICATION_PHONE)));

        TextView phone = (TextView) view.findViewById(R.id.txt_title);
        phone.setText(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NOTIFICATION_TITLE)));

        TextView message= (TextView) view.findViewById(R.id.txt_message);
        message.setText(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NOTIFICATION_MESSAGE)));
    }
}
