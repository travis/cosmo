<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
/*
 * Copyright 2005-2006 Open Source Applications Foundation
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

<%@ page    isErrorPage="true"               %>
<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>
<c:if test="${empty showNav}">
  <c:set var="showNav" value="false"/>
</c:if>

<cosmoui:user var="user"/>
<cosmo:baseurl var="baseUrl"/>
<fmt:setBundle basename="PimMessageResources"/>
<cosmo:standardLayout prefix="Error.LoggedIn." showNav="false" stylesheets="error">
	<div class="loggedInMessage">
	<fmt:message key="Error.LoggedIn">
		<fmt:param><c:out value="${user.username}"/></fmt:param>
		<fmt:param><a href='${baseUrl}/pim'></fmt:param>
		<fmt:param></a></fmt:param>
	</fmt:message>
	</div>
	<div class="loggedInMessage">
	<fmt:message key="Error.LoggedIn.LogOut">
		<fmt:param><c:out value="${user.username}"/></fmt:param>
		<fmt:param><a href='${baseUrl}/logout'></fmt:param>
		<fmt:param></a></fmt:param>
	</fmt:message>
	</div>
	<div class="loggedInMessage">
	<fmt:message key="Error.LoggedIn.SignUp">
		<fmt:param><a href='${baseUrl}/logout?signup=true'></fmt:param>
		<fmt:param></a></fmt:param>
	</fmt:message>
	</div>
	<hr/>
</cosmo:standardLayout>