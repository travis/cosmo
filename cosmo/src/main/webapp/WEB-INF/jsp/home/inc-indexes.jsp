<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
/*
 * Copyright 2006 Open Source Applications Foundation
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

<c:choose>
  <c:when test="${not empty Item.stampMap['eventexception']}">
    <c:set var="index" value="${Item.stampMap['eventexception'].timeRangeIndex}"/>
  </c:when>
  <c:otherwise>
    <c:set var="index" value="${Item.stampMap['event'].timeRangeIndex}"/>
  </c:otherwise>
</c:choose>

<div class="hd" style="margin-top: 12px;">
  Time Range Index
</div>

<div style="margin-top:12px;">
  <table cellpadding="4" cellspacing="1" border="0" width="100%">
    <tr>
      <td class="smTableColHead" style="text-align:left;">
        Start Date
      </td>
      <td class="smTableColHead" style="text-align:left;">
        End Date
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Floating?
      </td>
    </tr>
    <tr>
      <td class="smTableData" width="50%">
        <c:out value="${index.startDate}"/>
      </td>
      <td class="smTableData" width="50%">
        <c:out value="${index.endDate}"/>
      </td>
      <td class="smTableData" width="50%">
        <c:out value="${index.isFloating}"/>
      </td>
    </tr>
  </table>
</div>
