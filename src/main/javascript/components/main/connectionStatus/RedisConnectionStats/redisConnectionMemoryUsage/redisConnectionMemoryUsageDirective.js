(function() {
    var redisConnectionMemoryUsage = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/RedisConnectionStats/redisConnectionMemoryUsage/redisConnectionMemoryUsage.html',
            controller  : RedisConnectionMemoryUsageController,
            controllerAs: 'rcmuc',
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
        .directive('redisConnectionMemoryUsage', ['contextualData', redisConnectionMemoryUsage]);

    var RedisConnectionMemoryUsageController = function($scope, dcConnectionStatusService) {
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

        init();

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
                            label   : 'Total memory allocated',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'used_memory_peak',
                            label   : 'Peak memory consumed',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'used_memory_rss',
                            label   : '(resident set size) Number of bytes that Redis was allocated by the operating system',
                            type    : 'number',
                            p       : {}
                        }
                    ],
                    rows: [
                    ]
                },
                options: {
                    title               : "Memory Usage",
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
            entry.c[1].v = connectionStatus.used_memory_human/1000000;
            entry.c[1].f = connectionStatus.used_memory_human/1000000 + ' MB';
            entry.c[2].v = connectionStatus.used_memory_peak/1000000;
            entry.c[2].f = connectionStatus.used_memory_peak/1000000 + ' MB';
            entry.c[3].v = connectionStatus.used_memory_rss/1000000;
            entry.c[3].f = connectionStatus.used_memory_rss/1000000 + ' MB';

            rcmuc.memoryUsageChart.options.title = 'Memory Consumed:' + connectionStatus.used_memory_human + 'MB';

            if (rcmuc.memoryUsageChart.data.rows.length == 10) {
                rcmuc.memoryUsageChart.data.rows.shift();
            }
            rcmuc.memoryUsageChart.data.rows.push(entry);
        }
    };


    RedisConnectionMemoryUsageController.$inject = ['$scope', 'dcConnectionStatusService'];
})();