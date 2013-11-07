<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>

<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js"/>
<template:addResources type="css" resources="admin-bootstrap.css, database-connector.css"/>

<template:addResources type="inlinejavascript">
    <script type="text/javascript">

        var textToReplace = "databaseId";

        function manageConnection(eventId, databaseId, databaseTypeName) {
            var form = $("#manageConnection");
            form.find("input[name='_eventId']").attr('name', "_eventId_"+eventId);
            form.find("input[name='databaseId']").val(databaseId);
            form.find("input[name='databaseTypeName']").val(databaseTypeName);
            form.submit();
        }

        function confirmRemoveConnection(databaseId, databaseTypeName) {
            var modalTitle = $("#removeConnectionModalTitle");
            modalTitle.text(modalTitle.text().replace(textToReplace, databaseId));
            textToReplace = databaseId;
            $("#removeConnectionModalConfirmButton").attr("onclick", "manageConnection('removeConnection', '"+databaseId+"', '"+databaseTypeName+"')");
            $("#removeConnectionModal").modal("show");
        }
    </script>
</template:addResources>

<fmt:message key="dc_databaseConnector.label.title.registeredConnections" var="title"/>
<%@include file="header.jspf"%>

<form class="form-inline" action="${flowExecutionUrl}" method="POST">
    <button class="btn" type="submit" name="_eventId_addConnection">
        <i class="icon-plus"></i>
        &nbsp;<fmt:message key="dc_databaseConnector.label.addConnection"/>
    </button>
</form>

<%--@elvariable id="registeredConnections" type="java.util.Map<java.lang.String, java.util.List<org.jahia.modules.databaseConnector.ConnectionData>>"--%>
<c:if test="${not empty registeredConnections}">

    <form id="manageConnection" action="${flowExecutionUrl}" method="POST">
        <input type="hidden" name="_eventId">
        <input type="hidden" name="databaseTypeName"/>
        <input type="hidden" name="databaseId"/>
    </form>
    <ul id="registeredConnections" class="thumbnails">
        <c:forEach var="entry" items="${registeredConnections}">
            <c:set var="databaseType" value="${entry.key}"/>

            <c:forEach var="connectionData" items="${entry.value}">
                <li class="span3 registeredConnection">
                    <div class="thumbnail">
                        <img src="/modules/database-connector/images/${databaseType}/logo_60.png">
                        <dl>
                            <dt>Database type</dt>
                            <dd>${connectionData.displayName}</dd>
                            <dt>Database ID</dt>
                            <dd>${connectionData.id}</dd>
                        </dl>
                        <%--<button class="btn" onclick="manageConnection('editConnection', '${connectionData.id}')">--%>
                        <button class="btn">
                            <i class="icon-pencil"></i>
                            &nbsp;<fmt:message key="dc_databaseConnector.label.editConnection"/>
                        </button>
                        <button class="btn" onclick="confirmRemoveConnection('${connectionData.id}', '${databaseType}')">
                            <i class="icon-trash"></i>
                            &nbsp;<fmt:message key="dc_databaseConnector.label.removeConnection"/>
                        </button>
                    </div>
                </li>
            </c:forEach>

        </c:forEach>
    </ul>

    <div id="removeConnectionModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="removeConnectionModalTitle" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="removeConnectionModalTitle"><fmt:message key="dc_databaseConnector.label.removeConnection.modal.title"/></h3>
        </div>
        <div class="modal-body">
            <fmt:message key="dc_databaseConnector.label.removeConnection.modal.body"/>
        </div>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true"><fmt:message key="dc_databaseConnector.label.removeConnection.modal.cancel"/></button>
            <button id="removeConnectionModalConfirmButton" class="btn btn-danger"><fmt:message key="dc_databaseConnector.label.removeConnection.modal.confirm"/></button>
        </div>
    </div>
</c:if>