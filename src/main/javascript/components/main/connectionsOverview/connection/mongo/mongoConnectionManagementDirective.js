(function() {
    'use strict';
    var mongoConnectionManagement = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/mongo/mongoConnectionManagement.html',
            controller      : mongoConnectionManagementController,
            scope           : {
                mode: '@',
                connection: '='
            },
            controllerAs    : 'cmcc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('mongoConnectionManagement', ['$log', 'contextualData', mongoConnectionManagement]);

    var mongoConnectionManagementController = function($scope, contextualData, dcDataFactory, toaster) {
        var cmcc = this;
        cmcc.imageUrl = contextualData.context + '/modules/database-connector/images/' + cmcc.connection.databaseType + '/logo_60.png';
        cmcc.isEmpty = {};
        cmcc.spinnerOptions = {
            showSpinner: false,
            mode: 'indeterminate'
        };
        
        cmcc.validations = {
            host: {
                'required'      : 'Field is required',
                'md-maxlength'  : 'This has to be less than 15 characters.',
                'minlength'     : 'This has to be more than 4 characters.'
            },
            port:{
                'required'      : 'Field is required',
                'md-maxlength'  : 'This has to be less than 5 digits.',
                'pattern'       : 'This should consist of a number ranging from 1 - 65535'
            },
            id: {
                'required'      : 'Field is required',
                'connection-id-validator' : 'This connection Id is already being used',
                'pattern'       : 'It should contain alphanumeric characters only.',
                'md-maxlength'  : 'This has to be less than 30 characters.',
                'minlength'     : 'This has to be more than 3 characters.'

            },
            dbName: {
                'required'      : 'Field is required',
                'pattern'       : 'It should contain alphanumeric characters only.',
                'md-maxlength'  : 'This has to be less than 30 characters.',
                'minlength'     : 'This has to be more than 2 characters.'

            },
            authDb:{
                'required': 'Field is required'
            }
        };

        cmcc.createMongoConnection = createMongoConnection;
        cmcc.editMongoConnection = editMongoConnection;
        cmcc.testMongoConnection = testMongoConnection;
        cmcc.cancelCreation = cancelCreation;
        cmcc.updateIsEmpty = updateIsEmpty;

        init();

        function init() {
            cmcc.isEmpty.password = updateIsEmpty('password');
            cmcc.isEmpty.user = updateIsEmpty('user');
            if (cmcc.mode === 'edit') {
                cmcc.connection.oldId = angular.copy(cmcc.connection.id);
            } else {
                cmcc.connection.isConnected = true;
            }
        }

        function createMongoConnection() {
            cmcc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/mongodb/add';
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: cmcc.connection
            }).then(function(response){
                cmcc.spinnerOptions.showSpinner = false;
                $scope.$emit('connectionSuccessfullyCreated', null);
                showConfirmationToast(response.connectionVerified);
            }, function(response){
                cmcc.spinnerOptions.showSpinner = false;
                console.log('error', response);
                toaster.pop({
                    type   : 'error',
                    title: 'Connection is invalid!',
                    toastId: 'cti',
                    timeout: 3000
                });
            });
        }

        function editMongoConnection() {
            cmcc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/mongodb/edit';
            dcDataFactory.customRequest({
                url: url,
                method: 'PUT',
                data: cmcc.connection
            }).then(function(response){
                cmcc.spinnerOptions.showSpinner = false;
                $scope.$emit('connectionSuccessfullyCreated', null);
                showConfirmationToast(response.connectionVerified);
            }, function(response){
                cmcc.spinnerOptions.showSpinner = false;
                console.log('error', response);
                toaster.pop({
                    type   : 'error',
                    title: 'Connection is invalid!',
                    toastId: 'cti',
                    timeout: 3000
                });
            });
        }

        function testMongoConnection() {
            cmcc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/mongodb/testconnection';
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: cmcc.connection
            }).then(function(response){
                if (response.result) {
                    toaster.pop({
                        type   : 'success',
                        title: 'Connection is valid!',
                        toastId: 'ctv',
                        timeout: 3000
                    });
                } else {
                    toaster.pop({
                        type   : 'error',
                        title: 'Connection is invalid!',
                        toastId: 'cti',
                        timeout: 3000
                    });
                }
                cmcc.spinnerOptions.showSpinner = false;
            }, function(response){
                console.log('error', response);
                cmcc.spinnerOptions.showSpinner = false;
            });
        }

        function cancelCreation() {
         $scope.$emit('creationCancelled', null);
        }

        function updateIsEmpty(property) {
            return cmcc.isEmpty[property] = cmcc.connection[property] === undefined || cmcc.connection[property] === null || (typeof cmcc.connection[property] === 'string' && cmcc.connection[property].trim().length === 0);
        }

        function showConfirmationToast(verified) {
            if (verified) {
                toaster.pop({
                    type   : 'success',
                    title: 'Connection Successfully Saved!',
                    body: 'Connection verification was successful!',
                    toastId: 'cm',
                    timeout: 4000
                });
            } else {
                toaster.pop({
                    type   : 'warning',
                    title: 'Connection Successfully Saved!',
                    body: 'Connection verification failed!',
                    toastId: 'cm',
                    timeout: 4000
                });
            }
        }
    };

    mongoConnectionManagementController.$inject = ['$scope', 'contextualData','dcDataFactory', 'toaster'];

})();