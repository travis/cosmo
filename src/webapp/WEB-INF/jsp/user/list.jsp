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

<cosmo:standardLayout prefix="User.List.">

<%@ include file="/WEB-INF/jsp/pim/dojo.jsp" %>
<script type="text/javascript">

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.Button");
dojo.require("cosmo.env");
</script>

<style type="text/css">
		/***
			The following is just an example of how to use the table.
			You can override any class names to be used if you wish.
		***/


		table#userList {
			width:100%;

		}

		* html div.tableContainer {	/* IE only hack */
			width:95%;

		}

		table#userList td,
		table#userList th{
			border-right:1px solid #999;
			padding:2px;
			font-weight:normal;
			cursor: pointer;

		}
		table#userList thead td, table#userList thead th {
		    font-size: 11px; 
	  		color:#666666;
   	 		font-weight:bold;
    		text-align:center;
    		background:#eeeeee;
  		  	border:1px solid #cccccc;
    		white-space:nowrap;
		}
		
		* html div.tableContainer table thead tr td,
		* html div.tableContainer table thead tr th{
			/* IE Only hacks */
			position:relative;
			top:expression(dojo.html.getFirstAncestorByTag(this,'table').parentNode.scrollTop-2);
		}
		
		html>body tbody.userListBody {

		}

		tbody.userListBody td, tbody.userListBody tr td {
		    font-size: 11px;
    		border:1px solid #cccccc;
		
		}

		tbody.userListBody tr.alternateRow td {
			background: #e3edfa;
			padding: 2px;
		}

		tbody.userListBody tr.selected td {
			background: yellow;
			padding: 2px;
		}
		tbody.userListBody tr:hover td {
			background: #a6c2e7;
			padding: 2px;
		}
		tbody.userListBody tr.selected:hover td {
			background: #ff3;
			padding: 2px;
		}
		
		a.userListPagingLink {
			text-decoration: none;
			color: blue;
		}
		
		span#pageNumberChooser {
			position: absolute;
			right: 2em;
		}
		
		span#pageSizeChooser {
			position: absolute;
			left: 2em;
		}
		
		div#userListControls {
			font-size: 0.9em;
			height: 2em;
		}
		
		form.modifyUserForm {

			width: 50%;
			position: fixed;
			left: 25%;
			top: 25%;
			background: white;
			
		}
		
		img#orderIndicator {
			padding-left : 5px;
		}
		
		div#userAdminLinkBlock{
			margin-top: 0.2em;		
		}
		
		div#userAdminLinkBlock>a {
			text-decoration: none;
			font-size: 0.98em;
			margin-right: 1em;
		}

	</style>
	


<script type="text/javascript" src="${staticBaseUrl}/js/cosmo/cmp/cmp.js"></script>

<script language="JavaScript">

var GLOBAL_BAR;

dojo.require("cosmo.ui.widget.CosmoUserList");
dojo.require("cosmo.ui.widget.ModifyUserDialog");

</script>

<cosmo:staticbaseurl var="staticBaseUrl"/>



<cosmo:cnfmsg/>

<table dojoType="cosmo:CosmoUserList" id="userList"
	   widgetId="userList" headClass="userListHead" tbodyClass="userListBody" enableMultipleSelect="true" 
	   enableAlternateRows="true" rowAlternateClass="alternateRow" multiple="true"
	   valueField = "username">

<thead>

	<tr>

			<th field="name" dataType="String">Name</th>
			<th field="username" dataType="String" align="center">Username</th>
			<th field="email" dataType="String" align="center">Email</th>
			<th field="admin" dataType="String" align="center">Administrator</th>
			<th field="created" dataType="Date" align="center">Created</th>
			<th field="modified" dataType="Date" align="center">Last Modified</th>
	</tr>
</thead>
</table>


<div id="userAdminLinkBlock">

<a href="javascript:toggleNewUser()">Create New User</a>
<a id="modifySelectedUserLink" href="javascript:showModifySelectedUser()" style="display:none;">Modify Selected User</a>
<a id="deleteSelectedUsersLink" href="javascript:dojo.widget.byId('userList').deleteSelectedUsers()">Delete Selected Users</a>
</div>



<script>


function toggleNewUser(){
	var createUserDialog = dojo.widget.byId("createUserDialog")
	if (createUserDialog.isHidden) { 
		createUserDialog.show() 
	} else { 
		createUserDialog.hide()
	};
	
	void(0);
}

function showModifySelectedUser(){
	var modifyDialog = dojo.widget.byId("modifyUserDialog")
	
	username = dojo.widget.byId("userList").getSelectedData()[0].username;

	modifyDialog.populateFields(username)
	modifyDialog.show();
	void(0);
	}



modifyHandlerDict= {
	handle : function(type, data, evt){
		if (evt.status == 204){
			var modifyDialog = dojo.widget.byId("modifyUserDialog")
			
			modifyDialog.hide();
			modifyDialog.form.reset();
			dojo.widget.byId('userList').updateUserList();	

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

createHandlerDict= {

	handle : function(type, data, evt){

		if (evt.status == 201){
			var createDialog = dojo.widget.byId("createUserDialog");
			createDialog.hide();
			createDialog.form.reset();
			
			dojo.widget.byId('userList').updateUserList();	

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


dojo.addOnLoad(function (){
	
	var userList = dojo.widget.byId("userList");
	var modifyLink = document.getElementById("modifySelectedUserLink");
	var deleteLink = document.getElementById("deleteSelectedUsersLink");
	
	modifyLink.disableIfNotSingleSelect = function(){
	
		var selection = userList.getSelectedData()
	
		if (dojo.lang.isArray(selection) &&
			selection.length != 1){
			this.style.display = 'none';
	
		} else {
			this.style.display = 'inline'
		}
	
	}
	
	
	
	deleteLink.disableIfRootSelected = function(){
		this.style.display = (userList.isValueSelected(cosmo.env.OVERLORD_USERNAME))?
			'none':'inline';
	
	}
	
	
	/*dojo.event.topic.subscribe(
		"/userListSelectionChanged", 
		modifyLink,
		"disableIfNotSingleSelect"
		);
	dojo.event.topic.subscribe(
		"/userListUpdate", 
		modifyLink,
		"disableIfNotSingleSelect"
		);*/
	userList = dojo.widget.byId("userList")
	dojo.event.connect("after", userList, "renderSelections", deleteLink, "disableIfRootSelected");
	dojo.event.connect("after", userList, "renderSelections", modifyLink, "disableIfNotSingleSelect");
	dojo.event.connect("after", userList, "updateUserListCallback", modifyLink, "disableIfRootSelected");
	dojo.event.connect("after", userList, "updateUserListCallback", modifyLink, "disableIfNotSingleSelect");

})



</script>



<div 	dojoType="cosmo:ModifyUserDialog" widgetId="createUserDialog"
		
		createNew="true"
		
		usernameLabel='<fmt:message key="User.Form.Username"/>'
        firstNameLabel='<fmt:message key="User.Form.FirstName"/>'
        lastNameLabel='<fmt:message key="User.Form.LastName"/>'
        emailLabel='<fmt:message key="User.Form.Email"/>'
        passwordLabel='<fmt:message key="User.Form.Password"/>'
        confirmLabel='<fmt:message key="User.Form.Confirm"/>'
        adminLabel='<fmt:message key="User.Form.MakeAdministrator"/>'
        postActionHandler="createHandlerDict"
        role="cosmo.ROLE_ADMINISTRATOR"
        cancelButtonText='<fmt:message key="User.Form.Button.Cancel"/>'
        submitButtonText='<fmt:message key="User.Form.Button.Create"/>'
        
        isHidden="true"
		> </div>

<div 	dojoType="cosmo:ModifyUserDialog" widgetId="modifyUserDialog"
        usernameLabel='<fmt:message key="User.Form.Username"/>'
        firstNameLabel='<fmt:message key="User.Form.FirstName"/>'
        lastNameLabel='<fmt:message key="User.Form.LastName"/>'
        emailLabel='<fmt:message key="User.Form.Email"/>'
        passwordBlurb='<fmt:message key="User.Form.PasswordBlurb"/>'
        passwordLabel='<fmt:message key="User.Form.Password"/>'
        confirmLabel='<fmt:message key="User.Form.Confirm"/>'
        adminLabel='<fmt:message key="User.Form.MakeAdministrator"/>'
        postActionHandler="modifyHandlerDict"
        role="cosmo.ROLE_ADMINISTRATOR"
        cancelButtonText='<fmt:message key="User.Form.Button.Cancel"/>'
        submitButtonText='<fmt:message key="User.Form.Button.Update"/>'

        isHidden="true"
		> </div>
</cosmo:standardLayout>

