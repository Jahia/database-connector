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
                'pattern' : 'Timeout is in seconds'
            },
            socketTimeoutMS: {
                'pattern' : 'Timeout is in seconds'

            },
            maxPoolSize: {
                'pattern' : 'Should be an integer'

            },
            minPoolSize: {
                'pattern' : 'Should be an integer'

            },
            waitQueueTimeoutMS: {
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
        cmcc.updateReplicaSetOptions = updateReplicaSetOptions;
        
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
            cmcc.isReplicaSet = !_.isUndefined(cmcc.connection.options.repl);
            if (_.isUndefined(cmcc.connection.options.conn)) {
                cmcc.connection.options.conn = {}
            }
            if (_.isUndefined(cmcc.connection.options.connPool)) {
                cmcc.connection.options.connPool = {}
            }
        }

        function createMongoConnection() {
            if (cmcc.mode === 'import-edit') {
                return;
            }
            cmcc.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/mongodb/add';

            var data = angular.copy(cmcc.connection);
            var options = prepareOptions(data.options);
            if (options == null) {
                delete data.options;
            } else {
                data.options = options;
            }
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
            var options = prepareOptions(data.options);
            if (options == null) {
                delete data.options;
            } else {
                data.options = options;
            }
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
            var options = prepareOptions(data.options);
            if (options == null) {
                delete data.options;
            } else {
                data.options = options;
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
            if (!_.isEmpty(options.repl) && options.repl == null) {
                if (options.repl.replicaSet == null || (_.isString(options.repl.replicaSet) && options.repl.replicaSet.trim().length == 0)) {
                    delete options.repl.replicaSet
                }
                if (_.isEmpty(options.repl.members) || options.repl.members == null) {
                    delete options.repl.members
                }
            }
            //Check if the repl object is empty after we checked the members
            if (_.isEmpty(options.repl) || options.repl == null) {
                delete options.repl
            }

            if (options.conn.connectTimeoutMS == null || (_.isString(options.conn.connectTimeoutMS) && options.conn.connectTimeoutMS.trim().length == 0)) {
                delete options.conn.connectTimeoutMS
            }
            if (options.conn.socketTimeoutMS == null || (_.isString(options.conn.socketTimeoutMS) && options.conn.socketTimeoutMS.trim().length == 0)) {
                delete options.conn.socketTimeoutMS
            }
            //Check if the conn object is empty after we checked the members
            if (_.isEmpty(options.conn) || options.conn == null) {
                delete options.conn;
            }

            if (options.connPool.maxPoolSize == null || (_.isString(options.connPool.maxPoolSize) && options.connPool.maxPoolSize.trim().length == 0)) {
                delete options.connPool.maxPoolSize
            }
            if (options.connPool.minPoolSize == null || (_.isString(options.connPool.minPoolSize) && options.connPool.minPoolSize.trim().length == 0)) {
                delete options.connPool.minPoolSize
            }
            if (options.connPool.waitQueueTimeoutMS == null || (_.isString(options.connPool.waitQueueTimeoutMS) && options.connPool.waitQueueTimeoutMS.trim().length == 0)) {
                delete options.connPool.waitQueueTimeoutMS
            }
            //Check if the connPool object is empty after we checked the members
            if (_.isEmpty(options.connPool) || options.connPool == null) {
                delete options.connPool

            }

            return _.isEmpty(options) ? null : JSON.stringify(options);
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

        function updateReplicaSetOptions() {
            if (cmcc.isReplicaSet) {
                if (_.isUndefined(cmcc.connection.options.repl) || cmcc.connection.options.repl == null) {
                    //create replica set object
                    cmcc.connection.options.repl = {
                        replicaSet: "",
                        members: []
                    }
                }
            } else {
                //remove replicaSetObject
                delete cmcc.connection.options.repl;
            }
        }

    };

    mongoConnectionManagementController.$inject = ['$scope', 'contextualData', 'dcDataFactory', 'toaster'];

})();