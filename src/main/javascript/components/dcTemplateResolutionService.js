(function() {
    'use strict';
    /**
     * This service will resolve the template of the corresponding directive
     *
     * @class
     * @private
     * @memberOf ff
     */
    var ResolveTemplatesService = function($log) {
        /**
         * Resolves the template path for the current directive
         *
         * @param path the path for the provided template
         * @param viewType the defined view type that will be inserted into the path
         * @returns {*}
         */
        this.resolveTemplatePath = function(path, viewType) {
            //if view type is undefined it'll be set to an empty string in order to use the default view.
            viewType = viewType !== undefined ? '.' + viewType : '';
            return path.replace('.html.ajax', viewType + '.html.ajax');
        };
    };

    ResolveTemplatesService.$inject = ['$log'];

    angular
        .module('databaseConnector', [])
        .service('dcTemplateResolver', ResolveTemplatesService);
})();
