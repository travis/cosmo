dojo.provide("cosmo.ui.widget.LoginDialog");

dojo.require("dojo.io.*");
dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.util.cookie");
dojo.require("cosmo.account.login");
dojo.require("cosmo.convenience");

dojo.widget.defineWidget("cosmo.ui.widget.LoginDialog", dojo.widget.HtmlWidget,
    {
        stylesheet: "",
        templatePath: dojo.uri.dojoUri( "../../cosmo/ui/widget/templates/LoginDialog/LoginDialog.html"),

        // Props from template or set in constructor
        authProc: cosmo.env.getAuthProc(),
        passwordLabel: _("Login.Password"),
        usernameLabel: _("Login.Username"),
        loginPrompt: _("Login.Prompt.Init"),
        redirectHome: true,

        // Attach points
        loginPromptContainer: null,
        usernameLabelContainer: null,
        passwordLabelContainer: null,
        passwordInput: null,
        usernameInput: null,
        loginForm: null,
        submitButtonContainer: null,
        logoContainer: null,

        _usernameFocus: false,
        _passwordFocus: false,

        handleLoginSuccess: function (type, str, evt) {

                var username  = this.usernameInput.value;
                cosmo.util.auth.setCred(this.usernameInput.value,
                    this.passwordInput.value);

                location = str;
        },

        handleLoginError: function (type, str, evt){
                this.showErr(_('Login.Error.AuthFailed'));
                this.passwordInput.value = '';
        },
                         
        doLogin: function () {
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
                self.showPrompt('normal', _('Login.Prompt.Processing'));
                cosmo.account.login.doLogin(
                    un, pw, 
                    {load: dojo.lang.hitch(this, this.handleLoginSuccess),
                     error: dojo.lang.hitch(this, this.handleLoginError) 
                    });
            }
            return false;
        },
        showErr: function (str) {
            this.showPrompt('error', str);
        },
        showPrompt: function (promptType, str) {
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
        keyUpHandler: function (e) {
            e = !e ? window.event : e;
            if (e.keyCode == 13) {
                if (cosmo.app.modalDialog.isDisplayed) {
                    cosmo.app.modalDialog.defaultAction();
                }
                else if (this._usernameFocus || this._passwordFocus)  {
                    this.doLogin();
                    return false;
                }
            }
        },
        setFocus: function (e) {
            // Toggle values for _usernameFocus, _passwordFocus
            t = e.target.id || '';
            if (t) {
                var f = e.type == 'focus' ? true : false;
                t = t.toLowerCase();
                t = t.replace('logindialog', '');
                t = t.replace('input', '');
                this['_' + t + 'Focus'] = f;
            }
        },
        postCreate: function () {
            var self = this;
            var button = dojo.widget.createWidget("cosmo:Button",
                { text: _("Login.Button.Ok"), width: 74, widgetId: "loginSubmitButton" } );
            var logo = document.createElement('img');

            this.submitButtonContainer.appendChild(button.domNode);
            this.submitButton = button;

            logo.src = cosmo.env.getImageUrl(_("App.LogoUri"));
            this.logoContainer.appendChild(logo);
            dojo.event.connect(this.passwordInput, "onfocus", this, 'setFocus');
            dojo.event.connect(this.passwordInput, "onblur", this, 'setFocus');
            dojo.event.connect(this.usernameInput, "onfocus", this, 'setFocus');
            dojo.event.connect(this.usernameInput, "onblur", this, 'setFocus');
            dojo.event.connect(this.submitButton, "handleOnClick", this, "doLogin");
            dojo.addOnLoad(function(){self.usernameInput.focus()})
        },
        setStyle: function (){
            var stylesheetName = dojo.string.capitalize(this.widgetId);
        }
    },
    "html" ,
    function (){
        dojo.event.connect("after", this, "mixInProperties", this, "setStyle");
        dojo.event.connect("after", document, "onkeyup", this, "keyUpHandler");
    }
);
