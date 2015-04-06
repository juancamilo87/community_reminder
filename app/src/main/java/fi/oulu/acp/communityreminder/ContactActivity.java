package fi.oulu.acp.communityreminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import fi.oulu.acp.communityreminder.db.NotificationsDataSource;
import fi.oulu.acp.communityreminder.tasks.ChangeTemperatureTimesTask;

/**
 * Created by JuanCamilo on 4/5/2015.
 */
public class ContactActivity extends FragmentActivity {

    private String name;
    private String phone;
    private String birthday;
    private int stepGoals;
    private Context context;
    private NotificationsDataSource ds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_contactdetails);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        name = extras.getString("name");
        phone = extras.getString("phone");
        birthday = extras.getString("birthday","");
        stepGoals = extras.getInt("stepGoal",0);

        TextView txtName = (TextView) findViewById(R.id.GetContactName);
        TextView txtPhone = (TextView) findViewById(R.id.GetPhoneNumber);
        TextView txtBirthday = (TextView) findViewById(R.id.GetBirthday);
        TextView txtStepGoal = (TextView) findViewById(R.id.GetStepsGoal);

        txtName.setText(name);
        txtPhone.setPaintFlags(txtPhone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtPhone.setText(phone);
        if(!birthday.equals("")&&!birthday.equals("null"))
        {
            txtBirthday.setText(birthday);
        }
        else
        {
            ((LinearLayout) findViewById(R.id.ll_birth)).setVisibility(View.GONE);
        }

        if(stepGoals !=0)
        {
            txtStepGoal.setText(stepGoals + " steps");
        }
        else
        {
            ((LinearLayout) findViewById(R.id.ll_steps)).setVisibility(View.GONE);
        }

        LinearLayout callBtn = (LinearLayout) findViewById(R.id.ll_call);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phone));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException e) {
                    Log.e("Call", "Call failed", e);
                    Toast.makeText(context, "Error making the call", Toast.LENGTH_SHORT).show();
                }
            }
        });


        ListView notifications = (ListView) findViewById(R.id.GetOnesNotifications);

        ds = new NotificationsDataSource(this);
        ds.open();
        Cursor cursor = ds.getNotificationsFromContact(phone);

        NotificationsCursorAdapter adapter = new NotificationsCursorAdapter(this, R.layout.notification_row, cursor, 0);
        notifications.setAdapter(adapter);
        notifications.setEmptyView(findViewById(R.id.no_notifications_txt));

        Button setTemp = (Button) findViewById(R.id.btn_temp_limits);
        setTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTempDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        ds.close();
        super.onDestroy();
    }

    public void showTempDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new TemperatureDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("phone",phone);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "TemperatureDialogFragment");
    }


    public static class TemperatureDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.dialog_set_temperature, null))

                    .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Bundle bundle = getArguments();
                            String[] times= null;
                            new ChangeTemperatureTimesTask().execute(bundle.getString("phone"),getActivity(), times);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
