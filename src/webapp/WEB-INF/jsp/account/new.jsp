<%@ page language="java" contentType="text/html; charset=UTF-8" %>

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

<div class="widgetBorder" style="width:460px; margin-top:24px;">
<div class="widgetContent" style="padding:8px;">

<div class="hd" style="margin-bottom:4px;"><fmt:message key="Account.New.NewAccount"/></div>

<p>
  <fmt:message key="Account.New.AllFieldsRequired"/>
</p>

<cosmo:cnfmsg/>
<cosmo:errmsg/>

<html:form action="/account/signup">
  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.Form.Username"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="username"/></div>
        <div><html:text property="username" size="32" maxlength="32" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.Form.FirstName"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="firstName"/></div>
        <div><html:text property="firstName" size="32" maxlength="128" styleClass="textInput"/></iv>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.Form.LastName"/>
      </td>
      <td>
        <div class="smData"><cosmo:errmsg property="lastName"/></div>
        <div><html:text property="lastName" size="32" maxlength="128" styleClass="textInput"/></iv>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.Form.Email"/>
      </td>
      <td class="mdData">
        <div class="smData"><cosmo:errmsg property="email"/></div>
        <div><html:text property="email" size="32" maxlength="128" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.Form.Password"/>
      </td>
      <td class="mdData">
        <div class="smData"><cosmo:errmsg property="password"/></div>
        <div><html:password property="password" size="16" maxlength="16" styleClass="textInput"/></div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Account.Form.Confirm"/>
      </td>
      <td class="mdData">
        <div class="smData"><cosmo:errmsg property="confirm"/></div>
        <div><html:password property="confirm" size="16" maxlength="16" styleClass="textInput"/></div>
      </td>
    </tr>
  </table>
  
<div style="margin-top:12px; text-align:right;">   
<html:submit property="create" styleClass="buttonInput">
<fmt:message key="Account.Form.Button.Create"/>
</html:submit>
</div>
  
</html:form>

</div>
</div>
