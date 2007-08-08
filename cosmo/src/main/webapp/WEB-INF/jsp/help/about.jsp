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
<fmt:setBundle basename="PimMessageResources"/>

<cosmo:dialogLayout prefix="About.">
<cosmo:staticbaseurl var="staticBaseUrl"/>
<fmt:message key="App.LogoUri" var="logoUri"/>
<fmt:message key="App.TemplateName" var="templateName"/>

<style type="text/css">

* {
    font-family: "Lucida Grande", "Verdana", "Arial", sans-serif;
    font-size: 12px;
}
a {
    color:#333399;
}

</style>
<script type="text/javascript" src="${staticBaseUrl}/cosmo.js"></script>
<u:bind var="PRODUCT_VERSION"
        type="org.osaf.cosmo.CosmoConstants"
        field="PRODUCT_VERSION"/>

<div style="width:100%;" align="center">

  <div style="margin-top:28px;">

    <div>
    <a href="<fmt:message key="About.LogoLink"/>"
               onclick="goURLMainWin('<fmt:message key="About.LogoLink"/>'); 
               return false;"><img src="${staticBaseUrl}/templates/${templateName}/images/${logoUri}" alt="<fmt:message 
               key="About.LogoAltText"/>" style="border: 0px"/></a>    
    </div>
    <div class="smLabel"><fmt:message 
         key="About.VersionString"/> <c:out value="${PRODUCT_VERSION}"/>
    </div>
    <div style="margin-top:28px;">
    <fmt:message key="About.LicenseLink" var="licenseLink"/>
    <fmt:message key="About.License">
         <fmt:param><a href='${licenseLink}' onclick="goURLMainWin('${licenseLink}'); return false;"></fmt:param>
         <fmt:param></a></fmt:param>
    </fmt:message>
    </div>
    <div style="margin-top:16px;">
    <fmt:message key="About.InfoLink" var="infoLink"/>
    <fmt:message key="About.Info">
         <fmt:param><a href='${infoLink}' onclick="goURLMainWin('${infoLink}'); return false;"></fmt:param>
         <fmt:param></a></fmt:param>
    </fmt:message>
    
    <div class="notices">
        <fmt:message key="About.NoticesUrl" var="noticesUrl"/>
    	<jsp:include page="${noticesUrl}"/>
    </div>
    </div>
    
  </div>

</div>

</cosmo:dialogLayout>
