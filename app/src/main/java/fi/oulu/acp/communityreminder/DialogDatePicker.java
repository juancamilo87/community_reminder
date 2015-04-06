package fi.oulu.acp.communityreminder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by JuanCamilo on 4/6/2015.
 */
public class DialogDatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private SignupActivity context;
    private DatePickerDialog datePickerDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        int year = 0;
        int month = 0;
        int day = 0;
        Bundle bundle = this.getArguments();
        if(bundle!=null){
        String birthday = bundle.getString("birthday","0000-01-00");
        String[] parts = birthday.split("-");
        year = Integer.parseInt(parts[0]);
        month = Integer.parseInt(parts[1])-1;
        day = Integer.parseInt(parts[2]);}
        if(year == 0)
        {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        // Create a new instance of DatePickerDialog and return it
        return datePickerDialog;
    }

    public void setClass(SignupActivity sContext)
    {
        context = sContext;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        context.updateBirthday(year, month+1, day);
        // Do something with the date chosen by the user
    }

}