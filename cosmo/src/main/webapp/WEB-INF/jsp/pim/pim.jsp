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

<cosmo:dojoBoilerplate timezones="true" dojoLayers=""/>

<!--<script type="text/javascript">
console.debug("abc")
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
var params = (location.search)? dojo.queryToObject(location.search.substring(1)) : {};

if (params.ticket) 
    cosmo.app.initParams.ticketKey = params.ticket[0];

if (params.view)
	cosmo.app.initParams.initialView = cosmo.app.pim.views[params.view[0].toUpperCase()];

-->
<script type="text/javascript">
dojo.require("dojo.parser");
dojo.require("cosmo.app");
dojo.require("cosmo.app.pim");
cosmo.app.initObj = cosmo.app.pim;
cosmo.app.initParams = {};
dojo.require("cosmo.util.auth");
cosmo.app.initParams.authAccess = cosmo.util.auth.currentlyAuthenticated();
<authz:authorize ifAnyGranted="ROLE_USER">
cosmo.app.initParams.roleUser = true;
</authz:authorize>
<authz:authorize ifAllGranted="ROLE_ROOT">
cosmo.app.initParams.roleRoot = true;
</authz:authorize>
dojo.addOnLoad(function(){
debugger
    cosmo.app.init();
});
</script>
<!--
dojo.require("cosmo.ui.event.listeners");

cosmo.ui.event.listeners.hookUpListeners();
</script>
-->
<script type="text/javascript">
dojo.require("dijit.layout.LayoutContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("cosmo.app.pim.layout");
dojo.require("cosmo.ui.selector");
dojo.require("cosmo.ui.widget.CollectionSelector");
dojo.require("cosmo.ui.minical");
dojo.require('cosmo.service.conduits.common');
dojo.require('cosmo.service.tickler');
cosmo.app.pim.serv = cosmo.service.tickler.wrapService(cosmo.service.conduits.getAtomPlusEimConduit());

</script>
    <style> 
        html, body { height: 100%; width: 100%; margin: 0; padding: 0; }
    </style>
<link rel="stylesheet" href="${staticBaseUrl}/js/lib/dojo/dijit/themes/dijit.css"/>
</head>

<body id="body">
<div dojoType="dijit.layout.LayoutContainer" style="width: 100%; height: 100%; padding: 0; margin: 0; border: 0;" id="baseLayout">
  <div dojoType="dijit.layout.ContentPane" layoutAlign="top" style="height: 47px;">
    <div dojoType="cosmo.app.pim.layout.MenuBar"></div>
   </div>
   <div dojoType="dijit.layout.ContentPane" layoutAlign="left"
        style="width: 168px;">
     <authz:authorize ifAnyGranted="ROLE_USER">
       <div dojoType="cosmo.ui.selector.CollectionSelector"></div>
     </authz:authorize>
     <authz:authorize ifNotGranted="ROLE_USER">
       <div dojoType="cosmo.ui.widget.CollectionSelector"></div>
     </authz:authorize>
     <div dojoType="cosmo.ui.minical.MiniCal" currDate="${cosmo.app.pim.currDate}"></div>
   </div>
   <div dojoType="dijit.layout.ContentPane" layoutAlign="client" style="background-color:yellow">
    baz
    </div>
   <div dojoType="dijit.layout.ContentPane" layoutAlign="right"
        style="background-color:green;width: 257px;">
     bar
    </div>

</div>
<!--    <div id="baseLayout" style="position: absolute;"></div>
    <div id="maskDiv">
      <div id="appLoadingMessage">
        Loading the app ...
      </div>
    <div id="dojoDebug">
    </div>
    </div>-->

</body>

</html>
