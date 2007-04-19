<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
/*
 * Copyright 2005-2007 Open Source Applications Foundation
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

<c:set var="eventstamp" value="${Item.stampMap['event']}" />
<c:set var="taskstamp" value="${Item.stampMap['task']}" />
<c:set var="messagestamp" value="${Item.stampMap['message']}" />

<c:if test="${eventstamp != null}">
  <c:url var="webcalUrl" value="/browse/download/item/${Item.uid}/${Item.displayName}.ics" />
</c:if>
<c:url var="downloadUrl" value="/browse/download${Path}" />

<cosmo:standardLayout prefix="HomeDirectory.Item.">
<div>
  <span class="hd" style="margin-top: 12px;">
    <fmt:message key="HomeDirectory.Item.Title">
      <fmt:param value="${Item.displayName}"/>
    </fmt:message>
  </span>
  - <span class="md">${Path}</span>
</div>

<c:if test="${Item.class.name == 'org.osaf.cosmo.model.ContentItem' || eventstamp!=null}">
<div style="margin-top:12px;">
<c:choose>
<c:when test="${eventstamp != null}">
<a href="${webcalUrl}">[download as iCalendar]</a>
</c:when>
<c:otherwise>
<a href="${downloadUrl}">[download]</a>
</c:otherwise>
</c:choose>
</div>
</c:if>

<div class="hd" style="margin-top: 12px;">
  Item Properties
</div>

<div style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        UUID
      </td>
      <td class="mdData">
        ${Item.uid}
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Triage Status
      </td>
      <td class="mdData">
        <cosmo:triagestatus property="code" value="${Item.triageStatus}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Triage Rank
      </td>
      <td class="mdData">
        <cosmo:triagestatus property="rank" value="${Item.triageStatus}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Auto Triage?
      </td>
      <td class="mdData">
        <cosmo:triagestatus property="auto" value="${Item.triageStatus}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Sent?
      </td>
      <td class="mdData">
        <cosmo:yesno value="${Item.sent}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Needs Reply?
      </td>
      <td class="mdData">
        <cosmo:yesno value="${Item.needsReply}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Last Modified By:
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Item.lastModifiedBy != null}">${Item.lastModifiedBy}</c:when><c:otherwise><span class="disabled">(anonymous)</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Created on Client:
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Item.clientCreationDate}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Last Modified on Client:
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Item.clientModifiedDate}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Created on Server:
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Item.creationDate}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Last Modified on Server:
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Item.modifiedDate}" type="both"/>
      </td>
    </tr>
  </table>
</div>

<div class="hd" style="margin-top: 12px;">
  Content Properties
</div>

<div style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Size
      </td>
      <td class="mdData">
        <fmt:formatNumber value="${Item.contentLength}"/> b
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Media Type
      </td>
      <td class="mdData">
        ${Item.contentType}
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Encoding
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Item.contentEncoding != null}">${Item.contentEncoding}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Language
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Item.contentLanguage != null}">${Item.contentLanguage}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
  </table>
</div>

<c:if test="${eventstamp != null}">
<div class="hd" style="margin-top: 12px;">
  Event Properties
</div>

<div style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        UID
      </td>
      <td class="mdData">
        ${eventstamp.icalUid}
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Summary
      </td>
      <td class="mdData">
        <c:choose><c:when test="${eventstamp.summary != null}">${eventstamp.summary}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Description
      </td>
      <td class="mdData">
        <c:choose><c:when test="${eventstamp.description != null}">${eventstamp.description}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Starts At
      </td>
      <td class="mdData">
        <c:choose><c:when test="${eventstamp.startDate != null}"><fmt:formatDate value="${eventstamp.startDate}" type="both"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Ends At
      </td>
      <td class="mdData">
        <c:choose><c:when test="${eventstamp.endDate != null}"><fmt:formatDate value="${eventstamp.endDate}" type="both"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Anytime?
      </td>
      <td class="mdData">
        <cosmo:yesno value="${eventstamp.anyTime}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Location
      </td>
      <td class="mdData">
        <c:choose><c:when test="${not empty eventstamp.location}">${eventstamp.location}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Status
      </td>
      <td class="mdData">
        <c:choose><c:when test="${not empty eventstamp.status}">${eventstamp.status}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
  </table>
</div>
</c:if>

<c:set var="item" value="${Collection}" scope="request"/>
<c:set var="path" value="${Path}" scope="request"/>

<jsp:include page="inc-tickets.jsp" />

<jsp:include page="inc-attributes.jsp" />

<c:if test="${eventstamp != null}">
<div class="hd" style="margin-top: 12px;">
  Original iCalendar
</div>

<pre>

${eventstamp.calendar}
</pre>

<jsp:include page="inc-indexes.jsp" />

</c:if>

</cosmo:standardLayout>
