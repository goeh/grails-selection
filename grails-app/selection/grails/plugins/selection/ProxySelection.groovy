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
 * A selection handler that retrieves the real selection URI from a proxy.
 *
 * Examples:
 * https://dialer.mycompany.com/call/outbound?agent=liza
 * http://localhost:8080/myapp/selection/453
 *
 * @author Goran Ehrsson
 * @since 0.5
 */
class ProxySelection {

    def selectionService

    /**
     * This selection handler support http, https, ftp and file queries.
     * @param uri the URI to check support for
     * @return true if uri.scheme is http, https, ftp or file.
     */
    boolean supports(URI uri) {
        return ['http', 'https', 'ftp', 'file'].contains(uri?.scheme)
    }

    def select(URI uri, Map params) {
        // Call the proxy URL and expect a new URI (as text) in return.
        def realUri = new URI(uri.toURL().text?.trim())

        if(log.isDebugEnabled()) {
            log.debug("Proxy selection [$uri] translated to [$realUri]")
        }

        // Now call the real selection URI and return the result.
        selectionService.select(realUri, params)
    }
}