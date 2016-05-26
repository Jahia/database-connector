(function() {
    'use strict';
    var Connection = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/Connection.html',
            scope: {
                connection: '=',
                exportConnections: '='
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
        .directive('dcConnection', ['$log', 'contextualData', Connection]);

    var ConnectionController = function($scope, contextualData, dcDataFactory, $mdDialog, $filter, toaster) {
        var cc = this;
        cc.imageUrl = contextualData.context + '/modules/database-connector/images/' + cc.connection.databaseType + '/logo_60.png';
        cc.originalConnection = angular.copy(cc.connection);
        cc.updateConnection = updateConnection;
        cc.openDeleteConnectionDialog = openDeleteConnectionDialog;
        cc.editConnection = editConnection;
        cc.exportValueChanged = exportValueChanged;
        cc.spinnerOptions = {
            mode: 'indeterminate',
            showSpinner: false
        };
        init();

        function init() {
            //Replace any null values, so they dont show up.
            for (var i in cc.connection) {
               cc.connection[i] = $filter('replaceNull')(cc.connection[i]);
            }
            cc.originalConnection = angular.copy(cc.connection);
        }
        function updateConnection(connect) {
            cc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[cc.connection.databaseType] + '/' + (connect ? 'connect' : 'disconnect') + '/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'PUT'
            }).then(function(response) {
                if (response.success) {
                    cc.connection.isConnected = connect;
                    toaster.pop({
                        type   : 'success',
                        title: 'Connection status successfully updated!',
                        toastId: 'cu',
                        timeout: 3000
                    });
                } else {
                    toaster.pop({
                        type   : 'error',
                        title: 'Connection status update failed!',
                        toastId: 'cu',
                        timeout: 3000
                    });
                }
                cc.spinnerOptions.showSpinner = false;
            }, function(response) {
                cc.spinnerOptions.showSpinner = false;
            });
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
            cc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[cc.connection.databaseType] + '/' +'remove'+ '/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'DELETE'
            }).then(function(response){
                cc.connection = response;
                $scope.$emit('connectionSuccessfullyDeleted', null);
                toaster.pop({
                    type   : 'success',
                    title: 'Successfully deleted connection!',
                    toastId: 'cd',
                    timeout: 3000
                });
                cc.spinnerOptions.showSpinner = false;
            }, function(response){
                console.log('error connection not deleted', response);
                cc.spinnerOptions.showSpinner = false;
                toaster.pop({
                    type   : 'error',
                    title: 'Failed to delete connection',
                    toastId: 'cd',
                    timeout: 3000
                });
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
        
        function exportValueChanged() {
            if (cc.connection.export) {
                var exportConnectionsTemp = {};
                for (var i in cc.exportConnections) {
                    exportConnectionsTemp[i] = cc.exportConnections[i];
                }
                cc.exportConnections = exportConnectionsTemp;
                cc.exportConnections[cc.connection.id] = cc.connection.databaseType;
            } else {
                var exportConnectionsTemp2 = {};
                for (var i in cc.exportConnections) {
                    if (i != cc.connection.id) {
                        exportConnectionsTemp2[i] = cc.exportConnections[i];
                    }
                }
                cc.exportConnections = exportConnectionsTemp;
            }
        }
    };

    ConnectionController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog', '$filter', 'toaster'];
    
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
            $mdDialog.hide();
        }
    }

    DeleteConnectionPopupController.$inject = ['$scope', '$mdDialog'];
    
})();