(function() {
    var redisCpuUsage= function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/redisConnectionStats/redisCpuUsage/redisCpuUsage.html',
            controller  : RedisCpuUsageController,
            controllerAs : 'rcuc',
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
        .directive('redisCpuUsage', ['contextualData', redisCpuUsage]);

    var RedisCpuUsageController = function($scope, dcConnectionStatusService, i18n) {
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

        rcuc.$onInit = function() {
            init();
        };

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
                            label   : i18n.message('dc_databaseConnector.label.statistics.redis.systemConsumedCPU'),
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'used_cpu_user',
                            label   : i18n.message('dc_databaseConnector.label.statistics.redis.userConsumedCPU'),
                            type    : 'number',
                            p       : {}

                        },
                        {
                            id      : 'used_cpu_sys_children',
                            label   : i18n.message('dc_databaseConnector.label.statistics.redis.systemConsumedBackgroundCPU'),
                            type    : 'number',
                            p       : {}

                        }
                    ],
                    rows: [
                    ]
                },
                options: {
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
            var hits = parseInt(connectionStatus.keyspace_hits)/(parseInt(connectionStatus.keyspace_hits) + parseInt(connectionStatus.keyspace_misses));
            console.log(hits);
            rcuc.operationCountersChart.options.title = i18n.format('dc_databaseConnector.label.statistics.redis.hitRate', isNaN(hits) ? '0' : hits + '');

            if (rcuc.operationCountersChart.data.rows.length == 10) {
                rcuc.operationCountersChart.data.rows.shift();
            }
            rcuc.operationCountersChart.data.rows.push(entry);
        }

    };

    RedisCpuUsageController.$inject = ['$scope', 'dcConnectionStatusService', 'i18nService'];
})();