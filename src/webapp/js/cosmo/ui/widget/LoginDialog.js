dojo.provide("cosmo.ui.widget.LoginDialog");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.cmp");
dojo.require("cosmo.env");
dojo.require("dojo.io.*");
dojo.require("dojo.widget.*");
dojo.require("cosmo.util.i18n");
dojo.require("dojo.widget.*");
_ = cosmo.util.i18n.getText 

dojo.widget.defineWidget("cosmo.ui.widget.LoginDialog", dojo.widget.HtmlWidget,
	{
		stylesheet : "",

		templatePath : dojo.uri.dojoUri( "../../cosmo/ui/widget/templates/LoginDialog/LoginDialog.html"),
	   	templateCssPath : dojo.uri.dojoUri("../../cosmo/ui/widget/templates/LoginDialog/LoginDialog.css"),
	
		// Subwidgets do not currently work in safari. Re-enable and cleanup once they do.
		//widgetsInTemplate:true,
		
		// Set in user HTML	
		authProc : "",
		passwordPrompt : _("Login.Password"),
		usernamePrompt : _("Login.Username"),
		loginPrompt : _("Login.Prompt"),

		redirectHome : true,

		// Attach points		
		loginPromptContainer : null,
		usernamePromptContainer : null,
		passwordPromptContainer : null,
		passwordInput : null,
		usernameInput : null,
		loginForm : null,
		
		// internal use		
		loginFocus : false,
		
		handleLoginResp : function(str) {
	        if (str.indexOf('login-page-2ksw083judrmru58') > -1){

	            this.showErr(_('Login.Error.AuthFailed'));
	            this.passwordInput.value = ''; 
	        }
	        else {
	
	            var username  = this.usernameInput.value;
	            if (username == cosmo.env.OVERLORD_USERNAME) {
	                location = cosmo.env.getBaseUrl() + "/account/view";
	            } else {
	                location = cosmo.env.getBaseUrl() + "/pim";
	            }
	            cosmo.cmp.setUser(this.usernameInput.value,
								  this.passwordInput.value);
	            
	        }
	    },
	    
	    doLogin : function() {

	    	var self = this;
	        var un = self.usernameInput.value;
	        var pw = self.passwordInput.value;
	        var postData = {};
	        var err = '';
	
	        if (!un || !pw) {
	            err = _('Login.Error.RequiredFields');
	        }
	        if (err) {
	            self.showErr(err);
	        }
	        else {
	            self.showPrompt('normal', 'Logging you on. Please wait ...');
	            Cookie.set('username', un);

	            postData = { 'j_username': un, 'j_password': pw };
	
	            dojo.io.bind({
	                url: self.authProc,
	                method: 'POST',
	                content: postData,
	                load: function(type, data, evt) {self.handleLoginResp(data); },
	                error: function(type, error) { alert(error.message); }
	            });
	        }
	        return false;
	    },

	    showErr : function(str) {
	        this.showPrompt('error', str);
	    },

	    showPrompt : function(promptType, str) {
	        var promptDiv = this.loginPromptContainer;
	        if (promptType.toLowerCase() == 'error') {
	        	dojo.html.removeClass(promptDiv, 'promptText')
	        	dojo.html.addClass(promptDiv, 'promptTextError')
	        }
	        else {
	        	dojo.html.removeClass(promptDiv, 'promptTextError')
	        	dojo.html.addClass(promptDiv, 'promptText')
	        }
	        promptDiv.innerHTML = str;
	    },
	    
	    keyUpHandler : function(e) {
	    	
	       	e = !e ? window.event : e;
	       	if (e.keyCode == 13 && this.loginFocus) {
	       	    this.doLogin();
	        	return false;
	       	}
	    },
	    
		postCreate : function(){
			var self = this;


    		dojo.event.connect(this.passwordInput, "onfocus",function(){self.loginFocus = true});
    		dojo.event.connect(this.passwordInput, "onblur",function(){self.loginFocus = false});


			// Programmatic subwidget creation should be removed once safari supports 
			// widgetsInTemplate
			var button = dojo.widget.createWidget("cosmo:Button", {text:_("Login.Button.Ok")});
	
			dojo.dom.prependChild(button.domNode, this.submitButton.parentNode);
			dojo.dom.removeNode(this.submitButton);
			this.submitButton = button;
    		dojo.event.connect(this.submitButton, "handleOnClick",this, "doLogin");
    					
			dojo.addOnLoad(function(){self.usernameInput.focus()})
 
		},

	    setStyle : function(){
	    	var stylesheetName = dojo.string.capitalize(this.widgetId);
	    }
	    
		
	},
    "html" ,
    function (){
		
		dojo.event.connect("after", this, "mixInProperties", this, "setStyle");
		dojo.event.connect("after", document, "onkeyup", this, "keyUpHandler");


	}

    
    
);