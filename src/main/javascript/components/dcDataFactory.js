(function() {
    var dataFactory = function($http, $log) {
        return {
            getData: getData,
            customRequest: customRequest,
            parseRedisStatus: parseRedisStatus
        };

        function getData(url) {
            return $http.get(url)
                .then(getDataComplete)
                .catch(getDataFailed);
        }

        function customRequest(obj) {
            return $http(obj).then(getDataComplete);
        }

        function getDataComplete(response) {
            return response.data;
        }

        function getDataFailed(error) {
            $log.error('XHR Failed to execute your request.' + error.data);
            return error.data;
        }

        function parseRedisStatus(response) {
            var lines = response.split( "\r\n" );
            var RedisJsonStats = { };
            for ( var i = 0, l = response.length; i < l; i++ ) {
                var line = lines[ i ];
                if ( line && line.split ) {
                    line = line.split( ":" );
                    if ( line.length > 1 ) {
                        var key = line.shift( );
                        RedisJsonStats[ key ] = line.join( ":" );
                    }
                }
            }
            response = RedisJsonStats;

            return response;
        }
    };

    dataFactory.$inject = ['$http', '$log'];

    angular
        .module('databaseConnector.dataFactory', [])
        .factory('dcDataFactory', dataFactory);
})();