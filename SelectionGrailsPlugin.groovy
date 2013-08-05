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
    // the plugin version
    def version = "0.9.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
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

    def title = "Selection Plugin" // Headline display name of the plugin
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

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/selection"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]

    // Any additional developers beyond the author specified above.
    //    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [system: "JIRA", url: "http://jira.grails.org/browse/GPSELECTION"]

    // Online location of the plugin's browseable source code.
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
            def uriParameterName = application.config.selection.uri.parameter ?: 'id'
            def s = ctx.getBean('selectionService').encodeSelection(delegate)
            return [(uriParameterName): s]
        }

        // Add convenient methods to controller params
        def mc = GrailsParameterMap.metaClass
        // Get all query values by filtering out known non-query values.
        mc.getSelectionQuery = { Map opts = [:] ->
            def uriParameterName = application.config.selection.uri.parameter ?: 'id'
            def excludeList = [uriParameterName, 'offset', 'max', 'sort', 'order', 'action', 'controller']
            if (opts.exclude) {
                excludeList.addAll(opts.exclude)
            }
            def collection = opts.collection
            def self = delegate
            def rval = [:]
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
                uriParameterName = application.config.selection.uri.parameter ?: 'id'
            }
            String uri = delegate[uriParameterName]
            if (uri) {
                switch (application.config.selection.uri.encoding.toString().toLowerCase()) {
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

    }

    def doWithApplicationContext = { applicationContext ->
        println "Installed selection handlers ${application.selectionClasses*.propertyName}"

    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
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
