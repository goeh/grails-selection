/*
 *  Copyright 2012 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package grails.plugins.selection

import org.codehaus.groovy.grails.web.util.WebUtils

/**
 * Main user facing service for the selection framework.
 */
class SelectionService {

    static transactional = false // TODO true?

    def grailsApplication

    /**
     * Selection data using a URI based syntax.
     *
     * @param query query URI, for example gorm://book/list?author=Anderson
     * @param params (optional) parameters sent to the query handler, i.e. offset, max, sort.
     * @return query result could be a domain instance or a List of instances, or anything...
     */
    def select(query, params = null) {
        def uri = (query instanceof URI) ? query : new URI(query.toString())
        def handler = getSelectionHandler(uri)
        if (log.isDebugEnabled()) {
            log.debug("${handler.class.name} selected for $uri $params")
        }
        handler.select(uri, params)
    }

    /**
     * Return the selection handler bean that supports the specified query URI.
     *
     * @param uri uri the URI to find support for
     * @return a selection handler
     * @throws IllegalArgumentException if no handler was found
     */
    def getSelectionHandler(URI uri) {
        for (GrailsSelectionClass clazz in grailsApplication.selectionClasses) {
            if (clazz.supports(uri)) {
                return grailsApplication.mainContext.getBean(clazz.propertyName)
            }
        }
        throw new IllegalArgumentException("No selection provider found for URI [$uri]. Installed providers ${grailsApplication.selectionClasses*.propertyName}")
    }

    /**
     * Append query to a URI with values from a Map.
     * @param uri the base URI, existing query will be preserved
     * @param query key/value pairs to be appended to the URI query
     * @return a new URI instance with query appended
     */
    URI addQuery(URI uri, Map query) {
        def tmp = uri.toString()
        def queryString = WebUtils.toQueryString(query).substring(1) // Remove the leading '?'
        if (queryString) {
            if (tmp.indexOf('?') > -1) {
                if (tmp[-1] != '?') {
                    tmp += '&' // Append our query to the existing query.
                }
            } else {
                tmp += '?' // No existing query, our query will be the complete query.
            }
        }
        new URI(tmp + queryString)
    }
}