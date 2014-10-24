package demo.lite.couchbase.com.officeradar;

import android.app.*;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.util.Log;

import java.util.Map;

/**
 * The main radar screen that shows all users and when/where
 * they were last spotted.
 */
public class LastSeenUsersListAdapter extends LiveQueryAdapter {

    private Database database;

    public LastSeenUsersListAdapter(Context context, LiveQuery query, Database database) {
        super(context, query);
        this.database = database;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_office, null);
        }

        Map<String, Object> value = (Map<String, Object>) getValue(position);
        String name = (String) value.get("name");

        String beaconLocation = getBeaconLocation(position);


        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText(name + " - " + beaconLocation);

        return convertView;
    }

    private String getBeaconLocation(int position) {
        try {

            // find the location by looking up the beacon doc (via the geofence doc)
            Document profileDocument = (Document) getItem(position);
            String geofenceDocId = (String) profileDocument.getProperties().get("latestEvent");
            Document geofenceDocument = database.getDocument(geofenceDocId);
            String beaconDocId = (String) geofenceDocument.getProperties().get("beacon");
            Document beaconDocument = database.getDocument(beaconDocId);
            return (String) beaconDocument.getProperties().get("location");

        } catch (Exception e) {
            Log.e(Application.TAG, "Error getting beacon location", e);
        }
        return "Error";
    }

}
