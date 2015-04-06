package fi.oulu.acp.communityreminder.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by JuanCamilo on 4/6/2015.
 */
public class ResetValues extends IntentService {


    public ResetValues(){
        super(ResetValues.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("steps",0);
        editor.apply();
    }
}
