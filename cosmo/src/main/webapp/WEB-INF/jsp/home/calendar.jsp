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

<cosmo:standardLayout prefix="HomeDirectory.Collection." contentWrapperClass="fullPageWidthContent" stylesheets="account_browser">
<div>
  <span class="hd" style="margin-top: 12px;">
    <fmt:message key="HomeDirectory.Calendar.Title">
      <fmt:param><c:out value="${Calendar.item.displayName}"/></fmt:param>
    </fmt:message>
  </span>
  - <span class="md"><c:out value="${Path}"/></span>
</div>

<div style="margin-top:12px;">
  <table cellpadding="4" cellspacing="1" border="0" width="100%" class="vcalendar">
    <tr>
      <td class="smTableColHead" style="width:1%;">
        &nbsp;
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Summary
      </td>
      <td class="smTableColHead">
        Location
      </td>
      <td class="smTableColHead">
        Starts At
      </td>
      <td class="smTableColHead">
        Ends At
      </td>
    </tr>
    <c:forEach var="event" items="${Calendar.events}">
      <c:if test="${not empty event.timeZone}">
        <c:set var="tz" value="${event.timeZone.ID}"/>
      </c:if>
    <tr class="vevent">
      <td class="smTableData" style="text-align:center; white-space:nowrap;">
        <a href='<c:url value="/browse/view${Path}/${event.item.name}" />'>
          [view]
        </a>
      </td>
      <td class="smTableData">
        <c:choose><c:when test="${not empty event.summary}"><span class="summary"><c:out value="${event.summary}"/></span></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${not empty event.location}"><span class="location"><c:out value="${event.location}"/></span></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose> 
      </td>
      <td class="smTableData" style="text-align:center;">         
        <c:choose>
          <c:when test="${event.hasStartTime}">
            <c:set var="type" value="both"/>
            <c:set var="pattern" value="MMM d, yyyy h:mm a zzz"/>
          </c:when>
          <c:otherwise>
            <c:set var="type" value="date"/>
            <c:set var="pattern" value="MMM d, yyyy"/>
          </c:otherwise>
        </c:choose>
        <abbr class="dtstart" title="${event.dtStart}">
          <fmt:formatDate value="${event.start}"
                          type="${type}"
                          pattern="${pattern}"
                          timeZone="${tz}"/>
        </abbr>
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose>
          <c:when test="${not empty event.dtEnd}">
            <c:set var="class" value="dtend"/>
            <c:set var="title" value="${event.dtEnd}"/>
          </c:when>
          <c:when test="${not empty event.duration}">
            <c:set var="class" value="duration"/>
            <c:set var="title" value="${event.duration}"/>
          </c:when>
        </c:choose>
        <c:choose>
          <c:when test="${event.hasEndTime}">
            <c:set var="type" value="both"/>
            <c:set var="pattern" value="MMM d, yyyy h:mm a zzz"/>
          </c:when>
          <c:otherwise>
            <c:set var="type" value="date"/>
            <c:set var="pattern" value="MMM d, yyyy"/>
          </c:otherwise>
        </c:choose>
        <abbr class="${class}" title="${title}">
          <fmt:formatDate value="${event.end}"
                          type="${type}"
                          pattern="${pattern}"
                          timeZone="${tz}"/>
        </abbr>
      </td>
    </tr>
    </c:forEach>
  </table>
</div>

<p>
Note 1: Values may be in a different language or encoding than the rest
of the page (default UTF-8 US English).
</p>

<p>
Note 2: hCalendar spec doesn't seem to address including timezones for
<strong>dtstart</strong> and <strong>dtend</strong>.
</p>
</cosmo:standardLayout>