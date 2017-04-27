(function() {
    'use strict';

    var ConnectionIdValidator = function($log, contextualData, $timeout, $q, dcDataFactory) {
        var directive = {
            restrict: 'A',
            require: ['^ngModel'],
            link: linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {
            var model = ctrls[0];
            var databaseType = attr.connectionIdValidator;
            model.$asyncValidators['connection-id-validator'] = function(modelValue, viewValue) {
                if (!viewValue || viewValue === attr.originalValue) {
                    return $q.when(true);
                }
                var deferred = $q.defer();
                var url = contextualData.apiUrl + contextualData.connectorsMetaData[databaseType].entryPoint + '/isconnectionvalid/' + viewValue;
                dcDataFactory.customRequest({
                    url: url,
                    method: 'GET'
                }).then(function (response) {
                    if (response) {
                        console.log('connection id is available');
                        deferred.resolve();
                    } else {
                        console.log('connection id is taken');
                        deferred.reject();
                    }
                }, function (response) {
                    console.log('error', response);
                    deferred.reject();
                });
                return deferred.promise;
            };
        }
    };

    angular
        .module('databaseConnector')
        .directive('connectionIdValidator', ['$log', 'contextualData', '$timeout', '$q', 'dcDataFactory', ConnectionIdValidator]);

})();