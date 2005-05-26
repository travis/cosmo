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

<fmt:message var="okButton" key="Login.Button.Ok"/>

<c:if test="${not empty paramValues['login_failed']}">
  <p class="md">
    <span class="error"><fmt:message key="Login.Failed"/></span>
  </p>
</c:if>

<form method="POST" action="j_acegi_security_check">
  <table border="0" cellpadding="0" cellspacing="3">
    <tr>
      <td align="right" valign="middle">
        <span class="md"><fmt:message key="Login.Username"/></span>
      </td>
      <td align="left" valign="middle">
        <span class="md">
          <input type="text" name="j_username" size="16"
                 maxlength="32" class="md"/>
        </span>
      </td>
    </tr>
    <tr>
      <td align="right" valign="middle">
        <span class="md"><fmt:message key="Login.Password"/></span>
      </td>
      <td align="left" valign="middle">
        <span class="md">
          <input type="password" name="j_password" size="16"
                 maxlength="16" class="md"/>
        </span>
      </td>
    </tr>
    <tr>
      <td align="right" valign="middle">
        &nbsp;
      </td>
      <td align="left" valign="middle">
        <span class="md">
          <input type="submit" name="ok" value="${okButton}"
                 class="md"/>
        </span>
      </td>
    </tr>
  </table>
</form>
