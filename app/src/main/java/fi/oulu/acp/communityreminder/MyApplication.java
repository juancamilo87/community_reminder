package fi.oulu.acp.communityreminder;

import android.app.Application;

import com.flurry.android.FlurryAgent;

/**
 * Created by JuanCamilo on 5/12/2015.
 */
public class MyApplication extends Application {

    public static String MY_FLURRY_APIKEY = "SSGQNYNR2KN49GNQ4P79";

    @Override
    public void onCreate() {
        super.onCreate();

        // configure Flurry
        FlurryAgent.setLogEnabled(true);

        // init Flurry
        FlurryAgent.init(this, MY_FLURRY_APIKEY);
    }
}