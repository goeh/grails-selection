package grails.plugins.selection

import test.TestEntity

/**
 * Test the ProxySelection class.
 */
class ProxySelectionTests {

    def selectionService

    def testHttpRequest() {
        new TestEntity(number: "1", name: "Foo").save()
        new TestEntity(number: "2", name: "Bar").save()
        new TestEntity(number: "3", name: "Bert").save()
        new TestEntity(number: "4", name: "Folke").save()
        new TestEntity(number: "5", name: "David").save()

        def file = File.createTempFile("grails-", ".sel")
        file.deleteOnExit()
        file << "gorm://testEntity/list?name=Fo"
        def uri = file.toURI()
        println "file uri=$uri"

        def result = selectionService.select(uri)

        file.delete()

        assert result.size() == 2
        result.each {
            assert it.name.startsWith('Fo')
        }
    }
}
