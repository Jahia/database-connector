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
        .state('connectionsStatus', {
            url: '/connectionsStatus',
            template: '<dc-connection-status>',
            params: {
                connectionId: null,
                options: {}
            }
        })
        .state('importResults', {
            url: '/import-results',
            template: '<dc-import-results>',
            params: {
                results: null
            }
        })
}, ['contextualData', '$stateProvider', '$urlRouterProvider']);