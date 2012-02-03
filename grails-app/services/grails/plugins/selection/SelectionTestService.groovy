package grails.plugins.selection

/**
 * This service is used during integration tests only.
 */
class SelectionTestService {

    static transactional = false

    String hello() {
        "Hello World"
    }

    Object echo(Object arg) {
        arg
    }

    String join(Map arg) {
        arg.keySet().sort().join('+')
    }

    String join(List arg) {
        arg.join('-')
    }

    String join(List arg, Map params) {
        arg.join(params.separator)
    }
}
