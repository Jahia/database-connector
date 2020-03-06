(function() {
    window.jahia.i18n.loadNamespaces('database-connector');

    var level = 'server';
    var parentTarget = 'administration-server';

    var cPath = '/administration/database-connector';
    var routeId = 'database-connector';
    window.jahia.uiExtender.registry.add('adminRoute', `${level}-${cPath.toLowerCase()}`, {
        id: routeId,
        targets: [`${parentTarget}-configuration:99`],
        path: cPath,
        route: routeId,
        // requiredPermission: 'admin',
        defaultPath: cPath,
        icon: null,
        label: 'database-connector:label',
        childrenTarget: null,
        isSelectable: true,
        level: level
    });

    console.log('%c Database Connector is activated', 'color: #3c8cba');
})();