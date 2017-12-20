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
                exportConnections: '=',
                isCardSelectionEnabled: '=',
                exportConnection: "=exportSingleConnection"
            },
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}
    };

    angular
        .module('databaseConnector')
        .directive('dcConnection', ['$log', 'contextualData', Connection]);

    var ConnectionController = function($scope, contextualData, dcDataFactory,
                                        $mdDialog, $filter, toaster, $state, i18n, $DCSS, $DCCMS, $DCMS) {
        var cc = this;
        cc.spinnerOptions = {
            mode: 'indeterminate',
            showSpinner: false
        };
        cc.updateConnection = updateConnection;
        cc.openDeleteConnectionDialog = openDeleteConnectionDialog;
        cc.editConnection = editConnection;
        cc.updateExportValue = updateExportValue;
        cc.goToStatus = goToStatus;
        cc.serverStatusAvailable = serverStatusAvailable;

        cc.$onInit = function() {
            cc.imageUrl = contextualData.context + '/modules/' + $DCSS.connectorsMetaData[cc.connection.databaseType].moduleName + '/images/' + cc.connection.databaseType.toLowerCase() + '/logo_60.png';
            for (var i in cc.connection) {
                cc.connection[i] = $filter('replaceNull')(cc.connection[i]);
            }
            cc.originalConnection = angular.copy(cc.connection);
            $scope.$on('resetExportSelection', function(){
                cc.connection.export = false;
            });
            $DCCMS.verifyServerStatus({ connection: cc.connection }).then(function(data){
                cc.connection = data.connection;
                cc.originalConnection = angular.copy(data.connection);
            }, function(error){
                cc.connection = error.connection;
                cc.originalConnection = angular.copy(error.connection);
            });
        };

        function updateConnection(connect) {
            cc.spinnerOptions.showSpinner = true;
            $DCCMS.updateConnection(cc.connection, connect).then(function(data) {
                cc.connection = data.connection;
                cc.originalConnection = angular.copy(data.connection);
                cc.spinnerOptions.showSpinner = false;
            }, function(error) {
                cc.connection = error.connection;
                cc.originalConnection = angular.copy(error.connection);
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
            }, function(){});
        }

        function deleteConnection() {
            cc.spinnerOptions.showSpinner = true;
            var url = contextualData.apiUrl + $DCSS.connectorsMetaData[cc.connection.databaseType].entryPoint + '/remove/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'DELETE'
            }).then(function(response){
                if (cc.connection.id in cc.exportConnections) {
                    var exportConnectionsTemp = angular.copy(cc.exportConnections);
                    delete exportConnectionsTemp[cc.connection.id];
                    cc.exportConnections = exportConnectionsTemp;
                }
                $DCMS.removeConnection(cc.connection.id, cc.connection.databaseType);
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
            //Set currently active connection.
            $DCSS.setActiveConnection(cc.connection);
            $mdDialog.show({
                controller: EditConnectionPopupController,
                templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionPopups/editConnectionPopup.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: true

            }).then(function(){
                getUpdatedConnection();
                $DCSS.state.activeConnection = null;
            }, function(){
                cc.connection = angular.copy(cc.originalConnection);
                $DCSS.state.activeConnection = null;
            });
        }

        function getUpdatedConnection() {
            var url = contextualData.apiUrl + '/connection/' + cc.connection.databaseType + '/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function(updatedConnection) {
                $DCCMS.verifyServerStatus({ connection: updatedConnection }).then(function(data){
                    cc.connection = data.connection;
                    cc.originalConnection = angular.copy(data.connection);
                }, function(error){
                    cc.connection = error.connection;
                    cc.originalConnection = angular.copy(error.connection);
                });
            }, function(error) {
                cc.connection = error.connection;
                cc.originalConnection = angular.copy(error.connection);
            });
        }

        function updateExportValue() {
            if (!cc.isCardSelectionEnabled) {
                return;
            }
            cc.connection.export = !cc.connection.export;
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
            $DCSS.selectedDatabaseType = cc.connection.databaseType;
            $state.go('connectionsStatus', {connection: cc.connection});
        }

        function serverStatusAvailable() {
            return !_.isUndefined(cc.connection.canRetrieveStatus) && cc.connection.canRetrieveStatus && cc.connection.isConnected;
        }
    };

    ConnectionController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$mdDialog',
        '$filter', 'toaster', '$state', 'i18nService', '$DCStateService',
        '$DCConnectionManagerService', '$DCManagementService'];
    
    function EditConnectionPopupController($scope, $mdDialog, $DCSS, CS) {
        $scope.ecp = this;
        $scope.ecp.connection = $DCSS.state.activeConnection;
        $scope.compiled = false;
        var compiledUUID = null;

        $scope.closeDialog = function(action) {
            CS.removeCompiledDirective(compiledUUID);
            switch(action) {
                case 'hide':
                    $mdDialog.hide();
                    break;
                case 'cancel':
                default:
                    $mdDialog.cancel();
            }
        };

        compileDirective();
        function compileDirective() {
            var attrs = [
                {attrName: "mode", attrValue: "edit"},
                {attrName: "database-type", attrValue: "{{ecp.connection.databaseType}}"},
                {attrName: "connection", attrValue: "ecp.connection"},
                {attrName: "closeDialog", attrValue: "ecp.closeDialog"}
            ];

            CS.compileDirective($scope, '#editConnectionContent', $scope.ecp.connection.databaseType, attrs).then(function(data){
                compiledUUID = data.UUID;
            }, function(error){
                //compilation failed.
            });
        }
    }

    EditConnectionPopupController.$inject = ['$scope', '$mdDialog', '$DCStateService',
        'dcCompilationService'];
    
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