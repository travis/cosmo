<%--
/*
 * Copyright 2007 Open Source Applications Foundation
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
<%@ attribute name="value"              required="true"
              type="java.lang.Integer"                   %>

<c:choose>
  <c:when test="${value != null}">
    <c:choose>
      <c:when test="${value == 100}">EDITED</c:when>
      <c:when test="${value == 200}">QUEUED</c:when>
      <c:when test="${value == 300}">SENT</c:when>
      <c:when test="${value == 400}">UPDATED</c:when>
      <c:when test="${value == 500}">CREATED</c:when>
      <c:otherwise><c:out value="${value}"/></c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    -
  </c:otherwise>
</c:choose>
