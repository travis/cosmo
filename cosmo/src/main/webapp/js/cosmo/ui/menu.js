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
dojo.require("cosmo.util.popup");
dojo.require('cosmo.convenience');
dojo.require('cosmo.account.settings');
dojo.require('cosmo.account.create');
dojo.require("cosmo.ui.ContentBox");
dojo.require("cosmo.util.deferred");

cosmo.ui.menu = new function () {
    var self = this;
    this.items = null;
    // A copy of the preferences, we need to watch the PreferencesUpdatedMessage topic
    // to make sure we keep this up to date
    this.preferences = {};
    this.init = function () {
        return this.loadItems();
    }
    this.loadItems = function (dontReloadPrefs) {
        var prefsDeferred = (cosmo.app.initParams.authAccess && !dontReloadPrefs)?
            cosmo.account.preferences.getPreferences() : 
            cosmo.util.deferred.getFiredDeferred();
            
        prefsDeferred.addCallback(dojo.lang.hitch(this, function (prefs) {
            if (prefs){
                this.preferences = prefs;
            }
            var items = cosmo.ui.menu.allItems;
            this.items = new cosmo.util.hash.Hash();
            // Instantiate all the items as MenuItem obj
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                if (this.itemShownInDisplayMode(item, this.calculateDisplayMode()) &&
                    this.userHasRequiredRolesForItem(item) &&
                    this.userHasRequiredPrefForItem(item)) {
                    
                    this.items.addItem(item.id, new cosmo.ui.menu.MenuItem(item));
                }
            }
        }));
        cosmo.util.deferred.addStdErrback(prefsDeferred);
        return prefsDeferred;
    };

    this.calculateDisplayMode = function(){
        var mode = 0;
        var modes = cosmo.ui.menu.displayModes;
        if (cosmo.app.initParams.authAccess) mode |= modes.AUTH;
        else mode |= modes.ANON;
        if (cosmo.app.initParams.ticketKey) mode |= modes.TICKETED;
        return mode;
    };

    this.itemShownInDisplayMode = function (item, displayMode) {
        return displayMode & item.displayMode;
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
    this.userHasRequiredPrefForItem = function (item) {
        var pref = item.requiredPref;
        if (pref) {
            return !!(this.preferences[pref]);
        }
        else {
            return true;
        }
    };
};

cosmo.ui.menu.displayModes = {
    ANON: 1,
    AUTH: 2,
    TICKETED: 4,
    ANY: 7
};

cosmo.ui.menu.requiredRoles = {
    USER: 'roleUser',
    ROOT: 'roleRoot'
};

cosmo.ui.menu.urls = {
    HELP: _("Main.CollectionDetails.HelpLink"),
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
    { id: 'subscribeWithMenuItem',
      createFunc: function(){
        var s = _createElem('span');
        s.id = 'subscribeSelector';
        var form = _createElem('form');
        s.appendChild(form);
        var clientOpts = cosmo.ui.widget.CollectionDetailsDialog.getClientOptions();
        clientOpts.unshift({ text: _('Main.SubscribeWith'), value: '' });
        var selOpts = { name: 'subscriptionSelect', id: 'subscriptionSelect',
           options: clientOpts, className: 'selectElem' };
        var subscrSel = cosmo.util.html.createSelect(selOpts);
        var f = function (e) {
            // Show the subcription dialog if the empty "Subscribe with ..."
            // option is not the one selected
            var sel = e.target;
            if (sel.selectedIndex != 0) {
            cosmo.app.showDialog(
                cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                    cosmo.app.pim.currentCollection,
                    sel.options[sel.selectedIndex].value));
            }
        };
        dojo.event.connect(subscrSel, 'onchange', f);
        form.appendChild(subscrSel)
        return s;
      },
      displayMode: cosmo.ui.menu.displayModes.ANON,
      requiredRoles: []
    },
    { id: 'welcomeMenuItem',
        displayText: _('Main.Welcome', [cosmo.app.currentUsername]),
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
        displayText: _('Main.Settings'),
        onclickFunc: function () { cosmo.account.settings.showDialog(); },
        displayMode: cosmo.ui.menu.displayModes.AUTH,
        requiredRoles: [cosmo.ui.menu.requiredRoles.USER]
        },
    { id: 'accountBrowserMenuItem',
        displayText: _('Main.AccountBrowser'),
        onclickFunc: function (e) {
            window.open(cosmo.ui.menu.urls.ACCOUNT_BROWSER); e.preventDefault(); },
        urlString: cosmo.ui.menu.urls.ACCOUNT_BROWSER,
        displayMode: cosmo.ui.menu.displayModes.AUTH,
        requiredRoles: [cosmo.ui.menu.requiredRoles.USER],
        requiredPref: cosmo.account.preferences.SHOW_ACCOUNT_BROWSER_LINK
        },
    { id: 'signupMenuItem',
        displayText: _('Main.SignUp'),
        onclickFunc: function () { cosmo.account.create.showForm() },
        displayMode: cosmo.ui.menu.displayModes.ANON,
        requiredRoles: []
        },
    { id: 'aboutMenuItem',
        displayText: _('Main.About'),
        onclickFunc: function () {cosmo.util.popup.open(cosmo.env.getBaseUrl() + '/help/about', 360, 280)},
        displayMode: cosmo.ui.menu.displayModes.ANON,
        requiredRoles: []
        },
    { id: 'helpMenuItem',
        displayText: _('Main.Help'),
        onclickFunc: function (e) {
            window.open(cosmo.ui.menu.urls.HELP); e.preventDefault(); },
        urlString: cosmo.ui.menu.urls.HELP,
        displayMode: cosmo.ui.menu.displayModes.ANY,
        requiredRoles: []
        },
    { id: 'loginMenuItem',
        displayText: _('Main.LogIn'),
        onclickFunc: function(){location = cosmo.env.getLoginRedirect()},
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
    this.subscribeTo = {};
    this.span = null;
    this.createFunc = null;
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
    this.show = function (){this.span.style.display = this.divider.style.display = 'inline';};
    this.hide = function (){this.span.style.display = this.divider.style.display = 'none';};
    for (var n in params) { this[n] = params[n]; }
    for (topic in this.subscribeTo){
        dojo.event.topic.subscribe(topic, dojo.lang.hitch(this, this.subscribeTo[topic]));
    }
};

cosmo.ui.menu.MainMenu = function (p) {
    var params = p || {};
    this.domNode = null;
    this.hasBeenRendered = false;
    this.currentlyDisplayedItems = [];
    this.createItem = function (item, lastItem) {
        if (item.createFunc) {
            return item.createFunc();
        } else {
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
        }
    };
    this.appendItem = function (item, lastItem) {
        var s = this.createItem(item, lastItem);
        this.domNode.appendChild(s);
        item.span = s;
        var s = _createElem('span');
        s.className = 'menuBarDivider';
        s.appendChild(cosmo.util.html.nbsp());
        if (!lastItem) {
            s.appendChild(_createText('|'));
        }
        s.appendChild(cosmo.util.html.nbsp());
        this.domNode.appendChild(s);
        item.divider = s; 
    };
    this.insertItem = function (item, referenceItem) {

    };
    this.renderSelf = function () {
        var initDeferred;
        if (!this.hasBeenRendered) {
            initDeferred = cosmo.ui.menu.init();
            initDeferred.addCallback(
                dojo.lang.hitch(this, 
                    function () {
                        this.hasBeenRendered = true;
                    }
                    )
            
            );
        } else {
            initDeferred = cosmo.util.deferred.getFiredDeferred();
        }
        
        initDeferred.addCallback(dojo.lang.hitch(this, function () {
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
        }));
        cosmo.util.deferred.addStdErrback(initDeferred, _("Error.Rendering", this))
        return initDeferred;
    }
    for (var n in params) { this[n] = params[n]; }
    dojo.event.topic.subscribe(cosmo.topics.PreferencesUpdatedMessage.topicName, 
                           dojo.lang.hitch(this, function (message) {
                               for (var pref in message.preferences){
                                   cosmo.ui.menu.preferences[pref] = message.preferences[pref];
                               }
                               cosmo.ui.menu.loadItems(true);
                               this.renderSelf();
                           }));
    
};
cosmo.ui.menu.MainMenu.prototype =
    new cosmo.ui.ContentBox();

