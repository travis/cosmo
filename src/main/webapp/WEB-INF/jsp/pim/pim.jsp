<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
/*
 * Copyright 2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
--%>


<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>
<cosmo:baseurl var="baseUrl"/>
<cosmo:staticbaseurl var="staticBaseUrl"/>
<cosmoui:user var="user"/>

<fmt:setBundle basename="PimMessageResources"/>

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

<title><fmt:message key="App.Welcome"/></title>

<c:if test="${not empty relationLinks}">
<link rel="self" type="text/html" href="${relationLinks['web']}"/>
<link rel="alternate" type="application/atom+xml" href="${relationLinks['atom']}"/>
<link rel="alternate" type="text/html" href="${relationLinks['dav']}"/>
<link rel="alternate" type="text/xml" href="${relationLinks['mc']}"/>
<link rel="alternate" type="text/calendar" href="${relationLinks['webcal']}"/>
</c:if>

<cosmo:dojoBoilerplate timezones="true"/>

<script type="text/javascript" src="${staticBaseUrl}/js/lib/jsonrpc-java-js/jsonrpc.js"></script>

<script type="text/javascript">
// Dojo requires
dojo.require('cosmo.app');
dojo.require('cosmo.ui.cal_main');
dojo.require('cosmo.ui.global_css');

// FIXME: Need to get timeout value from server
var TIMEOUT_MIN = 30;

// Added automatically to window.onload by 
// cosmo.ui.event.listeners.hookUpListeners
cosmo.ui.event.handlers.init = function () {
    var collectionUid = undefined;
    var ticketKey = undefined;
<c:if test="${collection != null}">
    collectionUid = '${collection.uid}';
</c:if>
<c:if test="${not empty ticketKey}">
    ticketKey = '${ticketKey}';
</c:if>
    
    cosmo.app.initObj = Cal;
    cosmo.app.init(collectionUid, ticketKey);
}

dojo.require("cosmo.ui.event.listeners");
cosmo.ui.event.listeners.hookUpListeners();

</script>

</head>

<body id="body">
        <div id="menuBarDiv">
            <div id="smallLogoDiv"></div>
            <%-- Begin main nav menu --%>
            <c:choose>
              <c:when test="${empty ticketKey}">
                <authz:authorize ifAnyGranted="ROLE_USER">
                <fmt:message key="Main.Welcome"><fmt:param value="${user.username}"/></fmt:message>
                  <span class="menuBarDivider">|</span>
                  <c:url var="homeUrl" value="/browse/${user.username}"/>
                  <a href="${homeUrl}"><fmt:message key="Main.Home"/></a>
                  <span class="menuBarDivider">|</span>
                  <c:url var="accountUrl" value="/account/view"/>
                  <a href="${accountUrl}"><fmt:message key="Main.Account"/></a>
                  <span class="menuBarDivider">|</span>
                  <c:url var="calendarUrl" value="/pim"/>
                  <a href="${calendarUrl}"><fmt:message key="Main.Calendar"/></a>
                  <span class="menuBarDivider">|</span>
                </authz:authorize>
                <authz:authorize ifAllGranted="ROLE_ROOT">
                  <span class="menuBarDivider">|</span>
                  <c:url var="consoleUrl" value="/admin/users"/>
                  <a href="${consoleUrl}"><fmt:message key="Main.Console"/></a>
                  <span class="menuBarDivider">|</span>
                </authz:authorize>
                <c:url var="helpUrl" value="/help"/>
                <a href="${helpUrl}"><fmt:message key="Main.Help"/></a>
                <span class="menuBarDivider">|</span>
                <c:url var="aboutUrl" value="/help/about"/>
                <a href="javascript:Popup.open('${aboutUrl}', 380, 280);">
                  <fmt:message key="Main.About"/>
                </a>
                <authz:authorize ifAnyGranted="ROLE_USER">
                <span class="menuBarDivider">|</span>
                <a href="${staticBaseUrl}/logout">
                   <fmt:message key="Main.LogOut"/>
                </a>&nbsp;&nbsp;
                </authz:authorize>
              </c:when>
              <c:otherwise>
                <div id="signupGraphic"></div>
                <div id="subscribeSelector"></div>
              </c:otherwise>
            </c:choose>
            <%-- End main nav menu --%>
        </div>
        <div id="calDiv">
            <form method="post" id="calForm" name="calForm" action="">
                <div id="leftSidebarDiv">
                    <div id="calSelectNav"></div>
                    <div id="jumpToDateDiv"></div>
                    <div id="miniCalDiv"></div>
                </div>
                <div id="calTopNavDiv">
                    <table cellpadding="0" cellspacing="0">
                        <tr>
                            <td>&nbsp;&nbsp;&nbsp;</td>
                            <td id="viewNavButtons"></td>
                            <td>&nbsp;&nbsp;&nbsp;</td>
                            <td id="monthHeaderDiv" class="labelTextXL"></td>
                        </tr>
                    </table>
                </div>
                <div id="dayListDiv"></div>
                <div id="allDayResizeMainDiv">
                    <div id="allDayHourSpacerDiv"></div>
                    <div id="allDayContentDiv"></div>
                </div>
                <div id="allDayResizeHandleDiv"></div>
                <div id="timedScrollingMainDiv">
                    <div id="timedHourListDiv"></div>
                    <div id="timedContentDiv"></div>
                </div>
                <div id="rightSidebarDiv">
                    <div id="eventInfoDiv"></div>
                </div>
            </form>
        </div>
        <div id="maskDiv">
          <div id="processingDiv">
              <fmt:message key="Main.Processing" />
          </div>
        </div>
</body>

</html>
