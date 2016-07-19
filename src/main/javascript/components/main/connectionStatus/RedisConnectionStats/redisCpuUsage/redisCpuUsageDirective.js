(function() {
    var redisCpuUsage= function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/RedisConnectionStats/redisCpuUsage/redisCpuUsage.html',
            controller  : RedisCpuUsageController,
            controllerAs: 'rcuc',
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
        .directive('redisCpuUsage', ['contextualData', redisCpuUsage]);

    var RedisCpuUsageController = function($scope, dcConnectionStatusService) {
        var rcuc            = this;
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

        rcuc.getHeight = getHeight;
        rcuc.getWidth  = getWidth;

        init();

        function init() {
            rcuc.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            initChart();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                rcuc.connectionStatus = connectionStatus;
                updateChartEntries(connectionStatus);
            });
        }

        function getHeight() {
            return _.isUndefined(rcuc.chartHeight) || rcuc.chartHeight === null || _.isString(rcuc.chartHeight) && rcuc.chartHeight == '' ? DEFAULT_HEIGHT : rcuc.chartHeight;
        }

        function getWidth() {
            return _.isUndefined(rcuc.chartWidth) || rcuc.chartWidth === null || _.isString(rcuc.chartWidth) && rcuc.chartWidth == '' ? DEFAULT_WIDTH : rcuc.chartWidth;
        }

        function initChart() {
            rcuc.pointSize = _.isUndefined(rcuc.pointSize) || rcuc.pointSize === null || _.isString(rcuc.pointSize) && rcuc.pointSize == '' ? DEFAULT_POINT_SIZE : rcuc.pointSize;
            rcuc.operationCountersChart = {
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
                            id      : 'used_cpu_sys',
                            label   : 'System CPU consumed by the Redis server',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'used_cpu_user',
                            label   : 'User CPU consumed by the Redis server',
                            type    : 'number',
                            p       : {}

                        },
                        {
                            id      : 'used_cpu_sys_children',
                            label   : 'System CPU consumed by the background processes',
                            type    : 'number',
                            p       : {}

                        }
                    ],
                    rows: [
                    ]
                },
                options: {
                    title               : "CPU usage",
                    colors              : ['#009900', '#3366ff', '#cc66ff'],
                    fill                : 20,
                    displayExactValues  : true,
                    pointSize: rcuc.pointSize,
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
            entry.c[1].v = connectionStatus.used_cpu_sys;
            entry.c[1].f = connectionStatus.used_cpu_sys;
            entry.c[2].v = connectionStatus.used_cpu_user;
            entry.c[2].f = connectionStatus.used_cpu_user;
            entry.c[3].v = connectionStatus.used_cpu_sys_children;
            entry.c[3].f = connectionStatus.used_cpu_sys_children;


            /* The hit rate, which is calculated using the keyspace_hits and keyspace_misses metrics from the Stats section like this:
             *
             *   HitRate = keyspace_hits/(keyspace_hits+keyspace_misses)
             */

            rcuc.operationCountersChart.options.title = 'Hit Rate: ' + connectionStatus.keyspace_hits/(connectionStatus.keyspace_hits + connectionStatus.keyspace_misses);

            if (rcuc.operationCountersChart.data.rows.length == 10) {
                rcuc.operationCountersChart.data.rows.shift();
            }
            rcuc.operationCountersChart.data.rows.push(entry);
        }

    };

    RedisCpuUsageController.$inject = ['$scope', 'dcConnectionStatusService'];
})();