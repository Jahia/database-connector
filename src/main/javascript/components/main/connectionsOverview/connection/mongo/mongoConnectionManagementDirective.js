(function () {
    'use strict';
    var mongoConnectionManagement = function ($log, contextualData) {

        var directive = {
            restrict: 'E',
            templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/mongo/mongoConnectionManagement.html',
            controller: mongoConnectionManagementController,
            scope: {
                mode: '@',
                connection: '='
            },
            controllerAs: 'cmcc',
            bindToController: true,
            link: linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {
        }

    };

    angular
        .module('databaseConnector')
        .directive('mongoConnectionManagement', ['$log', 'contextualData', mongoConnectionManagement]);

    var mongoConnectionManagementController = function ($scope, contextualData, dcDataFactory, toaster) {
        var cmcc = this;
        cmcc.imageUrl = contextualData.context + '/modules/database-connector/images/' + cmcc.connection.databaseType + '/logo_60.png';
        cmcc.isEmpty = {};
        cmcc.spinnerOptions = {
            showSpinner: false,
            mode: 'indeterminate'
        };

        cmcc.validations = {
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
                'pattern': 'User should contain alphanumeric characters and consist of 4-10 characters'
            },
            authDb: {
                'required': 'Field is required'
            },
            replicaSet: {
                name: {
                    'required': 'Field is required'
                }
            },
            members: {
                host: {
                    'required': 'Field is required'
                },
                port: {
                    'pattern': 'This should consist of a number ranging from 1 - 65535'
                }
            },
            connectTimeoutMS: {
                'required': 'Field is required',
                'pattern' : 'Timeout is in seconds'
            },
            socketTimeoutMS: {
                'required': 'Field is required',
                'pattern' : 'Timeout is in seconds'

            },
            maxPoolSize: {
                'required': 'Field is required'
            },
            minPoolSize: {
                'required': 'Field is required'
            },
            waitQueueTimeoutMS: {
                'required': 'Field is required',
                'pattern' : 'Timeout is in seconds'
            }
            
        };

        cmcc.createMongoConnection = createMongoConnection;
        cmcc.editMongoConnection = editMongoConnection;
        cmcc.testMongoConnection = testMongoConnection;
        cmcc.cancel = cancel;
        cmcc.updateIsEmpty = updateIsEmpty;
        cmcc.updateImportedConnection = updateImportedConnection;
        cmcc.addReplicaMember = addReplicaMember;
        cmcc.removeReplicaMember = removeReplicaMember;

        init();

        function init() {

            if (_.isUndefined(cmcc.connection.port) || cmcc.connection.port == null) {
                cmcc.connection.port = "27017";
            }

            cmcc.isEmpty.password = updateIsEmpty('password');
            cmcc.isEmpty.user = updateIsEmpty('user');
            if (cmcc.mode === 'edit' || cmcc.mode === 'import-edit') {
                cmcc.connection.oldId = angular.copy(cmcc.connection.id);
            } else {
                cmcc.connection.isConnected = true;
            }
            if (_.isUndefined(cmcc.connection.options) || cmcc.connection.options == null || _.isString(cmcc.connection.options) && cmcc.connection.options.trim() == '') {
                console.log("the advanced options are empty! ");
                cmcc.connection.options = {};
            } else {
                cmcc.connection.options = JSON.parse(cmcc.connection.options);
            }
            if (_.isUndefined(cmcc.connection.options.repl)) {
                cmcc.connection.options.repl = {
                    replicaSet: null,
                    members: []
                }
            }
            if (_.isUndefined(cmcc.connection.options.conn)) {
                cmcc.connection.options.conn = {
                    conn: {}
                }
            }
            if (_.isUndefined(cmcc.connection.options.connPool)) {
                cmcc.connection.options.connPool = {
                    connPool: {}
                }
            }
        }

        function createMongoConnection() {
            if (cmcc.mode === 'import-edit') {
                return;
            }
            cmcc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/mongodb/add';

            var data = angular.copy(cmcc.connection);
            data.options = prepareOptions(data.options);
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: data
            }).then(function (response) {

                cmcc.spinnerOptions.showSpinner = false;
                $scope.$emit('connectionSuccessfullyCreated', null);
                showConfirmationToast(response.connectionVerified);
            }, function (response) {
                cmcc.spinnerOptions.showSpinner = false;
                console.log('error', response);
                toaster.pop({
                    type: 'error',
                    title: 'Connection is invalid!',
                    toastId: 'cti',
                    timeout: 3000
                });
            });
        }

        function editMongoConnection() {
            if (cmcc.mode === 'import-edit') {
                return;
            }
            cmcc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/mongodb/edit';
            var data = angular.copy(cmcc.connection);
            data.options = prepareOptions(data.options);
            dcDataFactory.customRequest({
                url: url,
                method: 'PUT',
                data: data
            }).then(function (response) {
                cmcc.spinnerOptions.showSpinner = false;
                $scope.$emit('connectionSuccessfullyCreated', null);
                showConfirmationToast(response.connectionVerified);
            }, function (response) {
                cmcc.spinnerOptions.showSpinner = false;
                console.log('error', response);
                toaster.pop({
                    type: 'error',
                    title: 'Connection is invalid!',
                    toastId: 'cti',
                    timeout: 3000
                });
            });
        }

        function testMongoConnection() {
            cmcc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/mongodb/testconnection';
            var data = angular.copy(cmcc.connection);
            data.options = prepareOptions(data.options);
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
                cmcc.spinnerOptions.showSpinner = false;
            }, function (response) {
                console.log('error', response);
                cmcc.spinnerOptions.showSpinner = false;
            });
        }

        function cancel() {
            if (cmcc.mode === 'import-edit') {
                $scope.$emit('importConnectionClosed', null);
            } else {
                $scope.$emit('creationCancelled', null);
            }
        }

        function updateIsEmpty(property) {
            return cmcc.isEmpty[property] = cmcc.connection[property] === undefined || cmcc.connection[property] === null || (typeof cmcc.connection[property] === 'string' && cmcc.connection[property].trim().length === 0);
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

        function prepareOptions(options) {
            if (_.isEmpty(options.repl) || options.repl == null) {
                delete options.repl
            } else {
                if (_.isEmpty(options.repl.replicaSet) || options.repl.replicaSet == null) {
                    delete options.repl.replicaSet
                }
                if (_.isEmpty(options.repl.members) || options.repl.members == null) {
                    delete options.repl.members
                }
            }
            if (_.isEmpty(options.conn) || options.conn == null) {
                delete options.conn;
            } else {
                if (_.isEmpty(options.conn.connectTimeoutMS) || options.conn.connectTimeoutMS == null) {
                    delete options.conn.connectTimeoutMS
                }
                if (_.isEmpty(options.conn.socketTimeoutMS) || options.conn.socketTimeoutMS == null) {
                    delete options.conn.socketTimeoutMS
                }
            }

            if (_.isEmpty(options.connPool) || options.connPool == null) {
                delete options.connPool

            } else {
                if (_.isEmpty(options.connPool.maxPoolSize) || options.connPool.maxPoolSize == null) {
                    delete options.connPool.maxPoolSize
                }
                if (_.isEmpty(options.connPool.minPoolSize) || options.connPool.minPoolSize == null) {
                    delete options.connPool.minPoolSize
                }
                if (_.isEmpty(options.connPool.waitQueueTimeoutMS) || options.connPool.waitQueueTimeoutMS == null) {
                    delete options.connPool.waitQueueTimeoutMS
                }
            }

            return JSON.stringify(options);
        }

        function updateImportedConnection() {
            $scope.$emit('importConnectionClosed', cmcc.connection);
        }

        function addReplicaMember() {
            if (!_.isUndefined(cmcc.connection.options.repl.members)) {
                cmcc.connection.options.repl.members.push({});
            }
            else {
                console.log("repl Members is Undefined !", cmcc.connection.options.repl.members);
            }
        }

        function removeReplicaMember(index) {
            if (!_.isUndefined(cmcc.connection.options.repl.members)) {
                cmcc.connection.options.repl.members.splice(index, 1);
            }
            else {
                console.log("repl Members is Undefined !", cmcc.connection.options.repl.members);
            }
        }

    };

    mongoConnectionManagementController.$inject = ['$scope', 'contextualData', 'dcDataFactory', 'toaster'];

})();