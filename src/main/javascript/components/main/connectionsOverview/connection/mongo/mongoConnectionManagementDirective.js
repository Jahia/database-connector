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

    var mongoConnectionManagementController = function($scope, contextualData, dcDataFactory) {
        var cmcc = this;
        cmcc.imageUrl = contextualData.context + '/modules/database-connector/images/' + cmcc.connection.databaseType + '/logo_60.png';
        cmcc.isEmpty = {};

        cmcc.createMongoConnection = createMongoConnection;
        cmcc.editMongoConnection = editMongoConnection;
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
            var url = contextualData.context + '/modules/databaseconnector/mongodb/add';
            dcDataFactory.customRequest({
                url: url,
                method: 'POST',
                data: cmcc.connection
            }).then(function(response){
                $scope.$emit('connectionSuccessfullyCreated', null);
            }, function(response){
                console.log('error', response);
            });
        }

        function editMongoConnection() {
            var url = contextualData.context + '/modules/databaseconnector/mongodb/edit';
            dcDataFactory.customRequest({
                url: url,
                method: 'PUT',
                data: cmcc.connection
            }).then(function(response){
                $scope.$emit('connectionSuccessfullyCreated', null);
            }, function(response){
                console.log('error', response);
            });
        }

/*        function deleteMongoConnection() {
            var url = contextualData.context + '/modules/databaseconnector/remove';
            dcDataFactory.customRequest({
                url: url,
                method: 'DELETE',
                data: cmcc.connection
            }).then(function(response){
                $scope.$emit('connectionSuccessfullyCreated', null);
            }, function(response){
                console.log('error', response);
            });
        }*/
        
         function cancelCreation() {
             $scope.$emit('creationCancelled', null);
         }

        function updateIsEmpty(property) {
            return cmcc.isEmpty[property] = cmcc.connection[property] === undefined || cmcc.connection[property] === null || (typeof cmcc.connection[property] === 'string' && cmcc.connection[property].trim().length === 0);
        }
    };

    mongoConnectionManagementController.$inject = ['$scope', 'contextualData','dcDataFactory'];

})();