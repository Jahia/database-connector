(function () {
    'use strict';
    var spinner = function(contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/dcSpinner.html',
            scope           : {
                spinnerMode: '=',
                show: '='
            },
            link            : linkFunc
        };
            return directive;

            function linkFunc(scope, el, attr, ctrl) {}

    };
    angular
        .module('databaseConnector')
        .directive('dcSpinner', ['contextualData', spinner]);
    
})();