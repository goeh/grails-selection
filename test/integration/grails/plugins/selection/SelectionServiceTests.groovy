package grails.plugins.selection

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

/**
 * Tests for SelectionService.
 */
class SelectionServiceTests extends GroovyTestCase {

    def selectionService
    def grailsApplication

    protected void setUp() {
        super.setUp()

        grailsApplication.config.selection.uri.encoding = null
        grailsApplication.config.selection.uri.parameter = null
    }

    void testAddQuery() {
        assert selectionService.addQuery(new URI("gorm://testEntity/list"), [name: 'Joe']).query == 'name=Joe'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?"), [name: 'Joe']).query == 'name=Joe'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [:]).query == 'name=Joe'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [age: 40]).query == 'name=Joe&age=40'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe+Adams"), [age: 40]).query == 'name=Joe+Adams&age=40'
    }

    void testParameterMapQuery() {
        def values = [id: "gorm://testEntity/list".encodeAsURL(), offset: 0, max: 10, controller: "integration", action: "test", name: "Foo"]
        def params = new GrailsParameterMap(values, null)
        // The method getSelectionQuery() is dynamically added by SelectionGrailsPlugin#doWithDynamicMethods()
        def query = params.getSelectionQuery()

        // The following parameters should be available in params
        assert params.id != null
        assert params.offset != null
        assert params.max != null
        assert params.controller != null
        assert params.action != null

        // The following parameters should be available in query
        assert query.name != null

        // The following parameters should be discarded from query
        assert query.id == null
        assert query.offset == null
        assert query.max == null
        assert query.controller == null
        assert query.action == null
    }

    void testParameterMapURI() {
        def values = [id: "gorm://testEntity/list?name=Foo"]
        def params = new GrailsParameterMap(values, null)
        // The method getSelectionURI() is dynamically added by SelectionGrailsPlugin#doWithDynamicMethods()
        def uri = params.getSelectionURI()
        assert uri.toString() == "gorm://testEntity/list?name=Foo"
        assert uri.query == 'name=Foo'
    }

    void testParameterMapURIEncoded() {
        def values = [id: "gorm://testEntity/list?name=Foo".encodeAsURL()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'url'
        def uri = params.getSelectionURI()
        assert uri.toString() == "gorm://testEntity/list?name=Foo"
        assert uri.query == 'name=Foo'
    }

    void testParameterMapURIBase64Encoded() {
        def values = [id: "gorm://testEntity/list?name=Foo".encodeAsBase64()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'base64'
        def uri = params.getSelectionURI()
        assert uri.toString() == "gorm://testEntity/list?name=Foo"
        assert uri.query == 'name=Foo'
    }

    void testParameterMapURIHexEncoded() {
        def values = [id: "gorm://testEntity/list?name=Foo".encodeAsHex()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'hex'
        def uri = params.getSelectionURI()
        assert uri.toString() == "gorm://testEntity/list?name=Foo"
        assert uri.query == 'name=Foo'
    }

    void testParameterMapURIName() {
        def uri = "gorm://testEntity/list".encodeAsURL()
        // 'id' is the default URI parameter name
        def params = new GrailsParameterMap([id: uri], null)
        assert params.getSelectionURI() != null

        // Change parameter name to 'uri
        grailsApplication.config.selection.uri.parameter = 'uri'

        // Not found using default parameter name anymore
        params = new GrailsParameterMap([id: uri], null)
        assert params.getSelectionURI() == null

        // It's found with the name 'uri'
        params = new GrailsParameterMap([uri: uri], null)
        assert params.getSelectionURI() != null
    }
}
