(function() {
    'use strict';
    var Connection = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/Connection.html',
            scope: {
                connection: '='
            },
            controller      : ConnectionController,
            controllerAs    : 'cc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcMongoConnection', ['$log', 'contextualData', Connection]);

    var ConnectionController = function($scope, contextualData,dcDataFactory) {
        var cc = this;
        cc.imageUrl = contextualData.context + '/modules/database-connector/images/' + cc.connection.databaseType + '/logo_60.png';

        cc.updateConnection = updateConnection;

        function updateConnection(connect) {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[cc.connection.databaseType] + '/' + (connect ? 'connect' : 'disconnect') + '/' + cc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'PUT'
            }).then(function(response) {
                cc.connection.isConnected = connect;
            }, function(response) {});
        }
    };

    ConnectionController.$inject = ['$scope', 'contextualData', 'dcDataFactory'];
    
})();