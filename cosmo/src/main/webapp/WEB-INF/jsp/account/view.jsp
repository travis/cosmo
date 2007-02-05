<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
<cosmoui:user var="user" />
<cosmo:standardLayout prefix="Account.View.">


<script type="text/javascript">

dojo.require("cosmo.ui.widget.ModifyUserDialog");

modifyHandlerDict= {
    handle : function(type, data, evt){

        if (evt.status == 204){
            dojo.widget.byId("modifyUserDialog").populateFields();

        }
        else if (evt.status == 431){
            //TODO: username in use stuff
            alert("Username in use")
        }
        else if (evt.status == 432){
            //TODO: email in use stuff
            alert("Email in use")
        }

    }
}

</script>

<div style="width:500px;">

<p>
  <fmt:message key="Account.View.Welcome">
    <fmt:param value="${pageContext.request.serverName}"/>
  </fmt:message>
</p>

<authz:authorize ifAllGranted="ROLE_USER">
  <cosmoui:user var="user"/>
  <cosmo:homedir var="homedir" user="${user}"/>
  <cosmo:baseurl var="baseurl"/>

  <p class="hd">
    <fmt:message key="Account.View.HomeDirectory.Header"/>
  </p>

  <p style="line-height:18px;">
    <fmt:message key="Account.View.HomeDirectory.YourHomeDirectoryIs"/>
  </p>

  <p style="line-height:18px;">
    <fmt:message key="Account.View.HomeDirectory.FullURLIs"/><br/>
    <strong>${relationLinks['dav']}</strong>
  </p>

  <p>
    <a href="<c:url value="/browse/${user.username}"/>">
      <fmt:message key="Account.View.BrowseHomeDirectory"/>
    </a>
  </p>

<div 	dojoType="cosmo:ModifyUserDialog" widgetId="modifyUserDialog"
        role="cosmo.ROLE_AUTHENTICATED"
        header='<fmt:message key="Account.View.AccountDetails.Header"/>'
        firstNameLabel='<fmt:message key="Account.Form.FirstName"/>'
        lastNameLabel='<fmt:message key="Account.Form.LastName"/>'
        emailLabel='<fmt:message key="Account.Form.Email"/>'
        passwordBlurb='<fmt:message key="Account.Form.PasswordBlurb"/>'
        passwordLabel='<fmt:message key="Account.Form.Password"/>'
        confirmLabel='<fmt:message key="Account.Form.Confirm"/>'
        postActionHandler="modifyHandlerDict"
        removeInputs="username,admin"
        disableCancel="true"
        submitButtonText='<fmt:message key="Account.Form.Button.Update"/>'
        populateOnLoad="true"
		style="position: static; margin-top: 1em"
        > </div>

</authz:authorize>

</div>
</cosmo:standardLayout>