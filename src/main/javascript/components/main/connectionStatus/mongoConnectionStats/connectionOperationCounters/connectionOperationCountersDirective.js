(function() {
    var connectionOperationCounters = function(contextualData) {
        var directive = {
            restrict    : 'E',
            templateUrl : contextualData.context + '/modules/database-connector/javascript/angular/components/main/connectionStatus/mongoConnectionStats/connectionOperationCounters/connectionOperationCounters.html',
            controller  : ConnectionOperationCountersController,
            controllerAs : 'cocc',
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
        .directive('connectionOperationCounters', ['contextualData', connectionOperationCounters]);

    var ConnectionOperationCountersController = function($scope, dcConnectionStatusService, i18n) {
        var cocc            = this;
        var DEFAULT_HEIGHT  = '480px';
        var DEFAULT_WIDTH   = '640px';
        var DEFAULT_POINT_SIZE = 5;
        cocc.getHeight = getHeight;
        cocc.getWidth  = getWidth;

        cocc.$onInit = function() {
            init();
        };

        function init() {
            cocc.connectionStatus = dcConnectionStatusService.getCurrentConnectionStatus();
            initChart();
            $scope.$on('connectionStatusUpdate', function(event, connectionStatus) {
                cocc.connectionStatus = connectionStatus;
                updateChartEntries(connectionStatus);
            });
        }

        function getHeight() {
            return _.isUndefined(cocc.chartHeight) || cocc.chartHeight === null || _.isString(cocc.chartHeight) && cocc.chartHeight == '' ? DEFAULT_HEIGHT : cocc.chartHeight;
        }

        function getWidth() {
            return _.isUndefined(cocc.chartWidth) || cocc.chartWidth === null || _.isString(cocc.chartWidth) && cocc.chartWidth == '' ? DEFAULT_WIDTH : cocc.chartWidth;
        }

        function initChart() {
            cocc.pointSize = _.isUndefined(cocc.pointSize) || cocc.pointSize === null || _.isString(cocc.pointSize) && cocc.pointSize == '' ? DEFAULT_POINT_SIZE : cocc.pointSize;
            cocc.operationCountersChart = {
                type        : 'ColumnChart',
                displayed   : true,
                data        : {
                    cols: [
                        {
                            id      : 'operations',
                            label   : '',
                            type    : 'string'
                        },
                        {
                            id      : 'value',
                            label   : i18n.message('dc_databaseConnector.label.value'),
                            type    : 'number'

                        }
                    ],
                    rows: [
                        {
                            "c": [
                                {
                                    v: i18n.message('dc_databaseConnector.label.statistics.mongo.insert')
                                },
                                {
                                    v: 0
                                }
                            ]
                        },
                        {
                            "c": [
                                {
                                    v: i18n.message('dc_databaseConnector.label.update')
                                },
                                {
                                    v: 0
                                }
                            ]
                        },
                        {
                            "c": [
                                {
                                    v: i18n.message('dc_databaseConnector.label.statistics.mongo.query')
                                },
                                {
                                    v: 0
                                }
                            ]

                        },
                        {
                            "c": [
                                {
                                    v: i18n.message('dc_databaseConnector.label.delete')
                                },
                                {
                                    v: 0
                                }
                            ]
                        },
                        {
                            "c": [
                                {
                                    v: i18n.message('dc_databaseConnector.label.statistics.mongo.getmore')
                                },
                                {
                                    v: 0
                                }
                            ]
                        }
                    ]
                },
                options: {
                    title               : i18n.message('dc_databaseConnector.label.statistics.mongo.operationCounters'),
                    colors              : ['#00ff00'],
                    isStacked           : true,
                    fill                : 20,
                    displayExactValues  : true,
                    pointSize: cocc.pointSize,
                    drawZeroLine: true,
                    dataOpacity: 0.5,
                    vAxis               : {
                        gridlines: {
                            count: 5
                        }
                    }
                }
            };
        }

        function updateChartEntries(connectionStatus) {
            cocc.operationCountersChart.data.rows[0].c[1].v = connectionStatus.opcounters.insert;
            cocc.operationCountersChart.data.rows[1].c[1].v = connectionStatus.opcounters.update;
            cocc.operationCountersChart.data.rows[2].c[1].v = connectionStatus.opcounters.query;
            cocc.operationCountersChart.data.rows[3].c[1].v = connectionStatus.opcounters.delete;
            cocc.operationCountersChart.data.rows[4].c[1].v = connectionStatus.opcounters.getmore;
        }
    };

    ConnectionOperationCountersController.$inject = ['$scope', 'dcConnectionStatusService', 'i18nService'];
})();