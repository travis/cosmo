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

<form method="POST" action="j_acegi_security_check">

<div style="width:100%;" align="center">

<div id="loginErrDiv" style="width:280px; margin-top:8px; text-align:left;">
<div class="sm" style="height:36px;">
<c:choose>
<c:when test="${not empty paramValues['login_failed']}">
<span class="error"><fmt:message key="Login.Failed"/></span>
</c:when>
<c:otherwise>
&nbsp;
</c:otherwise>
</c:choose>
</div>
</div>

<div class="widgetBorder" style="margin-top:8px; width:280px;">
<div style="padding:12px;">

  <table border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        <fmt:message key="Login.Username"/>&nbsp;&nbsp;
      </td>
      <td>
          <input type="text" name="j_username" size="16" maxlength="32" class="textInput"/>
      </td>
    </tr>
    <tr>
    <td colspan="2" class="spacerDiv" style="height:8px;"></td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
	<fmt:message key="Login.Password"/>&nbsp;&nbsp;
      </td>
      <td>
          <input type="password" name="j_password" size="16" maxlength="16" class="textInput"/>
      </td>
    </tr>
    <tr>
    <td colspan="2" class="spacerDiv" style="height:16px;"></td>
    </tr>
    <tr>
    <td colspan="2" style="text-align:right;"><input type="submit" name="ok" value="${okButton}" class="buttonInput"/></td>
    </tr>
  </table>

<div class="sm" style="margin-top:12px;">
<c:url var="forgotUrl" value="/forgot"/>
<html:link page="/forgot" onclick="popup('${forgotUrl}', 'forgot', 'resizable=yes,width=480,height=200,left=40,screenx=40,top=20,screeny=20,scrollbars'); return false"><fmt:message key="Login.Forgot"/></html:link>
</div>
  
</div>
</div>
</div>

</form>

<script type="text/javascript" language="JavaScript1.3">

window.onload=doFocus;

function doFocus() {
	document.forms[0].j_username.focus();
}

</script>
