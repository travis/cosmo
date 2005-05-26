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
    <b><fmt:message key="Account.View.ClientSetup.Header"/></b>
  </p>
  <p>
    <fmt:message key="Account.View.ClientSetup.Info"/>
  </p>
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="Account.View.ClientSetup.Server"/></b>
      </td>
      <td class="md" align="left">
        ${pageContext.request.serverName}
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="Account.View.ClientSetup.Path"/></b>
      </td>
      <td class="md" align="left">
        ${homedir}
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="Account.View.ClientSetup.Username"/></b>
      </td>
      <td class="md" align="left">
        ${user.username}
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="Account.View.ClientSetup.Password"/></b>
      </td>
      <td class="md" align="left">
        <i><fmt:message key="Account.View.ClientSetup.PasswordHidden"/></i>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="Account.View.ClientSetup.Port"/></b>
      </td>
      <td class="md" align="left">
        ${pageContext.request.serverPort}
      </td>
      </td>
    </tr>
    <tr>
      <td class="md" align="right">
        <b><fmt:message key="Account.View.ClientSetup.Secure"/></b>
      </td>
      <td class="md" align="left">
        <c:choose><c:when test="${pageContext.request.secure}"><fmt:message key="Yes"/></c:when><c:otherwise><fmt:message key="No"/></c:otherwise></c:choose>
      </td>
    </tr>
  </table>

  <p class="hd">
    <b><fmt:message key="Account.View.HomeDirectory.Header"/></b>
  </p>
  <p>
    <fmt:message key="Account.View.HomeDirectory.YourHomeDirectoryIs"/>
    <html:link page="${homedir}">
      <b>${baseurl}${homedir}</b>
    </html:link>.
  </p>

  <p class="hd">
    <b><fmt:message key="Account.View.AccountDetails.Header"/></b>
  </p>
  <html:form action="/account/update">
    <table cellpadding="3" cellspacing="1" border="0">
      <tr>
        <td class="md" align="right">
          <b><fmt:message key="Account.Form.Username"/></b>
        </td>
        <td class="md" align="left">
          <html:text property="username" size="32" maxlength="32"
                     styleClass="md"/>
          <cosmo:errmsg property="username"/>
        </td>
      </tr>
      <tr>
        <td class="md" align="right">
          <b><fmt:message key="Account.Form.Email"/></b>
        </td>
        <td class="md" align="left">
          <html:text property="email" size="32" maxlength="32"
                     styleClass="md"/>
          <cosmo:errmsg property="email"/>
        </td>
      </tr>
      <tr>
        <td class="md" align="right">
          &nbsp;
        </td>
        <td class="md" align="left">
          <span class="sm"><fmt:message key="Account.Form.PasswordBlurb"/></span>
        </td>
      </tr>
      <tr>
        <td class="md" align="right">
          <b><fmt:message key="Account.Form.Password"/></b>
        </td>
        <td class="md" align="left">
          <html:password property="password" size="16" maxlength="16"
                         styleClass="md"/>
          <cosmo:errmsg property="password"/>
        </td>
      </tr>
      <tr>
        <td class="md" align="right">
          <b><fmt:message key="Account.Form.Confirm"/></b>
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
            <fmt:message key="Account.Form.Button.Update"/>
          </html:submit>
        </td>
      </tr>
    </table>
  </html:form>
</authz:authorize>
