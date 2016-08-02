(function() {
    'use strict';

    var i18nMessageKeyDirectiveFunction = function(i18nService) {
        return {
            restrict: 'A',
            link: function($scope, $element, $attrs) {
                var i18n;
                if (!$attrs.messageParams) {
                    i18n = i18nService.message($attrs.messageKey);
                } else {
                    i18n = i18nService.format($attrs.messageKey, $attrs.messageParams);
                }

                if ($attrs.messageAttr) {
                    // store the i18n in the specified element attr
                    $element.attr($attrs.messageAttr, i18n);
                } else {
                    // set the i18n as element text
                    $element.text(i18n);
                }
            }
        };
    };

    var i18nTranslateFilterFunction = function(i18nService) {
        return function(input) {
            return i18nService.message(input);
        };
    };

    angular.module('i18n', [])
        .service('i18nService', function() {
            this.message = function(key, fallback) {
                if (dci18n && dci18n[key]) {
                    return dci18n[key];
                } else if (angular.isDefined(fallback)) {
                    return fallback;
                } else {
                    return '???' + key + '???';
                }
            };

            this.format = function(key, params) {
                var replacer = function(params) {
                    return function(s, index) {
                        return params[index] ? (params[index] === '__void__' ? '' : params[index]) : '';
                    };
                };

                if (params) {
                    if (dci18n && dci18n[key]) {
                        return dci18n[key].replace(/\{(\w+)\}/g, replacer(params.split('|')));
                    } else {
                        return '???' + key + '???';
                    }
                } else {
                    return this.message(key);
                }
            };
        })

        .directive('messageKey', ['i18nService', i18nMessageKeyDirectiveFunction])

        .filter('translate', ['i18nService', i18nTranslateFilterFunction]);
})();
