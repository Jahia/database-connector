(function(){
    var connectionStatusService = function(contextualData, dcDataFactory, $rootScope, $interval) {

        var currentConnectionStatus = null;
        var connectionStatusInterval = null;
        var REQUEST_TIMEOUT = 1000;
        var connectionId;
        var databaseType;

        this.enable = function(id, dbType) {
            connectionId = id;
            databaseType = dbType;
            connectionStatusInterval = $interval(requestConnectionStatusEvent, REQUEST_TIMEOUT);
        };

        this.disable = function() {
            if (connectionStatusInterval !== null) {
                $interval.cancel(connectionStatusInterval);
            }
        };

        this.getCurrentConnectionStatus = function() {
            return currentConnectionStatus;
        };

        function requestConnectionStatusEvent() {
           dcDataFactory.customRequest({
                url: contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[databaseType] + '/status/' + connectionId, 
                method: 'GET'
           }).then(function(response) {
               currentConnectionStatus = response.success;
               $rootScope.$broadcast('connectionStatusUpdate', currentConnectionStatus);
           }, function() {
           });
        }
    };

    angular
        .module('databaseConnector')
        .service('dcConnectionStatusService', ['contextualData', 'dcDataFactory', '$rootScope', '$interval', connectionStatusService]);
})();