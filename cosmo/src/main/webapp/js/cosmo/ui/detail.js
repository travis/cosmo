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

/**
 * @fileoverview The event detail form that displays info for the
 * selected event
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.detail");

dojo.require("dojo.event.*");
dojo.require("dojo.lfx.*");
dojo.require("dojo.html.style");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require('cosmo.convenience');
dojo.require('cosmo.datetime.timezone');
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.ui.imagegrid");
dojo.require("cosmo.ui.DetailFormConverter");
dojo.require("cosmo.view.list.common");
dojo.require("cosmo.view.cal.common");
dojo.require("cosmo.model.Item");

cosmo.ui.detail = new function () {
    this.item = null;
    this.processingExpando = false;

    this.createFormElementsForStamp = function (stampType) {
        return new cosmo.ui.detail[stampType + 'FormElements']();
    };

    this.saveItem = function () {
        var converter = new cosmo.ui.DetailFormConverter(this.item.data);
        var deltaAndError = converter.createDelta();
        var error = deltaAndError[1];
        var delta = deltaAndError[0];

        if (error){
            cosmo.app.showErr(_('Main.DetailForm.Error'), error);
            return;
        } else {
            if (!delta.hasChanges()){
                return;
            }

            this.item.makeSnapshot();
            dojo.event.topic.publish('/calEvent', {
                action: 'saveConfirm', delta: delta, data: this.item });
        }
    };

    this.removeItem = function () {
        dojo.event.topic.publish('/calEvent',
            { action: 'removeConfirm', data: this.item });
    };

    // Utility functions
    this.createLabelDiv = function (str) {
        var d = _createElem('div');
        d.className = 'labelTextVert';
        d.appendChild(_createText((str)));
        return d;
    };

    this.createFormElemDiv = function (elem) {
        var d = _createElem('div');
        d.className = 'formElem';
        d.appendChild(elem);
        return d;
    };

    //some convienient methods for getting at the various forms and form values.

    this.getStampForm = function (stampName){
        //summary: returns the form object for the given stamp name
        stampName = stampName.toLowerCase();
        return cosmo.app.pim.baseLayout.mainApp.rightSidebar
                   .detailViewForm[stampName +"Section"].formSection.formNode;
    }

    this.getMainForm = function (){
        return cosmo.app.pim.baseLayout.mainApp.rightSidebar.detailViewForm.mainSection.formNode;
    }

    this.isStampEnabled = function (stampName){
        //summary: returns whether or not a particular stamp section is enabled
        var checkBox = $("section"+ this._upperFirstChar(stampName) +"EnableToggle");
        return checkBox.checked;
    }

    this._upperFirstChar = function (str){
        return str.charAt(0).toUpperCase() + str.substr(1,str.length -1 );
    }

};

cosmo.ui.detail.StampFormElemState = function (p) {
    var params = p || {};
    // Using typeof tests to see if these are set for a
    // particular form elem
    this.disabled = null; // Boolean
    this.value = null; // String
    this.hintText = null; // String
    for (var n in params) { this[n] = params[n]; }
};

cosmo.ui.detail.itemStamps = [
    { stampType: 'Mail',
    enablePrompt: 'Address this item',
    hasBody: true },
    { stampType: 'Event',
    enablePrompt: 'Add to calendar',
    hasBody: true },
    { stampType: 'Task',
    enablePrompt: 'Mark as a task',
    hasBody: false }
];
cosmo.ui.detail.DetailViewForm = function (p) {
    var self = this;
    var params = p || {};

    this.domNode = null;
    // If this is set, only one expando section of the
    // form will be visible at one time -- kicks in
    // automatically when the browser window is too
    // short to accommodate the entire form.
    this.accordionMode = false;
    // Title/description
    this.mainSection = null;
    // Stamp sections
    this.mailSection = null;
    this.eventSection = null;
    this.taskSection = null;
    // The total height of this ui widget, including
    // Triage Section and buttons at the bottom
    this.height = 0;

    this.stamps = cosmo.ui.detail.itemStamps;

    for (var n in params) { this[n] = params[n]; }

    this.domNode.id = this.id;

    // Markup bar
    var d = _createElem('div');
    var c = new cosmo.ui.detail.MarkupBar({ id: 'markupBar',
        parent: this, domNode: d });
    this.children.push(c);
    this.domNode.appendChild(c.domNode);
    this.markupBar = c;

    // Main section
    var d = _createElem('div');
    var c = new cosmo.ui.detail.MainSection({ parent: this,
        domNode: d });
    this.children.push(c);
    this.domNode.appendChild(c.domNode);
    this.mainSection = c;
    // Stamp sections
    var stamps = this.stamps;
    for (var i = 0; i < stamps.length; i++) {
        var st = stamps[i];
        var d = _createElem('div');
        var c = new cosmo.ui.detail.StampSection({ parent: this,
            domNode: d,
            stampType: st.stampType,
            promptText: st.enablePrompt,
            hasBody: st.hasBody });
        this.children.push(c);
        this.domNode.appendChild(c.domNode);
        this[st.stampType.toLowerCase() + 'Section'] = c;
    }
    var c = new cosmo.ui.detail.Byline();
    this.children.push(c);
    this.domNode.appendChild(c.domNode);
    this.byline = c;

    var c = new cosmo.ui.detail.ButtonSection();
    this.children.push(c);
    this.domNode.appendChild(c.domNode);
    this.buttonSection = c;
    this.renderSelf = function () {
        // Get the real, full height -- with all sections expanded
        // the first time the detail-view form renders
        if (!this.hasRendered) {
            this.height = this.domNode.offsetHeight + TOP_MENU_HEIGHT;
            this.hasRendered = true;
        }
        // Turn on accordion mode if height is too tall
        // Also check to see if we need to hide all the sections
        // to make sure the buttons show
        var winHeight = this.windowHeight || cosmo.app.pim.layout.baseLayout.height;
        if (this.height > winHeight) {
            this.accordionMode = true;
            // If the current height of the detail-view form (perhaps with
            // some sections collapsed) is too tall, collapse all the sections
            if ((this.domNode.offsetHeight + TOP_MENU_HEIGHT) > winHeight) {
                var stamps = this.stamps;
                for (var i = 0; i < stamps.length; i++) {
                    var st = stamps[i];
                    var sec = self[st.stampType.toLowerCase() + 'Section'];
                    sec.toggleExpando(false, true);
                }
            }
        }
        else {
            this.accordionMode = false;
        }
    };

    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');

    this.handlePub = function (cmd) {
        var act = cmd.action;
        var item = cmd.data;
        switch (act) {
            // Successfully displaying all the loaded items on the canvas
            case 'eventsDisplaySuccess':
            // Collections with nothing in them
            case 'noItems':
            // Items clicked to select, newly saved items
            case 'setSelected':
            // Nothing has the selection -- clear out the form
            case 'clearSelected':
                // An item has been clicked on, selected
                if (item) {
                    // Only update the values in the form if
                    // the item has actually changed -- note that
                    // in the cal, when navigating off the week
                    // where the selected item is displayed, the
                    // selected item will in the selectedItemCache
                    if (item != cosmo.ui.detail.item) {
                        self.updateFromItem(item);
                        // For brand-new items auto-focus the Title field
                        // to allow people to change the placeholder
                        // 'New Event' text quickly
                        if (cmd.saveType == 'new') {
                            this.mainSection.formNode.noteTitle.focus();
                        }
                    }
                }
                // No-item means 'clear the selection'
                // FIXME: We need better sematics for this --
                // an empty collection/week-view will also pass nothing here
                // On the other hand, the itemRegistry could now be empty
                // because the user just removed the last item, in which
                // case we need to clear out the form after all. We ought to
                // have a specific 'clear the selection' flag
                else if (act == 'clearSelected' ||
                    cosmo.view[cosmo.app.pim.currentView].itemRegistry.length) {
                    cosmo.ui.detail.item = null;
                    self.clear(true);
                    self.buttonSection.setButtons(false);
                }
                break;
            // Edited items, newly saved items (don't update newly saved
            // items; that happens when they get the selection)
            case 'saveSuccess':
                // Don't update here for newly saved items -- it wil
                // update again when the new item gets the selection,
                // resulting in weird flashing from the multiple updates
                if (cmd.saveType != 'new') {
                    self.updateFromItem(item);
                }
                break;
            case 'saveFailed':
                //self.setButtons(true, true);
                break;
            case 'saveFromForm':
                cosmo.ui.detail.saveItem();
                break;
            default:
                // Do nothing
                break;
        }
    };
};

cosmo.ui.detail.DetailViewForm.prototype =
    new cosmo.ui.ContentBox();

cosmo.ui.detail.DetailViewForm.prototype.updateFromItem =
    function (item) {

    var _html = cosmo.util.html;

    var data = item.data;
    var section = null;
    var f = null;
    var stamps = this.stamps;
    var stamp = null;

    //save the item
    cosmo.ui.detail.item = item;

    this.markupBar.render();
    this.mainSection.toggleEnabled(true);
    f = this.mainSection.formNode;
    // Values may be null -- IE coerces to string
    // "null" if we don't convert to empty string first
    f.noteTitle.value = data.getDisplayName() || '';
    f.noteDescription.value = data.getBody() || '';
    var func = cosmo.util.html.handleTextInputFocus;
    dojo.event.connect(f.noteTitle, 'onfocus', func);
    for (var i = 0; i < stamps.length; i++) {
        var st = stamps[i];
        stamp = data['get' + st.stampType + 'Stamp']();
        // Stamped -- wipe/Enable and fill in values
        if (stamp) {
            this[st.stampType.toLowerCase() + 'Section'].updateFromStamp(stamp);
        }
        // Unstamped -- wipe and disable
        else {
            this[st.stampType.toLowerCase() + 'Section'].toggleEnabled(false, {
                doCompleteDisable: false, disableStampFormElem: true });
        }
    }
    this.byline.updateFromItem(data);
    var writeable = cosmo.app.pim.getSelectedCollectionWriteable();
    this.buttonSection.setButtons(writeable);
};

cosmo.ui.detail.DetailViewForm.prototype.clear =
    function (doCompleteDisable) {
    var opts = {};
    if (doCompleteDisable) {
        opts.doCompleteDisable = true;
        opts.disableStampFormElem = true;
    }
    else {
        opts.doCompleteDisable = false;
        opts.disableStampFormElem = false;
    }
    this.markupBar.render();
    this.mainSection.toggleEnabled(false, opts);
    var stamps = this.stamps;
    for (var i = 0; i < stamps.length; i++) {
        var st = stamps[i];
        this[st.stampType.toLowerCase() + 'Section'].toggleEnabled(false,
            opts);
    }
};

cosmo.ui.detail.MarkupBar = function (p) {
    // Private vars
    // -------
    var self = this;
    var params = p || {};

    this.id = '';
    this.domNode = null; // Main node
    this.triageSection = null;

    // Override defaults with params passed in
    for (var n in params) { this[n] = params[n] };

    this.domNode.id = this.id;

    this.renderSelf = function () {
        var d = this.domNode;
        var writeable = cosmo.app.pim.getSelectedCollectionWriteable();
        var item = cosmo.ui.detail.item;
        var enabled = !!(item);

        this.clearAll();
        this.children = [];

        addTriageSection();
        addEmailThisIcon();
        // Read-only collections
        if (!writeable) {
            addReadOnlyIcon();
        }
        // Add e-mail icon when it's added to the image grid
        breakFloat();

        function addTriageSection() {
            var t = _createElem('div');
            t.id = 'triageButtonSection';
            t.className = 'floatLeft';
            var c = new cosmo.ui.detail.TriageSection({
              domNode: t, id: t.id, parent: self });
            self.children.push(c);
            d.appendChild(c.domNode);
            self.triageSection = c;
        }
        function addEmailThisIcon() {
            var doEMail = function () {
                var data = item.data;
                var timeFormat = _("Sidebar.Email.TimeFormat");
                var collection = cosmo.app.pim.getSelectedCollection();
                var subject = collection.getDisplayName() + ": " + data.getDisplayName();
                var body = [_("Sidebar.Email.Title") , data.getDisplayName() , "%0d%0a"];
                var eventStamp = data.getEventStamp();
                if (eventStamp){
                    var startDate = eventStamp.getStartDate();
                    var endDate = eventStamp.getEndDate();

                    if (startDate.tzId) {
                        body = body.concat([
                         _("Sidebar.Email.Timezone"), startDate.tzId , "%0d%0a"]);
                    }
                    body = body.concat([
                         _("Sidebar.Email.Starts") , dojo.date.strftime(startDate, timeFormat) , "%0d%0a" ,
                         _("Sidebar.Email.Ends") , dojo.date.strftime(endDate, timeFormat) , "%0d%0a"]);
                    if (eventStamp.getAllDay()) {
                        body.push(_("Sidebar.Email.AllDay") + "%0d%0a");
                    }

                    if (eventStamp.getRrule()) {
                        var rrule = eventStamp.getRrule();
                        body = body.concat([_("Sidebar.Email.Recurs") ,
                            rrule.getFrequency()]);
                        if (rrule.getEndDate()) {
                            body = body.concat([_("Sidebar.Email.EndingOn") ,
                                dojo.date.strftime(rrule.getEndDate(), timeFormat)]);
                        }
                        body.push(".%0d%0a");
                    }
                    if (eventStamp.getStatus()) {
                        body.concat([_("Sidebar.Email.Status") , eventStamp.getStatus() , "%0d%0a"]);
                    }
                }
                if (data.getBody()) {
                    body = body.concat([ ,
                        _("Sidebar.Email.Description") , data.getBody(), "%0d%0a"]);
                }
                var s = "mailto:?subject=" + subject + "&body=" + body.join("");
                location = s;
            };

            var t = _createElem('div');
            t.id = 'emailThisItemButton';
            t.className = 'floatRight';
            t = cosmo.ui.imagegrid.createImageButton({ domNode: t,
                enabled: enabled,
                handleClick: enabled ? doEMail : null,
                defaultState: 'emailButtonDefault',
                rolloverState: 'emailButtonRollover' });
            d.appendChild(t);
        }
        function addReadOnlyIcon() {
            var t = _createElem('div');
            t.id = 'readOnlyIcon';
            t.className = 'floatRight';
            t = cosmo.ui.imagegrid.createImageIcon({ domNode: t,
                iconState: 'readOnlyIcon' });
            t.style.marginTop = '3px';
            t.style.marginLeft = '6px';
            t.style.marginRight = '6px';
            d.appendChild(t);
        }
        function breakFloat() {
            var t = _createElem('div');
            t.className = 'clearBoth';
            d.appendChild(t);
        }
    }
}

cosmo.ui.detail.MarkupBar.prototype =
    new cosmo.ui.ContentBox();

cosmo.ui.detail.TriageSection = function (p) {
    var self = this;
    var params = p || {};
    var statuses = cosmo.model.Item.triageStatusCodeNumbers;
    var item = cosmo.ui.detail.item;
    var enabled = !!(item);

    this.domNode = null;
    this.currTriageStatus = item ? item.data.getTriageStatus() : null;

    for (var n in params) { this[n] = params[n]; }

    this.renderSelf = function () {
        var d = self.domNode;
        var stat = self.currTriageStatus;
        // Click handler -- grab the desired status from the id
        var hand = function (e) {
            if (e.target && e.target.id) {
                var s = e.target.id.replace('triageButton', '').toUpperCase();
                self.currTriageStatus = statuses[s];
                self.renderSelf();
            }
        };
        var createTriageButton = function (key) {
          var t = _createElem('div');
          var keyUpper = key.toUpperCase();
          var sel = (stat == statuses[keyUpper]);
          var def = sel ? 'triage' + key + 'ButtonSelected' :
              'triage' + key + 'ButtonDefault';
          var roll = sel ? 'triage' + key + 'ButtonSelected' :
              'triage' + key + 'ButtonRollover';
          t.id = 'triageButton' + key;
          t.className = 'floatLeft';
          t = cosmo.ui.imagegrid.createImageButton({ domNode: t,
              enabled: enabled,
              selected: (stat == statuses[keyUpper]),
              handleClick: enabled ? hand : null,
              defaultState: def,
              rolloverState: roll });
          d.appendChild(t);

        }
        this.clearAll();
        createTriageButton('Now');
        createTriageButton('Later');
        createTriageButton('Done');
        var t = _createElem('div');
        t.className = 'clearBoth';
        d.appendChild(t);
    };
};

cosmo.ui.detail.TriageSection.prototype =
    new cosmo.ui.ContentBox();

cosmo.ui.detail.StampSection = function (p) {
    // Private vars
    // -------
    var self = this;
    var params = p || {};

    // Public members
    // -------
    this.stampType = '';
    this.hasBody = true;
    this.id = '';
    this.promptText = '';
    this.domNode = null; // Main node
    this.headerNode = null; // Header with toolbar
    this.promptNode = null; // Stamp enabling prompt
    this.bodyNode = null; // Body with form section
    this.enablerSwitch = null; // Checkbox for toggling disabled state
    this.showHideSwitch = null // Show/hide link
    this.formSection = null; // Set of form elements for this stamp
    this.expanded = true; // Expanded/collapsed
    // IMPORTANT: initialize enabled to null, so the first
    // time around we can explicitly set enabled state to false
    // using toggleEnabled when everything is set up
    this.enabled = null; // Enabled/disabled
    this.bodyHeight = null;

    // Override defaults with params passed in
    for (var n in params) { this[n] = params[n] };

    // Use the stamp type as a basis for the DOM node ids
    // e.g., 'mailSectionExpander', 'eventSectionBody', etc.
    this.id = 'section' + this.stampType;
    setUpDOM();
    addBehaviors();
    this.toggleEnabled(false);

    // Private methods
    // -------
    function setUpDOM() {
        var d = null;
        var header = null;
        var body = null;
        var id = self.id;
        d = self.domNode;
        d.id = id;
        d.className = 'expando';

        // Header and toolbar
        // ------------------------
        header = _createElem('div');
        header.id = id + 'Header';
        header.className = 'expandoHead';
        // Use absolute-positioned foreground
        // and background divs so we can use a limited-height graphic
        // from the horizontal tiling image for a background-image
        // Background -- for bg gradient
        var bg = _createElem('div');
        bg.className = 'expandoHeadBg';
        bg.innerHTML = '&nbsp;';
        header.appendChild(bg);
        // Foreground -- append all the UI elems to this
        var fg = _createElem('div');
        fg.className = 'expandoHeadFg';
        header.appendChild(fg);
        var label = _createElem('label');
        // Enable/disable checkbox
        d = _createElem('div');
        d.id = id + 'Toggle';
        // Put the toggling checkbox in its own form -- not related
        // to the form proper that has actual values for the stamp
        var f = _createElem('form');
        // Kill form submission
        f.onsubmit = function () { return false; };
        var ch = _createElem('input');
        ch.type = 'checkbox';
        ch.id = id + 'EnableToggle';
        ch.name = id + 'EnableToggle';
        self.enablerSwitch = ch;
        f.appendChild(ch);
        d.appendChild(f);
        d.className = 'expandoEnableCheckbox floatLeft';
        label.appendChild(d);
        fg.appendChild(label);
        // Title
        d = _createElem('div');
        d.className = 'expandoPrompt floatLeft';
        d.id = id + 'Title';
        d.innerHTML = self.promptText;
        label.appendChild(d);
        self.promptNode = d;
        if (self.hasBody) {
            // Show/hide link
            d = _createElem('div');
            d.className = 'expandoToggle';
            d.id = id + 'Expander';
            var a = _createElem('a');
            a.id = id + 'showHideToggle';
            self.showHideSwitch = a;
            a.appendChild(_createText(_('Main.DetailForm.Hide')));
            d.appendChild(a);
            fg.appendChild(d);
        }
        d = _createElem('div');
        d.className = 'clearBoth';
        header.appendChild(d);
        self.headerNode = header;
        self.domNode.appendChild(header);
        // Body
        // ------------------------
        if (self.hasBody) {
            body = _createElem('div');
            body.id = id + 'Body';
            body.className = 'expandoBody';
            self.formSection = cosmo.ui.detail.createFormElementsForStamp(self.stampType);
            if (!self.formSection.domNode) {
               throw('Form section for ' + self.stampType + ' has no DOM node.');
            }
            body.appendChild(self.formSection.domNode);
            self.bodyNode = body;
            self.domNode.appendChild(body);
        }
    }

    function addBehaviors() {
        // Toggle enabled state for all form sections
        dojo.event.connect(self.enablerSwitch, 'onclick',
            self, 'toggleEnabled');
        // Form sections with no body have no expando toggle link
        if (self.hasBody) {
            dojo.event.connect(self.showHideSwitch, 'onclick',
                self, 'toggleExpando');
        }
    }
}

cosmo.ui.detail.StampSection.prototype =
    new cosmo.ui.ContentBox();

cosmo.ui.detail.StampSection.prototype.toggleExpando = function (p, accordion) {
    // Easier if we can treat all the form sections the same way
    // Just ignore expando calls for form sections with no body
    if (!this.bodyNode) {
        return false;
    }
    // Dojo bug http://trac.dojotoolkit.org/ticket/1776
    // Set processing lock: Don't trigger again until
    // animation completes -- Dojo doesn't allow an explicit
    // target height to be passed to the wipeIn; it guesses
    // based on the height of the node when the animation
    // begins. If the wipeIn is initiated when the wipeOut
    // is in progress, it mistakes the truncated height as the
    // desired target height. Setting a lock prevents this
    // from happening -- NOTE, the lock has to be removed as
    // a callback from the animation, otherwise it gets removed
    // before the animation has really completed.
    // -------------------------
    // If this is being called from accordion mode, bypass
    // the animation lock -- multiple sections need to collapse
    // at the same time, and since this is not user-invoked,
    // there are no issues with the Dojo bug above
    if (accordion) {
        // Dummy var for anim callback
        var f = null;
    }
    // This is normal, user-mode -- go through the locking
    // mechanism
    else {
        if (cosmo.ui.detail.processingExpando) {
            return false;
        }
        else {
            // Add the animation processing lock
            cosmo.ui.detail.processingExpando = true;
            // Callback to remove the lock
            var f = function () { cosmo.ui.detail.processingExpando = false; }
        }
    }

    // Allow to be passed in explicitly, or just trigger toggle
    var doShow = typeof p == 'boolean' ? p : !this.expanded;
    var display = '';
    var animKey = '';
    if (doShow) {
        var dvForm = this.parent;
        if (dvForm.accordionMode) {
            var stamps = dvForm.stamps;
            for (var i = 0; i < stamps.length; i++) {
                var st = stamps[i];
                var sec = dvForm[st.stampType.toLowerCase() + 'Section'];
                if (sec != this) {
                    sec.toggleExpando(false, true);
                }
            }
        }
        this.expanded = true;
        display = _('Main.DetailForm.Hide');
        animKey = 'wipeIn';
    }
    else {
        this.expanded = false;
        display = _('Main.DetailForm.Show');
        animKey = 'wipeOut';
    }
    // Toggle the switch text
    if (dojo.render.html.ie || dojo.render.html.safari) {
        this.showHideSwitch.innerText = display;
    }
    else {
        this.showHideSwitch.textContent = display;
    }
    // Do the expando animation
    dojo.lfx[animKey](this.bodyNode, 500, null, f).play();
}

cosmo.ui.detail.StampSection.prototype.toggleEnabled = function (e, o) {
    var opts = o || {};
    this.enablerSwitch.disabled = opts.doCompleteDisable;
    // Allow explicit enabled state to be passed
    if (typeof e == 'boolean') {
        // Don't bother enabling and setting up default state
        // if already enabled
        if (e == this.enabled) { return false; }
        this.enabled = e;
        this.enablerSwitch.checked = e;
        opts.setUpDefaults = (opts.setUpDefaults == false) ? false : true;
        opts.disableStampFormElem = (opts.disableStampFormElem == false) ? false : true;
    }
    // Otherwise it's just a DOM event, trigger toggle
    else {
        this.enabled = !this.enabled;
        // Don't pass click event along to the expando
        // when enabled/expanded states already match
        if (this.enabled != this.expanded) { this.toggleExpando(); }
        // Don't need to set this.enablerSwitch.checked --
        // this code was called by checking/unchecking the box
        opts.setUpDefaults = true;
        opts.disableStampFormElem = !this.enabled;
    }
    if (this.hasBody) {
        this.formSection.toggleEnabled(this.enabled, opts);
    }
}

cosmo.ui.detail.StampSection.prototype.updateFromStamp = function (stamp) {
    this.toggleEnabled(true, { disableStampFormElem: false, setUpDefaults: false });
    if (this.hasBody) {
        this.formSection.updateFromStamp(stamp);
    }
}

cosmo.ui.detail.StampFormElements = function () {
    // Public members
    // -------
    this.domNode = _createElem('div');
    this.formNode = _createElem('form');
    this.enabled = false;
    // Hint text for text inputs, default dimmed states, etc.
    this.elementDefaultStates = {};
    // Kill form submission
    this.formNode.onsubmit = function () { return false; };

    // Private methods
    // -------
    // Creates all the DOM for the elements
    function setUpDOM() {}
    // Dims elements, sets hint text in text inputs, etc.
    function setDefaultElemState() {}
    // Adds behaviors like disabling or select options
    // loading based on changes to other form inputs
    function addBehaviors() {}

    // Interface methods
    // -------
    // Updates all the input values based on a stamp
    this.updateFromStamp = function (stamp) {};

    // Ugly hacks
    // -------
    // Prevent form submission from hitting Enter key
    // while in a text box
    dojo.event.connect(this.formNode, 'onsubmit', function (e) {
        e.stopPropagation();
        e.preventDefault();
        return false;
    });
}

cosmo.ui.detail.StampFormElements.prototype =
    new cosmo.ui.ContentBox();

cosmo.ui.detail.StampFormElements.prototype.toggleEnabled
    = function (explicit, o) {
    var opts = o || {};
    var toggleText = function (tags, isEnabled) {
        var key = isEnabled ? 'remove' : 'add';
        for (var i = 0; i < tags.length; i++) {
            dojo.html[key + 'Class'](tags[i], 'disabledText');
        }
    }
    // If passed explicitly, reset the enabled prop
    if (typeof explicit == 'boolean') {
        this.enabled = explicit;
    }
    // Otherwise it's just the DOM event, trigger toggle
    else {
        this.enabled = !this.enabled;
    }

    // Disable/enable label text
    // ----------
    // Don't disable text when just clearing form for update
    // with a new selected item, but always re-enabled text
    // when toggling a form section on
    if (opts.disableStampFormElem || this.enabled) {
        var tagTypes = ['td','div','span'];
        for (var i in tagTypes) {
            var tags = this.domNode.getElementsByTagName(tagTypes[i]);
            toggleText(tags, this.enabled);
        }
    }

    // Disable/enable form elements
    // ----------
    var elems = cosmo.util.html.getFormElemNames(this.formNode);
    if (this.enabled) {
        for (var i in elems) {
            var elem = this.formNode[i];
            var state = this.elementDefaultStates[i];
            var elemType = elems[i];
            cosmo.util.html.enableFormElem(elem, elemType);
            if (opts.setUpDefaults != false) {
                this.setElemDefaultState(elem, elemType, state);
            }
            else {
                cosmo.util.html.clearFormElem(elem, elemType);
            }
        }
    }
    else {
        for (var i in elems) {
            var elem = this.formNode[i];
            var elemType = elems[i];
            if (opts.disableStampFormElem) {
                cosmo.util.html.clearAndDisableFormElem(elem, elemType);
            }
            else {
                cosmo.util.html.clearFormElem(elem, elemType);
            }
        }
    }
};

cosmo.ui.detail.StampFormElements.prototype.setElemDefaultState =
    function (elem, elemType, state) {
    if (!state) {
        cosmo.util.html.clearFormElem(elem, elemType);
    }
    else {
        for (var key in state) {
            var val = state[key];
            switch (key) {
                case 'hintText':
                    if (typeof val == 'string') {
                        cosmo.util.html.setTextInput(elem, val, true);
                    }
                    break;
                case 'disabled':
                    if (typeof val == 'boolean') {
                        elem.disabled = val;
                    }
                    break;
            }
        }
    }
};

cosmo.ui.detail.MainSection = function () {
    var _html = cosmo.util.html;
    var d = _createElem('div');
    var f = _createElem('form');
    // Kill form submission
    f.onsubmit = function () { return false; };

    d.id = 'detailViewMainFormSection';

    // Title
    var t = cosmo.ui.detail.createLabelDiv(_(
        'Main.DetailForm.displayName'));
    f.appendChild(t);
    var elem = _html.createInput({ type: 'text',
        id: 'noteTitle',
        name: 'noteTitle',
        size: 28,
        maxlength: 100,
        value: '',
        className: 'inputText' });
    elem.style.width = '220px';
    var t =  cosmo.ui.detail.createFormElemDiv(elem);
    f.appendChild(t);
    var t = cosmo.ui.detail.createLabelDiv(_(
        'Main.DetailForm.Description'));
    f.appendChild(t);
    var elem = _createElem('textarea');
    elem.className = 'inputText';
    elem.id = 'noteDescription';
    elem.name = 'noteDescription';
    elem.cols = '28';
    elem.rows = '4';
    elem.style.width = '220px';
    var t = _createElem('div');
    t.appendChild(elem);
    f.appendChild(t);

    this.formNode = f;
    d.appendChild(f);
    this.domNode = d;
}

cosmo.ui.detail.MainSection.prototype =
    new cosmo.ui.detail.StampFormElements();

cosmo.ui.detail.MailFormElements = function () {
    var d = this.domNode;
    var f = this.formNode;
    var _html = cosmo.util.html;

    setUpDOM();

    function setUpDOM() {
        var table = _createElem('table');
        var tbody = _createElem('tbody');
        var mailRow = function (label, name) {
            var tr = null;
            var td = null;
            tr = _createElem('tr');
            td = _createElem('td');
            td.style.width = '36px';
            td.style.textAlign = 'right';
            td.innerHTML = label;
            td.className = 'labelTextHoriz';
            tr.appendChild(td);
            td = _createElem('td');
            td.style.padding = '2px';
            var elem = _html.createInput({ type: 'text',
                id: 'mail' + name,
                name: 'mail' + name,
                size: 20,
                maxlength: 100,
                value: '',
                className: 'inputText' });
            elem.style.width = '182px';
            var func = cosmo.util.html.handleTextInputFocus;
            dojo.event.connect(elem, 'onfocus', func);
            td.appendChild(elem);
            tr.appendChild(td);
            return tr;
        }
        d.id = 'mailFormSection';
        d.style.padding = '8px';
        table.cellPadding = '0';
        table.cellSpacing = '0';
        table.appendChild(tbody);

        tbody.appendChild(mailRow(_('Main.DetailForm.From'), 'From'));
        tbody.appendChild(mailRow(_('Main.DetailForm.To'), 'To'));
        tbody.appendChild(mailRow(_('Main.DetailForm.Cc'), 'Cc'));
        tbody.appendChild(mailRow(_('Main.DetailForm.Bcc'), 'Bcc'));

        f.appendChild(table);
        d.appendChild(f);
    }
    // Interface methods
    // -------
    this.updateFromStamp = function (stamp) {
        function joinVals(a) {
            if (a && a.length) {
                return a.join(', ');
            }
            else {
                return '';
            }
        }
        var f = this.formNode;
        // Values may be null -- IE coerces to string
        // "null" if we don't convert to empty string first
        f.mailFrom.value = stamp.getFromAddress() || '';
        f.mailTo.value = stamp.getToAddress() || '';
        f.mailCc.value = stamp.getCcAddress() || '';
        f.mailBcc.value = stamp.getBccAddress() || '';
    }
};
cosmo.ui.detail.MailFormElements.prototype =
    new cosmo.ui.detail.StampFormElements();

cosmo.ui.detail.EventFormElements= function () {
    var self = this;
    var d = this.domNode;
    var f = this.formNode;
    var formElements = {};
    var _html = cosmo.util.html;

    setUpDOM();
    setDefaultElemState();
    addBehaviors();

    // Manually add elements to a fake form collection
    // we can use before appending to DOM
    function addToHashAndReturn (elem) {
        var name = elem.name;
        var type = elem.type;
        if (type == 'radio' || type == 'checkbox') {
            if (typeof formElements[name] != 'undefined') {
                if (!formElements[name].length) {
                    formElements[name] = [formElements[name]];
                }
                formElements[name].push(elem);
            }
            else {
                formElements[name] = elem;
            }
        }
        else {
            formElements[name] = elem;
        }
        return elem;
    }

    // Private methods
    // -------
    function createDateTimeInputs(label, name) {
        var d = _createElem('div');
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.' + label));
        d.appendChild(t);
        var elem = addToHashAndReturn(
            _html.createInput({ type: 'text',
                id: name + 'Date',
                name: name + 'Date',
                size: 10,
                maxlength: 10,
                value: '',
                className: 'inputText' }));
        var t = cosmo.ui.detail.createFormElemDiv(elem);
        t.style.whiteSpace = 'nowrap';
        t.appendChild(_html.nbsp());
        t.appendChild(_createText(
            _('Main.DetailForm.At')));
        t.appendChild(_html.nbsp());
        var elem = addToHashAndReturn(
            _html.createInput({ type: 'text',
                id: name + 'Time',
                name:name + 'Time',
                size: 5,
                maxlength: 5,
                value: '',
                className: 'inputText' }));
        t.appendChild(elem);
        t.appendChild(_html.nbsp());s
        var elem = addToHashAndReturn(
            _html.createInput({ type: 'radio',
                id: name + 'MeridianAM',
                name: name + 'Meridian',
                value: "am" }));
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        t.appendChild(_createText(
            _('App.AM')));
        t.appendChild(_html.nbsp());
        var elem = addToHashAndReturn(
            _html.createInput({ type: 'radio',
                id: name + 'MeridianPM',
                name: name + 'Meridian',
                value: "pm" }));
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        t.appendChild(_createText(
            _('App.PM')));
        d.appendChild(t);
        return d;
    }

    function getTimezoneRegionOptions(){
        var options = [];
        var option = {opt: null,
                     text: _("Main.DetailForm.Region")};
        options.push(option);
        var regions = cosmo.datetime.timezone.REGIONS;
        for (var x = 0; x < regions.length; x++){
            option = { text: regions[x], value: regions[x]};
            options.push(option);
        }
        return options;
    }

    function getTimezoneIdOptions(region){
        var tzIds = region ? cosmo.datetime.timezone.getTzIdsForRegion(
            region).sort() : null;
        var options = [{
            text: _("Main.DetailForm.TimezoneSelector.None"),
            value: "" }];
        if (tzIds){
            dojo.lang.map(tzIds, function (tzId) {
                // Strip off the Region, turn underscores into spaces for display
                options.push({text:tzId.substr(
                    tzId.indexOf("/") + 1).replace(/_/g," "), value:tzId});
            });
        }

        return options;
    }
    function clearTimezone() {
        f.tzRegion.selectedIndex = 0;
        f.tzId.selectedIndex = 0;
        cosmo.util.html.setSelectOptions(f.tzId, getTimezoneIdOptions());
    }
    function getStatusOpt() {
        var statusOpt = [];
        var opt = null;
        var str = '';

        for (var i in cosmo.model.EventStatus) {
            opt = new Object();
            str = cosmo.model.EventStatus[i];
            if(str == cosmo.model.EventStatus.FYI) {
                opt.text = i;
            }
            else {
                opt.text = dojo.string.capitalize(i.toLowerCase());
            }
            // Make sure null values convert to empty
            // string, not the string "null"
            opt.value = str || '';
            statusOpt.push(opt);
        }
        return statusOpt;
    }
    function getRecurOpt() {
        var recurOpt = [];
        var opt = null;
        var str = '';

        opt = new Object();
        opt.text = _("Main.DetailForm.RecurrenceInterval.Once");
        opt.value = '';
        recurOpt.push(opt);
        for (var i in cosmo.model.RRULE_FREQUENCIES) {
            opt = new Object();
            str = cosmo.model.RRULE_FREQUENCIES[i];
            opt.text = _("Main.DetailForm.RecurrenceInterval." + dojo.string.capitalize(str));
            opt.value = str;
            recurOpt.push(opt);
        }
        return recurOpt;
    }

    function setUpDOM() {
        d.id = 'eventFormSection';
        d.style.padding = '8px';
        // Location
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.Location'));
        f.appendChild(t);
        var elem = addToHashAndReturn(
            _html.createInput({ type: 'text',
                id: 'eventLocation',
                name: 'eventLocation',
                size: 28,
                maxlength: 100,
                value: '',
                className: 'inputText' }));
        elem.style.width = '220px';
        var t =  cosmo.ui.detail.createFormElemDiv(elem);
        f.appendChild(t);
        // All-day checkbox
        var elem = addToHashAndReturn(
            _html.createInput({ type: 'checkbox',
                id: 'eventAllDay',
                name: 'eventAllDay',
                value: 'true' }));
        var t =  cosmo.ui.detail.createFormElemDiv(elem);
        t.appendChild(_html.nbsp());
        t.appendChild(_createText('All day'));
        f.appendChild(t);
        // Event start
        var t = createDateTimeInputs('startDate', 'start');
        f.appendChild(t);
        // Event end
        var t = createDateTimeInputs('endDate', 'end');
        f.appendChild(t);
        // Timezone
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.Timezone'));
        t.style.whiteSpace = 'nowrap';
        t.className += ' formElem';
        t.appendChild(_html.nbsp());
        var elem = addToHashAndReturn(
            _html.createSelect({ id: 'tzRegion',
                name: 'tzRegion',
                multiple: false,
                className: 'selectElem',
                options: getTimezoneRegionOptions() }));
        t.appendChild(_html.nbsp());
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        var elem = addToHashAndReturn(
            _html.createSelect({ id: 'tzId',
                name: 'tzId',
                multiple: false,
                className: 'selectElem',
                options: getTimezoneIdOptions(null) }));
        elem.style.width = '100px';
        t.appendChild(elem);
        f.appendChild(t);
        // Event status
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.Status'));
        t.className += ' formElem';
        t.appendChild(_html.nbsp());
        var elem = addToHashAndReturn(
            _html.createSelect({ id: 'eventStatus',
                name: 'eventStatus',
                multi: false,
                options: getStatusOpt(),
                className: 'selectElem' }));
        t.appendChild(_html.nbsp());
        t.appendChild(elem);
        f.appendChild(t);
        // Recurrence
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.rrule'));
        t.className += ' formElem';
        t.style.whiteSpace = 'nowrap';
        var elem = addToHashAndReturn(
            _html.createSelect({ id: 'recurrenceInterval',
                name: 'recurrenceInterval',
                multi: false,
                options: getRecurOpt(),
                className: 'selectElem' }));
        t.appendChild(_html.nbsp());
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        t.appendChild(_createText(_('Main.DetailForm.Ending')));
        t.appendChild(_html.nbsp());
        var elem = addToHashAndReturn(
            _html.createInput({ type: 'text',
                id: 'recurrenceEnd',
                name: 'recurrenceEnd',
                size: 10,
                maxlength: 10,
                value: '',
                className: 'inputText' }));
        t.appendChild(elem);
        f.appendChild(t);
        d.appendChild(f);
    }

    function setDefaultElemState() {
        var _st = cosmo.ui.detail.StampFormElemState;
        self.elementDefaultStates = {
            startDate: new _st({ hintText: 'mm/dd/yyyy' }),
            startTime: new _st({ hintText: 'hh:mm' }),
            endDate: new _st({ hintText: 'mm/dd/yyyy' }),
            endTime: new _st({ hintText: 'hh:mm' }),
            recurrenceEnd: new _st({ disabled: true })
        };
    }

    function addBehaviors() {
        // Make hint text in text inputs disappear on focus
        var func = cosmo.util.html.handleTextInputFocus;
        var txtIn = ['eventLocation', 'startDate',
            'startTime', 'endDate', 'endTime', 'recurrenceEnd'];
        for (var el in txtIn) {
            dojo.event.connect(formElements[txtIn[el]], 'onfocus', func);
        }
        // Clear out time inputs if All-day checkbox is checked
        // Unchecking does nothing -- this would create an anytime
        // event, adding a start/end time would create a timed event item
        var func = function (e) {
            var allDay = e.target.checked;
            var handlerFunc;
            if (allDay) {
                handlerFunc = cosmo.util.html.clearAndDisableFormElem;
            }
            else {
                handlerFunc = cosmo.util.html.enableFormElem;
            }
            handlerFunc(formElements.startTime, 'text');
            handlerFunc(formElements.endTime, 'text');
            handlerFunc(formElements.startMeridian, 'radio');
            handlerFunc(formElements.endMeridian, 'radio');
            handlerFunc(formElements.tzRegion, 'select');
            handlerFunc(formElements.tzId, 'select');
        };
        // All-day event / normal event toggling
        dojo.event.connect(formElements.eventAllDay, 'onclick', func);
        // Recurrence -- disable 'ending' text box if event
        // does not recur
        var elem = formElements.recurrenceInterval;
        var func = function () {
            var txtElem = formElements.recurrenceEnd;
            if (elem.selectedIndex == 0) {
                cosmo.util.html.clearAndDisableFormElem(txtElem, 'text');
            } else {
                cosmo.util.html.enableFormElem(txtElem, 'text');
                if (!txtElem.value){
                    cosmo.util.html.setTextInput(txtElem, 'mm/dd/yyyy', true);
                }
            }
        }
        dojo.event.connect(elem, 'onchange', func);

        // Timezone selector -- selecting region should populate the
        // tz selector
        var func = function (e) {
            var r = e.target.value
            var tzIds = r ?
                cosmo.datetime.timezone.getTzIdsForRegion(r).sort() : null;
            var options = [{
                text: _("Main.DetailForm.TimezoneSelector.None"),
                value: "" }];
            if (tzIds){
                dojo.lang.map(tzIds, function (tzId) {
                    //Strip off the Region, turn underscores into spaces for display
                    options.push({text:tzId.substr(
                        tzId.indexOf("/") + 1).replace(/_/g," "), value:tzId});
                });
            }
            _html.setSelectOptions(formElements.tzId, options);
        };
        dojo.event.connect(formElements.tzRegion, 'onchange', func);
        dojo.event.connect(formElements.eventAllDay, 'onchange', self.enableDisableEventStatus);
        dojo.event.connect(formElements.endTime, 'onblur', self.enableDisableEventStatus);
    }

    // Interface methods
    // -------
    this.updateFromStamp = function (stamp) {
        var setTimeElem = function (form, name, dt, untimed, allDay) {
            var timeElem = null;
            var meridianElem = null;
            var str = '';

            timeElem = form[name + 'Time'];
            meridianElem = form[name + 'Meridian'];
            if (dt && !untimed) {
                str = dt.strftime('%I:%M');
                // Trim leading zero if need be
                str = str.indexOf('0') == 0 ? str.substr(1) : str;
                meridianElem[1].checked = false;
                meridianElem[0].checked = false;
                if (dt.getHours() > 11) {
                    meridianElem[1].checked = true;
                }
                else {
                    meridianElem[0].checked = true;
                }
                _html.setTextInput(timeElem, str);
            }
            else {
                meridianElem[1].checked = false;
                meridianElem[0].checked = false;
                timeElem.value = '';
            }
            meridianElem[1].disabled = allDay
            meridianElem[0].disabled = allDay;
            timeElem.disabled = allDay
        };
        var allDay = stamp.getAllDay();
        var anyTime = stamp.getAnyTime();
        var untimed = allDay || anyTime;
        f.eventAllDay.checked = allDay;
        _html.setTextInput(f.eventLocation, stamp.getLocation() || '');
        var start = stamp.getStartDate();
        _html.setTextInput(f.startDate, start.strftime('%m/%d/%Y'));
        setTimeElem(f, 'start', start, untimed, allDay);
        var end = stamp.getEndDate();
        _html.setTextInput(f.endDate, end.strftime('%m/%d/%Y'));
        setTimeElem(f, 'end', end, untimed, allDay);
        if (start.tzId){
            var tz = cosmo.datetime.timezone.getTimezone(start.tzId);
            if (!tz){
                clearTimezone();
                var options = {text:start.tzId, value:start.tzId};
                _html.setSelectOptions(f.tzId, [options]);
                f.tzId.selectedIndex = 0;
            }
            else {
                //we use this tzid in case the event has a "legacy" tzId,
                //like "US/Pacific" as opposed to "America/Los_angeles"
                var tzId = tz.tzId;
                var region = tzId.split("/")[0];
                _html.setSelect(f.tzRegion, region);
                var opt = getTimezoneIdOptions(region);
                _html.setSelectOptions(f.tzId, opt);
                _html.setSelect(f.tzId, tzId);
            }
        }
        else {
            clearTimezone();
            f.tzRegion.disabled = allDay;
            f.tzId.disabled = allDay;
        }
        var stat = stamp.getStatus();
        if (stat) {
            _html.setSelect(f.eventStatus, stat);
        }
        else {
            _html.setSelect(f.eventStatus, "CONFIRMED");
        }
        var recur = stamp.getRrule();
        var recurEnd = f.recurrenceEnd;
        if (recur && recur.isSupported()) {
            _html.setSelect(f.recurrenceInterval, recur.getFrequency());
            _html.enableFormElem(recurEnd, 'text');
            if (recur.getEndDate()) {
                _html.setTextInput(recurEnd,
                    dojo.date.strftime(recur.getEndDate(), '%m/%d/%Y'), false, false);
            }
            else {
                _html.setTextInput(recurEnd, 'mm/dd/yyyy', true, false);
            }
        }
        else {
            _html.setSelect(f.recurrenceInterval, '');
            _html.clearAndDisableFormElem(recurEnd, 'text');
        }

        this.enableDisableEventStatus();
    };

};

cosmo.ui.detail.EventFormElements.prototype =
    new cosmo.ui.detail.StampFormElements();

cosmo.ui.detail.EventFormElements.prototype.enableDisableEventStatus = function (){
        //summary: disable the event status if item is anytime or attime,
        //         enable it otherwise.
        // FIXME: Since these types of items are supposed to have no
        // status, we should probably setting the value of the select
        // to 'empty' by dynamically adding an empty option  --
        // right now it looks like the items have a 'Confirmed' status
        var html = cosmo.util.html;
        var detail = cosmo.ui.detail;
        var form = detail.getStampForm("event");
        var show = true;
        var startTime = html.getFormValue(form, "startTime");
        var endTime = html.getFormValue(form, "endTime");
        var startDate = html.getFormValue(form, "startDate");
        var endDate  = html.getFormValue(form, "endDate");

        if (html.getFormValue(form, "eventAllDay") == "0"){
            if (!endTime || endTime == "hh:mm"
               || ( (startTime == endTime && startDate == endDate))){
                show = false;
            }
        }

        form["eventStatus"].disabled = !show;
}

cosmo.ui.detail.Byline = function () {
    this.domNode = _createElem("div");
    this.domNode.id = "detailViewByline";
    // Link the action codes in the model to the verbs used in the
    // localized string resources, e.g., 100 => 'Edited'
    this._keyList = new function () {
        var actionList = ['Edited', 'Queued', 'Sent', 'Updated', 'Created'];
        for (var i = 0; i < actionList.length; i++) {
            var act = actionList[i];
            var key = cosmo.model['ACTION_' + act.toUpperCase()];
            this[key] = act;
        }
    };
    this.updateFromItem = function (item){
        var modby = item.getModifiedBy();
        var actionCode = modby.getAction();
        var userId = modby.getUserId();

        // Blow away any previous content
        this.clearAll();

        if (userId) {
            // The action in question
            var d = _createElem('div');
            d.className = 'detailViewBylineEntry';
            d.appendChild(_createText(_('Main.DetailForm.Byline.' +
                this._keyList[actionCode])));
            this.domNode.appendChild(d);

            // The responsible party
            var d = _createElem('div');
            d.className = 'detailViewBylineEntry';
            var label = _('Main.DetailForm.Byline.Who');
            if (label && userId) {
                var str = label + ' ' + userId;
                d.appendChild(_createText(str));
                d.title = str;
            }
            this.domNode.appendChild(d);

            // When did the action happen
            var dt = new cosmo.datetime.Date();
            dt.updateFromUTC(modby.getTimeStamp());
            // Desired date format, e.g., "11/26/07, at 6:00:58 PM"
            dt = dt.strftime(_("Main.DetailForm.Byline.DateFormat"));
            var d = _createElem('div');
            d.className = 'detailViewBylineEntry';
            var label = _('Main.DetailForm.Byline.When');
            if (label && userId) {
                var str = label + ' ' + dt;
                d.appendChild(_createText(str));
                d.title = str;
            }
            this.domNode.appendChild(d);
        }
    };
};
cosmo.ui.detail.Byline.prototype = new cosmo.ui.ContentBox();

cosmo.ui.detail.ButtonSection = function () {
    var self = this;

    // Public members
    this.domNode = _createElem('div');
    this.removeButtonNode = null;
    this.saveButtonNode = null;
    this.removeButton = null;
    this.saveButton = null;

    // Interface methods
    // -------
    this.setButtons = function (enabled) {
        var btns = ['Remove', 'Save'];
        for (var i = 0; i < btns.length; i++) {
            var key = btns[i].toLowerCase();
            var btn = this[key + 'Button'];
            if (btn) {
                btn.destroy();
            }
            var func = enabled ? dojo.lang.hitch(cosmo.ui.detail,cosmo.ui.detail[key + 'Item']) : null;
            this[key + 'Button'] = dojo.widget.createWidget("cosmo:Button", {
                text: _("App.Button." + btns[i]),
                id: "detail" + btns[i] + "Button",
                handleOnClick: func,
                enabled: enabled },
                this[key + 'ButtonNode'], 'last');
        }
    };

    setUpDOM();
    this.setButtons(false);

    // Private methods
    // -------
    function setUpDOM() {
        var d = self.domNode;
        d.style.padding = '6px';
        var t = _createElem('div');
        t.id = 'detailRemoveButtonContainer';
        t.className = 'floatLeft';
        self.removeButtonNode = t;
        d.appendChild(t);
        var t = _createElem('div');
        t.id = 'detailSaveButtonContainer';
        t.className = 'floatRight';
        self.saveButtonNode = t;
        d.appendChild(t);
        var t = _createElem('div');
        t.className = 'clearBoth';
        d.appendChild(t);
    }
};

cosmo.ui.detail.ButtonSection.prototype =
    new cosmo.ui.ContentBox();



