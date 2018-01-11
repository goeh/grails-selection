package grails.plugins.selection

import grails.test.spock.IntegrationSpec
import test.TestEntity

/**
 * Test the ProxySelection class.
 */
class ProxySelectionSpec extends IntegrationSpec {

    def selectionService

    def testHttpRequest() {
        given:
        new TestEntity(number: "1", name: "Foo").save()
        new TestEntity(number: "2", name: "Bar").save()
        new TestEntity(number: "3", name: "Bert").save()
        new TestEntity(number: "4", name: "Folke").save()
        new TestEntity(number: "5", name: "David").save()
        when:
        def file = File.createTempFile("grails-", ".sel")
        file.deleteOnExit()
        file << "gorm://testEntity/list?name=Fo"
        def result = selectionService.select(file.toURI())

        file.delete()

        then:
        result.size() == 2
        result.findAll { it.name.startsWith('Fo') }.size() == 2
    }
}
