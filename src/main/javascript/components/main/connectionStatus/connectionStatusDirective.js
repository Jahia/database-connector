(function() {
    'use strict';
    var connectionStatus = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/connectionStatus.html',
            controller      : ConnectionStatusController,
            controllerAs    : 'csc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcConnectionStatus', ['$log', 'contextualData', connectionStatus]);

    function ConnectionStatusController($scope, $state, $stateParams, dcConnectionStatusService) {
        var csc = this;
        csc.connectionStatus = connectionStatus;
        csc.goToConnections = goToConnections;

        $scope.$on('$destroy', function() {
            //disable the connection status service
            dcConnectionStatusService.disable();
        });

        init();

        function init() {
            if (!$stateParams.connection) {
                goToConnections();
            } else {
                csc.connection = $stateParams.connection;
                if (!csc.connection.canRetrieveStatus) {
                    //verify that we can retrieve the request, else we redirect.
                    goToConnections();
                }
                dcConnectionStatusService.enable(csc.connection.id, csc.connection.databaseType);
            }
        }

        function goToConnections() {
            $state.go('connections');
        }

    }

    ConnectionStatusController.$inject = ['$scope', '$state', '$stateParams', 'dcConnectionStatusService'];
})();