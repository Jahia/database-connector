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
            var minChars = attr['lengthParserMin'];
            var maxChars = attr['lengthParserMax'];
            console.log(attr);
            model.$parsers.push(function(value) {
                // console.log(value);
                // console.log('min: ' +minChars, 'max: ' + maxChars);
                if (!_.isUndefined(minChars) && !_.isUndefined(maxChars)) {
                    if (value.length > maxChars || value.length < minChars ){
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
            // model.$formatters.push(function(value) {
            //     return parseInt(value);
            // });
        }
    };

    angular
        .module('databaseConnector')
        .directive('lengthParser',  ['$log', lengthParserDirective]);
})();