package demo.lite.couchbase.com.officeradar;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Application Singleton
 */
public class Application extends android.app.Application {

    public static final String TAG = "OfficeRadar";
    private static final String DATABASE_NAME = "officeradar";
    private static final String SYNC_URL = "http://demo.mobile.couchbase.com/officeradar/";

    private Manager manager;
    private Database database;
    private Replication pullReplication;
    private Replication pushReplication;

    @Override
    public void onCreate() {

        super.onCreate();

        Log.i(Application.TAG, "Application State: onCreate()");

        initDatabase();
        startReplications();

    }

    private void initDatabase() {

        Log.i(Application.TAG, "Initialize Couchbase Lite");

        try {

            Manager.enableLogging(Log.TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);

            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);

        } catch (IOException e) {
            Log.e(TAG, "Cannot create Manager object", e);
            return;
        }

        try {
            database = manager.getDatabase(DATABASE_NAME);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get Database", e);
            return;
        }
    }

    public void startReplications() {

        Log.i(Application.TAG, "Start Couchbase Lite replications");

        URL syncUrl;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid Sync Url", e);
            throw new RuntimeException(e);
        }

        pullReplication = database.createPullReplication(syncUrl);
        pushReplication = database.createPushReplication(syncUrl);

        pullReplication.setContinuous(true);
        pushReplication.setContinuous(true);

        pullReplication.start();
        pushReplication.start();

    }



}
