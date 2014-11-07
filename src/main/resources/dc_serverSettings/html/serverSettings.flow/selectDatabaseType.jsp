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

<fmt:message key="dc_databaseConnector.label.title.addConnection" var="title"/>
<%@include file="header.jspf"%>

<form class="form-inline" action="${flowExecutionUrl}" method="POST">
    <button class="btn" type="submit" name="_eventId_cancel">
        <i class="icon-ban-circle"></i>
        &nbsp;<fmt:message key="dc_databaseConnector.label.cancel"/>
    </button>
</form>

<form class="form-inline" action="${flowExecutionUrl}" method="POST">

    <input type="hidden" name="_eventId_setDatabaseType">

    <ul id="allDatabaseTypes" class="thumbnails">
        <%--@elvariable id="allDatabaseTypes" type="java.util.Map<org.jahia.modules.databaseConnector.DatabaseTypes, java.util.Map<java.lang.String, java.lang.Object>"--%>
        <c:forEach items="${allDatabaseTypes}" var="database">

            <c:set var="databaseType" value="${database.key}"/>
            <c:set var="databaseTypeDetails" value="${database.value}"/>
            <c:choose>
                <c:when test="${databaseTypeDetails.connectedDatabases eq 0}">
                    <fmt:message var="labelConnectedDatabases" key="dc_databaseConnector.label.noneConnected"/>
                </c:when>
                <c:otherwise>
                    <fmt:message var="labelConnectedDatabases" key="dc_databaseConnector.label.alreadyConnected">
                        <fmt:param>${databaseTypeDetails.connectedDatabases}</fmt:param>
                    </fmt:message>
                </c:otherwise>
            </c:choose>

            <li class="span3 databaseType">
                <div class="thumbnail">
                    <img src="${url.context}/modules/database-connector/images/${fn:toLowerCase(databaseType)}/logo_60.png">
                    <h3>${databaseTypeDetails.displayName}</h3>
                    <p>${labelConnectedDatabases}</p>
                    <button class="btn" type="submit" name="databaseType" value="${databaseType}">
                        <i class="icon-plus"></i>
                        &nbsp;<fmt:message key="dc_databaseConnector.label.addConnection"/>
                    </button>
                </div>
            </li>

        </c:forEach>
    </ul>
</form>