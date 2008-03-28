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

<cosmo:staticbaseurl var="staticBaseUrl"/>

<cosmo:standardLayout prefix="Error.Forbidden." showNav="false" selfLink="${staticBaseUrl}/error/forbidden">
<%--
    Because Acegi Security's filters don't execute for forwarded requests,
    the SecurityContextHolder for this thread has been cleared, and we
    need to access the http session directly.
  --%>

<u:bind var="SecureContext"
        type="org.acegisecurity.context.HttpSessionContextIntegrationFilter"
        field="ACEGI_SECURITY_CONTEXT_KEY"/>

<c:if test="${not empty sessionScope[SecureContext] &&
              not empty sessionScope[SecureContext].authentication}">
  <c:set var="authen" value="${sessionScope[SecureContext].authentication}"/>
  <c:forEach var="role" items="${authen.authorities}">
    <c:if test="${role == 'ROLE_USER'}">
      <c:set var="isUser" value="true"/>
    </c:if>
    <c:if test="${role == 'ROLE_ROOT'}">
      <c:set var="isAdmin" value="true"/>
    </c:if>
  </c:forEach>
</c:if>

<c:choose>
  <c:when test="${isUser}">
  <c:set var="username" value="${authen.principal.username}"/>
<p>
  <fmt:message key="Error.Forbidden.NotWhileLoggedIn">
    <fmt:param><c:out value="${username}"/></fmt:param>
  </fmt:message>
</p>
<p>
  <a href="<c:url value="/logout"/>">
    <fmt:message key="Error.Forbidden.ClickToLogOut"/>
  </a>
</p>
  </c:when>
  <c:when test="${isAdmin}">
<p>
  <fmt:message key="Error.Forbidden.NotAsAdmin"/>
</p>
<p>
  <a href="<c:url value="/logout"/>">
    <fmt:message key="Error.Forbidden.ClickToLogOut"/>
  </a>
</p>
  </c:when>
  <c:otherwise>
<p>
  <fmt:message key="Error.Forbidden.NotAllowed"/>
</p>
  </c:otherwise>
</c:choose>
</cosmo:standardLayout>