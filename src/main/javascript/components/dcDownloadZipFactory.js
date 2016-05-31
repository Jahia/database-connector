(function() {

    var DownloadZipFactory = function($log, $resource, contextualData) {
        return $resource(null, null,
            {
                download: {
                    //@TODO Update to work with all databases.
                    url: contextualData.context + '/modules/databaseconnector/export',
                    method: 'POST',
                    headers: {
                        accept: 'application/zip'
                    },
                    responseType: 'arraybuffer',
                    cache: false,
                    transformResponse: function(data, headers) {
                        var zip = null;
                        if (data) {
                            zip = new Blob([data], {
                                type: 'application/zip'
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
        );

        function getFileNameFromHeader(header) {
            if (!header) {
                return null;
            }
            var result = header.split(';')[1].trim().split('=')[1];
            return result.replace(/"/g, '');
        }
    };

    DownloadZipFactory.$inject = ['$log', '$resource', 'contextualData'];

    angular
        .module('databaseConnector.downloadZipFactory', [])
        .factory('dcDownloadZipFactory', DownloadZipFactory);

})();
