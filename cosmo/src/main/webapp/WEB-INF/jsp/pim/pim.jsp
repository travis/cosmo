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
<c:set var="ticketedView" value="${not empty ticketKey }"/>
<cosmoui:user var="user"/>

<fmt:setBundle basename="PimMessageResources"/>

<u:bind var="PRODUCT_VERSION"
        type="org.osaf.cosmo.CosmoConstants"
        field="PRODUCT_VERSION"/>

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

<title><fmt:message key="App.Welcome"/></title>

<link rel="stylesheet" href="${staticBaseUrl}/templates/default/global.css"/>

<c:if test="${not empty relationLinks}">
<link rel="self" type="text/html" href="${relationLinks['pim']}"/>
<link rel="alternate" type="application/atom+xml" href="${relationLinks['atom']}"/>
<link rel="alternate" type="text/html" href="${relationLinks['dav']}"/>
<link rel="alternate" type="text/xml" href="${relationLinks['mc']}"/>
<link rel="alternate" type="text/calendar" href="${relationLinks['webcal']}"/>
</c:if>

<cosmo:dojoBoilerplate timezones="true"/>

<script type="text/javascript" src="${staticBaseUrl}/js-${PRODUCT_VERSION}/lib/jsonrpc-java-js/jsonrpc.js"></script>

<script type="text/javascript">
// Dojo requires
dojo.require('cosmo.app');
dojo.require('cosmo.ui.cal_main');
dojo.require('cosmo.ui.global_css');
dojo.require('cosmo.convenience');
dojo.require('cosmo.topics');
dojo.require('cosmo.account.preferences');
dojo.require('cosmo.account.settings');

cosmo.app.initObj = cosmo.ui.cal_main.Cal;
cosmo.app.initParams = {};

<c:if test="${collection != null}">
cosmo.app.initParams.collectionUid = '${collection.uid}';
</c:if>
<c:if test="${not empty ticketKey}">
cosmo.app.initParams.ticketKey = '${ticketKey}';
</c:if>
<c:if test="${not ticketedView}">
cosmo.app.initParams.authAccess = true;
</c:if>

dojo.require("cosmo.ui.event.listeners");
cosmo.ui.event.listeners.hookUpListeners();

</script>

</head>

<body id="body">
        <div id="menuBarDiv">
          <div id="smallLogoDiv"></div>
            <%-- Begin main nav menu --%>
            <div id="menuNavItems">
            <c:choose>
              <c:when test="${not ticketedView}">
              	<%-- Start non-ticketed links --%>
                <authz:authorize ifAnyGranted="ROLE_USER">
                <fmt:message key="Main.Welcome"><fmt:param value="${user.username}"/></fmt:message>
                  <span class="menuBarDivider">|</span>
                </authz:authorize>
                <authz:authorize ifAllGranted="ROLE_ROOT">
                  <c:url var="consoleUrl" value="/admin/users"/>
                  <a href="${consoleUrl}"><fmt:message key="Main.Console"/></a>
                  <span class="menuBarDivider">|</span>
                </authz:authorize>
                <a href="javascript:cosmo.account.settings.showDialog();">
                  Settings
                </a>
                <span class="menuBarDivider">|</span>
                <span id="accountBrowserLink" style="display: none;">
                <a href="${staticBaseUrl}/browse/${user.username}" 
                  onclick="window.open('${staticBaseUrl}/browse/${user.username}'); 
                  return false;">
                  Account Browser
                </a>
                <span class="menuBarDivider">|</span>
                </span>
                <%-- End non-ticketed links --%>
              </c:when>
              <c:otherwise>
              	<%-- 
                    Ticketed version of links
                    Add divs for subscribeSelector, signupGraphic via JavaScript DOM
                    so we can get accurate offsetWidth as code executes
                --%>
              </c:otherwise>
            </c:choose>
              
            <c:url var="helpUrl" 
                value="http://wiki.osafoundation.org/bin/view/Projects/CosmoHelpPortal"/>
            <a href="${helpUrl}" 
                onclick="window.open('${helpUrl}'); 
                return false;"><fmt:message key="Main.Help"/></a>
                
            <c:if test="${not ticketedView}">
               <authz:authorize ifAnyGranted="ROLE_USER">
                 <span class="menuBarDivider">|</span>
                  <a href="${staticBaseUrl}/logout">
                    <fmt:message key="Main.LogOut"/>
                  </a>
               </authz:authorize>
            </c:if>&nbsp;&nbsp;
          </div>
          <%-- End main nav menu --%>
        </div>
        <div id="calDiv">
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
                <div id="allDayResizeMainDiv" onSelectStart="return false;">
                    <div id="allDayHourSpacerDiv"></div>
                    <div id="allDayContentDiv"></div>
                </div>
                <div id="allDayResizeHandleDiv"></div>
                <div id="timedScrollingMainDiv" onSelectStart="return false;">
                    <div id="timedHourListDiv"></div>
                    <div id="timedContentDiv"></div>
                </div>
                <div id="rightSidebarDiv">
                    <form method="post" id="calForm" action="">
                    <div id="eventInfoDiv"></div>
            </form>
        </div>
        </div>
        <div id="maskDiv">
          <div id="processingDiv">
              <fmt:message key="Main.Processing" />
          </div>
        </div>
</body>

</html>
