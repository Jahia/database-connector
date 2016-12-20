(function() {
    var connectionsAvailable = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/mongoConnectionStats/connectionsAvailable/connectionsAvailable.html',
            controller  : connectionsAvailableController,
            controllerAs : 'cac',
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
        .directive('connectionsAvailable', ['contextualData', connectionsAvailable]);

    var connectionsAvailableController = function($scope, dcConnectionStatusService, i18n) {
        var cac            = this;
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
        cac.getHeight = getHeight;
        cac.getWidth  = getWidth;

        cac.$onInit = function() {
            init();
        };

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
                            label   : i18n.message('dc_databaseConnector.label.statistics.mongo.currentlyActive'),
                            type    : 'number',
                            p       : {}
                        },
                        {
                            id      : 'available',
                            label   : i18n.message('dc_databaseConnector.label.statistics.mongo.available'),
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
            cac.connectionsChart.options.title = i18n.format('dc_databaseConnector.label.statistics.mongo.connectionsCreated', connectionStatus.connections.totalCreated + '');
            if (cac.connectionsChart.data.rows.length == 10) {
                cac.connectionsChart.data.rows.shift();
            }
            cac.connectionsChart.data.rows.push(entry);
        }
    };

    connectionsAvailableController.$inject = ['$scope', 'dcConnectionStatusService', 'i18nService'];
})();