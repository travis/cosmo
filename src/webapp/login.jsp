<%--
/*
 * Copyright 2005 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
 <!DOCTYPE html
  PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

<title><fmt:message key="App.Welcome"/></title>

<script type="text/javascript" src="i18n.js"></script>
<script type="text/javascript" src="js/scooby/ui/ui.conf.js"></script>
<script type="text/javascript" src="js/scooby/ui/button.js"></script>
<script type="text/javascript" src="js/scooby/ui/login.js"></script>
<script type="text/javascript" src="js/scooby/ui/styler.js"></script>
<script type="text/javascript" src="js/scooby/util/ajax.js"></script>
<script type="text/javascript" src="js/scooby/util/cookie.js"></script>
<script type="text/javascript" src="js/scooby/util/log.js"></script>
<script type="text/javascript" src="js/scooby/util/popup.js"></script>


<script type="text/javascript">

var AUTH_PROC = 'j_acegi_security_check';

</script>

<script type="text/javascript" src="js/scooby/ui/global.css.js"></script>

</head>

<body>
  <div>
    <div style="margin:auto; width:280px; text-align:left; padding-top:24px;">
      <div id="logoDiv" style="padding-bottom:12px;"></div>
      <div id="promptDiv" class="promptText" style="height:46px;">
        <fmt:message key="Login.Prompt"/>
      </div>
      <div class="baseWidget">
        <div style="padding:12px;">
          <form id="loginForm" name="loginForm" method="post" 
            action="j_acegi_security_check" onsubmit="return false;">
            <table>
              <tr>
                <td class="formTitleHoriz">
                  <fmt:message key="Login.Username"/>&nbsp;
                </td>
                <td style="padding-bottom:4px;">
                  <input class="inputText" type="text" name="j_username"/>
                </td>
              </tr>
              <tr>
                <td class="formTitleHoriz">
                  <fmt:message key="Login.Password"/>&nbsp;
                </td>
                <td><input class="inputText" type="password" 
                  name="j_password" onfocus="Login.loginFocus=true;" 
                  onblur="Login.loginFocus=false;"/></td>
              </tr>
            </table>
            <div id="submitButtonDiv" 
              style="margin-top:16px; float:right;">
            </div>
            <div style="clear:both;"></div>
          </form>
        </div>
      </div>
      <div style="padding-top:36px; text-align:center;">
        <a href="javascript:Popup.open('about.html', 380, 280);">
          <fmt:message key="Login.About"/>
        </a>
      </div>
    </div>
  </div>
</body>

</html>
