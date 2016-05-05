angular.module('databaseConnector').config(function(contextualData, $stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise(function ($injector, $location) {
        var $state = $injector.get('$state');
        if ($location.$$state == null) {
            $state.go('connections', {}, {
                location: false
            });
        }
    });

    $stateProvider
        .state('connections', {
            url: '/connections',
            templateUrl:contextualData.urlBaseSiteSettingsTemplates + 'main.html.ajax'
        })
        .state('connectionsManagement', {
            url: '/connectionsManagement',
            template: '<dc-controller>',
            params: {
                connectionId: null,
                options: {}
            }
        })
}, ['contextualData', '$stateProvider', '$urlRouterProvider']);