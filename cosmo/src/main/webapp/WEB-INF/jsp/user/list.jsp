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

<script language="JavaScript">
dojo.require("cosmo.data.UserStore");
dojo.require("dojo.data.ItemFileReadStore");
dojo.require("dojox.grid.Grid");
dojo.require("dojox.grid._data.model");
dojo.require("dojo.parser");
dojo.require("dojox.grid.editors");
</script>

<cosmo:cnfmsg/>

<script language="JavaScript">
    function userListSearchKey(event){
        if (event.keyCode == 13) userListSearch();
    }
    function userListSearch(){
        var query = document.getElementById("searchBox").value;
        var newModel = new dojox.grid.data.DojoData(null,null,{rowsPerPage: 20, 
            store: userStore, query: query, clientSort: true});
        userList.setModel(newModel);        
    }
</script>

    <input type="text" id="searchBox" onKeyPress="userListSearchKey(event)"/>
<button id="searchButton" onClick="userListSearch()">Search</button>
<div dojoType="cosmo.data.UserStore" jsId="userStore">
  <script type="dojo/connect" event="onSet" args="item,attr,oldVal,newVal">
    if (oldVal != newVal){
        console.debug("About to change "+attr+" from "+oldVal+" to "+newVal);
        this.save();
    }
  </script>
</div>
<div dojoType="dojox.grid.data.DojoData" jsId="model"
     rowsPerPage="20" store="userStore" query="">
</div>
    <div id="userList" autoHeight="true" dojoType="dojox.Grid" model="model" jsId="userList">
<script type="dojo/method">
  var view1 = {
      noscroll: true,
      cells: [[
          {name: 'Username', field: "username", width: "auto",
           editor: dojox.grid.editors.Input
          },
          {name: 'First Name', field: "firstName",  width: "auto",
           editor: dojox.grid.editors.Input
          },
          {name: 'Last Name', field: "lastName", width: "auto",
           editor: dojox.grid.editors.Input
          },
          {name: 'Email',  field: "email", width: "auto",
           editor: dojox.grid.editors.Input
          },
          {name: 'Created',  field: "dateCreated", width: "auto"},
          {name: 'Modified',  field: "dateModified", width: "auto"},
          {name: 'Url',  field: "url", width: "auto"},
          {name: 'Locked',  field: "locked", width: "6em", 
           styles: "text-align: center;", editor: dojox.grid.editors.Bool
          },
          {name: 'Admin',  field: "administrator", width: "6em",
           styles: "text-align: center;", editor: dojox.grid.editors.Bool
          },
          {name: 'Unactivated',  field: "unactivated", width: "auto"},
          {name: 'Password', field: "password", width: "auto",
           editor: dojox.grid.editors.Input
          }
      ]]
  };
  // a grid layout is an array of views.
  var layout = [ view1 ];
  userList.setStructure(layout);
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

