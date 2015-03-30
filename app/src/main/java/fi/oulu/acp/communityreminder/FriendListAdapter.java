package fi.oulu.acp.communityreminder;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class FriendListAdapter extends ArrayAdapter<Contact> {

    public FriendListAdapter(Context context, int resource, ArrayList<Contact> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

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
            else
            {
                TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
                sectionView.setText(p.getName());
            }

        }

        return v;

    }

}
