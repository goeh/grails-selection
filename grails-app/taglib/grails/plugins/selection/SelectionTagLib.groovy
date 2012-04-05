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

        def uri = attrs.remove('selection')
        if (!uri) {
            throwTagError("Tag [link] is missing required attribute [selection]")
        }
        def uriParameterName = attrs.remove('parameter')
        if (uriParameterName == null) {
            uriParameterName = grailsApplication.config.selection.uri.parameter ?: 'id'
        }
        def value = selectionService.encodeSelection(uri)
        def params = attrs.params
        if(! params) {
            params = attrs.params = [:]
        }
        def id = attrs.remove('id')
        if(id != null) {
            params.id = id
        }
        params[uriParameterName] = value

        out << g.link(attrs, body)
    }
}
