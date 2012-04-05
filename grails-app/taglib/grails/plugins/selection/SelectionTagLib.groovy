package grails.plugins.selection

/**
 * Selection Tag Library.
 */
class SelectionTagLib {

    static namespace = "selection"

    def grailsApplication
    def selectionService

    /**
     * Create a link that includes a selection URI with encoding specified in Config.groovy.
     * @attr REQUIRED selection the selection uri to include in the link
     * @attr parameter parameter name, if omitted config option 'selection.uri.parameter' or 'id' is used.
     */
    def link = {attrs, body ->
        if (!attrs.selection) {
            throwTagError("Tag [link] is missing required attribute [selection]")
        }
        out << g.link(createLinkParams(attrs), body)
    }

    /**
     * Create a URI that includes a selection URI with encoding specified in Config.groovy.
     * @attr REQUIRED selection the selection uri to include
     * @attr parameter parameter name, if omitted config option 'selection.uri.parameter' or 'id' is used.
     */
    def createLink = {attrs ->
        if (!attrs.selection) {
            throwTagError("Tag [createLink] is missing required attribute [selection]")
        }
        out << g.createLink(createLinkParams(attrs))
    }

    private Map createLinkParams(Map attrs) {
        def linkParams = [:]
        linkParams.putAll(attrs)
        def uri = linkParams.remove('selection')
        def uriParameterName = linkParams.remove('parameter')
        if (uriParameterName == null) {
            uriParameterName = grailsApplication.config.selection.uri.parameter ?: 'id'
        }
        def value = selectionService.encodeSelection(uri)
        def params = linkParams.params
        if (!params) {
            params = linkParams.params = [:]
        }
        def id = linkParams.remove('id')
        if (id != null) {
            params.id = id
        }
        params[uriParameterName] = value

        return linkParams
    }
}
