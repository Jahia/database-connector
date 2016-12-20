(function() {
    'use strict';
    var connectionStatisticsOverview = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/connectionStatisticsOverview/connectionStatisticsOverview.html',
            controller      : ConnectionStatisticsOverviewController,
            controllerAs    : 'cso',
            bindToController: {
                databaseType : '='
            },
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcConnectionStatisticsOverview', ['$log', 'contextualData', connectionStatisticsOverview]);

    function ConnectionStatisticsOverviewController($scope, dcConnectionStatusService, i18n, $filter) {
        var cso = this;
        cso.goToConnections = goToConnections;
        cso.getMessage = i18n.message;
        cso.$onInit = function() {
            init();
        };

        function init() {
            cso.title = i18n.format('dc_databaseConnector.label.statistics.databaseOverview', $filter('fLUpperCase')(cso.databaseType));

            cso.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                cso.connectionStatus = connectionStatus;
            });
        }
        
        function goToConnections() {
            $state.go('connections');
        }

    }

    ConnectionStatisticsOverviewController.$inject = ['$scope', 'dcConnectionStatusService', 'i18nService', '$filter'];
})();