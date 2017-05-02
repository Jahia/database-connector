(function(){
    var DCStateService = function(contextualData, $http, $q) {
        this.dcActive = false;
        this.connectorsMetaData = null;
        this.connections = null;
        this.selectedConnection = null;
        this.selectedDatabaseType = null;
        this.exportConnections = {};
        var cache = {};

        /**
         * Load item from server. Item is the subpath in url i.e. alldirectives etc.
         *
         * @param item
         * @returns {*}
         */
        this.getFromCache = function(item) {
            return $q(function(resolve, reject) {
                if (cache[item]) {
                    resolve(cache[item]);
                    return;
                }
                load(item).then(function(data) {
                    resolve(data);
                }, function(error) {
                    reject(error);
                });
            });
        };

        /**
         * Get available directives for database type i.e. MONGO
         *
         * @param type
         * @returns {*}
         */
        this.getDirectivesForType = function(type) {
            var self = this;
            return $q(function(resolve, reject) {
                self.getFromCache("alldirectives").then(function(data) {
                    var directives = data.directives;
                    for (var directiveForType in directives) {
                        if (directives[directiveForType]["databaseType"] === type) {
                            resolve(directives[directiveForType]);
                            return;
                        }
                    }
                    reject(null);
                })
            }, function(error) {
                reject(error);
            });
        };

        function load(item) {
            return $q(function(resolve, reject) {
                $http.get(contextualData.apiUrl + "/" + item).then(function(data) {
                    cache[item] = data.data;
                    resolve(data.data);
                }, function(error) {
                    reject(error);
                });
            });
        }
    };
    angular
        .module('databaseConnector')
        .service('$DCStateService', ['contextualData', '$http', '$q', DCStateService]);
})();