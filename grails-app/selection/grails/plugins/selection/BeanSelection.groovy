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
 * A selection handler that call methods on Spring Beans.
 *
 * Examples:
 * bean://myBean/getEvents?status=1
 * bean://anotherBean/getSomething/arg1/arg2/arg3
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

    /**
     * Invoke method on a spring bean encoded in the specified URI
     * @param uri the encoded bean/method
     * @param params (optional) parameters to the method invocation
     * @return result returned by the bean method
     */
    def select(URI uri, Map params) {
        // Lookup the bean in application context.
        def bean = grailsApplication.mainContext.getBean(uri.host)

        // path contains the method name to invoke, and optional method arguments.
        def path = uri.path?.decodeURL()
        if (path.startsWith('/')) {
            path = path.substring(1)
        }
        if (!path) {
            throw new IllegalArgumentException("URI has no bean method (path) [$uri]")
        }
        // If the method name is followed by one or more slashes, they are treated as positional method arguments.
        def args = path.split('/').toList()
        def method = args.remove(0)

        if (args.size() == 0) {
            args = null
        } else if (args.size() == 1) {
            args = args[0]
        }

        // Query will be sent to the method as a named arguments Map.
        def query = uri.rawQuery ? WebUtils.fromQueryString(uri.rawQuery) : [:]

        if(log.isDebugEnabled()) {
            log.debug("method=$method args=$args query=$query")
        }

        // Construct method arguments depending on supplied parameters.
        def methodArguments = []
        if (args) {
            methodArguments << args
        }
        if (query) {
            methodArguments << query
        }
        if (params != null) {
            methodArguments << params
        }
        def result
        switch (methodArguments.size()) {
            case 0:
                result = bean."$method"()
                break
            case 1:
                result = bean.invokeMethod(method, methodArguments[0])
                break
            default:
                result = bean.invokeMethod(method, methodArguments.toArray())
                break
        }
        return result
    }
}