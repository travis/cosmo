<%--
/*
 * Copyright 2008 Open Source Applications Foundation
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
<%@ attribute name="selfLink"        %>
<%@ attribute name="stylesheets"     %>
<%@ attribute name="dojoLayers"     %>

<cosmo:staticbaseurl var="staticBaseUrl"/>

<fmt:setBundle basename="PimMessageResources" var="uiBundle"/>

<!DOCTYPE html
          PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">
  <head>
    <title>
      <fmt:message key="${prefix}HeadTitle" bundle="${uiBundle}">
        <c:forEach var="p" items="${TitleParam}">
          <fmt:param value="${p}"/>
        </c:forEach>
      </fmt:message>
    </title>
    
    <!-- Stylesheets -->
    <c:choose>
      <c:when  test="${not empty stylesheets}">
    	<c:set var="stylesheets" value="${stylesheets}"/>
	  </c:when>
	  <c:otherwise>
		<c:set var="stylesheets" value=""/>
	  </c:otherwise>
    </c:choose>

    <cosmo:stylesheets stylesheets="three_column,${stylesheets}"/>    
    
    <c:if test="${not empty selfLink}">
      <link rel="self" type="text/html" href="${selfLink }"/>
    </c:if>
    
    <cosmo:dojoBoilerplate dojoLayers="${dojoLayers}"/>
    <style type="text/css">
      /* tundraGrid.css matches Dijit Tundra style.  Others forthcoming.
      Use Grid.css on the same path for a more color-neutral theme */
      @import "${staticBaseUrl}/js/lib/dojo/dojox/grid/_grid/tundraGrid.css";
      @import "${staticBaseUrl}/js/lib/dojo/dijit/themes/tundra/tundra.css";
      @import "${staticBaseUrl}/js/lib/dojo/dojo/resources/dojo.css";
    </style>
  </head>
  <body class="tundra">
    <div id="logoHeader"></div>
    <div id="container">
      <!-- page body -->
      <jsp:doBody/>
      <!-- end page body -->
    </div>
  </body>
</html>
