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

    var connectionsOverviewController = function($scope, contextualData, dcDataFactory, $mdDialog, dcDownloadFactory, toaster, $state) {
        var coc = this;
        coc.getConnections = getConnections;
        coc.createConnection = createConnection;
        coc.exportConnections = {};
        coc.exportSelectedConnections = exportSelectedConnections;
        coc.isExportDisabled = isExportDisabled;
        coc.importConnections = importConnections;
        
        init();

        function init() {
            getDatabaseTypes();
            getConnections();
        }

        function importConnections (file, errFiles) {
            $scope.f = file;
            $scope.errFile = errFiles && errFiles[0];
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
                var allConnectionsImported = true;
                for (var databaseType in coc.databaseTypes) {
                    if (!_.isUndefined(response.results[databaseType]) && !_.isEmpty(response.results[databaseType].failed)) {
                        allConnectionsImported = false;
                        break;
                    }
                }
                $state.go('importResults', {results:response.results, status: allConnectionsImported});
            }, function (response) {
                toaster.pop({
                    type: 'error',
                    title: 'Import Failed',
                    body: 'Failed to perform import!',
                    toastId: 'ims',
                    timeout: 3000
                });
            });
        };

        function getConnections() {
            var url = contextualData.context + '/modules/databaseconnector/mongodb/getconnections';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function (response) {
                coc.connections = {};
                coc.connections = response.connections;
            }, function (response) {
            });
        }

        function getDatabaseTypes() {
            var url = contextualData.context + '/modules/databaseconnector/databasetypes';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function (response) {
                coc.databaseTypes = response;
            }, function (response) {
            });
        }

        function createConnection(ev) {
            $mdDialog.show({
                locals: {
                    updateConnections: coc.getConnections
                },
                controller: CreateConnectionPopupController,
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionPopups/createConnectionPopup.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose: true,
                fullscreen: true
            }).then(function () {
                getConnections();
            }, function () {
            });
        }

        function exportSelectedConnections() {
            var data = {};
            for (var i in coc.exportConnections) {
                if (_.isUndefined(data[coc.exportConnections[i]])) {
                    data[coc.exportConnections[i]] = [];
                }
                data[coc.exportConnections[i]].push(i);
            }
            var url = contextualData.context + '/modules/databaseconnector/export';
            return dcDownloadFactory.download(url, 'text/plain', data).$promise.then(function (data) {
                if (data.response.blob != null) {
                    saveAs(data.response.blob, data.response.fileName);
                    coc.exportConnections = {};
                    $scope.$broadcast('resetExportSelection', null);
                    getConnections();
                    toaster.pop({
                        type: 'success',
                        title: 'Connection Successfully Exported!',
                        body: 'Connection Export was successful!',
                        toastId: 'ce',
                        timeout: 4000
                    });
                } else {
                    toaster.pop({
                        type: 'error',
                        title: 'Export Connection Failed!',
                        toastId: 'eti',
                        timeout: 3000
                    });
                };
            });
        }

        function isExportDisabled() {
            return _.isEmpty(coc.exportConnections);
        }

        $scope.$on('connectionSuccessfullyDeleted', function () {
            getConnections();
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
    
    CreateConnectionPopupController.$inject = ['$scope', '$mdDialog', 'contextualData', 'dcDataFactory', 'updateConnections'];
})();