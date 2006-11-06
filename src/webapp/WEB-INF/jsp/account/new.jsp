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
<cosmo:standardLayout prefix="Account.New." showNav="false">

<%@ include file="/WEB-INF/jsp/pim/dojo.jsp" %>
<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/cmp/cmp.js"></script>

<script language="JavaScript">
dojo.require("cosmo.cmp")
dojo.require("cosmo.ui.widget.ModifyUserDialog")

signupHandlerDict= {
	handle : function(type, data, evt){
		if (evt.status == 201){
			window.location = cosmo.env.getNewAccountRedirect()

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

<style type="text/css">
.modifyUserForm {
width: 50%;
position: absolute;
left: 25%;
}
</style>

<div 	dojoType="cosmo:ModifyUserDialog" widgetId="signupDialog"
		role="cosmo.ROLE_ANONYMOUS"		
		createNew="true"
		header='<fmt:message key="Account.New.NewAccount"/><p><fmt:message key="Account.New.AllFieldsRequired"/></p>'
        firstNameLabel='<fmt:message key="Account.Form.FirstName"/>'
        lastNameLabel='<fmt:message key="Account.Form.LastName"/>'
        emailLabel='<fmt:message key="Account.Form.Email"/>'
        passwordLabel='<fmt:message key="Account.Form.Password"/>'
        confirmLabel='<fmt:message key="Account.Form.Confirm"/>'
        postActionHandler="signupHandlerDict"
        removeInputs="admin"
        submitButtonText='<fmt:message key="Account.Form.Button.Create"/>'
        disableCancel="true"
		> </div>


</cosmo:standardLayout>