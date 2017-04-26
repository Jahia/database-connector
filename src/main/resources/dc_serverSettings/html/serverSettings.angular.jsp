<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="dbconnector" uri="http://www.jahia.org/dbconnector/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<template:addResources type="css" resources="lib/font-awesome/css/font-awesome.css"/>
<template:addResources type="css" resources="database-connector.css"/>
<template:addResources type="css" resources="spinner.css"/>

<template:addResources type="javascript" resources="lib/_dc.min.js"/>
<%--<template:addResources type="javascript" resources="lib/_dc-main.min.js"/>--%>
<%--<template:addResources type="javascript" resources="lib/_dc.min.js"/>--%>
<template:addResources type="javascript" resources="angular/components/dc-main.js"/>
<template:addResources type="javascript" resources="angular/components/dc-core.js"/>
<template:addResources type="javascript" resources="lib/jasny-bootstrap.fileinput.js"/>
<template:addResources type="javascript" resources="angular/components/i18n.js"/>

<template:addResources type="javascript" resources="i18n/database-connector-i18n_${renderContext.UILocale.language}.js" var="i18nJSFile"/>
<c:if test="${empty i18nJSFile}">
    <template:addResources type="javascript" resources="i18n/database-connector-i18n.js"/>
</c:if>

<%--<formfactory:jsResources/>--%>
<dbconnector:jsResources />

<div ui-view id="main_${currentNode.identifier}"></div>

<script>
    (function() {
        var contextualData = {};
        contextualData.supportedLocales = {en:'English',fr:'French',it:'Italian',da:'Dutch',es:'Spanish',pt:'Portuguese'};
        contextualData.siteIdentifier = '${renderContext.site.identifier}';
        contextualData.context = '${url.context}';
        contextualData.locale = '${renderContext.UILocale.language}';
        contextualData.language = '${renderContext.mainResourceLocale.language}';
        contextualData.urlSiteBase = '${url.context}${url.basePreview}${renderContext.site.path}';
        contextualData.urlBase = '${url.context}${url.basePreview}';
        contextualData.urlBaseSiteSettingsTemplates = '${url.context}${url.basePreview}${url.currentModule}/${script.view.moduleVersion}/templates/server-settings-base/configurations/database-connector/pagecontent/databaseConnectorSettings.';
        contextualData.sitePath = '${renderContext.site.path}';
        contextualData.pageQuery = '<c:url value="/modules/api/jcr/v1/default/${currentResource.locale}/query?noLinks"/>';
        contextualData.apiUrl = '${url.context}/modules/dbconn';
        angular.module('databaseConnector').constant('contextualData', contextualData);
        angular.element(document).ready(function () {
            moment.locale('${renderContext.UILocale.language}');
            angular.bootstrap(document.getElementById('main_${currentNode.identifier}'), ['databaseConnector']);
        });
    })();
</script>