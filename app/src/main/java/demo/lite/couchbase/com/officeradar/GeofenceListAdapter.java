package demo.lite.couchbase.com.officeradar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.LiveQuery;

/**
 * Show a list of geofence events
 */
public class GeofenceListAdapter extends LiveQueryAdapter {

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

        Object key = getKey0(position);
        Object value = getValue(position);

        // final Document doc = (Document) getItem(position);

        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText(key + " - " + value);

        return convertView;
    }

}
