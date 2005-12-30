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
  <fmt:message key="HomeDirectory.Resource.Title">
    <fmt:param value="${Resource.path}"/>
  </fmt:message>
</div>

<div style="margin-top:12px;">
<c:choose>
<c:when test="${Resource.class.name == 'org.osaf.cosmo.model.EventResource'}">
<html:link page="/console/home/download${Resource.path}">
  [download as iCalendar]
</html:link>
<html:link page="/console/home/view${Resource.path}">
  [view as HTML]
</html:link>
</c:when>
<c:otherwise>
<html:link page="/console/home/download${Resource.path}">
  [download]
</html:link>
</c:otherwise>
</c:choose>
</div>

<div style="margin-top:12px;">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Display Name
      </td>
      <td class="mdData">
        ${Resource.displayName}
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Size
      </td>
      <td class="mdData">
        <fmt:formatNumber value="${Resource.contentLength}"/> b
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Media Type
      </td>
      <td class="mdData">
        ${Resource.contentType}
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Encoding
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Resource.contentEncoding}">${Resource.contentEncoding}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Language
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Resource.contentLanguage}">${Resource.contentLanguage}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Created
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Resource.dateCreated}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Last Modified
      </td>
      <td class="mdData">
        <fmt:formatDate value="${Resource.dateModified}" type="both"/>
      </td>
    </tr>
  </table>
</div>


<tiles:insert definition="home.inc.tickets">
  <tiles:put name="resource" beanName="Resource"/>
</tiles:insert>

<tiles:insert definition="home.inc.properties">
  <tiles:put name="resource" beanName="Resource"/>
</tiles:insert>
