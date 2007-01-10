dojo.provide("cosmo.ui.widget.AuthBox");

dojo.require("dojo.io.*");
dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.utl.html");
dojo.require("cosmo.ui.widget.Button");

_ = cosmo.util.i18n.getText

dojo.widget.defineWidget("cosmo.ui.widget.AuthBox", dojo.widget.HtmlWidget,
    {
        templateString: '<span></span>',

        // Props from template or set in constructor
        authAction: null, 
        //authProc: "",
        authProc: cosmo.env.getAuthProc(),
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
        _handleAuthResp: function (str) {
            /*
            Login page recognition string: login-page-2ksw083judrmru58
            This is an ugly hack to allow the AJAX handler to recognize
            this page. In previous versions of Cosmo, this was done by
            detecting the name of the login.js file, so I'd actually
            call this an improvment.
           
            Authentication in general should be rethought soon. 
            */
            // Auth failed -- bad password? Reset for retry
            if (str.indexOf('login-page-2ksw083judrmru58') > -1){
                this._showErr(_('Login.Error.AuthFailed'));
                this.passwordInput.value = '';
            }
            // Auth successful -- try to do whatever action
            // was contingent on the auth
            else {
                this.attemptAuthAction(this.authAction.attemptParams);
            }
        },
        _attemptOrHandle: function (type, args) {
            var res = null;
            // If this is just a plain ol' function, execute it inline here
            if (this.authAction.execInline) {
                res = this.authAction[type + 'Func'].apply(null, args);
            }
            // Otherwise it's a method to execute in an obj context 
            else {
                // If execution context got passed in, apply the method
                // to that object
                if (this.authAction.execContext) {
                    res = this.authAction[type + 'Func'].apply(this.authAction.execContext, args);
                }
                // No execution context -- execute the method in the
                // context of the AuthBox itself
                else {
                    res = this.authAction[type + 'Func'].apply(this, args);
                }
            }
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

                postData = { 'j_username': un, 'j_password': pw };

                var self = this;
                dojo.io.bind({
                    url: self.authProc,
                    method: 'POST',
                    content: postData,
                    load: function(type, data, obj) { self._handleAuthResp(data); },
                    error: function(type, error) { alert(error.message); }
                });
            }
            return false;
        },
        fillInTemplate: function () {
            var _ = cosmo.util.i18n.getText
            var _createElem = function (str) { return document.createElement(str); }; 
            var _createText = function (str) { return document.createTextNode(str); }; 
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
        text: getText("App.Button.Cancel"),
        width: '60px',
        handleOnClick: cosmo.app.hideDialog,
        small: true }, s, 'last');
    s.removeChild(cancelButton.domNode); 
    var submitButton = dojo.widget.createWidget("cosmo:Button", { 
        text: getText("App.Button.Submit"),
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
        text: getText("App.Button.Close"),
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

