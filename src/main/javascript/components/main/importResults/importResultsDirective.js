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
                    console.log(irc);
                } else {
                    $state.go('connections');
                }
            }, function(response) {
                $state.go('connections');
            });
        }

        function reImportConnection(connection, $index) {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[connection.type] + '/reimport/false';
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
            }, function(response) {});
        }

        function reImportConnections() {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[connection.type] + '/reimport/true';
            dcDataFactory.customRequest({
                url: url,
                method: 'POST'
            }).then(function(response) {
            }, function(response) {});
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
                var tempSelectedImports = [];
                for (var i in  irc.selectedImports[databaseType]) {
                    if ( irc.selectedImports[databaseType][i] !==  irc.importResults[databaseType].failed[index].reImport) {
                        tempSelectedImports.push( irc.selectedImports[databaseType][i]);
                    }
                }
                 irc.selectedImports[databaseType] = tempSelectedImports;
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
    }

    ImportResultsController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$state', '$stateParams', 'toaster', '$mdDialog'];

    function EditImportedConnectionPopupController($scope, $mdDialog, connection) {
        $scope.eicc = this;

        init();

        function init() {
            $scope.eicc.connection = connection;
        }
        
        $scope.$on('importConnectionClosed', function(event, connection){
            console.log('connection when import edit dialog closed ', connection);
            $mdDialog.hide(connection);
        });

    }

    EditImportedConnectionPopupController.$inject = ['$scope', '$mdDialog', 'connection'];
})();