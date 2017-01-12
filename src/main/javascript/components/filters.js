(function() {
    var replaceNull = function() {
        return function(value) {
            return value == null ? '' : value;
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

    var sizeFormatter = function() {
        return function(value) {
            return value / 1024 >= 1 ? (value / 1024 / 1024 >= 1 ? formatPrecision(value / 1024 / 1024) + ' MB' : formatPrecision(value / 1024) + ' KB') : value + ' B';
        };

        function formatPrecision (value) {
            value += '';
            var decimalPosition = value.indexOf('.');
            if (decimalPosition != -1) {
                var decimalValue = value.substring(decimalPosition+1);
                if (decimalValue.length > 2) {
                    decimalValue = Math.round(decimalValue.substring(0, 2) + '.' + decimalValue.substring(2, 3));
                    value = value.substring(0, decimalPosition) + '.' + decimalValue;
                }
            }
            return value;
        }
    };
    /*
    Uppercase first letter of value.
     */
    var fLUpperCase = function() {
        return function(value) {
            return _.isString(value) ? value.toUpperCase().slice(0,1) + value.toLowerCase().slice(1,value.length) : value;
        }
    };

    angular.module('databaseConnector')
        .filter('replaceNull', [replaceNull])
        .filter('momentFilter', [momentFilter])
        .filter('momentFormatter', [momentFormatter])
        .filter('sizeFormatter', [sizeFormatter])
        .filter('fLUpperCase', [fLUpperCase]);
})();