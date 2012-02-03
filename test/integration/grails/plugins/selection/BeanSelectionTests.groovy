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

    void testNonExistingBen() {
        shouldFail(NoSuchBeanDefinitionException) {
            selectionService.select("bean://nonExistingBean/foo")
        }
    }

    void testNonExistingMethod() {
        shouldFail(MissingMethodException) {
            selectionService.select("bean://selectionTestService/nonExistingMethod")
        }
    }

    void testNoArguments() {
        assert selectionService.select("bean://selectionTestService/hello") == "Hello World"
    }

    void testOneArgument() {
        assert selectionService.select("bean://selectionTestService/echo/Grails+Rocks") == "Grails Rocks"
    }

    void testMultipleArguments() {
        assert selectionService.select("bean://selectionTestService/join/2012/01/31") == "2012-01-31"
    }

    void testListAndMapArguments() {
        assert selectionService.select("bean://selectionTestService/join/2012/01/31?separator=%2B") == "2012+01+31"
    }
}
