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

dojo.provide('cosmo.view.list.canvas');

dojo.require('dojo.event.*');
dojo.require("cosmo.app.pim");
dojo.require("cosmo.app.pim.layout");
dojo.require("cosmo.view.common");
dojo.require("cosmo.view.list.common");
dojo.require("cosmo.view.list.sort");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.convenience");
dojo.require("cosmo.ui.ContentBox");
dojo.require("cosmo.ui.imagegrid");

cosmo.view.list.canvas.Canvas = function (p) {
    var self = this;
    var params = p || {};

    dojo.lang.mixin(this, cosmo.view.canvasBase);

    this.domNode = null;
    this.id = '';
    this.view = cosmo.view.list;
    //this.currSelectedId = '';
    // UIDs for selected events keyed by the uid of
    // the currently displayed collection
    this.selectedItemIdRegistry = {};
    //this.currSelectedItem = null;
    this.currSortCol = 'Triage';
    this.currSortDir = 'Desc';
    this.itemsPerPage = 20;
    this.itemCount = 0;
    this.pageCount = 0;
    this.currPageNum = 1;

    for (var n in params) { this[n] = params[n]; }

    dojo.event.topic.subscribe('/calEvent', self, 'handlePub_calEvent');

    // Interface methods
    this.handlePub_calEvent = function (cmd) {
        if (!cosmo.view.list.isCurrentView()) { return false; }

        var act = cmd.action;
        var qual = cmd.qualifier || null;
        var data = cmd.data || {};
        var opts = cmd.opts;
        var delta = cmd.delta;
        switch (act) {
            case 'saveSuccess':
                this._saveSuccess(cmd)
                break;
            case 'removeSuccess':
                var ev = cmd.data;
                this._removeSuccess(cmd);
            default:
                // Do nothing
                break;
        }

    };
    this.renderSelf = function () {
        if (!cosmo.view.list.isCurrentView()) { return false; }

        var reg = this.view.itemRegistry;
        this._updateSize();
        this.setPosition(0, CAL_TOP_NAV_HEIGHT);
        this.setSize();

        cosmo.view.list.sort.doSort(reg, this.currSortCol, this.currSortDir);
        this.displayTable();
        var sel = this.getSelectedItem();
        if (sel) {
            cosmo.app.pim.baseLayout.mainApp.rightSidebar.detailViewForm.updateFromItem(
                sel);
        }

    }
    this.handleMouseOver = function (e) {
        if (e && e.target) {
            // get the UID from the row's DOM node id
            var p = e.target.parentNode;
            if (!p.id) { return false; }
            var ch = p.childNodes;
            for (var i = 0; i < ch.length; i++) {
                ch[i].className = 'listViewDataCell listViewSelectedCell';
            }
        }
    };
    this.handleMouseOut = function (e) {
        if (e && e.target) {
            // get the UID from the row's DOM node id
            var p = e.target.parentNode;
            if (!p.id || (p.id ==  'listView_item' + self.getSelectedItemId())) { return false; }
            var ch = p.childNodes;
            for (var i = 0; i < ch.length; i++) {
                ch[i].className = 'listViewDataCell';
            }
        }
    };
    this.handleClick = function (e) {
        if (e && e.target) {
            var targ = e.target;
            // Header cell clicked
            if (targ.id && targ.id.indexOf('Header') > -1) {
                this._doSortAndDisplay(targ.id);
            }
            // Normal row cell clicked
            else {
                var p = targ.parentNode;
                if (!p.id) { return false; }
                // Deselect any original selection
                var orig = $('listView_item' + self.getSelectedItemId());
                if (orig) {
                    ch = orig.childNodes;
                    for (var i = 0; i < ch.length; i++) {
                        ch[i].className = 'listViewDataCell';
                    }
                }
                // The new selection
                var ch = p.childNodes;
                for (var i = 0; i < ch.length; i++) {
                    ch[i].className = 'listViewDataCell listViewSelectedCell';
                }
                var id = p.id.replace('listView_item', '');
                var item = this.view.itemRegistry.getItem(id);
                // Load the selected item's stuff into the detail-view form
                if (item) {
                    self.setSelectedItem(item);
                    var f = function () {
                      dojo.event.topic.publish('/calEvent', { 'action': 'setSelected',
                        'data': item });
                    };
                    // Free up the UI thread so we don't see two items
                    // selected at once while the message is being published
                    // to all parts of the UI
                    setTimeout(f, 0);
                }
            }
        }
    };
    // innerHTML will be much faster for table display with
    // lots of rows
    this.displayTable = function () {
        var _list = cosmo.view.list;
        var hash = _list.itemRegistry;
        var selId = 'listView_item' + self.getSelectedItemId();
        var map = cosmo.view.list.triageStatusCodeNumberMappings;
        var d = _createElem('div'); // Dummy div
        var taskIcon = cosmo.ui.imagegrid.createImageIcon({ domNode: d,
            iconState: 'listViewTaskIcon' });
        var taskIconStyle = taskIcon.style;
        var t = '<table id="listViewTable" cellpadding="0" cellspacing="0" style="width: 100%;">\n';
        var r = '';
        r += '<tr>';
        r += '<td id="listViewTaskHeader" class="listViewHeaderCell" style="width: 16px;">&nbsp;</td>';
        r += '<td id="listViewTitleHeader" class="listViewHeaderCell">Title</td>';
        r += '<td id="listViewWhoHeader" class="listViewHeaderCell">Updated By</td>';
        r += '<td id="listViewStartDateHeader" class="listViewHeaderCell">Starts On</td>';
        r += '<td id="listViewTriageHeader" class="listViewHeaderCell" style="border-right: 0px;">Triage</td>';
        r += '</tr>\n';
        t += r;
        var getRow = function (key, val) {
            var item = val;
            var display = item.display;
            var selCss = 'listView_item' + display.uid == selId ? ' listViewSelectedCell' : '';
            r = '';
            r += '<tr id="listView_item' + display.uid + '">';
            r += '<td class="listViewDataCell' + selCss + '">';
            if (display.task) {
                r += '<div style="margin: 0px 2px; width: ' + taskIconStyle.width +
                    '; height: ' + taskIconStyle.height +
                    '; font-size: 1px; background-image: ' +
                    taskIconStyle.backgroundImage + '; background-position: ' +
                    taskIconStyle.backgroundPosition + '">&nbsp;</div>';
            }
            r += '</td>';
            r += '<td class="listViewDataCell' + selCss + '">' + display.title + '</td>';
            r += '<td class="listViewDataCell' + selCss + '">' + display.who + '</td>';
            r += '<td class="listViewDataCell' + selCss + '">' + display.startDate + '</td>';
            r += '<td class="listViewDataCell' + selCss + '">' + display.triage + '</td>';
            r += '</tr>\n';
            t += r;
        }
        var size = this.itemsPerPage;
        var st = (this.currPageNum * size) - size;
        hash.each(getRow, { start: st, items: size });

        t += '</table>';
        this.domNode.innerHTML = t;

        // Attach event listeners -- event will be delagated by row
        dojo.event.connect($('listViewTable'), 'onmouseover', this, 'handleMouseOver');
        dojo.event.connect($('listViewTable'), 'onmouseout', this, 'handleMouseOut');
        dojo.event.connect($('listViewTable'), 'onclick', this, 'handleClick');

        dojo.event.topic.publish('/calEvent', { action: 'navigateLoadedCollection',
            opts: null });
    };
    this.initListProps = function () {
        var items = cosmo.view.list.itemRegistry.length;
        var pages = parseInt(items/this.itemsPerPage);
        if (items % this.itemsPerPage > 0) {
            pages++;
        }
        this.itemCount =  items;
        this.pageCount = pages;
        this.currPageNum = 1;
    };
    this.goNextPage = function () {
        self.currPageNum++;
        self.render();
    };
    this.goPrevPage = function () {
        self.currPageNum--;
        self.render();
    };


    // Private methods
    this._updateSize = function () {
        if (this.parent) {
            this.width = this.parent.width - 2; // 2px for borders
            this.height = this.parent.height - CAL_TOP_NAV_HEIGHT;
        }
    };
    this._saveSuccess = function (cmd) {
        var recurOpts = cosmo.view.service.recurringEventOptions;
        var item = cmd.data;
        var saveType = cmd.saveType || null;
        var delta = cmd.delta;
        var deferred = null;
        var newItem = cmd.newItem;

        if (item.data.hasRecurrence() && saveType != recurOpts.ONLY_THIS_EVENT) {
            self.view.loadItems();
        }
        else {
            self.view.setSortAndDisplay(item);
            self._doSortAndDisplay();
        }
    };
    this._removeSuccess = function (cmd) {
        var recurOpts = cosmo.view.service.recurringEventOptions;
        var item = cmd.data;
        var opts = cmd.opts;
        var removeType = opts.removeType;

        self.clearSelectedItem();
        switch (removeType){
            case recurOpts.ALL_EVENTS:
            case recurOpts.ALL_FUTURE_EVENTS:
                self.view.loadItems();
                break;
            case recurOpts.ONLY_THIS_EVENT:
            case 'singleEvent':
                self.view.itemRegistry.removeItem(item.id);
                self._doSortAndDisplay();
                break;
        }
    };
    this._doSortAndDisplay = function (id) {
        var s = '';
        var reg = cosmo.view.list.itemRegistry;
        // If id was passed in, it means a change to the sort
        // if no id, then just re-run the current sort and re-display
        if (typeof id != 'undefined') {
            s = id.replace('listView', '').replace('Header', '');
            if (this.currSortCol == s) {
                this.currSortDir = this.currSortDir == 'Desc' ? 'Asc' : 'Desc';
            }
            else {
                this.currSortDir = cosmo.view.list.sort.defaultDirections[s.toUpperCase()];
            }
            this.currPageNum = 1;
            this.currSortCol = s;
        }
        if (cosmo.view.list.sort.doSort(reg, this.currSortCol, this.currSortDir)) {
            this.displayTable();
        }
        else {
            throw('Could not sort item registry.');
        }
    };
};

cosmo.view.list.canvas.Canvas.prototype =
  new cosmo.ui.ContentBox();

