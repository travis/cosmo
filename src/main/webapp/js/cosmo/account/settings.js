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

dojo.require("dojo.event.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.convenience");
dojo.require("cosmo.ui.widget.TabContainer");

dojo.provide("cosmo.account.settings");

cosmo.account.settings = new function () {
    this.showDialog = function () {
        var o = {};
        var b = null;
        var c = null;
        var s = document.createElement('span');
        var tabs = [];
        var tabLabel = '';
        var tabContent = null;
        
        tabLabel = 'General';
        tabContent = _createElem('div');
        tabContent.innerHTML = 'This is the content for the general preferences';
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
        
        c = dojo.widget.createWidget("cosmo:TabContainer", { tabs: tabs }, s, 'last');
        s.removeChild(c.domNode);
        o.content = c;
        b = new cosmo.ui.button.Button({ text:_('App.Button.Close'), width:60, small: true,
            handleOnClick: function () { cosmo.app.modalDialog.hide(); } });
        o.btnsLeft = [b];
        b = new cosmo.ui.button.Button({ text:_('App.Button.Submit'), width:60, small: true,
            handleOnClick: function () { alert('Howdy') } });
        o.btnsRight = [b];
        o.defaultAction = function () { alert('Hi there'); };
        
        cosmo.app.modalDialog.show(o);
    }
    
};
