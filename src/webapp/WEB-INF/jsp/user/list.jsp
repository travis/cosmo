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

<u:bind var="USER_ROOT" field="USER_ROOT"
        type="org.osaf.cosmo.security.CosmoSecurityManager"/>

<cosmo:cnfmsg/>

<c:choose>
  <c:when test="${not empty Users}">
    <table cellpadding="3" cellspacing="1" border="0" width="100%">
      <tr>
        <td class="md" valign="bottom">
          <b><fmt:message key="User.List.TH.FullName"/></b>
        </td>
        <td class="md" valign="bottom">
          <b><fmt:message key="User.List.TH.Username"/></b>
        </td>
        <td class="md" valign="bottom">
          <b><fmt:message key="User.List.TH.Homedir"/></b>
        </td>
        <td class="md" valign="bottom">
          <b><fmt:message key="User.List.TH.Email"/></b>
        </td>
        <td class="md" valign="bottom">
          <b><fmt:message key="User.List.TH.DateCreated"/></b>
        </td>
        <td class="md" valign="bottom">
          <b><fmt:message key="User.List.TH.DateLastModified"/></b>
        </td>
      </tr>
      <c:forEach var="user" items="${Users}">
        <cosmo:homedir var="homedir" user="${user}"/>
        <cosmo:fullName var="fullName" user="${user}"/>
        <tr>
          <td class="md" nowrap="nowrap">
            ${fullName}
          </td>
          <td class="md" nowrap="nowrap">
            <html:link page="/user/${user.username}">
              ${user.username}
            </html:link>
          </td>
          <td class="md" nowrap="nowrap">
            <c:choose>
              <c:when test="${user.username ne USER_ROOT}">
                <html:link target="homedir" page="${homedir}">
                  ${homedir}
                </html:link>
              </c:when>
            </c:choose>
          </td>
          <td class="md" nowrap="nowrap">
            <html:link href="mailto:${user.email}">${user.email}</html:link>
          </td>
          <td class="md" nowrap="nowrap">
            <fmt:formatDate value="${user.dateCreated}" type="both"/>
          </td>
          <td class="md" nowrap="nowrap">
            <fmt:formatDate value="${user.dateModified}" type="both"/>
          </td>
        </tr>
      </c:forEach>
    </table>
  </c:when>
  <c:otherwise>
    <div class="md">
      <i><fmt:message key="User.List.NoUsers"/></i>
      </div>
  </c:otherwise>
</c:choose>

<p class="hd">
  <b><fmt:message key="User.List.NewUser"/></b>
</p>

<cosmo:errmsg/>

<html:form action="/user/create">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.Form.Username"/></b>
      </td>
      <td class="md" align="left">
        <html:text property="username" size="32" maxlength="32"
                   styleClass="md"/>
        <cosmo:errmsg property="username"/>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.Form.FirstName"/></b>
      </td>
      <td class="md" align="left">
        <html:text property="firstName" size="32" maxlength="32"
                   styleClass="md"/>
        <cosmo:errmsg property="firstName"/>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="User.Form.LastName"/></b>
      </td>
      <td class="md" align="left">
        <html:text property="lastName" size="32" maxlength="32"
                   styleClass="md"/>
        <cosmo:errmsg property="lastName"/>
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
        <html:submit property="create" styleClass="md">
          <fmt:message key="User.Form.Button.Create"/>
        </html:submit>
      </td>
    </tr>
  </table>
</html:form>
