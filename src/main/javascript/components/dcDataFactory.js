(function() {
    var dataFactory = function($http, $log, contextualData) {
        return {
            getData: getData,
            customRequest: customRequest,
            getConnectorsMetaData: getConnectorsMetaData
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

        function getConnectorsMetaData() {
            return customRequest({
                url: contextualData.apiUrl + '/connectorsmetadata',
                method: 'GET'
            });
        }
    };

    dataFactory.$inject = ['$http', '$log', 'contextualData'];

    angular
        .module('databaseConnector.dataFactory', [])
        .factory('dcDataFactory', dataFactory);
})();