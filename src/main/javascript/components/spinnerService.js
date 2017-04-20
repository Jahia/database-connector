(function () {

    var spinner = function($compile, $timeout) {
        var spinners = {};

        this.activate = function (id, scope, el, mode) {
            if (spinners[id]) {
                spinners[id].show = true;
            }
            if (!el) {
                //attach to first element in body
                el = angular.element('body').children.first();
            }
            if (el) {
                spinners[id] = scope.$new();
                var spinner = jQuery(document.createElement('dc-spinner'))
                    .addClass(id)
                    .attr('spinner-mode', mode);
                el.prepend(spinner);
                spinners[id].show = true;
                $compile(el.children().first())(spinners[id]);
            }
        };

        this.deactivate = function(id, destroy) {
            if (destroy) {
                if (!_.isEmpty(spinners[id])) {
                    spinners[id].$destroy;
                }
                var spinner = angular.element('.' + id);
                if (spinner.length > 0) {
                    spinner[0].parentNode.removeChild(spinner[0]);
                }
                delete spinners[id];
            } else {
                if (!_.isEmpty(spinners[id])) {
                    //May need to use apply
                   spinners[id].show = false;
                }
            }
        }
    };

    spinner.$inject = ['$compile', '$timeout'];

    angular
        .module('databaseConnector')
        .service('$dcSpinner', spinner)
})();