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
<%@ attribute name="user"               required="true"
              type="org.osaf.cosmo.model.User"           %>
<%@ attribute name="reverse"            required="false" %>
<%@ attribute name="var"                required="true"
              rtexprvalue="false"                        %>
<%@ variable  name-from-attribute="var" alias="result"
              scope="AT_END"                             %>

<c:if test="${empty user}">
  <cosmoui:user var="user"/>
</c:if>

<c:if test="${empty reverse}">
  <c:set var="reverse" value="false"/>
</c:if>

<c:choose>
  <c:when test="${reverse}">
    <c:set var="result" value="${user.lastName}, ${user.firstName}"/>
  </c:when>
  <c:otherwise>
    <c:set var="result" value="${user.firstName} ${user.lastName}"/>
  </c:otherwise>
</c:choose>

