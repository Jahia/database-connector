(function() {
    'use strict';
    var Connection = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/Connection.html',
            scope: {
                connection: '='
            },
            controller      : ConnectionController,
            controllerAs    : 'cc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcMongoConnection', ['$log', 'contextualData', Connection]);

    var ConnectionController = function($scope, contextualData, dcDataFactory, $mdDialog) {
        var cc = this;
        cc.imageUrl = contextualData.context + '/modules/database-connector/images/' + cc.connection.databaseType + '/logo_60.png';
        cc.originalConnection = angular.copy(cc.connection);
        cc.updateConnection = updateConnection;
        cc.openDeleteConnectionDialog = openDeleteConnectionDialog;
        cc.editConnection = editConnection;

        function updateConnection(connect) {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[cc.connection.databaseType] + '/' + (connect ? 'connect' : 'disconnect') + '/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'PUT'
            }).then(function(response) {
                cc.connection.isConnected = connect;
            }, function(response) {});
        }

        function openDeleteConnectionDialog(ev1) {
            $mdDialog.show({
                locals: {
                    connection: cc.connection
                    
                },
                controller: DeleteConnectionPopupController,
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionPopups/deleteConnectionPopup.html',
                parent: angular.element(document.body),
                targetEvent: ev1,
                clickOutsideToClose:true,
                fullscreen: false

            }).then(function(){
                deleteConnection();
                getUpdatedConnection();
            }, function(){});
        }
        function deleteConnection() {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[cc.connection.databaseType] + '/' +'remove'+ '/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'DELETE',
            }).then(function(response){
                cc.connection = response;
                $scope.$emit('connectionSuccessfullyDeleted', null);

            }, function(response){
                console.log('error connection not deleted', response);
            });

        }
        
        

        function editConnection(ev) {
            $mdDialog.show({
                locals: {
                    connection: cc.connection
                },
                controller: EditConnectionPopupController,
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionPopups/editConnectionPopup.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: true

            }).then(function(){
                getUpdatedConnection();
            }, function(){
                cc.connection = angular.copy(cc.originalConnection);
            });
        }

        function getUpdatedConnection() {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[cc.connection.databaseType] + '/connection/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function(response) {
                cc.connection = response;
                cc.originalConnection = angular.copy(response);
            }, function(response) {});
        }
    };

    ConnectionController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog'];



    function EditConnectionPopupController($scope, $mdDialog, connection) {
        $scope.ecp = this;
        $scope.ecp.connection = connection;

        $scope.$on('connectionSuccessfullyCreated', function(){
            $mdDialog.hide();
        });

        $scope.$on('creationCancelled', function() {
            $mdDialog.cancel();
        });

    }

    EditConnectionPopupController.$inject = ['$scope', '$mdDialog', 'connection'];




    function DeleteConnectionPopupController($scope, $mdDialog) {
        $scope.dcp = this;
        $scope.dcp.cancel = cancel;
        $scope.dcp.deleteConnection = deleteConnection;

        function cancel() {
            $mdDialog.cancel();
        }

        function deleteConnection() {
            console.log('deleting');
            $mdDialog.hide();
        }

    }



    DeleteConnectionPopupController.$inject = ['$scope', '$mdDialog'];



})();