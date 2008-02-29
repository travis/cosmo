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

<cosmo:standardLayout prefix="User.List." contentWrapperClass="foo">

<cosmo:cnfmsg/>

<script language="JavaScript">

dojo.require("cosmo.data.UserStore");
dojo.require("cosmo.util.notify");
dojo.require("dojox.grid.Grid");
dojo.require("dojox.grid._data.dijitEditors");
dojo.require("dijit.form.ValidationTextBox");
dojo.require("dijit.form.CheckBox");
dojo.require("dojox.validate.regexp");

function userListSearchKey(event){
    if (event.keyCode == 13) userListSearch();
    }
function userListSearch(){
    var query = document.getElementById("searchBox").value;
    var newModel = new dojox.grid.data.DojoData(null,null,{rowsPerPage: 20, 
        store: userStore, query: {q: query}, clientSort: true});
    userList.setModel(newModel);        
}

dojo.requireLocalization("cosmo.ui.widget", "UserList");
var l10n =  dojo.i18n.getLocalization("cosmo.ui.widget", "UserList");
DEFAULT_PASSWORD_VALUE = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022"
var userListLayout = [{
    cells: [[
        {name: l10n.username, field: "username",
         editor: dojox.grid.editors.Dijit,
         editorClass: "dijit.form.ValidationTextBox",
         editorProps: {regExp:".{3,32}", required: true, 
                       invalidMessage: l10n.usernameValid
                      }
        },
        {name: l10n.firstName, field: "firstName",
         editor: dojox.grid.editors.Dijit,
         editorClass: "dijit.form.ValidationTextBox",
         editorProps: {regExp:".{1,128}", required: true,
                       invalidMessage: l10n.firstNameValid
                      }
        },
        {name: l10n.lastName, field: "lastName",
         editor: dojox.grid.editors.Dijit,
         editorClass: "dijit.form.ValidationTextBox",
         editorProps: {regExp:".{1,128}", required: true,
                       invalidMessage: l10n.lastNameValid
                      }
        },
        {name: l10n.email,  field: "email", width: "10em",
         editor: dojox.grid.editors.Dijit,
         editorClass: "dijit.form.ValidationTextBox",
         editorProps: {regExp:dojox.regexp.emailAddress(), required: true,
                       invalidMessage: l10n.emailValid
                      }
        },
        {name: l10n.password, field: "password", 
         styles: "text-align: center;", value: DEFAULT_PASSWORD_VALUE,
         editor: dojox.grid.editors.Dijit,
         editorClass: "dijit.form.ValidationTextBox",
         applyEdit: 
         function(inValue, inRowIndex){
             if (inValue == DEFAULT_PASSWORD_VALUE) this.cancelEdit(inRowIndex);
             else if (window.prompt(l10n.passwordConfirm) == inValue) this.inherited("applyEdit", arguments);
             else {
                 this.cancelEdit(inRowIndex);
                 setTimeout(function(){
                     cosmo.util.notify.showMessage(l10n.passwordMismatch);
                 }, 50);
             }
         },
         editorProps: {regExp:".{5,16}", 
                       invalidMessage: l10n.passwordValid
                      }
        },
        {name: l10n.created,  field: "dateCreated", width: "6.5em"},
        {name: l10n.modified,  field: "dateModified", width: "6.5em"},
        {name: l10n.locked,  field: "locked", width: "6em", noresize: "true",
         styles: "text-align: center;", editor: dojox.grid.editors.CheckBox
        },
        {name: l10n.admin,  field: "administrator", width: "6em", noresize: "true",
         styles: "text-align: center;", editor: dojox.grid.editors.CheckBox
        },
        {name: l10n.unactivated,  field: "unactivated", width: "6em"},
        {name: l10n.url,  field: "url", width: "auto" }
    ]]
}];
</script>

<input type="text" id="searchBox" onKeyPress="userListSearchKey(event)"/>
<button id="searchButton" onClick="userListSearch()">Search</button>
<div dojoType="cosmo.data.UserStore" jsId="userStore">
  <script type="dojo/connect" event="onSet" args="item,attr,oldVal,newVal">
    // make sure value has changed and, if password, value is not default
    if (oldVal != newVal){
        console.debug("About to change "+attr+" from "+oldVal+" to "+newVal);
        this.save({
            onComplete: function(){
                cosmo.util.notify.showMessage(dojo.string.substitute(
                    l10n.attributeUpdate, 
                    {attr: l10n[attr], 
                     newVal: newVal}));
            },
            onError: function(e){
                cosmo.util.notify.showMessage(dojo.string.substitute(l10n.attributeUpdateFailed, {attr: l10n[attr]}));
                console.log(e);
            }
        });
        
    }
  </script>
</div>
<div dojoType="dojox.grid.data.DojoData" jsId="model"
     rowsPerPage="20" store="userStore" query="{}">
</div>
    <div id="userList" dojoType="dojox.Grid" model="model" 
         structure="userListLayout" jsId="userList">
<script type="dojo/method">
    dojo.connect(userList, "onStyleRow", function(inRow){
        var row = model.getRow(inRow.index);
        if (row &&
            row.username == 'root'){
            inRow.customStyles = 'background-color:red';
        }
    });
dojo.connect(window, "onresize", dojo.hitch(userList, userList.update));
</script>
<script type="dojo/connect" event="onCellDblClick" args="e">
    if (e.cell.grid.model.getDatum(e.rowIndex, e.cell.index) == "root") {
        var fn = e.cell.field;
        if (fn == "username" ||
            fn == "firstName" ||
            fn == "lastName" ||
            fn == "admin"
           ){
            e.cell.grid.edit.cancel();
        }
    }
    return false;
}
console.log(e.cellNode.textContent)
return e;

</script>

</div>

</cosmo:standardLayout>

