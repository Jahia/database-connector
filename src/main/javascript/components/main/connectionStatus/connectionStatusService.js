(function(){
    var connectionStatusService = function(contextualData, dcDataFactory, $rootScope, $interval, toaster, $timeout) {

        var currentConnectionStatus = null;
        var connectionStatusInterval = null;
        var REQUEST_TIMEOUT = 10000;//time interval is in milliseconds.
        var connectionId;
        var databaseType;

        this.enable = function(id, dbType) {
            connectionId = id;
            databaseType = dbType;
            $timeout(function () {
                //Run the first request immediately after the digest(This is so that the charts are ready)
                requestConnectionStatusEvent();
            });
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
               currentConnectionStatus = databaseType == 'REDIS' ? dcDataFactory.parseRedisStatus(response.success) : response.success;
               $rootScope.$broadcast('connectionStatusUpdate', currentConnectionStatus);
           }, function() {

               toaster.pop({
                   type: 'error',
                   title: 'Connection is currently unavailable',
                   body: 'Please wait for the server to come back online, and try to load stats again!',
                   toastId: 'sta',
                   timeout: 3000
               });
                // this stops the GET requests should be stopped if server goes down
               $interval.cancel(connectionStatusInterval);
           });
        }
    };

    angular
        .module('databaseConnector')
        .service('dcConnectionStatusService', ['contextualData', 'dcDataFactory', '$rootScope', '$interval', 'toaster', '$timeout', connectionStatusService]);
})();