<%@ page language="java" contentType="text/html; charset=UTF-8" %>

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

<fmt:setBundle basename="PimMessageResources"/>
<cosmo:staticbaseurl var="staticBaseUrl"/>

<fmt:message key="Login.DownloadLink" var="downloadLink"/>
<fmt:message key="Login.HomeLink" var="homeLink"/>
<cosmo:threeColumnLayout prefix="Login." stylesheets="login" dojoLayers="login"
                         selfLink="${staticBaseUrl}/login">
<c:set var="showSignup" value="${not properties['cosmo.service.account.disableSignups']}"/>
  <script type="text/javascript">

    dojo.require("cosmo.app");
    dojo.require("cosmo.account.create");
    dojo.require("cosmo.convenience");
    dojo.require("cosmo.ui.widget.LoginDialog");
    dojo.require("cosmo.ui.widget.ModalDialog");
    dojo.require("dojo.cookie");
    dojo.addOnLoad(init);

    function init() {
        dojo.cookie('JSESSIONID', null, {expires: -1});
        dojo.cookie('inputTimestamp', null, {expires: -1});
        dojo.cookie('username', null, {expires: -1});
        cosmo.util.auth.clearAuth();
        cosmo.app.init();
        <c:if test="${showSignup}">
        if (dojo.queryToObject(location.search.substring(1))['signup'] == 'true'){
            dojo.addOnLoad(function(){cosmo.account.create.showForm()});
        }
        </c:if>
    
    }
  </script>

  <div id="center" class="column">
    <div dojoType="cosmo.ui.widget.LoginDialog" id="loginDialog">
    </div>
    <c:if test="${showSignup}">
    <div class="bigger separate">
      <fmt:message key="Login.CreateAccount"/>
      <a class="biggest" id="signup" href="javascript:cosmo.account.create.showForm();">
        <fmt:message key="Login.CreateClickHere"/>
      </a>
    </div>
    </c:if>

    <div class="lightText">
      <a href="${staticBaseUrl}/account/password/recover">
        <fmt:message key="Login.Forgot"/>
      </a>
      <c:if test="${cosmoui:getConfigProperty('cosmo.service.account.requireActivation')}">
        |
        <a href="${staticBaseUrl}/account/activation/recover">
          <fmt:message key="Login.LostActivation"/>
        </a>
      </c:if>
    </div>

    <div id="gallery"></div>
    
    <div class="padtop">
      <cosmo:aboutPopupLink/>
    </div>
    <div class="tallLine">
	  <fmt:message key="Login.Extra"/>
    </div>
  </div>

  <div id="left" class="column"></div>
  <div id="right" class="column">
    <div id="promo">
      <p class="biggest">
        <a href="${downloadLink}">
          <fmt:message key="Login.PromoClickHere"/>
        </a>            
        <fmt:message key="Login.Promo"/>
      </p>
      <p class="bigger separate">
        <a href="${homeLink}">
          <fmt:message key="Login.HomeClickHere"/>
        </a>
      </p>
    </div>
  </div>
</cosmo:threeColumnLayout>

