(function() {
    'use strict';
    var importResults = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/importResults/importResults.html',
            controller      : ImportResultsController,
            controllerAs    : 'irc',
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcImportResults', ['$log', 'contextualData', importResults]);

    function ImportResultsController($scope, contextualData, dcDataFactory,
                                     $state, $stateParams, toaster, $mdDialog, i18n,
                                     $DCSS) {
        var irc = this;
        irc.importResults = {};
        irc.selectedImports = {};
        irc.importInProgress = false;
        
        irc.hasResults = hasResults;
        irc.updateSelectedImports = updateSelectedImports;
        irc.reImportConnections = reImportConnections;
        irc.reImportConnection = reImportConnection;
        irc.editConnection = editConnection;
        irc.goToConnections = goToConnections;
        irc.isReImportDisabled = isReImportDisabled;
        irc.getFormattedImportHeader = getFormattedImportHeader;
        irc.getMessage = i18n.message;
        irc.connectorsMetaData = $DCSS.connectorsMetaData;
        irc.$onInit = function() {
          init();
        };

        function init() {
            var url = contextualData.context + '/modules/databaseconnector/databasetypes';
            if ($stateParams.results !== null) {
                irc.importResults = $stateParams.results;
                toaster.pop({
                    type: $stateParams.status ? 'success' : 'error',
                    title: i18n.message('dc_databaseConnector.toast.title.importStatus'),
                    body: $stateParams.status ? i18n.message('dc_databaseConnector.toast.message.connectionsImportedSuccessfully') : i18n.message('dc_databaseConnector.toast.message.connectionsImportIncomplete'),
                    toastId: 'irc',
                    timeout: 3000
                });
            } else {
                $state.go('connections');
            }
        }

        function reImportConnection(connection, $index) {
            irc.importInProgress = true;
            var url = contextualData.apiUrl + '/reimport/false';
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: connection
            }).then(function(response) {
                if (!_.isUndefined(response.success)) {
                    irc.importResults[connection.databaseType].success.push(connection);
                    irc.importResults[connection.databaseType].failed.splice($index, 1);
                }
                clearSelectedImports();
                irc.importInProgress = false;
            }, function(response) {
                irc.importInProgress = false;
                clearSelectedImports();
            });
        }

        function reImportConnections() {
            irc.importInProgress = true;
            var url = contextualData.apiUrl + '/reimport/true';
            var connections = [];
            for (var databaseType in irc.selectedImports) {
                for(var i in irc.selectedImports[databaseType]) {
                    connections.push(irc.importResults[databaseType].failed[i]);
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
                    irc.importResults[results.success[i].connection.databaseType].failed[results.success[i].reImport].omit = true;
                }
                for (var dataType in irc.importResults) {
                    var updatedFailedConnections = [];
                    for (var i in irc.importResults[dataType].failed) {
                        if (irc.importResults[dataType].failed[i].omit) {
                            continue;
                        } else {
                            updatedFailedConnections.push(irc.importResults[dataType].failed[i]);
                        }
                    }
                    irc.importResults[dataType].failed = updatedFailedConnections;
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
                var tempSelectedImports = _.isUndefined(irc.selectedImports[databaseType]) ? {} : angular.copy(irc.selectedImports[databaseType]);
                tempSelectedImports[irc.importResults[databaseType].failed[index].reImport] = true;
                irc.selectedImports[databaseType] = tempSelectedImports;
            } else {
                delete irc.selectedImports[databaseType][index];
            }
        }

        function editConnection($index, databaseType, ev) {
            var backupConnection = angular.copy(irc.importResults[databaseType].failed[$index]);
            $DCSS.setActiveConnection(irc.importResults[databaseType].failed[$index]);
            $mdDialog.show({
                controller: EditImportedConnectionPopupController,
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/importResults/importPopups/editImportedConnectionPopup.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: true

            }).then(function(connection){
                if (!_.isUndefined(connection) && connection !== null) {
                    irc.importResults[databaseType].failed[$index] = connection;
                }
                $DCSS.state.activeConnection = null;
            }, function(){
                irc.importResults[databaseType].failed[$index] = backupConnection;
                $DCSS.state.activeConnection = null;
            });
        }

        function goToConnections() {
            $state.go('connections');
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
            for (var connectorMetaData in irc.connectorsMetaData) {
                if (connectorMetaData.databaseType in irc.importResults) {
                    for (var i in irc.importResults[connectorMetaData.databaseType].failed) {
                        irc.importResults[connectorMetaData.databaseType].failed[i].reImport = null;
                    }
                }
            }
            irc.selectedImports = {};
        }

        function getFormattedImportHeader(displayableName) {
           return i18n.format('dc_databaseConnector.label.modal.importDisplayableName', displayableName);
        }
    }

    ImportResultsController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$state',
        '$stateParams', 'toaster', '$mdDialog', 'i18nService', '$DCStateService'];

    function EditImportedConnectionPopupController($scope, $mdDialog, $DCSS, CS) {
        $scope.eicc = this;
        var compiledUUID = null;

        $scope.closeDialog = function(action) {
            CS.removeCompiledDirective(compiledUUID);
            switch(action) {
                case 'hide':
                    $mdDialog.hide($scope.eicc.connection);
                    break;
                case 'cancel':
                default:
                    $mdDialog.cancel();
            }
        };

        init();

        function init() {
            $scope.eicc.connection = $DCSS.state.activeConnection;
            $scope.compiled = false;
            compileDirective();
        }

        function compileDirective() {
            var attrs = [
                {attrName: "mode", attrValue: "import-edit"},
                {attrName: "database-type", attrValue: "{{eicc.connection.databaseType}}"},
                {attrName: "connection", attrValue: "eicc.connection"},
                {attrName: "closeDialog", attrValue: "eicc.closeDialog"}
            ];

            CS.compileDirective($scope, '#editImportedConnectionContent', $scope.eicc.connection.databaseType, attrs).then(function(data){
                compiledUUID = data.UUID;
            }, function(error){
                //compilation failed.
            });
        }
    }

    EditImportedConnectionPopupController.$inject = ['$scope', '$mdDialog', '$DCStateService', 'dcCompilationService'];
})();