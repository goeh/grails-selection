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

import grails.spring.BeanBuilder
import grails.plugins.selection.SelectionArtefactHandler
import grails.plugins.selection.GrailsSelectionClass
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class SelectionGrailsPlugin {
    def version = "0.9.9"
    def grailsVersion = "2.0 > *"
    def dependsOn = [:]
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/**",
            "grails-app/controllers/grails/plugins/selection/SelectionTestController.groovy",
            "grails-app/services/grails/plugins/selection/SelectionTestService.groovy"
    ]
    def loadAfter = ['logging']
    def watchedResources = [
            "file:./grails-app/selection/**/*Selection.groovy",
            "file:./plugins/*/grails-app/selection/**/*Selection.groovy"
    ]
    def artefacts = [new SelectionArtefactHandler()]

    def title = "Unified Selection"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
The selection plugin provides unified selection of information.

It uses a URI based syntax to select any information from any resource.

Grails plugins can add custom search providers.

Example 1: gorm://person/list?name=Gr%25

Example 2: ldap:dc=my-company&dc=com&cn=users

Example 3: bean://myService/method

Example 4: https://dialer.mycompany.com/outbound/next?agent=liza
'''

    def documentation = "https://github.com/goeh/grails-selection"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-selection/issues"]
    def scm = [url: "https://github.com/goeh/grails-selection"]

    def doWithSpring = {
        // Create default criteria factory.
        selectionCriteriaFactory(grails.plugins.selection.GormCriteriaFactory)

        // Configure selection handlers
        def selectionClasses = application.selectionClasses
        selectionClasses.each { selectionClass ->
            "${selectionClass.propertyName}"(selectionClass.clazz) { bean ->
                bean.autowire = "byName"
            }
        }
    }

    def doWithDynamicMethods = { ctx ->

        // Enhance URI class.
        URI.class.metaClass.encodeAsSelection = {
            ctx.getBean('selectionService').encodeSelection(delegate)
        }
        URI.class.metaClass.getSelectionMap = {
            def uriParameterName = application.config.selection.uri.parameter ?: 'q'
            def s = ctx.getBean('selectionService').encodeSelection(delegate)
            return [(uriParameterName): s]
        }

        // Add convenient methods to controller params
        def mc = GrailsParameterMap.metaClass
        // Get all query values by filtering out known non-query values.
        mc.getSelectionQuery = { Map opts = [:] ->
            def uriParameterName = application.config.selection.uri.parameter ?: 'q'
            def excludeList = [uriParameterName, 'offset', 'max', 'sort', 'order', 'action', 'controller']
            if (opts.exclude) {
                excludeList.addAll(opts.exclude)
            }
            def collection = opts.collection
            def self = delegate
            def rval = [:]
            if(collection instanceof String) {
                collection = [collection]
            }
            delegate.each {key, value ->
                if (value && !key.startsWith('_') && !excludeList.contains(key)) {
                    rval[key] = collection?.contains(key) ? self.list(key) : value
                }
            }
            return rval
        }
        // Create a URI from
        mc.getSelectionURI = {String uriParameterName = null ->
            if (uriParameterName == null) {
                uriParameterName = application.config.selection.uri.parameter ?: 'q'
            }
            String uri = delegate[uriParameterName]
            if (uri) {
                switch (application.config.selection.uri.encoding.toString().toLowerCase()) {
                    case 'none':
                    case 'url':
                        uri = uri.decodeURL()
                        break
                    case 'hex':
                        uri = new String(uri.decodeHex())
                        break
                    default: /* base64 */
                        uri = new String(uri.decodeBase64())
                        break
                }
                return new URI(uri)
            }
            return null
        }

    }

    def doWithApplicationContext = { applicationContext ->
        println "Installed selection handlers ${application.selectionClasses*.propertyName}"
    }

    def onChange = { event ->
        if (application.isSelectionClass(event.source)) {
            log.debug "Selection ${event.source} modified!"

            def context = event.ctx
            if (!context) {
                log.debug("Application context not found - can't reload.")
                return
            }

            // Make sure the new selection class is registered.
            def selectionClass = application.addArtefact(GrailsSelectionClass.TYPE, event.source)

            // Create the selection bean.
            def bb = new BeanBuilder()
            bb.beans {
                "${selectionClass.propertyName}"(selectionClass.clazz) { bean ->
                    bean.autowire = "byName"
                }
            }
            bb.registerBeans(context)
        }
    }

}
