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

<cosmo:cnfmsg/>

<c:choose>
  <c:when test="${not empty Users}">
    <div style="margin-top:24px;">
    <table cellpadding="4" cellspacing="1" border="0" width="100%">
      <tr>
        <td class="smTableColHead" style="width:1%;">
          &nbsp;
        </td>
        <td class="smTableColHead">
          <fmt:message key="User.List.TH.FullName"/>
        </td>
        <td class="smTableColHead">
          <fmt:message key="User.List.TH.Username"/>
        </td>
        <td class="smTableColHead">
          <fmt:message key="User.List.TH.IsAdmin"/>
        </td>
        <td class="smTableColHead">
          <fmt:message key="User.List.TH.Homedir"/>
        </td>
        <td class="smTableColHead">
          <fmt:message key="User.List.TH.Email"/>
        </td>
        <td class="smTableColHead">
          <fmt:message key="User.List.TH.DateCreated"/>
        </td>
        <td class="smTableColHead">
          <fmt:message key="User.List.TH.DateLastModified"/>
        </td>
      </tr>
      <c:forEach var="user" items="${Users}">
        <cosmo:homedir var="homedir" user="${user}"/>
        <cosmo:fullName var="fullName" user="${user}"/>
        <tr>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <html:link page="/user/${user.username}">
              <fmt:message key="User.List.EditControl"/>
            </html:link>
            <c:choose>
              <c:when test="${not user.overlord}">
                <html:link page="/user/remove.do?username=${user.username}">
                  <fmt:message key="User.List.RemoveControl"/>
                </html:link>
              </c:when>
              <c:otherwise>
                <span class="disabled">
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
          <td class="smTableData">
            <c:choose>
              <c:when test="${not user.overlord}">
                <html:link target="homedir" page="${homedir}">
                  ${homedir}
                </html:link>
              </c:when>
              <c:otherwise>
              <span class="disabled">
                <fmt:message key="User.List.NotApplicable"/>
              </span>
              </c:otherwise>
            </c:choose>
          </td>
          <td class="smTableData">
            <html:link href="mailto:${user.email}">${user.email}</html:link>
          </td>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <fmt:formatDate value="${user.dateCreated}" type="both" pattern="MM-dd-yyyy"/>
          </td>
          <td class="smTableData" style="text-align:center; white-space:nowrap;">
            <fmt:formatDate value="${user.dateModified}" type="both" pattern="MM-dd-yyyy"/>
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

<div class="widgetBorder" style="width:460px; margin-top:24px;">
<div class="widgetContent" style="padding:8px;">

<cosmo:errmsg/>

<html:form action="/user/create">

<div class="hd" style="margin-bottom:4px;"><fmt:message key="User.List.NewUser"/></div>

  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Username"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="username"/></div>
        <div><html:text property="username" size="32" maxlength="32" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.FirstName"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="firstName"/></div>
        <div><html:text property="firstName" size="32" maxlength="128" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.LastName"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="lastName"/></div>
        <div><html:text property="lastName" size="32" maxlength="128" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Email"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="email"/></div>
        <div><html:text property="email" size="32" maxlength="128" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Password"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="password"/></div>
        <div><html:password property="password" size="16" maxlength="16" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Confirm"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="confirm"/></div>
        <div><html:password property="confirm" size="16" maxlength="16" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right; vertical-align:top;">&nbsp;</td>
      <td>
        <div style="margin-top:8px;">
          <html:checkbox property="admin" value="true"/>
          <fmt:message key="User.Form.MakeAdministrator"/>
        </div>
      </td>
    </tr>
  </table>
 
  <div style="margin-top:12px; text-align:right;">
    <html:submit property="create" styleClass="md">
      <fmt:message key="User.Form.Button.Create"/>
    </html:submit>
  </div> 
 
</html:form> 
 
</div> 
</div>

