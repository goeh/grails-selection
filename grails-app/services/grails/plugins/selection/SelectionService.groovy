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
     * @param params parameters sent to the query handler, i.e. offset, max, sort.
     * @return query result could be a domain instance or a List of instances, or anything...
     */
    def select(query, params = [:]) {
        def uri = (query instanceof URI) ? query : new URI(query.toString())
        def handler = getSelectionHandler(uri)
        if(log.isDebugEnabled()) {
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
        for(GrailsSelectionClass clazz in grailsApplication.selectionClasses) {
            if(clazz.supports(uri)) {
                return grailsApplication.mainContext.getBean(clazz.propertyName)
            }
        }
        throw new IllegalArgumentException("No selection provider found for URI [$uri]. Installed providers ${grailsApplication.selectionClasses*.propertyName}")
    }
}
