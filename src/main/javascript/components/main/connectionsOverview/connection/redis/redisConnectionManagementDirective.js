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

    var redisConnectionManagementController = function ($scope, contextualData, dcDataFactory, toaster, i18n) {
        var rcm = this;
        rcm.imageUrl = contextualData.context + '/modules/database-connector/images/' + rcm.connection.databaseType + '/logo_60.png';
        rcm.isEmpty = {};
        rcm.spinnerOptions = {
            showSpinner: false,
            mode: 'indeterminate'
        };

        rcm.validations = {
            host: {
                'required': i18n.message('dc_databaseConnector.label.validation.required'),
                'md-maxlength': i18n.format('dc_databaseConnector.label.validation.maxLength', '15'),
                'minlength': i18n.format('dc_databaseConnector.label.validation.minLength', '4')
            },
            port: {
                'pattern': i18n.format('dc_databaseConnector.label.validation.range', '1|65535')
            },
            id: {
                'required': i18n.message('dc_databaseConnector.label.validation.required'),
                'connection-id-validator': i18n.message('dc_databaseConnector.label.validation.connectionIdInUse'),
                'pattern': i18n.message('dc_databaseConnector.label.validation.alphanumeric'),
                'md-maxlength': i18n.format('dc_databaseConnector.label.validation.maxLength', '30')

            },
            dbName: {
                'required': i18n.message('dc_databaseConnector.label.validation.required'),
                'pattern': i18n.message('dc_databaseConnector.label.validation.positiveNumber')
            },
            redisTimeout: {
                'pattern' : i18n.message('dc_databaseConnector.label.validation.integer')
            },
            redisWeight: {
                'pattern' : i18n.message('dc_databaseConnector.label.validation.integer')
            },
            refreshPeriod: {
                'pattern' : i18n.message('dc_databaseConnector.label.validation.integer')
            }
        };

        rcm.createRedisConnection    = createRedisConnection;
        rcm.editRedisConnection      = editRedisConnection;
        rcm.testRedisConnection      = testRedisConnection;
        rcm.cancel                   = cancel;
        rcm.updateIsEmpty            = updateIsEmpty;
        rcm.updateImportedConnection = updateImportedConnection;
        rcm.updateClusterOptions     = updateClusterOptions;
        rcm.getMessage               = i18n.message;
        
        init();

        function init() {
            if (_.isUndefined(rcm.connection.port) || rcm.connection.port == null) {
                rcm.connection.port = "6379";
            }
            if (rcm.connection.timeout == null) {
                rcm.connection.timeout = '';
            }
            if (rcm.connection.weight == null) {
                rcm.connection.weight = '';
            }
            rcm.isEmpty.password = updateIsEmpty('password');
            if (rcm.mode === 'import-edit') {
                rcm.connection.oldId = null;
            }
            if (rcm.mode === 'edit') {
                rcm.connection.oldId = angular.copy(rcm.connection.id);
            } else {
                rcm.connection.isConnected = true;
            }

            if (_.isUndefined(rcm.connection.options) || rcm.connection.options == null || _.isString(rcm.connection.options) && rcm.connection.options.trim() == '') {
                rcm.connection.options = {};
            } else if (_.isString(rcm.connection.options)){
                rcm.connection.options = JSON.parse(rcm.connection.options);
                rcm.isCluster = !_.isUndefined(rcm.connection.options.cluster);
            }

        }

        function createRedisConnection() {
            if (rcm.mode === 'import-edit') {
                return;
            }
            rcm.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/redis/add';
            var data = angular.copy(rcm.connection);
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
                rcm.spinnerOptions.showSpinner = false;
                $scope.$emit('connectionSuccessfullyCreated', null);
                showConfirmationToast(response.connectionVerified);
            }, function (response) {
                rcm.spinnerOptions.showSpinner = false;
                toaster.pop({
                    type: 'error',
                    title: i18n.message('dc_databaseConnector.toast.title.connectionInvalid'),
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
            var url = contextualData.context + '/modules/databaseconnector/redis/edit';
            var data = angular.copy(rcm.connection);
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
                rcm.spinnerOptions.showSpinner = false;
                $scope.$emit('connectionSuccessfullyCreated', null);
                showConfirmationToast(response.connectionVerified);
            }, function (response) {
                rcm.spinnerOptions.showSpinner = false;
                toaster.pop({
                    type: 'error',
                    title: i18n.message('dc_databaseConnector.toast.title.connectionInvalid'),
                    toastId: 'cti',
                    timeout: 3000
                });
            });
        }

        function testRedisConnection() {
            rcm.spinnerOptions.showSpinner = true;
            var url = contextualData.context + '/modules/databaseconnector/redis/testconnection';
            var data = angular.copy(rcm.connection);
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
                rcm.spinnerOptions.showSpinner = false;
            }, function (response) {
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

        function updateImportedConnection() {
            $scope.$emit('importConnectionClosed', rcm.connection);
        }

        function updateClusterOptions(option) {
            switch(option) {
                case 'cluster' :
                    if (rcm.isCluster) {
                        rcm.connection.options.cluster = {
                            refreshClusterView  : true,
                            refreshPeriod       : 60
                        }
                    } else {
                        delete rcm.connection.options.cluster;
                    }
                    break;
                case 'refreshClusterView' :
                    if (rcm.connection.options.cluster.refreshClusterView) {
                        rcm.connection.options.cluster.refreshPeriod = 60
                    } else {
                        delete rcm.connection.options.cluster.refreshClusterView;
                    }
                    break;
            }
        }

        function prepareOptions(options) {
            //Check if cluster is enabled
            if (!rcm.isCluster) {
                delete options.cluster
            }
            return _.isEmpty(options) ? null : JSON.stringify(options);
        }
    };

    redisConnectionManagementController.$inject = ['$scope', 'contextualData', 'dcDataFactory', 'toaster', 'i18nService'];

})();