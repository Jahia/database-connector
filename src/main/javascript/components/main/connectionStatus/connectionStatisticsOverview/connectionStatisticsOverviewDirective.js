(function() {
    'use strict';
    var connectionStatisticsOverview = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/connectionStatisticsOverview/connectionStatisticsOverview.html',
            controller      : ConnectionStatisticsOverviewController,
            controllerAs    : 'cso',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcConnectionStatisticsOverview', ['$log', 'contextualData', connectionStatisticsOverview]);

    function ConnectionStatisticsOverviewController($scope, contextualData, dcConnectionStatusService) {
        
        var cso = this;
        cso.goToConnections = goToConnections;
        init();

        function init() {

            cso.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                cso.connectionStatus = connectionStatus;
                console.log("cso.connectionStatus", cso.connectionStatus);
            });
        }
        
        function goToConnections() {
            $state.go('connections');
        }

    }

    connectionStatisticsOverview.$inject = ['$scope', 'contextualData', 'dcConnectionStatusService'];
})();