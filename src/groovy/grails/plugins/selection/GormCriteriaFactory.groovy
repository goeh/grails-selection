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

/**
 * Provides mapping for domain classes and their associated GORM Criteria.
 * Mapped criteria can be overridden with the setCriteria() method.
 */
class GormCriteriaFactory {

    private static final Map<String, Closure> criteriaMap = new HashMap<String, Closure>()

    protected void setCriteria(Class domainClass, Closure criteria) {
        criteriaMap[domainClass.name] = criteria
    }

    /**
     * Returns the criteria associated with a domain class.
     *
     * @param clazz the domain class to get criteria for
     * @return a GORM Criteria Closure.
     */
    public Closure getCriteria(Class clazz) {
        criteriaMap[clazz.name] ?: getDefaultCriteria()
    }

    /**
     * Returns a criteria closure that perform 'ilike(property, value)' on all provided properties.
     *
     * @return default criteria
     */
    public Closure getDefaultCriteria() {
        {Map query, Map params ->
            query.each {key, value ->
                if (value) {
                    if(key == 'id') {
                        eq(key, Long.valueOf(value))
                    } else {
                        ilike(key, wildcard(value))
                    }
                }
            }
        }
    }

    /**
     * Replace '*' wildcards in a query string with '%' or append '%' if no wildcard is given.
     *
     * Asterisks '*' are replaced with percent '%'
     * If a value begins with equal sign '=' an exact match will be performed.
     * If no '*' or '=' is found, a '%' will be appended to the value, thus making "begins with" the default query behaviour.
     *
     * @param q the query value to convert
     * @return the same query value but with wildcards applied.
     */
    private String wildcard(String q) {
        q = q.toLowerCase()
        if (q.contains('*')) {
            return q.replace('*', '%')
        } else if (q[0] == '=') { // Exact match.
            return q[1..-1]
        } else { // Starts with is default.
            return q + '%'
        }
    }
}
