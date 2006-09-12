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

<div class="hd" style="margin-top: 12px;">
  <fmt:message key="HomeDirectory.Item.Title">
    <fmt:param value="${Path}"/>
  </fmt:message>
</div>

<div style="margin-top:12px;">
<c:choose>
<c:when test="${Item.class.name == 'org.osaf.cosmo.model.CalendarEventItem'}">
<html:link page="/console/home/download${Path}">
  [download as iCalendar]
</html:link>
<html:link page="/console/home/view${Path}">
  [view as HTML]
</html:link>
</c:when>
<c:otherwise>
<html:link page="/console/home/download${Path}">
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
        ${Item.name}
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
        <c:choose><c:when test="${Item.contentEncoding}">${Item.contentEncoding}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Language
      </td>
      <td class="mdData">
        <c:choose><c:when test="${Item.contentLanguage}">${Item.contentLanguage}</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
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


<tiles:insert definition="home.inc.tickets">
  <tiles:put name="item" beanName="Item"/>
  <tiles:put name="path" beanName="Path"/>
</tiles:insert>

<tiles:insert definition="home.inc.properties">
  <tiles:put name="item" beanName="Item"/>
  <tiles:put name="path" beanName="Path"/>
</tiles:insert>
