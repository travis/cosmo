dojo.provide("cosmo.ui.widget.AuthBox");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.convenience");
dojo.require("cosmo.account.login");

dojo.declare(
    "cosmo.ui.widget.AuthBox",
    [dijit._Widget, dijit._Templated],
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

        _authSuccessCB: function(data){
            this.deferred.callback(data);
        },

        _authErrorCB: function(err){
            this._handleError(err);
            return err;
        },

        _handleError: function(err){
            this._showErr(err.message);
            if (!this.noRetry){
                this.passwordInput.value = '';
            } else {
                this.deferred.errback(err);
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
                var d = cosmo.account.login.doLogin(un, pw);
                d.addCallback(dojo.hitch(this, "_authSuccessCB"));
                d.addErrback(dojo.hitch(this, "_authErrorCB"));

            }
            return false;
        },

        postCreate: function () {
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
            dojo.connect(signupLink, "onclick",
                               dojo.hitch(this, function(){
                                   cosmo.account.create.showForm(this.subscription);
                                   return false;
                               })
                              );
            signupLinkDiv.appendChild(signupLink);
            this.domNode.appendChild(signupLinkDiv);
        },

        constructor: function(){
            this.deferred = new dojo.Deferred();
        }
    }
);


cosmo.ui.widget.AuthBox.getInitProperties = function ( /* Object */ authAction) {
    var initPrompt = authAction.authInitPrompt || _('Login.Prompt.Init');

    var c = new cosmo.ui.widget.AuthBox({
        'authAction': authAction,
        'subscription': authAction.subscription});

    var cancelButton = new dijit.form.Button({
        label: _("App.Button.Cancel"),
        width: '60px',
        onClick: cosmo.app.hideDialog,
        small: true });

    var submitButton = new dijit.form.Button({
        label: _("App.Button.Submit"),
        width: '60px',
        onClick: function () { c.doAuth.apply(c); },
        small: true });

    return {prompt: initPrompt,
            content: c,
            height: 200,
            width: 360,
            btnsLeft: [cancelButton],
            btnsRight: [submitButton],
            deferred: c.deferred,
            defaultAction: function () { c.doAuth.apply(c); } };
};
