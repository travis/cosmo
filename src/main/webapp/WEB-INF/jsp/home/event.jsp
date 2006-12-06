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

<cosmo:standardLayout prefix="HomeDirectory.Event.">
<div>
  <span class="hd" style="margin-top: 12px;">
    <fmt:message key="HomeDirectory.Event.Title">
      <fmt:param value="${Event.item.displayName}"/>
    </fmt:message>
  </span>
  - <span class="md">${Path}</span>

</div>

<c:if test="${not empty Event.timeZone}">
  <c:set var="tz" value="${Event.timeZone.ID}"/>
</c:if>

<div class="vevent" style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Summary
      </td>
      <td class="mdData">
        <c:choose><c:when test="${not empty Event.summary}"><span class="summary">${Event.summary}</span></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Location
      </td>
      <td class="mdData">
        <c:choose><c:when test="${not empty Event.location}"><span class="location">${Event.location}</span></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose> 
     </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Starts At
      </td>
      <td class="mdData">
        <c:choose>
          <c:when test="${Event.hasStartTime}">
            <c:set var="type" value="both"/>
            <c:set var="pattern" value="MMM d, yyyy h:mm a zzz"/>
          </c:when>
          <c:otherwise>
            <c:set var="type" value="date"/>
            <c:set var="pattern" value="MMM d, yyyy"/>
          </c:otherwise>
        </c:choose>
        <abbr class="dtstart" title="${Event.dtStart}">
          <fmt:formatDate value="${Event.start}"
                          type="${type}"
                          pattern="${pattern}"
                          timeZone="${tz}"/>
        </abbr>
      </td>
    </tr>
    <c:if test="${not empty Event.end}">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Ends At
      </td>
      <td class="mdData">
        <c:choose>
          <c:when test="${not empty Event.dtEnd}">
            <c:set var="class" value="dtend"/>
            <c:set var="title" value="${Event.dtEnd}"/>
          </c:when>
          <c:when test="${not empty Event.duration}">
            <c:set var="class" value="duration"/>
            <c:set var="title" value="${Event.duration}"/>
          </c:when>
        </c:choose>
        <c:choose>
          <c:when test="${Event.hasEndTime}">
            <c:set var="type" value="both"/>
            <c:set var="pattern" value="MMM d, yyyy h:mm a zzz"/>
          </c:when>
          <c:otherwise>
            <c:set var="type" value="date"/>
            <c:set var="pattern" value="MMM d, yyyy"/>
          </c:otherwise>
        </c:choose>
        <abbr class="${class}" title="${title}">
          <fmt:formatDate value="${Event.end}"
                          type="${type}"
                          pattern="${pattern}"
                          timeZone="${tz}"/>
        </abbr>
      </td>
    </tr>
    </c:if>
  </table>
</div>

<p>
Note 1: Values may be in a different language or encoding than the rest
of the page (default UTF-8 US English).
</p>

<p>
Note 2: Recurrence and alarms not supported; unclear how to display.
</p>

<p>
Note 3: hCalendar spec doesn't seem to address including timezones for
<strong>dtstart</strong> and <strong>dtend</strong>.
</p>

<div class="hd" style="margin-top: 12px;">
  Original iCalendar
</div>

<pre>
${Event}
</pre>
</cosmo:standardLayout>