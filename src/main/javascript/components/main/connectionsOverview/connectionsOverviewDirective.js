(function() {
    'use strict';
    var connectionsOverview = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connectionsOverview.html',
            controller      : connectionsOverviewController,
            controllerAs    : 'coc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcConnectionsOverview', ['$log', 'contextualData', connectionsOverview]);
    
    var connectionsOverviewController = function($scope, contextualData, dcDataFactory) {
        var coc = this;
        coc.getConnections = getConnections;

        init();

        function init() {
            getConnections();
        }

        function getConnections() {
            var url = contextualData.context + '/modules/databaseconnector/mongodb/getconnections';
            dcDataFactory.customRequest({
                url: url,
                method: 'GET'
            }).then(function(response) {
               coc.connections = response.connections;
            }, function(response) {});
        }
    };

    connectionsOverviewController.$inject = ['$scope', 'contextualData', 'dcDataFactory'];
})();


