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

<div style="margin-top:12px;">
  <table cellpadding="4" cellspacing="1" border="0" width="100%">
    <tr>
      <td class="smTableColHead" style="width:1%;">
        &nbsp;
      </td>
      <td class="smTableColHead">
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
      <c:choose><c:when test="${resource.class.name == 'org.osaf.cosmo.model.CollectionResource'}"><c:set var="homeurl" value="/console/home/browse${resource.path}"/></c:when><c:otherwise><c:set var="homeurl" value="/console/home/download${resource.path}"/></c:otherwise></c:choose>
    <tr>
      <td class="smTableData" style="text-align:center; white-space:nowrap;">
        <html:link page="${homeurl}">
        [browse]
        </html:link>    
      </td>
      <td class="smTableData">
        ${resource.displayName}
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${resource.class.name == 'org.osaf.cosmo.model.CollectionResource'}">Folder</c:when><c:otherwise>File</c:otherwise></c:choose>
      </td>
      <td class="smTableData" style="text-align:center;">         
        <fmt:formatDate value="${resource.dateCreated}" type="both"/>
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${resource.class.name == 'org.osaf.cosmo.model.FileResource'}"><fmt:formatDate value="${resource.dateModified}" type="both"/></c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
      <td class="smTableData" style="text-align:center;">
        <c:choose><c:when test="${resource.class.name == 'org.osaf.cosmo.model.FileResource'}"><fmt:formatNumber value="${resource.contentLength}"/> b</c:when><c:otherwise><span class="disabled">-</span></c:otherwise></c:choose>
      </td>
    </tr>
    </c:forEach>
  </table>
</div>
