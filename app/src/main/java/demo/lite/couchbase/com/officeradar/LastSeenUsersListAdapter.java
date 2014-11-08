package demo.lite.couchbase.com.officeradar;

import android.app.*;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.util.Log;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import demo.lite.couchbase.com.officeradar.misc.ISO8601;
import demo.lite.couchbase.com.officeradar.misc.Util;

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
        name = Util.truncateName(name, 15);

        String latestEventCreatedAt = (String) value.get("latestEventCreatedAt");
        String latestEventDescription = getLatestEventDescription(latestEventCreatedAt);

        ImageView profileImage = (ImageView) convertView.findViewById(R.id.image);
        profileImage.setImageResource(R.drawable.profile_placeholder);

        String beaconLocation = getBeaconLocation(position);

        TextView textViewName = (TextView) convertView.findViewById(R.id.name);
        textViewName.setText(name);

        TextView textViewEntryExit = (TextView) convertView.findViewById(R.id.entry_exit_time);
        textViewEntryExit.setText(latestEventDescription);

        TextView textViewLastLocation = (TextView) convertView.findViewById(R.id.last_seen_location);
        textViewLastLocation.setText(beaconLocation);

        return convertView;
    }

    /**
     * Given the ISO8601 encoded date (2014-10-24T20:09:19.233Z), get a string description of how long
     * ago this was, eg "2 hours ago".
     */
    private String getLatestEventDescription(String latestEventCreatedAt) {
        String latestEventDescription = latestEventCreatedAt;

        try {
            Calendar eventCal = ISO8601.toCalendar(latestEventCreatedAt);
            eventCal.toString();
            Calendar nowCal = new GregorianCalendar();

            long secs = (nowCal.getTime().getTime() - eventCal.getTime().getTime()) / 1000;
            long hours = secs / 3600;
            long minutes = secs / 60;
            latestEventDescription = String.format("Last seen %d hours ago", hours);

            if (hours == 0) {
                latestEventDescription = String.format("Last seen %d minutes ago", minutes);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return latestEventDescription;
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
