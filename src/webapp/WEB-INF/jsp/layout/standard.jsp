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

<u:bind var="SC_ATTR_SERVER_ADMIN"
        type="org.osaf.cosmo.CosmoConstants"
        field="SC_ATTR_SERVER_ADMIN"/>
<u:bind var="SC_ATTR_VERSION"
        type="org.osaf.cosmo.CosmoConstants"
        field="SC_ATTR_VERSION"/>

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
            <html:link page="/">
              <img src="/cosmo_logo.gif" alt="Cosmo Sharing Server"/>
            </html:link>
          </div>
        </td>
        <c:if test="${showNav}">
          <td align="right" valign="top">
            <!-- main navbar -->
            <div class="mdData">
              <authz:authorize ifAllGranted="ROLE_ROOT">
                <html:link page="/home/"><fmt:message key="Layout.Nav.Main.HomeDirectories"/></html:link>
              |
              </authz:authorize>
              <%-- bug 3920
              <authz:authorize ifAllGranted="ROLE_USER">
                <html:link page="/dirlisting.jsp?rtype=home&username=${user.username}"><fmt:message key="Layout.Nav.Main.HomeDirectory"/></html:link>
              |
              </authz:authorize>
              --%>
              <html:link page="/logout">
                <fmt:message key="Layout.Nav.Main.LogOut"/>
              </html:link>
              |
              <a href="mailto:${applicationScope[SC_ATTR_SERVER_ADMIN]}">
                <fmt:message key="Layout.Nav.Main.Help"/>
              </a>
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
            <c:choose><c:when test="${fn:endsWith(body, '/user/list.jsp')}"><b><fmt:message key="Layout.Nav.Console.Users"/></b></c:when><c:otherwise><html:link page="/users"><fmt:message key="Layout.Nav.Console.Users"/></html:link></c:otherwise></c:choose>
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
    <!-- footer -->
    <html:img page="/spacer.gif" width="1" height="60" alt=""
              border="0" styleId="footerSpacer"/>
    <hr noshade="noshade"/>
    <div class="footer">
      <a href="mailto:${applicationScope[SC_ATTR_SERVER_ADMIN]}">
        <fmt:message key="Layout.Footer">
          <fmt:param value="${applicationScope[SC_ATTR_VERSION]}"/>
        </fmt:message>
      </a>
      <jsp:useBean id="now" class="java.util.Date"/>
      &nbsp;&nbsp;&nbsp;
      <fmt:formatDate value="${now}" type="both"/>
    </div>
    <script language="JavaScript" type="text/javascript">
      setFoot();
    </script>
    <!-- end footer -->
  </body>
</html:html>
