(function() {
    var connectionsAvailable = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/connectionsAvailable/connectionsAvailable.html',
            controller  : connectionsAvailableController,
            controllerAs: 'cac',
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
        .directive('connectionsAvailable', ['contextualData', connectionsAvailable]);

    var connectionsAvailableController = function($scope, dcConnectionStatusService) {
        var cac            = this;
        var DEFAULT_HEIGHT  = 480;
        var DEFAULT_WIDTH   = 640;
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
        cac.getHeight = getHeight;
        cac.getWidth  = getWidth;

        init();

        function init() {
            cac.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            initChart();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                cac.connectionStatus = connectionStatus;
                updateChartEntries(connectionStatus);
            });
        }

        function getHeight() {
            return _.isUndefined(cac.chartHeight) || cac.chartHeight === null || _.isString(cac.chartHeight) && cac.chartHeight == '' ? DEFAULT_HEIGHT : cac.chartHeight;
        }

        function getWidth() {
            return _.isUndefined(cac.chartWidth) || cac.chartWidth === null || _.isString(cac.chartWidth) && cac.chartWidth == '' ? DEFAULT_WIDTH : cac.chartWidth;
        }

        function initChart() {
            cac.pointSize = _.isUndefined(cac.pointSize) || cac.pointSize === null || _.isString(cac.pointSize) && cac.pointSize == '' ? DEFAULT_POINT_SIZE : cac.pointSize;
            cac.connectionsChart = {
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
                            id      : 'current',
                            label   : 'Currently Active',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'available',
                            label   : 'Available',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'totalCreated',
                            label   : 'Total Created ',
                            type    : 'number',
                            p       : {}
                        }
                    ],
                    rows: [
                    ]
                },
                options: {
                    title               : "Connections Available",
                    isStacked           : true,
                    fill: 20,
                    displayExactValues  : true,
                    pointSize: cac.pointSize,
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
            entry.c[1].v = connectionStatus.connections.current;
            entry.c[2].v = connectionStatus.connections.available;
            entry.c[3].v = connectionStatus.connections.totalCreated;

            if (cac.connectionsChart.data.rows.length == 10) {
                cac.connectionsChart.data.rows.shift();
            }
            cac.connectionsChart.data.rows.push(entry);
        }
    };

    connectionsAvailableController.$inject = ['$scope', 'dcConnectionStatusService'];
})();