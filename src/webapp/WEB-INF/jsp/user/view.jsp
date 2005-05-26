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

<p class="hd">
  <b>${User.username}</b>
</p>

<html:form action="/user/update">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.View.DateCreated"/></b>
      </td>
      <td class="md" align="left">
        <fmt:formatDate value="${User.dateCreated}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.View.DateLastModified"/></b>
      </td>
      <td class="md" align="left">
        <fmt:formatDate value="${User.dateModified}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.Form.Email"/></b>
      </td>
      <td class="md" align="left">
        <html:text property="email" size="32" maxlength="32"
                   styleClass="md"/>
        <cosmo:errmsg property="email"/>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.Form.Roles"/></b>
      </td>
      <td class="md" align="left">
        <c:forEach var="role" items="${Roles}">
          <html:multibox property="role" value="${role.id}"
                         styleClass="md"/>
          ${role.name}<br/>
        </c:forEach>
        <cosmo:errmsg property="role"/>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        &nbsp;
      </td>
      <td class="md" align="left">
        <span class="sm"><fmt:message key="User.Form.PasswordBlurb"/></span>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.Form.Password"/></b>
      </td>
      <td class="md" align="left">
        <html:password property="password" size="16" maxlength="16"
                       styleClass="md"/>
        <cosmo:errmsg property="password"/>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.Form.Confirm"/></b>
      </td>
      <td class="md" align="left">
        <html:password property="confirm" size="16" maxlength="16"
                       styleClass="md"/>
        <cosmo:errmsg property="confirm"/>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        &nbsp;
      </td>
      <td class="md" align="left">
        <html:submit property="create" styleClass="md">
          <fmt:message key="User.Form.Button.Update"/>
        </html:submit>
        <html:cancel styleClass="md">
          <fmt:message key="User.Form.Button.Cancel"/>
        </html:cancel>
        &nbsp;&nbsp;&nbsp;
        <html:link page="/user/remove.do?id=${User.id}">
          <fmt:message key="User.Form.Button.Remove"/>
        </html:link>
      </td>
    </tr>
  </table>
  <html:hidden property="id"/>
</html:form>
