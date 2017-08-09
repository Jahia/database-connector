(function() {
    var DCManagementService = function($http, contextualData, $q, $DCSS, $DCCMS, $mdDialog) {
        var self = this;
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
                    $DCSS.state.connections = response.data.connections;
                    resolve($DCSS.state.connections);
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
                    resolve ($DCSS.state.connections);
                } else {
                    retrieveAvailableConnections().then(function(connections){
                        resolve(connections);
                    }, function(error) {
                        reject(error);
                    })
                }
            });
        };

        this.refreshAllConnectionsStatus = function() {
            return $q(function(resolve, reject){
                self.getAvailableConnections().then(function(response) {
                    $DCSS.state.connections = angular.copy(response);
                }, function(error){
                    reject(error);
                });
            });
        };

        this.removeConnection = function (connectionId){
            for (var i = 0; i < $DCSS.state.connections.length; i++) {
                if (connectionId == $DCSS.state.connections[i].id) {
                    $DCSS.state.connections.splice(i, 1);
                    return;
                }
            }
        }
    };

    angular
        .module('databaseConnector')
        .service('$DCManagementService', ['$http', 'contextualData',
            '$q', '$DCStateService', '$DCConnectionManagerService', '$mdDialog', DCManagementService]);
})();