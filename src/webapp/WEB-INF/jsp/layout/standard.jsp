<%--
/*
 * Copyright 2005 Open Source Applications Foundation
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

<tiles:importAttribute name="body"/>
<tiles:importAttribute name="prefix"/>
<tiles:importAttribute name="showNav" ignore="true"/>

<c:if test="${empty showNav}">
  <c:set var="showNav" value="true"/>
  <cosmoui:user var="user"/>
</c:if>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html:html xhtml="true">
  <head>
    <title>
      <fmt:message key="${prefix}HeadTitle">
        <c:forEach var="p" items="${TitleParam}">
          <fmt:param value="${p}"/>
        </c:forEach>
      </fmt:message>
    </title>
    <link rel="stylesheet" type="text/css"
          href="<html:rewrite page="/cosmo.css"/>"/>
    <script type="text/javascript"
            src="<html:rewrite page="/cosmo.js"/>"></script>
  </head>
  <body class="bodystyle">
    <table border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <td align="left" valign="top">
          <div class="lg">
            <html:link page="/console/account">
              <img src="/cosmo_logo.gif" alt="Cosmo Sharing Server"/>
            </html:link>
          </div>
        </td>
        <c:if test="${showNav}">
          <td align="right" valign="top">
            <!-- main navbar -->
            <div class="mdData">
              <%-- bug 3920
              <authz:authorize ifAllGranted="ROLE_USER">
                <html:link page="/console/dirlisting.jsp?rtype=home&username=${user.username}"><fmt:message key="Layout.Nav.Main.HomeDirectory"/></html:link>
              |
              </authz:authorize>
              --%>
              <c:choose><c:when test="${fn:endsWith(body, '/help.jsp')}"><strong><fmt:message key="Layout.Nav.Main.Help"/></strong></c:when><c:otherwise><html:link page="/console/help"><fmt:message key="Layout.Nav.Main.Help"/></html:link></c:otherwise></c:choose>
              |
              <html:link page="/console/about" onclick="simplePopUp('/console/about', 340, 280, false); return false;"><fmt:message key="Layout.Nav.Main.About"/></html:link>
              |
              <html:link page="/console/logout">
                <fmt:message key="Layout.Nav.Main.LogOut"/>
              </html:link>
            </div>
            <div class="mdData" style="margin-top:8px;">
              <fmt:message key="Layout.Nav.Main.LoggedInAs">
                <fmt:param value="${user.username}"/>
              </fmt:message>
            </div>
            <!-- end main navbar -->
          </td>
        </c:if>
      </tr>
    </table>
    <hr noshade="noshade"/>
    <c:choose>
      <c:when test="${showNav}">
        <authz:authorize ifAllGranted="ROLE_ROOT">
          <!-- admin console navbar -->
          <div class="md">
            <fmt:message key="Layout.Nav.Console.Label"/>
            <c:choose><c:when test="${fn:endsWith(body, '/user/list.jsp')}"><strong><fmt:message key="Layout.Nav.Console.Users"/></strong></c:when><c:otherwise><html:link page="/console/users"><fmt:message key="Layout.Nav.Console.Users"/></html:link></c:otherwise></c:choose>
            |
            <html:link page="/console/home/browse/"><fmt:message key="Layout.Nav.Console.HomeDirectories"/></html:link>
            |
            <c:choose><c:when test="${fn:endsWith(body, '/status/view.jsp')}"><strong><fmt:message key="Layout.Nav.Console.ServerStatus"/></strong></c:when><c:otherwise><html:link page="/console/status"><fmt:message key="Layout.Nav.Console.ServerStatus"/></html:link></c:otherwise></c:choose>
            <!-- end admin console navbar -->
          </div>
          <hr noshade="noshade"/>
        </authz:authorize>
      </c:when>
      <c:otherwise>
      </c:otherwise>
    </c:choose>
    <div class="md">
      <!-- page body -->
      <tiles:insert attribute="body" flush="false"/>
      <!-- end page body -->
    </div>
  </body>
</html:html>
