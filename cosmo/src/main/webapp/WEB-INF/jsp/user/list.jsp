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

<cosmo:staticbaseurl var="staticBaseUrl"/>

<cosmo:standardLayout prefix="User.List." contentWrapperClass="fullPageWidthContent">

<script type="text/javascript">

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.Button");
dojo.require("cosmo.env");
</script>

<script language="JavaScript">

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
            <th field="activated" dataType="String" align="center">Activated</th>
            <th field="locked" dataType="String" align="center">Locked</th>
            <th field="created" dataType="Date" align="center">Created</th>
            <th field="modified" dataType="Date" align="center">Last Modified</th>
    </tr>
</thead>
</table>


<div id="userAdminLinkBlock">

<a href="javascript:showNewUser()">Create New User</a>
<a 	id="modifySelectedUserLink" 
	href="javascript:showModifySelectedUser()" 
	style="display:none;">Modify Selected User</a>
<a 	id="browseSelectedUserLink" 
	href="javascript:browseSelectedUser()" 
	style="display:none;">Browse Selected User</a>
<a 	id="activateSelectedUserLink" 
	href="javascript:activateSelectedUser()" 
	style="display:none;">Activate Selected User</a>
<a 	id="deleteSelectedUsersLink" 
	href="javascript:dojo.widget.byId('userList').deleteSelectedUsers()" 
	style="display:none;">Delete Selected Users</a>
</div>



<script>


function showNewUser(){
    var createUserDialog = dojo.widget.byId("createUserDialog")
    createUserDialog.show();

    void(0);
}

function showModifySelectedUser(){
    var modifyDialog = dojo.widget.byId("modifyUserDialog");

    var username = dojo.widget.byId("userList").getSelectedData()[0].username;

    modifyDialog.populateFields(username);
    modifyDialog.show();
    void(0);
    }

function browseSelectedUser(){

    var username = dojo.widget.byId("userList").getSelectedData()[0].username;

    location = cosmo.env.getBaseUrl() + "/browse/" + username;
    }

function activateSelectedUser(){

	var username = dojo.widget.byId("userList").getSelectedData()[0].userObject.username;

	var activateHandlerDict = { 
		load: function (type, data, evt){
            dojo.widget.byId('userList').updateUserList();
		},
		error: function(type, data, evt){
			alert("Error activating user");
			// TODO: Remove alert messages
		}
	}

	cosmo.cmp.activate(username, activateHandlerDict);

}



var modifyHandlerDict= {
    handle : function(type, data, evt){
        var modifyDialog = dojo.widget.byId("modifyUserDialog")
	    if (evt.status == 204){
            modifyDialog.hide();
            modifyDialog.form.reset();
            
            dojo.widget.byId('userList').updateUserList();

        }
        else if (evt.status == 431){
            //TODO: username in use stuff
            modifyDialog.usernameError.innerHTML = "Username in use";
        }
        else if (evt.status == 432){
            //TODO: email in use stuff
            modifyDialog.emailError.innerHTML = "Email in use";
        } else {
        	alert("Problem handling modify result: " + evt.status);
        }
    }
}

var createHandlerDict = {

    handle : function(type, data, evt){

        var createDialog = dojo.widget.byId("createUserDialog");
        if (evt.status == 201){
            createDialog.hide();
            createDialog.form.reset();

            dojo.widget.byId('userList').updateUserList();

        }
        else if (evt.status == 431){
            //TODO: username in use stuff
            createDialog.usernameError.innerHTML = "Username in use";
        }
        else if (evt.status == 432){
            //TODO: email in use stuff
            createDialog.emailError.innerHTML = "Email in use";
        } else {
        	alert("Problem handling create result: " + evt.status);
        }
        

    }
}


dojo.addOnLoad(function (){
	dojo.widget.byId("modifyUserDialog").hide();
	dojo.widget.byId("createUserDialog").hide();

    var userList = dojo.widget.byId("userList");
    var modifyLink = document.getElementById("modifySelectedUserLink");
    var browseLink = document.getElementById("browseSelectedUserLink");
    var activateLink = document.getElementById("activateSelectedUserLink");
    var deleteLink = document.getElementById("deleteSelectedUsersLink");

    /*
     * Methods for use by multiple links
     */


    function disableIfNotSingleSelect(){

        var selection = userList.getSelectedData()

        if (dojo.lang.isArray(selection) &&
            selection.length != 1){
            this.style.display = 'none';

        } else {
            this.style.display = 'inline'
        }

    }
    
    /*
     * Special methods for specific links
     */
    function isSelectionActivated(){
    	var selection = userList.getSelectedData()[0];
    	
    	return selection.userObject.unactivated == undefined;

    }

    function isRootSelected(){
        return userList.isValueSelected(cosmo.env.OVERLORD_USERNAME);
    }

    var getNumberSelected = function(){
        return userList.getSelectedData().length;
    }
    var hide = function(){this.style.display = 'none'};
    
    function refreshControlLinks(){
    	var numberSelected = getNumberSelected();

		deleteLink.style.display = 
			isRootSelected() || numberSelected == 0
			? 'none':'inline';

		modifyLink.style.display = 
			numberSelected == 1
			? 'inline':'none';

		browseLink.style.display = 
			numberSelected == 1
			? 'inline':'none';

		activateLink.style.display = 
			numberSelected == 1 && !isSelectionActivated()
			? 'inline':'none';
    }

    dojo.event.connect("after", userList, "renderSelections", refreshControlLinks);
    dojo.event.connect("after", userList, "updateUserListCallback", refreshControlLinks);
    
    refreshControlLinks();

})



</script>



<div 	dojoType="cosmo:ModifyUserDialog" widgetId="createUserDialog"

        createNew="true"
        postActionHandler="createHandlerDict"
        role="cosmo.ROLE_ADMINISTRATOR"
		classes='floating'
		title='<fmt:message key="User.List.NewUser"/>'
        > </div>

<div 	dojoType="cosmo:ModifyUserDialog" widgetId="modifyUserDialog"
        postActionHandler="modifyHandlerDict"
        role="cosmo.ROLE_ADMINISTRATOR"
		classes='floating'
		title='<fmt:message key="User.List.ModifyUser"/>'
        > </div>
</cosmo:standardLayout>

