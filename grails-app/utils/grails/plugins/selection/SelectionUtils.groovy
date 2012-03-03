package grails.plugins.selection

import org.codehaus.groovy.grails.web.util.WebUtils

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
        query ? WebUtils.fromQueryString(query) : [:]
    }
}
