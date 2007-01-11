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

<cosmo:standardLayout prefix="HomeDirectory.Item.">
<div>
  <span class="hd" style="margin-top: 12px;">
    <fmt:message key="HomeDirectory.Item.Title">
      <fmt:param value="${Item.displayName}"/>
    </fmt:message>
  </span>
  - <span class="md">${Path}</span>

</div>

<div style="margin-top:12px;">
<c:choose>
<c:when test="${Item.stampMap['event'] != null}">
<a href='<c:url value="/browse/download${Path}" />'>
  [download as iCalendar]
</a>
<a href='<c:url value="/browse/view${Path}" />'>
  [view as HTML]
</a>
</c:when>
<c:otherwise>
<a href='<c:url value="/browse/download${Path}" />'>
  [download]
</a>
</c:otherwise>
</c:choose>
</div>

<div style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        UID
      </td>
      <td class="mdData">
        ${Item.uid}
      </td>
    </tr>
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
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Created
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Item.creationDate}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Last Modified
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Item.modifiedDate}" type="both"/>
      </td>
    </tr>
  </table>
</div>

<c:set var="item" value="${Collection}" scope="request"/>
<c:set var="path" value="${Path}" scope="request"/>

<jsp:include page="inc-tickets.jsp" />


<jsp:include page="inc-properties.jsp" />



<jsp:include page="inc-indexes.jsp" />


</cosmo:standardLayout>