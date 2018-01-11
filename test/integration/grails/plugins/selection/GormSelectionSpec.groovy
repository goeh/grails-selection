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

import grails.test.spock.IntegrationSpec
import test.TestEntity

/**
 * Integration test for the GORM Selection Provider (GormSelection.groovy).
 * @author Goran Ehrsson
 */
public class GormSelectionSpec extends IntegrationSpec {

    def grailsApplication
    def selectionService
    def gormSelection

    def setup() {
        grailsApplication.config.selection.gorm = true
    }

    def cleanup() {
        gormSelection.fixedCriteria = null
    }

    void testConfiguration1() {
        given:
        grailsApplication.config.selection.remove('gorm')
        when:
        selectionService.select("gorm://test.TestEntity/list")
        then:
        thrown(SecurityException)
    }

    def testConfiguration2() {
        when:
        grailsApplication.config.selection.remove('gorm')
        grailsApplication.config.selection.gorm.test.TestEntity = true
        then:
        selectionService.select("gorm://test.TestEntity/list") != null

        when:
        selectionService.select("gorm://test.TestEntity2/list")
        then:
        thrown(SecurityException)
    }

    def testConfiguration3() {
        when:
        grailsApplication.config.selection.remove('gorm')
        grailsApplication.config.selection.gorm.test = true
        then:
        selectionService.select("gorm://test.TestEntity/list") != null
        selectionService.select("gorm://test.TestEntity2/list") != null
    }

    void testNonExistingHandler() {
        when:
        selectionService.select("dummy:foo")
        then:
        thrown(IllegalArgumentException)
    }

    void testNonExistingMethod() {
        when:
        selectionService.select("gorm://test.TestEntity/dummy")
        then:
        thrown(IllegalArgumentException)
    }

    void testUriParameter() {
        when:
        new TestEntity(number: "1", name: "Foo").save()
        new TestEntity(number: "2", name: "Bar").save()
        new TestEntity(number: "3", name: "Baz").save()
        then:
        selectionService.select("gorm://test.TestEntity/list").size() == 3
        selectionService.select(new URI("gorm://test.TestEntity/list")).size() == 3
    }

    void testGet() {
        when:
        def contact = new TestEntity(number: "1", name: "Foo").save(flush: true)
        then:
        contact != null
        when:
        def result = selectionService.select("gorm://test.TestEntity/get?id=${contact.id}")
        then:
        result != null
        result.name == "Foo"
    }

    void testList() {
        given:
        new TestEntity(number: "1", name: "Foo").save()
        new TestEntity(number: "2", name: "Bar").save()
        new TestEntity(number: "3", name: "Baz").save()
        when:
        def result = selectionService.select("gorm://test.TestEntity/list?name=Ba", [max: 10])
        then:
        result != null
        result.size() == 2
        result.totalCount == 2
    }

    void testListWithId() {
        given:
        new TestEntity(number: "100", name: "Green").save()
        new TestEntity(number: "101", name: "Blue").save()
        def reference = new TestEntity(number: "102", name: "Red").save(flush: true)
        when:
        def result = selectionService.select("gorm://test.TestEntity/list?id=" + reference.id)
        then:
        result != null
        result.size() == 1
        result.totalCount == 1
        result.find { it }.id == reference.id
    }

    void testDomainShortName() {
        when:
        10.times {
            new TestEntity(number: "$it", name: "Number $it").save()
        }
        then:
        selectionService.select("gorm://testEntity/list").size() == 10
    }

    void testRandom() {
        given:
        new TestEntity(number: "1", name: "One").save(flush: true)
        new TestEntity(number: "2", name: "Two").save(flush: true)
        new TestEntity(number: "3", name: "Three").save(flush: true)
        new TestEntity(number: "4", name: "Four").save(flush: true)
        new TestEntity(number: "5", name: "Five").save(flush: true)
        new TestEntity(number: "6", name: "Six").save(flush: true)
        new TestEntity(number: "7", name: "Seven").save(flush: true)
        new TestEntity(number: "8", name: "Eight").save(flush: true)
        new TestEntity(number: "9", name: "Nine").save(flush: true)
        new TestEntity(number: "10", name: "Ten").save(flush: true)
        when:
        def result = selectionService.select("gorm://test.TestEntity/random")
        then:
        result != null
        result instanceof TestEntity
        when:
        result = selectionService.select("gorm://test.TestEntity/random", [max: 5])
        then:
        result.size() == 5
    }

    void testRandomHalf() {
        given:
        100.times {
            new TestEntity(number: "$it", name: "Number $it").save()
        }
        when:
        def result = selectionService.select("gorm://test.TestEntity/random", [max: 50])
        then:
        result.size() == 50
    }

    void testRandomTough() {
        given:
        200.times {
            new TestEntity(number: "$it", name: "Number $it").save()
        }
        when:
        def result = selectionService.select("gorm://test.TestEntity/random", [max: 199])
        then:
        result.size() == 199
    }

    void testRandomAll() {
        given:
        100.times {
            new TestEntity(number: "$it", name: "Number $it").save()
        }
        when:
        def result = selectionService.select("gorm://test.TestEntity/random", [max: 100])
        then:
        result.size() == 100
    }

    void testRandomWithCriteria() {
        given:
        new TestEntity(number: "1", name: "One").save(flush: true)
        new TestEntity(number: "2", name: "Two").save(flush: true)
        new TestEntity(number: "3", name: "Three").save(flush: true)
        new TestEntity(number: "4", name: "Four").save(flush: true)
        new TestEntity(number: "5", name: "Five").save(flush: true)
        new TestEntity(number: "6", name: "Six").save(flush: true)
        new TestEntity(number: "7", name: "Seven").save(flush: true)
        new TestEntity(number: "8", name: "Eight").save(flush: true)
        new TestEntity(number: "9", name: "Nine").save(flush: true)
        new TestEntity(number: "10", name: "Ten").save(flush: true)
        new TestEntity(number: "11", name: "Eleven").save(flush: true)
        new TestEntity(number: "12", name: "Twelve").save(flush: true)
        new TestEntity(number: "13", name: "Thirteen").save(flush: true)
        when:
        def result = selectionService.select("gorm://test.TestEntity/random?name=" + "T%".encodeAsURL(), [max: 3])
        then:
        result.size() == 3
        result.findAll { it.name.startsWith('T') }.size() == 3
    }

    void testRandomFixedCriteria() {
        given:
        new TestEntity(number: "1", name: "One").save(flush: true)
        new TestEntity(number: "2", name: "Two").save(flush: true)
        new TestEntity(number: "3", name: "Three").save(flush: true)
        new TestEntity(number: "4", name: "Four").save(flush: true)
        new TestEntity(number: "5", name: "Five").save(flush: true)
        new TestEntity(number: "6", name: "Six").save(flush: true)
        new TestEntity(number: "7", name: "Seven").save(flush: true)
        new TestEntity(number: "8", name: "Eight").save(flush: true)
        new TestEntity(number: "9", name: "Nine").save(flush: true)
        new TestEntity(number: "10", name: "Ten").save(flush: true)
        new TestEntity(number: "11", name: "Eleven").save(flush: true)
        new TestEntity(number: "12", name: "Twelve").save(flush: true)
        new TestEntity(number: "13", name: "Thirteen").save(flush: true)

        gormSelection.fixedCriteria = { query, params ->
            ilike('name', 'T%')
        }
        when:
        def result = selectionService.select("gorm://test.TestEntity/random", [max: 3])
        then:
        result.size() == 3
        result.findAll { it.name.startsWith('T') }.size() == 3
    }

    void testCustomCriteria() {
        given:
        new TestEntity(name: "Joe Average", age: 40).save(flush: true)
        new TestEntity(name: "Linda Average", age: 37).save(flush: true)
        new TestEntity(name: "Jason Average", age: 11).save(flush: true)
        new TestEntity(name: "Lisa Average", age: 9).save(flush: true)
        new TestEntity(name: "Ben Average", age: 63).save(flush: true)
        new TestEntity(name: "Mary Average", age: 65).save(flush: true)

        def backup = gormSelection.getCriteria(TestEntity)

        gormSelection.setCriteria(TestEntity) { query, params ->
            if (query.name) {
                ilike('name', '%' + query.name + '%')
            }
            if (query.age) {
                if (query.age[0] == '<') {
                    lt('age', Integer.valueOf(query.age[1..-1]))
                } else if (query.age[0] == '>') {
                    gt('age', Integer.valueOf(query.age[1..-1]))
                } else if (query.age.indexOf('-') != -1) {
                    def (from, to) = query.age.split('-').toList()
                    between('age', Integer.valueOf(from), Integer.valueOf(to))
                } else {
                    eq('age', Integer.valueOf(query.age))
                }
            }
        }
        when:
        try {
            // Test greater than.
            def result = selectionService.select("gorm://test.TestEntity/list?age=" + ">40".encodeAsURL())
            assert result.size() == 2
            result.each { assert it.age > 40 }

            // Test less than.
            result = selectionService.select("gorm://test.TestEntity/list?age=" + "<40".encodeAsURL())
            assert result.size() == 3
            result.each { assert it.age < 40 }

            // Test equals.
            result = selectionService.select("gorm://test.TestEntity/list?age=40")
            assert result.size() == 1
            result.each { assert it.age == 40 }

            // Test between.
            result = selectionService.select("gorm://test.TestEntity/list?age=10-40")
            assert result.size() == 3
            result.each { assert it.age >= 10 && it.age <= 40 }

            // Test between and name combined, this should not match.
            result = selectionService.select("gorm://test.TestEntity/list?name=Lisa&age=10-40")
            assert result.size() == 0

            // Test between and name combined.
            result = selectionService.select("gorm://test.TestEntity/list?name=Jason&age=10-40")
            assert result.size() == 1
            result.each { assert it.age == 11 && it.name == "Jason Average" }
        } finally {
            gormSelection.setCriteria(TestEntity, backup)
        }
        then:
        true

    }
}
