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

    var momentFormatter = function() {
        return function(time, unit, format) {
            switch (format) {
                case 'fancy' :
                    var mUptime = moment().subtract(time, unit);
                    if (mUptime.isBefore(moment().subtract(1, 'week'), unit)) {
                        //use from now
                        return mUptime.fromNow().toString();
                    } else {
                        //use calendar time
                        return mUptime.calendar().toString();
                    }
                    break;
                default:
                    return null
            }
        };
    };

    angular.module('databaseConnector')
        .filter('replaceNull', [replaceNull])
        .filter('momentFilter', [momentFilter])
        .filter('momentFormatter', [momentFormatter]);
})();