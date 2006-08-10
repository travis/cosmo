<%--
/*
 * Copyright 2006 Open Source Applications Foundation
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

<title><fmt:message key="App.Welcome"/></title>

<%@ include file="dojo.jsp" %>
<script type="text/javascript" src="i18n.js"></script>
<script type="text/javascript" src="js/scooby/util/log.js"></script>
<script type="text/javascript" src="js/scooby/util/ajax.js"></script>
<script type="text/javascript" src="js/scooby/util/date.js"></script>
<script type="text/javascript" src="js/scooby/util/hash.js"></script>
<script type="text/javascript" src="js/scooby/util/debug.js"></script>
<script type="text/javascript" src="js/scooby/util/validate.js"></script>
<script type="text/javascript" src="js/scooby/util/cookie.js"></script>
<script type="text/javascript" src="js/scooby/util/text.js"></script>
<script type="text/javascript" src="js/scooby/util/popup.js"></script>
<script type="text/javascript" src="js/scooby/model/scoobydate.js"></script>
<script type="text/javascript" src="js/scooby/model/model.js"></script>
<script type="text/javascript" src="js/scooby/ui/ui.conf.js"></script>
<script type="text/javascript" src="js/scooby/ui/block.js"></script>
<script type="text/javascript" src="js/scooby/ui/dialog.js"></script>
<script type="text/javascript" src="js/scooby/ui/cal_main.js"></script>
<script type="text/javascript" src="js/scooby/ui/draggable.js"></script>
<script type="text/javascript" src="js/scooby/ui/resize_area.js"></script>
<script type="text/javascript" src="js/scooby/ui/cal_form.js"></script>
<script type="text/javascript" src="js/scooby/ui/event/handlers.js"></script>
<script type="text/javascript" src="js/scooby/ui/styler.js"></script>
<script type="text/javascript" src="js/scooby/ui/contentcontainer.js"></script>
<script type="text/javascript" src="js/scooby/ui/button.js"></script>
<script type="text/javascript" src="js/scooby/ui/minical.js"></script>
<script type="text/javascript" src="js/scooby/ui/widget/Layout.js"></script>
<script type="text/javascript" src="js/scooby/facade/pref.js"></script>
<script type="text/javascript" src="js/scooby/service/service_stub.js"></script>
<script type="text/javascript" src="js/scooby/service/json_service_impl.js"></script>
<script type="text/javascript" src="js/scooby/legacy/cal_event.js"></script>
<script type="text/javascript" src="js/scooby/legacy/async.js"></script>
<script type="text/javascript" src="js/lib/jsonrpc-java-js/jsonrpc.js"></script>

<script type="text/javascript">
// FIXME: Need to get timeout value from server
var TIMEOUT_MIN = 30;

function init() {
    Cal.init();
}
</script>

<script type="text/javascript" src="js/scooby/ui/event/listeners.js"></script>
<script type="text/javascript" src="js/scooby/ui/global.css.js"></script>


</head>

<body id="body">
        <div id="menuBarDiv">
            <div id="smallLogoDiv"></div>
            <fmt:message key="Main.Welcome"/> ${user.username}
            <span class="menuBarDivider">|</span>
            <a href="javascript:Popup.open('about.html', 380, 280);">
              <fmt:message key="Main.About"/> 
            </a>
            <span class="menuBarDivider">|</span>
            <a href="redirect_login.html"> 
               <fmt:message key="Main.LogOut"/>
            </a>&nbsp;&nbsp;
        </div>
        <div id="calDiv">
            <form method="post" id="calForm" name="calForm" action="">
                <div id="leftSidebarDiv">
                    <div id="calSelectNav"></div>
                    <div id="miniCalDiv"></div>
                </div>
                <div id="calTopNavDiv">
                    <table cellpadding="0" cellspacing="0">
                        <tr>
                            <td>&nbsp;&nbsp;&nbsp;</td>
                            <td id="viewNavButtons"></td>
                            <td>&nbsp;&nbsp;&nbsp;</td>
                            <td id="monthHeaderDiv"></td>
                        </tr>
                    </table>
                </div>
                <div id="dayListDiv"></div>
                <div id="allDayResizeMainDiv">
                    <div id="allDayHourSpacerDiv"></div>
                    <div id="allDayContentDiv"></div>
                </div>
                <div id="allDayResizeHandleDiv"></div>
                <div id="timedScrollingMainDiv">
                    <div id="timedHourListDiv"></div>
                    <div id="timedContentDiv"></div>
                </div>
                <div id="rightSidebarDiv">
                    <div id="eventInfoDiv"></div>
                </div>
            </form>
        </div>
        <div id="maskDiv">
          <div id="processingDiv">
              <fmt:message key="Main.Processing" />
          </div>
        </div>
        <div id="fullMaskDiv"></div>
        <div id="dojoDebug" dojoType="debug"><div>
</body>

</html>

