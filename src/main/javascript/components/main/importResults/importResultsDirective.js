(function() {
    'use strict';
    var importResults = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/importResults/importResults.html',
            controller      : ImportResultsController,
            controllerAs    : 'irc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcImportResults', ['$log', 'contextualData', importResults]);

    function ImportResultsController($scope, contextualData, dcDataFactory, $state, $stateParams, toaster, $mdDialog) {
        var irc = this;
        irc.importResults = {};
        irc.selectedImports = {};
        irc.hasResults = hasResults;
        irc.updateSelectedImports = updateSelectedImports;
        irc.reImportConnections = reImportConnections;
        irc.reImportConnection = reImportConnection;
        irc.editConnection = editConnection;
        irc.goToConnections = goToConnections;
        irc.filterFailedConnections = filterFailedConnections;
        irc.isReImportDisabled = isReImportDisabled;
        irc.importInProgress = false;
        init();

        function init() {
            var url = contextualData.context + '/modules/databaseconnector/databasetypes';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function(response) {
                if ($stateParams.results !== null) {
                    irc.importResults = $stateParams.results;
                    irc.databaseTypes = response;
                    toaster.pop({
                        type: $stateParams.status ? 'success' : 'warning',
                        title: 'Import Successful',
                        body: $stateParams.status ? 'All connections imported successfully!' : 'Not all connections were imported!',
                        toastId: 'irc',
                        timeout: 3000
                    });
                } else {
                    $state.go('connections');
                }
            }, function(response) {
                $state.go('connections');
            });
        }

        function reImportConnection(connection, $index) {
            irc.importInProgress = true;
            var url = contextualData.context + '/modules/databaseconnector/reimport/false';
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: connection
            }).then(function(response) {
                if (!_.isUndefined(response.connection)) {
                    irc.importResults[connection.databaseType].success.push(connection);
                    irc.importResults[connection.databaseType].failed[$index] = null;
                    irc.importResults[connection.databaseType].failed[$index] = connection.ignore = true;
                }
                irc.importInProgress = false;
            }, function(response) {
                irc.importInProgress = false;
            });
        }

        function reImportConnections() {
            irc.importInProgress = true;
            var url = contextualData.context + '/modules/databaseconnector/reimport/true';
            var connections = [];
            for (var databaseType in irc.selectedImports) {
                for(var i in irc.selectedImports[databaseType]) {
                    connections.push(irc.importResults[databaseType].failed[irc.selectedImports[databaseType][i]]);
                }
            }
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data:connections
            }).then(function(response) {
                var results = response.connections;
                for (var i in results.success) {
                    irc.importResults[results.success[i].connection.databaseType].success.push(results.success[i].connection);
                    results.success[i].connection.ignore = true;
                    irc.importResults[results.success[i].connection.databaseType].failed[results.success[i].reImport] = null;
                    irc.importResults[results.success[i].connection.databaseType].failed[results.success[i].reImport] = results.success[i].connection;
                }
                irc.importInProgress = false;
                clearSelectedImports();
            }, function(response) {
                irc.importInProgress = false;
                clearSelectedImports();
            });
        }

        function hasResults(databaseType) {
            return !_.isUndefined(irc.importResults[databaseType]);
        }

        function updateSelectedImports(index, databaseType) {
            if (irc.importResults[databaseType].failed[index].reImport !== null) {
                var tempSelectedImports = _.isUndefined(irc.selectedImports[databaseType]) ? [] : angular.copy(irc.selectedImports[databaseType]);
                tempSelectedImports.push(irc.importResults[databaseType].failed[index].reImport);
                irc.selectedImports[databaseType] = tempSelectedImports;
            } else {
                irc.selectedImports[databaseType].splice(index, 1);
            }
        }

        function editConnection($index, databaseType, ev) {
            var backupConnection = angular.copy(irc.importResults[databaseType].failed[$index]);
            $mdDialog.show({
                locals: {
                    connection: irc.importResults[databaseType].failed[$index]
                },
                controller: EditImportedConnectionPopupController,
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/importResults/importPopups/editImportedConnectionPopup.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: true

            }).then(function(connection){
                if (!_.isUndefined(connection) && connection !== null) {
                    irc.importResults[databaseType].failed[$index] = {};
                    irc.importResults[databaseType].failed[$index] = connection;
                }
            }, function(){
                irc.importResults[databaseType].failed[$index] = backupConnection;
            });
        }

        function goToConnections() {
            $state.go('connections');
        }
        
        function filterFailedConnections(connection) {
            return (!connection.ignore);
        }

        function isReImportDisabled() {
            for(var i in irc.selectedImports) {
                if (!_.isUndefined(irc.selectedImports[i]) && !_.isEmpty(irc.selectedImports[i])) {
                    return false;
                }
            }
            return true;
        }

        function clearSelectedImports() {
            for (var databaseType in irc.selectedImports) {
                for (var i in irc.selectedImports[databaseType]) {
                    irc.importResults[databaseType].failed[irc.selectedImports[databaseType][i]].reImport = null;
                }
            }
            irc.selectedImports = {};
        }
    }

    ImportResultsController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$state', '$stateParams', 'toaster', '$mdDialog'];

    function EditImportedConnectionPopupController($scope, $mdDialog, connection) {
        $scope.eicc = this;

        init();

        function init() {
            $scope.eicc.connection = connection;
        }
        
        $scope.$on('importConnectionClosed', function(event, connection){
            $mdDialog.hide(connection);
        });

    }

    EditImportedConnectionPopupController.$inject = ['$scope', '$mdDialog', 'connection'];
})();