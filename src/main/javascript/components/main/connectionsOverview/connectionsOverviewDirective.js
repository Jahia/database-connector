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
    
    var connectionsOverviewController = function($scope, contextualData, dcDataFactory, $mdDialog) {
        var coc = this;
        coc.getConnections = getConnections;
        coc.createConnection = createConnection;
        init();

        function init() {
            getConnections();
        }

        function getConnections() {
            var url = contextualData.context + '/modules/databaseconnector/mongodb/getconnections';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function(response) {
                coc.connections = [];
                for (var i in response.connections) {
                    if (i % 2 == 0) {
                        coc.connections.push([response.connections[i]]);
                    } else {
                        coc.connections[coc.connections.length - 1].push(response.connections[i]);
                    }
                }
            }, function(response) {});
        }

        function createConnection(ev) {
            console.log($mdDialog);

            $mdDialog.show({
                locals: {
                    updateConnections: coc.getConnections
                },
                controller: ConnectionPopupsController,
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionPopups/connectionPopups.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: true

        })
        }
    };

    connectionsOverviewController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog'];


    function ConnectionPopupsController($scope, $mdDialog, contextualData, dcDataFactory, updateConnections) {
        $scope.cpc = this;
        $scope.cpc.databaseTypeSelected = false;
        $scope.cpc.setSelectedDatabaseType = setSelectedDatabaseType;

        init();

        function init() {
            getDatabaseTypes();
        }

        $scope.hide = function() {
            $mdDialog.hide();
        };
        $scope.cancel = function() {
            $mdDialog.cancel();
        };
        $scope.answer = function(answer) {
            $mdDialog.hide(answer);
        };

        $scope.$on('connectionSuccessfullyCreated', function(){
            updateConnections();
            $scope.hide();
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
            $scope.cpc.databaseTypeSelected = false;
            $scope.cpc.selectedDatabaseType = '';
        }
        function setSelectedDatabaseType(databaseType) {
            $scope.cpc.selectedDatabaseType = databaseType;
            $scope.cpc.databaseTypeSelected = true;
        }
    
    }

    
    ConnectionPopupsController.$inject = ['$scope', '$mdDialog', 'contextualData', 'dcDataFactory', 'updateConnections'];



})();


