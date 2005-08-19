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

<p>
  <fmt:message key="Welcome.WelcomeMsg">
    <fmt:param value="${pageContext.request.serverName}"/>
  </fmt:message>
</p>

<authz:authorize ifAllGranted="ROLE_USER">
  <cosmo-core:user var="user"/>
  <cosmo:homedir var="homedir" user="${user}"/>
  <cosmo:baseurl var="baseurl"/>

  <p class="hd">
    <fmt:message key="Account.View.ClientSetup.Header"/>
  </p>
  <p>
    <fmt:message key="Account.View.ClientSetup.Info"/>
  </p>
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.View.ClientSetup.Server"/>
      </td>
      <td class="mdData">
        ${pageContext.request.serverName}
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.View.ClientSetup.Path"/>
      </td>
      <td class="mdData">
        ${homedir}
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.View.ClientSetup.Username"/>
      </td>
      <td class="mdData">
        ${user.username}
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.View.ClientSetup.Password"/>
      </td>
      <td class="mdData">
        <i><fmt:message key="Account.View.ClientSetup.PasswordHidden"/></i>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.View.ClientSetup.Port"/>
      </td>
      <td class="mdData">
        ${pageContext.request.serverPort}
      </td>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.View.ClientSetup.Secure"/>
      </td>
      <td class="mdData">
        <c:choose><c:when test="${pageContext.request.secure}"><fmt:message key="Yes"/></c:when><c:otherwise><fmt:message key="No"/></c:otherwise></c:choose>
      </td>
    </tr>
  </table>
  <p class="hd">
    <fmt:message key="Account.View.HomeDirectory.Header"/>
  </p>
  <p>
    <fmt:message key="Account.View.HomeDirectory.YourHomeDirectoryIs"/>
    <html:link page="${homedir}">
      ${baseurl}${homedir}
    </html:link>.
  </p>
  <p class="hd">
    <fmt:message key="Account.View.AccountDetails.Header"/>
  </p>
  <cosmo:errmsg/>
  <html:form action="/account/update">
    <table cellpadding="3" cellspacing="1" border="0">
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.FirstName"/>
        </td>
        <td class="mdData">
          <html:text property="firstName" size="32" maxlength="32"
                     styleClass="textInput"/>
          <cosmo:errmsg property="firstName"/>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.LastName"/>
        </td>
        <td class="mdData">
          <html:text property="lastName" size="32" maxlength="32"
                     styleClass="textInput"/>
          <cosmo:errmsg property="lastName"/>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.Email"/>
        </td>
        <td class="mdData">
          <html:text property="email" size="32" maxlength="32"
                     styleClass="textInput"/>
          <cosmo:errmsg property="email"/>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          &nbsp;
        </td>
        <td class="mdData">
          <span class="sm"><fmt:message key="Account.Form.PasswordBlurb"/></span>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.Password"/>
        </td>
        <td class="mdData">
          <html:password property="password" size="16" maxlength="16"
                         styleClass="textInput"/>
          <cosmo:errmsg property="password"/>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.Confirm"/>
        </td>
        <td class="mdData">
          <html:password property="confirm" size="16" maxlength="16"
                         styleClass="textInput"/>
          <cosmo:errmsg property="confirm"/>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          &nbsp;
        </td>
        <td class="mdData">
          <html:submit property="create" styleClass="buttonInput">
            <fmt:message key="Account.Form.Button.Update"/>
          </html:submit>
        </td>
      </tr>
    </table>
  </html:form>
</authz:authorize>
