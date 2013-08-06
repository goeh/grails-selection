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

//import org.codehaus.groovy.grails.web.util.WebUtils

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
        def q = SelectionUtils.queryAsMap(uri.query)
        query.each { key, value ->
            if (value instanceof Collection) {
                def list = q.get(key, [])
                if (!(list instanceof Collection)) {
                    list = [list]
                    q[key] = list
                }
                for (v in value) {
                    if (!list.contains(v)) {
                        list << v
                    }
                }
            } else {
                q[key] = value
            }
        }
        def queryString = SelectionUtils.toQueryString(q)
        def s = new StringBuilder()
        s << uri.scheme
        s << ':'
        if (uri.userInfo || uri.host || uri.port) {
            s << '//'
            if (uri.userInfo) {
                s << uri.userInfo.encodeAsURL()
                s << '@'
            }
            if (uri.host) {
                s << uri.host
            }
            if (uri.port > 0) {
                s << ':'
                s << uri.port.toString()
            }
        }
        if (uri.path) {
            s << uri.path
        }
        if (queryString) {
            s << queryString // Already encoded.
        }
        if(uri.fragment) {
            s << '#'
            s << uri.fragment.encodeAsURL()
        }
        if (log.isDebugEnabled()) {
            log.debug "Added query $query to URI [$uri], new URI: [$s]"
        }
        new URI(s.toString())
    }

    /**
     * Encode selection URI with the encoding specified in Config.groovy [selection.uri.encoding]
     * @param uri the selection URI to encode
     * @return the encoded selection (typically base64, hex or url encoded)
     */
    String encodeSelection(URI uri) {
        switch (grailsApplication.config.selection.uri.encoding.toString().toLowerCase()) {
            case 'none':
                return uri.toString()
            case 'base64':
                return uri.toString().encodeAsBase64()
            case 'hex':
                return uri.toString().encodeAsHex()
            default:
                return uri.toString().encodeAsURL()
        }
    }

    /**
     * Decode selection String with the encoding specified in Config.groovy [selection.uri.encoding]
     * @param uri the selection string to decode
     * @return the decoded selection URI
     */
    URI decodeSelection(String uri) {
        if (uri) {
            switch (grailsApplication.config.selection.uri.encoding.toString().toLowerCase()) {
                case 'none':
                    break
                case 'base64':
                    uri = new String(uri.decodeBase64())
                    break
                case 'hex':
                    uri = new String(uri.decodeHex())
                    break
                default:
                    uri = uri.decodeURL()
                    break
            }
            return new URI(uri)
        }
        return null
    }

    Map createSelectionParameters(URI selection, String uriParameterName = null) {
        def result = [:]
        if (uriParameterName == null) {
            uriParameterName = grailsApplication.config.selection.uri.parameter ?: 'q'
        }
        result[uriParameterName] = encodeSelection(selection)

        return result
    }
}