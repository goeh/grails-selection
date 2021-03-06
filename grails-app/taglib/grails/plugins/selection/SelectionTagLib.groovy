package grails.plugins.selection

/**
 * Selection Tag Library.
 */
class SelectionTagLib {

    static namespace = "select"

    def grailsApplication
    def selectionService

    /**
     * Create a link that includes a selection URI with encoding specified in Config.groovy [selection.uri.encoding].
     * @attr selection the selection uri to include in the link, if null or not specified this tag is equivalent to g:link
     * @attr parameter parameter name, if omitted config option 'selection.uri.parameter' or 'id' is used.
     * @attr query optional Map with query values that will be appended to the URI specified by 'selection'.
     */
    def link = { attrs, body ->
        out << g.link(attrs.selection ? createLinkParams(attrs) : attrs, body)
    }

    /**
     * Create a URI that includes a selection URI with encoding specified in Config.groovy [selection.uri.encoding].
     * @attr selection the selection uri to include
     * @attr parameter parameter name, if omitted config option 'selection.uri.parameter' or 'id' is used.
     */
    def createLink = { attrs ->
        def uri = g.createLink(createLinkParams(attrs))
        out << uri
        if (!attrs.selection) {
            out << (new URI(uri.toString()).query ? '&' : '?') // TODO Is uri.toString() correct here? encoding???
            out << grailsApplication.config.selection.uri.parameter ?: 'q'
            out << '='
            // No selection specified so value is set to empty/null
        }
    }

    /**
     * Encode selection URI with encoding specified in Config.groovy [selection.uri.encoding].
     */
    def encode = { attrs ->
        def uri = attrs.selection
        if (!uri) {
            throwTagError("Tag [encode] is missing required attribute [selection]")
        }
        if (!(uri instanceof URI)) {
            uri = new URI(uri.toString())
        }
        out << selectionService.encodeSelection(uri)
    }

    def hiddenField = { attrs ->
        def uri = attrs.selection
        if (uri && !(uri instanceof URI)) {
            uri = new URI(uri.toString())
        }
        out << '<input type="hidden" name="'
        out << (attrs.name ?: 'selection')
        out << '" value="'
        if(uri) {
            out << selectionService.encodeSelection(uri)
        }
        out << '"/>'
    }

    private Map createLinkParams(Map attrs) {
        def linkParams = [:]
        linkParams.putAll(attrs)
        def uri = linkParams.remove('selection')
        if (uri) {
            def params = linkParams.params
            if (!params) {
                params = linkParams.params = [:]
            }
            def id = linkParams.remove('id')
            if (id != null) {
                params.id = id
            }
            def query = linkParams.remove('query')
            if(query) {
                uri = selectionService.addQuery(uri, query)
            }
            params.putAll(selectionService.createSelectionParameters(uri, linkParams.remove('parameter')))
        }
        return linkParams
    }
}
