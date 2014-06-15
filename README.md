# Grails Selection Plugin

The selection plugin provides a unified method to query information.
It uses a URI based syntax to query any information from any resource. 

Two requirements motivated this plugin:

1. Make it easy to query domain instances without the need to import the domain class being queried
2. Make it possible to save a query for later use. No matter if the query was a simple *findBy* query
or a complex *Criteria* query, or any other type of query.

A URI based standard for query expressions makes both requirements possible.
A query is expressed as a URI and contains all necessary information to perform
the query at any time anywhere in the application.

In most use-cases you implement the actual query logic in a grails service method.
Then you create a query URI that includes the service, method and parameters.
Then you call *selectionService.select(URI)* to perform the query and get the result.

    def query = new URI("bean:personService/list?name=A*")
    def people = selectionService.select(query, [offset:0, max: 10])

This means that to perform a query anywhere in the application you only need
an injected SelectionService and a query URI.

Other grails plugins can add custom selection providers. The basic *selection* plugin contains
three selection providers: *bean*, *gorm* and *proxy* that will be described below.

The [GR8 CRM ecosystem](http://gr8crm.github.io) makes extensive use of the selection plugin.
Each GR8 CRM plugin focuses on one specific domain, for example *contact*, *project* or *document*.
Each plugin defines a [Bounded Context](http://martinfowler.com/bliki/BoundedContext.html)
and has minimal dependencies on other GR8 CRM plugins. 
Each plugin implements the query logic for it's domain model in a service.
Anywhere, from the application or from a plugin a query can be executed without
the need to import the domain class being queried. The only objects needed are
a SelectionService instance and a URI that contains the query expression.
This avoids tight coupling between plugins but still makes it possible to query
almost any information.

**URI examples**

- gorm://person/list?name=Gra%25
- bean://myService/method/arg
- https://dialer.mycompany.com/outbound/next?agent=liza

The result of invoking selectionService.selection(...) is implementation dependent,
but typically the result is an instance of PagedResult or ArrayList.
The 'gorm' selection always returns a List of domain instances.

## Default Selection Providers

### GORM Selection

The **gorm** selection handler provides standard GORM list() and get() queries.

Examples:

- gorm://grails.plugins.crm.contact.CrmContact/list?lastName=Anderson
- gorm://crmContact/list?firstName=Sven&lastName=Anderson
- gorm://demo.Book/get?id=42

This it very powerful and flexible but it also introduces a security risk if you put this tool
in the hands of your users (like accepting query URI:s as request parameters).
So to enable gorm selections you must configure (in Config.groovy) a white list of domain classes
that you want to use with *gorm:* selections.

    selection.gorm.true // No restrictions, all domain classes are selectable (use with care).
    selection.gorm.com.mycompany.Person // Allow gorm selection on Person domain class only
    selection.gorm.com.mycompany // Allow gorm selections on all domain classes in package com.mycompany

####Fixed criteria

You can also restrict *gorm:* selections with a fixed criteria that will always be added to the query.

Example: With this code in *BootStrap.groovy* every *gorm:* selection will be filtered on current user (Apache Shiro).

    class BootStrap {
        
        def gormSelection
        
        def init = { servletContext ->
            gormSelection.fixedCriteria = { query, params ->
                eq('username', SecurityUtils.subject.principal)
            }
        }
    }

### Bean Selection

The **bean** selection handler makes it possible to call a method on a Spring bean.
For security reasons the method to call must be annotated with *@grails.plugins.selection.Selectable*.

    class CustomerService {
      ...
      @Selectable
      def list(Map query, Map params) {
        Customer.createCriteria().list(params) {
          if(query.customerNo) {
            eq('customerNo', query.customerNo)
          }
          if(query.name) {
            ilike('name', query.name.replace('*', '%'))
          }
        }
      }

Examples:

- bean:customerService/list?name=Acme+Inc.&offset=0&max=25
- bean:crmContactService/list?name=Fred*&state=CA&offset=50&max=25
- bean://logService/getEvents?status=new
- bean://anotherBean/getSomething/arg1/arg2/arg3
- bean://erpIntegrationBean/getInvoices?date=%3E2014-06-15

### Proxy Selection

The proxy selection uses a normal URL with *http:*, *https:*, *ftp:* or *file:* scheme to retrieve a URI string.
The returned URI will then be used to perform the actual query via *SelectionService*.

Examples:

- https://dialer.mycompany.com/call/outbound?agent=liza
- http://localhost:8080/myapp/selection/453

## LDAP Selection Plugin

LDAP support is provided by the [selection-ldap](https://github.com/goeh/grails-selection-ldap) plugin.

Examples:

- ldap:dc=example,dc=com&filter=(objectClass=people)

## Persistent Selections Plugin

Queries can be saved for later use with the [selection-repo](https://github.com/goeh/grails-selection-repo) plugin.

Example 1 - save a query and use the returned URI in another part of the application to execute the query:

    def query = new URI("gorm://person/list?name=A*")
    def uri = selectionRepositoryService.put(query, "person", null, "People who's name begins with A")
    
    Later...
    def result = selectionService.select(uri, [offset:0, max: 25])

Example 2 - save a query and retrieve it later to execute the query:

    def query = new URI("gorm://person/list?name=David+Johnson")
    selectionRepositoryService.put(query, "person", null, "David Johnson")
    
    Later...
    def savedQueries = selectionRepositoryService.list("person")
    def davidQuery = savedQueries.first().uri
    def result = selectionService.select(davidQuery, [offset:0, max: 25])
    assert result.size() == 1
    assert result[0].name == "David Johnson"

## Develop your own custom selection provider

The selection plugin uses standard Grails artifacts as selection providers.
So if you want to add a custom provider, just create a a Groovy class in *grails-app/selection*.
The class name must end with "Selection" and have two methods:

**boolean supports(URI uri)**

This method must return true if the selection provider supports the given URI.


**def select(URI uri, Map params)**

This method is responsible for returning the query result.
It could perform the query itself, or delegate to another service.
The *URI* parameter contains the query expression and the *Map* can contain parameters for pagination and sorting.
