package grails.plugins.selection

/**
 * Utility methods for the selection framework.
 *
 * @author Goran Ehrsson
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
        if (!query) return [:]

        query.split('&').inject([:]) {map, kvp ->
            def idx = kvp.indexOf('=')
            def key, value
            if (idx != -1) {
                key = kvp.substring(0, idx)
                value = kvp.substring(idx + 1)
                map[key] = value
            }
            return map
        }
    }
}
