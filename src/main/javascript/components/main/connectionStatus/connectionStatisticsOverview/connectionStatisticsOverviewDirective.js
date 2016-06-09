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
        cso.getTimeFromNow = getTimeFromNow;
        cso.getCalendarTime = getCalendarTime;
        cso.uptime = null;
        init();

        function init() {

            cso.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            cso.uptime = getFormattedUptime();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                cso.connectionStatus = connectionStatus;
                if (cso.uptime === null) {
                    cso.uptime = getFormattedUptime();
                }
            });
        }

        function getFormattedUptime() {
            if (cso.connectionStatus === null) {
                return null;
            }
            return moment().subtract(cso.connectionStatus.uptime, 'seconds').format('MMMM Do YYYY, h:mm:ss a').toString();
        }

        function getTimeFromNow() {
            if (cso.connectionStatus === null) {
                return null;
            }
            return moment(cso.connectionStatus.uptime.unix).fromNow().toString();
        }
        
        function getCalendarTime() {
            if (cso.connectionStatus === null) {
                return null;
            }
            return moment().subtract(cso.connectionStatus.uptime, 'seconds').calendar().toString();

        }


        function goToConnections() {
            $state.go('connections');
        }

    }

    connectionStatisticsOverview.$inject = ['$scope', 'contextualData', 'dcConnectionStatusService'];
})();