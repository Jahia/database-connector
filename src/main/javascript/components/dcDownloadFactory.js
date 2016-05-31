(function() {

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
