# Grails Selection Plugin

The selection plugin provides unified selection of information.
It uses a URI based syntax to select any information from any resource.
Grails plugins can add custom search providers.

def result = selectionService.select(URI, params)

**URI examples**
- gorm://person/list?name=Gra%25
- bean://myService/method/arg
- http://api.example.com/rest/events?system=42

*LDAP support is provided by the selection-ldap plugin*
- ldap:dc=example,dc=com&filter=(objectClass=people)
