package fi.oulu.acp.communityreminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class NotificationAdapter extends ArrayAdapter<Notification>{
    private NotificationActivity context;
    private int resourceId;

    public NotificationAdapter (Context context, int resourceId, ArrayList<Notification> items){
        super(context, resourceId, items);
        this.context = (NotificationActivity) context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        View row = convertView;
        NotifHolder holder;

        if (row == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(R.layout.notification_row, null);
        }

        Notification notification = getItem(position);

        if (notification != null){
            holder = new NotifHolder();
            TextView phoneNumber = (TextView) row.findViewById(R.id.txt_phoneNumber);
            TextView title = (TextView) row.findViewById(R.id.txt_title);
            TextView message = (TextView) row.findViewById(R.id.txt_message);

            if (phoneNumber != null){
                phoneNumber.setText(notification.getPhoneNumber());
            }

            if (title != null){
                title.setText(notification.getTitle());
            }

            if (message != null){
                message.setText(notification.getMessage());
            }
        }

        return row;
    }

    private AdapterView.OnItemClickListener notifClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    static class NotifHolder{
        TextView name;
        TextView title;
        TextView message;

    }
}
