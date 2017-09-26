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
                                                 dcDownloadFactory, toaster, $state, i18n, $q, $timeout, $DCSS, $DCMS) {
        var coc = this;
        coc.getAllConnections = getAllConnections;
        coc.createConnection = createConnection;
        coc.exportConnections = {};
        coc.exportSelectedConnections = exportSelectedConnections;
        coc.isExportDisabled = isExportDisabled;
        coc.importConnections = importConnections;
        coc.resolveTracker = resolveTracker;
        coc.connectorsAvailable = connectorsAvailable;

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
                    var errorToast = {
                        type: 'error',
                        title: i18n.message('dc_databaseConnector.toast.title.importFailed'),
                        toastId: 'ims',
                        timeout: 3000
                    };
                    if (response.data.results !== undefined && response.data.results.report !== undefined) {
                        errorToast.body = i18n.message('dc_databaseConnector.toast.message.' + response.data.results.report.reason);
                    }
                    coc.spinnerOn = false;
                    toaster.pop(errorToast);
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
                        toaster.pop({
                            type: 'error',
                            title: i18n.message('dc_databaseConnector.toast.title.exportImport'),
                            toastId: 'eti',
                            timeout: 3000
                        });
                    }
                });
        }

        function isExportDisabled() {
            return _.isEmpty(coc.exportConnections);
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
    };

    connectionsOverviewController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog', '$http',
        'dcDownloadFactory', 'toaster', '$state', 'i18nService', '$q', '$timeout', '$DCStateService', '$DCManagementService'];


    function CreateConnectionPopupController($scope, contextualData, $DCSS, $mdDialog, CS) {
        $scope.cpc = this;
        $scope.cpc.setSelectedDatabaseType = setSelectedDatabaseType;
        $scope.compiled = false;
        $scope.cpc.selectedDatabaseType = '';

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

    }

    CreateConnectionPopupController.$inject = ['$scope', 'contextualData', '$DCStateService', '$mdDialog', 'dcCompilationService'];
})();