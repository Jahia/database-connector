(function() {
    var DCManagementService = function($http, contextualData, $q, $DCSS) {

        init();
        function init() {
            retrieveConnectorsMetaData();
            retrieveAvailableConnections();
        }

        function retrieveAvailableConnections() {
            return $q(function(resolve, reject){
                var req = {
                    url: contextualData.apiUrl + '/allconnections',
                    method: 'GET'
                };
                $http(req).then(function(response) {
                    $DCSS.connections = response.data.connections;
                    resolve($DCSS.connections);
                }, function(response) {
                    reject(response);
                });
            });
        }

        function retrieveConnectorsMetaData() {
            return $q(function(resolve, reject) {
                var req = {
                    url: contextualData.apiUrl + '/connectorsmetadata',
                    method: 'GET'
                };
                $http(req).then(function(response) {
                    $DCSS.connectorsMetaData = response.data;
                    resolve(response.data);
                }, function(error) {
                    reject(error);
                });
            });
        }

        this.updateAvailableConnections = function() {
            return this.getAvailableConnections(false);
        };

        this.getAvailableConnections = function (fromCache) {
            return $q(function(resolve, reject) {
                if ($DCSS != null && fromCache) {
                    resolve ($DCSS.connections);
                } else {
                    retrieveAvailableConnections().then(function(connections){
                        resolve(connections);
                    }, function(error) {
                        reject(error);
                    })
                }
            });
        }
    };

    angular
        .module('databaseConnector')
        .service('$DCManagementService', ['$http', 'contextualData', '$q', '$DCStateService', DCManagementService]);
})();