(function() {
    'use strict';
    var mongoConnection = function($log, contextualData) {

        var directive = {
            restrict        : 'E',
            templateUrl     : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionsOverview/connection/mongoConnection.html',
            scope: {
                connection: '='
            },
            controller      : mongoConnectionController,
            controllerAs    : 'mcc',
            bindToController: true,
            link            : linkFunc
        };

        return directive;

        function linkFunc(scope, el, attr, ctrls) {}

    };

    angular
        .module('databaseConnector')
        .directive('dcMongoConnection', ['$log', 'contextualData', mongoConnection]);

    var mongoConnectionController = function($scope, contextualData) {
        var mcc = this;
        mcc.imageUrl = contextualData.context + '/modules/database-connector/images/' + mcc.connection.databaseType + '/logo_60.png';

         // console.log(mcc.connection);
    };

    mongoConnectionController.$inject = ['$scope', 'contextualData'];
})();