package demo.lite.couchbase.com.officeradar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;

import java.util.Map;

/**
 * The main radar screen that shows all users and when/where
 * they were last spotted.
 */
public class LastSeenUsersListAdapter extends LiveQueryAdapter {

    public LastSeenUsersListAdapter(Context context, LiveQuery query) {
        super(context, query);
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

        Document profileDocument = (Document) getItem(position);

        // TODO: load the geofence event document from doc pointer
        // TODO: and then lookup the beacon, and then display the location

        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText(name);

        return convertView;
    }

}
