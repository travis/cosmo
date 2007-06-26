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

dojo.provide('cosmo.ui.menu');

dojo.require("dojo.io.cookie");
dojo.require("dojo.event.*");
dojo.require("cosmo.env");
dojo.require('cosmo.app');
dojo.require('cosmo.account.preferences');
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.util.hash");
dojo.require('cosmo.convenience');
dojo.require('cosmo.account.settings');
dojo.require('cosmo.account.create');
dojo.require("cosmo.ui.ContentBox");

cosmo.ui.menu = new function () {
    var self = this;
    this.items = new cosmo.util.hash.Hash();
    this.init = function () {
        this.loadItems();
    }
    this.loadItems = function () {
        var _c = cosmo.ui.menu.MenuItem;
        var items =  cosmo.ui.menu.allItems;
        var prefs = cosmo.app.initParams.authAccess? 
            cosmo.account.preferences.getPreferences() : {};
        // Instantiate all the items as MenuItem obj
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            if (this.isItemInCurrentDisplayMode(item) &&
                this.userHasRequiredRolesForItem(item) &&
                this.userHasRequiredPrefForItem(item, prefs)) {
                this.items.addItem(items[i].id, new _c(items[i]));
            }
        }
    };
    this.isItemInCurrentDisplayMode = function (item) {
        var modes = cosmo.ui.menu.displayModes;
        switch (item.displayMode) {
            case modes.ANY:
                return true;
                break;
            case modes.AUTH:
                return !!(cosmo.app.initParams.authAccess);
                break;
            case modes.ANON:
                return !(cosmo.app.initParams.authAccess);
                break;
        }
        return false;
    };
    this.userHasRequiredRolesForItem = function (item) {
        var roles = item.requiredRoles;
        for (var i = 0; i < roles.length; i++) {
            if (!cosmo.app.initParams[roles[i]]) {
                return false;
            }
        }
        return true;
    };
    this.userHasRequiredPrefForItem = function (item, prefs) {
        var pref = item.requiredPref;
        if (pref) {
            return !!(prefs[pref]);
        }
        else {
            return true;
        }
    };
};

cosmo.ui.menu.displayModes = {
    AUTH: 'authAccess',
    ANON: 'anonAccess',
    ANY: 'anyAccess'
};

cosmo.ui.menu.requiredRoles = {
    USER: 'roleUser',
    ROOT: 'roleRoot'
};

cosmo.ui.menu.urls = {
    HELP: 'http://wiki.osafoundation.org/Projects/CosmoHelpRel0dot6',
    ACCOUNT_BROWSER: cosmo.env.getBaseUrl() + '/browse/' + 
        cosmo.app.currentUsername,
    ADMIN_CONSOLE: cosmo.env.getBaseUrl() + '/admin/users'
};

// A slighly cleaner way to list all the menu item props
// these will all be instantiated as MenuItem objects
// The reason for doing these as keyword/value pairs is
// make it easy to rearrange order, or leave items out
// based on display mode, user permissions, and user prefs
cosmo.ui.menu.allItems = [
    { id: 'welcomeMenuItem',
        displayText: 'Welcome, ' + cosmo.app.currentUsername,
        displayMode: cosmo.ui.menu.displayModes.AUTH,
        requiredRoles: [cosmo.ui.menu.requiredRoles.USER]
        },
    { id: 'adminConsoleMenuItem',
        displayText: _('Main.Console'),
        urlString: cosmo.ui.menu.urls.ADMIN_CONSOLE,
        displayMode: cosmo.ui.menu.displayModes.AUTH,
        requiredRoles: [cosmo.ui.menu.requiredRoles.ROOT]
        },
    { id: 'settingsMenuItem',
        displayText: 'Settings', 
        onclickFunc: function () { cosmo.account.settings.showDialog(); }, 
        displayMode: cosmo.ui.menu.displayModes.AUTH,
        requiredRoles: [cosmo.ui.menu.requiredRoles.USER]
        },
    { id: 'accountBrowserMenuItem',
        displayText: 'Account Browser', 
        onclickFunc: function (e) { 
            window.open(cosmo.ui.menu.urls.ACCOUNT_BROWSER); e.preventDefault(); },
        urlString: cosmo.ui.menu.urls.ACCOUNT_BROWSER,
        displayMode: cosmo.ui.menu.displayModes.AUTH,
        requiredRoles: [cosmo.ui.menu.requiredRoles.USER]
        //requiredPref: cosmo.account.preferences.SHOW_ACCOUNT_BROWSER_LINK
        },
    { id: 'signupMenuItem',
        displayText: 'Sign Up!', 
        onclickFunc: function () { cosmo.account.create.showForm() }, 
        displayMode: cosmo.ui.menu.displayModes.ANON,
        requiredRoles: []
        },
    { id: 'helpMenuItem',
        displayText: 'Help', 
        onclickFunc: function (e) { 
            window.open(cosmo.ui.menu.urls.HELP); e.preventDefault(); },
        urlString: cosmo.ui.menu.urls.HELP,
        displayMode: cosmo.ui.menu.displayModes.ANY,
        requiredRoles: []
        },
    { id: 'loginMenuItem',
        displayText: 'Log In', 
        onclickFunc: cosmo.env.getLoginRedirect(),
        displayMode: cosmo.ui.menu.displayModes.ANON,
        requiredRoles: []
        },
    { id: 'logoutMenuItem',
        displayText: _('Main.LogOut'), 
        urlString: cosmo.env.getRedirectUrl(),
        displayMode: cosmo.ui.menu.displayModes.AUTH,
        requiredRoles: [cosmo.ui.menu.requiredRoles.USER]
        }
];

cosmo.ui.menu.MenuItem = function (p) {
    var params = p || {};
    this.id = '';
    this.displayText = '';
    this.displayMode = '';
    this.requiredRoles = [];
    this.requiredPref = null;
    // Note that yes, there are sometimes that you want
    // *both* an onclickFunc and a urlString -- example
    // is opening something in a new window where you
    // also want to allow right-click to open in a new tab
    // IMPORTANT: In those cases be sure to pass the event
    // to the handler function and call preventDefault()
    // to prevent the browser from proceeding on to the URL
    // after exec'ing the function
    this.onclickFunc = null;
    this.urlString = '';
    for (var n in params) { this[n] = params[n]; }
};

cosmo.ui.menu.MainMenu = function (p) {
    var params = p || {};
    this.domNode = null;
    this.hasBeenRendered = false;
    this.currentlyDisplayedItems = [];
    this.createItem = function (item, lastItem) {
        var s = _createElem('span');
        s.id = item.id;
        if (item.onclickFunc || item.urlString) {
            var a = _createElem('a');
            a.className = 'menuBarLink';
            a.innerHTML = item.displayText;
            if (item.onclickFunc && typeof item.onclickFunc == 'function') {
                dojo.event.connect(a, 'onclick', item.onclickFunc);
            }
            if (item.urlString && typeof item.urlString == 'string') {
                a.href = item.urlString;
            }
            s.appendChild(a);
        }
        else {
            s.innerHTML = item.displayText;
        }
        return s;
    };
    this.appendItem = function (item, lastItem) {
        var s = this.createItem(item, lastItem);
        this.domNode.appendChild(s);
        var s = _createElem('span');
        s.className = 'menuBarDivider';
        s.appendChild(cosmo.util.html.nbsp());
        if (!lastItem) {
            s.appendChild(_createText('|'));
        }
        s.appendChild(cosmo.util.html.nbsp());
        this.domNode.appendChild(s);
    };
    this.insertItem = function (item, referenceItem) {
        
    };
    this.renderSelf = function () {
        if (!this.hasBeenRendered) {
            cosmo.ui.menu.init();
            this.hasBeenRendered = true;
        }
        this.clearAll();
        // Render menu according to loaded items
        var items = cosmo.ui.menu.items;
        var last = (items.length - 1);
        for (var i = 0; i < items.length; i++) {
            var item = items.getAtPos(i);
            var lastItem = i == last;
            this.appendItem(item, lastItem);
        }
        this.setPosition();
    }
    for (var n in params) { this[n] = params[n]; }
};
cosmo.ui.menu.MainMenu.prototype =
    new cosmo.ui.ContentBox();

