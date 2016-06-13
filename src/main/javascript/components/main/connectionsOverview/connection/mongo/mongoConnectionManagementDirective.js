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
                'required': 'Field is required',
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
            if (_.isEmpty(cmcc.connection.options.repl) || cmcc.connection.options.repl == null) {
                delete cmcc.connection.options.repl
            } else {
                if (_.isEmpty(cmcc.connection.options.repl.replicaSet) || cmcc.connection.options.repl.replicaSet == null) {
                    delete cmcc.connection.options.repl.replicaSet
                }
                if (_.isEmpty(cmcc.connection.options.repl.members) || cmcc.connection.options.repl.members == null) {
                    delete cmcc.connection.options.repl.members
                }
            }
            if (_.isEmpty(cmcc.connection.options.conn) || cmcc.connection.options.conn == null) {
                delete cmcc.connection.options.conn;
            } else {
                if (_.isEmpty(cmcc.connection.options.conn.connectTimeoutMS) || cmcc.connection.options.conn.connectTimeoutMS == null) {
                    delete cmcc.connection.options.conn.connectTimeoutMS
                }
                if (_.isEmpty(cmcc.connection.options.conn.socketTimeoutMS) || cmcc.connection.options.conn.socketTimeoutMS == null) {
                    delete cmcc.connection.options.conn.socketTimeoutMS
                }
            }

            if (_.isEmpty(cmcc.connection.options.connPool) || cmcc.connection.options.connPool == null) {
                delete cmcc.connection.options.connPool

            } else {
                if (_.isEmpty(cmcc.connection.options.connPool.maxPoolSize) || cmcc.connection.options.connPool.maxPoolSize == null) {
                    delete cmcc.connection.options.connPool.maxPoolSize
                }
                if (_.isEmpty(cmcc.connection.options.connPool.minPoolSize) || cmcc.connection.options.connPool.minPoolSize == null) {
                    delete cmcc.connection.options.connPool.minPoolSize
                }
                if (_.isEmpty(cmcc.connection.options.connPool.waitQueueTimeoutMS) || cmcc.connection.options.connPool.waitQueueTimeoutMS == null) {
                    delete cmcc.connection.options.connPool.waitQueueTimeoutMS
                }
            }

            var data = angular.copy(cmcc.connection);
            data.options = JSON.stringify(data.options);
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: cmcc.connection
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
            if (_.isEmpty(cmcc.connection.options.repl) || cmcc.connection.options.repl == null) {
                delete cmcc.connection.options.repl
            } else {
                if (_.isEmpty(cmcc.connection.options.repl.replicaSet) || cmcc.connection.options.repl.replicaSet == null) {
                    delete cmcc.connection.options.repl.replicaSet
                }
                if (_.isEmpty(cmcc.connection.options.repl.members) || cmcc.connection.options.repl.members == null) {
                    delete cmcc.connection.options.repl.members
                }
            }
            if (_.isEmpty(cmcc.connection.options.conn) || cmcc.connection.options.conn == null) {
                delete cmcc.connection.options.conn;
            } else {
                if (_.isEmpty(cmcc.connection.options.conn.connectTimeoutMS) || cmcc.connection.options.conn.connectTimeoutMS == null) {
                    delete cmcc.connection.options.conn.connectTimeoutMS
                }
                if (_.isEmpty(cmcc.connection.options.conn.socketTimeoutMS) || cmcc.connection.options.conn.socketTimeoutMS == null) {
                    delete cmcc.connection.options.conn.socketTimeoutMS
                }
            }

            if (_.isEmpty(cmcc.connection.options.connPool) || cmcc.connection.options.connPool == null) {
                delete cmcc.connection.options.connPool

            } else {
                if (_.isEmpty(cmcc.connection.options.connPool.maxPoolSize) || cmcc.connection.options.connPool.maxPoolSize == null) {
                    delete cmcc.connection.options.connPool.maxPoolSize
                }
                if (_.isEmpty(cmcc.connection.options.connPool.minPoolSize) || cmcc.connection.options.connPool.minPoolSize == null) {
                    delete cmcc.connection.options.connPool.minPoolSize
                }
                if (_.isEmpty(cmcc.connection.options.connPool.waitQueueTimeoutMS) || cmcc.connection.options.connPool.waitQueueTimeoutMS == null) {
                    delete cmcc.connection.options.connPool.waitQueueTimeoutMS
                }
            }
            var data = angular.copy(cmcc.connection);
            data.options = JSON.stringify(data.options);
            dcDataFactory.customRequest({
                url: url,
                method: 'PUT',
                data: cmcc.connection
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
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: cmcc.connection
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

        function updateImportedConnection() {
            $scope.$emit('importConnectionClosed', cmcc.connection);
        }

        function addReplicaMember() {
            if (!_.isUndefined(cmcc.connection.options.repl.members)) {
                cmcc.connection.options.repl.members.push({})

            }
            else {
                console.log("repl Members is Undefined !", cmcc.connection.options.repl.members);
            }
        }

        function removeReplicaMember() {
            if (!_.isUndefined(cmcc.connection.options.repl.members)) {
                cmcc.connection.options.repl.members.pop()

            }
            else {
                console.log("repl Members is Undefined !", cmcc.connection.options.repl.members);
            }
        }

    };

    mongoConnectionManagementController.$inject = ['$scope', 'contextualData', 'dcDataFactory', 'toaster'];

})();