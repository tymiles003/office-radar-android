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
import com.couchbase.lite.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import demo.lite.couchbase.com.officeradar.Application;
import demo.lite.couchbase.com.officeradar.document.GeofenceEvent;

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
                    Object valueToEmit = document.get("created_at");
                    Object[] key = {document.get("profile"), document.get("created_at")};
                    emitter.emit(key, valueToEmit);
                }
            }
        }, "5");

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

            Map<String, Object> properties = new HashMap<String, Object>(profileDoc.getProperties());
            properties.put("latestEvent", row.getDocumentId());
            properties.put("latestEventCreatedAt", row.getValue());
            try {
                profileDoc.putProperties(properties);
            } catch (CouchbaseLiteException e) {
                throw new RuntimeException(e);
            }

        }



    }

    /**
     * For some reason, some profile objects are missing the "type" field.
     * Fix them.
     */
    public static void fixupProfilesMissingTypeField(Database database) {

        Map<String, String> uniqueProfileNames = new HashMap<String, String>();
        Query query = GeofenceEvent.getQuery(database);
        try {
            QueryEnumerator queryEnumerator = query.run();
            while (queryEnumerator.hasNext()) {
                QueryRow row = queryEnumerator.next();
                String profileId = (String) row.getValue();
                Document profileDocument = database.getDocument(profileId);
                if (profileDocument == null) {
                    Log.d(Application.TAG, "profile doc not found for id: " + profileId);
                }
                String name = (String) profileDocument.getProperties().get("name");
                if (!uniqueProfileNames.containsKey(name)) {
                    uniqueProfileNames.put(name, profileId);
                }

                if (!profileDocument.getProperties().containsKey("type")) {
                    Map<String, Object> properties = new HashMap<String, Object>(profileDocument.getProperties());
                    properties.put("type", "profile");
                    profileDocument.putProperties(properties);
                }

            }

            Log.d(Application.TAG, uniqueProfileNames.toString());
        } catch (CouchbaseLiteException e) {
            throw new RuntimeException(e);
        }


    }

}
