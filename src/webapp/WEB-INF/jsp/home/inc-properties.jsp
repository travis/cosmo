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

<c:if test="${isCollection}">
  <c:set var="Item" value="${Collection}"/>
</c:if>

<div class="hd" style="margin-top: 12px;">
  Properties
</div>

<div style="margin-top:12px;">
  <table cellpadding="4" cellspacing="1" border="0" width="100%">
    <tr>
       <td class="smTableColHead" style="text-align:left;">
        Namespace
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Name
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Value
      </td>
    </tr>
    <c:forEach var="entry" items="${Item.attributes}">
    <tr>
      <td class="smTableData" width="50%">
        ${entry.key.namespace}
      </td>
      <td class="smTableData" width="50%">
        ${entry.key.localName}
      </td>
      <td class="smTableData" width="50%">
        ${entry.value.value}
      </td>
    </tr>
    </c:forEach>
  </table>
</div>
