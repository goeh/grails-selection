package grails.plugins.selection

/**
 * Tests for SelectionService.
 */
class SelectionServiceTests extends GroovyTestCase {

    def selectionService

    void testAddQuery() {
        assert selectionService.addQuery(new URI("gorm://testEntity/list"), [name:'Joe']).query == 'name=Joe'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?"), [name:'Joe']).query == 'name=Joe'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [:]).query == 'name=Joe'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe"), [age:40]).query == 'name=Joe&age=40'
        assert selectionService.addQuery(new URI("gorm://testEntity/list?name=Joe+Adams"), [age:40]).query == 'name=Joe+Adams&age=40'
    }
}
