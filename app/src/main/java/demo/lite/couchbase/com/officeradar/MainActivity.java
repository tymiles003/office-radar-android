package demo.lite.couchbase.com.officeradar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import demo.lite.couchbase.com.officeradar.document.GeofenceEvent;


public class MainActivity extends Activity {

    private LastSeenUsersListAdapter lastSeenUsersListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMainListView();

    }

    private void initMainListView() {

        Query query = getLastSeenUsers();

        try {
            QueryEnumerator enumerator = query.run();
            while (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                Log.d(Application.TAG, "row: %s", row);
            }


        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }


        lastSeenUsersListAdapter = new LastSeenUsersListAdapter(this, query.toLiveQuery());

        ListView listView = (ListView) findViewById(R.id.mainListView);

        listView.setAdapter(lastSeenUsersListAdapter);
    }

    private Database getDatabase() {
        Application application = (Application) getApplication();
        return application.getDatabase();
    }

    private Query getLastSeenUsers() {

        com.couchbase.lite.View view = getDatabase().getView("lastSeenUsers");
        if (view.getMap() == null) {
            Mapper map = new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (document.get("type") != null && document.get("type").equals("profile")) {
                        // this should be sorted descending by the last time
                        // the user was seen, so use latestEventCreatedAt for the key
                        Object key = document.get("latestEventCreatedAt");
                        HashMap<String, Object> value = new HashMap<String, Object>();
                        value.put("latestEventCreatedAt", document.get("latestEventCreatedAt"));
                        value.put("name", document.get("name"));
                        emitter.emit(key, value);
                    }
                }
            };
            view.setMap(map, "2");
        }

        Query query = view.createQuery();
        return query;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
