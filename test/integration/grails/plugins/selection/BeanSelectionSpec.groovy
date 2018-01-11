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
import org.springframework.beans.factory.NoSuchBeanDefinitionException

class BeanSelectionSpec extends IntegrationSpec {

    def selectionService

    /**
     * Expect exception when referencing non-existing bean.
     */
    def testNonExistingBen() {
        when:
        selectionService.select("bean://nonExistingBean/foo")
        then:
        thrown(NoSuchBeanDefinitionException)
    }

    /**
     * Expect exception when referencing non-existing method.
     */
    def testNonExistingMethod() {
        when:
        selectionService.select("bean://selectionTestService/nonExistingMethod")
        then:
        thrown(MissingMethodException)
    }

    /**
     * Expect exception when referencing a method not annotated with @Selectable.
     */
    def testNotSelectableMethod() {
        when:
        selectionService.select("bean://selectionTestService/notSelectable1")
        then:
        thrown(SecurityException)
    }

    def testAllMethodsSelectable() {
        given:
        SelectionTestService.selectable = true
        when:
        selectionService.select("bean://selectionTestService/notSelectable1")
        SelectionTestService.selectable = null
        then:
        SelectionTestService.selectable == null
    }

    def testOneMethodSelectable() {
        given:
        SelectionTestService.selectable = 'notSelectable1'
        when:
        selectionService.select("bean://selectionTestService/notSelectable1")
        then:
        true
        when:
        selectionService.select("bean://selectionTestService/notSelectable2")
        then:
        thrown(SecurityException)
        when:
        SelectionTestService.selectable = null
        then:
        SelectionTestService.selectable == null
    }

    def testTwoMethodsSelectable() {
        given:
        SelectionTestService.selectable = ['notSelectable1', 'notSelectable2']
        when:
        selectionService.select("bean://selectionTestService/notSelectable1")
        selectionService.select("bean://selectionTestService/notSelectable2")
        then:
        true
        when:
        selectionService.select("bean://selectionTestService/notSelectable3")
        then:
        thrown(SecurityException)
        when:
        SelectionTestService.selectable = null
        then:
        SelectionTestService.selectable == null
    }

    /**
     * Test method with no arguments.
     */
    def testNoArguments() {
        expect:
        selectionService.select("bean://selectionTestService/hello") == "Hello World"
    }

    /**
     * Test method with one argument.
     */
    def testOneArgument() {
        expect:
        selectionService.select("bean://selectionTestService/echo/Grails+Rocks") == "Grails Rocks"
    }

    /**
     * Test method that takes a List argument.
     */
    def testMultipleArguments() {
        expect:
        selectionService.select("bean://selectionTestService/join/2012/01/31") == "2012-01-31"
    }

    /**
     * Test method that takes two arguments, a List and a Map.
     */
    def testListAndMapArguments() {
        expect:
        selectionService.select("bean://selectionTestService/join/2012/01/31?separator=%2B") == "2012+01+31"
        selectionService.select("bean://selectionTestService/join/2012/01/31", [separator: '+']) == "2012+01+31"
    }

    /**
     * Test method that takes three arguments, an Object or List and two Maps.
     */
    def testListAndMapAndParams() {
        // Test method signature (Object, Map, Map)
        expect:
        selectionService.select("bean://selectionTestService/convert1/Hello?type=uppercase", [repeat: 5]) == "HELLOHELLOHELLOHELLOHELLO"
        // Test method signature (List, Map, Map)
        selectionService.select("bean://selectionTestService/convert2/Hello/World?type=lowercase", [repeat: 2]) == "hello worldhello world"
    }

    /**
     *
     */
    def testRepeatedParams() {
        expect:
        selectionService.select("bean://selectionTestService/list?items=Foo&items=Bar") == "Foo+Bar"
    }

    /**
     * Test URL encoded arguments.
     */
    def testUrlEncoding() {
        expect:
        selectionService.select("bean://selectionTestService/join/%3C/%3E?separator=%C3%B6") == "<ö>"
    }
}
