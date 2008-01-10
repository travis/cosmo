dojo.provide("cosmo.ui.widget.LoginDialog");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.util.cookie");
dojo.require("cosmo.account.login");
dojo.require("cosmo.convenience");

dojo.declare(
    "cosmo.ui.widget.LoginDialog", 
    [dijit._Widget, dijit._Templated],
    {
        stylesheet: "",
        templatePath: dojo.moduleUrl("cosmo", "../../cosmo/ui/widget/templates/LoginDialog/LoginDialog.html"),

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

        handleLoginSuccess: function (str) {

                var username  = this.usernameInput.value;
                cosmo.util.auth.setCred(this.usernameInput.value,
                    this.passwordInput.value);

                location = str;
        },

        handleLoginError: function (err){
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
                var d = cosmo.account.login.doLogin(
                    un, pw 
                    );
                d.addCallback(dojo.hitch(this, this.handleLoginSuccess));
                d.addErrback(dojo.hitch(this, this.handleLoginError)); 

            }
            return false;
        },
        showErr: function (str) {
            this.showPrompt('error', str);
        },
        showPrompt: function (promptType, str) {
            var promptDiv = this.loginPromptContainer;
            if (promptType.toLowerCase() == 'error') {
                dojo.removeClass(promptDiv, 'promptText')
                dojo.addClass(promptDiv, 'promptTextError')
            }
            else {
                dojo.removeClass(promptDiv, 'promptTextError')
                dojo.addClass(promptDiv, 'promptText')
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
            dojo.connect(this.passwordInput, "onfocus", this, 'setFocus');
            dojo.connect(this.passwordInput, "onblur", this, 'setFocus');
            dojo.connect(this.usernameInput, "onfocus", this, 'setFocus');
            dojo.connect(this.usernameInput, "onblur", this, 'setFocus');
            dojo.connect(this.submitButton, "handleOnClick", this, "doLogin");
            dojo.addOnLoad(function(){self.usernameInput.focus()})
        },
        setStyle: function (){
            var stylesheetName = cosmo.util.string.capitalize(this.widgetId);
        },
        constructor: function (){
            dojo.connect(this, "mixInProperties", this, "setStyle");
            dojo.connect(document, "onkeyup", this, "keyUpHandler");
        }
    }
);
