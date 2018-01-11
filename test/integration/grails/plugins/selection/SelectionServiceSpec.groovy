package grails.plugins.selection

import grails.test.spock.IntegrationSpec
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

/**
 * Tests for SelectionService.
 */
class SelectionServiceSpec extends IntegrationSpec {

    def selectionService
    def grailsApplication

    def setup() {
        grailsApplication.config.selection.uri.encoding = null
        grailsApplication.config.selection.uri.parameter = null
    }

    def testAddQuery() {
        expect:
        selectionService.addQuery(new URI("gorm://testEntity/list"), [name: 'Joe']).query == 'name=Joe'
        selectionService.addQuery(new URI("gorm://testEntity/list?"), [name: 'Joe']).query == 'name=Joe'
        selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [:]).query == 'name=Joe'
        selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [age: '40']).query == 'name=Joe&age=40'
        selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe+Adams"), [age: '40']).query == 'name=Joe+Adams&age=40'
        selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [name: 'Joe']).query == 'name=Joe'
        selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [name: 'Eve']).query == 'name=Eve'
    }

    def testAddQueryWithListParameter() {
        expect:
        selectionService.addQuery(new URI("gorm://testEntity/list"), [name: 'Joe', age: ['40', '50']]).query == 'name=Joe&age=40&age=50'
    }

    def testAddQueryWithUrlEncodedParameter() {
        expect:
        selectionService.addQuery(new URI("gorm://testEntity/list"), [name: 'Göran', age: '47']).query == 'name=Göran&age=47'
    }

    def testParameterMapQuery() {
        when:
        def values = [q: "gorm://testEntity/list".encodeAsURL(), offset: 0, max: 10, controller: "integration", action: "test", name: "Foo"]
        def params = new GrailsParameterMap(values, null)
        // The method getSelectionQuery() is dynamically added by SelectionGrailsPlugin#doWithDynamicMethods()
        def query = params.getSelectionQuery()
        then:
        // The following parameters should be available in params
        params.q != null
        params.offset != null
        params.max != null
        params.controller != null
        params.action != null

        // The following parameters should be available in query
        query.name != null

        // The following parameters should be discarded from query
        query.q == null
        query.offset == null
        query.max == null
        query.controller == null
        query.action == null
    }

    def testParameterMapListValues() {
        when:
        def values = [single: 42, multiple: ['43', '44', '45'].toArray(new String[3])]
        def params = new GrailsParameterMap(values, null)
        def query = params.getSelectionQuery()
        then:
        query.single == 42
        (query.multiple instanceof String[])
        !query.multiple.contains('42')
        query.multiple.contains('43')
        query.multiple.contains('44')
        query.multiple.contains('45')
    }

    def testParameterMapWithListHint() {
        when:
        def values = [single: '42', multiple: ['43', '44', '45'].toArray(new String[3])]
        def params = new GrailsParameterMap(values, null)
        def query = params.getSelectionQuery(collection: ['multiple'])
        then:
        query.single == '42'
        (query.multiple instanceof List)
        !query.multiple.contains('42')
        query.multiple.contains('43')
        query.multiple.contains('44')
        query.multiple.contains('45')
    }

    def testParameterMapWithListHintSingle() {
        when:
        def values = [single: '42', multiple: '43']
        def params = new GrailsParameterMap(values, null)
        def query = params.getSelectionQuery(collection: ['multiple'])
        then:
        (query.single instanceof String)
        query.single == '42'
        (query.multiple instanceof List)
        !query.multiple.contains('42')
        query.multiple.contains('43')
    }

    def testParameterMapUnderscore() {
        when:
        def values = [foo: 42, _bar: 'hello', bar: 'hello', _msg: 'world']
        def params = new GrailsParameterMap(values, null)
        def query = params.getSelectionQuery()
        then:
        query.foo == 42
        query.bar == 'hello'
        query._bar == null
        query._msg == null
    }

    def testParameterMapQueryExclude() {
        given:
        def values = [id: 1, uri: "gorm://testEntity/list".encodeAsURL(), offset: 0, max: 10, controller: "integration", action: "test", name: "Foo", idx: 42]
        def params = new GrailsParameterMap(values, null)

        // Change parameter name to 'uri
        grailsApplication.config.selection.uri.parameter = 'uri'
        when:
        // Now 'uri' should be excluded instead of 'id'.
        def query = params.getSelectionQuery()
        then:
        query.size() == 3
        query.id == 1
        query.idx == 42
        query.name == "Foo"
        query.uri == null
        query.offset == null
        query.max == null
        query.controller == null
        query.action == null

        when:
        // Let's exclude 'idx' for this call.
        query = params.getSelectionQuery(exclude: ['idx'])
        then:
        query.size() == 2
        query.id == 1
        query.name == "Foo"
        query.idx == null
        query.uri == null
    }

    def testParameterMapURI() {
        given:
        def values = [q: "gorm://testEntity/list?name=Foo"]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'none' // base64 is default since 0.9.4
        // The method getSelectionURI() is dynamically added by SelectionGrailsPlugin#doWithDynamicMethods()
        when:
        def uri = params.getSelectionURI()
        then:
        uri.toString() == "gorm://testEntity/list?name=Foo"
        uri.query == 'name=Foo'
    }

    def testParameterMapURIEncoded() {
        given:
        def values = [q: "gorm://testEntity/list?name=Foo".encodeAsURL()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'url'
        when:
        def uri = params.getSelectionURI()
        then:
        uri.toString() == "gorm://testEntity/list?name=Foo"
        uri.query == 'name=Foo'
    }

    def testParameterMapURIBase64Encoded() {
        given:
        def values = [q: "gorm://testEntity/list?name=Foo".encodeAsBase64()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = null // base64 is default since 0.9.4
        when:
        def uri = params.getSelectionURI()
        then:
        uri.toString() == "gorm://testEntity/list?name=Foo"
        uri.query == 'name=Foo'
    }

    def testParameterMapURIHexEncoded() {
        given:
        def values = [q: "gorm://testEntity/list?name=Foo".encodeAsHex()]
        def params = new GrailsParameterMap(values, null)
        grailsApplication.config.selection.uri.encoding = 'hex'
        when:
        def uri = params.getSelectionURI()
        then:
        uri.toString() == "gorm://testEntity/list?name=Foo"
        uri.query == 'name=Foo'
    }

    def testParameterMapURIName() {
        when:
        def uri = "gorm://testEntity/list".encodeAsBase64()
        // 'q' is the default URI parameter name
        def params = new GrailsParameterMap([q: uri], null)
        then:
        params.getSelectionURI() != null

        when:
        // Change parameter name to 'uri
        grailsApplication.config.selection.uri.parameter = 'uri'

        // Not found using default parameter name anymore
        params = new GrailsParameterMap([q: uri], null)
        then:
        params.getSelectionURI() == null
        when:
        // It's found with the name 'uri'
        params = new GrailsParameterMap([uri: uri], null)
        then:
        params.getSelectionURI() != null
        when:
        // Override the configured name for this statement
        params = new GrailsParameterMap([foo: uri], null)
        then:
        params.getSelectionURI() == null
        params.getSelectionURI('foo') != null

    }

    def testEncodeDecodeSelection() {
        when:
        def uri = new URI("gorm://testEntity/list?name=Foo")
        def encoded = selectionService.encodeSelection(uri)
        def decoded = selectionService.decodeSelection(encoded)
        then:
        decoded == uri
    }

    def testSelectionParams() {
        given:
        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'base64'
        grailsApplication.config.selection.uri.parameter = 'q'
        when:
        def uri = new URI("gorm://testEntity/list?name=A*")
        def params = selectionService.createSelectionParameters(uri)

        then:
        params.q != null

    }

    def testEnhancedURI() {
        given:
        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'url'
        grailsApplication.config.selection.uri.parameter = 'q'
        when:
        def uri1 = new URI("gorm://testEntity/list?name=A*")
        def map = uri1.selectionMap
        def encoded = map.q
        then:
        encoded == "gorm%3A%2F%2FtestEntity%2Flist%3Fname%3DA*"
        when:
        def uri2 = new URI(encoded.decodeURL())
        then:
        uri1 == uri2
    }

    def testParameterMapArray() {
        given:
        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'none'
        grailsApplication.config.selection.uri.parameter = 'q'
        def uri = new URI("gorm://testEntity/list?name=Foo&name=Bar")
        def values = [name: ["Foo", "Bar"].toArray(new String[2])]
        def params = new GrailsParameterMap(values, null)
        when:
        def query = params.getSelectionQuery(collection: 'name')
        then:
        query.name == ['Foo', 'Bar']
    }

    def testParameterMapList() {
        given:
        // Initialize a known config.
        grailsApplication.config.selection.uri.encoding = 'none'
        grailsApplication.config.selection.uri.parameter = 'q'
        def uri = new URI("gorm://testEntity/list?name=Foo&name=Bar")
        def values = [name: ["Foo", "Bar"]]
        def params = new GrailsParameterMap(values, null)
        when:
        def query = params.getSelectionQuery(collection: 'name')
        then:
        query.name == ['Foo', 'Bar']
    }
}
