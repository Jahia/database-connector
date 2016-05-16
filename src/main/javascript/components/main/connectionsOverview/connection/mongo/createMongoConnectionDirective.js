(function() {
    'use strict';
    var createMongoConnection = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/mongo/createMongoConnection.html',
            controller      : createMongoConnectionController,
            controllerAs    : 'cmcc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('createMongoConnection', ['$log', 'contextualData', createMongoConnection]);

    var createMongoConnectionController = function($scope, contextualData, dcDataFactory) {
        var cmcc = this;
        cmcc.connection = {};
        cmcc.imageUrl = contextualData.context + '/modules/database-connector/images/' + cmcc.connection.databaseType + '/logo_60.png';
        // $scope.data.cb2 = true;

        cmcc.createMongoConnection = createMongoConnection;
        cmcc.cancelCreation = cancelCreation;
        cmcc.updatePassword = updatePassword;
        function updatePassword() {
            console.log(_.isUndefined(cmcc.connection.password));
        }
        function createMongoConnection() {
            var url = contextualData.context + '/modules/databaseconnector/mongodb/add'
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
        
         function cancelCreation() {
             $scope.$emit('creationCancelled', null);
         }
    };

    createMongoConnectionController.$inject = ['$scope', 'contextualData','dcDataFactory'];

})();