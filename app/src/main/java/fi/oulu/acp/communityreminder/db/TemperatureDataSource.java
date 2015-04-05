package fi.oulu.acp.communityreminder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JuanCamilo on 3/19/2015.
 */
public class TemperatureDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_TEMP_ID,
            MySQLiteHelper.COLUMN_TEMP_SESSION, MySQLiteHelper.COLUMN_TEMP_PERFORMANCE,
            MySQLiteHelper.COLUMN_TEMP_ENV_TEMP, MySQLiteHelper.COLUMN_TEMP_TEMP};

    public TemperatureDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public int addTempData(int session, String performance, String envTemp, float temp) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TEMP_SESSION, session);
        values.put(MySQLiteHelper.COLUMN_TEMP_PERFORMANCE, performance);
        values.put(MySQLiteHelper.COLUMN_TEMP_ENV_TEMP, envTemp);
        values.put(MySQLiteHelper.COLUMN_TEMP_TEMP, temp);
        long insertId = database.insert(MySQLiteHelper.TABLE_TEMP_DATA, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TEMP_DATA,
                allColumns, MySQLiteHelper.COLUMN_TEMP_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(0);

    }

}
