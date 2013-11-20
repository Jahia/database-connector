<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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

<%--@elvariable id="connection" type="org.jahia.modules.databaseConnector.webflow.model.Connection"--%>
<%--@elvariable id="option" type="org.jahia.services.content.nodetypes.initializers.ChoiceListValue"--%>

<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js"/>
<template:addResources type="css" resources="admin-bootstrap.css, database-connector.css"/>

<c:set var="databaseType" value="${connection.databaseType}"/>
<c:set var="hasAdvancedOptions" value="${databaseType eq 'REDIS' || databaseType eq 'MONGO'}"/>

<template:addResources type="inlinejavascript">
    <script type="text/javascript">
        $(document).ready(function () {
            var connectionForm = $("#connectionForm");
            connectionForm.find(".errorMessage").parentsUntil(connectionForm, ".control-group").addClass("error");

            <c:if test="${hasAdvancedOptions}">
                $('#advancedConfigToggle').click(function() {
                    $('#advancedConfig').slideToggle();
                });
            </c:if>
        });
    </script>
</template:addResources>

<c:choose>
    <c:when test="${isEdition}">
        <fmt:message key="dc_databaseConnector.label.title.editConnection" var="title"/>
    </c:when>
    <c:otherwise>
        <fmt:message key="dc_databaseConnector.label.title.addConnection" var="title"/>
    </c:otherwise>
</c:choose>
<%@include file="header.jspf"%>

<form class="form-inline" action="${flowExecutionUrl}" method="POST">
    <c:if test="${not isEdition}">
        <button class="btn" type="submit" name="_eventId_back">
            <i class="icon-arrow-left"></i>
            &nbsp;<fmt:message key="dc_databaseConnector.label.back"/>
        </button>
        <button class="btn" type="submit" name="_eventId_cancel">
            <i class="icon-ban-circle"></i>
            &nbsp;<fmt:message key="dc_databaseConnector.label.cancel"/>
        </button>
    </c:if>
</form>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">
        <p><fmt:message key="dc_databaseConnector.alert.errorMessage"/>&nbsp;${errorMessage}</p>
    </div>
</c:if>

<div class="box-1">
    <form:form modelAttribute="connection" id="connectionForm" class="form form-horizontal">
        <fieldset>

            <legend>
                <fmt:message key="dc_databaseConnector.label.${isEdition ? 'edit' : 'enter'}Config">
                    <fmt:param value="${connection.displayName}"/>
                </fmt:message>
            </legend>

            <div class="control-group">
                <label class="control-label" for="id"><fmt:message key="dc_databaseConnector.label.id"/></label>
                <div class="controls">
                    <form:input path="id" type="text" value="${id}" disabled="${isEdition}" autocomplete="false"/>
                    <form:errors path="id" cssClass="help-inline errorMessage"/>
                </div>
            </div>

            <c:if test="${databaseType ne 'NEO4J'}">
                <div class="control-group">
                    <label class="control-label" for="host"><fmt:message key="dc_databaseConnector.label.host"/></label>
                    <div class="controls">
                        <form:input path="host" type="text" value="${host}"/>
                        <form:errors path="host" cssClass="help-inline errorMessage"/>
                    </div>
                </div>
            </c:if>

            <c:if test="${databaseType ne 'NEO4J'}">
                <div class="control-group">
                    <label class="control-label" for="port"><fmt:message key="dc_databaseConnector.label.port"/></label>
                    <div class="controls">
                        <form:input path="port" type="text" value="${port}"/>
                        <form:errors path="port" cssClass="help-inline errorMessage"/>
                    </div>
                </div>
            </c:if>

            <c:if test="${databaseType ne 'REDIS' && databaseType ne 'NEO4J'}">
                <div class="control-group">
                    <label class="control-label" for="dbName"><fmt:message key="dc_databaseConnector.label.dbName"/></label>
                    <div class="controls">
                        <form:input path="dbName" type="text" value="${dbName}"/>
                        <form:errors path="dbName" cssClass="help-inline errorMessage"/>
                    </div>
                </div>
            </c:if>

            <c:if test="${databaseType eq 'NEO4J'}">
                <div class="control-group">
                    <label class="control-label" for="uri"><fmt:message key="dc_databaseConnector.label.uri"/></label>
                    <div class="controls">
                        <form:input path="uri" type="url" value="${uri}"/>
                        <form:errors path="uri" cssClass="help-inline errorMessage"/>
                    </div>
                </div>
            </c:if>

            <div class="control-group">
                <label class="control-label" for="user"><fmt:message key="dc_databaseConnector.label.user"/></label>
                <div class="controls">
                    <form:input path="user" type="text" value="${user}"/>
                    <form:errors path="user" cssClass="help-inline errorMessage"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="password"><fmt:message key="dc_databaseConnector.label.password"/></label>
                <div class="controls">
                    <form:password path="password" value="${password}" autocomplete="false"/>
                    <form:errors path="password" cssClass="help-inline errorMessage"/>
                </div>
            </div>

            <c:if test="${hasAdvancedOptions}">
                <div class="control-group">
                    <p class="control-label" id="advancedConfigToggle"><i class="icon-plus"></i> <fmt:message key="dc_databaseConnector.label.advancedConfig"/></p>
                </div>
                <div id="advancedConfig">
                    <c:if test="${databaseType eq 'REDIS'}">
                        <div class="control-group">
                            <label class="control-label" for="timeout"><fmt:message key="dc_databaseConnector.label.redis.timeout"/></label>
                            <div class="controls">
                                <form:input path="timeout" value="${timeout}"/>
                                <form:errors path="timeout" cssClass="help-inline errorMessage"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="weight"><fmt:message key="dc_databaseConnector.label.redis.weight"/></label>
                            <div class="controls">
                                <form:input path="weight" value="${weight}"/>
                                <form:errors path="weight" cssClass="help-inline errorMessage"/>
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${databaseType eq 'MONGO'}">
                        <div class="control-group">
                            <label class="control-label" for="writeConcern"><fmt:message key="dc_databaseConnector.label.mongo.writeConcern"/></label>
                            <div class="controls">
                                <jcr:propertyInitializers var="options" node="${currentNode}" name="dc:writeConcern" nodeType="dc:mongoConnection"/>
                                <form:select path="writeConcern" value="${writeConcern}">
                                    <form:options items="${options}" itemValue="value.string" itemLabel="displayName" />
                                </form:select>
                                <form:errors path="writeConcern" cssClass="help-inline errorMessage"/>
                            </div>
                        </div>
                    </c:if>
                </div>
            </c:if>

            <div class="form-actions">
                <button class="btn btn-primary" type="submit" name="_eventId_submit">
                    <fmt:message key="dc_databaseConnector.label.submit"/>
                </button>
                <c:if test="${isEdition}">
                    <button class="btn" type="submit" name="_eventId_cancel">
                        <fmt:message key="dc_databaseConnector.label.cancel"/>
                    </button>
                </c:if>
            </div>

        </fieldset>
    </form:form>
</div>