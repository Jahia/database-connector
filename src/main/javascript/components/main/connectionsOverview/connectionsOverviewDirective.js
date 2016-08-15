(function() {
    'use strict';
    var connectionsOverview = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionsOverview.html',
            controller      : connectionsOverviewController,
            controllerAs    : 'coc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcConnectionsOverview', ['$log', 'contextualData', connectionsOverview]);

    var connectionsOverviewController = function($scope, contextualData, dcDataFactory, $mdDialog, dcDownloadFactory, toaster, $state, i18n) {
        var coc = this;
        coc.getAllConnections = getAllConnections;
        coc.createConnection = createConnection;
        coc.exportConnections = {};
        coc.exportSelectedConnections = exportSelectedConnections;
        coc.isExportDisabled = isExportDisabled;
        coc.importConnections = importConnections;
        
        init();

        function init() {
            getDatabaseTypes();
            getAllConnections();
        }

        function importConnections (file, mode) {
            if (file == null && mode == 'test') {
                file = angular.element('#importFileSelector').prop('files')[0];
            }
            
            if (file) {
                var request = {
                    url: contextualData.context + '/modules/databaseconnector/import',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/octet-stream'
                    },
                    data: file
                };

            }

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
                $state.go('importResults', {results:response.results, status: allImportsSuccessful});
            }, function (response) {
                toaster.pop({
                    type: 'error',
                    title: i18n.message('dc_databaseConnector.toast.title.importFailed'),
                    body: i18n.message('dc_databaseConnector.toast.message.importFailed'),
                    toastId: 'ims',
                    timeout: 3000
                });
            });
        }
        function getAllConnections() {
            var url = contextualData.context + '/modules/databaseconnector/getallconnections';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function (response) {
                coc.connections = response.connections;
            }, function (response) {
            });

        }
        function getDatabaseTypes() {
            dcDataFactory.getDatabaseTypes().then(function(response){
                coc.databaseTypes = response;
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
                getAllConnections();
            }, function () {
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
            var url = contextualData.context + '/modules/databaseconnector/export/' + multipleConnections;
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

        $scope.$on('connectionSuccessfullyDeleted', function () {
            getAllConnections();
        });
    };

    connectionsOverviewController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog', 'dcDownloadFactory', 'toaster', '$state'];


    function CreateConnectionPopupController($scope, $mdDialog, contextualData, dcDataFactory) {
        $scope.cpc = this;
        $scope.cpc.databaseTypeSelected = false;
        $scope.cpc.setSelectedDatabaseType = setSelectedDatabaseType;

        init();

        function init() {
            $scope.cpc.connection = {};
            getDatabaseTypes();
        }


        $scope.$on('connectionSuccessfullyCreated', function(){
            $mdDialog.hide();
        });

        $scope.$on('creationCancelled', function() {
            resetCreationProcess();
        });


        function getDatabaseTypes() {
            var url = contextualData.context + '/modules/databaseconnector/databasetypes';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function(response) {
                $scope.cpc.images = {};
                $scope.cpc.databaseTypes = response;
                for (var databaseType in $scope.cpc.databaseTypes) {
                    $scope.cpc.images[databaseType] = contextualData.context + '/modules/database-connector/images/' + databaseType.toLowerCase() + '/logo_60.png';
                }
            }, function(response) {});
        }

        function resetCreationProcess() {
            $scope.cpc.connection = {};
            $scope.cpc.databaseTypeSelected = false;
            $scope.cpc.selectedDatabaseType = '';
        }
        function setSelectedDatabaseType(databaseType) {
            $scope.cpc.selectedDatabaseType = databaseType;
            $scope.cpc.databaseTypeSelected = true;
        }
    
    }
    
    CreateConnectionPopupController.$inject = ['$scope', '$mdDialog', 'contextualData', 'dcDataFactory', 'updateConnections', 'i18nService'];
})();