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

<script language="JavaScript">

function goToPage(pageOffset){
   
   var pageNumberElement = getUserPageNumberElement();
   var currentPage = parseInt(pageNumberElement.value);
   pageNumberElement.value = currentPage + pageOffset; 
   getPagedListForm().submit();
}

function getPagedListForm(){
	return document.forms["pagedListForm"];
}

function getPagedListFormElement(elementName){
    var form = getPagedListForm();
    return form.elements[elementName];
}

function getUserPageNumberElement(){
    return getPagedListFormElement("pageCriteria.pageNumber");
}
</script>

<cosmo:cnfmsg/>

<c:choose>
  <c:when test="${not empty Users}">
	<html:form method="GET" action="/users.do">
      <html:hidden property="pageCriteria.sortTypeString"/>
      <html:hidden property="pageCriteria.sortAscending"/>
      <div style="margin-top:12px;">
      <table border="0" cellpadding="0" cellspacing="0" width="100%">
        <tr>
          <td align="left" valign="top">
              <fmt:message key="User.List.PageSize"/>
              <html:select property="pageCriteria.pageSize" onchange="goToPage(0);">
                <html:option value="10">10</html:option>
                <html:option value="25">25</html:option>
                <html:option value="50">50</html:option>
                <html:option value="-1">All</html:option>
              </html:select>
          </td>
        
          <td align="right" valign="top">
            <html:link href="javascript: goToPage(-${CurrentPage - 1});">&lt;&lt;</html:link>  
  		    <html:link href="javascript: goToPage(-1);">&lt;</html:link>
  		    <fmt:message key="User.List.Page"/>
  		    <html:select property="pageCriteria.pageNumber" onchange="goToPage(0);">
    		  <c:forEach var="i" begin="1" end="${NumPages}">
      		    <html:option value="${i}">${i}</html:option>
              </c:forEach>
  		    </html:select>
  	 	    <fmt:message key="User.List.Of"/>
		    ${NumPages}
		    <html:link href="javascript: goToPage(1);">&gt;</html:link>
		    <html:link href="javascript: goToPage(${NumPages - CurrentPage});">&gt;&gt;</html:link>
          </td>

        
        </tr>
      </table>
      </div>


  </html:form>

    <div style="margin-top:12px;">
    <table cellpadding="4" cellspacing="1" border="0" width="100%">
      <tr>
        <td class="smTableColHead" style="width:1%;">
          &nbsp;
        </td>
        <td class="smTableColHead">
          <c:choose>
            <c:when test='${pagedListForm.pageCriteria.sortTypeString == "Name"}'>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Name&pageCriteria.sortAscending=${!pagedListForm.pageCriteria.sortAscending}&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.FullName"/></html:link> 
              <c:choose>
                <c:when test='${pagedListForm.pageCriteria.sortAscending == "true"}'>
   	               &nbsp;&nbsp;&#8595;
   	   		    </c:when>
   	            <c:otherwise>
   	               &nbsp;&nbsp;&#8593;
   	            </c:otherwise>
   	          </c:choose>
            </c:when>
            <c:otherwise>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Name&pageCriteria.sortAscending=true&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.FullName"/></html:link> 
            </c:otherwise>
          </c:choose>
        </td>
        <td class="smTableColHead">
          <c:choose>
            <c:when test='${pagedListForm.pageCriteria.sortTypeString == "Username"}'>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Username&pageCriteria.sortAscending=${!pagedListForm.pageCriteria.sortAscending}&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.Username"/></html:link> 
              <c:choose>
                <c:when test='${pagedListForm.pageCriteria.sortAscending == "true"}'>
   	               &nbsp;&nbsp;&#8595;
   	   		    </c:when>
   	            <c:otherwise>
   	               &nbsp;&nbsp;&#8593;
   	            </c:otherwise>
   	          </c:choose>
            </c:when>
            <c:otherwise>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Username&pageCriteria.sortAscending=true&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.Username"/></html:link> 
            </c:otherwise>
          </c:choose>
        </td>
        <td class="smTableColHead">
          <c:choose>
            <c:when test='${pagedListForm.pageCriteria.sortTypeString == "Administrator"}'>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Administrator&pageCriteria.sortAscending=${!pagedListForm.pageCriteria.sortAscending}&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.IsAdmin"/></html:link> 
              <c:choose>
                <c:when test='${pagedListForm.pageCriteria.sortAscending == "true"}'>
   	               &nbsp;&nbsp;&#8595;
   	   		    </c:when>
   	            <c:otherwise>
   	               &nbsp;&nbsp;&#8593;
   	            </c:otherwise>
   	          </c:choose>
            </c:when>
            <c:otherwise>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Administrator&pageCriteria.sortAscending=true&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.IsAdmin"/></html:link> 
            </c:otherwise>
          </c:choose>
        </td>
        <td class="smTableColHead"">
          <c:choose>
            <c:when test='${pagedListForm.pageCriteria.sortTypeString == "Email"}'>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Email&pageCriteria.sortAscending=${!pagedListForm.pageCriteria.sortAscending}&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.Email"/></html:link> 
              <c:choose>
                <c:when test='${pagedListForm.pageCriteria.sortAscending == "true"}'>
   	               &nbsp;&nbsp;&#8595;
   	   		    </c:when>
   	            <c:otherwise>
   	               &nbsp;&nbsp;&#8593;
   	            </c:otherwise>
   	          </c:choose>
            </c:when>
            <c:otherwise>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Email&pageCriteria.sortAscending=true&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.Email"/></html:link> 
            </c:otherwise>
          </c:choose>
        </td>
        <td class="smTableColHead">
          <c:choose>
            <c:when test='${pagedListForm.pageCriteria.sortTypeString == "Created"}'>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Created&pageCriteria.sortAscending=${!pagedListForm.pageCriteria.sortAscending}&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.DateCreated"/></html:link> 
              <c:choose>
                <c:when test='${pagedListForm.pageCriteria.sortAscending == "true"}'>
   	               &nbsp;&nbsp;&#8595;
   	   		    </c:when>
   	            <c:otherwise>
   	               &nbsp;&nbsp;&#8593;
   	            </c:otherwise>
   	          </c:choose>
            </c:when>
            <c:otherwise>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Created&pageCriteria.sortAscending=true&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.DateCreated"/></html:link> 
            </c:otherwise>
          </c:choose>
        </td>
        <td class="smTableColHead">
          <c:choose>
            <c:when test='${pagedListForm.pageCriteria.sortTypeString == "Last Modified"}'>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Last%20Modified&pageCriteria.sortAscending=${!pagedListForm.pageCriteria.sortAscending}&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.DateLastModified"/></html:link> 
              <c:choose>
                <c:when test='${pagedListForm.pageCriteria.sortAscending == "true"}'>
   	               &nbsp;&nbsp;&#8595;
   	   		    </c:when>
   	            <c:otherwise>
   	               &nbsp;&nbsp;&#8593;
   	            </c:otherwise>
   	          </c:choose>
            </c:when>
            <c:otherwise>
              <html:link href="/cosmo/console/users?pageCriteria.sortTypeString=Last%20Modified&pageCriteria.sortAscending=true&pageCriteria.pageSize=${pagedListForm.pageCriteria.pageSize}&pageCriteria.pageNumber=1"><fmt:message key="User.List.TH.DateLastModified"/></html:link> 
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
      <c:forEach var="user" items="${Users}">
        <cosmo:fullName var="fullName" user="${user}"/>
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <html:link page="/console/user/${user.username}">
              <fmt:message key="User.List.EditControl"/>
            </html:link>
            <c:choose>
              <c:when test="${not user.overlord}">
                <html:link page="/console/home/browse/${user.username}">
                  <fmt:message key="User.List.HomeDirectoryControl"/>
                </html:link>
                <html:link page="/console/user/remove?username=${user.username}">
                  <fmt:message key="User.List.RemoveControl"/>
                </html:link>
              </c:when>
              <c:otherwise>
                <span class="disabled">
                  <fmt:message key="User.List.HomeDirectoryControl"/>
                  <fmt:message key="User.List.RemoveControl"/>
                </span>
              </c:otherwise>
            </c:choose> 
          </td>
          <td class="smTableData">
            ${fullName}
          </td>
          <td class="smTableData" style="text-align:center;">
            ${user.username}
          </td>
          <td class="smTableData" style="text-align:center;">
            <c:if test="${user.admin}">
              Yes
            </c:if>
          </td>
          <td class="smTableData" style="text-align:center;">
            <html:link href="mailto:${user.email}">${user.email}</html:link>
          </td>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <fmt:formatDate value="${user.dateCreated}" type="date" pattern="MM-dd-yyyy"/>
          </td>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <fmt:formatDate value="${user.dateModified}" type="date" pattern="MM-dd-yyyy"/>
          </td>
        </tr>
      </c:forEach>
    </table>
    </div>
  </c:when>
  <c:otherwise>
    <div class="md">
      <i><fmt:message key="User.List.NoUsers"/></i>
      </div>
  </c:otherwise>
</c:choose>

<div style="margin-top:12px;">
<html:link page="/console/user/new">
  Create New User
</html:link>
</div>



