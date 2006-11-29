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

<div class="hd" style="margin-top: 12px;">
  Property Indexes
</div>

<div style="margin-top:12px;">
  <table cellpadding="4" cellspacing="1" border="0" width="100%">
    <tr>
      <td class="smTableColHead" style="text-align:left;">
        Name
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Value
      </td>
    </tr>
    <c:forEach var="index" items="${Item.stampMap['event'].propertyIndexes}">
    <tr>
      <td class="smTableData" width="50%">
        ${index.name}
      </td>
      <td class="smTableData" width="50%">
        ${index.value}
      </td>
    </tr>
    </c:forEach>
  </table>
</div>

<div class="hd" style="margin-top: 12px;">
  Time Range Indexes
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
        Recurring?
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Floating?
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Type
      </td>
    </tr>
    <c:forEach var="index" items="${Item.stampMap['event'].timeRangeIndexes}">
    <tr>
      <td class="smTableData" width="50%">
        ${index.startDate}
      </td>
      <td class="smTableData" width="50%">
        <c:choose><c:when test="${index.endDate != null}">${index.endDate}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
      <td class="smTableData" width="50%">
        ${index.isRecurring}
      </td>
      <td class="smTableData" width="50%">
        ${index.isFloating}
      </td>
      <td class="smTableData" width="50%">
        ${index.type}
      </td>
    </tr>
    </c:forEach>
  </table>
</div>
