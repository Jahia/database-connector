(function() {
    'use strict';
    var connectionManagement = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionManagement/connectionManagement.html',
            controller      : connectionManagementController,
            controllerAs    : 'cmc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcController', ['$log', 'contextualData', connectionManagement]);

    var connectionManagementController = function() {
        var cmc = this;
    };

    connectionManagementController.$inject = [];
})();