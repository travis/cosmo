<%@ page language="java" contentType="text/html; charset=UTF-8" %>

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
<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>
<cosmo:staticbaseurl var="staticBaseUrl"/>

<script type="text/javascript">

	// Set this to true to get nice dojo debugging messages.
	
    var djConfig = {isDebug: false};
</script>

<script type="text/javascript" src="${staticBaseUrl}/js/lib/dojo-event_and_io/dojo.js.uncompressed.js"></script>
<script type="text/javascript">
{
    dojo.require("dojo.widget.*");

    var staticBaseUrl = "${staticBaseUrl}";

    dojo.registerNamespaceManifest("cosmo", "../../cosmo", "cosmo", "cosmo.ui.widget",null);

    dojo.require("cosmo.env");
    dojo.require("cosmo.ui.widget.Debug");
    cosmo.env.setBaseUrl("${staticBaseUrl}");
    dojo.widget.manager.registerWidgetPackage("cosmo.ui.widget");
}
    
</script>


