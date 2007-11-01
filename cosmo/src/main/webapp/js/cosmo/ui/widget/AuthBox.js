dojo.provide("cosmo.ui.widget.AuthBox");

dojo.require("dojo.io.*");
dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.convenience");
dojo.require("cosmo.account.login");

dojo.widget.defineWidget("cosmo.ui.widget.AuthBox", dojo.widget.HtmlWidget,
    {
        templateString: '<span></span>',

        // Props from template or set in constructor
        authAction: null,
        usernameLabel: _("Login.Username"),
        passwordLabel: _("Login.Password"),
        // Clients can pass a subscription
        // that will be passed to the signup
        // dialog if users decide to create a new 
        // account instead of log in.
        subscription: null,

        // Attach points
        usernameInput: null,
        passwordInput: null,

        // Auth deferred; fires callback on auth success
        //    fires errback on auth fail if noRetry is true
        deferred: null,
        noRetry: false,

        _showErr: function (str) {
            this._showPrompt(str, 'error');
        },
        _showPrompt: function (str, type) {
            cosmo.app.modalDialog.setPrompt(str, type);
        },

        _authSuccessCB: function(type, data, obj){
            this.deferred.callback(data);
        },

        _authErrorCB: function(type, str, obj){
            this._handleError(str);
            return false;
        },

        _handleError: function(msg){
            this._showErr(msg);
            if (!this.noRetry){
                this.passwordInput.value = '';
            } else {
                this.deferred.errback(msg);
            }
        },

        doAuth: function () {
            var un = this.usernameInput.value;
            var pw = this.passwordInput.value;
            var postData = {};
            var err = '';

            if (!un || !pw) {
                err = _('Login.Error.RequiredFields');
            }

            if (err) {
                this._showErr(err);
            }
            else {
                if (this.authAction.authProcessingPrompt) {
                    this._showPrompt(this.authAction.authPocessingPrompt);
                }
                else {
                    this._showPrompt(_('Login.Prompt.Processing'));
                }
                cosmo.account.login.doLogin(
                    un, pw, {load: dojo.lang.hitch(this, "_authSuccessCB"),
                             error: dojo.lang.hitch(this, "_authErrorCB")}
                );
            }
            return false;
        },

        fillInTemplate: function () {
            var table = _createElem('table');
            var tbody = _createElem('tbody');
            var tr = null;
            var td = null;
            var input = null;

            table.style.width = '240px';
            table.style.margin = 'auto';
            table.style.textAlign = 'center';
            table.appendChild(tbody);

            // Username row
            tr = _createElem('tr');
            // Label
            td = _createElem('td');
            td.className = 'labelTextHoriz labelTextCell';
            td.appendChild(_createText(this.usernameLabel));
            tr.appendChild(td);
            // Input elem
            td = _createElem('td');
            this.usernameInput = cosmo.util.html.createInput({
                type: 'text',
                name: 'authBoxUsernameInput',
                id: 'authBoxUsernameInput',
                className: 'inputText',
                value: '' });
            td.appendChild(this.usernameInput);
            tr.appendChild(td);
            tbody.appendChild(tr);

            // Password row
            tr = _createElem('tr');
            // Label
            td = _createElem('td');
            td.className = 'labelTextHoriz labelTextCell';
            td.appendChild(_createText(this.passwordLabel));
            tr.appendChild(td);
            // Input elem
            td = _createElem('td');
            this.passwordInput = cosmo.util.html.createInput({
                type: 'password',
                name: 'authBoxUsernameInput',
                id: 'authBoxUsernameInput',
                className: 'inputText',
                value: '' });
            td.appendChild(this.passwordInput);
            tr.appendChild(td);
            tbody.appendChild(tr);

            this.domNode.appendChild(table);
            var recoverPasswordDiv = _createElem("div");
            recoverPasswordDiv.className = "authBoxRecoverPassword";
            this.domNode.appendChild(recoverPasswordDiv);
            recoverPasswordDiv.innerHTML = [_("Login.Forgot"), "<a href=", cosmo.env.getFullUrl("ForgotPassword"), 
                                            " target=\"_blank\"> ", _("Login.ClickHere"), "</a>"].join("");

            // Sign up link
            var signupLinkDiv = _createElem("div");
            signupLinkDiv.className = "authBoxSignupLink";
            signupLinkDiv.appendChild(_createText(_("AuthBox.CreateAccount")));

            var signupLink = _createElem("a");
            signupLink.appendChild(_createText(_("AuthBox.CreateClickHere")));
            dojo.event.connect(signupLink, "onclick",  
                               dojo.lang.hitch(this, function(){
                                   cosmo.account.create.showForm(this.subscription);
                                   return false;
                               })
                              );
            signupLinkDiv.appendChild(signupLink);
            this.domNode.appendChild(signupLinkDiv);
        },
        
        initializer: function(){
            this.deferred = new dojo.Deferred();
        },

        postCreate: function () {
        }
    },
    "html");

cosmo.ui.widget.AuthBox.getInitProperties = function ( /* Object */ authAction) {
    var initPrompt = authAction.authInitPrompt || _('Login.Prompt.Init')
    var s = document.createElement('span');
    var c = dojo.widget.createWidget("cosmo:AuthBox", {
        'authAction': authAction, 
        'subscription': authAction.subscription }, 
                                     s, 'last');
    s.removeChild(c.domNode);
    var cancelButton = dojo.widget.createWidget("cosmo:Button", {
        text: _("App.Button.Cancel"),
        width: '60px',
        handleOnClick: cosmo.app.hideDialog,
        small: true }, s, 'last');
    s.removeChild(cancelButton.domNode);
    var submitButton = dojo.widget.createWidget("cosmo:Button", {
        text: _("App.Button.Submit"),
        width: '60px',
        handleOnClick: function () { c.doAuth.apply(c) },
        small: true }, s, 'last');
    s.removeChild(submitButton.domNode);

    return {prompt: initPrompt,
            content: c,
            height: 200,
            width: 360,
            btnsLeft: [cancelButton],
            btnsRight: [submitButton],
            deferred: c.deferred,
            defaultAction: function () { c.doAuth.apply(c) } };
};
