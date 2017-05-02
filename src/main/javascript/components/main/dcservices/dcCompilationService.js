(function() {
    'use strict';

    var CompilationService = function($timeout, $compile, $q) {

        /**
         * Compiles directive
         *
         * @param compilationScope
         * @param directiveTag
         * @param elementId
         * @param parameterMap if defined => [{attrName: "mode", attrValue: "edit"}, ...]
         */
        this.compileInsideElement = function(compilationScope, directiveTag, elementId, parameterMap) {
            return $q(function(resolve, reject) {
                $timeout(function() {
                    var el = document.getElementById(elementId);
                    if (el === undefined || el === null) {
                        reject("No element with id '" + elementId + "' found");
                    }
                    var element = angular.element(document.getElementById(elementId));

                    var scope = compilationScope.$new();
                    if (parameterMap !== undefined && parameterMap !== null) {
                        directiveTag = directiveTag.replace(">", buildAttributeString(parameterMap) + ">");
                    }
                    element.html(directiveTag);

                    $compile(element.contents())(scope);

                    resolve({
                        scope: scope,
                        element: element
                    });
                });
            });
        };

        function buildAttributeString(parameterMap) {
            var str = "";
            for (var index in parameterMap) {
                var attr = parameterMap[index];
                str += " " + attr.attrName + "=\"" + attr.attrValue + "\"";
            }
            return str;
        }
    };

    CompilationService.$inject = ['$timeout', '$compile', '$q'];

    angular
        .module('databaseConnector')
        .service('dcCompilationService', CompilationService);
})();
