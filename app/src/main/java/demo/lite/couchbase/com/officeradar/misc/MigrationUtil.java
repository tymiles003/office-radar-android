package demo.lite.couchbase.com.officeradar.misc;


import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Revision;
import com.couchbase.lite.View;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MigrationUtil {

    /**
     * Add last_geofence_event to all profile docs
     *
     * - Create a view that shows geofence events for a given profile id, ordered descending (most recent first)
     * - Query it with limit 1
     * - Set the latestEvent field in the profile with that value
     * - Save
     */
    public static void addLatestEvents(Database database) {

        // Create a view that shows all profiles in the system
        View allProfiles = database.getView("all_profiles");
        allProfiles.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                if (document.get("type") != null && document.get("type").equals("profile")) {
                    emitter.emit(document.get("_id"), document.get("name"));
                }
            }
        }, "1");

        Query query = allProfiles.createQuery();
        QueryEnumerator queryEnumerator = null;
        try {
            queryEnumerator = query.run();
        } catch (CouchbaseLiteException e) {
            throw new RuntimeException(e);
        }

        while (queryEnumerator.hasNext()) {
            QueryRow row = queryEnumerator.next();
            addLatestEventsForUser(database, (String) row.getKey(), row.getDocument());
        }

    }

    public static void addLatestEventsForUser(Database database, String userId, Document profileDoc) {

        // Create a view that shows geofence events for a given profile id, ordered descending (most recent first)
        View eventsForProfile = database.getView("events_for_profile");
        eventsForProfile.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                if (document.get("type") != null && document.get("type").equals("geofence_event")) {
                    Object[] key = Arrays.asList(
                            document.get("profile"),
                            document.get("created_at")
                    ).toArray();
                    emitter.emit(new Object[] { document.get("profile"), document.get("created_at") }, null);
                }
            }
        }, "4");

        Query query = eventsForProfile.createQuery();
        query.setDescending(true);
        query.setLimit(1);
        query.setStartKey(Arrays.asList(userId, new HashMap<String, Object>()));
        query.setEndKey(Arrays.asList(userId));

        QueryEnumerator queryEnumerator = null;
        try {
            queryEnumerator = query.run();
        } catch (CouchbaseLiteException e) {
            throw new RuntimeException(e);
        }

        while (queryEnumerator.hasNext()) {
            QueryRow row = queryEnumerator.next();
            Object key = row.getKey();
            Object value = row.getValue();
            Document doc = row.getDocument();
            Map<String, Object> properties = new HashMap<String, Object>(profileDoc.getProperties());
            properties.put("latestEvent", row.getDocumentId());
            try {
                profileDoc.putProperties(properties);
            } catch (CouchbaseLiteException e) {
                throw new RuntimeException(e);
            }

        }



    }

}
