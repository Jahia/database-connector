(function () {
    'use strict';
    var mongoConnectionManagement = function ($log, contextualData) {

        var directive = {
            restrict: 'E',
            templateUrl: contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/mongo/mongoConnectionManagement.html',
            controller: mongoConnectionManagementController,
            controllerAs: 'cmcc',
            bindToController: {
                mode: '@',
                connection: '='
            },
            link: linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {
        }

    };

    angular
        .module('databaseConnector')
        .directive('mongoConnectionManagement', ['$log', 'contextualData', mongoConnectionManagement]);

    var mongoConnectionManagementController = function ($scope, contextualData, dcDataFactory, toaster, i18n) {
        var cmcc = this;

        cmcc.isEmpty = {};
        cmcc.spinnerOptions = {
            showSpinner: false,
            mode: 'indeterminate'
        };
        cmcc.tabLabels = {
            settings: i18n.message('dc_databaseConnector.label.settings'),
            advancedSettings: i18n.message('dc_databaseConnector.label.advancedSettings')
        };
        cmcc.validations = {
            host: {
                'required': i18n.message('dc_databaseConnector.label.validation.required'),
                'md-maxlength': i18n.format('dc_databaseConnector.label.validation.maxLength', '15'),
                'minlength': i18n.format('dc_databaseConnector.label.validation.minLength', '4')
            },
            port: {
                'pattern': i18n.format('dc_databaseConnector.label.validation.range', '1|65535'),
            },
            id: {
                'required': i18n.message('dc_databaseConnector.label.validation.required'),
                'connection-id-validator': i18n.message('dc_databaseConnector.label.validation.connectionIdInUse'),
                'pattern': i18n.message('dc_databaseConnector.label.validation.alphanumeric'),
                'md-maxlength': i18n.format('dc_databaseConnector.label.validation.minLength', '30')
            },
            dbName: {
                'required': i18n.message('dc_databaseConnector.label.validation.required'),
                'pattern': i18n.message('dc_databaseConnector.label.validation.alphanumeric'),
                'md-maxlength': i18n.format('dc_databaseConnector.label.validation.minLength', '30')
            },
            user: {
                'pattern': i18n.message('User should contain alphanumeric characters (underscore and hyphen permitted)'),
                'minlength': i18n.format('dc_databaseConnector.label.validation.minLength', '4'),
                'md-maxlength': i18n.format('dc_databaseConnector.label.validation.minLength', '30')
            },
            authDb: {
                'required': i18n.message('dc_databaseConnector.label.validation.required')
            },
            replicaSet: {
                name: {
                    'required': i18n.message('dc_databaseConnector.label.validation.required')
                }
            },
            members: {
                host: {
                    'required': i18n.message('dc_databaseConnector.label.validation.required')
                },
                port: {
                    'pattern': i18n.format('dc_databaseConnector.label.validation.range', '1|65535')
                }
            },
            connectTimeoutMS: {
                'pattern' : i18n.message('dc_databaseConnector.label.validation.integer')
            },
            socketTimeoutMS: {
                'pattern' : i18n.message('dc_databaseConnector.label.validation.integer')
            },
            maxPoolSize: {
                'pattern' : i18n.message('dc_databaseConnector.label.validation.integer')

            },
            minPoolSize: {
                'pattern' : i18n.message('dc_databaseConnector.label.validation.integer')

            },
            waitQueueTimeoutMS: {
                'pattern' : i18n.message('dc_databaseConnector.label.validation.integer')
            }
        };

        cmcc.createMongoConnection = createMongoConnection;
        cmcc.editMongoConnection = editMongoConnection;
        cmcc.testMongoConnection = testMongoConnection;
        cmcc.cancel = cancel;
        cmcc.updateIsEmpty = updateIsEmpty;
        cmcc.updateImportedConnection = updateImportedConnection;
        cmcc.initReplicaMember = initReplicaMember;
        cmcc.addReplicaMember = addReplicaMember;
        cmcc.removeReplicaMember = removeReplicaMember;
        cmcc.updateReplicaSetOptions = updateReplicaSetOptions;
        cmcc.getMessage = i18n.message;

        cmcc.$onInit = function init() {
            var url = contextualData.context + '/modules/databaseconnector/mongodb/writeconcernoptions';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function (response) {
                cmcc.spinnerOptions.showSpinner = false;
                cmcc.writeConcernOptions = response;
            }, function (response) {
                cmcc.spinnerOptions.showSpinner = false;
            });
            if (_.isUndefined(cmcc.connection.port) || cmcc.connection.port == null) {
                cmcc.connection.port = "27017";
            }

            cmcc.isEmpty.password = updateIsEmpty('password');
            cmcc.isEmpty.user = updateIsEmpty('user');
            if (cmcc.mode === 'import-edit') {
                cmcc.connection.oldId = null;
            }
            if (cmcc.mode === 'edit') {
                cmcc.connection.oldId = angular.copy(cmcc.connection.id);
            } else {
                cmcc.connection.isConnected = true;
            }
            if (_.isUndefined(cmcc.connection.options) || cmcc.connection.options == null || _.isString(cmcc.connection.options) && cmcc.connection.options.trim() == '') {
                cmcc.connection.options = {};
            } else if (_.isString(cmcc.connection.options)){
                cmcc.connection.options = JSON.parse(cmcc.connection.options);
            }
            cmcc.isReplicaSet = !_.isUndefined(cmcc.connection.options.repl);
            if (_.isUndefined(cmcc.connection.options.conn)) {
                cmcc.connection.options.conn = {}
            }
            if (_.isUndefined(cmcc.connection.options.connPool)) {
                cmcc.connection.options.connPool = {}
            }
        };

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
            if(data.user == null || _.isEmpty(data.user)) {
                data.authDb="";
                data.password="";
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
                    title: i18n.message('dc_databaseConnector.toast.title.connectionInvalid'),
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
            if(data.user == null || _.isEmpty(data.user)) {
                 data.authDb="";
                 data.password="";
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
                    title: i18n.message('dc_databaseConnector.toast.title.connectionInvalid'),
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
                        title: i18n.message('dc_databaseConnector.toast.title.connectionValid'),
                        toastId: 'ctv',
                        timeout: 3000
                    });
                } else {
                    toaster.pop({
                        type: 'error',
                        title: i18n.message('dc_databaseConnector.toast.title.connectionInvalid'),
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
                    title: i18n.message('dc_databaseConnector.toast.title.connectionSavedSuccessfully'),
                    body: i18n.message('dc_databaseConnector.toast.message.connectionVerificationSuccessful'),
                    toastId: 'cm',
                    timeout: 4000
                });
            } else {
                toaster.pop({
                    type: 'warning',
                    title: i18n.message('dc_databaseConnector.toast.title.connectionSavedSuccessfully'),
                    body: i18n.message('dc_databaseConnector.toast.message.connectionVerificationFailed'),
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
            if(cmcc.connection.user == null || _.isEmpty(cmcc.connection.user)) {
                cmcc.connection.authDb="";
                cmcc.connection.password="";
            }
            $scope.$emit('importConnectionClosed', cmcc.connection);
        }

        function addReplicaMember() {
            if (!_.isUndefined(cmcc.connection.options.repl.members)) {
                cmcc.connection.options.repl.members.push({});
            }
        }

        function initReplicaMember() {
            if (!_.isUndefined(cmcc.connection.options.repl.members) && _.isEmpty(cmcc.connection.options.repl.members)) {

                cmcc.connection.options.repl.members.push({});
            }
        }

        function removeReplicaMember(index) {
            if (!_.isUndefined(cmcc.connection.options.repl.members)) {
                cmcc.connection.options.repl.members.splice(index, 1);
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

    mongoConnectionManagementController.$inject = ['$scope', 'contextualData', 'dcDataFactory', 'toaster', 'i18nService'];

})();
