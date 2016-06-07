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
        cso.getFormattedUptime = getFormattedUptime;


        init();

        function init() {

            cso.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                cso.connectionStatus = connectionStatus;
            });
        }

        function getFormattedUptime() {
            return moment().subtract(cso.connectionStatus.uptime, 'seconds').format('MMMM Do YYYY, h:mm:ss a').toString();
        }

        function goToConnections() {
            $state.go('connections');
        }

    }

    connectionStatisticsOverview.$inject = ['$scope', 'contextualData', 'dcConnectionStatusService'];
})();