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
                    this.userHasRequiredPrefForItem(item) &&
                    this.confForItem(item)) {

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
    this.confForItem = function (item) {
        var conf = item.requiredConf;
        if (conf){
            for (var i = 0; i < conf.length; i++) {
                if (!cosmo.ui.conf.getBooleanValue([conf[i]])) {
                    return false;
                }
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
    ACCOUNT_BROWSER: cosmo.env.getFullUrl("Browse") + "/" +
        cosmo.app.currentUsername,
    ADMIN_CONSOLE: cosmo.env.getFullUrl("UserList")
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
                    cosmo.app.pim.getSelectedCollection(),
                    sel.options[sel.selectedIndex].value));
            }
        };
        dojo.event.connect(subscrSel, 'onchange', f);
        form.appendChild(subscrSel)
        return s;
      },
      displayMode: cosmo.ui.menu.displayModes.ANON,
      requiredRoles: [],
      dividerText: "\u00A0\u00A0"
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
    { id: 'loginMenuItem',
        displayText: _('Main.LogIn'),
        onclickFunc: function(){location = cosmo.env.getLoginRedirect()},
        displayMode: cosmo.ui.menu.displayModes.ANON,
        requiredRoles: [],
        dividerText: "\u00A0\u00A0"
        },
    { id: 'logoutMenuItem',
        displayText: _('Main.LogOut'),
        urlString: cosmo.env.getRedirectUrl(),
        displayMode: cosmo.ui.menu.displayModes.AUTH,
        requiredRoles: [cosmo.ui.menu.requiredRoles.USER],
        dividerText: "\u00A0\u00A0"
        },
    { id: 'aboutMenuItem',
        displayText: _('Main.About'),
        onclickFunc: function () {cosmo.util.popup.open(cosmo.env.getFullUrl("About"), 360, 280)},
        displayMode: cosmo.ui.menu.displayModes.ANON,
        requiredRoles: []
        },
    { id: 'tosMenuItem',
        displayText: _('Main.TermsOfService'),
        onclickFunc: function (e) {
            cosmo.util.popup.open(cosmo.env.getFullUrl("TermsOfService"), _("TermsOfService.Width"), _("TermsOfService.Height"), true); },
        displayMode: cosmo.ui.menu.displayModes.ANY,
        requiredRoles: [],
        requiredConf: ["tosRequired"]
        },
    { id: 'privacyMenuItem',
        displayText: _('Main.PrivacyPolicy'),
        onclickFunc: function (e) {
            cosmo.util.popup.open(cosmo.env.getFullUrl("PrivacyPolicy"), _("PrivacyPolicy.Width"), _("PrivacyPolicy.Height"), true); },
        displayMode: cosmo.ui.menu.displayModes.ANY,
        requiredRoles: [],
        requiredConf: ["tosRequired"]
        },
    { id: 'helpMenuItem',
        displayText: _('Main.Help'),
        onclickFunc: function (e) {
            window.open(cosmo.ui.menu.urls.HELP); e.preventDefault(); },
        urlString: cosmo.ui.menu.urls.HELP,
        displayMode: cosmo.ui.menu.displayModes.ANY,
        requiredRoles: []
        }
];

cosmo.ui.menu.MenuItem = function (p) {
    var params = p || {};
    this.id = '';
    this.displayText = '';
    this.displayMode = '';
    this.requiredRoles = [];
    this.requiredPref = null;
    this.requiredConf = [];
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
            s.appendChild(_createText(item.dividerText? item.dividerText : '|'));
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

cosmo.ui.menu.HierarchicalMenuManager = new function () {
    // Config -- used in width calculations
    var HORIZ_MARGIN_WIDTH = 4;
    var BORDER_WIDTH = 1;
    var EXPANDO_ARROW_WIDTH = 16;
    var MENU_OVERLAP = 2;

    this.displayedMenu = null;
    this.contextMenuRegistry = {};

    // Private props
    this._currX = 0;
    this._currY = 0;
    this._currLevel = 0;
    this._expandedItemForEachLevel = [];
    this._xPosMarksForEachLevel = [];
    this._yPosMarksForEachLevel = [];

    // Public interface methods
    this.createContextMenu = function(id, items, o) {
        var opts = o || {};
        var menu = new cosmo.ui.menu.HierarchicalMenu(id, items, opts);
        this.contextMenuRegistry[id] = menu;
        return menu;
    };
    this.showContextMenu = function (e, menu) {
        var xPos = e.clientX;
        var yPos = e.clientY;
        var items = menu.items;
        if (!items || !items.length) {
            throw new Error('Contextual menu "' + menu.id +'" has no menu items.');
        }
        this.hideHierarchicalMenu();
        this.displayedMenu = menu;
        if (menu.doBeforeShowing && typeof menu.doBeforeShowing == 'function') {
            menu.doBeforeShowing();
        }
        this._showHierMenuLevel(0, menu, xPos, yPos);
        menu.displayed = true;
        document.body.onselectstart = function () { return false; };
        e.preventDefault();
        e.stopPropagation();
        return false;
    };
    this.hideHierarchicalMenu = function (e) {
        this._hideSubMenus(-1);
        if (this.displayedMenu) {
            if (typeof this.displayedMenu.doAfterHiding == 'function') {
                this.displayedMenu.doAfterHiding();
            }
            this.displayedMenu.displayed = false;
            this.displayedMenu = null;
        }
        document.body.onselectstart = null;
    };

    // Public DOM-event handlers
    this.handleMouseOver = function (e) {
        var targ = e.target;
        while (!targ.id) { targ = targ.parentNode; }
        if (targ.id == 'body') { return false; }
        if (targ && targ.className.indexOf('hierMenuItem') > -1) {
            var currMenuNode = this._getMenuNodeForMenuItemNode(targ);
            var currLevel = this._getHierarchyLevelForMenuNode(currMenuNode);
            var nextLevel = currLevel + 1;
            var key = targ.id.replace('hierMenuItem_', '');
            var index = key.substr(key.length - 1); // Negative pos param breaks in IE
            var menuItem = this.displayedMenu.getMenuItem(key);
            var currSub = $('hierMenuLevel_' + nextLevel);
            dojo.html.addClass(targ, 'selectedItem');
            if (currSub) {
                var subKey = currSub.firstChild.firstChild.id.replace('hierMenuItem_', '');
                if (subKey.substr(0, subKey.length - 1) == key) {
                    return false;
                }
                var expandedItem = this._expandedItemForEachLevel[currLevel];
                var expandedNode = this._getMenuItemNodeForMenuItem(expandedItem);
                dojo.html.removeClass(expandedNode, 'selectedItem');
                this._hideSubMenus(currLevel);
            }
            else if (menuItem.items && menuItem.items.length) {
                this._expandedItemForEachLevel[currLevel] = menuItem;
                this._xPosMarksForEachLevel[currLevel] = this._currX;
                this._yPosMarksForEachLevel[currLevel] = this._currY;
                var newX = this._currX + currMenuNode.offsetWidth;
                var newY = this._currY + (index*24);
                this._showHierMenuLevel(nextLevel, menuItem, newX, newY);
            }
        }
    };
    this.handleMouseOut = function (e) {
        var targ = e.target;
        while (!targ.id) { targ = targ.parentNode; }
        if (targ.id == 'body') { return false; }
        if (targ && targ.className.indexOf('hierMenuItem') > -1) {
            var currMenuNode = this._getMenuNodeForMenuItemNode(targ);
            var currLevel = this._getHierarchyLevelForMenuNode(currMenuNode);
            var menuItem = this._getMenuItemForMenuItemNode(targ);
            if (this._expandedItemForEachLevel[currLevel] == menuItem) {
                return false;
            }
            dojo.html.removeClass(targ, 'selectedItem');
        }
    };
    this.handleClick = function (e) {
        var targ = e.target;
        while (!targ.id) { targ = targ.parentNode; }
        if (targ.id == 'body') { return false; }
        if (targ && targ.className.indexOf('hierMenuItem') > -1) {
            var menuItem = this._getMenuItemForMenuItemNode(targ);
            if (typeof menuItem.handleClick == 'function') {
                setTimeout(menuItem.handleClick, 0);
            }
            else {
                e.stopPropagation();
                return false;
            }
        }
        this.hideHierarchicalMenu();
    };

    // Private methods
    this._showHierMenuLevel = function (level, menuItem, x, y) {
        var table = _createElem('table');
        var tbody = _createElem('tbody');
        var tr = null;
        var td = null;
        var div = null;
        var viewport = dojo.html.getViewport();

        table.cellPadding = 0;
        table.cellSpacing = 0;
        table.className = 'hierMenu';
        table.id = 'hierMenuLevel_' + level;

        var items = menuItem.items;
        this._currLevel = level;
        // User-configuratble min/max
        var min = this.displayedMenu.minWidth;
        var max = this.displayedMenu.maxWidth;
        // The natural width the menu would be based on the
        // text of the items inside -- add 4px x 2 for the
        // margin, and 2 px for the borders so we can be
        // talking about the width of the containing box
        var nat = menuItem.naturalWidth + (HORIZ_MARGIN_WIDTH * 2) +
            (BORDER_WIDTH * 2);

        // Width-calc fu
        var menuLevelWidth = 0;
        // Min width set -- use the min if wider than
        // the natural width
        if (min) {
            menuLevelWidth = min > nat ? min : nat;
        }
        // Otherwise just use the natural width
        else {
            menuLevelWidth = nat;
        }
        if (max) {
            menuLevelWidth = menuLevelWidth > max ? max : menuLevelWidth;
        }
        if (menuItem.subItemHasSubItems) {
            menuLevelWidth += EXPANDO_ARROW_WIDTH;
        }
        // Menu would extend outside the browser window
        // X position overlap -- go into reverso mode
        if ((x + menuLevelWidth) > viewport.width) {
            x -= menuLevelWidth;
            if (level > 0) {
                var parentWidth =
                    $('hierMenuLevel_' + (level - 1)).offsetWidth;
                x -= parentWidth;
            }
            // A bit of backward overlap
            x += MENU_OVERLAP;
        }
        else {
            // A bit of overlap
            x -= MENU_OVERLAP;
        }
        // Y position overlap -- compensate by the
        // amount of the overlap
        var yOver = (y + (items.length * 24)) - viewport.height;
        if (yOver > 0) {
            y -= (yOver + (BORDER_WIDTH * 2));
        }

        // Record the current XY to use for calc'ing
        // the next sub-menu
        this._currX = x;
        this._currY = y;

        var titleColumnWidth = menuItem.subItemHasSubItems ?
            menuLevelWidth - EXPANDO_ARROW_WIDTH : menuLevelWidth;

        table.style.width = menuLevelWidth + 'px';
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            tr = _createElem('tr');
            td = _createElem('td');
            div = _createElem('div');
            tr.id = 'hierMenuItem_' + item.hierarchyKey;
            tr.className = 'hierMenuItem';
            td.className = 'hierMenuText';
            div.className = 'hierMenuTextClipper';
            td.style.width = titleColumnWidth + 'px';
            div.style.width = (titleColumnWidth -
                (HORIZ_MARGIN_WIDTH * 2)) + 'px';
            if (document.all) {
                var nobr = _createElem('nobr');
                nobr.innerHTML = item.display;
                div.appendChild(nobr);
            }
            else {
                div.innerHTML = item.display;
            }
            td.appendChild(div);
            tr.appendChild(td);
            td = _createElem('td');
            if (menuItem.subItemHasSubItems) {
                td.style.textAlign = 'center';
                td.style.width = EXPANDO_ARROW_WIDTH + 'px';
                if (item.items) {
                    td.innerHTML = '&gt;';
                }
            }
            else {
                td.style.width = '1px';
            }
            tr.appendChild(td);
            tbody.appendChild(tr);
        }
        tbody.appendChild(tr);
        table.appendChild(tbody);

        table.style.left = x + 'px';
        table.style.top = y + 'px';
        document.body.appendChild(table);

        dojo.event.connect(table, 'onmouseover', this, 'handleMouseOver');
        dojo.event.connect(table, 'onmouseout', this, 'handleMouseOut');
        dojo.event.connect(table, 'onclick', this, 'handleClick');
    };
    this._hideSubMenus = function (level) {
        this._currX = this._xPosMarksForEachLevel[level];
        this._currY = this._yPosMarksForEachLevel[level];
        var nextLevel = level + 1;
        var max = this._currLevel + 1;
        for (var n = nextLevel; n < max; n++) {
            var removeMenu = $('hierMenuLevel_' + n);
            delete this._expandedItemForEachLevel[n];
            if (removeMenu) {
                dojo.event.disconnect(removeMenu, 'onmouseover',
                    this, 'handleMouseOver');
                dojo.event.disconnect(removeMenu, 'onclick',
                    this, 'handleClick');
                document.body.removeChild(removeMenu);
            }
        }
    };
    this._getHierarchyLevelForMenuNode = function (node) {
        return parseInt(node.id.replace('hierMenuLevel_', ''));
    };
    this._getMenuNodeForMenuItemNode = function (node) {
        return node.parentNode.parentNode;
    };
    this._getMenuItemForMenuItemNode = function (node) {
        var key = node.id.replace('hierMenuItem_', '');
        return this.displayedMenu.getMenuItem(key);
    };
    this._getMenuItemNodeForMenuItem = function (item) {
        return $('hierMenuItem_' + item.hierarchyKey);
    };
};

cosmo.ui.menu.HierarchicalMenuItem = function (p) {
    var params = p || {};
    this.display = params.display || '';
    this.handleClick = params.handleClick || null;
    this.items = params.items || null;
    this.naturalWidth = null;
    this.subItemHasSubItems = false;
    this.hierarchyKey = '';
};

cosmo.ui.menu.HierarchicalMenu = function (id, items, o) {
    this.id = id;
    this.items = items || null;
    this.naturalWidth = null;
    this.subItemHasSubItems = false;
    this.map;
    this.displayed = false;

    // User-customizable props
    var opts = o || {};
    // An action to perform whenever the menu is dismissed
    // Useful, for example, for releasing control of a
    // rollover highlight that's pinned to a contextual
    // menu's pseudo-selected item
    this.doAfterHiding = opts.doAfterHiding || null;
    // Do this before showing the menu -- useful for
    // refreshing the data for dynamic menus
    this.doBeforeShowing = opts.doBeforeShowing || null;
    // Force each menu section to a minimum width -- useful
    // if the titles for a set of menu items is too short
    // to provide a reasonable click surface. Can be used
    // in combination with maxWidth
    this.minWidth = opts.minWidth || null;
    // Constrain each menu section to this max width --
    // useful if you may have items that are ridiculously
    // long. Can be used in conbination with minWidth
    this.maxWidth = opts.maxWidth || null;

    this.updateDisplayData();
};

// The top-level menu object is a special case of
// the single menu item -- it's the "item" that
// contains the sub-items shown in (the top) level 0
// of the menu hierarchy
cosmo.ui.menu.HierarchicalMenu.prototype =
    new cosmo.ui.menu.HierarchicalMenuItem();

cosmo.ui.menu.HierarchicalMenu.prototype.updateDisplayData =
    function () {
    this.updateMap();
    this.setNaturalWidths();
};

// Creates a map of the entire menu structure --
// map keys are used as the id suffixes
// in the DOM hierarchy of the menus, and lookup
// happens when responding to clicks
// '00' -> Some Item
// '000' -> Some Sub-Item
// '0000' -> Another Level Down
// '0001' -> Another Level Down 2
// '001' -> Some Other Sub-Item
// '01' -> Another Item
cosmo.ui.menu.HierarchicalMenu.prototype.updateMap =
    function () {
    var items = this.items;
    var map = {};
    var mapHier = function (hierKey, hierItems) {
        for (var i = 0; i < hierItems.length; i++) {
            var item = hierItems[i];
            var itemKey = hierKey + i.toString();
            if (item.items) {
                mapHier(itemKey, item.items);
            }
            item.hierarchyKey = itemKey;
            map[itemKey] = item;
        }
    };
    mapHier('0', items);
    this.map = map;
};
// This is an ugly hack to measure out actual widths
// per item-title -- the max width for the set of items
// at a level determines the width of the menu
// min-width is broken in FF for anything more complicated
// than a simple div, and works not at all in IE6.
// The other alternative would be hard-coding menu width
// and wrapping menu items, which makes the Baby Jesus cry
cosmo.ui.menu.HierarchicalMenu.prototype.setNaturalWidths =
    function () {
    var d = _createElem('div');
    d.style.position = 'absolute';
    // IE6 defaults to 100% unless you give it a width
    // FF & IE7 default to 'the minimum,' which is what we want
    if (navigator.appVersion.indexOf('MSIE 6') > -1) {
        d.style.width = '1%';
    }
    d.style.whiteSpace = 'nowrap';
    d.style.left = '-9999999px';
    d.style.top = '-9999999px';
    document.body.appendChild(d);
    var setWidths = function (widthItem) {
        var items = widthItem.items;
        if (items) {
            var max = 0;
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                d.innerHTML = item.display;
                max = d.offsetWidth > max ? d.offsetWidth : max;
                setWidths(item);
                if (item.items) {
                    widthItem.subItemHasSubItems = true;
                }
            }
            widthItem.naturalWidth = max;
        }
        else {
            widthItem.naturalWidth = null;
        }
    };
    setWidths(this);
};
cosmo.ui.menu.HierarchicalMenu.prototype.getMenuItem =
    function (key) {
    return this.map[key];
};

// Close menus via any click on the doc body
dojo.event.connect(document, 'onclick',
    cosmo.ui.menu.HierarchicalMenuManager,
        'hideHierarchicalMenu');


