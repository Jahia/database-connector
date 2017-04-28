(function(){
    var DCStateService = function() {
        this.dcActive = false;
        this.connectorsMetaData = null;
        this.connections = null;
        this.selectedConnection = null;
        this.selectedDatabaseType = null;
        this.exportConnections = {};
    };
    angular
        .module('databaseConnector')
        .service('$DCStateService', [DCStateService]);
})();