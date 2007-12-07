/*
 * Copyright 2007 Open Source Applications Foundation
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

dojo.provide("cosmo.ui.selector");
dojo.require("cosmo.ui.ContentBox"); // Superclass

dojo.require("dojo.event.*");
dojo.require("dojo.gfx.color.hsv");

dojo.require("cosmo.app.pim");
dojo.require('cosmo.convenience');
dojo.require("cosmo.topics");
dojo.require("cosmo.view.names");
dojo.require("cosmo.util.html");
dojo.require("cosmo.ui.menu");

cosmo.ui.selector.CollectionSelector = function (p) {
    var _this = this;
    this.parent = null;
    this.domNode = null;

    var params = p || {};
    for (var n in params) { this[n] = params[n]; }

    dojo.event.topic.subscribe('/calEvent', _this, 'handlePub_calEvent');
    dojo.event.topic.subscribe(cosmo.topics.CollectionUpdatedMessage.topicName,
        _this, 'handlePub_app');
    dojo.event.topic.subscribe(cosmo.topics.SubscriptionUpdatedMessage.topicName,
        _this, 'handlePub_app');

    // Private vars
    this._scrollTop = 0;
    this._doRolloverEffect =  function(e, isOver, isFromContextual) {
        // Safari 2 sucks -- DOM-event/DOM-node contention problems
        if (navigator.userAgent.indexOf('Safari/41') > -1) {
            return false;
        }
        // Don't do rollovers when contextual menu is showing,
        // except to move it if the contextual menu moves
        //if (_this.contextMenu.displayed && !isFromContextual) {
        //    return false;
        //}
        if (e && e.target) {
            var targ = e.target;
            while (!targ.className) { targ = targ.parentNode; }
            if (targ.id == 'body') { return false; }
            var prefix = 'collectionSelector';
            if (targ.className.indexOf(prefix) > -1) {
                if (targ.className.indexOf('Details') > -1) {
                    var collId = targ.id.replace('collectionSelectorItemDetails_', '');
                    var coll = cosmo.app.pim.collections.getItem(collId);
                    var hue = coll.hue;
                    var sv = isOver ? [50, 100] : [80, 90];
                    var colorString = this._getRGB(hue, sv[0], sv[1]);
                    targ.style.backgroundColor = colorString;
                }
                else {
                    // Don't apply rollover fu to selected item
                    var id = targ.id.replace('collectionSelectorItemSel_', '');
                    if (id == cosmo.app.pim.getSelectedCollectionId()) {
                        return false;
                    }
                    var addRemoveKey = isOver ? 'add' : 'remove';
                    var par = targ.parentNode;
                    var ch = par.childNodes;
                    for (var i = 0; i < ch.length; i++) {
                        var node = ch[i];
                        if (node.className != 'collectionSelectorDetails') {
                            dojo.html[addRemoveKey + 'Class'](ch[i], 'mouseoverItem');
                        }
                    }
                }
            }
        }
    };
    this._getRGB = function (h, s, v) {
        var rgb = dojo.gfx.color.hsv2rgb(h, s, v, {
            inputRange: [360, 100, 100], outputRange: 255 });
        return 'rgb(' + rgb.join() + ')';
    };

    // Interface methods
    this.handlePub_calEvent = function (cmd) {
        var act = cmd.action;
        switch (act) {
            // FIXME: Piggybacking rendering on view changes -- used here
            // so the overlay checkboxes can appear/disapper. This also
            // triggers initial render when app loads
            case 'eventsLoadSuccess':
                this.render();
                break;
            default:
                // Do nothing
                break;
        }
    };
    // Interface methods
    this.handlePub_app = function (cmd) {
        this.render();
    };

    this.renderSelf = function () {
        // Preserve scrolled state
        var origContainer = $('collectionSelectorContainer');
        if (origContainer) {
            this._scrollTop = origContainer.scrollTop;
        }
        var _this = this;
        var collections = cosmo.app.pim.collections;
        var currColl = cosmo.app.pim.getSelectedCollection();
        var container = _createElem('div');
        container.id = 'collectionSelectorContainer';
        var form = _createElem('form');
        var table = _createElem('table');
        table.cellPadding = 0;
        table.cellSpacing = 0;
        table.id = 'collectionSelectorTable';
        var tbody = _createElem('tbody');
        var tr = null;
        var td = null;
        var displayColl = function (key, c) {
            var cUid = c.getUid();
            var sel = cUid == currColl.getUid();
            var className = '';
            tr = _createElem('tr');

            if (cosmo.app.pim.currentView == cosmo.view.names.CAL) {
                td = _createElem('td');
                var isChecked = !!c.isOverlaid;
                var ch = cosmo.util.html.createInput({
                    type: 'checkbox',
                    name: 'collectionSelectorItemCheck',
                    id: 'collectionSelectorItemCheck_' + cUid,
                    checked: isChecked
                });
                td.appendChild(ch);
                className = 'collectionSelectorCheckbox';
                if (sel) {
                    className += ' collectionSelectorSel';
                }
                td.className = className;
                tr.appendChild(td);
            }

            td = _createElem('td');
            td.id = 'collectionSelectorItemSel_' + cUid;
            td.appendChild(_createText(c.getDisplayName()));
            className = 'collectionSelectorCollectionName';
            if (sel) {
                className += ' collectionSelectorSel';
            }
            td.className = className;
            tr.appendChild(td);

            td = _createElem('td');
            td.id = 'collectionSelectorItemDetails_' + cUid;
            var d = _createElem('div');
            d.style.width = '6px';
            d.style.height = '12px';
            d.style.margin = 'auto';
            var icon = cosmo.ui.imagegrid.createImageIcon(
                { domNode: d, iconState: 'collectionDetailsDefault' });
            td.className = 'collectionSelectorDetails';
            td.style.backgroundColor = _this._getRGB(c.hue, 80, 90);
            td.appendChild(icon);
            tr.appendChild(td);

            tbody.appendChild(tr);
        };

        // Clear the DOM
        this.clearAll();
        if (collections.length) {
            collections.each(displayColl);
        }
        else {
            tr = _createElem('tr');
            td = _createElem('td');
            td.id = 'collectionSelectorNoCollectionsPrompt'
            td.appendChild(_createText(_("Main.NewCollectionPrompt")));
            tr.appendChild(td);
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);
        this.domNode.style.width = LEFT_SIDEBAR_WIDTH + 'px';
        // Allocate space for the "New collection" link at the bottom
        var scrollingAreaHeight = COLLECTION_SELECTOR_HEIGHT - 16;
        container.style.height = scrollingAreaHeight + 'px';
        container.appendChild(table);

        // Attach event listeners -- event will be delagated
        // to clicked cell or checkbox
        dojo.event.connect(container, 'onmouseover',
            this, 'handleMouseOver');
        dojo.event.connect(container, 'onmouseout',
            this, 'handleMouseOut');
        dojo.event.connect(container, 'onclick',
            this, 'handleClick');
        dojo.event.connect(container, 'oncontextmenu',
            this, 'handleClick');

        this.domNode.appendChild(form);
        form.appendChild(container);
        // Fix various IE brokennesses in width/scrollbar
        // interaction
        if (document.all) {
            // IE7 -- make sure scrollbar appears
            // It doesn't show until some DOM events hit it, even
            // though content is clearly taller than the fixed
            // height of the container
            if (table.clientHeight > container.clientHeight) {
                container.style.overflowY = 'scroll';
            }
            // Now fix scrollbar positioning bugs
            var currWidth = parseInt(this.domNode.style.width);
            // IE6 -- puts the scrollbar outside the container
            // after content has been rendered
            var wDiff = this.domNode.offsetWidth - LEFT_SIDEBAR_WIDTH;
            if (wDiff > 0) {
                this.domNode.style.width = (currWidth - wDiff) + 'px';
            }
            // IE7 -- puts the scrollbar correctly inside
            // the container, but overlays the content inside
            var wDiff = LEFT_SIDEBAR_WIDTH - container.clientWidth;
            if (wDiff > 0) {
                table.style.width = (currWidth - wDiff) + 'px';
            }
        }
        // Preserve scrolled state on re-render
        container.scrollTop = this._scrollTop;

        var d = _createElem("div");
        d.id = "newCollectionDiv";
        var a = _createElem("a");
        a.id = "newCollectionLink";
        a.appendChild(_createText(_("Main.NewCollectionLink")));

        dojo.event.connect(a, "onclick", function(){
            var collectionNameDeferred =
                cosmo.app.getValue(
                    _("Main.NewCollection.NamePrompt"),
                    _("Main.NewCollection.DefaultName"),
                    [function(name){
                        for (var i = 0; i < collections.length; i++){
                            if (name == collections.getAtPos(i).getDisplayName()){
                                return _("Main.NewCollection.NameInUse");
                            }
                        }
                    }],
                    { defaultActionButtonText: _('App.Button.Save'),
                        showCancel: true }
                    );
            collectionNameDeferred.addCallback(function(name){
                cosmo.app.modalDialog.setPrompt(_('App.Status.Processing'));
                var createDeferred = cosmo.app.pim.serv.createCollection(name);
                createDeferred.addCallback(function(result){
                    //TODO: This is bad. Giant waste of bandwidth.
                    // We can fix this by returning a collection from a create request.
                    // On the plus side, most of the collections should be cached since
                    // we already had them loaded.
                    return cosmo.app.pim.reloadCollections();
                });
                createDeferred.addBoth(function(){
                    var f = function () {
                        cosmo.topics.publish(cosmo.topics.CollectionUpdatedMessage);
                    }
                    setTimeout(f, 0);
                    cosmo.app.hideDialog();
                });
            });
        });

        d.appendChild(a);
        this.domNode.appendChild(d);

        if (!this.hasBeenRendered) {
            /*
            var _menuItem = cosmo.ui.menu.HierarchicalMenuItem;
            var items = [];
            this.contextMenu =
                cosmo.ui.menu.HierarchicalMenuManager.createContextMenu(
                    'collectionSelectorContext', items, { minWidth: 100 });
            this.contextMenu.doAfterHiding = function () {
                _this._doRolloverEffect(
                    { target: _this._contextMenuCurrrentCollection }, false, true);
            };
            */
            this.hasBeenRendered = true;
        }

    };
    this.handleMouseOver = function (e) {
        this._doRolloverEffect(e, true);
    };
    this.handleMouseOut = function (e) {
        this._doRolloverEffect(e, false);
    };
    this.handleClick = function (e) {
        if (e && e.target) {
            var targ = e.target;
            while (!targ.id) { targ = targ.parentNode; }
            if (targ.id == 'body') { return false; }
            var prefix = 'collectionSelectorItem';
            if (targ.id.indexOf(prefix) > -1) {
                var collections = cosmo.app.pim.collections;
                var currColl = cosmo.app.pim.getSelectedCollection();
                var currId = currColl.getUid();
                var newCurrColl = null;;
                if (targ.id.indexOf(prefix + 'Details_') > -1) {
                    var id = targ.id.replace(prefix + 'Details_', '');
                    cosmo.app.showDialog(
                        cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                            cosmo.app.pim.collections.getItem(id)));
                    return true;
                }
                // Selector
                if (targ.id.indexOf(prefix + 'Sel_') > -1) {
                    var id = targ.id.replace(prefix + 'Sel_', '');

                    /*
                    // Right-click -- contextual menu
                    if (e.button == 2) {
                        if (this._contextMenuCurrrentCollection) {
                            cosmo.ui.menu.HierarchicalMenuManager.hideHierarchicalMenu();
                        }
                        this._contextMenuCurrrentCollection = targ;
                        cosmo.ui.menu.HierarchicalMenuManager.showContextMenu(e,
                            _this.contextMenu);
                        this._doRolloverEffect(e, true, true);
                        return false;
                    }
                    */

                    newCurrColl = collections.getItem(id);
                    if (id != currId) {
                        // Turn off display for the originally selected
                        // collection if it isn't explicitly checked for overlay
                        // FIXME: similar logic exists in setSelectedCalItem of
                        // cosmo.view.cal.canvas. This should be refactored
                        // into a method of some kind of abstracted UI-only
                        // collection object
                        var ch = $(prefix + 'Check_' + currId);
                        if (!ch || (ch && !ch.checked)) {
                            currColl.doDisplay = false;
                        }
                        newCurrColl.doDisplay = true;
                        cosmo.view.displayViewFromCollections(newCurrColl);
                    }
                }
                // Overlays
                else if (targ.id.indexOf(prefix + 'Check_') > -1) {
                    var id = targ.id.replace(prefix + 'Check_', '');
                    var d = targ.checked;
                    newCurrColl = collections.getItem(id);
                    newCurrColl.doDisplay = d;
                    newCurrColl.isOverlaid = d;
                    if (id == currId) { return false; }
                    cosmo.view.displayViewFromCollections();
                }
            }
        }
    };
};

cosmo.ui.selector.CollectionSelector.prototype =
    new cosmo.ui.ContentBox();



