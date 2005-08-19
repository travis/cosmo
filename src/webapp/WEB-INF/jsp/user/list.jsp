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

<cosmo:cnfmsg/>

<c:choose>
  <c:when test="${not empty Users}">
    <div style="margin-top:24px;">
    <table cellpadding="3" cellspacing="1" border="0" width="100%">
      <tr>
        <td class="smTableColHead" nowrap="nowrap">
          <fmt:message key="User.List.TH.FullName"/>
        </td>
        <td class="smTableColHead" nowrap="nowrap">
          <fmt:message key="User.List.TH.Username"/>
        </td>
        <td class="smTableColHead" nowrap="nowrap">
          <fmt:message key="User.List.TH.Homedir"/>
        </td>
        <td class="smTableColHead" nowrap="nowrap">
          <fmt:message key="User.List.TH.Email"/>
        </td>
        <td class="smTableColHead" nowrap="nowrap">
          <fmt:message key="User.List.TH.DateCreated"/>
        </td>
        <td class="smTableColHead" nowrap="nowrap">
          <fmt:message key="User.List.TH.DateLastModified"/>
        </td>
        <td class="smTableColHead" nowrap="nowrap">
          &nbsp;
        </td>
      </tr>
      <c:forEach var="user" items="${Users}">
        <cosmo:homedir var="homedir" user="${user}"/>
        <cosmo:fullName var="fullName" user="${user}"/>
        <tr>
          <td class="smTableData" nowrap="nowrap">
            ${fullName}
          </td>
          <td class="smTableData" nowrap="nowrap" style="text-align:center;">
            ${user.username}
          </td>
          <td class="smTableData" nowrap="nowrap">
            <c:choose>
              <c:when test="${user.username ne USER_ROOT}">
                <html:link target="homedir" page="${homedir}">
                  ${homedir}
                </html:link>
              </c:when>
              <c:otherwise>
              <span class="disabled">(N/A)</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td class="smTableData" nowrap="nowrap">
            <html:link href="mailto:${user.email}">${user.email}</html:link>
          </td>
          <td class="smTableData" nowrap="nowrap" style="text-align:center;">
            <fmt:formatDate value="${user.dateCreated}" type="both"/>
          </td>
          <td class="smTableData" nowrap="nowrap" style="text-align:center;">
            <fmt:formatDate value="${user.dateModified}" type="both"/>
          </td>
          <td class="smTableData" nowrap="nowrap" style="text-align:center;">
            <html:link page="/user/${user.username}">[edit]</html:link>
            <c:choose>
              <c:when test="${user.username ne USER_ROOT}">
                <html:link page="/user/remove.do?id=${user.id}">[remove]</html:link>
              </c:when>
              <c:otherwise>
                <span class="disabled">[remove]</span>
              </c:otherwise>
            </c:choose> 
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

<p class="hd">
  <fmt:message key="User.List.NewUser"/>
</p>

<cosmo:errmsg/>

<html:form action="/user/create">


  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Username"/>
      </td>
      <td class="mdData">
        <html:text property="username" size="32" maxlength="32" styleClass="textInput"/>
        <cosmo:errmsg property="username"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.FirstName"/>
      </td>
      <td class="mdData">
        <html:text property="firstName" size="32" maxlength="32" styleClass="textInput"/>
        <cosmo:errmsg property="firstName"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.LastName"/>
      </td>
      <td class="mdData">
        <html:text property="lastName" size="32" maxlength="32" styleClass="textInput"/>
        <cosmo:errmsg property="lastName"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Email"/>
      </td>
      <td class="mdData">
        <html:text property="email" size="32" maxlength="32" styleClass="textInput"/>
        <cosmo:errmsg property="email"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Password"/>
      </td>
      <td class="mdData">
        <html:password property="password" size="16" maxlength="16" styleClass="textInput"/>
        <cosmo:errmsg property="password"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Confirm"/>
      </td>
      <td class="mdData">
        <html:password property="confirm" size="16" maxlength="16" styleClass="textInput"/>
        <cosmo:errmsg property="confirm"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right; vertical-align:top;">
        <fmt:message key="User.Form.Roles"/>
      </td>
      <td class="mdData">
        <html:checkbox property="role" value="1"> Make this user an administrator</html:checkbox>
        <input type="hidden" name="role" value="2" />
        <cosmo:errmsg property="role"/>
      </td>
    </tr>
    <tr>
      <td colspan="2" style="text-align:right;">
        <div style="margin-top:12px;">
        <html:submit property="create" styleClass="md">
          <fmt:message key="User.Form.Button.Create"/>
        </html:submit>
        </div>
      </td>
    </tr>
  </table>
  
  
</html:form>
