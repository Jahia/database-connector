(function() {
    'use strict';
    var connectionsOverviewController;
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

    connectionsOverviewController = function ($scope, contextualData, dcDataFactory, $mdDialog, dcDownloadFactory, toaster) {
        var coc = this;
        coc.getConnections = getConnections;
        coc.createConnection = createConnection;
        coc.exportConnections = {};
        coc.exportSelectedConnections = exportSelectedConnections;
        coc.isExportDisabled = isExportDisabled;

        init();

        function init() {
            getDatabaseTypes();
            getConnections();
        }

        $scope.uploadFiles = function (file, errFiles) {
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
                console.log(response);
                for (var i in coc.databaseTypes) {
                    if (!_.isUndefined(response.results[coc.databaseTypes[i]])) {
                        if (_.findWhere(response.results[coc.databaseTypes[i]].status, {result: 'failed'}) != undefined) {
                            allConnectionsImported = false;
                        }
                    }
                }

                toaster.pop({
                    type: allConnectionsImported ? 'success' : 'warning',
                    title: 'Import Successful',
                    body: allConnectionsImported ? 'All connections imported successfully!' : 'Not all imports were successful!',
                    toastId: 'ims',
                    timeout: 3000
                });

                $mdDialog.show({
                    locals: {
                        importResults: response.results,
                        databaseTypes: coc.databaseTypes
                    },
                    controller: ImportConnectionsPopupController,
                    templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionPopups/importResultsPopup.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: true,
                    fullscreen: true
                }).then(function () {
                    getConnections();
                }, function () {
                });
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


    connectionsOverviewController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog', 'dcDownloadFactory', 'toaster'];


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
                for (var i in $scope.cpc.databaseTypes) {
                    $scope.cpc.images[$scope.cpc.databaseTypes[i]] = contextualData.context + '/modules/database-connector/images/' + $scope.cpc.databaseTypes[i].toLowerCase() + '/logo_60.png';
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

    function ImportConnectionsPopupController($scope, $mdDialog, contextualData, dcDataFactory, importResults, databaseTypes) {
        $scope.icpc = this;
        $scope.icpc.importResults = importResults;
        $scope.icpc.databaseTypes = databaseTypes;
        $scope.icpc.hasResults = hasResults;

        init();

        function init() {
            console.log($scope.icpc.importResults);
        }

        function reImportConnection(connection) {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[connection.type] + '/add';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function(response) {
            }, function(response) {});
        }

        function hasResults(databaseType) {
            return !_.isUndefined($scope.icpc.importResults[databaseType]);
        }
    }

    ImportConnectionsPopupController.$inject = ['$scope', '$mdDialog', 'contextualData', 'dcDataFactory', 'importResults', 'databaseTypes'];
})();