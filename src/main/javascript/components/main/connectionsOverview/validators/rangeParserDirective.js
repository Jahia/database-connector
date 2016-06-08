(function() {
    'use strict';

    var rangeParserDirective = function($log, $timeout) {

        var directive = {
            restrict: 'A',
            require: ['^ngModel'],
            link: linkFunction
        };

        return directive;

        function linkFunction(scope, el, attr, ctrls) {
            var model = ctrls[0];
            var maxValue = parseInt(attr['rangeMaxValue']);
            var backupValue = null;
            model.$parsers.push(function(value) {

                if (value != parseInt(value)) {
                    value = parseInt(value);
                }

                if (isNaN(value)) {
                    value = '';
                    model.$setViewValue(value);
                    model.$render();
                    return value;
                }

                if (!_.isUndefined(maxValue)) {
                    if (value < maxValue ){
                        backupValue = value;
                    }
                    else{
                        value = backupValue;
                    }


                }

                model.$setViewValue(value);
                model.$render();
                //Override the md-maxlength since we are taking care of that validation through the parser.
                $timeout(function () {
                    model.$setValidity('md-maxlength', true);

                });
                return value;

            });
        }
    };

    angular
        .module('databaseConnector')
        .directive('rangeParser',  ['$log', '$timeout', rangeParserDirective]);
})();