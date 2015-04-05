package fi.oulu.acp.communityreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fi.oulu.acp.communityreminder.tasks.AddFriendTask;


/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class ContactListAdapter extends ArrayAdapter<Contact> {

    private Context sContext;

    public ContactListAdapter(Context context, int resource, ArrayList<Contact> items) {
        super(context, resource, items);
        sContext = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.contact_row_add, null);

        }

        Contact p = getItem(position);

        if (p != null) {

            final String friend_id = p.getPhones().get(0);
            ImageView imageView = (ImageView) v.findViewById(R.id.contact_photo);
            TextView nameText = (TextView) v.findViewById(R.id.contact_name);
            TextView phoneText = (TextView) v.findViewById(R.id.contact_phone);
            ImageButton addBtn = (ImageButton) v.findViewById(R.id.add_contact_btn);
            if(addBtn != null)
            {
                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
                        String user_id = prefs.getString("phoneNumber","none");
                        new AddFriendTask().execute(sContext, user_id, friend_id);
                        remove(getItem(position));
                    }
                });
            }


            if (nameText != null) {
                nameText.setText(p.getName());
            }
            if (phoneText != null) {
                phoneText.setText(p.getPhones().get(0));
            }
            if (imageView != null) {
                Bitmap photo = p.getPicture();
                if(photo!=null)
                    imageView.setImageBitmap(p.getPicture());
                else
                    imageView.setImageBitmap(null);
            }

        }

        return v;

    }

}
