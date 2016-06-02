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

    function ImportResultsController($scope, contextualData, dcDataFactory, $state, $stateParams, toaster) {
        var irc = this;
        irc.importResults = {};
        irc.hasResults = hasResults;
        irc.updateSelectedImports = updateSelectedImports;
        irc.reImportConnections = reImportConnections;
        irc.reImportConnection = reImportConnection;
        irc.editConnection = editConnection;
        irc.goToConnections = goToConnections;
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

        function reImportConnection(connection) {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[connection.type] + '/reimport/false';
            dcDataFactory.customRequest({
                url: url,
                method: 'POST'
            }).then(function(response) {
            }, function(response) {});
        }

        function reImportConnections(connection) {
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
                var tempSelectedImports = angular.copy(irc.selectedImports);
                tempSelectedImports.push(irc.importResults[databaseType].failed[index].reImport);
                irc.selectedImports = tempSelectedImports
            } else {
                var tempSelectedImports = [];
                for (var i in  irc.selectedImports) {
                    if ( irc.selectedImports[i] !==  irc.importResults[databaseType].failed[index].reImport) {
                        tempSelectedImports.push( irc.selectedImports[i]);
                    }
                }
                 irc.selectedImports = tempSelectedImports;
            }
        }

        function editConnection() {

        }

        function goToConnections() {
            $state.go('connections');
        }
    }

    ImportResultsController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$state', '$stateParams', 'toaster'];
})();