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

import junit.*
import org.springframework.beans.factory.NoSuchBeanDefinitionException

class BeanSelectionTests extends GroovyTestCase {

    def selectionService

    /**
     * Expect exception when referencing non-existing bean.
     */
    void testNonExistingBen() {
        shouldFail(NoSuchBeanDefinitionException) {
            selectionService.select("bean://nonExistingBean/foo")
        }
    }

    /**
     * Expect exception when referencing non-existing method.
     */
    void testNonExistingMethod() {
        shouldFail(MissingMethodException) {
            selectionService.select("bean://selectionTestService/nonExistingMethod")
        }
    }

    /**
     * Test method with no arguments.
     */
    void testNoArguments() {
        assert selectionService.select("bean://selectionTestService/hello") == "Hello World"
    }

    /**
     * Test method with one argument.
     */
    void testOneArgument() {
        assert selectionService.select("bean://selectionTestService/echo/Grails+Rocks") == "Grails Rocks"
    }

    /**
     * Test method that takes a List argument.
     */
    void testMultipleArguments() {
        assert selectionService.select("bean://selectionTestService/join/2012/01/31") == "2012-01-31"
    }

    /**
     * Test method that takes two arguments, a List and a Map.
     */
    void testListAndMapArguments() {
        assert selectionService.select("bean://selectionTestService/join/2012/01/31?separator=%2B") == "2012+01+31"
        assert selectionService.select("bean://selectionTestService/join/2012/01/31", [separator: '+']) == "2012+01+31"
    }

    /**
     * Test method that takes three arguments, an Object or List and two Maps.
     */
    void testListAndMapAndParams() {
        // Test method signature (Object, Map, Map)
        assert selectionService.select("bean://selectionTestService/convert1/Hello?type=uppercase", [repeat: 5]) == "HELLOHELLOHELLOHELLOHELLO"
        // Test method signature (List, Map, Map)
        assert selectionService.select("bean://selectionTestService/convert2/Hello/World?type=lowercase", [repeat: 2]) == "hello worldhello world"
    }

    /**
     * Test URL encoded arguments.
     */
    void testUrlEncoding() {
        assert selectionService.select("bean://selectionTestService/join/%3C/%3E?separator=%C3%B6") == "<ö>"
    }
}
