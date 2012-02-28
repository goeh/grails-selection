#Grails Selection Plugin

The selection plugin provides unified selection of information.
It uses a URI based syntax to select any information from any resource.
Grails plugins can add custom search providers.

def result = selectionService.select(URI, params)

**URI examples**

- gorm://person/list?name=Gra%25
- bean://myService/method/arg
- https://dialer.mycompany.com/outbound/next?agent=liza

*LDAP support is provided by the selection-ldap plugin*

- ldap:dc=example,dc=com&filter=(objectClass=people)

*Persistent selections are provided by the selection-repository plugin*

- repo:453?offset=0&max=10&sort=lastName&order=asc

The result of invoking selectionService.selection(...) is implementation dependent,
but the 'gorm' selection always returns domain instances.