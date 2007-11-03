<%@ page language="java" contentType="text/html; charset=UTF-8" %>

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


<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>
<cosmo:baseurl var="baseUrl"/>
<cosmo:staticbaseurl var="staticBaseUrl"/>
<c:set var="ticketedView" value="${not empty ticketKey }"/>
<cosmoui:user var="user"/>

<fmt:setBundle basename="PimMessageResources"/>

<u:bind var="PRODUCT_VERSION"
        type="org.osaf.cosmo.CosmoConstants"
        field="PRODUCT_VERSION"/>

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

<title><fmt:message key="App.Welcome"/></title>
<cosmo:stylesheets stylesheets="pim"/>

<!--[if IE]>
<style type="text/css">
  html, body { overflow: hidden; }
</style>
<![endif]-->

<c:if test="${not empty relationLinks}">
<link rel="self" type="text/html" href="${relationLinks['pim']}"/>
<link rel="alternate" type="application/atom+xml" href="${relationLinks['atom']}"/>
<link rel="alternate" type="text/html" href="${relationLinks['dav']}"/>
<link rel="alternate" type="text/xml" href="${relationLinks['mc']}"/>
<link rel="alternate" type="text/calendar" href="${relationLinks['webcal']}"/>
</c:if>

<cosmo:dojoBoilerplate timezones="true" dojoLayers="cosmo-login,cosmo-pim"/>

<script type="text/javascript">
// Dojo requires
dojo.require('cosmo.app');
dojo.require('cosmo.app.pim.layout');
dojo.require('cosmo.convenience');
dojo.require('cosmo.topics');
dojo.require('cosmo.account.preferences');
dojo.require('cosmo.account.settings');

cosmo.app.initObj = cosmo.app.pim;
cosmo.app.initParams = {};

var collectionUrlIndex = location.pathname.indexOf("collection");
if (collectionUrlIndex >= 0){
	cosmo.app.initParams.collectionUrl = 
		location.pathname.substring(collectionUrlIndex) + location.search
	cosmo.app.initParams.collectionUid = 
		location.pathname.substring(collectionUrlIndex + 11);
}
var params = (location.search)? cosmo.util.uri.parseQueryString(location.search) : {};

if (params.ticket) 
    cosmo.app.initParams.ticketKey = params.ticket[0];

if (params.view)
	cosmo.app.initParams.initialView = cosmo.app.pim.views[params.view[0].toUpperCase()];

cosmo.app.initParams.authAccess = cosmo.util.auth.currentlyAuthenticated();

<authz:authorize ifAnyGranted="ROLE_USER">
cosmo.app.initParams.roleUser = true;
</authz:authorize>
<authz:authorize ifAllGranted="ROLE_ROOT">
cosmo.app.initParams.roleRoot = true;
</authz:authorize>

dojo.require("cosmo.ui.event.listeners");
cosmo.ui.event.listeners.hookUpListeners();

</script>

</head>

<body id="body">
    <div id="baseLayout" style="position: absolute;"></div>
    <div id="maskDiv">
      <div id="appLoadingMessage">
        Loading the app ...
      </div>
    <div id="dojoDebug">
    </div>
    </div>
</body>

</html>
