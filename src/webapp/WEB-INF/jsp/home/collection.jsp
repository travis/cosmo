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

<div>
  <span class="hd" style="margin-top: 12px;">
    <fmt:message key="HomeDirectory.Collection.Title">
      <fmt:param value="${Collection.displayName}"/>
    </fmt:message>
  </span>
  - <span class="md">${Path}</span>
</div>

<div style="margin-top:12px;">
<c:if test="${Collection.class.name == 'org.osaf.cosmo.model.CalendarCollectionItem'}">
<html:link page="/console/home/download${Path}">
  [download as iCalendar]
</html:link>
<html:link page="/console/home/view${Path}">
  [view as HTML]
</html:link>
</c:if>
<html:link page="/feed/atom/1.0${Path}">
  [subscribe to feed]
</html:link>
</div>

<c:if test="${Path != '/'}">
<div style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        UID
      </td>
      <td class="mdData">
        ${Collection.uid}
      </td>
    </tr>
    <c:if test="${Collection.class.name == 'org.osaf.cosmo.model.CalendarCollectionItem'}">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Description
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Collection.description != null}">${Collection.description}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Language
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Collection.language != null}">${Collection.language}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="HomeDirectory.Collection.Attributes.SupportedCalendarItems"/>
      </td>
      <td class="mdData">
        <c:forEach var="type" items="${Collection.supportedComponents}">
          <fmt:message key="HomeDirectory.Collection.Attributes.SupportedCalendarItem.${type}"/>
        </c:forEach>
      </td>
    </tr>
    </c:if>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Created
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Collection.creationDate}" type="both"/>
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
    <c:forEach var="item" items="${Collection.children}">
    <tr>
      <td class="smTableData" style="text-align:center; white-space:nowrap;">
      <html:link page="/console/home/browse${Path}/${item.name}">[browse]</html:link>
        <c:if test="${item.parent != null}"><html:link page="/console/home/remove${Path}/${item.name}">[remove]</html:link></c:if>
      </td>
      <td class="smTableData">
        ${item.displayName}
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${item.parent == null}">Home</c:when><c:when test="${item.class.name == 'org.osaf.cosmo.model.CollectionItem'}">Folder</c:when><c:when test="${item.class.name == 'org.osaf.cosmo.model.CalendarCollectionItem'}">Calendar</c:when><c:when test="${item.class.name == 'org.osaf.cosmo.model.EventCalendarItem'}">Event</c:when><c:otherwise>File</c:otherwise></c:choose>
      </td>
      <td class="smTableData" style="text-align:center;">         
        <fmt:formatDate value="${item.creationDate}" type="both"/>
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${item.class.name == 'org.osaf.cosmo.model.ContentItem' || item.class.name == 'org.osaf.cosmo.model.EventCalendarItem'}"><fmt:formatDate value="${item.modifiedDate}" type="both"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${item.class.name == 'org.osaf.cosmo.model.ContentItem' || item.class.name == 'org.osaf.cosmo.model.EventCalendarItem'}"><fmt:formatNumber value="${item.contentLength}"/> b</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    </c:forEach>
  </table>
</div>

<tiles:insert definition="home.inc.tickets">
  <tiles:put name="item" beanName="Collection"/>
  <tiles:put name="path" beanName="Path"/>
  <tiles:put name="isCollection" value="true"/>
</tiles:insert>

<tiles:insert definition="home.inc.properties">
  <tiles:put name="item" beanName="Collection"/>
  <tiles:put name="path" beanName="Path"/>
</tiles:insert>
