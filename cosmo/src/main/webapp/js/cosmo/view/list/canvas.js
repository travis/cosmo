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
dojo.require('dojo.html.common');
dojo.require('dojo.string');
dojo.require("cosmo.app");
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
    // Set self to the view's canvasInstance
    this.view.canvasInstance = this;
    // UIDs for selected events keyed by the uid of
    // the currently displayed collection
    this.selectedItemIdRegistry = {};
    // Stash references to the selected object here
    // The current itemRegistry won't always have the
    // selected item loaded. If it's not in the
    // itemRegistry, pull it from here to persist the
    // collection's selected object in the detail view
    this.selectedItemCache = {};
    this.currSortCol = 'Triage';
    this.currSortDir = 'Desc';
    this.itemsPerPage = 0;
    this.itemCount = 0;
    this.pageCount = 0;
    this.currPageNum = 1;
    this.processingRow = null;

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
            case 'save':
            case 'remove':
                if (cmd.saveType != "new") {
                    this._showRowProcessing();
                }
                break;
            case 'eventsLoadSuccess':
                this.initListProps();
                this.render();
                if (this._doSortAndDisplay()) {
                    cosmo.app.hideMask();
                }
                break;
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
        // Rendering can be messages published to calEvent
        // or by window resizing
        if (!cosmo.view.list.isCurrentView()) { return false; }

        this._updateSize();
        this.setPosition(0, CAL_TOP_NAV_HEIGHT);
        this.setSize();
        this.itemsPerPage = parseInt((this.height - 15) / 21);
        this.initListProps();
        this.displayListViewTable();
    }
    this.handleMouseOver = function (e) {
        // Avoid DOM-event/DOM-node contention problems in Safari
        // Just forego the purty rollovers
        if (navigator.userAgent.indexOf('Safari/41') > -1) {
            return false;
        }
        if (e && e.target) {
            var targ = e.target;
            // In some cases we want the parent node's id -- in all
            // those cases, the event source has no id of its own
            while (!targ.id) { targ = targ.parentNode; }
            if (targ.id == 'body') { return false; }
            if (targ.id.indexOf('Header') > -1) {
                if (targ.className.indexOf('Sel') > -1) {
                    dojo.html.replaceClass(targ, 'listViewHeaderCellSelLit',
                      'listViewHeaderCellSel');
                }
                else {
                    dojo.html.addClass(targ, 'listViewHeaderCellLit');
                }
            }
            else {
                if (targ.id ==  'listView_item' +
                    self.getSelectedItemId()) { return false; }
                var ch = targ.childNodes;
                for (var i = 0; i < ch.length; i++) {
                    // Don't apply rollover effect to triage col
                    if (ch[i].className.indexOf('listViewTriage') == -1) {
                        dojo.html.addClass(ch[i], 'mouseoverItem');
                    }
                }
            }
        }
    };
    this.handleMouseOut = function (e) {
        if (e && e.target) {
            var targ = e.target;
            // In some cases we want the parent node's id -- in all
            // those cases, the event source has no id of its own
            while (!targ.id) { targ = targ.parentNode; }
            if (targ.id == 'body') { return false; }
            if (targ.id.indexOf('Header') > -1) {
                if (targ.className.indexOf('Sel') > -1) {
                    dojo.html.replaceClass(targ, 'listViewHeaderCellSel',
                      'listViewHeaderCellSelLit');
                }
                else {
                  dojo.html.removeClass(targ, 'listViewHeaderCellLit');
                }
            }
            else {
                var ch = targ.childNodes;
                for (var i = 0; i < ch.length; i++) {
                    dojo.html.removeClass(ch[i], 'mouseoverItem');
                }
            }
        }
    };
    this.handleClick = function (e) {
        if (e && e.target) {
            var targ = e.target;
            // In some cases we want the parent node's id -- in all
            // those cases, the event source has no id of its own
            while (!targ.id) { targ = targ.parentNode; }
            if (targ.id == 'body') { return false; }
            // Header cell clicked
            if (targ.id.indexOf('Header') > -1) {
                this._doSortAndDisplay(targ.id);
            }
            // Normal row cell clicked
            else {
                self.handleSelectionChange(e, targ);

            }
        }
    };
    this.handleSelectionChange = function (e, target, discardUnsavedChanges) {
        var args = Array.prototype.slice.call(arguments);
        var writeable = cosmo.app.pim.getSelectedCollectionWriteable();
        // Original selection
        var origSelection = self.getSelectedItem();
        // Paranoia check -- bail if there's no target, or the target
        // has no id -- this shouldn't ever happen, but it does in IE7
        if (!target || !target.id) { return false; }
        // New selection
        var id = target.id.replace('listView_item', '');
        var item = this.view.itemRegistry.getItem(id);

        if ((!origSelection) || (origSelection.id != item.id)) {
            // Make sure the user isn't leaving unsaved edits --
            // blow by this when re-called with explicit 'discard changes'
            // Note: we have to spoon-feed the execution context to the
            // callback methods for the buttons in the dialog, hence
            // passing the 'self' param below
            if (!discardUnsavedChanges && origSelection && writeable) {
                // Add the explicit ignore flag to the args
                args.push(true);
                // Discarding just re-invokes this call with the ignore flag
                var discardFunc = function () {
                    self.handleSelectionChange.apply(self, args);
                };
                if (!cosmo.view.handleUnsavedChanges(origSelection, discardFunc)) {
                    return false;
                }
            }

            // Deselect any original selection
            if (origSelection) {
                var origSelectionId = origSelection ? origSelection.id : '';
                var origSelectionNode = $('listView_item' + origSelectionId);
                if (origSelectionNode) {
                    ch = origSelectionNode.childNodes;
                    for (var i = 0; i < ch.length; i++) {
                        dojo.html.removeClass(ch[i], 'selectedItem');
                    }
                }

            }

            // The new selection
            var ch = target.childNodes;
            for (var i = 0; i < ch.length; i++) {
                // Don't apply selection effect to triage col
                if (ch[i].className.indexOf('listViewTriage') == -1) {
                    dojo.html.removeClass(ch[i], 'mouseoverItem');
                    dojo.html.addClass(ch[i], 'selectedItem');
                }
            }
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
        if (e.button == 2) {
              cosmo.ui.menu.HierarchicalMenuManager.showContextMenu(e,
                  cosmo.view.contextMenu.menu);
              return false;
        }

    };
    // innerHTML will be much faster for table display with
    // lots of rows
    this.displayListViewTable = function () {
        var _list = cosmo.view.list;
        var _tMap = cosmo.view.list.triageStatusCodeMappings;
        var hash = _list.itemRegistry;
        var selId = 'listView_item' + self.getSelectedItemId();
        var map = cosmo.view.list.triageStatusCodeMappings;
        var d = _createElem('div'); // Dummy div
        // Proxy icon div for getting background image properties
        var taskIcon = cosmo.ui.imagegrid.createImageIcon({ domNode: d,
            iconState: 'listViewTaskIcon' });
        var taskStyle = taskIcon.style;
        var taskBgImg = taskStyle.backgroundImage;
        // Safari 2 will render backgroundPosition, but doesn't preserve
        // the actual value set, so reconstruct it from the X-/Y-specific
        // values that it does set on the proxy div
        // This is fixed in the Safari 3 Beta, which is why we're checking
        // here for a specific version string
        var taskBgPos = (navigator.userAgent.indexOf('Safari/41') > -1) ?
            taskStyle.backgroundPositionX + ' ' + taskStyle.backgroundPositionY :
            taskStyle.backgroundPosition;
        var taskBgPos = taskStyle.backgroundPosition;
        var remainingWidth = this.width;
        // Icon/buttons living in the col headers (task, triage)
        var colHeaderIcons = {};
        var t = '';
        var r = '';
        var cols = [];
        var colCount = 0; // Used to generated the 'processing' row
        var fillCell = function (s) {
            var cell = s;
            if (s) s = dojo.string.escapeXml(s);
            return  s || '&nbsp;';
        };
        var createContentRow = function (key, val) {
            var item = val;
            var display = item.display;
            var sort = item.sort;
            var selCss = 'listView_item' + display.uid == selId ?
              ' selectedItem' : '';
            var title = fillCell(display.title);
            var who = fillCell(display.who);
            var start = fillCell(display.start);
            r = '';
            r += '<tr id="listView_item' + display.uid + '">';
            r += '<td class="listViewDataCell' + selCss + '">';
            if (display.task) {
                r += '<div style="margin: 3px 5px; width: ' + taskStyle.width +
                    '; height: ' + taskStyle.height +
                    '; font-size: 1px; background-image: ' + taskBgImg +
                    '; background-position: ' + taskBgPos + ';">&nbsp;</div>';
            }
            r += '</td>';
            r += '<td class="listViewDataCell' + selCss + '" title="' + title + '">' +
              title + '</td>';
            r += '<td class="listViewDataCell' + selCss + '" title="' + who + '">' +
              who + '</td>';
            r += '<td class="listViewDataCell' + selCss +
              '" style="white-space: nowrap;" title="' + start + '">' + start + '</td>';
            r += '<td class="listViewDataCell' +
                ' listViewTriageCell listViewTriage' +
                _tMap[item.data.getTriageStatus()] + selCss + '">' +
                fillCell(display.triage) + '</td>';
            r += '</tr>\n';
            t += r;
        }
        var size = this.itemsPerPage;
        var st = (this.currPageNum * size) - size;
        
        // Create an ordered list of the column objects, in order
        var order = cosmo.view.list.columnOrder;
        for (var i = 0; i < order.length; i++) {
            cols.push(cosmo.view.list.columns[order[i]]);
        }

        t = '<table id="listViewTable" cellpadding="0" cellspacing="0" style="width: ' + this.width + 'px;">\n';
        // Header row
        r += '<tr>';
        // Subtract static width cols from the total
        for (var i = 0; i < cols.length; i++) {
            var w = cols[i].width;
            if (w.indexOf('px') > -1) {
                remainingWidth -= parseInt(w);
            }
        }
        for (var i = 0; i < cols.length; i++) {
            var col = cols[i];
            var colStyle = '';
            var isSelected = (col.name == this.currSortCol);
            if (col.isIcon) {
                var iconPrefix = isSelected ? 'Selected' : 'Default';
                var iconDiv = _createElem('div');
                var mouseOver = function (e) { self.handleMouseOver(e); }
                var mouseOut = function (e) { self.handleMouseOut(e); }
                var click = function (e) { self.handleClick(e); }
                var colIcon = cosmo.ui.imagegrid.createImageButton({ domNode: iconDiv,
                    defaultState: col.display + iconPrefix,
                    rolloverState: col.display + iconPrefix + 'Rollover',
                    handleMouseOver: mouseOver,
                    handleMouseOut: mouseOut,
                    handleClick: click });
                iconDiv.style.margin = 'auto';
                colHeaderIcons[col.name] = iconDiv;
                colStyle += ' text-align: center;';
            }
            if (col.width.indexOf('px') > -1) {
                var w = parseInt(col.width) - 1;
            }
            else {
                var w = parseInt(col.width) / 100;
                w = parseInt(remainingWidth * w) - 1;
            }
            colStyle += ' width: ' + w + 'px;';

            r += '<td id="listView_' + col.name +
                'Header" class="listViewHeaderCell';
            if (isSelected) {
              r += ' listViewHeaderCellSel'
            }
            r += '"';
            if (colStyle) {
              r += ' style="' + colStyle + '"';
            }
            r += '>';
            if (!col.isIcon) {
                var displ = _('Dashboard.ColHeaders.' + col.display);
                r += '<div style="padding-left: 5px;" title="' + displ +
                '"><nobr>' + displ + '</nobr></div>';
            }
            r += '</td>';
            colCount++;
        }
        r += '</tr>\n';
        t += r;

        hash.each(createContentRow, { start: st, items: size });

        t += '</table>';
        // ============
        // Create the table
        // ============
        this.domNode.innerHTML = t;

        // Add column header icons
        for (var i in colHeaderIcons) {
          var icon = colHeaderIcons[i];
          $('listView_' + i + 'Header').appendChild(icon);
        }

        // Create the 'processing' row
        var row = _createElem('tr');
        var cell = _createElem('td');
        cell.colSpan = colCount - 1;
        cell.className = 'listViewDataCell selectedItem';
        cell.style.textAlign = 'center';
        cell.style.whiteSpace = 'nowrap';
        cell.innerHTML = 'Processing ...';
        row.appendChild(cell);
        var cell = _createElem('td');
        row.appendChild(cell);
        this.processingRow = row;

        // Attach event listeners -- event will be delagated by row
        dojo.event.connect($('listViewTable'), 'onmouseover',
            this, 'handleMouseOver');
        dojo.event.connect($('listViewTable'), 'onmouseout',
            this, 'handleMouseOut');
        dojo.event.connect($('listViewTable'), 'onclick',
            this, 'handleClick');
        // HACK: Do explicit single selection on right-click
        // Because the appearance of the context menu in
        // Safari 2 breaks the normal mouseout event and makes
        // it look like multi-select is enabled
        dojo.event.connect($('listViewTable'), 'oncontextmenu',
            this, 'handleClick');

        dojo.event.topic.publish('/calEvent', { action: 'navigateLoadedCollection',
            opts: null });

        return true;
    };
    this.initListProps = function () {
        this.itemCount =  cosmo.view.list.itemRegistry.length;
        this.currPageNum = 1;
        this._calcPageCount();
    };
    this.goNextPage = function () {
        self.currPageNum++;
        self.displayListViewTable();
    };
    this.goPrevPage = function () {
        self.currPageNum--;
        self.displayListViewTable();
    };

    // Private methods
    this._calcPageCount = function () {
        var items = cosmo.view.list.itemRegistry.length;
        var pages = parseInt(items/this.itemsPerPage);
        if (items % this.itemsPerPage > 0) {
            pages++;
        }
        this.pageCount = pages; 
    };
    this._updateSize = function () {
        if (this.parent) {
            this.width = this.parent.width - 2; // 2px for borders
            this.height = this.parent.height - CAL_TOP_NAV_HEIGHT;
        }
    };
    /**
     * Handles a successful update of a ListItem
     * @param cmd JS Object, the command object passed in the
     * published 'success' message
     */
    this._saveSuccess = function (cmd) {
        dojo.debug("saveSuccess: ");
        var recurOpts = cosmo.view.service.recurringEventOptions;
        var item = cmd.data
        var data = item.data;
        var saveType = cmd.saveType || null;
        dojo.debug("saveSuccess saveType: " + saveType);
        var delta = cmd.delta;
        var deferred = null;
        var newItemNote = cmd.newItemNote; // stamped Note
        var recurrenceRemoved = item.recurrenceRemoved();

        //if the event is recurring and all future or all events are changed, we need to
        //re expand the event
        if (item.data.hasRecurrence() && saveType != recurOpts.ONLY_THIS_EVENT) {
            dojo.debug("saveSuccess: has recurrence");
            //first remove the event and recurrences from the registry.
            var idsToRemove = [data.getUid()];
            if (saveType == recurOpts.ALL_FUTURE_EVENTS){
                idsToRemove.push(newItemNote.getUid());
            }
            var newRegistry = self.view.filterOutRecurrenceGroup(
                self.view.itemRegistry.clone(), idsToRemove);

            //now we have to expand out the item for the viewing range
            var deferredArray = [cosmo.app.pim.serv.getDashboardItems(data.getMaster())];
            if (saveType == recurOpts.ALL_FUTURE_EVENTS){
                deferredArray.push(cosmo.app.pim.serv.getDashboardItems(newItemNote));
            }
            deferred = new dojo.DeferredList(deferredArray);
            cosmo.util.deferred.addStdDLCallback(deferred);
            var addExpandedOccurrences = function (results) {
                var error = cosmo.util.deferred.getFirstError(results);
                var occurrences = results[0][1];
                if (results[1]){
                    var otherOccurrences = results[1][1]
                    occurrences = occurrences.concat(otherOccurrences);
                }
                var newHash = cosmo.view.list.createItemRegistry(occurrences);
                newRegistry.append(newHash);
                self.view.itemRegistry = newRegistry;
            };
            deferred.addCallback(addExpandedOccurrences);
        }
        // Non-recurring / "only this item'
        else {
            // The item just had its recurrence removed.
            // The only item that should remain is the item that was the
            // first occurrence
            if (recurrenceRemoved) {
                // Remove all the recurrence items from the list
                var newRegistry = self.view.filterOutRecurrenceGroup(
                    self.view.itemRegistry.clone(), [item.data.getUid()]);
                // Update the list
                self.view.itemRegistry = newRegistry;
                // Create a new item based on the updated version of
                // the edited ocurrence's master
                var note = item.data.getMaster();
                var id = note.getItemUid();
                var newItem = new cosmo.view.list.ListItem();
                newItem.data = note;
                newItem.id = id;
                self.view.itemRegistry.setItem(id, newItem);
                // Use the updated item from here forward -- set its precalc'd
                // sort/display props, and point the selection at it in
                // updateEventsCallback -- recurOpts.ALL_EVENTS case
                item = newItem;
            }
            self.view.setSortAndDisplay(item);
        }

        var updateEventsCallback = function () {
            dojo.debug("updateEventsCallback")
            // Don't re-render when requests are still processing
            if (!cosmo.view.service.processingQueue.length) {

                // Anything except editing an existing event requires
                // adding the selection to an item in the itemRegistry
                if (saveType) {
                    var sel = null;
                    switch (saveType) {
                        case 'new':
                            sel = item;
                            break;
                        case recurOpts.ALL_EVENTS:
                        case recurOpts.ONLY_THIS_EVENT:
                            sel = item.data.getItemUid();
                            break;
                        case recurOpts.ALL_FUTURE_EVENTS:
                            sel = newItemNote.getNoteOccurrence(
                                newItemNote.getEventStamp().getStartDate()).getItemUid();
                            break;
                            break;
                        default:
                            throw('Undefined saveType of "' + saveType +
                                '" in command object passed to saveSuccess');
                            break;

                    }
                    // Needs to be set and then gotten because sel may be
                    // an itemUID string, or the ListItem it points to
                    // has been replaced in the itemRegistry
                    self.setSelectedItem(sel);
                    sel = self.getSelectedItem();
                    dojo.event.topic.publish('/calEvent', { action: 'setSelected',
                        data: sel });
                }
            }
            else {
                dojo.debug("how many left in queue: " +
                    cosmo.view.service.processingQueue.length);
            }
        }
        deferred = deferred || cosmo.util.deferred.getFiredDeferred()
        deferred.addCallback(updateEventsCallback);
        deferred.addCallback(function (){ 
            self._calcPageCount();
            self._doSortAndDisplay();
        });
        return deferred;
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
                // If we just removed the last item, clear the form
                if (self.view.itemRegistry.length == 0) {
                    dojo.event.topic.publish('/calEvent', { 'action':
                        'clearSelected', 'data': null });
                }
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
            s = id.replace('listView_', '').replace('Header', '');
            if (this.currSortCol == s) {
                this.currSortDir = this.currSortDir == 'Desc' ? 'Asc' : 'Desc';
            }
            else {
                this.currSortDir = cosmo.view.list.columns[s.toUpperCase()].initSort;
            }
            this.currPageNum = 1;
            this.currSortCol = s;
        }
        if (cosmo.view.list.sort.doSort(reg, this.currSortCol, this.currSortDir)) {
            // Wait for the result to ensure all the DOM operations
            // are done before moving on
            var waitForIt = this.displayListViewTable();
            if (cosmo.view.list.itemRegistry.length) {
                // List view has all items loaded at once
                // in the itemRegistry -- no need for selectedItemCache
                var sel = self.getSelectedItem();
                dojo.event.topic.publish('/calEvent', { 'action':
                    'eventsDisplaySuccess', 'data': sel });

            }
            else {
                dojo.event.topic.publish('/calEvent', { 'action': 'noItems' });
            }
        }
        else {
            throw('Could not sort item registry.');
        }
        return true;
    };
    this._showRowProcessing = function () {
        var id = 'listView_item' + self.getSelectedItemId();
        var sel = $(id);
        if (sel) {
            selLast = sel.lastChild;
            procLast = this.processingRow.lastChild;
            sel.style.display = 'none';
            procLast.className = selLast.className;
            procLast.innerHTML = selLast.innerHTML;
            sel.parentNode.insertBefore(this.processingRow, sel);
        }
    };
};

cosmo.view.list.canvas.Canvas.prototype =
  new cosmo.ui.ContentBox();

