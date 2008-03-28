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
<%@ include file="/WEB-INF/jsp/taglibs.jsp"    %>
<%@ attribute name="exception" required="true"
              type="java.lang.Throwable"       %>

<c:if test="${not empty exception}">
  <div class="pre">
<c:out value="${exception}"/><c:forEach var="element" items="${exception.stackTrace}">
    <c:out value="at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"/></c:forEach>
</div>
  <c:if test="${not empty exception.cause}">
    <div class="pre">
<c:out value="${exception.cause}"/><c:forEach var="element" items="${exception.cause.stackTrace}">
    <c:out value="at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"/></c:forEach>
  </div>
  </c:if>
</c:if>
