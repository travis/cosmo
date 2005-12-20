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

<div class="widgetBorder" style="width:460px; margin-top:24px;">
<div class="widgetContent" style="padding:8px;">

<div class="hd" style="margin-bottom:4px;">
  <fmt:message key="User.View.Title">
    <fmt:param value="${User.username}"/>
  </fmt:message>
</div>

<c:choose>
  <c:when test="${User.overlord}">
    <c:set var="action" value="/user/root/update"/>
  </c:when>
  <c:otherwise>
    <c:set var="action" value="/user/update"/>
  </c:otherwise>
</c:choose>

<html:form action="${action}">
  
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.View.DateCreated"/>
      </td>
      <td class="mdData">
        <fmt:formatDate value="${User.dateCreated}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.View.DateLastModified"/>
      </td>
      <td class="mdData">
        <fmt:formatDate value="${User.dateModified}" type="both"/>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.Username"/>
      </td>
      <td>
      	<div class="smData"><cosmo:errmsg property="username"/></div>
      	<div>
          <html:text property="username" size="32" maxlength="32"
                     styleClass="textInput"
                     disabled="${User.overlord}"/>
      	</div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.FirstName"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="firstName"/></div>
        <div>
          <html:text property="firstName" size="32" maxlength="128"
                     styleClass="textInput"
                     disabled="${User.overlord}"/>
      	</div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="User.Form.LastName"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="lastName"/></div>
        <div>
          <html:text property="lastName" size="32" maxlength="128"
                     styleClass="textInput"
                     disabled="${User.overlord}"/>
      	</div>
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
        &nbsp;
      </td>
      <td class="mdData">
        <span class="sm"><fmt:message key="User.Form.PasswordBlurb"/></span>
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
          <html:checkbox property="admin" value="true"
                         disabled="${User.overlord}"/>
          <fmt:message key="User.Form.MakeAdministrator"/>
        </div>
      </td>
    </tr>
   
  </table>
  
  <div style="margin-top:12px;">
     <div style="float:left;">
         <html:cancel styleClass="buttonInput">
          <fmt:message key="User.Form.Button.Cancel"/>
        </html:cancel>
      </div>
      <div style="float:right;">
        <html:submit property="create" styleClass="buttonInput">
          <fmt:message key="User.Form.Button.Update"/>
        </html:submit>
      </div>
      <br style="clear:both;"/>
    </div>
</html:form>

</div>
</div>
