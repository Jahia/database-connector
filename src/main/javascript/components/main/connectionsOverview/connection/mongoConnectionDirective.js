(function() {
    'use strict';
    var mongoConnection = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/mongoConnection.html',
            scope: {
                connection: '='
            },
            controller      : mongoConnectionController,
            controllerAs    : 'mcc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcMongoConnection', ['$log', 'contextualData', mongoConnection]);

    var mongoConnectionController = function($scope, contextualData,dcDataFactory) {
        var mcc = this;
        mcc.imageUrl = contextualData.context + '/modules/database-connector/images/' + mcc.connection.databaseType + '/logo_60.png';

        mcc.updateConnection = updateConnection;

        function updateConnection(connect) {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[mcc.connection.databaseType] + '/' + (connect ? 'connect' : 'disconnect') + '/' + mcc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'PUT'
            }).then(function(response) {
                mcc.connection.isConnected = connect;
            }, function(response) {});
        }
    };

    mongoConnectionController.$inject = ['$scope', 'contextualData', 'dcDataFactory'];
    
})();