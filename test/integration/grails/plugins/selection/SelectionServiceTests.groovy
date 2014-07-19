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
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [age: '40']).query == 'name=Joe&age=40'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe+Adams"), [age: '40']).query == 'name=Joe+Adams&age=40'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [name: 'Joe']).query == 'name=Joe'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [name: 'Eve']).query == 'name=Eve'
    }

    void testAddQueryWithListParameter() {
        assert selectionService.addQuery(new URI("gorm://testEntity/list"), [name: 'Joe', age: ['40', '50']]).query == 'name=Joe&age=40&age=50'
    }

    void testAddQueryWithUrlEncodedParameter() {
        assert selectionService.addQuery(new URI("gorm://testEntity/list"), [name: 'Göran', age: '47']).query == 'name=Göran&age=47'
    }

    void testParameterMapQuery() {
        def values = [q: "gorm://testEntity/list".encodeAsURL(), offset: 0, max: 10, controller: "integration", action: "test", name: "Foo"]
        def params = new GrailsParameterMap(values, null)
        // The method getSelectionQuery() is dynamically added by SelectionGrailsPlugin#doWithDynamicMethods()
        def query = params.getSelectionQuery()

        // The following parameters should be available in params
        assert params.q != null
        assert params.offset != null
        assert params.max != null
        assert params.controller != null
        assert params.action != null

        // The following parameters should be available in query
        assert query.name != null

        // The following parameters should be discarded from query
        assert query.q == null
        assert query.offset == null
        assert query.max == null
        assert query.controller == null
        assert query.action == null
    }

    void testParameterMapListValues() {
        def values = [single: 42, multiple: ['43', '44', '45'].toArray(new String[3])]
        def params = new GrailsParameterMap(values, null)
        def query = params.getSelectionQuery()
        assert query.single == 42
        assert (query.multiple instanceof String[])
        assert !query.multiple.contains('42')
        assert query.multiple.contains('43')
        assert query.multiple.contains('44')
        assert query.multiple.contains('45')
    }

    void testParameterMapWithListHint() {
        def values = [single: '42', multiple: ['43', '44', '45'].toArray(new String[3])]
        def params = new GrailsParameterMap(values, null)
        def query = params.getSelectionQuery(collection: ['multiple'])
        assert query.single == '42'
        assert (query.multiple instanceof List)
        assert !query.multiple.contains('42')
        assert query.multiple.contains('43')
        assert query.multiple.contains('44')
        assert query.multiple.contains('45')
    }

    void testParameterMapWithListHintSingle() {
        def values = [single: '42', multiple: '43']
        def params = new GrailsParameterMap(values, null)
        def query = params.getSelectionQuery(collection: ['multiple'])
        assert (query.single instanceof String)
        assert query.single == '42'
        assert (query.multiple instanceof List)
        assert !query.multiple.contains('42')
        assert query.multiple.contains('43')
    }

    void testParameterMapUnderscore() {
        def values = [foo: 42, _bar: 'hello', bar: 'hello', _msg: 'world']
        def params = new GrailsParameterMap(values, null)
        def query = params.getSelectionQuery()
        assert query.foo == 42
        assert query.bar == 'hello'
        assert query._bar == null
        assert query._msg == null
    }

    void testParameterMapQueryExclude() {
        def values = [id: 1, uri: "gorm://testEntity/list".encodeAsURL(), offset: 0, max: 10, controller: "integration", action: "test", name: "Foo", idx: 42]
        def params = new GrailsParameterMap(values, null)

        // Change parameter name to 'uri
        grailsApplication.config.selection.uri.parameter = 'uri'

        // Now 'uri' should be excluded instead of 'id'.
        def query = params.getSelectionQuery()
        assert query.size() == 3
        assert query.id == 1
        assert query.idx == 42
        assert query.name == "Foo"
        assert query.uri == null
        assert query.offset == null
        assert query.max == null
        assert query.controller == null
        assert query.action == null

        // Let's exclude 'idx' for this call.
        query = params.getSelectionQuery(exclude: ['idx'])
        assert query.size() == 2
        assert query.id == 1
        assert query.name == "Foo"
        assert query.idx == null
        assert query.uri == null
    }

    void testParameterMapURI() {
        def values = [q: "gorm://testEntity/list?name=Foo"]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'none' // base64 is default since 0.9.4
        // The method getSelectionURI() is dynamically added by SelectionGrailsPlugin#doWithDynamicMethods()
        def uri = params.getSelectionURI()
        assert uri.toString() == "gorm://testEntity/list?name=Foo"
        assert uri.query == 'name=Foo'
    }

    void testParameterMapURIEncoded() {
        def values = [q: "gorm://testEntity/list?name=Foo".encodeAsURL()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'url'
        def uri = params.getSelectionURI()
        assert uri.toString() == "gorm://testEntity/list?name=Foo"
        assert uri.query == 'name=Foo'
    }

    void testParameterMapURIBase64Encoded() {
        def values = [q: "gorm://testEntity/list?name=Foo".encodeAsBase64()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = null // base64 is default since 0.9.4
        def uri = params.getSelectionURI()
        assert uri.toString() == "gorm://testEntity/list?name=Foo"
        assert uri.query == 'name=Foo'
    }

    void testParameterMapURIHexEncoded() {
        def values = [q: "gorm://testEntity/list?name=Foo".encodeAsHex()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'hex'
        def uri = params.getSelectionURI()
        assert uri.toString() == "gorm://testEntity/list?name=Foo"
        assert uri.query == 'name=Foo'
    }

    void testParameterMapURIName() {
        def uri = "gorm://testEntity/list".encodeAsBase64()
        // 'q' is the default URI parameter name
        def params = new GrailsParameterMap([q: uri], null)
        assert params.getSelectionURI() != null

        // Change parameter name to 'uri
        grailsApplication.config.selection.uri.parameter = 'uri'

        // Not found using default parameter name anymore
        params = new GrailsParameterMap([q: uri], null)
        assert params.getSelectionURI() == null

        // It's found with the name 'uri'
        params = new GrailsParameterMap([uri: uri], null)
        assert params.getSelectionURI() != null

        // Override the configured name for this statement
        params = new GrailsParameterMap([foo: uri], null)
        assert params.getSelectionURI() == null
        assert params.getSelectionURI('foo') != null

    }

    void testEncodeDecodeSelection() {
        def uri = new URI("gorm://testEntity/list?name=Foo")
        def encoded = selectionService.encodeSelection(uri)
        def decoded = selectionService.decodeSelection(encoded)
        assert decoded == uri
    }

    void testSelectionParams() {
        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'base64'
        grailsApplication.config.selection.uri.parameter = 'q'

        def uri = new URI("gorm://testEntity/list?name=A*")
        def params = selectionService.createSelectionParameters(uri)

        println "$params"
        assert params.q != null

    }

    void testEnhancedURI() {
        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'url'
        grailsApplication.config.selection.uri.parameter = 'q'

        def uri1 = new URI("gorm://testEntity/list?name=A*")
        def map = uri1.selectionMap
        def encoded = map.q
        assert encoded == "gorm%3A%2F%2FtestEntity%2Flist%3Fname%3DA*"

        def uri2 = new URI(encoded.decodeURL())
        assert uri1 == uri2
    }
}
