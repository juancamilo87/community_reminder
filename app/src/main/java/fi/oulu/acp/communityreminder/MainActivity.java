package fi.oulu.acp.communityreminder;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToEmergencyButton(View view)
    {
        Intent intent = new Intent(this, EmergencyButton.class);
        startActivity(intent);
    }

    public void goToTemperature(View view)
    {
        Intent intent = new Intent(this, TemperatureActivity.class);
        startActivity(intent);
    }

    public void goToSettingsActivity(View view){
        Intent intent = new Intent (this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goToPedometerActivity(View view){
        Intent intent = new Intent(this, PedometerActivity.class);
        startActivity(intent);
    }

    public void goToSignUp(View view){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    public void goToContactList(View view){
        Intent intent = new Intent(this, AddContactListActivity.class);
        startActivity(intent);
    }
}
