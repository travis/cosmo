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
<cosmo:baseurl var="baseUrl"/>
<cosmo:jsurl var="jsUrl"/>
<u:bind var="PRODUCT_VERSION"
        type="org.osaf.cosmo.CosmoConstants"
        field="PRODUCT_VERSION"/>

<%@ attribute name="timezones"        %>
<%@ attribute name="parseWidgets"        %>
<%@ attribute name="dojoLayers"        %>

<fmt:setBundle basename="PimMessageResources"/>
<fmt:message key="App.TemplateName" var="templateName"/>
        

<c:if test="${empty timezones}">
  <c:set var="timezones" value="false"/>
</c:if>

<c:if test="${empty parseWidgets}">
  <c:set var="parseWidgets" value="true"/>
</c:if>

<c:if test="${not empty properties}">
  <c:set var="disableSignups" value="${properties['cosmo.service.account.disableSignups']}"/>
</c:if>


<script type="text/javascript">

    // Set isDebug to true to get nice dojo debugging messages
    var djConfig = {isDebug: false, 
                    staticBaseUrlTemplate: "${cosmoui:getStaticHostUrlTemplate()}",
                    staticBaseUrlRange: "${cosmoui:getStaticHostUrlRange()}",
                    i18nLocation: "${baseUrl}/i18n.js",
                    confLocation: "${baseUrl}/webui.conf",
                    templateName: "${templateName}",
                    parseOnLoad: ${parseWidgets},
                    serverBaseUrl: "${baseUrl}",
                    cosmoDisableSignups: "${disableSignups}"}
</script>

<c:set var="dojoPath" value="${jsUrl}/dojo"/>
<script type="text/javascript" src="${dojoPath}/dojo.js"></script>
<c:forEach var="layerName" items="${dojoLayers}">
<script type="text/javascript" src="${dojoPath}/../cosmo/${layerName}.js"></script>
</c:forEach>

<script type="text/javascript">
(function bootstrap(){
    dojo.require("cosmo.env");
    cosmo.env.setVersion("${PRODUCT_VERSION}");
    dojo.require("cosmo.ui.conf");

    <%-- 
      Note: It is possible to set this value to negative numbers --
      Setting the canonical client-side value with a function ensures
      we end up with reasonable numbers in getTimeoutSeconds and getTimeoutMinutes
    --%>
    cosmo.env.setTimeoutSeconds(
        cosmo.ui.conf.uiTimeout ||
        <%=session.getMaxInactiveInterval()%>);

    if (${timezones}){
        dojo.require("cosmo.datetime.timezone.LazyCachingTimezoneRegistry");
        var registry = new cosmo.datetime.timezone.LazyCachingTimezoneRegistry("${baseUrl}/js-${PRODUCT_VERSION}/olson-tzdata");
        cosmo.datetime.timezone.setTimezoneRegistry(registry);
    }
    dojo.require('cosmo.ui.conf');
})();
</script>

