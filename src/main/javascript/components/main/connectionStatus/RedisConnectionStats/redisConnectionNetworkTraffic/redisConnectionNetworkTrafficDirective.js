(function() {
    var redisConnectionNetworkTraffic = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/RedisConnectionStats/redisConnectionNetworkTraffic/redisConnectionNetworkTraffic.html',
            controller  : RedisConnectionNetworkTrafficController,
            controllerAs: 'rcntc',
            bindToController: true,
            scope       : {
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
        .directive('redisConnectionNetworkTraffic', ['contextualData', redisConnectionNetworkTraffic]);

    var RedisConnectionNetworkTrafficController = function($scope, dcConnectionStatusService) {
        var rcntc            = this;
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
                },
                {
                    v: ''
                }
            ]
        };
        rcntc.getHeight = getHeight;
        rcntc.getWidth  = getWidth;

        init();

        function init() {
            rcntc.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            initChart();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                rcntc.connectionStatus = connectionStatus;
                updateChartEntries(connectionStatus);
            });
        }

        function getHeight() {
            return _.isUndefined(rcntc.chartHeight) || rcntc.chartHeight === null || _.isString(rcntc.chartHeight) && rcntc.chartHeight == '' ? DEFAULT_HEIGHT : rcntc.chartHeight;
        }

        function getWidth() {
            return _.isUndefined(rcntc.chartWidth) || rcntc.chartWidth === null || _.isString(rcntc.chartWidth) && rcntc.chartWidth == '' ? DEFAULT_WIDTH : rcntc.chartWidth;
        }

        function initChart() {
            rcntc.pointSize = _.isUndefined(rcntc.pointSize) || rcntc.pointSize === null || _.isString(rcntc.pointSize) && rcntc.pointSize == '' ? DEFAULT_POINT_SIZE : rcntc.pointSize;
            rcntc.networkTrafficChart = {
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
                            id      : 'total_net_input_bytes',
                            label   : 'Incoming MB',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'total_net_output_bytes',
                            label   : 'Outgoing MB',
                            type    : 'number',
                            p       : {}
                        }
                    ],
                    rows: [
                    ]
                },
                options: {
                    title               : "Network Traffic: ",
                    fill                : 20,
                    displayExactValues  : true,
                    pointSize: rcntc.pointSize,
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
            entry.c[1].v = Math.round(connectionStatus.total_net_input_bytes / 1024 / 1024);
            entry.c[2].v = Math.round(connectionStatus.total_net_output_bytes / 1024 / 1024);
            rcntc.networkTrafficChart.options.title = 'Network Traffic (Total connections Received, ' + connectionStatus.total_connections_received + ')' ;

            if (rcntc.networkTrafficChart.data.rows.length == 10) {
                rcntc.networkTrafficChart.data.rows.shift();
            }
            rcntc.networkTrafficChart.data.rows.push(entry);
        }
    };

    RedisConnectionNetworkTrafficController.$inject = ['$scope', 'dcConnectionStatusService'];
})();