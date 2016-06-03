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
            url: '/connections-status',
            template: '<dc-connection-status>',
            params: {
                connection: null
            }
        })
        .state('importResults', {
            url: '/import-results',
            template: '<dc-import-results>',
            params: {
                results: null,
                status: null
            }
        })
}, ['contextualData', '$stateProvider', '$urlRouterProvider']);