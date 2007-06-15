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
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require('cosmo.convenience');
dojo.require('cosmo.datetime.timezone');
dojo.require("cosmo.ui.widget.Button");

cosmo.ui.detail = new function () {
    this.getFormElementsForStamp = function (stampType) {
        return new cosmo.ui.detail[stampType + 'FormElements']();
    };
    this.saveItem = function () {
        alert('save');
    };
    this.removeItem = function () {
        alert('remove');
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
    this.mainSection = null;
    this.mailSection = null;
    this.eventSection = null;
    this.taskSection = null;
    this.stamps = cosmo.ui.detail.itemStamps;

    for (var n in params) { this[n] = params[n]; }

    // Markup bar
    /*
    var d = _createElem('div');
    var c = new cosmo.ui.detail.MarkupBar({ id: 'markupBar',
        parent: this, domNode: d });
    this.children.push(c);
    this.domNode.appendChild(c.domNode);
    this.markupBar = c;
    */

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

    var c = new cosmo.ui.detail.ButtonSection();
    this.children.push(c);
    this.domNode.appendChild(c.domNode);
    this.buttonSection = c;

    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');

    this.handlePub = function (cmd) {
        var act = cmd.action;
        var item = cmd.data;
        switch (act) {
            case 'eventsLoadPrepare':
                //self.clear();
                break;
            case 'eventsDisplaySuccess':
                // FIXME: This may not be needed if there's always
                // an item that gets the default selection
                //self.updateFromItem(item);
                //console.log('asdf');
                //self.buttonSection.setButtons(true);
                /*
                toggleReadOnlyIcon();
                */
                break;
            case 'saveFromForm':
                //saveCalEvent(ev);
                break;
            case 'setSelected':
                self.updateFromItem(item);
                self.buttonSection.setButtons(true);
                break;
            case 'saveSuccess':
                /*
                // Changes have placed the saved event off-canvas
                if (!cmd.qualifier.onCanvas) {
                    self.setButtons(false, false);
                    self.clear();
                }
                // Saved event is still in view
                else {
                    self.updateFromEvent(ev);
                    self.setButtons(true, true);
                    // If event title is 'New Event', auto-focus/select
                    // title field to make it easy to give it a real title
                    if (ev.data.title == _('Main.NewEvent')) {
                        var f = function () {
                            self.form.eventtitle.select();
                            self.form.eventtitle.focus();
                        }
                        setTimeout(f, 10);
                    }
                }
                */
                break;
            case 'saveFailed':
                //self.setButtons(true, true);
                break;
            case 'noItems':
                //self.setButtons(false, false);
                //self.clear();
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
    function (i) {

    var _html = cosmo.util.html;

    var item = i || cosmo.view.cal.canvas.getSelectedEvent();
    var data = item.data;
    var section = null;
    var f = null;
    var stamps = this.stamps;
    var stamp = null;

    // Clear out previous values
    this.clear();

    this.mainSection.toggleEnabled(true);
    f = this.mainSection.formNode;
    f.noteTitle.value = data.getDisplayName();
    f.noteDescription.value = data.getBody() + "\n" +data.getTriageStatus() +"\n"+ data.getAutoTriage();
    for (var i = 0; i < stamps.length; i++) {
        var st = stamps[i];
        stamp = data['get' + st.stampType + 'Stamp']();
        if (stamp) {
            this[st.stampType.toLowerCase() + 'Section'].updateFromStamp(stamp);
        }
    }
};

cosmo.ui.detail.DetailViewForm.prototype.clear =
    function () {
    this.mainSection.toggleEnabled(false);
    var stamps = this.stamps;
    for (var i = 0; i < stamps.length; i++) {
        var st = stamps[i];
        this[st.stampType.toLowerCase() + 'Section'].toggleEnabled(false);
    }
};

cosmo.ui.detail.MarkupBar = function (p) {
    // Private vars
    // -------
    var self = this;
    var params = p || {};

    this.id = '';
    this.domNode = null; // Main node
    // Override defaults with params passed in
    for (var n in params) { this[n] = params[n] };

    this.domNode.id = this.id;
    var d = this.domNode;

    this.renderSelf = function () {
        this.clearAll();

        // Add e-mail icon when it's added to the image grid
        addEmailThisIcon();

        // Do test for read-only collection here
        addReadOnlyIcon();

        breakFloat();
    }
    function addEmailThisIcon() {}
    function addReadOnlyIcon() {
        var t = _createElem('div');
        t.id = 'readOnlyIcon';
        t.className = 'floatRight';
        t.style.width = '12px';
        t.style.height = '12px';
        t.style.backgroundImage = 'url(' + cosmo.env.getImagesUrl() +
            'image_grid.png)';
        t.style.backgroundPosition = '-280px -45px';
        t.style.margin = '6px';
        d.appendChild(t);
    }
    function breakFloat() {
        var t = _createElem('div');
        t.className = 'clearBoth';
        d.appendChild(t);
    }
}

cosmo.ui.detail.MarkupBar.prototype =
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
        var label = _createElem('label');
        // Enable/disable checkbox
        d = _createElem('div');
        d.id = id + 'Toggle';
        // Put the toggling checkbox in its own form -- not related
        // to the form proper that has actual values for the stamp
        var f = _createElem('form');
        var ch = _createElem('input');
        ch.type = 'checkbox';
        ch.id = id + 'EnableToggle';
        ch.name = id + 'EnableToggle';
        self.enablerSwitch = ch;
        f.appendChild(ch);
        d.appendChild(f);
        d.className = 'expandoEnableCheckbox floatLeft';
        label.appendChild(d);
        header.appendChild(label);
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
            d.className = 'expandoTriangle';
            d.id = id + 'Expander';
            var a = _createElem('a');
            a.id = id + 'showHideToggle';
            self.showHideSwitch = a;
            a.appendChild(_createText('[hide]'));
            d.appendChild(a);
            header.appendChild(d);
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
            self.formSection = cosmo.ui.detail.getFormElementsForStamp(self.stampType);
            if (!self.formSection.domNode) {
               throw('Form section for ' + self.stampType + ' has no DOM node.');
            }
            body.appendChild(self.formSection.domNode);
            self.bodyNode = body;
            self.domNode.appendChild(body);
        }
    }

    function addBehaviors() {
        if (self.hasBody) {
            // Attach events
            dojo.event.connect(self.enablerSwitch, 'onclick',
                self, 'toggleEnabled');
            dojo.event.connect(self.showHideSwitch, 'onclick', self, 'toggleExpando');
        }
    }
}

cosmo.ui.detail.StampSection.prototype =
    new cosmo.ui.ContentBox();

cosmo.ui.detail.StampSection.prototype.toggleExpando = function (e) {
    var self = this;
    var s = '';
    if (!this.expanded) {
        this.expanded = true;
        dojo.lfx.wipeIn(this.bodyNode, 500).play();
        //$(self.domNode.id + 'Expander').innerHTML = 'v'
        this.showHideSwitch.textContent = '[hide]';
    }
    else {
        this.expanded = false;
        dojo.lfx.wipeOut(this.bodyNode, 500).play();
        //$(self.domNode.id + 'Expander').innerHTML = '>';
        this.showHideSwitch.textContent = '[show]';
    }
}

cosmo.ui.detail.StampSection.prototype.toggleEnabled = function (e, setUp) {
    var setUpDefaults = true;
    // Allow explicit enabled state to be passed
    if (typeof e == 'boolean') {
        // Don't bother enabling and setting up default state
        // if already enabled
        if (e == this.enabled) { return false; }
        this.enabled = e;
        this.enablerSwitch.checked = e;
        setUpDefaults = (setUp == false) ? false : true;
    }
    else {
        this.enabled = !this.enabled;
        // Don't pass click event along to the expando
        // when enabled/expanded states already match
        if (this.hasBody && (this.enabled != this.expanded)) { this.toggleExpando(); }
        setUpDefaults = true;
    }
    if (this.hasBody) {
        this.formSection.toggleEnabled(this.enabled, setUpDefaults);
    }
}

cosmo.ui.detail.StampSection.prototype.updateFromStamp = function (stamp) {
    this.toggleEnabled(true, false);
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
    = function (explicit, setUpDefaults) {
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
    else {
        this.enabled = !this.enabled;
    }

    // Disable label text
    var tagTypes = ['td','div','span'];
    for (var i in tagTypes) {
        var tags = this.domNode.getElementsByTagName(tagTypes[i]);
        toggleText(tags, this.enabled);
    }

    // Disable/enable form elements
    var f = this.formNode;
    var elems = cosmo.util.html.getFormElemNames(f);
    var d = this.elementDefaultStates;
    if (this.enabled) {
        for (var i in elems) {
            var elem = f[i];
            var state = d[i];
            var elemType = elems[i];
            cosmo.util.html.enableFormElem(elem, elemType);
            if (setUpDefaults != false) {
                this.setElemDefaultState(elem, elemType, state);
            }
            else {
                cosmo.util.html.clearFormElem(elem, elemType);
            }
        }
    }
    else {
        for (var i in elems) {
            var elem = f[i];
            var elemType = elems[i];
            cosmo.util.html.clearAndDisableFormElem(elem, elemType);
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

    d.id = 'mainFormSection';
    d.style.padding = '4px 8px 8px 8px';

    // Title
    var t = cosmo.ui.detail.createLabelDiv(_(
        'Main.DetailForm.Title'));
    f.appendChild(t);
    var elem = _html.createInput({ type: 'text',
        id: 'noteTitle',
        name: 'noteTitle',
        size: 28,
        maxlenght: 100,
        value: '',
        className: 'inputText' });
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
            td.innerHTML = label + ':&nbsp;';
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
            td.appendChild(elem);
            tr.appendChild(td);
            return tr;
        }
        d.id = 'mailFormSection';
        d.style.padding = '8px';
        table.cellPadding = '0';
        table.cellSpacing = '0';
        table.appendChild(tbody);

        tbody.appendChild(mailRow('Fr', 'From'));
        tbody.appendChild(mailRow('To', 'To'));
        tbody.appendChild(mailRow('Cc', 'Cc'));

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
        f.mailFrom.value = joinVals(stamp.getFromAddress());
        f.mailTo.value = joinVals(stamp.getToAddress());
        f.mailCc.value = joinVals(stamp.getCcAddress());
    }
};
cosmo.ui.detail.MailFormElements.prototype =
    new cosmo.ui.detail.StampFormElements();

cosmo.ui.detail.EventFormElements= function () {
    var self = this;
    var d = this.domNode;
    var f = this.formNode;
    var _html = cosmo.util.html;

    setUpDOM();
    setDefaultElemState();
    addBehaviors();

    // Private methods
    // -------
    function createDateTimeInputs(label, name) {
        var d = _createElem('div');
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.' + label));
        d.appendChild(t);
        var elem = _html.createInput({ type: 'text',
            id: name + 'Date',
            name: name + 'Date',
            size: 10,
            maxlength: 10,
            value: '',
            className: 'inputText' });
        var t = cosmo.ui.detail.createFormElemDiv(elem);
        t.style.whiteSpace = 'nowrap';
        t.appendChild(_html.nbsp());
        t.appendChild(_createText(
            _('Main.DetailForm.At')));
        t.appendChild(_html.nbsp());
        var elem = _html.createInput({ type: 'text',
            id: name + 'Time',
            name:name + 'Time',
            size: 5,
            maxlength: 5,
            value: '',
            className: 'inputText' });
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        var elem = _html.createInput({ type: 'radio',
            id: name + 'Meridian0',
            name: name + 'Meridian',
            value: 1 });
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        t.appendChild(_createText(
            _('App.AM')));
        t.appendChild(_html.nbsp());
        var elem = _html.createInput({ type: 'radio',
            id: name + 'Meridian1',
            name: name + 'Meridian',
            value: 2 });
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
        getTimezoneIdOptions();
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
            opt.value = str;
            statusOpt.push(opt);
        }
        return statusOpt;
    }
    function getRecurOpt() {
        var recurOpt = [];
        var opt = null;
        var str = '';

        opt = new Object();
        opt.text = 'Once';
        opt.value = '';
        recurOpt.push(opt);
        for (var i in cosmo.model.RRULE_FREQUENCIES) {
            opt = new Object();
            str = cosmo.model.RRULE_FREQUENCIES[i];
            opt.text = dojo.string.capitalize(str);
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
        var elem = _html.createInput({ type: 'text',
            id: 'eventLocation',
            name: 'eventLocation',
            size: 28,
            maxlength: 100,
            value: '',
            className: 'inputText' });
        var t =  cosmo.ui.detail.createFormElemDiv(elem);
        f.appendChild(t);
        // All-day checkbox
        var elem = _html.createInput({ type: 'checkbox',
            id: 'eventAllDay',
            name: 'eventAllDay',
            value: 'true' });
        var t =  cosmo.ui.detail.createFormElemDiv(elem);
        t.appendChild(_html.nbsp());
        t.appendChild(_createText('All day'));
        f.appendChild(t);
        // Event start
        var t = createDateTimeInputs('Starts', 'start');
        f.appendChild(t);
        // Event end
        var t = createDateTimeInputs('Ends', 'end');
        f.appendChild(t);
        // Timezone
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.Timezone'));
        t.style.whiteSpace = 'nowrap';
        t.className += ' formElem';
        t.appendChild(_html.nbsp());
        var elem = _html.createSelect({ id: 'tzRegion',
            name: 'tzRegion',
            multiple: false,
            className: 'selectElem',
            options: getTimezoneRegionOptions() });
        t.appendChild(_html.nbsp());
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        var elem = _html.createSelect({ id: 'tzId',
            name: 'tzId',
            multiple: false,
            className: 'selectElem',
            options: getTimezoneIdOptions(null) });
        elem.style.width = '100px';
        t.appendChild(elem);
        f.appendChild(t);
        // Event status
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.Status'));
        t.className += ' formElem';
        t.appendChild(_html.nbsp());
        var elem = _html.createSelect({ id: 'eventStatus',
            name: 'eventStatus',
            multi: false,
            options: getStatusOpt(),
            className: 'selectElem' });
        t.appendChild(_html.nbsp());
        t.appendChild(elem);
        f.appendChild(t);
        // Recurrence
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.Occurs'));
        t.className += ' formElem';
        t.style.whiteSpace = 'nowrap';
        var elem = _html.createSelect({ id: 'recurrenceInterval',
            name: 'recurrenceInterval',
            multi: false,
            options: getRecurOpt(),
            className: 'selectElem' });
        t.appendChild(_html.nbsp());
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        t.appendChild(_createText(_('Main.DetailForm.Ending')));
        t.appendChild(_html.nbsp());
        var elem = _html.createInput({ type: 'text',
            id: 'recurrenceEnd',
            name: 'recurrenceEnd',
            size: 10,
            maxlength: 10,
            value: '',
            className: 'inputText' });
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
        var txtIn = ['noteTitle', 'eventLocation', 'startDate',
            'startTime', 'endDate', 'endTime', 'recurrenceEnd'];
        for (var el in txtIn) {
            dojo.event.connect(f[txtIn[el]], 'onfocus', func);
        }

        var func = function (e) {
            var allDay = e.target.checked;
            if (allDay) {
                cosmo.util.html.clearFormElem(f.startTime, 'text');
                cosmo.util.html.clearFormElem(f.endTime, 'text');
                cosmo.util.html.clearFormElem(f.startMeridian, 'radio');
                cosmo.util.html.clearFormElem(f.endMeridian, 'radio');
            }
            else {
                self.setElemDefaultState(f.startTime, 'text',
                    self.elementDefaultStates['startTime']);
                self.setElemDefaultState(f.endTime, 'text',
                    self.elementDefaultStates['endTime']);
                self.setElemDefaultState(f.startMeridian, 'radio');
                self.setElemDefaultState(f.endMeridian, 'radio');
            }
        };
        // All-day event / normal event toggling
        dojo.event.connect(f.eventAllDay, 'onclick', func);

        // Recurrence -- disable 'ending' text box if event
        // does not recur
        var elem = f.recurrenceInterval;
        var func = function () {
            var txtElem = f.recurrenceEnd;
            if (elem.selectedIndex == 0) {
                cosmo.util.html.clearAndDisableFormElem(txtElem, 'text');
            }
            else {
                cosmo.util.html.enableFormElem(txtElem, 'text');
                cosmo.util.html.setTextInput(txtElem, 'mm/dd/yyyy', true);
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
            _html.setSelectOptions(f.tzId, options);
        };
        dojo.event.connect(f.tzRegion, 'onchange', func);
    }

    // Interface methods
    // -------
    this.updateFromStamp = function (stamp) {
        var setTimeElem = function (form, name, time) {
            var timeElem = null;
            var meridianElem = null;
            var strtime = '';

            timeElem = form[name + 'Time'];
            meridianElem = form[name + 'Meridian'];
            if (time) {
                strtime = time.strftime('%I:%M');
                // Trim leading zero if need be
                strtime = strtime.indexOf('0') == 0 ? strtime.substr(1) : strtime;
                meridianElem[1].checked = false;
                meridianElem[0].checked = false;
                if (time.getHours() > 11) {
                    meridianElem[1].checked = true;
                }
                else {
                    meridianElem[0].checked = true;
                }
                _html.setTextInput(timeElem, strtime);
            }
            else {
                meridianElem[1].checked = false;
                meridianElem[0].checked = false;
                timeElem.value = '';
            }
        };
        _html.setTextInput(f.eventLocation, stamp.getLocation());
        var start = stamp.getStartDate();
        _html.setTextInput(f.startDate, start.strftime('%m/%d/%Y'));
        setTimeElem(f, 'start', start);
        var end = stamp.getEndDate();
        _html.setTextInput(f.endDate, end.strftime('%m/%d/%Y'));
        setTimeElem(f, 'end', end);
        if (start.tzId){
            var tz = cosmo.datetime.timezone.getTimezone(start.tzId);
            if (!tz){
                clearTimezone();
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
        if (recur) {
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
    };
};
cosmo.ui.detail.EventFormElements.prototype =
    new cosmo.ui.detail.StampFormElements();

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
            var func = enabled ? cosmo.ui.detail[key + 'Item'] : null;
            this[key + 'Button'] = dojo.widget.createWidget("cosmo:Button", {
                text: _("App.Button." + btns[i]),
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
        d.style.borderTop = '1px solid #ccc';
        var t = _createElem('div');
        t.id = 'detailRemoveButton';
        t.className = 'floatLeft';
        self.removeButtonNode = t;
        d.appendChild(t);
        var t = _createElem('div');
        t.id = 'detailSaveeButton';
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



