(function() {
    var redisConnectionMemoryUsage = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/redisConnectionStats/redisConnectionMemoryUsage/redisConnectionMemoryUsage.html',
            controller  : RedisConnectionMemoryUsageController,
            controllerAs : 'rcmuc',
            bindToController: {
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
        .directive('redisConnectionMemoryUsage', ['contextualData', redisConnectionMemoryUsage]);

    var RedisConnectionMemoryUsageController = function($scope, dcConnectionStatusService, $filter, i18n) {
        var rcmuc            = this;
        var DEFAULT_HEIGHT  = '480px';
        var DEFAULT_WIDTH   = '520px';
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
        rcmuc.getHeight = getHeight;
        rcmuc.getWidth  = getWidth;

        rcmuc.$onInit = function() {
            init();
        };

        function init() {
            rcmuc.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            initChart();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                rcmuc.connectionStatus = connectionStatus;
                updateChartEntries(connectionStatus);
            });
        }

        function getHeight() {
            return _.isUndefined(rcmuc.chartHeight) || rcmuc.chartHeight === null || _.isString(rcmuc.chartHeight) && rcmuc.chartHeight == '' ? DEFAULT_HEIGHT : rcmuc.chartHeight;
        }

        function getWidth() {
            return _.isUndefined(rcmuc.chartWidth) || rcmuc.chartWidth === null || _.isString(rcmuc.chartWidth) && rcmuc.chartWidth == '' ? DEFAULT_WIDTH : rcmuc.chartWidth;
        }

        function initChart() {
            rcmuc.pointSize = _.isUndefined(rcmuc.pointSize) || rcmuc.pointSize === null || _.isString(rcmuc.pointSize) && rcmuc.pointSize == '' ? DEFAULT_POINT_SIZE : rcmuc.pointSize;
            rcmuc.memoryUsageChart = {
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
                            id      : 'used_memory_human',
                            label   : i18n.message('dc_databaseConnector.label.statistics.redis.totalMemoryAllocated'),
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'used_memory_peak',
                            label   : i18n.message('dc_databaseConnector.label.statistics.redis.peakMemoryConsumed'),
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'used_memory_rss',
                            label   : i18n.message('dc_databaseConnector.label.statistics.redis.residentSetSize'),
                            type    : 'number',
                            p       : {}
                        }
                    ],
                    rows: [
                    ]
                },
                options: {
                    title               : i18n.message('dc_databaseConnector.label.statistics.memoryUsage'),
                    colors              : ['#009900', '#3366ff', '#cc66ff'],
                    fill                : 20,
                    displayExactValues  : true,
                    pointSize: rcmuc.pointSize,
                    vAxis               : {
                        title: "Megabytes",
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
            entry.c[1].v = connectionStatus.used_memory/1024/1024;
            entry.c[1].f = $filter('sizeFormatter')(connectionStatus.used_memory);
            entry.c[2].v = connectionStatus.used_memory_peak/1024/1024;
            entry.c[2].f = $filter('sizeFormatter')(connectionStatus.used_memory_peak);
            entry.c[3].v = connectionStatus.used_memory_rss/1024/1024;
            entry.c[3].f = $filter('sizeFormatter')(connectionStatus.used_memory_rss);

            rcmuc.memoryUsageChart.options.title = 'Memory Consumed:' + $filter('sizeFormatter')(connectionStatus.used_memory);

            if (rcmuc.memoryUsageChart.data.rows.length == 10) {
                rcmuc.memoryUsageChart.data.rows.shift();
            }
            rcmuc.memoryUsageChart.data.rows.push(entry);
        }
    };


    RedisConnectionMemoryUsageController.$inject = ['$scope', 'dcConnectionStatusService', '$filter', 'i18nService'];
})();