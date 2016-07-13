(function () {
    'use strict';
    var redisConnectionManagement = function ($log, contextualData) {

        var directive = {
            restrict: 'E',
            templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/redis/redisConnectionManagement.html',
            controller: redisConnectionManagementController,
            scope: {
                mode: '@',
                connection: '='
            },
            controllerAs: 'rcm',
            bindToController: true,
            link: linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {
        }

    };

    angular
        .module('databaseConnector')
        .directive('redisConnectionManagement', ['$log', 'contextualData', redisConnectionManagement]);

    var redisConnectionManagementController = function ($scope, contextualData, dcDataFactory, toaster) {
        var rcm = this;
        rcm.imageUrl = contextualData.context + '/modules/database-connector/images/' + rcm.connection.databaseType + '/logo_60.png';
        rcm.isEmpty = {};
        rcm.spinnerOptions = {
            showSpinner: false,
            mode: 'indeterminate'
        };

        rcm.validations = {
            host: {
                'required': 'Field is required',
                'md-maxlength': 'This has to be less than 15 characters.',
                'minlength': 'This has to be more than 4 characters.'
            },
            port: {
                'pattern': 'This should consist of a number ranging from 1 - 65535'
            },
            id: {
                'required': 'Field is required',
                'connection-id-validator': 'This connection Id is already being used',
                'pattern': 'It should contain alphanumeric characters only.',
                'md-maxlength': 'This has to be less than 30 characters.'

            },
            dbName: {
                'required': 'Field is required',
                'pattern': 'It should contain alphanumeric characters only.',
                'md-maxlength': 'This has to be less than 30 characters.'
            },
            user: {
                'pattern': 'User should contain alphanumeric characters (underscore and hyphen permitted)',
                'minlength': 'This has to be more than 4 characters.',
                'md-maxlength': 'This has to be less than 30 characters.'
            }
            
        };

        rcm.createRedisConnection = createRedisConnection;
        rcm.editRedisConnection = editRedisConnection;
        rcm.testRedisConnection = testRedisConnection;
        rcm.cancel = cancel;
        rcm.updateIsEmpty = updateIsEmpty;
        rcm.updateImportedConnection = updateImportedConnection;
        
        init();

        function init() {
            if (_.isUndefined(rcm.connection.port) || rcm.connection.port == null) {
                rcm.connection.port = "6379";
            }

            rcm.isEmpty.password = updateIsEmpty('password');
            rcm.isEmpty.user = updateIsEmpty('user');
            if (rcm.mode === 'import-edit') {
                rcm.connection.oldId = null;
            }
            if (rcm.mode === 'edit') {
                rcm.connection.oldId = angular.copy(rcm.connection.id);
            } else {
                rcm.connection.isConnected = true;
            }

        }

        function createRedisConnection() {
            if (rcm.mode === 'import-edit') {
                return;
            }
            rcm.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/redisdb/add';

            var data = angular.copy(rcm.connection);
            // if (options == null) {
            //     delete data.options;
            // } else {
            //     data.options = options;
            // }
            console.log("data",data);
            if(data.user == null || _.isEmpty(data.user)) {
                data.authDb="";
                data.password="";
            }
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: data
            }).then(function (response) {

                rcm.spinnerOptions.showSpinner = false;
                $scope.$emit('connectionSuccessfullyCreated', null);
                showConfirmationToast(response.connectionVerified);
            }, function (response) {
                rcm.spinnerOptions.showSpinner = false;
                console.log('error', response);
                toaster.pop({
                    type: 'error',
                    title: 'Connection is invalid!',
                    toastId: 'cti',
                    timeout: 3000
                });
            });
        }

        function editRedisConnection() {
            if (rcm.mode === 'import-edit') {
                return;
            }
            rcm.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/redisdb/edit';
            var data = angular.copy(rcm.connection);
            // if (options == null) {
            //     delete data.options;
            // } else {
            //     data.options = options;
            // }
            if(data.user == null || _.isEmpty(data.user)) {
                 data.authDb="";
                 data.password="";
            }
                dcDataFactory.customRequest({
                url: url,
                method: 'PUT',
                data: data
            }).then(function (response) {
                rcm.spinnerOptions.showSpinner = false;
                $scope.$emit('connectionSuccessfullyCreated', null);
                showConfirmationToast(response.connectionVerified);
            }, function (response) {
                rcm.spinnerOptions.showSpinner = false;
                console.log('error', response);
                toaster.pop({
                    type: 'error',
                    title: 'Connection is invalid!',
                    toastId: 'cti',
                    timeout: 3000
                });
            });
        }

        function testRedisConnection() {
            rcm.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/redisdb/testconnection';
            var data = angular.copy(rcm.connection);
            // if (options == null) {
            //     delete data.options;
            // } else {
            //     data.options = options;
            // }
            if(data.user == null || _.isEmpty(data.user)) {
                data.authDb="";
                data.password="";
            }
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: data
            }).then(function (response) {
                if (response.result) {
                    toaster.pop({
                        type: 'success',
                        title: 'Connection is valid!',
                        toastId: 'ctv',
                        timeout: 3000
                    });
                } else {
                    toaster.pop({
                        type: 'error',
                        title: 'Connection is invalid!',
                        toastId: 'cti',
                        timeout: 3000
                    });
                }
                rcm.spinnerOptions.showSpinner = false;
            }, function (response) {
                console.log('error', response);
                rcm.spinnerOptions.showSpinner = false;
            });
        }

        function cancel() {
            if (rcm.mode === 'import-edit') {
                $scope.$emit('importConnectionClosed', null);
            } else {
                $scope.$emit('creationCancelled', null);
            }
        }

        function updateIsEmpty(property) {
            return rcm.isEmpty[property] = rcm.connection[property] === undefined || rcm.connection[property] === null || (typeof rcm.connection[property] === 'string' && rcm.connection[property].trim().length === 0);
        }

        function showConfirmationToast(verified) {
            if (verified) {
                toaster.pop({
                    type: 'success',
                    title: 'Connection Successfully Saved!',
                    body: 'Connection verification was successful!',
                    toastId: 'cm',
                    timeout: 4000
                });
            } else {
                toaster.pop({
                    type: 'warning',
                    title: 'Connection Successfully Saved!',
                    body: 'Connection verification failed!',
                    toastId: 'cm',
                    timeout: 4000
                });
            }
        }


        function updateImportedConnection() {
            if(rcm.connection.user == null || _.isEmpty(rcm.connection.user)) {
                rcm.connection.authDb="";
                rcm.connection.password="";
                console.log('after update', rcm.connection);
            }
            $scope.$emit('importConnectionClosed', rcm.connection);
        }

    };

    redisConnectionManagementController.$inject = ['$scope', 'contextualData', 'dcDataFactory', 'toaster'];

})();