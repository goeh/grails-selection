package grails.plugins.selection

import grails.test.GroovyPagesTestCase

/**
 * Test SelectionTagLib.
 */
class SelectionTagLibTests extends GroovyPagesTestCase {

    def grailsApplication

    void testMissingParameter() {
        shouldFail {
            def template = '<select:link controller="testEntity" action="list">Test</select:link>'
            applyTemplate(template)
        }
    }

    void testSelectionLinkNoEncoding() {

        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'none'
        grailsApplication.config.selection.uri.parameter = 'id'

        def uri = new URI("gorm://testEntity/list?name=A*")
        def template = '<select:link controller="testEntity" action="list" selection="\${uri}">Test</select:link>'
        assert applyTemplate(template, [uri: uri]) == '<a href="/testEntity/list/gorm%3A%2F%2FtestEntity%2Flist%3Fname%3DA*">Test</a>'
    }

    void testSelectionLinkBase64Encoding() {

        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'base64'
        grailsApplication.config.selection.uri.parameter = 'id'

        def uri = new URI("gorm://testEntity/list?name=A*")
        def template = '<select:link controller="testEntity" action="list" selection="\${uri}">Test</select:link>'
        assert applyTemplate(template, [uri: uri]) == '<a href="/testEntity/list/Z29ybTovL3Rlc3RFbnRpdHkvbGlzdD9uYW1lPUEq">Test</a>'
    }

    void testSelectionLinkHexEncoding() {

        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'hex'
        grailsApplication.config.selection.uri.parameter = 'id'

        def uri = new URI("gorm://testEntity/list?name=A*")
        def template = '<select:link controller="testEntity" action="list" selection="\${uri}">Test</select:link>'
        assert applyTemplate(template, [uri: uri]) == '<a href="/testEntity/list/676f726d3a2f2f74657374456e746974792f6c6973743f6e616d653d412a">Test</a>'
    }

    void testSelectionLinkParameter() {

        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'base64'
        grailsApplication.config.selection.uri.parameter = 'q'

        def uri = new URI("gorm://testEntity/list?name=A*")
        def template = '<select:link controller="testEntity" action="list" selection="\${uri}">Test</select:link>'
        assert applyTemplate(template, [uri: uri]) == '<a href="/testEntity/list?q=Z29ybTovL3Rlc3RFbnRpdHkvbGlzdD9uYW1lPUEq">Test</a>'
    }

    void testCreateLink() {

        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'base64'
        grailsApplication.config.selection.uri.parameter = 'q'

        def uri = new URI("gorm://testEntity/list?name=A*")
        def template = '<select:createLink controller="testEntity" action="list" selection="\${uri}"/>'
        assert applyTemplate(template, [uri: uri]) == '/testEntity/list?q=Z29ybTovL3Rlc3RFbnRpdHkvbGlzdD9uYW1lPUEq'
    }

    void testEncode() {

        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'base64'

        def uri = new URI("gorm://testEntity/list?name=A*")
        def template = '<select:encode selection="\${uri}"/>'
        assert applyTemplate(template, [uri: uri]) == "Z29ybTovL3Rlc3RFbnRpdHkvbGlzdD9uYW1lPUEq"
    }
}
