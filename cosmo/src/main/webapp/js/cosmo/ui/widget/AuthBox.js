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

        // Attach points
        usernameInput: null,
        passwordInput: null,

        _showErr: function (str) {
            this._showPrompt(str, 'error');
        },
        _showPrompt: function (str, type) {
            cosmo.app.modalDialog.setPrompt(str, type);
        },

        _handleAuthSuccess: function(type, str, obj){
                // Auth failed -- bad password? Reset for retry
                if (str == cosmo.env.getBaseUrl() + "/loginfailed"){
                    this._showErr(_('Login.Error.AuthFailed'));
                    this.passwordInput.value = '';
                }
                // Auth successful -- try to do whatever action
                // was contingent on the auth
                else {
                    cosmo.util.auth.setCred(this.usernameInput.value,
                                            this.passwordInput.value);

                    this.attemptAuthAction(this.authAction.attemptParams);
                }
        },

        _handleAuthError: function(type, str, obj){
            cosmo.app.hideDialog();
            cosmo.app.showErr(data.message);
            return false;
        },

        _attemptOrHandle: function (type, args) {
            var res = null;
            var f = this.authAction[type + 'Func'];
            var a = args || []; // Can't pass args to IE6 if it's undefined
            var context = null;
            // If this is just a plain ol' function, execute in window context
            if (this.authAction.execInline) {
                context = window;
            }
            // If execution context got passed in, apply the method
            // to that object
            else if (this.authAction.execContext) {
                context = this.authAction.execContext;
            }
            // No execution context -- execute the method in the
            // context of the AuthBox itself
            else {
                context = this;
            }
            res = f.apply(context, a);
            return res;
        },
        attemptAuthAction: function (args) {
            // If an informational prompt for the action was
            // specified, display it
            if (this.authAction.attemptPrompt) {
                this._showPrompt(this.authAction.attemptPrompt);
            }
            // Take whatever action was specified in the authAction obj
            return this._attemptOrHandle('attempt', args);
        },
        handleAuthActionResp: function () {
            var args = Array.prototype.slice.apply(arguments);
            // Take whatever response (if any) to the action was specified
            // in the authAction obj
            return this._attemptOrHandle('success', args);
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
                    un, pw, {load: dojo.lang.hitch(this, "_handleAuthSuccess"),
                             error: dojo.lang.hitch(this, "_handleAuthError")}
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
        },

        postCreate: function () {
        }
    },
    "html");

cosmo.ui.widget.AuthBox.getInitProperties = function ( /* Object */ authAction) {
    var initPrompt = authAction.authInitPrompt || _('Login.Prompt.Init')
    var s = document.createElement('span');
    var c = dojo.widget.createWidget("cosmo:AuthBox", {
        'authAction': authAction }, s, 'last');
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
    return { prompt: initPrompt,
        content: c,
        height: 200,
        width: 360,
        btnsLeft: [cancelButton],
        btnsRight: [submitButton],
        defaultAction: function () { c.doAuth.apply(c) } };
};

cosmo.ui.widget.AuthBox.getSuccessProperties = function ( /* String */ message) {
    var s = document.createElement('span');
    var closeButton = dojo.widget.createWidget("cosmo:Button", {
        text: _("App.Button.Close"),
        width: '60px',
        handleOnClick: cosmo.app.hideDialog,
        small: true }, s, 'last');
    s.removeChild(closeButton.domNode);
    return { prompt: '',
        content: message,
        height: 200,
        width: 360,
        btnsCenter: [closeButton],
        defaultAction: cosmo.app.hideDialog };
};

