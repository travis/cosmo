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
<%@ attribute name="property"           required="true"
              rtexprvalue="false"                        %>
<%@ attribute name="value"              required="true"
              type="org.osaf.cosmo.model.TriageStatus"   %>

<c:choose>
  <c:when test="${property eq 'code'}">
    <c:choose>
      <c:when test="${value.code != null}">
        <c:out value="${cosmoui:triageStatusLabel(value.code)}"/>
      </c:when>
      <c:otherwise>
        -
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:when test="${property eq 'rank'}">
    <c:choose>
      <c:when test="${value.code != null}">
        <c:out value="${value.rank}"/>
      </c:when>
      <c:otherwise>
        -
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:when test="${property eq 'auto'}">
    <c:choose><c:when test="${value.autoTriage}">Yes</c:when><c:otherwise>No</c:otherwise></c:choose>
  </c:when>
</c:choose>
