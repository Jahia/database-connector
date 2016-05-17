(function() {
    var replaceNull = function() {
        return function(value) {
            return value === null ? '' : value;
        }
    };

    angular.module('databaseConnector')
        .filter('replaceNull', [replaceNull]);
})();