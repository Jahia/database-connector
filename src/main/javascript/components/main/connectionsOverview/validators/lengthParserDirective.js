(function() {
    'use strict';

    var lengthParserDirective = function($log) {

        var directive = {
            restrict: 'A',
            require: ['^ngModel'],
            link: linkFunction
        };

        return directive;
        

        function linkFunction(scope, el, attr, ctrls) {
            var model = ctrls[0];
            var origValue = model.viewValue;
            var maxChars = attr['lengthParserMax'];
            model.$parsers.push(function(value) {
                var testValue = value + '';
                if (!_.isUndefined(maxChars)) {
                    if (testValue.length > maxChars){
                        value = origValue;
                        model.$setViewValue(value);
                        model.$render();
                    } else {
                        origValue = value;
                    }
                } else {
                    origValue = value;
                }
                return value;

            });
        }
    };

    angular
        .module('databaseConnector')
        .directive('lengthParser',  ['$log', lengthParserDirective]);
})();