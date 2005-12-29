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

<div class="hd" style="margin-top: 12px;">
  <fmt:message key="HomeDirectory.Collection.Title">
    <fmt:param value="${Collection.path}"/>
  </fmt:message>
</div>

<c:if test="${Collection.class.name == 'org.osaf.cosmo.model.CalendarCollectionResource'}">
<div style="margin-top:12px;">
<html:link page="/console/home/download${Collection.path}">
  [download as iCalendar]
</html:link>
<html:link page="/console/home/view${Collection.path}">
  [view as HTML]
</html:link>
<html:link page="/console/home/feed${Collection.path}">
  [subscribe to feed]
</html:link>
</div>
</c:if>

<c:if test="${Collection.path != '/'}">
<div style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Display Name
      </td>
      <td class="mdData">
        ${Collection.displayName}
      </td>
    </tr>
    <c:if test="${Collection.class.name == 'org.osaf.cosmo.model.HomeCollectionResource' || Collection.class.name == 'org.osaf.cosmo.model.CalendarCollectionResource'}">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Description
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Collection.description}">${Collection.description}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Language
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Collection.language}">${Collection.language}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    </c:if>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Created
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Collection.dateCreated}" type="both"/>
      </td>
    </tr>
  </table>
</div>
</c:if>

<div class="hd" style="margin-top: 12px;">
  Contents
</div>

<div style="margin-top:12px;">
  <table cellpadding="4" cellspacing="1" border="0" width="100%">
    <tr>
      <td class="smTableColHead" style="width:1%;">
        &nbsp;
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Name
      </td>
      <td class="smTableColHead">
        Type
      </td>
      <td class="smTableColHead">
        Created
      </td>
      <td class="smTableColHead">
        Last Modified
      </td>
      <td class="smTableColHead">
        Size
      </td>
    </tr>
    <c:forEach var="resource" items="${Collection.resources}">
    <tr>
      <td class="smTableData" style="text-align:center; white-space:nowrap;">
      <html:link page="/console/home/browse${resource.path}">[browse]</html:link>
        <c:if test="${resource.class.name != 'org.osaf.cosmo.model.HomeCollectionResource'}"><html:link page="/console/home/remove${resource.path}">[remove]</html:link></c:if>
      </td>
      <td class="smTableData">
        ${resource.displayName}
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${resource.class.name == 'org.osaf.cosmo.model.HomeCollectionResource'}">Home</c:when><c:when test="${resource.class.name == 'org.osaf.cosmo.model.CollectionResource'}">Folder</c:when><c:when test="${resource.class.name == 'org.osaf.cosmo.model.CalendarCollectionResource'}">Calendar</c:when><c:when test="${resource.class.name == 'org.osaf.cosmo.model.EventResource'}">Event</c:when><c:otherwise>File</c:otherwise></c:choose>
      </td>
      <td class="smTableData" style="text-align:center;">         
        <fmt:formatDate value="${resource.dateCreated}" type="both"/>
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${resource.class.name == 'org.osaf.cosmo.model.FileResource' || resource.class.name == 'org.osaf.cosmo.model.EventResource'}"><fmt:formatDate value="${resource.dateModified}" type="both"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${resource.class.name == 'org.osaf.cosmo.model.FileResource' || resource.class.name == 'org.osaf.cosmo.model.EventResource'}"><fmt:formatNumber value="${resource.contentLength}"/> b</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    </c:forEach>
  </table>
</div>

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
    <c:forEach var="ticket" items="${Collection.tickets}">
    <tr>
      <td class="smTableData" style="text-align:center; white-space:nowrap;">
        <html:link page="/console/home${Collection.path}/ticket/${ticket.id}/revoke">
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

<div class="hd" style="margin-top: 12px;">
  Properties
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
    <c:forEach var="property" items="${Collection.properties}">
    <tr>
      <td class="smTableData" width="50%">
        ${property.name}
      </td>
      <td class="smTableData" width="50%">
        ${property.value}
      </td>
    </tr>
    </c:forEach>
  </table>
</div>
