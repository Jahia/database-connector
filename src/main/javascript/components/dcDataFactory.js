(function() {
    var dataFactory = function($http, $log) {
        return {
            getData: getData,
            customRequest: customRequest
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
    };

    dataFactory.$inject = ['$http', '$log'];

    angular
        .module('databaseConnector.dataFactory', [])
        .factory('dcDataFactory', dataFactory);
})();