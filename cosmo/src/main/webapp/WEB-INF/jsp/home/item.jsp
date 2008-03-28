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

<cosmo:standardLayout prefix="HomeDirectory.Item." contentWrapperClass="fullPageWidthContent" stylesheets="account_browser">
<div>
  <span class="hd" style="margin-top: 12px;">
    <fmt:message key="HomeDirectory.Item.Title">
      <fmt:param><c:out value="${Item.displayName}"/></fmt:param>
    </fmt:message>
  </span>
  - <span class="md"><c:out value="${Path}"/></span>
</div>

<c:if test="${cosmoui:instanceOf('org.osaf.cosmo.model.FileItem', Item) || eventstamp!=null}">
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
        <c:out value="${Item.uid}"/>
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
        <c:choose><c:when test="${Item.lastModifiedBy != null}"><c:out value="${Item.lastModifiedBy}"/></c:when><c:otherwise><span class="disabled">(anonymous)</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Last Modification:
      </td>
      <td class="mdData">
        <cosmo:lastmodification value="${Item.lastModification}"/>
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
<c:if test="${cosmoui:instanceOf('org.osaf.cosmo.model.NoteItem', Item)}">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        iCalendar UID:
      </td>
      <td class="mdData">
        <cosmo:property value="${Item.icalUid}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Body:
      </td>
      <td class="mdData">
        <cosmo:property value="${Item.body}"/>
      </td>
    </tr>
</c:if>
  </table>
</div>

<c:if test="${cosmoui:instanceOf('org.osaf.cosmo.model.FileItem', Item)}">
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
        <c:out value="${Item.contentType}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Encoding
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Item.contentEncoding != null}"><c:out value="${Item.contentEncoding}"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Language
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Item.contentLanguage != null}"><c:out value="${Item.contentLanguage}"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
  </table>
</div>
</c:if>

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
        <c:out value="${eventstamp.icalUid}"/>
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
        <c:choose><c:when test="${not empty eventstamp.location}"><c:out value="${eventstamp.location}"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Status
      </td>
      <td class="mdData">
        <c:choose><c:when test="${not empty eventstamp.status}"><c:out value="${eventstamp.status}"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
  </table>
</div>
</c:if>

<c:if test="${messagestamp != null}">
<div class="hd" style="margin-top: 12px;">
  Message Properties
</div>

<div style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Message ID
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.messageId}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Headers
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.headers}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        From
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.from}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        To
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.to}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Cc
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.cc}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Bcc
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.bcc}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Originators
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.originators}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Date Sent
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.dateSent}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        In Reply To
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.inReplyTo}"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        References
      </td>
      <td class="mdData">
        <cosmo:property value="${messagestamp.references}"/>
      </td>
    </tr>
  </table>
</div>
</c:if>

<c:if test="${taskstamp != null}">
<div class="hd" style="margin-top: 12px;">
  Task Properties
</div>

<div style="margin-top: 12px;">
  Tasks have no properties.
</div>
</c:if>

<c:if test="${cosmoui:instanceOf('org.osaf.cosmo.model.NoteItem', Item) && fn:length(Item.modifications) > 0}">
<div class="hd" style="margin-top: 12px;">
  Modifications
</div>

<div style="margin-top:12px;">
  <table cellpadding="4" cellspacing="1" border="0" width="100%">
    <tr>
      <td class="smTableColHead" style="width:1%;">
        &nbsp;
      </td>
      <td class="smTableColHead" style="text-align:left;">
        Recurrence ID
      </td>
      <td class="smTableColHead">
        Created
      </td>
      <td class="smTableColHead">
        Last Modified
      </td>
    </tr>
    <c:forEach var="modification" items="${Item.modifications}">
      <c:set var="stamp" value="${modification.stampMap['eventexception']}"/>
      <c:url var="browseUrl" value="/browse/${Item.owner.username}/${Item.parent.name}/${modification.name}"/>
      <c:url var="removeUrl" value="/browse/${Item.owner.username}/${Item.parent.name}/${modification.name}"/>
    <tr>
      <td class="smTableData" style="text-align:center; white-space:nowrap;">
        <a href="${browseUrl}">[browse]</a><a href="${removeUrl}">[remove]</a>
      </td>
      <td class="smTableData">
        <fmt:formatDate value="${stamp.recurrenceId}" type="both"/>
      </td>
      <td class="smTableData" style="text-align:center;">         
        <fmt:formatDate value="${modification.creationDate}" type="both"/>
      </td>
      <td class="smTableData" style="text-align:center;">
        <fmt:formatDate value="${modification.modifiedDate}" type="both"/>
      </td>
    </tr>
    </c:forEach>
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

<c:out value="${eventstamp.calendar}"/>
</pre>

</c:if>

</cosmo:standardLayout>
