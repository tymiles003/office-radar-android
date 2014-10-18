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

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;

import demo.lite.couchbase.com.officeradar.document.GeofenceEvent;


public class MainActivity extends Activity {

    private GeofenceListAdapter geofenceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMainListView();

    }

    private void initMainListView() {

        Query query = GeofenceEvent.getQuery(getDatabase());

        geofenceListAdapter = new GeofenceListAdapter(this, query.toLiveQuery());

        ListView listView = (ListView) findViewById(R.id.mainListView);

        listView.setAdapter(geofenceListAdapter);
    }

    private Database getDatabase() {
        Application application = (Application) getApplication();
        return application.getDatabase();
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

    private class GeofenceListAdapter extends LiveQueryAdapter {

        public GeofenceListAdapter(Context context, LiveQuery query) {
            super(context, query);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.view_office, null);
            }

            final Document doc = (Document) getItem(position);

            TextView text = (TextView) convertView.findViewById(R.id.text);
            text.setText((String) doc.getProperty("_id"));

            return convertView;
        }

    }

}
