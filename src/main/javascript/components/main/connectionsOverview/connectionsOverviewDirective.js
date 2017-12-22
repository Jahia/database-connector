(function() {
    'use strict';
    var connectionsOverview = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionsOverview.html',
            controller      : connectionsOverviewController,
            controllerAs    : 'coc',
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcConnectionsOverview', ['$log', 'contextualData', connectionsOverview]);

    var connectionsOverviewController = function($scope, contextualData, dcDataFactory, $mdDialog, $http,
                                                 dcDownloadFactory, $mdToast, $state, i18n, $q, $timeout, $DCSS, $DCMS) {
        var coc = this;
        coc.getAllConnections = getAllConnections;
        coc.createConnection = createConnection;
        coc.exportConnections = {};
        coc.exportSelectedConnections = exportSelectedConnections;
        coc.isExportDisabled = isExportDisabled;
        coc.importConnections = importConnections;
        coc.resolveTracker = resolveTracker;
        coc.connectorsAvailable = connectorsAvailable;
        coc.enableCardSelection = enableCardSelection;
        coc.isCardSelectionEnabled = isCardSelectionEnabled;
        coc.exportSingleConnection = exportSingleConnection;

        coc.$onInit = function() {
            getAllConnections();
            populateCache();
        };

        function importConnections (file, mode) {
            if (file == null && mode == 'test') {
                file = angular.element('#importFileSelector').prop('files')[0];
            }
            
            if (file) {
                var request = {
                    url: contextualData.apiUrl + '/import',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/octet-stream'
                    },
                    data: file
                };
                
                coc.spinnerOn = true;

                dcDataFactory.customRequest(request).then(function (response) {
                    var allImportsSuccessful = true;
                    for (var i in response.results) {
                        if (i == 'report') {
                            continue;
                        }
                        if (response.results[i].failed.length > 0) {
                            allImportsSuccessful = false;
                            break;
                        }
                    }
                    coc.spinnerOn = false;
                    $state.go('importResults', {results:response.results, status: allImportsSuccessful});
                }, function (response) {
                    var textContent;
                    if (response.data.results !== undefined && response.data.results.report !== undefined) {
                        textContent = i18n.message('dc_databaseConnector.toast.message.' + response.data.results.report.reason);
                    } else {
                        textContent = i18n.message('dc_databaseConnector.toast.title.importFailed');
                    }
                    coc.spinnerOn = false;
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent(textContent)
                            .position('top right')
                            .toastClass('toast-error')
                            .hideDelay(3000)
                    );
                });
            }
        }

        function getAllConnections() {
            return $q(function(resolve, reject){
                $DCMS.getAvailableConnections().then(function(){
                    coc.state = $DCSS.state;
                }, function(response){
                    //Could not retrieve connections.
                    reject();
                })
            });
        }

        function createConnection(ev) {
            $mdDialog.show({
                controller: CreateConnectionPopupController,
                controllerAs: "cpc",
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionPopups/createConnectionPopup.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose: true,
                fullscreen: true
            }).then(function () {
                $mdDialog.hide();
                $DCMS.refreshAllConnectionsStatus();
            });
        }

        function exportSingleConnection(connection) {
            var data = {};
            data[connection.databaseType] = [connection.id];
            var url = contextualData.apiUrl + '/export/false';
            return dcDownloadFactory.download(url, 'text/plain', data).$promise
                .then(function (data) {
                    // promise fulfilled
                    if (data.response.blob != null) {
                        saveAs(data.response.blob, data.response.fileName);
                    } else {
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent(i18n.message('dc_databaseConnector.toast.title.exportImport'))
                                .position('top right')
                                .toastClass('toast-error')
                                .hideDelay(3000)
                        );
                    }
                });
        }

        function exportSelectedConnections() {
            var data = {};
            var databaseTypes = _.keys(coc.exportConnections);
            var multipleConnections = false;
            if (databaseTypes.length == 1 && _.keys(coc.exportConnections[databaseTypes[0]]).length == 1) {
                data[databaseTypes[0]] = [];
                data[databaseTypes[0]].push(_.keys(coc.exportConnections[databaseTypes[0]])[0]);
            } else {
                multipleConnections = true;
                for (var i in coc.exportConnections) {
                    data[i] = _.keys(coc.exportConnections[i]);
                }
            }
            var url = contextualData.apiUrl + '/export/' + multipleConnections;
            return dcDownloadFactory.download(url, 'text/plain', data).$promise
                .then(function (data) {
                    // promise fulfilled
                    if (data.response.blob != null) {
                        saveAs(data.response.blob, data.response.fileName);
                        coc.exportConnections = {};
                        $scope.$broadcast('resetExportSelection', null);
                    } else {
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent(i18n.message('dc_databaseConnector.toast.title.exportImport'))
                                .position('top right')
                                .toastClass('toast-error')
                                .hideDelay(3000)
                        );
                    }
                });
        }

        function isExportDisabled() {
            return _.isEmpty(coc.exportConnections) || !coc.isCardSelectionEnabled();
        }

        function populateCache() {
            //Preload alldirective
            $DCSS.getFromCache("alldirectives").then(function(data) {
                //Do nothing
                //console.log(data);
            }, function(error) {
                console.error(error);
            });
        }

        function resolveTracker(id, databaseType) {
            return databaseType + '_' + id;
        }

        function connectorsAvailable(){
            return !_.isEmpty($DCSS.connectorsMetaData);
        }

        function isCardSelectionEnabled() {
            return contextualData.cardSelectionEnabled;
        }

        function enableCardSelection() {
            contextualData.cardSelectionEnabled = !contextualData.cardSelectionEnabled;
            if (!contextualData.cardSelectionEnabled)  {
                //reset exportConnections
                coc.exportConnections = {};
                $scope.$broadcast('resetExportSelection', null);
            }
        }
    };

    connectionsOverviewController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog', '$http',
        'dcDownloadFactory', '$mdToast', '$state', 'i18nService', '$q', '$timeout', '$DCStateService', '$DCManagementService'];


    function CreateConnectionPopupController($scope, contextualData, $DCSS, $mdDialog, CS) {
        $scope.cpc = this;
        $scope.cpc.setSelectedDatabaseType = setSelectedDatabaseType;
        $scope.compiled = false;
        $scope.cpc.selectedDatabaseType = '';
        $scope.cpc.isConnectorResolved = isConnectorResolved;
        $scope.cpc.isConnectorUnresolved = isConnectorUnresolved;
        $scope.cpc.reloadPage = reloadPage;

        var compiledUUID = null;
        init();

        function init() {
            $scope.cpc.connection = {};
            $scope.cpc.images = {};
            $scope.cpc.databaseTypes = angular.copy($DCSS.connectorsMetaData);

            for (var i in $scope.cpc.databaseTypes) {
                $scope.cpc.images[$scope.cpc.databaseTypes[i].databaseType] = contextualData.context + '/modules/' + $scope.cpc.databaseTypes[i].moduleName  + '/images/' + $scope.cpc.databaseTypes[i].databaseType.toLowerCase() + '/logo_60.png';
            }
        }

        $scope.closeDialog = function(action) {
            CS.removeCompiledDirective(compiledUUID);
            switch(action) {
                case 'hide':
                    $mdDialog.hide();
                    break;
                case 'cancel':
                default:
                    resetCreationProcess();
            }
        };

        function resetCreationProcess() {
            CS.removeCompiledDirective(compiledUUID);
            $scope.cpc.connection = {};
            $scope.cpc.selectedDatabaseType = '';
            $scope.compiled = false;
        }

        function setSelectedDatabaseType(databaseType) {
            //We should never manipulate variables from this controller inside compilation functions as it leads to bugs,
            //confusion and reduces readability of code!!! It belongs here!!!
            if (!$scope.compiled) {
                $scope.cpc.selectedDatabaseType = databaseType;
                var attrs = [
                    {attrName:"mode", attrValue:"create"},
                    {attrName:"database-type", attrValue:"{{cpc.selectedDatabaseType}}"},
                    {attrName:"connection", attrValue:"cpc.connection"},
                    {attrName: "closeDialog", attrValue: "cpc.closeDialog"}
                ];
                CS.compileDirective($scope, '#createConnectionContent', $scope.cpc.selectedDatabaseType, attrs).then(function(data){
                    compiledUUID = data.UUID;
                    $scope.compiled = true;
                }, function(error){
                    //compilation failed.
                });
            }
        }

        function isConnectorResolved(type) {
             return _.contains(contextualData.resolvedConnectors, type);
        }

        function isConnectorUnresolved() {
            return _.keys($scope.cpc.databaseTypes).length !== contextualData.resolvedConnectors.length;
        }

        function reloadPage() {
            window.location.reload();
        }
    }

    CreateConnectionPopupController.$inject = ['$scope', 'contextualData', '$DCStateService', '$mdDialog', 'dcCompilationService'];
})();