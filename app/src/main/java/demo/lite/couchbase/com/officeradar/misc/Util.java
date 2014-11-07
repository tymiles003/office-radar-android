package demo.lite.couchbase.com.officeradar.misc;

/**
 * Various utilities
 */
public class Util {

    public static String truncateName(String name, int maxchars) {

        String formatString = "%s";
        if (name.length() > maxchars) {
            formatString = "%s ..";
        }
        return String.format(formatString, name.substring(0, Math.min(name.length(), maxchars)));
        
    }

}
