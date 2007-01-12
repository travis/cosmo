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
    
    this.accountInfo = null;
    this.fieldList = []; 
    this.fillInFieldValues = function (type, data, resp) {
        this.accountInfo = data;
        this.showDialog();
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
            var f = function (type, data, resp) { self.fillInFieldValues(type, data, resp); };
            var hand = { load: f, error: f };
            cosmo.cmp.cmpProxy.getAccount(hand, true);
            return;
        }
       
        this.fieldList = cosmo.account.getFieldList(this.accountInfo); 
        
        form = cosmo.account.getFormTable(this.fieldList, false);
        
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
        tabContent.innerHTML = 'This is the content for About Cosmo';
        tabs.push({ label: tabLabel, content: tabContent });
        
        o.width = 540;
        o.height = 380;
        o.title = '';
        o.prompt = '';
        
        var self = this;
        c = dojo.widget.createWidget("cosmo:TabContainer", { tabs: tabs }, s, 'last');
        s.removeChild(c.domNode);
        o.content = c;
        b = new cosmo.ui.button.Button({ text:_('App.Button.Close'), width:60, small: true,
            handleOnClick: function () { cosmo.app.modalDialog.hide(); } });
        o.btnsLeft = [b];
        b = new cosmo.ui.button.Button({ text:_('App.Button.Save'), width:60, small: true,
            handleOnClick: function () { self.submitSave.apply(self); } });
        o.btnsRight = [b];
        o.defaultAction = function () { alert('Hi there'); };
        
        cosmo.app.modalDialog.show(o);
    }
    this.submitSave = function () {
        // Validate the form input using each field's
        // attached validators
        var fieldList = this.fieldList;
        var err = cosmo.account.validateForm(form, fieldList, false);
        
        if (err) {
            //cosmo.app.modalDialog.setPrompt(err);
        }
        else {
            //var hand = { load: handleCreateResult, error: handleCreateResult };
            var user = {};
            // Create a hash from the form field values
            for (var i = 1; i < fieldList.length; i++) {
                f = fieldList[i];
                user[f.elemName] = form[f.elemName].value;
            }
            alert('Save function not implemented.');
            // Hand off to CMP
            //cosmo.cmp.cmpProxy.signup(user, hand);
        }
        
    };
};
