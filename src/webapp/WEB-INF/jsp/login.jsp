<%--
/*
 * Copyright 2006 Open Source Applications Foundation
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
<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<cosmo:staticbaseurl var="staticBaseUrl"/>


<%@ include file="/WEB-INF/jsp/pim/dojo.jsp" %>

<script type="text/javascript" src="${staticBaseUrl}/i18n.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/ui/ui.conf.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/ui/button.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/ui/login.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/ui/styler.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/util/cookie.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/util/log.js"></script>
<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/util/popup.js"></script>
<fmt:setBundle basename="PimMessageResources"/>


<script type="text/javascript">
var AUTH_PROC = '${staticBaseUrl}/console/j_acegi_security_check';
</script>

<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/ui/global.css.js"></script>


  <div>
    <div style="margin:auto; width:280px; text-align:left; padding-top:24px;">
      <div id="logoDiv" style="padding-bottom:12px;"></div>
      <div id="promptDiv" class="promptText" style="height:46px;">
        <fmt:message key="Login.Prompt"/>
      </div>
      <div class="baseWidget">
        <div style="padding:12px;">
          <form id="loginForm" name="loginForm" method="post" 
            action="${staticBaseUrl}/console/j_acegi_security_check" onsubmit="return false;">
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
      <div style="padding-top:24px; text-align:center">
              <fmt:message key="Login.NoAccount"/>
      </div>
      <div style="padding-top:4px; text-align:center">
          <a href="${staticBaseUrl}/console/account/new">
              <fmt:message key="Login.CreateAccount"/>
          </a>
      </div>
      <div style="padding-top:36px; text-align:center;">
        <a href="javascript:Popup.open('${staticBaseUrl}/console/about', 340, 280);">
          <fmt:message key="Login.About"/>
        </a>
      </div>
    </div>
  </div>

