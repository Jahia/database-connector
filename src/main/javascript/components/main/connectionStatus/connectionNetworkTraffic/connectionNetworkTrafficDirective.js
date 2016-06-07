(function() {
    var connectionNetworkTraffic = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/connectionNetworkTraffic/connectionNetworkTraffic.html',
            controller  : ConnectionNetworkTrafficController,
            controllerAs: 'cmuc',
            bindToController: true,
            scope       : {
                chartHeight : '=',
                chartWidth  : '=',
                pointSize   : '='
            },
            link        : linkFunc
        };

        function linkFunc($scope, el, attr, ctrls) {
        }

        return directive;
    };

    angular
        .module('databaseConnector')
        .directive('connectionNetworkTraffic', ['contextualData', connectionNetworkTraffic]);

    var ConnectionNetworkTrafficController = function($scope, dcConnectionStatusService) {
        var cntc            = this;
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
        cntc.getHeight = getHeight;
        cntc.getWidth  = getWidth;

        init();

        function init() {
            cntc.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            initChart();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                cntc.connectionStatus = connectionStatus;
                updateChartEntries(connectionStatus);
            });
        }

        function getHeight() {
            return _.isUndefined(cntc.chartHeight) || cntc.chartHeight === null || _.isString(cntc.chartHeight) && cntc.chartHeight == '' ? DEFAULT_HEIGHT : cntc.chartHeight;
        }

        function getWidth() {
            return _.isUndefined(cntc.chartWidth) || cntc.chartWidth === null || _.isString(cntc.chartWidth) && cntc.chartWidth == '' ? DEFAULT_WIDTH : cntc.chartWidth;
        }

        function initChart() {
            cntc.pointSize = _.isUndefined(cntc.pointSize) || cntc.pointSize === null || _.isString(cntc.pointSize) && cntc.pointSize == '' ? DEFAULT_POINT_SIZE : cntc.pointSize;
            cntc.networkTrafficChart = {
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
                            id      : 'requests',
                            label   : 'Requests',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'incoming_mb',
                            label   : 'Incoming MB',
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'outgoing_mb',
                            label   : 'Outgoing MB',
                            type    : 'number',
                            p       : {}
                        }
                    ],
                    rows: [
                    ]
                },
                options: {
                    title               : "Network Traffic",
                    isStacked           : true,
                    fill: 20,
                    displayExactValues  : true,
                    pointSize: cntc.pointSize,
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
            entry.c[1].v = connectionStatus.network.numRequests;
            entry.c[2].v = Math.round(connectionStatus.network.bytesIn / 1024 / 1024);
            entry.c[3].v = Math.round(connectionStatus.network.bytesOut / 1024 / 1024);

            if (cntc.networkTrafficChart.data.rows.length == 10) {
                cntc.networkTrafficChart.data.rows.shift();
            }
            cntc.networkTrafficChart.data.rows.push(entry);
        }
    };

    ConnectionNetworkTrafficController.$inject = ['$scope', 'dcConnectionStatusService'];
})();