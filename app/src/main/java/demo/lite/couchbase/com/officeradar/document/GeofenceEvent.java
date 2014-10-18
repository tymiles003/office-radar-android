package demo.lite.couchbase.com.officeradar.document;


import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;

import java.util.Map;

public class GeofenceEvent {

    private static final String VIEW_NAME = "geofenceevents";
    private static final String DOC_TYPE = "geofence_event";

    public static Query getQuery(Database database) {
        com.couchbase.lite.View view = database.getView(VIEW_NAME);
        if (view.getMap() == null) {
            Mapper map = new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (DOC_TYPE.equals(document.get("type"))) {
                        emitter.emit(document.get("_id"), null);
                    }
                }
            };
            view.setMap(map, "1");
        }

        Query query = view.createQuery();
        return query;
    }

}
