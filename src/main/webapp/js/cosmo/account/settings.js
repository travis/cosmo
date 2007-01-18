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

dojo.require("dojo.html.common");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("cosmo.cmp");
dojo.require("cosmo.util.validate");
dojo.require("cosmo.ui.widget.TabContainer");

dojo.provide("cosmo.account.settings");

cosmo.account.settings = new function () {
    
    var self = this; // Stash a copy of this
    var form = null; // The form containing the signup fields
    var f = null; // Temp var
    // Localized strings
    var strings = {};
    strings['passwordBlank'] = _('AccountSettings.Prompt.BlankPassword');
    strings['settingsErrorLoad'] = _('AccountSettings.Error.Load');
    strings['settingsErrorUpdate'] = _('AccountSettings.Error.Update');

    this.accountInfo = null;
    this.fieldList = []; 
    this.accountInfoLoadSuccess = function (type, data, resp) {
        this.accountInfo = data;
        this.showDialog();
    };
    this.accountInfoLoadError = function (type, data, resp) {
        var err = strings['settingsErrorLoad'];
        cosmo.app.showErr(err, data);
    };
    this.showDialog = function () {
        var o = {};
        var b = null; var c = null;
        var s = document.createElement('span');
        var tabs = [];
        var tabLabel = '';
        var tabContent = null;

        if (!this.accountInfo) {
            var self = this;
            var success = function (type, data, resp) { self.accountInfoLoadSuccess(type, data, resp); };
            var error = function (type, data, resp) { self.accountInfoLoadError(type, data, resp); };
            var hand = { load: success, error: error };
            cosmo.cmp.cmpProxy.getAccount(hand, true);
            return;
        }
       
        this.fieldList = cosmo.account.getFieldList(this.accountInfo); 
        
        form = cosmo.account.getFormTable(this.fieldList, false);

        var passCell = form.password.parentNode;
        var d = null;
        var pass = passCell.removeChild(form.password);
        d = _createElem('div');
        d.className = 'floatLeft';
        d.style.width = '40%';
        d.appendChild(pass);
        passCell.appendChild(d);
        d = _createElem('div');
        d.className = 'promptText floatLeft';
        d.style.width = '59%';
        d.style.paddingLeft = '4px'
        d.innerHTML = strings['passwordBlank'];
        passCell.appendChild(d);
        d = _createElem('div');
        d.className = 'clearBoth';
        passCell.appendChild(d);
        
        tabLabel = 'General';
        tabContent = _createElem('div');
        tabContent.appendChild(form);
        tabs.push({ label: tabLabel, content: tabContent });
        
        tabLabel = 'Advanced';
        tabContent = _createElem('div');
        tabContent.innerHTML = 'This is the content for the advanced preferences';
        tabs.push({ label: tabLabel, content: tabContent });
        
        
        tabLabel = 'About Cosmo';
        tabContent = _createElem('div');
        tabContent.style.textAlign = 'center';
        tabContent.style.margin = 'auto';
        tabContent.style.width = '100%';
        
        d = _createElem('div');
        d.appendChild(_createText('Cosmo'));
        d.className = 'labelTextXL';
        tabContent.appendChild(d);
        
        d = _createElem('div');
        d.appendChild(_createText(cosmo.env.getVersion()));
        tabContent.appendChild(d);
        
        tabs.push({ label: tabLabel, content: tabContent });
        
        o.width = 580;
        o.height = 380;
        o.title = 'Settings';
        o.prompt = '';
        
        var self = this;
        var f = function () { self.submitSave.apply(self); };
        c = dojo.widget.createWidget("cosmo:TabContainer", { tabs: tabs }, s, 'last');
        s.removeChild(c.domNode);
        o.content = c;
        b = new cosmo.ui.button.Button({ text:_('App.Button.Close'), width:60, small: true,
            handleOnClick: function () { cosmo.app.hideDialog(); } });
        o.btnsLeft = [b];
        b = new cosmo.ui.button.Button({ text:_('App.Button.Save'), width:60, small: true,
            handleOnClick: f });
        o.btnsRight = [b];
        o.defaultAction = f;
        cosmo.app.showDialog(o);
    }
    this.submitSave = function () {
        // Validate the form input using each field's
        // attached validators
        var fieldList = this.fieldList;
        var err = cosmo.account.validateForm(form, fieldList, false);
        
        if (err) {
            // Do nothing
        }
        else {
            var self = this;
            var f = function (type, data, resp) { self.handleAccountSave(type, data, resp); };
            var hand = { load: f, error: f };
            var account = {};
            // Create a hash from the form field values
            for (var i = 0; i < fieldList.length; i++) {
                var f = fieldList[i];
                var val = form[f.elemName].value;
                if (val) {
                    account[f.elemName] = val;
                }
            }
            delete account.confirm;
            
            // Only set the property at all if it's initially true
            // 'administrator' is an empty tag -- its presence will 
            if (this.accountInfo.administrator) {
                account.administrator = true;
            };
            // Hand off to CMP
            cosmo.cmp.cmpProxy.modifyAccount(account, hand);
        }
        
    };
    this.handleAccountSave = function (type, data, resp) {
        var stat = resp.status;
        var err = '';
        // Add bogus 1223 HTTP status from 204s in IE as a success code
        if ((stat > 199 && stat < 300) || (stat == 1223)) {
            // Success
        }
        else {
            err = strings['settingsErrorUpdate'];
        }
        this.accountInfo = null;
        cosmo.app.hideDialog();
        if (err) {
            cosmo.app.showErr(err, data);
        }
    };
};
