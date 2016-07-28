(function() {
    var connectionMemoryUsage = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/mongoConnectionStats/connectionMemoryUsage/connectionMemoryUsage.html',
            controller  : ConnectionMemoryUsageController,
            controllerAs: 'cmuc',
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
        .directive('connectionMemoryUsage', ['contextualData', connectionMemoryUsage]);

    var ConnectionMemoryUsageController = function($scope, dcConnectionStatusService) {
        var cmuc            = this;
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
        cmuc.getHeight = getHeight;
        cmuc.getWidth  = getWidth;

        init();

        function init() {
            cmuc.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            initChart();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                cmuc.connectionStatus = connectionStatus;
                updateChartEntries(connectionStatus);
            });
        }

        function getHeight() {
            return _.isUndefined(cmuc.chartHeight) || cmuc.chartHeight === null || _.isString(cmuc.chartHeight) && cmuc.chartHeight == '' ? DEFAULT_HEIGHT : cmuc.chartHeight;
        }

        function getWidth() {
            return _.isUndefined(cmuc.chartWidth) || cmuc.chartWidth === null || _.isString(cmuc.chartWidth) && cmuc.chartWidth == '' ? DEFAULT_WIDTH : cmuc.chartWidth;
        }

        function initChart() {
            cmuc.pointSize = _.isUndefined(cmuc.pointSize) || cmuc.pointSize === null || _.isString(cmuc.pointSize) && cmuc.pointSize == '' ? DEFAULT_POINT_SIZE : cmuc.pointSize;
            cmuc.memoryUsageChart = {
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
                            id      : 'mapped',
                            label   : 'Mapped',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'used',
                            label   : 'Used',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'virtual',
                            label   : 'Virtual',
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
                    pointSize: cmuc.pointSize,
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
            entry.c[1].v = connectionStatus.mem.mapped;
            entry.c[1].f = connectionStatus.mem.mapped + ' MB';
            entry.c[2].v = connectionStatus.mem.resident;
            entry.c[2].f = connectionStatus.mem.resident + ' MB';
            entry.c[3].v = connectionStatus.mem.virtual;
            entry.c[3].f = connectionStatus.mem.virtual + ' MB';

            if (cmuc.memoryUsageChart.data.rows.length == 10) {
                cmuc.memoryUsageChart.data.rows.shift();
            }
            cmuc.memoryUsageChart.data.rows.push(entry);
        }
    };

    ConnectionMemoryUsageController.$inject = ['$scope', 'dcConnectionStatusService'];
})();