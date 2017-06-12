(function() {
    'use strict';

    var CompilationService = function($timeout, $compile, $q) {

        /**
         * Compiles directive
         *
         * @param compilationScope
         * @param directiveTag
         * @param selector
         * @param parameterMap if defined => [{attrName: "mode", attrValue: "edit"}, ...]
         */
        this.compileInsideElement = function(compilationScope, directiveTag, selector, parameterMap) {
            return $q(function(resolve, reject) {
                $timeout(function() {
                    var $el = angular.element(selector);
                    if ($el.length == 0) {
                        reject("No element for '" + selector + "' found");
                    }
                    var scope = compilationScope.$new();
                    if (parameterMap !== undefined && parameterMap !== null) {
                        directiveTag = directiveTag.replace(">", buildAttributeString(parameterMap) + ">");
                    }
                    $el.append(directiveTag);
                    $compile($el.contents())(scope);

                    resolve({
                        scope: scope,
                        element: $el
                    });

                    scope.$on('$destroy', function() {
                        $el.empty();
                    })
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
