(function(){
    var DCConnectionManagerService = function($http, contextualData, $q, toaster, i18n, $DCSS, dcDataFactory, dcConnectionStatusService) {
        var self = this;
        this.updateConnection = function(connection, connect) {
            return $q(function(resolve, reject){
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
                    connection.canRetrieveStatus = false;
                    resolve({data:response.data, connection:connection});
                });
            });
        };

        //@TODO this should just be replaced with a call that will compile a brief status directive for each connection
        this.verifyServerStatus = function(connection) {
            return $q(function(resolve, reject){
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
                        } else if (connection.databaseType == "REDIS") {
                            //@TODO this needs to be extracted into the redis module
                            console.log(response.data.success);
                            response.data.success = dcConnectionStatusService.getParsedStatus(connection.databaseType, response.data.success);
                            if (response.data.success != null) {
                                connection.dbVersion = response.data.success.redis_version;
                                connection.uptime = response.data.success.uptime_in_seconds;
                            }
                        }
                    } else {
                        connection.canRetrieveStatus = false;
                    }
                    resolve(connection);
                }, function(response) {
                    //status cannot be retrieved
                    connection.canRetrieveStatus = false;
                    reject({data:response.data, connection:connection});
                });
            });
        };
    };
    angular
        .module('databaseConnector')
        .service('$DCConnectionManagerService', ['$http', 'contextualData', '$q', 'toaster',
            'i18nService', '$DCStateService', 'dcDataFactory', 'dcConnectionStatusService', DCConnectionManagerService]);
})();