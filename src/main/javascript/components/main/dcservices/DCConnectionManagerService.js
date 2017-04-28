(function(){
    var DCConnectionManagerService = function($http, contextualData, $q, toaster, i18n, $DCSS, dcDataFactory) {
        var self = this;
        this.updateConnection = function(connection, connect) {
            return $q(function(resolve){
                var url = contextualData.apiUrl + $DCSS.connectorsMetaData[connection.databaseType].entryPoint + (connect ? '/connect/' : '/disconnect/') + connection.id;
                $http({
                    url: url,
                    method: 'PUT'
                }).then(function(response) {
                    if (response.data.success) {
                        connection.isConnected = connect;
                        if (connect) {
                            self.verifyServerStatus(connection).then(function(connection) {
                                resolve(connection);
                            });
                        } else {
                            connection.canRetrieveStatus = false;
                            resolve(connection);
                        }
                        toaster.pop({
                            type   : 'success',
                            title: i18n.message('dc_databaseConnector.toast.title.connectionSuccessfullyUpdated'),
                            toastId: 'cu',
                            timeout: 3000
                        });
                    } else {
                        toaster.pop({
                            type   : 'error',
                            title: i18n.message('dc_databaseConnector.toast.title.connectionUpdateFailed'),
                            toastId: 'cu',
                            timeout: 3000
                        });
                        connection.canRetrieveStatus = false;
                        resolve(connection);
                    }

                }, function(response) {
                    cc.connection.canRetrieveStatus = false;
                });
            });
        };

        this.verifyServerStatus = function(connection) {
            return $q(function(resolve){
                //verify if this connection is authenticated to retrieve server status
                var url = contextualData.apiUrl + $DCSS.connectorsMetaData[connection.databaseType].entryPoint + '/status/' + connection.id;
                $http({
                    url: url,
                    method: 'GET'
                }).then(function(response) {
                    //status can be retrieved
                    if (_.isUndefined(response.data.failed)) {
                        connection.canRetrieveStatus = true;
                        //@TODO needs to be updated for each external connector module to handle their own setting of custom properties.
                        if(connection.databaseType == "MONGO"){
                            connection.dbVersion = response.data.success.version;
                            connection.uptime = response.data.success.uptime;
                        } else if (cc.connection.databaseType == "REDIS") {
                            //@TODO this needs to be extracted into the redis module
                            response.data.success = dcDataFactory.parseRedisStatus(response.data.success);
                            connection.dbVersion = response.data.success.redis_version;
                            connection.uptime = response.data.success.uptime_in_seconds;
                        }
                    } else {
                        connection.canRetrieveStatus = false;
                    }
                    resolve(connection);
                }, function(response) {
                    //status cannot be retrieved
                    connection.canRetrieveStatus = false;
                    resolve(connection)
                });
            });
        };
    };
    angular
        .module('databaseConnector')
        .service('$DCConnectionManagerService', ['$http', 'contextualData', '$q', 'toaster',
            'i18nService', '$DCStateService', 'dcDataFactory', DCConnectionManagerService]);
})();