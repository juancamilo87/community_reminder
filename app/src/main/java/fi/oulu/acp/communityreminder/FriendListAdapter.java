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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import fi.oulu.acp.communityreminder.tasks.VerifyFriendTask;


/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class FriendListAdapter extends ArrayAdapter<Contact> {

    private ContactListActivity sContext;
    public FriendListAdapter(Context context, int resource, ArrayList<Contact> items) {
        super(context, resource, items);
        sContext = (ContactListActivity) context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;



        Contact p = getItem(position);

        if (p != null) {

            if (v == null) {

                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                if(p.getStatus()==3)
                    v = vi.inflate(R.layout.contact_category, null);
                else if(p.getStatus()==2)
                    v = vi.inflate(R.layout.contact_row_add, null);
                else
                    v = vi.inflate(R.layout.contact_row, null);

            }
            if(p.getStatus()!=3)
            {
                ImageView imageView = (ImageView) v.findViewById(R.id.contact_photo);
                TextView nameText = (TextView) v.findViewById(R.id.contact_name);
                TextView phoneText = (TextView) v.findViewById(R.id.contact_phone);
                final String friend_id = p.getPhones().get(0);
                if (nameText != null) {
                    nameText.setText(p.getName());
                }
                if (phoneText != null) {

                    phoneText.setText(friend_id);
                }
                if (imageView != null) {
                    Bitmap photo = p.getPicture();
                    if(photo!=null)
                        imageView.setImageBitmap(p.getPicture());
                    else
                        imageView.setImageBitmap(null);
                }
                if(p.getStatus()==2)
                {
                    final ImageButton acceptButton = (ImageButton) v.findViewById(R.id.add_contact_btn);
                    final ProgressBar add_progress = (ProgressBar) v.findViewById(R.id.progress_add_contact);
                    if(acceptButton!=null)
                    {
                        acceptButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
                                String user_id = prefs.getString("phoneNumber","none");
                                (new VerifyFriendTask()).execute(sContext, user_id, friend_id);
                                acceptButton.setVisibility(View.GONE);
                                add_progress.setVisibility(View.VISIBLE);
                            }
                        });

                    }
                }

            }
            else
            {
                TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
                if(sectionView!=null)
                    sectionView.setText(p.getName());
            }

        }

        return v;

    }

}
