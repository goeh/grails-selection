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


    String convert2(List words, Map conversionParams, Map otherParams) {
        convert1(words.join(otherParams.separator ?: ' '), conversionParams, otherParams)
    }
}
