<%--
/*
 * Copyright 2005 Open Source Applications Foundation
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

<tiles:importAttribute name="resource"/>

<div class="hd" style="margin-top: 12px;">
  Tickets
</div>

<div style="margin-top:12px;">
  <table cellpadding="4" cellspacing="1" border="0" width="100%">
    <tr>
      <td class="smTableColHead" style="width:1%;">
        &nbsp;
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Id
      </td>
      <td class="smTableColHead">
        Owner
      </td>
      <td class="smTableColHead">
        Timeout
      </td>
      <td class="smTableColHead">
        Privileges
      </td>
      <td class="smTableColHead">
        Created
      </td>
    </tr>
    <c:forEach var="ticket" items="${resource.tickets}">
    <tr>
      <td class="smTableData" style="text-align:center; white-space:nowrap;">
        <html:link page="/console/home${resource.path}/ticket/${ticket.id}/revoke">
          [revoke]
        </html:link>    
      </td>
      <td class="smTableData">
        ${ticket.id}
      </td>
      <td class="smTableData" style="text-align:center;">
        ${ticket.owner}
      </td>
      <td class="smTableData" style="text-align:center;">
        ${ticket.timeout}
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:forEach var="privilege" items="${ticket.privileges}">
          ${privilege}
        </c:forEach>
      </td>
      <td class="smTableData" style="text-align:center;">
        <fmt:formatDate value="${ticket.created}" type="both"/>
      </td>
    </tr>
    </c:forEach>
  </table>
</div>
