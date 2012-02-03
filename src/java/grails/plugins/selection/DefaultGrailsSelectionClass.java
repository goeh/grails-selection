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

package grails.plugins.selection;

import java.net.URI;
import java.util.Map;

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;

/**
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
public class DefaultGrailsSelectionClass extends AbstractInjectableGrailsClass implements GrailsSelectionClass {

    public DefaultGrailsSelectionClass(Class clazz) {
        super(clazz, GrailsSelectionClass.TYPE);
    }

    public boolean supports(URI uri) {
        return ((Boolean)getMetaClass().invokeMethod(getReferenceInstance(), SUPPORTS, new Object[]{uri})).booleanValue();
    }
    
    public void select(URI uri, Map params) {
        getMetaClass().invokeMethod(getReferenceInstance(), SELECT, new Object[]{uri, params});
    }

}
