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
<fmt:message key="Login.DownloadLink" var="downloadLink"/>
<fmt:message key="Login.HomeLink" var="homeLink"/>
<fmt:message key="Account.Activate.SetupLink" var="setupLink"/>

<cosmo:staticbaseurl var="staticBaseUrl"/>

<cosmo:threeColumnLayout prefix="Account.Activate." stylesheets="activation">
  <div id="center" class="column">

    <span id="congratulations"><fmt:message key="Account.Activate.Congrats"/></span>
    <span id="activatedMessage"><fmt:message key="Account.Activate.ActivatedMessage"/></span>
    <table id="user">
      <tbody>
        <tr><td class="label"><fmt:message key="Account.Activate.Username"/></td><td><c:out value="${user.username}"/></td></tr>
        <tr><td class="label"><fmt:message key="Account.Activate.Email"/></td><td><c:out value="${user.email}"/></td></tr>
      </tbody>
    </table>

    <div class="separateBig restrictWidth huge">
      <a href="${setupLink}">
        <fmt:message key="Account.Activate.CenterPromoLine1"/><br/>
        <fmt:message key="Account.Activate.CenterPromoLine2"/>
      </a>
    </div>

    <div class="separate padbottom restrictWidth">
      <a href="${setupLink}">
        <fmt:message key="Account.Activate.SetupClickHere"/>
      </a>
      <fmt:message key="Account.Activate.SetupText"/>
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
      <p class="huge separateBig close">
        <a id="login" href="${staticBaseUrl}/login">
          <fmt:message key="Account.Activate.ActivatedMessageLogIn"/>
        </a>
      </p>
      <p class="close">
        <fmt:message key="Account.Activate.LoginLine1"/>
      </p>
      <p class="separate">
        <fmt:message key="Account.Activate.LoginLine2"/>
      </p>
      <ol>
        <li><fmt:message key="Account.Activate.LoginList1"/></li>
        <li><fmt:message key="Account.Activate.LoginList2"/></li>
        <li><fmt:message key="Account.Activate.LoginList3"/></li>
      </ol>
      <p class="learn">
        <a href="${setupLink}">
          <fmt:message key="Account.Activate.LearnHowClickHere"/>
        </a>
      </p>
    </div>
  </div>

</cosmo:threeColumnLayout>
