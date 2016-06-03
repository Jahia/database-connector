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

    function ConnectionStatusController($scope, contextualData, dcDataFactory, $state, $stateParams, toaster, $mdDialog) {
        var csc = this;
        csc.connectionStatus = connectionStatus;
        csc.goToConnections = goToConnections;
        csc.checkServerStatus = checkServerStatus;

        init();

        function init() {
            if (!$stateParams.connection) {
                goToConnections();
            } else {
                csc.connection = $stateParams.connection;
            }
        }

        function checkServerStatus(connection) {
            var url = contextualData.context + '/modules/databaseconnector/' + contextualData.entryPoints[connection.type] + '/status/'+ csc.connection.id;
            dcDataFactory.customRequest({
                url: url,
                method: 'Get',
                data: connection
            }).then(function(response) {
                if (!_.isUndefined(response.connection)) {

                }
            }, function(response) {
                console.log("here is the server status !")
            });
        }

        function goToConnections() {
            $state.go('connections');
        }

    }

    ConnectionStatusController.$inject = ['$scope', 'contextualData', 'dcDataFactory', '$state', '$stateParams', 'toaster', '$mdDialog'];
})();