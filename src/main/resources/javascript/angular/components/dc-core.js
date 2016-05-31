(function() {
    var dataFactory = function($http, $log) {
        return {
            getData: getData,
            customRequest: customRequest
        };

        function getData(url) {
            return $http.get(url)
                .then(getDataComplete)
                .catch(getDataFailed);
        }

        function customRequest(obj) {
            return $http(obj).then(getDataComplete);
        }

        function getDataComplete(response) {
            return response.data;
        }

        function getDataFailed(error) {
            $log.error('XHR Failed to execute your request.' + error.data);
            return error.data;
        }
    };

    dataFactory.$inject = ['$http', '$log'];

    angular
        .module('databaseConnector.dataFactory', [])
        .factory('dcDataFactory', dataFactory);
})();(function() {

    var DownloadFactory = function($log, $resource) {
        return {
            download: download
        };

        function download(url, mimeType, data) {
            if (_.isUndefined(url) || url === null || _.isUndefined(mimeType) || mimeType === null ) {
                return null;
            }
            return $resource(null, null,
                {
                    download: {
                        //@TODO Update to work with all databases.
                        url: url,
                        method: 'POST',
                        headers: {
                            accept: mimeType
                        },
                        responseType: 'arraybuffer',
                        cache: false,
                        transformResponse: function(data, headers) {
                            var zip = null;
                            if (data) {
                                zip = new Blob([data], {
                                    type: mimeType
                                });
                            }
                            var fileName = getFileNameFromHeader(headers('content-disposition'));
                            var result = {
                                blob: zip,
                                fileName: fileName
                            };
                            return {
                                response: result
                            };
                        }
                    }
                }
            ).download(data);
        }

        function getFileNameFromHeader(header) {
            if (!header) {
                return null;
            }
            var result = header.split(';')[1].trim().split('=')[1];
            return result.replace(/"/g, '');
        }
    };

    DownloadFactory.$inject = ['$log', '$resource'];

    angular
        .module('databaseConnector.downloadFactory', [])
        .factory('dcDownloadFactory', DownloadFactory);
})();
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
    
})();(function() {
    var replaceNull = function() {
        return function(value) {
            return value === null ? '' : value;
        }
    };

    angular.module('databaseConnector')
        .filter('replaceNull', [replaceNull]);
})();(function() {
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
                if (ffi18n && ffi18n[key]) {
                    return ffi18n[key];
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
                    if (ffi18n && ffi18n[key]) {
                        return ffi18n[key].replace(/\{(\w+)\}/g, replacer(params.split('|')));
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
