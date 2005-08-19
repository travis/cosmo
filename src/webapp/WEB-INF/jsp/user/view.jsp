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

<u:bind var="USER_ROOT" field="USER_ROOT" type="org.osaf.cosmo.security.CosmoSecurityManager"/>

<p class="hd">
  <b>${User.username}</b>
</p>

<html:form action="/user/update">

  <div style="width:400px;">
  <div style="padding:12px;">
  
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <b><fmt:message key="User.View.DateCreated" /></b>
      </td>
      <td class="mdData">
        <fmt:formatDate value="${User.dateCreated}" type="both" />
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <b><fmt:message key="User.View.DateLastModified" /></b>
      </td>
      <td class="mdData">
        <fmt:formatDate value="${User.dateModified}" type="both" />
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <b><fmt:message key="User.Form.Username" /></b>
      </td>
      <td class="mdData">
        <html:text property="username" size="32" maxlength="32" styleClass="textInput" />
        <cosmo:errmsg property="username" />
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <b><fmt:message key="User.Form.FirstName" /></b>
      </td>
      <td class="mdData">
        <html:text property="firstName" size="32" maxlength="32" styleClass="textInput" />
        <cosmo:errmsg property="firstName" />
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <b><fmt:message key="User.Form.LastName" /></b>
      </td>
      <td class="mdData">
        <html:text property="lastName" size="32" maxlength="32" styleClass="textInput" />
        <cosmo:errmsg property="lastName" />
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <b><fmt:message key="User.Form.Email" /></b>
      </td>
      <td class="mdData">
        <html:text property="email" size="32" maxlength="32" styleClass="textInput" />
        <cosmo:errmsg property="email" />
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right; vertical-align:top;">
        <b><fmt:message key="User.Form.Roles" /></b>
      </td>
      <td class="mdData">
      
      <c:choose>
        <c:when test="${User.username eq USER_ROOT}">
          <div><input type="checkbox" name="roleDummy" value="" checked="checked" disabled="disabled" /> Make this user an administrator</div>
          <input type="hidden" name="role" value="1" />
        </c:when>
        <c:otherwise>
          <c:forEach var="role" items="${Roles}">
            <c:if test="${role.id == 1}">
            <div><html:multibox property="role" value="${role.id}" /> Make this user an administrator</div>
            </c:if>
          </c:forEach>
          <input type="hidden" name="role" value="2" />
        </c:otherwise>
      </c:choose>
        
        <cosmo:errmsg property="role" />
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        &nbsp;
      </td>
      <td class="mdData">
        <span class="sm"><fmt:message key="User.Form.PasswordBlurb" /></span>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <b><fmt:message key="User.Form.Password" /></b>
      </td>
      <td class="mdData">
        <html:password property="password" size="16" maxlength="16" styleClass="textInput" />
        <cosmo:errmsg property="password" />
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <b><fmt:message key="User.Form.Confirm" /></b>
      </td>
      <td class="mdData">
        <html:password property="confirm" size="16" maxlength="16" styleClass="textInput" />
        <cosmo:errmsg property="confirm" />
      </td>
    </tr>
  </table>
  
  <div style="margin-top:12px;">
     <div style="float:left;">
         <html:cancel styleClass="buttonInput">
          <fmt:message key="User.Form.Button.Cancel" />
        </html:cancel>
      </div>
      <div style="float:right;">
        <html:submit property="create" styleClass="buttonInput">
          <fmt:message key="User.Form.Button.Update" />
        </html:submit>
      </div>
      <br style="clear:both;" />
    </div>
    
  </div>
  </div>
  
  <html:hidden property="id" />
</html:form>
