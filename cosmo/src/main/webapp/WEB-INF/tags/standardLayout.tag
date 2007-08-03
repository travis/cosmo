<%--
/*
 * Copyright 2005-2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
--%>
<%@ include   file="/WEB-INF/jsp/taglibs.jsp"            %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<%@ attribute name="prefix" 		%>
<%@ attribute name="showNav"        %>
<%@ attribute name="contentWrapperClass"     %>
<%@ attribute name="selfLink"        %>
<%@ attribute name="stylesheets"     %>
<%@ attribute name="parseWidgets"    %>
<%@ attribute name="searchIds"       %>

<cosmo:staticbaseurl var="staticBaseUrl"/>

<c:if test="${empty contentWrapperClass}">
  <c:set var="contentWrapperClass" value="mainInfoBox"/>
</c:if>

<c:if test="${empty showNav}">
  <c:set var="showNav" value="true"/>
</c:if>

<c:if test="${showNav}">
  <cosmoui:user var="user"/>
</c:if>

<fmt:setBundle basename="PimMessageResources" var="uiBundle"/>

<fmt:message key="App.TemplateName" var="templateName" bundle="${uiBundle}"/>

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">
  <head>
    <title>
      <fmt:message key="${prefix}HeadTitle">
        <c:forEach var="p" items="${TitleParam}">
          <fmt:param value="${p}"/>
        </c:forEach>
      </fmt:message>
    </title>
    
    <!-- Stylesheets -->
    <c:choose>
    <c:when  test="${not empty stylesheets}">
    	<c:set var="stylesheets" value="admin,${stylesheets}"/>
	</c:when>
	<c:otherwise>
		<c:set var="stylesheets" value="admin"/>
	</c:otherwise>
    </c:choose>

    <cosmo:stylesheets stylesheets="${stylesheets}"/>
    
    
    <c:if test="${not empty selfLink}">
    <link rel="self" type="text/html" href="${selfLink }"/>
    </c:if>

    <cosmo:dojoBoilerplate parseWidgets="${parseWidgets}" searchIds="${searchIds}"/>

  </head>
  <body class="adminPage">
    <div id="menuBar">
          <div id="mainLogoContainer">
            <a href="<c:url value="/account/view"/>">
              <img id="logo" src="${staticBaseUrl}/templates/${templateName}/images/<fmt:message key="App.LogoUri" bundle="${uiBundle}"/>"
              	   alt="<fmt:message key="App.Name"  bundle="${uiBundle}"/>"/>
            </a>
          </div>
        <c:if test="${showNav}">
            <!-- main navbar -->
            <div id="menuNavItems">
              <fmt:message key="Layout.Nav.Main.Welcome"><fmt:param value="${user.username}"/></fmt:message>
              <authz:authorize ifAnyGranted="ROLE_USER">
                |
                <c:url var="homeUrl" value="/browse/${user.username}"/>
                <a href="${homeUrl}"><fmt:message key="Layout.Nav.Main.Home"/></a>
                |
                <c:url var="calendarUrl" value="/pim"/>
                <a href="${calendarUrl}"><fmt:message key="Layout.Nav.Main.Calendar"/></a>
              </authz:authorize>
        <authz:authorize ifAllGranted="ROLE_ROOT">
          <!-- admin console navbar -->
            |
            <c:choose>
            <c:when test="${fn:endsWith(body, '/user/list')}">
            	<strong><fmt:message key="Layout.Nav.Console.Users"/></strong>
            </c:when>
            <c:otherwise>
	            <a href="<c:url value="/admin/users"/>">
	            <fmt:message key="Layout.Nav.Console.Users"/></a>
		    </c:otherwise>
		    </c:choose>
		    |
            <c:choose><c:when test="${fn:endsWith(body, '/status/view')}"><strong><fmt:message key="Layout.Nav.Console.ServerStatus"/></strong></c:when><c:otherwise><a href="<c:url value="/admin/status"/>"><fmt:message key="Layout.Nav.Console.ServerStatus"/></a></c:otherwise></c:choose>
            <!-- end admin console navbar -->
        </authz:authorize>
              |
              <a href="<fmt:message key="Main.CollectionDetails.HelpLink" bundle="${uiBundle}"/>"><fmt:message key="Layout.Nav.Main.Help"/></a>
              |
              <a href="<c:url value="/logout"/>">
                <fmt:message key="Layout.Nav.Main.LogOut"/>
              </a>
            <!-- end main navbar -->


            </div>
        </c:if>
	</div>
    <div class="md" id="contentDiv">
    	<div id="contentWrapper" class="${contentWrapperClass}">

        <!-- page body -->
        <jsp:doBody/>
        <!-- end page body -->
      	<div class="aboutChandlerServer"><cosmo:aboutPopupLink/></div>
		</div>
    </div>
    <div id="debug"></div>
  </body>
</html>

