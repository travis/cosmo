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

/**
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.UserList");

dojo.require("dijit._Templated");
dojo.require("cosmo.data.UserStore");
dojo.require("cosmo.util.notify");
dojo.require("dojox.grid.Grid");
dojo.require("dojox.grid._data.dijitEditors");
dojo.require("dijit.form.ValidationTextBox");
dojo.require("dijit.form.CheckBox");
dojo.require("dojox.validate.regexp");
dojo.require("dijit.Dialog");

dojo.requireLocalization("cosmo.ui.widget", "UserList");

dojo.declare("cosmo.ui.widget.UserList", [dijit._Widget, dijit._Templated], {
    widgetsInTemplate: true,
    templatePath: dojo.moduleUrl("cosmo", "ui/widget/templates/UserList.html"),
    l10n: dojo.i18n.getLocalization("cosmo.ui.widget", "UserList"),
    userListSearchKey: function(event){
        if (event.keyCode == 13) this.userListSearch();
    },

    userListSearch: function(){
        var query = document.getElementById("searchBox").value;
        var newModel = new dojox.grid.data.DojoData(null,null,{rowsPerPage: 20, 
            store: this.store, query: {q: query}, clientSort: true});
        this.userList.setModel(newModel);
    },

    activateUser: function(event){
        var user = this.model.getRow(this.userList.selection.selectedIndex);
    },

    newUser: function(event){
        this.newUserDialog.show();
    },

    createNewUser: function(form){
        console.debug(form);
        var user = {
            
        }
    },

    deleteUser: function(event){
        var selection = userList.selection.getSelected();
        var usernames = [];
        for (var i in selection){
            usernames.push(model.getRow(selection[i]).username);
        }
        if (confirm(dojo.string.substitute(this.l10n.confirmDelete, {usernames: usernames.join()}))){
            for (var i in selection){
                var rowIndex = selection[i];
                userStore.deleteItem(model.getRow(rowIndex).__dojo_data_item);
            }
            userStore.save({
                onComplete: function(){
                    userList.model.remove(selection);
                    userList.selection.clear();
                }}
                          );
        }
        
    },

    DEFAULT_PASSWORD_VALUE: "\u2022\u2022\u2022\u2022\u2022\u2022\u2022",

    constructor: function(){
        var DEFAULT_PASSWORD_VALUE = this.DEFAULT_PASSWORD_VALUE;
        var l10n = this.l10n;
        this.userListLayout = [{
            cells: [[
                {name: l10n.username, field: "username",
                 editor: dojox.grid.editors.Dijit,
                 editorClass: "dijit.form.ValidationTextBox",
                 editorProps: {regExp:".{3,32}", required: true, 
                               invalidMessage: l10n.usernameValid
                              },
                 cellClasses: "rootNoChange"
                },
                {name: l10n.firstName, field: "firstName",
                 editor: dojox.grid.editors.Dijit,
                 editorClass: "dijit.form.ValidationTextBox",
                 editorProps: {regExp:".{1,128}", required: true,
                               invalidMessage: l10n.firstNameValid
                              },
                 cellClasses: "rootNoChange"
                },
                {name: l10n.lastName, field: "lastName",
                 editor: dojox.grid.editors.Dijit,
                 editorClass: "dijit.form.ValidationTextBox",
                 editorProps: {regExp:".{1,128}", required: true,
                               invalidMessage: l10n.lastNameValid
                              },
                 cellClasses: "rootNoChange"
                },
                {name: l10n.email,  field: "email", width: "10em",
                 editor: dojox.grid.editors.Dijit,
                 editorClass: "dijit.form.ValidationTextBox",
                 editorProps: {regExp:dojox.regexp.emailAddress({allowLocal: true}), required: true,
                               invalidMessage: l10n.emailValid
                              }
                },
                {name: l10n.password, field: "password", 
                 styles: "text-align: center;", value: this.DEFAULT_PASSWORD_VALUE,
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
                {name: l10n.created,  field: "dateCreated", width: "6.5em",  cellClasses: "noChange"},
                {name: l10n.modified,  field: "dateModified", width: "6.5em", cellClasses: "noChange"},
                {name: l10n.locked,  field: "locked", width: "6em", noresize: "true",
                 styles: "text-align: center;", editor: dojox.grid.editors.CheckBox,
                 cellClasses: "rootNoChange"
                },
                {name: l10n.administrator,  field: "administrator", width: "6em", noresize: "true",
                 styles: "text-align: center;", editor: dojox.grid.editors.CheckBox,
                 cellClasses: "rootNoChange"
                },
                {name: l10n.unactivated,  field: "unactivated", width: "6em",
                 cellClasses: "noChange"
                },
                {name: l10n.url,  field: "url", width: "auto",
                 cellClasses: "noChange"
                }
            ]]
        }];
        var userStore = new cosmo.data.UserStore();
        this.store = userStore;
        dojo.connect(userStore, "onSet", function(item, attr, oldVal, newVal){
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
        });
        
        var model = new dojox.grid.data.DojoData(null, null, {rowsPerPage: 20, store: userStore, query: {}});
        this.model = model;
    },

    postCreate: function(){
        var model = this.model;
        dojo.connect(this.userList, "onStyleRow", function(inRow){
            var row = model.getRow(inRow.index);
            if (row && row.username == 'root'){
                inRow.customClasses += " cosmoRootRow";
            }
        });
        dojo.connect(window, "onresize", dojo.hitch(this.userList, this.userList.update));

        this.userList.setStructure(this.userListLayout);
        this.userList.setModel(model);
    }
} 
);
