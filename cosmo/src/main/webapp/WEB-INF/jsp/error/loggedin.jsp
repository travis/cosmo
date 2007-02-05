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

<cosmo:standardLayout prefix="Error.LoggedIn." showNav="false">
<div class="mainInfoBox">
	<p class="mainInfoBoxText">
	You are already logged in as ${user.username}. Go to your <a href="${baseUrl}/pim"> Calendar</a>.<br/>
	Not ${user.username}? <a href="${baseUrl}/logout">Log out</a>.<br/>
	Don't have an account? <a href="${baseUrl}/logout?signup=true">Sign up</a>.
	</p>
	<hr/>
</div>
</cosmo:standardLayout>