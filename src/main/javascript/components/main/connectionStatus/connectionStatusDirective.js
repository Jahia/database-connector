(function() {
    'use strict';
    var connectionStatus = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/connectionStatus.html',
            controller      : ConnectionStatusController,
            controllerAs    : 'csc',
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcConnectionStatus', ['$log', 'contextualData', connectionStatus]);

    function ConnectionStatusController($scope, $state, $stateParams, dcConnectionStatusService, $DCCS, $DCSS) {
        var csc = this;
        csc.connectionStatus = connectionStatus;
        csc.goToConnections = goToConnections;

        $scope.$on('$destroy', function() {
            //disable the connection status service
            dcConnectionStatusService.disable();
        });

        csc.$onInit = function() {
            init();
            $DCSS.getDirectivesForType(csc.connection.databaseType).then(function(data) {
                var directives = data.statusDirectives;
                if(!_.isEmpty(directives)) { console.log(directives);
                    var $compilerStatusContainer = angular.element('#statusCompiler');
                    //Compile overview first
                    for (var i = directives.length - 1; i >=0; i--) {
                        console.log(directives[i]);
                        if (directives[i].name == 'overview') {
                            $compilerStatusContainer.append(createStatusColumnContainer(directives[i].name));
                            $DCCS.compileInsideElement($scope, directives[i].tag, '#' + directives[i].name);
                            directives.splice(i, 1);
                            break;
                        }
                    }
                    //Compile rest of directives in the order they appear
                    for (var i = directives.length - 1; i >=0; i--){

                        var parameters = [{
                                attrName: 'chart-height',
                                attrValue: "'320px'"
                            },
                            {
                                attrName: 'chart-width',
                                attrValue: "'100%'"
                            }
                        ];
                        $compilerStatusContainer.append(createStatusColumnContainer(directives[i].name));
                        $DCCS.compileInsideElement($scope, directives[i].tag, '#' + directives[i].name, parameters);
                    }
                }
                $DCCS.compileInsideElement($scope, '',  '#statusCompiler');
            });

            function createStatusColumnContainer(id) {
                var $div = jQuery(document.createElement('div'));
                $div.attr('flex', 100);
                var $boxWrapper = jQuery(document.createElement('div'));
                $boxWrapper.addClass('box-wrap');
                $boxWrapper.attr('id', id);
                $div.append($boxWrapper);
                console.log($div);
                return $div;
            }
        };

        function init() {
            if (!$stateParams.connection) {
                goToConnections();
            } else {
                csc.connection = $stateParams.connection;
                if (!csc.connection.canRetrieveStatus) {
                    //verify that we can retrieve the request, else we redirect.
                    goToConnections();
                }
                dcConnectionStatusService.enable(csc.connection.id, csc.connection.databaseType);
            }
        }

        function goToConnections() {
            $state.go('connections');
        }

    }

    ConnectionStatusController.$inject = ['$scope', '$state',
        '$stateParams', 'dcConnectionStatusService', 'dcCompilationService', '$DCStateService'];
})();