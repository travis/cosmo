<%--
/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
<%@ attribute name="timezones"        %>

<u:bind var="PRODUCT_VERSION"
        type="org.osaf.cosmo.CosmoConstants"
        field="PRODUCT_VERSION"/>

<c:if test="${empty timezones}">
  <c:set var="timezones" value="false"/>
</c:if>

<script type="text/javascript">

    // Set isDebug to true to get nice dojo debugging messages.


    var djConfig = {isDebug: false, 
                    staticBaseUrl: "${staticBaseUrl}",
                    i18nLocation: "${staticBaseUrl}/i18n.js"};
</script>

<script type="text/javascript" src="${staticBaseUrl}/js/lib/dojo-event_and_io/dojo.js"></script>
<script type="text/javascript">

function bootstrap(){
    dojo.require("dojo.widget.*");
    dojo.require("dojo.debug.console");

    dojo.registerNamespaceManifest("cosmo", "../../cosmo", "cosmo", "cosmo.ui.widget",null);
    dojo.widget.manager.registerWidgetPackage("cosmo.ui.widget");

    dojo.require("cosmo.env");

    cosmo.env.setBaseUrl(djConfig['staticBaseUrl']);
    cosmo.env.version = "${PRODUCT_VERSION}";

    dojo.require("cosmo.ui.widget.Debug");

    dojo.require("cosmo.datetime.*");

    if (${timezones}){
        var registry = new cosmo.datetime.timezone.SimpleTimezoneRegistry("${staticBaseUrl}/js/lib/olson-tzdata/");
        registry.init(["northamerica", "africa", "antarctica", "asia", "australasia", "europe", "pacificnew", "southamerica", "backward"]);
        //registry.init([ "europe"]);
        cosmo.datetime.timezone.setTimezoneRegistry(registry);
    }
    dojo.require('cosmo.ui.conf');
}
bootstrap();

</script>

