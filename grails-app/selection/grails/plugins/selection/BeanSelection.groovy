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

import groovy.transform.CompileStatic
import org.apache.commons.lang.reflect.FieldUtils
import org.apache.commons.lang.reflect.MethodUtils
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.util.ReflectionUtils

import java.lang.reflect.Method

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
    @CompileStatic
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
        List<String> parts = path.split('/').toList()
        String method = parts.remove(0)
        def args

        if (parts.size() == 0) {
            args = null
        } else if (parts.size() == 1) {
            args = parts[0]
        } else {
            args = parts
        }

        // Query will be sent to the method as a named arguments Map.
        def query = uri.rawQuery ? WebUtils.fromQueryString(uri.rawQuery) : [:]

        if (log.isDebugEnabled()) {
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

        def m = resolveMethod(bean, method, methodArguments)
        if (m == null) {
            throw new MissingMethodException(method, bean.class, methodArguments.toArray())
        }

        if (!isSelectable(bean, m)) {
            throw new SecurityException("Method [${m.name}] on class [${bean.class.name}] is not selectable")
        }

        invoke(bean, m, methodArguments)
    }

    @CompileStatic
    private Object invoke(Object target, Method m, List args) {
        switch (args.size()) {
            case 0:
                return ReflectionUtils.invokeMethod(m, target)
            case 1:
                return ReflectionUtils.invokeMethod(m, target, args[0])
            default:
                return ReflectionUtils.invokeMethod(m, target, args.toArray())
        }
    }

    @CompileStatic
    private Method resolveMethod(Object target, String methodName, List args) {
        Class[] types = args.collect { it.class }.toArray(new Class[args.size()])
        Method m
        if (types == null || types.length == 0) {
            m = MethodUtils.getMatchingAccessibleMethod(target.class, methodName)
        } else {
            m = MethodUtils.getMatchingAccessibleMethod(target.class, methodName, types)
        }
        return m
    }

    public static final String SELECTABLE_PROPERTY = 'selectable'

    /*
     * There are four ways to make a method selectable.
     * 1. Annotate method with @Selectable
     * 2. static selectable = true on the class will make all methods selectable
     * 3. static selectable = 'methodName' will make that method selectable
     * 4. static selectable = ['method1', 'method2', ...] will make multiple methods selectable
     *
     * @param target target instance
     * @param m method
     * @return true if method is selectable
     */

    @CompileStatic
    private boolean isSelectable(Object target, Method m) {
        if (m.isAnnotationPresent(Selectable)) {
            return true
        }
        Object selectable = FieldUtils.readField(target, SELECTABLE_PROPERTY, true)
        if (selectable != null) {
            if (selectable instanceof Collection) {
                return selectable.contains(m.getName())
            } else if (selectable instanceof Boolean) {
                return selectable
            }
            return selectable.toString() == m.getName()
        }
        return false
    }
}