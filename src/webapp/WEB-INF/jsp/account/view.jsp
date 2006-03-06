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

<div style="width:500px;">

<p>
  <fmt:message key="Account.View.Welcome">
    <fmt:param value="${pageContext.request.serverName}"/>
  </fmt:message>
</p>

<authz:authorize ifAllGranted="ROLE_USER">
  <cosmoui:user var="user"/>
  <cosmo:homedir var="homedir" user="${user}"/>
  <cosmo:baseurl var="baseurl"/>

  <p>
    <html:link page="/console/home/browse/${user.username}">
      <fmt:message key="Account.View.BrowseHomeDirectory"/>
    </html:link>
  </p>

  <p class="hd">
    <fmt:message key="Account.View.ClientSetup.Header"/>
  </p>
  <p>
    <fmt:message key="Account.View.ClientSetup.Info"/>
  </p>
  
  <div class="widgetBorder" style="width:460px;">
  
  <table cellpadding="3" cellspacing="0" border="0">
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
  
  </div>
  
<%-- bug 3920
  <p class="hd">
    <fmt:message key="Account.View.HomeDirectory.Header"/>
  </p>
  <p style="line-height:18px;">
    <fmt:message key="Account.View.HomeDirectory.YourHomeDirectoryIs"/>
    <html:link page="${homedir}">
      ${baseurl}${homedir}
    </html:link>.
  </p>
--%>
  
<div class="widgetBorder" style="width:460px; margin-top:24px;">
<div class="widgetContent" style="padding:8px;">  
  
  <div class="hd" style="margin-bottom:4px;"><fmt:message key="Account.View.AccountDetails.Header"/></div>
  
  <cosmo:errmsg/>
  
  <html:form action="/account/update">
    <table cellpadding="3" cellspacing="1" border="0">
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.FirstName"/>
        </td>
        <td>
          <div class="smData"><cosmo:errmsg property="firstName"/></div>
          <div><html:text property="firstName" size="32" maxlength="128" styleClass="textInput"/></div>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.LastName"/>
        </td>
        <td>
          <div class="smData"><cosmo:errmsg property="lastName"/></div>
          <div><html:text property="lastName" size="32" maxlength="128" styleClass="textInput"/></div>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.Email"/>
        </td>
        <td>
          <div class="smData"><cosmo:errmsg property="email"/></div>
          <div><html:text property="email" size="32" maxlength="128" styleClass="textInput"/></div>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          &nbsp;
        </td>
        <td>
          <span class="smData"><fmt:message key="Account.Form.PasswordBlurb"/></span>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.Password"/>
        </td>
        <td>
          <div class="smData"><cosmo:errmsg property="password"/></div>
          <div><html:password property="password" size="16" maxlength="16" styleClass="textInput"/></div>
        </td>
      </tr>
      <tr>
        <td class="mdLabel" style="text-align:right;">
          <fmt:message key="Account.Form.Confirm"/>
        </td>
        <td>
          <div class="smData"><cosmo:errmsg property="confirm"/></div>
          <div><html:password property="confirm" size="16" maxlength="16" styleClass="textInput"/></div>
        </td>
      </tr>
    </table>
    
<div style="margin-top:12px; text-align:right;">    
<html:submit property="create" styleClass="buttonInput">
<fmt:message key="Account.Form.Button.Update"/>
</html:submit>
</div>
    
  </html:form>
  
</div> 
</div>  
  
</authz:authorize>

</div>
