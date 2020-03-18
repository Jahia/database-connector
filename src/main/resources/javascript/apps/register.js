(function() {
    window.jahia.i18n.loadNamespaces('database-connector');

    window.jahia.uiExtender.registry.add('adminRoute', 'database-connector', {
        targets: ['administration-server-configuration:99'],
        // requiredPermission: 'admin',
        icon: null,
        label: 'database-connector:label',
        isSelectable: true,
        iframeUrl: window.contextJsParameters.contextPath + '/cms/adminframe/default/en/settings.database-connector.html?redirect=false'
    });
})();