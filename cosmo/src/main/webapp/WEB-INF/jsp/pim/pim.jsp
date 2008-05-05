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
<style type="text/css">
    @import "${baseUrl}/js/dojo/resources/dojo.css";
    @import "${baseUrl}/js/cosmo/themes/default/pim.css";
</style>
<cosmo:stylesheets stylesheets=""/>
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

<cosmo:dojoBoilerplate timezones="true" dojoLayers="login,pim"/>

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

var atomCollectionLink = dojo.query("link[rel=alternate][type='application/atom+xml']")[0];
if (atomCollectionLink) {
   var url = atomCollectionLink.getAttribute('href');
   cosmo.app.initParams.collectionUrl = url;
   cosmo.app.initParams.collectionUid = url.match(/collection\/(.*)/)[1].split("?")[0];
}

var params = (location.search)? dojo.queryToObject(location.search.substring(1)) : {};

if (params.ticket) 
    cosmo.app.initParams.ticketKey = params.ticket;

if (params.view)
	cosmo.app.initParams.initialView = cosmo.app.pim.views[params.view.toUpperCase()];

cosmo.app.initParams.authAccess = cosmo.util.auth.currentlyAuthenticated();

<security:authorize ifAnyGranted="ROLE_USER">
cosmo.app.initParams.roleUser = true;
</security:authorize>
<security:authorize ifAllGranted="ROLE_ROOT">
cosmo.app.initParams.roleRoot = true;
</security:authorize>

dojo.require("cosmo.ui.event.listeners");

cosmo.ui.event.listeners.hookUpListeners();
</script>

</head>

<body id="body" class="cosmo">
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
