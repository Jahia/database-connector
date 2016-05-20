(function() {
    'use strict';

    var parserDirective = function($log) {

        var directive = {
            restrict: 'A',
            require: ['^ngModel'],
            link: linkFunction
        };

        return directive;

        function linkFunction(scope, el, attr, ctrls) {
            var model = ctrls[0];
            model.$parsers.push(function(value) {
                return '' + parseInt(value);
            });
            // model.$formatters.push(function(value) {
            //     return parseInt(value);
            // });
        }
    };

    angular
        .module('databaseConnector')
        .directive('parser',  ['$log', parserDirective]);
})();