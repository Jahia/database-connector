(function() {
    var redisConnectionsAvailable = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/redisConnectionStats/redisConnectionsAvailable/redisConnectionsAvailable.html',
            controller  : RedisConnectionsAvailableController,
            controllerAs : 'rcac',
            bindToController : {
                chartHeight : '=',
                chartWidth  : '=',
                pointSize   : '=?'
            },
            link        : linkFunc
        };

        function linkFunc($scope, el, attr, ctrls) {
        }

        return directive;
    };

    angular
        .module('databaseConnector')
        .directive('redisConnectionsAvailable', ['contextualData', redisConnectionsAvailable]);

    var RedisConnectionsAvailableController = function($scope, dcConnectionStatusService, i18n) {
        var rcac            = this;
        var DEFAULT_HEIGHT  = '480px';
        var DEFAULT_WIDTH   = '640px';
        var DEFAULT_POINT_SIZE = 5;

        var CHART_ENTRY_TEMPLATE = {
            "c": [
                {
                    v: ''
                },
                {
                    v: ''
                },
                {
                    v: ''
                }
            ]
        };
        rcac.getHeight = getHeight;
        rcac.getWidth  = getWidth;

        rcac.$onInit = function() {
            init();
        };

        function init() {
            rcac.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            initChart();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                rcac.connectionStatus = connectionStatus;
                updateChartEntries(connectionStatus);
            });
        }

        function getHeight() {
            return _.isUndefined(rcac.chartHeight) || rcac.chartHeight === null || _.isString(rcac.chartHeight) && rcac.chartHeight == '' ? DEFAULT_HEIGHT : rcac.chartHeight;
        }

        function getWidth() {
            return _.isUndefined(rcac.chartWidth) || rcac.chartWidth === null || _.isString(rcac.chartWidth) && rcac.chartWidth == '' ? DEFAULT_WIDTH : rcac.chartWidth;
        }

        function initChart() {
            rcac.pointSize = _.isUndefined(rcac.pointSize) || rcac.pointSize === null || _.isString(rcac.pointSize) && rcac.pointSize == '' ? DEFAULT_POINT_SIZE : rcac.pointSize;
            rcac.connectionsChart = {
                type        : 'LineChart',
                displayed   : true,
                data        : {
                    cols: [
                        {
                            id      : 'localTime',
                            label   : '',
                            type    : 'string',
                            p       : {}
                        },
                        {
                            id      : 'blocked_clients',
                            label   : i18n.message('dc_databaseConnector.label.statistics.redis.blockedClients'),
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'connected_clients',
                            label   : i18n.message('dc_databaseConnector.label.statistics.redis.connectedClients'),
                            type    : 'number',
                            p       : {}
                        }
                    ],
                    rows: [
                    ]
                },
                options: {
                    colors              : ['#009900', '#3366ff'],
                    fill                : 20,
                    displayExactValues  : true,
                    pointSize: rcac.pointSize,
                    vAxis               : {
                        gridlines: {
                            count: 10
                        }
                    }
                }
            };
        }

        function updateChartEntries(connectionStatus) {
            var entry = angular.copy(CHART_ENTRY_TEMPLATE);
            entry.c[0].v = moment(connectionStatus.localTime).format('HH:mm:ss').toString();
            entry.c[1].v = connectionStatus.blocked_clients;
            entry.c[2].v = connectionStatus.connected_clients;
            rcac.connectionsChart.options.title = i18n.format('dc_databaseConnector.label.statistics.redis.totalCommandsProcessed', connectionStatus.total_commands_processed + '');
            if (rcac.connectionsChart.data.rows.length == 10) {
                rcac.connectionsChart.data.rows.shift();
            }
            rcac.connectionsChart.data.rows.push(entry);
        }
    };

    RedisConnectionsAvailableController.$inject = ['$scope', 'dcConnectionStatusService', 'i18nService'];
})();