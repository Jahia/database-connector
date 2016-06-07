(function() {
    var replaceNull = function() {
        return function(value) {
            return value === null ? '' : value;
        }
    };

    var momentFilter = function() {
        return function(dateString, format) {
            return moment(dateString).format(format);
        };
    };

    angular.module('databaseConnector')
        .filter('replaceNull', [replaceNull])
        .filter('momentFilter', [momentFilter]);
})();