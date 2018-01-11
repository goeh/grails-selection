package grails.plugins.selection

/**
 * This service is used during integration tests only.
 */
class SelectionTestService {

    static transactional = false
    static selectable = null

    String notSelectable1() {
        "This method cannot be called by beanSelection because it's not annotated with @Selectable"
    }

    String notSelectable2() {
        "This method cannot be called by beanSelection because it's not annotated with @Selectable"
    }

    String notSelectable3() {
        "This method cannot be called by beanSelection because it's not annotated with @Selectable"
    }

    @Selectable
    String hello() {
        "Hello World"
    }

    @Selectable
    Object echo(Object arg) {
        arg
    }

    @Selectable
    String join(Map arg) {
        arg.keySet().sort().join('+')
    }

    @Selectable
    String join(List arg) {
        arg.join('-')
    }

    @Selectable
    String join(List arg, Map params) {
        arg.join(params.separator)
    }

    @Selectable
    String convert1(String word, Map conversionParams, Map otherParams) {
        switch (conversionParams.type) {
            case 'uppercase':
                word = word.toUpperCase()
                break
            case 'lowercase':
                word = word.toLowerCase()
                break
        }
        if (conversionParams.reverse) {
            word = word.reverse()
        }
        StringBuilder s = new StringBuilder()
        int n = otherParams.repeat ?: 1
        n.times {
            s << word
        }
        return s.toString()
    }

    @Selectable
    String convert2(List words, Map conversionParams, Map otherParams) {
        convert1(words.join(otherParams.separator ?: ' '), conversionParams, otherParams)
    }

    @Selectable
    String list(Map<String, Object> query) {
        query.items.join('+')
    }
}
