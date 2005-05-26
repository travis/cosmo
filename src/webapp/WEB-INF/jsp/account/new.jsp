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
  <b><fmt:message key="Account.New.NewAccount"/></b>
</p>

<p>
  <fmt:message key="Account.New.AllFieldsRequired"/></b>
</p>

<cosmo:cnfmsg/>
<cosmo:errmsg/>

<html:form action="/account/signup">
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
          <fmt:message key="Account.Form.Button.Create"/>
        </html:submit>
      </td>
    </tr>
  </table>
</html:form>
