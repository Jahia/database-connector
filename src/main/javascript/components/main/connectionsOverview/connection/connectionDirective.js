(function() {
    'use strict';
    var Connection = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/connection.html',
            controller      : ConnectionController,
            controllerAs    : 'cc',
            bindToController: {
                index: '@',
                connection: '=',
                exportConnections: '='
            },
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}
    };

    angular
        .module('databaseConnector')
        .directive('dcConnection', ['$log', 'contextualData', Connection]);

    var ConnectionController = function($scope, contextualData, dcDataFactory, $mdDialog, $filter, toaster, $state, i18n) {
        var cc = this;
        cc.spinnerOptions = {
            mode: 'indeterminate',
            showSpinner: false
        };
        cc.updateConnection = updateConnection;
        cc.openDeleteConnectionDialog = openDeleteConnectionDialog;
        cc.editConnection = editConnection;
        cc.exportValueChanged = exportValueChanged;
        cc.goToStatus = goToStatus;
        cc.serverStatusAvailable = serverStatusAvailable;
        cc.$onInit = function() {
            cc.imageUrl = contextualData.context + '/modules/database-connector/images/' + cc.connection.databaseType.toLowerCase() + '/logo_60.png';
            cc.originalConnection = angular.copy(cc.connection);

            $scope.$on('resetExportSelection', function(){
                cc.connection.export = false;
            });
            $scope.$on('refreshConnectionStatus', function(){
                verifyServerStatus();
            });
            init();
        };

        function init() {
            //Replace any null values, so they dont show up.
            for (var i in cc.connection) {
                cc.connection[i] = $filter('replaceNull')(cc.connection[i]);
            }
            cc.originalConnection = angular.copy(cc.connection);
            verifyServerStatus();
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
                    cc.originalConnection.isConnected = connect;
                    if (connect) {
                        verifyServerStatus();
                    } else {
                        cc.connection.canRetrieveStatus = false;
                        cc.originalConnection.canRetrieveStatus = cc.connection.canRetrieveStatus;
                    }
                    toaster.pop({
                        type   : 'success',
                        title: i18n.message('dc_databaseConnector.toast.title.connectionSuccessfullyUpdated'),
                        toastId: 'cu',
                        timeout: 3000
                    });
                } else {
                    toaster.pop({
                        type   : 'error',
                        title: i18n.message('dc_databaseConnector.toast.title.connectionUpdateFailed'),
                        toastId: 'cu',
                        timeout: 3000
                    });
                    cc.connection.canRetrieveStatus = false;
                    cc.originalConnection.canRetrieveStatus = cc.connection.canRetrieveStatus;
                }
                cc.spinnerOptions.showSpinner = false;
            }, function(response) {
                cc.spinnerOptions.showSpinner = false;
                cc.connection.canRetrieveStatus = false;
                cc.originalConnection.canRetrieveStatus = cc.connection.canRetrieveStatus;
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
                if (cc.connection.id in cc.exportConnections) {
                    var exportConnectionsTemp = angular.copy(cc.exportConnections);
                    delete exportConnectionsTemp[cc.connection.id];
                    cc.exportConnections = exportConnectionsTemp;
                }
                $scope.$emit('notifyRefreshConnectionStatus', null);
                toaster.pop({
                    type   : 'success',
                    title: i18n.message('dc_databaseConnector.toast.title.connectionSuccessfullyDeleted'),
                    toastId: 'cd',
                    timeout: 3000
                });
                cc.spinnerOptions.showSpinner = false;
            }, function(response){
                console.log('error connection not deleted', response);
                cc.spinnerOptions.showSpinner = false;
                toaster.pop({
                    type   : 'error',
                    title: i18n.message('dc_databaseConnector.toast.title.connectionDeletionFailed'),
                    toastId: 'cd',
                    timeout: 3000
                });
            });
        }
        
        function editConnection(ev) {
            for (var i in cc.connection) {
                cc.connection[i] = $filter('replaceNull')(cc.connection[i]);
            }
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
                $scope.$emit('notifyRefreshConnectionStatus', null);
            }, function(response) {});
        }
        
        function exportValueChanged() {
            if (cc.connection.export) {
                var exportConnectionsTemp = angular.copy(cc.exportConnections);
                cc.exportConnections = exportConnectionsTemp;
                if (!(cc.connection.databaseType in cc.exportConnections)) {
                    cc.exportConnections[cc.connection.databaseType] = {};
                }
                cc.exportConnections[cc.connection.databaseType][cc.connection.id] = true;
            } else {
                var exportConnectionsTemp = angular.copy(cc.exportConnections);
                delete exportConnectionsTemp[cc.connection.databaseType][cc.connection.id];
                if (_.isEmpty(exportConnectionsTemp[cc.connection.databaseType])) {
                    delete exportConnectionsTemp[cc.connection.databaseType];
                }
                cc.exportConnections = exportConnectionsTemp;
            }
        }
        
        function goToStatus() {
            $state.go('connectionsStatus', {connection: cc.connection});
        }

        function verifyServerStatus() {
            //verify if this connection is authenticated to retrieve server status
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[cc.connection.databaseType] + '/status/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function(response) {
                //status can be retrieved
                if (_.isUndefined(response.failed)) {
                    cc.connection.canRetrieveStatus = true;
                    cc.originalConnection.canRetrieveStatus = true;
                    if(cc.connection.databaseType == "MONGO"){
                        cc.connection.dbVersion = response.success.version;
                        cc.connection.uptime = response.success.uptime;
                        cc.originalConnection.dbVersion = cc.connection.dbVersion;
                        cc.originalConnection.uptime = cc.connection.uptime;
                    } else if (cc.connection.databaseType == "REDIS") {
                        response.success = dcDataFactory.parseRedisStatus(response.success);
                        cc.connection.dbVersion = response.success.redis_version;
                        cc.connection.uptime = response.success.uptime_in_seconds;
                        cc.originalConnection.dbVersion = cc.connection.dbVersion;
                        cc.originalConnection.uptime = cc.connection.uptime;
                    }
                } else {
                    cc.connection.canRetrieveStatus = false;
                    cc.originalConnection.canRetrieveStatus = cc.connection.canRetrieveStatus;
                }
            }, function(response) {
                //status cannot be retrieved
                cc.connection.canRetrieveStatus = false;
                cc.originalConnection.canRetrieveStatus = false;
            });
        }

        function serverStatusAvailable() {
            return !_.isUndefined(cc.connection.canRetrieveStatus) && cc.connection.canRetrieveStatus && cc.connection.isConnected;
        }
    };

    ConnectionController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog', '$filter', 'toaster', '$state', 'i18nService'];
    
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
    
    function DeleteConnectionPopupController($scope, $mdDialog, $sce, i18n) {
        $scope.dcp = this;
        $scope.dcp.cancel = cancel;
        $scope.dcp.deleteConnection = deleteConnection;
        $scope.dcp.deleteConnectionMessage = $sce.trustAsHtml(i18n.message('dc_databaseConnector.label.modal.message.deleteConnection'));
        function cancel() {
            $mdDialog.cancel();
        }

        function deleteConnection() {
            $mdDialog.hide();
        }
    }

    DeleteConnectionPopupController.$inject = ['$scope', '$mdDialog', '$sce', 'i18nService'];
    
})();