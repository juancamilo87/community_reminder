package fi.oulu.acp.communityreminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by JuanCamilo on 4/5/2015.
 */
public class ContactActivity extends Activity {

    private String name;
    private String phone;
    private String birthday;
    private int stepGoals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        txtPhone.setText(phone);
        if(!birthday.equals(""))
        {
            txtBirthday.setText(birthday);
        }
        else
        {
            txtBirthday.setVisibility(View.GONE);
        }

        if(stepGoals !=0)
        {
            txtStepGoal.setText(stepGoals+"");
        }
        else
        {
            txtStepGoal.setVisibility(View.GONE);
        }



    }
}
