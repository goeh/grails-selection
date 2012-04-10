package grails.plugins.selection

import org.codehaus.groovy.grails.web.util.WebUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

/**
 * Utility methods for the selection framework.
 *
 * @author Goran Ehrsson
 * @author Graeme Rocher
 * @since 0.1
 */
class SelectionUtils {
    private SelectionUtils() {}

    /**
     * Convert a query string to a Map.
     *
     * @param query query string i.e. "sex=female&state=CA&age=%3E40"
     * @return the query as a Map i.e. [sex:"female", state:"CA", age:"<40]
     */
    static Map queryAsMap(String query) {
        query ? WebUtils.fromQueryString(query) : [:]
    }

    /**
     * Converts the given params into a query string started with ?
     * @param params The params
     * @param encoding The encoding to use
     * @return The query string
     * @throws UnsupportedEncodingException If the given encoding is not supported
     */
    @SuppressWarnings("rawtypes")
    public static String toQueryString(Map params, String encoding) throws UnsupportedEncodingException {
        if (encoding == null) encoding = "UTF-8";
        StringBuilder queryString = new StringBuilder("?");

        for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            boolean hasMore = i.hasNext();
            boolean wasAppended = appendEntry(entry, queryString, encoding, "");
            if (hasMore && wasAppended) queryString.append('&');
        }
        return queryString.toString();
    }

    /**
     * Converts the given parameters to a query string using the default  UTF-8 encoding
     * @param parameters The parameters
     * @return The query string
     * @throws UnsupportedEncodingException If UTF-8 encoding is not supported
     */
    @SuppressWarnings("rawtypes")
    public static String toQueryString(Map parameters) throws UnsupportedEncodingException {
        return toQueryString(parameters, "UTF-8");
    }

    @SuppressWarnings("rawtypes")
    private static boolean appendEntry(Map.Entry entry, StringBuilder queryString, String encoding, String path) throws UnsupportedEncodingException {
        String name = entry.getKey().toString();
        if (name.indexOf(".") > -1) return false; // multi-d params handled by recursion

        Object value = entry.getValue();
        if (value == null) value = "";
        else if (value instanceof GrailsParameterMap) {
            GrailsParameterMap child = (GrailsParameterMap) value;
            Set nestedEntrySet = child.entrySet();
            for (Iterator i = nestedEntrySet.iterator(); i.hasNext();) {
                Map.Entry childEntry = (Map.Entry) i.next();
                appendEntry(childEntry, queryString, encoding, entry.getKey().toString() + '.');
                boolean hasMore = i.hasNext();
                if (hasMore) queryString.append('&');
            }
        } else if (value instanceof List) {
            List valueList = (List) value;
            for (Iterator i = valueList.iterator(); i.hasNext();) {
                Object v = i.next();
                queryString.append(URLEncoder.encode(path + name, encoding)).append('=').append(URLEncoder.encode(v.toString(), encoding));
                boolean hasMore = i.hasNext();
                if (hasMore) queryString.append('&');
            }
        } else {
            queryString.append(URLEncoder.encode(path + name, encoding)).append('=').append(URLEncoder.encode(value.toString(), encoding));
        }
        return true;
    }

}
