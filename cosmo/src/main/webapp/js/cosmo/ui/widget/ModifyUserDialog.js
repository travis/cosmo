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
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("dojo.validate.web");
dojo.require("dojo.event");
dojo.require("dojo.lang");
dojo.require("cosmo.convenience");

dojo.widget.defineWidget("cosmo.ui.widget.ModifyUserDialog", dojo.widget.HtmlWidget,
    {
        templatePath : dojo.uri.dojoUri( "../../cosmo/ui/widget/templates/ModifyUserDialog/ModifyUserDialog.html"),
        templateCssPath : dojo.uri.dojoUri("../../cosmo/ui/widget/templates/ModifyUserDialog/ModifyUserDialog.css"),

        // Programmatic widget creation disabled because of problems in safari. Reenable when
        // this is fixed
        widgetsInTemplate:true,

        // Set in user HTML
        header : "",
        disableCancel : false,

        usernameLabel : _("ModifyUser.Username"),
        firstNameLabel : _("ModifyUser.FirstName"),
        lastNameLabel : _("ModifyUser.LastName"),
        emailLabel : _("ModifyUser.Email"),
        passwordBlurb : _("ModifyUser.PasswordBlurb"),
        passwordLabel : _("ModifyUser.Password"),
        confirmLabel : _("ModifyUser.Confirm"),
        adminLabel : _("ModifyUser.Admin"),
        lockedLabel: _("ModifyUser.Locked"),
        
        usernameError : null,
        firstNameError : null,
        lastNameError : null,
        emailError : null,
        passwordError : null,
        confirmError : null,
        
        classes: "",
        title: "",

        cancelButtonText : _("ModifyUser.Button.Cancel"),
        submitButtonText : _("ModifyUser.Button.Submit"),

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
                              admin: true,
                              locked: true
                          },

        editingUser : null,
        postActionHandlerDict : {handle: function(){}},

        hideInputs : function(){
            var inputs = this.removeInputs.split(",");

            for (i = 0; i < inputs.length; i++){
                this.enabledInputs[inputs[i]] = false;

                this[inputs[i] + "InputRow"].style.visibility = 'hidden';
            }
        },

        setupButtons : function(){

            // Programmatic subwidget creation should be phased out once safari supports it.
//            var button = dojo.widget.createWidget("cosmo:Button",
//                    {text:this.submitButtonText,
//                     small:true,
//                     widgetId: this.widgetId + "SubmitButton"});
//

//            dojo.dom.prependChild(button.domNode, this.submitButton.parentNode);
//            dojo.dom.removeNode(this.submitButton);
//            this.submitButton = button;


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
            
            // Make sure we reset errors before submitting.
            dojo.event.connect("before", this, "modifyUser", this, "clearErrors");
            dojo.event.connect("before", this, "createUser", this, "clearErrors");
            dojo.event.connect("before", this, "signupUser", this, "clearErrors");

            if (this.disableCancel) {
                dojo.dom.removeNode(this.cancelButton);
            } else {
//                var button = dojo.widget.createWidget("cosmo:Button",
//                        {text:this.cancelButtonText,
//                         small:true,
//                         widgetId: this.widgetId + "CancelButton"});

//                dojo.dom.prependChild(button.domNode, this.cancelButton.parentNode);
//                dojo.dom.removeNode(this.cancelButton);

//                this.cancelButton = button;

                dojo.event.connect(this.cancelButton, "handleOnClick", this, "cancelAction");
            }

        },
        
        fillInTemplate: function(){

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
            this.clearErrors();
            this.hide();
        },
        
        populateFields : function(populateUsername){
            // username only needed if logged in as administrator

            var handlerDict = {
                load: dojo.lang.hitch(this, function(type, data, evt){
                    
                    if (evt.status == 200){
                        var user = data;
                        this.editingUser = user
                        this.usernameInput.value = user.username;
                        
                        this.firstNameInput.value = user.firstName;
                        this.lastNameInput.value = user.lastName;
                        this.emailInput.value = user.email;
                        
                        this.adminInput.checked = user.administrator;
                        this.lockedInput.checked = user.locked;
                        
                        var overlord = (user.username == cosmo.env.OVERLORD_USERNAME)
                        this.usernameInput.disabled = overlord;
                        this.firstNameInput.disabled = overlord;
                        this.lastNameInput.disabled = overlord;
                        this.adminInput.disabled = overlord;
                        this.lockedInput.disabled = overlord;
                    }
			    }),
                
                error:  function(type, data, evt){
                    if (evt.status == 404){
                        alert("User does not exist");
                    } else {
                        throw new Error(data);
                    }
                }
            }
			
            if (populateUsername){
                   cosmo.cmp.getUser(populateUsername, handlerDict);
            } else {
                cosmo.cmp.getAccount(handlerDict);
            }
        },
        
        validateFields : function(){
			var usernameValid = this.validateUsername();
			var emailValid = this.validateEmail();
			var firstNameValid = this.validateFirstName();
			var lastNameValid = this.validateLastName();
			var passwordValid = this.validatePassword();
			var confirmValid = this.validateConfirm();
			return usernameValid &&
					emailValid &&
					firstNameValid &&
					lastNameValid &&
					passwordValid &&
					confirmValid;
        	
	    },
	    
	    clearErrors : function(){
	    	this.usernameError.innerHTML = "";
	    	this.firstNameError.innerHTML = "";
	    	this.lastNameError.innerHTML = "";
	    	this.emailError.innerHTML = "";
	    	this.passwordError.innerHTML = "";
	    	this.confirmError.innerHTML = "";
	    },
	    
	    validateUsername : function(){
	    	var username = this.usernameInput.value;
	    	if (username == ""){
	    		this.usernameError.innerHTML = _("Signup.Error.RequiredField");
	    		return false;
	    	}
	    	if (username.length < 3 || username.length > 32){
	    		this.usernameError.innerHTML = _("Signup.Error.UsernameInvalidLength");
	    		return false;
	    	}
	    	return true;
	    },

	    validateFirstName : function(){
	    	var firstName = this.firstNameInput.value;
	    	if (firstName == ""){
	    		this.firstNameError.innerHTML = _("Signup.Error.RequiredField");
	    		return false;
	    	}
	    	if (firstName.length < 1 || firstName.length > 128){
	    		this.firstNameError.innerHTML = _("Signup.Error.FirstNameInvalidLength");
	    		return false;
	    	}
	    	return true;
	    },

	    validateLastName : function(){
	    	var lastName = this.lastNameInput.value;
	    	if (lastName == ""){
	    		this.lastNameError.innerHTML = _("Signup.Error.RequiredField");
	    		return false;
	    	}
	    	if (lastName.length < 1 || lastName.length > 128){
	    		this.firstNameError.innerHTML = _("Signup.Error.LastNameInvalidLength");
	    		return false;
	    	}
	    	return true;
	    },

	    validateEmail : function(){
	    	var email = this.emailInput.value;
	    	if (email == ""){
	    		this.emailError.innerHTML = _("Signup.Error.RequiredField");
	    		return false;
	    	}
	    	if (!dojo.validate.isEmailAddress(email, {allowLocal: true})){
	    		this.emailError.innerHTML = _("Signup.Error.ValidEMail");
	    		return false;
	    	}
	    	return true;
	    },

	    validatePassword : function(){
	    	var password = this.passwordInput.value;
	    	if ((password.length < 5 || password.length > 16)
	    		&& password != ""){
	    		this.passwordError.innerHTML = _("Signup.Error.PasswordInvalidLength");
	    		return false;
	    	}
	    	return true;
	    },

	    validateConfirm : function(){
	    	var password = this.passwordInput.value;
	    	var confirm = this.confirmInput.value;
	    	if (password != confirm){
	    		this.confirmError.innerHTML = _("Signup.Error.MatchPassword");
	    		return false;
	    	}
	    	return true;
	    },

        modifyUser : function(){
        	if (!this.validateFields()){
        		return;
        	}

            var userHash = {};
            var user = this.editingUser;

            if (user.username != this.usernameInput.value
                && this.enabledInputs.username
                && this.role == cosmo.ROLE_ADMINISTRATOR){
                userHash.username = this.usernameInput.value;
            }
            if (user.firstName != this.firstNameInput.value
                && this.enabledInputs.firstName){
                userHash.firstName = this.firstNameInput.value;
            }
            if (user.lastName != this.lastNameInput.value
                && this.enabledInputs.lastName){
                userHash.lastName = this.lastNameInput.value;
            }
            if (user.email != this.emailInput.value
                && this.enabledInputs.email){
                userHash.email = this.emailInput.value;
            }
            if (this.passwordInput.value != ""
                && this.enabledInputs.password){
                userHash.password = this.passwordInput.value;
            }
            if (user.administrator != this.adminInput.checked
            	&& this.enabledInputs.admin){
            	userHash.administrator = this.adminInput.checked;
          	}
            if (user.locked != this.lockedInput.checked
            	&& this.enabledInputs.locked){
            	userHash.locked = this.lockedInput.checked;
          	}
            if (this.role == cosmo.ROLE_ADMINISTRATOR){
                cosmo.cmp.modifyUser(this.editingUser.username, userHash, this.postActionHandlerDict)
            } else if (this.role == cosmo.ROLE_AUTHENTICATED){
                cosmo.cmp.modifyAccount(userHash, this.postActionHandlerDict)
            }
        },

        userHashFromForm : function(form){
            var userHash = {username : this.usernameInput.value,
                        password : this.passwordInput.value,
                        firstName : this.firstNameInput.value,
                        lastName : this.lastNameInput.value,
                        email : this.emailInput.value};

            userHash.administrator = this.adminInput.checked;
            userHash.locked = this.lockedInput.checked;
            return userHash;
        },

        signupUser : function(){
         	if (!this.validateFields()){
        		return;
        	}
        	
            userHash = this.userHashFromForm(this.form);

            cosmo.cmp.signup(
                userHash,
                this.postActionHandlerDict
            );

        },

        createUser : function(){
         	if (!this.validateFields()){
        		return;
        	}
            
            var self = this;

            //Check if user exists
            cosmo.cmp.headUser(this.usernameInput.value,
                {handle : function(type, data, evt){

                    // a 404 means the user does not exist, so let's create it
                    if (evt.status == 404){

                        userHash = self.userHashFromForm(self.form)

                        cosmo.cmp.createUser(
                                    userHash,
                                    self.postActionHandlerDict
                                    )

                    // A 200 means the user exists
                    } else if (evt.status == 200){

                        self.usernameError.innerHTML = _("ModifyUser.Error.UsernameInUse");
                    }
                 }
                 })
        }
    },
    "html" ,
    function (){


    }



)