<%--
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
<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<fmt:setBundle basename="PimMessageResources"/>

<cosmoui:user var="user"/>

<div id="menuBar">
<div id="cosmoMainLogo">
</div>
<div id="menuNavItems">

<%--
Logged in items
--%>
<security:authorize ifAnyGranted="ROLE_USER,ROLE_ROOT">
<span id="welcomeMenuItem">
<fmt:message key="Main.Welcome">
	<fmt:param>${user.username}</fmt:param>
</fmt:message>
</span>
</security:authorize>

<%-- admin only --%>
<security:authorize ifAllGranted="ROLE_ROOT">
<span class="menuBarDivider"> | </span>
<span id="adminConsoleMenuItem">
<a class="menuBarLink" href="/admin/users"><fmt:message key="Main.Console"/></a>
</span>
</security:authorize>
<%-- end admin only --%>

<security:authorize ifAnyGranted="ROLE_USER,ROLE_ROOT">
<span class="menuBarDivider"> | </span>

<span id="settingsMenuItem">
<a class="menuBarLink" onclick="cosmo.account.settings.showDialog(); return false;"><fmt:message key="Main.Settings"/></a>
</span>

<span class="menuBarDivider"> | </span>

<%-- TODOrequire account browser pref --%>
<span id="accountBrowserMenuItem">
<a class="menuBarLink" href="/browse/${user}" target="_blank"><fmt:message key="Main.AccountBrowser"/></a>
</span>
<%-- end  --%>
<span class="menuBarDivider"> | </span>

<span id="logoutMenuItem">
<a class="menuBarLink" href="/logout"><fmt:message key="Main.LogOut"/></a>
</span>

<span class="menuBarDivider">&nbsp;&nbsp;</span>
</security:authorize>

<%--
Logged out items
--%>

<security:authorize ifAllGranted="ROLE_ANONYMOUS">
<c:set var="disableSignups" value="false"/>
<c:if test="${not empty properties}">
  <c:set var="disableSignups" value="${properties['cosmo.service.account.disableSignups']}"/>
</c:if>
<c:if test="${not disableSignups}">
  <span id="signupMenuItem">
    <a class="menuBarLink" onclick="cosmo.account.create.showForm()"><fmt:message key="Main.Signup"/></a>
  </span>
</c:if>

<span class="menuBarDivider"> | </span>

<span id="loginMenuItem">
<a class="menuBarLink" href="/login"> <fmt:message key="Main.LogIn"/></a>
</span>

<span class="menuBarDivider"> | </span>

<span id="whatIsMenuItem">
<a id="whatIsChandlerLink" href="http://chandlerproject.org" target="_blank"><fmt:message key="Main.WhatIsChandler"/></a>
</span>

<span class="menuBarDivider">&nbsp;&nbsp;</span>

<span id="aboutMenuItem">
<a class="menuBarLink" onClick="cosmo.util.popup.open(cosmo.env.getFullUrl("About"), 360, 280)"><fmt:message key="Main.About"/></a>
</span>

<span class="menuBarDivider"> | </span>

</security:authorize>

<%--
Both 
--%>

<%-- TODO only if tos enabled --%>
<span id="tosMenuItem">
<a class="menuBarLink" href="/help/tos" target="_blank"><fmt:message key="Main.TermsOfService"/></a>
</span>

<span class="menuBarDivider"> | </span>

<span id="privacyMenuItem">
<a class="menuBarLink" href="/help/privacy" target="_blank"><fmt:message key="Main.PrivacyPolicy"/></a>
</span>
<%-- end only if tos enabled --%>

<span class="menuBarDivider"> | </span>

<span id="helpMenuItem">
<a class="menuBarLink" href="http://chandlerproject.org/Projects/SubscribeToChandlerServer" target="_blank"><fmt:message key="Main.Help"/></a>
</span>
</div>
</div>
