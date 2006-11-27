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
 * @fileoverview ModifyUserDialog - a form that edits user details via CMP
 * @author Travis Vachon travis@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.ModifyUserDialog");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.cmp");
dojo.require("cosmo.env");

dojo.widget.defineWidget("cosmo.ui.widget.ModifyUserDialog", dojo.widget.HtmlWidget,
    {
        templatePath : dojo.uri.dojoUri( "../../cosmo/ui/widget/templates/ModifyUserDialog/ModifyUserDialog.html"),
           templateCssPath : dojo.uri.dojoUri("../../cosmo/ui/widget/templates/ModifyUserDialog/ModifyUserDialog.css"),

        // Programmatic widget creation disabled because of problems in safari. Reenable when
        // this is fixed
        //widgetsInTemplate:true,

        // Set in user HTML
        header : "",
        disableCancel : false,

        usernameLabel : "Username:",
        firstNameLabel : "First name:",
        lastNameLabel : "Last name:",
        emailLabel : "Email:",
        passwordBlurb : "",
        passwordLabel : "Password:",
        confirmLabel : "Confirm",
        adminLabel : "Admin?",

        cancelButtonText : "Cancel",
        submitButtonText : "Submit",

        removeInputs : "",

        createNew : false,
        role: cosmo.ROLE_ANONYMOUS,

        postActionHandler : "",

        populateOnLoad : false,

        // Attach points
        testAttach : null,
        submitButton : null,
        cancelButton: null,

        // Internal variables

        enabledInputs : {	username: true,
                              firstName: true,
                              lastName: true,
                              email: true,
                              password: true,
                              admin: true
                          },

        editingUsername : null,
        postActionHandlerDict : {handle: function(){}},

        cmpProxy : cosmo.cmp.cmpProxy,

        hideInputs : function(){
            var inputs = this.removeInputs.split(",");

            for (i = 0; i < inputs.length; i++){
                this.enabledInputs[inputs[i]] = false;

                this[inputs[i] + "Input"].style.visibility = 'hidden';
            }
        },

        setupButtons : function(){

            // Programmatic subwidget creation should be phased out once safari supports it.
            var button = dojo.widget.createWidget("cosmo:Button",
                    {text:this.submitButtonText,
                     small:true});


            dojo.dom.prependChild(button.domNode, this.submitButton.parentNode);
            dojo.dom.removeNode(this.submitButton);
            this.submitButton = button;


            if (this.createNew) {
                if (this.role == cosmo.ROLE_ADMINISTRATOR) {
                    dojo.event.connect(this.submitButton, "handleOnClick", this, "createUser");
                } else if (this.role == cosmo.ROLE_ANONYMOUS){
                    dojo.event.connect(this.submitButton, "handleOnClick", this, "signupUser");
                }
            } else {
                if (this.role == cosmo.ROLE_ADMINISTRATOR) {
                    dojo.event.connect(this.submitButton, "handleOnClick", this, "modifyUser");
                } else if (this.role == cosmo.ROLE_AUTHENTICATED){
                    dojo.event.connect(this.submitButton, "handleOnClick", this, "modifyUser");
                }
            }

            if (this.disableCancel) {
                dojo.dom.removeNode(this.cancelButton);
            } else {
                var button = dojo.widget.createWidget("cosmo:Button",
                        {text:this.cancelButtonText,
                         small:true});

                dojo.dom.prependChild(button.domNode, this.cancelButton.parentNode);
                dojo.dom.removeNode(this.cancelButton);

                this.cancelButton = button;

                dojo.event.connect(this.cancelButton, "handleOnClick", this, "cancelAction");
            }

        },

        fillInTemplate: function(){

            // Hide ourselves if specified
            if (this.isHidden){
                this.hide()
            }

            // Hook up the specified callbacks
            if (this.postActionHandler != ""){
                 eval("this.postActionHandlerDict = " + this.postActionHandler);
            }

            // if this.role isn't cosmo.ROLE_ANONYMOUS, it's a string set in the html
            if (this.role != cosmo.ROLE_ANONYMOUS){
                 eval("this.role = " + this.role);
            }

            // Hide the specified inputs
            if (this.removeInputs != ""){
                this.hideInputs();
            }


            // Populate form fields if that's requested
            if (this.populateOnLoad){
                this.populateFields();
            }

        },

        postCreate : function(){

            this.setupButtons();
        },

        cancelAction : function(){
            this.form.reset();
            this.hide();
        },

        populateFields : function(populateUsername){
            // username only needed if logged in as administrator

            var self = this;

            var handlerDict = {
                handle: function(type, data, evt){

                    if (evt.status == 200){

                        var user = data;

                        self.editingUsername = user.username

                        form = self.form;

                        form.username.value = user.username;

                        form.firstName.value = user.firstName;
                        form.lastName.value = user.lastName;
                        form.email.value = user.email;

                        form.admin.checked = user.administrator;

                        overlord = (user.username == cosmo.env.OVERLORD_USERNAME)
                        form.username.disabled = overlord;
                        form.firstName.disabled = overlord;
                        form.lastName.disabled = overlord;
                        form.admin.disabled = overlord;


                    } else if (evt.status == 404){
                        alert("User does not exist");
                    }
                }

            }

            if (populateUsername){
                   cosmo.cmp.cmpProxy.getUser(populateUsername, handlerDict);
            } else {
                cosmo.cmp.cmpProxy.getAccount(handlerDict);
            }
        },

        modifyUser : function(){

            var form = this.form;

            var userHash = {}

            if (user.username != form.username.value
                && this.enabledInputs.username
                && this.role == cosmo.ROLE_ADMINISTRATOR){
                userHash.username = form.username.value;
            }
            if (user.firstName != form.firstName.value
                && this.enabledInputs.firstName){
                userHash.firstName = form.firstName.value;
            }
            if (user.lastName != form.lastName.value
                && this.enabledInputs.lastName){
                userHash.lastName = form.lastName.value;
            }
            if (user.email != form.email.value
                && this.enabledInputs.email){
                userHash.email = form.email.value;
            }
            if (form.password.value != ""
                && this.enabledInputs.password){
                userHash.password = form.password.value;
            }
            if (form.admin.checked){
                userHash.administrator = form.admin.checked;
            }


            if (this.role == cosmo.ROLE_ADMINISTRATOR){
                this.cmpProxy.modifyUser(this.editingUsername, userHash, this.postActionHandlerDict)
            } else if (this.role == cosmo.ROLE_AUTHENTICATED){
                this.cmpProxy.modifyAccount(userHash, this.postActionHandlerDict)
            }
        },

        userHashFromForm : function(form){
            var userHash = {username : form.username.value,
                        password : form.password.value,
                        firstName : form.firstName.value,
                        lastName : form.lastName.value,
                        email : form.email.value};

            if (form.admin.checked){
                userHash.administrator = form.admin.checked;
            }

            return userHash;
        },

        signupUser : function(){
            userHash = this.userHashFromForm(this.form)

            cosmo.cmp.cmpProxy.signup(
                        userHash,
                        this.postActionHandlerDict
                        )

        },

        createUser : function(){
            var self = this

            //Check if user exists
            cosmo.cmp.cmpProxy.headUser(this.form.username.value,
                {handle : function(type, data, evt){

                    // a 404 means the user does not exist, so let's create it
                    if (evt.status == 404){

                        userHash = self.userHashFromForm(self.form)

                        cosmo.cmp.cmpProxy.createUser(
                                    userHash,
                                    self.postActionHandlerDict
                                    )

                    // A 200 means the user exists
                    } else if (evt.status == 200){
                        //TODO: handle user already exists
                        alert("User already exists");
                    }
                 }
                 })
        }
    },
    "html" ,
    function (){


    }



)