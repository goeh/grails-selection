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
 * A selection handler that support standard GORM list() and get() queries.
 *
 * Examples:
 * gorm://grails.plugins.crm.contact.CrmContact/list?lastName=Anderson
 * gorm://crmContact/list?firstName=Sven&lastName=Anderson
 * gorm://demo.Book/get?id=42
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
class GormSelection {

    def grailsApplication

    //GormCriteriaFactory criteriaFactory = new GormCriteriaFactory()
    def selectionCriteriaFactory
    
    /*
     * Criteria applied to all list queries.
     * Please note that /get queries do not use the fixed criteria.
     * Authorization and/or tenant filters must always be applied by calling code when using /get.
     */
    Closure fixedCriteria

    /**
     * This selection handler support all gorm: queries.
     * @param uri the URI to check support for
     * @return true if uri.scheme is 'gorm'
     */
    boolean supports(URI uri) {
        return uri?.scheme == 'gorm'
    }

    def select(URI uri, Map params) {
        if (uri == null) {
            throw new IllegalArgumentException("mandatory parameter [uri] is null")
        }
        if (!supports(uri)) {
            throw new IllegalArgumentException("URI scheme [${uri.scheme}] is not supported by this selection handler")
        }
        def clazz = getDomainClass(uri.host)
        def method = uri.path?.decodeURL()
        def query = SelectionUtils.queryAsMap(uri.query)
        switch (method) {
            case '/list':
                return doList(clazz, query, params)
            case '/get':
                return doGet(clazz, query, params)
            case '/random':
                return doRandom(clazz, query, params)
            default:
                throw new IllegalArgumentException("$method: unknown method")
        }
    }

    /**
     * Find a domain class in application context.
     *
     * @param name domain property name i.e. "homeAddress" or class name "com.mycompany.HomeAddress"
     */
    private Class getDomainClass(String name) {
        def domain = grailsApplication.domainClasses.find {it.propertyName == name}
        if (domain) {
            domain = domain.clazz
        } else {
            domain = grailsApplication.classLoader.loadClass(name)
        }
        return domain
    }

    /**
     * Perform a normal GORM Domain.get(primary-key) request.
     *
     * @param clazz domain class
     * @param query query.id will be used to get the record
     * @param params not currently used
     * @return domain instance
     */
    private Object doGet(Class clazz, Map query, Map params) {
        if (!query.id) {
            throw new IllegalArgumentException("/get is missing query parameter [id]")
        }
        clazz.get(query.id)
    }

    /**
     * Standard GORM list() using Criteria.
     *
     * @param clazz the domain class to query
     * @param query ilike(key, value) will be applied for all values in the map
     * @param params parameters sent to list(), i.e. offset, max, sort...
     * @return result of the query operation
     */
    private List doList(Class clazz, Map query, Map params) {
        def crit = getCriteria(clazz)
        def backupDelegate = crit.delegate
        def backupStrategy = crit.resolveStrategy
        def result
        try {
            result = clazz.createCriteria().list(params) {
                if (fixedCriteria) {
                    def tmp = fixedCriteria.clone()
                    tmp.delegate = delegate
                    tmp.resolveStrategy = Closure.DELEGATE_FIRST
                    tmp.call(query, params)
                }
                crit.delegate = delegate
                crit.resolveStrategy = Closure.DELEGATE_FIRST
                crit.call(query, params)
            }
        } finally {
            crit.delegate = backupDelegate
            crit.resolveStrategy = backupStrategy
        }
        return result
    }

    /**
     * Pick one or many random record(s) from
     * @param clazz
     * @param query
     * @param params
     * @return
     */
    private Object doRandom(Class clazz, Map query, Map params) {
        def result
        // Wanted number of records.
        def nbr = params.max
        if (nbr) {
            nbr = Integer.valueOf(nbr)
        } else {
            nbr = 1
        }
        // Get primary keys.
        def crit = getCriteria(clazz)
        def ids = clazz.withCriteria {
            projections {
                property('id')
            }
            if (fixedCriteria) {
                def tmp = fixedCriteria.clone()
                tmp.delegate = delegate
                tmp.resolveStrategy = Closure.DELEGATE_FIRST
                tmp.call(query, params)
            }
            crit.delegate = delegate
            crit.resolveStrategy = Closure.DELEGATE_FIRST
            crit.call(query, params)
        }
        if (nbr > ids.size()) {
            log.warning("Requested $nbr records but total size is ${ids.size()}, returning ${ids.size()} records")
            nbr = ids.size()
        }
        def random = new Random(System.currentTimeMillis())
        if (nbr > 1) {
            if (nbr < ids.size()) {
                // Return wanted number of records, eliminate duplicates.
                result = new ArrayList(nbr)
                for (int i = 0; i < nbr; i++) {
                    def idx = random.nextInt(ids.size())
                    def id = ids.remove(idx)
                    result << clazz.get(id)
                }
            } else {
                Collections.shuffle(ids, random) // All records wanted! Stupid user? Let's return them shuffled.
                result = ids.collect {clazz.get(it)} // Number of records wanted is greater or equal to total size.
            }
        } else if (nbr > 0) {
            // Only one record wanted, return it.
            def idx = random.nextInt(ids.size())
            result = clazz.get(ids[idx])
        }
        return result
    }

    private Closure getCriteria(Class clazz) {
        selectionCriteriaFactory.getCriteria(clazz)
    }

    public void setCriteria(Class clazz, Closure criteria)
    {
        selectionCriteriaFactory.setCriteria(clazz, criteria)
    }
}
