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

    var connectionsOverviewController = function($scope, contextualData, dcDataFactory, $mdDialog,
                                                 dcDownloadFactory, toaster, $state, i18n, $q, $timeout, $DCSS, $DCMS) {
        var coc = this;
        coc.getAllConnections = getAllConnections;
        coc.createConnection = createConnection;
        coc.exportConnections = {};
        coc.exportSelectedConnections = exportSelectedConnections;
        coc.isExportDisabled = isExportDisabled;
        coc.importConnections = importConnections;

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
                $DCMS.getAvailableConnections().then(function(connections){
                    coc.connections = connections;
                    resolve(connections);
                }, function(response){
                    //Could not retrieve connections.
                    reject();
                })
            });
        }

        function createConnection(ev) {
            $mdDialog.show({
                locals: {
                    updateConnections: coc.getAllConnections
                },
                controller: CreateConnectionPopupController,
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionPopups/createConnectionPopup.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose: true,
                fullscreen: true
            }).then(function () {
                $DCMS.refreshAllConnectionsStatus().then(function(connections){
                    coc.connections = connections;
                });
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
    };

    connectionsOverviewController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog',
        'dcDownloadFactory', 'toaster', '$state', 'i18nService', '$q', '$timeout', '$DCStateService', '$DCManagementService'];


    function CreateConnectionPopupController($scope, $mdDialog, contextualData, $DCSS, $timeout, CS) {
        $scope.cpc = this;
        $scope.cpc.databaseTypeSelected = false;
        $scope.cpc.setSelectedDatabaseType = setSelectedDatabaseType;
        $scope.compiled = false;
        var compilationResult = {}; //scope and element

        init();

        function init() {
            $scope.cpc.connection = {};
            $scope.cpc.images = {};
            $scope.cpc.databaseTypes = angular.copy($DCSS.connectorsMetaData);
            for (var i in $scope.cpc.databaseTypes) {
                $scope.cpc.images[$scope.cpc.databaseTypes[i].databaseType] = contextualData.context + '/modules/' + $scope.cpc.databaseTypes[i].moduleName  + '/images/' + $scope.cpc.databaseTypes[i].databaseType.toLowerCase() + '/logo_60.png';
            }
        }


        $scope.$on('connectionSuccessfullyCreated', function(){
            $mdDialog.hide();
        });

        $scope.$on('creationCancelled', function() {
            resetCreationProcess();
        });

        $scope.compileDirective = function() {
            if (!$scope.compiled) {
                $timeout(function() {
                    $DCSS.getDirectivesForType($scope.cpc.selectedDatabaseType).then(function(data) {
                        var promise = CS.compileInsideElement($scope, data.connectionDirective.tag, "#createConnectionContent", [
                                {attrName:"mode", attrValue:"create"},
                                {attrName:"database-type", attrValue:"{{cpc.selectedDatabaseType}}"},
                                {attrName:"connection", attrValue:"cpc.connection"}
                            ]
                        );
                        promise.then(function(data) {
                            //console.log(data);
                            compilationResult = data;
                        }, function(error) {
                            console.error(error);
                        })
                    }, function(error) {
                        console.error(error);
                    });
                });
                $scope.compiled = true;
            }
        };


        function resetCreationProcess() {
            $scope.cpc.connection = {};
            $scope.cpc.databaseTypeSelected = false;
            $scope.cpc.selectedDatabaseType = '';
            compilationResult.scope.$destroy();
            compilationResult.element.empty();
        }
        function setSelectedDatabaseType(databaseType) {
            $scope.cpc.selectedDatabaseType = databaseType;
            $scope.cpc.databaseTypeSelected = true;
            $scope.compileDirective();
        }

    }

    CreateConnectionPopupController.$inject = ['$scope', '$mdDialog', 'contextualData', '$DCStateService', '$timeout', 'dcCompilationService'];
})();