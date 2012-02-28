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
 * A selection handler that call methods on Spring Beans.
 *
 * Examples:
 * bean://myBean/getEvents?status=1
 * bean://erpIntegrationBean/getInvoices?date=%3E2012-01-01
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
class BeanSelection {

    def grailsApplication

    /**
     * This selection handler supports bean: queries.
     * @param uri the URI to check support for
     * @return true if uri.scheme is 'bean'
     */
    boolean supports(URI uri) {
        return uri?.scheme == 'bean'
    }

    def select(URI uri, Map params) {
        def bean = grailsApplication.mainContext.getBean(uri.host)
        def path = uri.path?.decodeURL()
        if(path.startsWith('/')) {
            path = path.substring(1)
        }
        if (!path) {
            throw new IllegalArgumentException("URI has no bean method (path) [$uri]")
        }
        def args = path.split('/').toList()
        def method = args.remove(0)
        def query = SelectionUtils.queryAsMap(uri.query)
        if(args.size() == 0) {
            args = null
        } else if(args.size() == 1) {
            args = args[0]
        }
        log.debug("method=$method args=$args query=$query")
        def result
        if(args && query) {
            result = bean.invokeMethod(method, [args, query].toArray())
        } else if(args) {
            if(args)
            result = bean.invokeMethod(method, args)
        } else if(query) {
            result = bean.invokeMethod(method, query)
        } else {
            result = bean."$method"()
        }
        return result
    }
}