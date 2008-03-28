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

<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>
<fmt:setBundle basename="PimMessageResources"/>
<cosmo:standardLayout prefix="Account.Activate." showNav="false" stylesheets="activation">
<cosmo:staticbaseurl var="staticBaseUrl"/>

<span id="congratulations"><fmt:message key="Account.Activate.Congrats"/></span>
<span id="activatedMessage"><fmt:message key="Account.Activate.ActivatedMessage"/></span>
<table id="user">
<tbody>
<tr><td class="label"><fmt:message key="Account.Activate.Username"/></td><td><c:out value="${user.username}"/></td></tr>
<tr><td class="label"><fmt:message key="Account.Activate.Email"/></td><td><c:out value="${user.email}"/></td></tr>
</tbody>
</table>
<a id="login" href="${staticBaseUrl}/login">
<fmt:message key="Account.Activate.ActivatedMessageLogIn"/></a>

<hr class="seperator"/>

<span class="publishInstructions"><fmt:message key="Help.PublishInstructions"/></span>

</cosmo:standardLayout>