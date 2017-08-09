(function() {
    'use strict';

    var CompilationService = function($timeout, $compile, $q, $DCSS, $DCU) {

        var compiledDirectives = {};
        var self = this;
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

        this.compileDirective = function($scope) {
            return $q(function(resolve, reject){
                if (!$scope.compiled) {
                    $timeout(function() {
                        $DCSS.getDirectivesForType($scope.cpc.selectedDatabaseType).then(function(data) {
                            var promise = self.compileInsideElement($scope, data.connectionDirective.tag, "#createConnectionContent", [
                                    {attrName:"mode", attrValue:"create"},
                                    {attrName:"database-type", attrValue:"{{cpc.selectedDatabaseType}}"},
                                    {attrName:"connection", attrValue:"cpc.connection"}
                                ]
                            );
                            promise.then(function(data) {
                                console.log(data);
                                var uuid = $DCU.generateUUID();
                                compiledDirectives[uuid] = data;
                                resolve({UUID: uuid})
                            }, function(error) {
                                console.error(error);
                                reject(error);
                            })
                        }, function(error) {
                            console.error(error);
                            reject(error);
                        });
                    });
                    $scope.compiled = true;
                }
            });
        };

        this.removeCompiledDirective = function(uuid) {
            if (uuid in compiledDirectives) {
                compiledDirectives[uuid].scope.$destroy();
                compiledDirectives[uuid].element.empty();
            }
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

    CompilationService.$inject = ['$timeout', '$compile', '$q', '$DCStateService', '$DCUtilService'];

    angular
        .module('databaseConnector')
        .service('dcCompilationService', CompilationService);
})();
