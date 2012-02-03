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

import test.TestEntity

/**
 * Integration test for the GORM Selection Provider (GormSelection.groovy).
 * @author Goran Ehrsson
 */
public class GormSelectionTests extends GroovyTestCase {

    def selectionService
    def gormSelection

    void testGet() {

        def contact = new TestEntity(number:"1", name: "Foo").save(flush:true)
        assert contact != null

        def result = selectionService.select("gorm://test.TestEntity/get?id=${contact.id}")
        assert result != null
        assert result.name == "Foo"
    }

    void testList() {

        new TestEntity(number:"1", name: "Foo").save()
        new TestEntity(number:"2", name: "Bar").save()
        new TestEntity(number:"3", name: "Baz").save()

        def result = selectionService.select("gorm://test.TestEntity/list?name=Ba", [max:10])
        assert result != null
        assert result.size() == 2
        assert result.totalCount == 2
    }

    void testDomainShortName() {
        10.times{
            new TestEntity(number:"$it", name: "Number $it").save()
        }
        assert selectionService.select("gorm://testEntity/list").size() == 10
    }

    void testRandom() {

        new TestEntity(number:"1", name: "One").save(flush:true)
        new TestEntity(number:"2", name: "Two").save(flush:true)
        new TestEntity(number:"3", name: "Three").save(flush:true)
        new TestEntity(number:"4", name: "Four").save(flush:true)
        new TestEntity(number:"5", name: "Five").save(flush:true)
        new TestEntity(number:"6", name: "Six").save(flush:true)
        new TestEntity(number:"7", name: "Seven").save(flush:true)
        new TestEntity(number:"8", name: "Eight").save(flush:true)
        new TestEntity(number:"9", name: "Nine").save(flush:true)
        new TestEntity(number:"10", name: "Ten").save(flush:true)

        def result = selectionService.select("gorm://test.TestEntity/random")
        assert result != null
        assert result instanceof TestEntity

        result = selectionService.select("gorm://test.TestEntity/random", [max:5])
        assert result.size() == 5
    }

    void testRandomHalf() {
        100.times{
            new TestEntity(number:"$it", name: "Number $it").save()
        }
        def result = selectionService.select("gorm://test.TestEntity/random", [max:50])
        assert result.size() == 50
    }

    void testRandomTough() {
        200.times{
            new TestEntity(number:"$it", name: "Number $it").save()
        }
        def result = selectionService.select("gorm://test.TestEntity/random", [max:199])
        assert result.size() == 199
    }

    void testRandomAll() {
        100.times{
            new TestEntity(number:"$it", name: "Number $it").save()
        }
        def result = selectionService.select("gorm://test.TestEntity/random", [max:100])
        assert result.size() == 100
    }

    void testRandomWithCriteria() {
        new TestEntity(number:"1", name: "One").save(flush:true)
        new TestEntity(number:"2", name: "Two").save(flush:true)
        new TestEntity(number:"3", name: "Three").save(flush:true)
        new TestEntity(number:"4", name: "Four").save(flush:true)
        new TestEntity(number:"5", name: "Five").save(flush:true)
        new TestEntity(number:"6", name: "Six").save(flush:true)
        new TestEntity(number:"7", name: "Seven").save(flush:true)
        new TestEntity(number:"8", name: "Eight").save(flush:true)
        new TestEntity(number:"9", name: "Nine").save(flush:true)
        new TestEntity(number:"10", name: "Ten").save(flush:true)
        new TestEntity(number:"11", name: "Eleven").save(flush:true)
        new TestEntity(number:"12", name: "Twelve").save(flush:true)
        new TestEntity(number:"13", name: "Thirteen").save(flush:true)
        def result = selectionService.select("gorm://test.TestEntity/random?" + "name=T%".encodeAsURL(), [max:3])
        assert result.size() == 3
        result.each{assert it.name.startsWith('T')}
    }

    void testRandomFixedCriteria() {
        new TestEntity(number:"1", name: "One").save(flush:true)
        new TestEntity(number:"2", name: "Two").save(flush:true)
        new TestEntity(number:"3", name: "Three").save(flush:true)
        new TestEntity(number:"4", name: "Four").save(flush:true)
        new TestEntity(number:"5", name: "Five").save(flush:true)
        new TestEntity(number:"6", name: "Six").save(flush:true)
        new TestEntity(number:"7", name: "Seven").save(flush:true)
        new TestEntity(number:"8", name: "Eight").save(flush:true)
        new TestEntity(number:"9", name: "Nine").save(flush:true)
        new TestEntity(number:"10", name: "Ten").save(flush:true)
        new TestEntity(number:"11", name: "Eleven").save(flush:true)
        new TestEntity(number:"12", name: "Twelve").save(flush:true)
        new TestEntity(number:"13", name: "Thirteen").save(flush:true)

        gormSelection.fixedCriteria = { query, params->
            ilike('name', 'T%')
        }

        def result = selectionService.select("gorm://test.TestEntity/random", [max:3])
        assert result.size() == 3
        result.each{assert it.name.startsWith('T')}
    }
}
