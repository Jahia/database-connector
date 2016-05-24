(function() {
    'use strict';

    var rangeParserDirective = function($log) {

        var directive = {
            restrict: 'A',
            require: ['^ngModel'],
            link: linkFunction
        };

        return directive;

        function linkFunction(scope, el, attr, ctrls) {
            var model = ctrls[0];
            var origValue = model.viewValue;
            // var minRangeChars = attr['rangeParserMin'];
            var maxRangeChars = attr['rangeParserMax'];
            console.log(attr);
            model.$parsers.push(function(value) {
                console.log(value);
                console.log( 'max Range #: ' + maxRangeChars);
                if (!_.isUndefined(maxRangeChars)) {
                    if (value > maxRangeChars ){
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
        .directive('rangeParser',  ['$log', rangeParserDirective]);
})();