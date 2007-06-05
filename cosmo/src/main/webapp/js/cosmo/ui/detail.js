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

cosmo.ui.detail.DetailViewForm = function (p) {
    var self = this;
    var params = p || {};

    this.domNode = null;

    for (var n in params) { this[n] = params[n]; }

    // Main section
    var d = _createElem('div');
    var c = new cosmo.ui.detail.MainSection({ parent: this,
        domNode: d });
    this.children.push(c);
    this.domNode.appendChild(c.domNode);

    // Address stamp
    var d = _createElem('div');
    var c = new cosmo.ui.detail.StampSection({ parent: this,
        domNode: d,
        stampType: 'Address', title: 'Address' });
    this.children.push(c);
    this.domNode.appendChild(c.domNode);

    // Event stamp
    var d = _createElem('div');
    var c = new cosmo.ui.detail.StampSection({ parent: this,
        domNode: d,
        stampType: 'Event', title: 'Event' });
    this.children.push(c);
    this.domNode.appendChild(c.domNode);
};

cosmo.ui.detail.DetailViewForm.prototype =
    new cosmo.ui.ContentBox();

cosmo.ui.detail.MainSection = function () {
    var _html = cosmo.util.html;
    var d = _createElem('div');
    var f = _createElem('form');

    d.id = 'mainFormSection';
    d.style.padding = '8px';

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
    new cosmo.ui.ContentBox();

cosmo.ui.detail.StampSection = function (p) {
    var self = this;
    var params = p || {};
    this.stampType = '';
    this.id = '';
    this.title = '';
    this.domNode = null; // Main node
    this.headerNode = null; // Header with toolbar
    this.titleNode = null; // Stamp title
    this.bodyNode = null; // Body with form section
    this.enablerSwitch = null; // Checkbox for toggling disabled state
    this.showHideSwitch = null // Show/hide link
    this.formSection = null; // Set of form elements for this stamp
    this.expanded = true; // Expanded/collapsed
    this.enabled = false; // Enabled/disabled
    this.bodyHeight = null;
    for (var n in params) { this[n] = params[n] };
    // Use the stamp type as a basis for the DOM node ids
    // e.g., 'addressSectionExpander', 'eventSectionBody', etc.
    this.id = 'section' + this.stampType;

    var d = null;
    var header = null;
    var body = null;
    var id = self.id;
    d = this.domNode;
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
    this.enablerSwitch = ch;
    f.appendChild(ch);
    d.appendChild(f);
    d.className = 'expandoEnableCheckbox floatLeft';
    label.appendChild(d);
    header.appendChild(label);
    // Title
    d = _createElem('div');
    d.className = 'expandoTitle floatLeft';
    d.id = id + 'Title';
    d.innerHTML = this.title;
    label.appendChild(d);
    this.titleNode = d;
    // Show/hide link
    d = _createElem('div');
    d.className = 'expandoTriangle';
    d.id = id + 'Expander';
    var a = _createElem('a');
    a.id = id + 'showHideToggle';
    this.showHideSwitch = a;
    a.appendChild(_createText('[hide]'));
    d.appendChild(a);
    header.appendChild(d);
    d = _createElem('div');
    d.className = 'clearBoth';
    header.appendChild(d);
    this.headerNode = header;
    this.domNode.appendChild(header);

    // Body
    // ------------------------
    body = _createElem('div');
    body.id = id + 'Body';
    body.className = 'expandoBody';
    this.formSection = cosmo.ui.detail.getFormElementsForStamp(this.stampType);
    if (!this.formSection.domNode) {
       throw('Form section for ' + this.stampType + ' has no DOM node.');
    }
    body.appendChild(this.formSection.domNode);
    this.bodyNode = body;
    this.domNode.appendChild(body);

    this.toggleEnabled(false);
    // Save rendered height
    this.bodyHeight = this.bodyNode.offsetHeight;
    // Attach events
    dojo.event.connect(self.enablerSwitch, 'onclick',
        self, 'toggleEnabled');
    dojo.event.connect(this.showHideSwitch, 'onclick', self, 'toggleExpando');
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

cosmo.ui.detail.StampSection.prototype.toggleEnabled = function (e) {

    // Allow explicit enabled state to be passed
    if (typeof e == 'boolean') {
        this.enabled = e;
    }
    else {
        this.enabled = !this.enabled;
        // Don't pass click event along to the expando
        // when enabled/expanded states already match
        if (this.enabled != this.expanded) { this.toggleExpando(); }
    }
    this.formSection.toggleEnabled(this.enabled);
}

cosmo.ui.detail.getFormElementsForStamp = function (stampType) {
    return new cosmo.ui.detail[stampType + 'FormElements']();
};

cosmo.ui.detail.StampFormElements = function () {
    this.domNode = _createElem('div');
    this.formNode = _createElem('form');
    this.enabled = false;
    this.elementDefaultStates = {};
    // Prevent form submission from hitting Enter key
    // while in a text box
    dojo.event.connect(this.formNode, 'onsubmit', function (e) {
        e.stopPropagation();
        e.preventDefault();
        return false;
    });
}

cosmo.ui.detail.StampFormElemState = function (p) {
    var params = p || {};
    // Using typeof tests to see if these are set for a
    // particular form elem
    this.disabled = null; // Boolean
    this.value = null; // String
    this.hintText = null; // String
    for (var n in params) { this[n] = params[n]; }
}

cosmo.ui.detail.StampFormElements.prototype.toggleEnabled
    = function (explicit) {
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
            this.setElemDefaultState(elem, elemType, state);
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

    cosmo.util.html.enableFormElem(elem, elemType);

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

cosmo.ui.detail.AddressFormElements = function () {
    var _html = cosmo.util.html;
    var d = this.domNode;
    var f = this.formNode;
    var table = _createElem('table');
    var tbody = _createElem('tbody');
    var addressRow = function (label, name) {
        var tr = null;
        var td = null;
        tr = _createElem('tr');
        td = _createElem('td');
        td.style.width = '56px';
        td.style.textAlign = 'right';
        td.innerHTML = label + ':&nbsp;';
        td.className = 'labelTextHoriz';
        tr.appendChild(td);
        td = _createElem('td');
        td.style.padding = '2px';
        var elem = _html.createInput({ type: 'text',
            id: 'address' + name,
            name: 'address' + name,
            size: 20,
            maxlength: 100,
            value: '',
            className: 'inputText' });
        td.appendChild(elem);
        tr.appendChild(td);
        return tr;
    }
    d.id = 'addressFormSection';
    d.style.padding = '8px';
    table.cellPadding = '0';
    table.cellSpacing = '0';
    table.appendChild(tbody);

    tbody.appendChild(addressRow('Fr', 'From'));
    tbody.appendChild(addressRow('To', 'To'));
    tbody.appendChild(addressRow('Cc', 'Cc'));

    f.appendChild(table);
    d.appendChild(f);
};
cosmo.ui.detail.AddressFormElements.prototype =
    new cosmo.ui.detail.StampFormElements();

cosmo.ui.detail.EventFormElements= function () {
    var _html = cosmo.util.html;
    var d = this.domNode;
    var f = this.formNode;
    var createDateTimeInputs = function (label, name) {
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
    };

    var getTimezoneRegionOptions = function (){
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
    };

    var getTimezoneIdOptions = function (region){
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
    };

    var getStatusOpt = function () {
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
    };
    var getRecurOpt = function () {
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
    };

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

    var _st = cosmo.ui.detail.StampFormElemState;
    this.elementDefaultStates = {
        startDate: new _st({ hintText: 'mm/dd/yyyy' }),
        startTime: new _st({ hintText: 'hh:mm' }),
        endDate: new _st({ hintText: 'mm/dd/yyyy' }),
        endTime: new _st({ hintText: 'hh:mm' }),
        recurrenceEnd: new _st({ disabled: true })
    };

    // Make hint text in text inputs disappear on focus
    var func = cosmo.util.html.handleTextInputFocus;
    var txtIn = ['noteTitle', 'eventLocation', 'startDate',
        'startTime', 'endDate', 'endTime', 'recurrenceEnd'];
    for (var el in txtIn) {
        dojo.event.connect(f[txtIn[el]], 'onfocus', func);
    }

    var self = this;
    var func = function (e) {
        var allDay = e.target.checked;
        if (allDay) {
            cosmo.util.html.clearFormElem(f.startTime, 'text');
            cosmo.util.html.clearFormElem(f.endTime, 'text');
            cosmo.util.html.clearFormElem(f.startMeridian, 'radio');
            cosmo.util.html.clearFormElem(f.endMeridian, 'radio');
        }
        else {
            self.setElemDefaultState(f.startTime, 'text', self.elementDefaultStates['startTime']);
            self.setElemDefaultState(f.endTime, 'text', self.elementDefaultStates['endTime']);
            self.setElemDefaultState(f.startMeridian, 'radio');
            self.setElemDefaultState(f.endMeridian, 'radio');
        }
    };
    // All-day event / normal event toggling
    dojo.event.connect(f.eventAllDay, 'onclick', func);

    // Recurrence -- disable 'ending' text box if event
    // does not recur
    var self = this;
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
        var tzIds = r ? cosmo.datetime.timezone.getTzIdsForRegion(r).sort() : null;
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

    f.appendChild(t);
    d.appendChild(f);
};
cosmo.ui.detail.EventFormElements.prototype = new cosmo.ui.detail.StampFormElements();

// Utility functions
cosmo.ui.detail.createLabelDiv = function (str) {
    var d = _createElem('div');
    d.className = 'labelTextVert';
    d.appendChild(_createText((str)));
    return d;
};

cosmo.ui.detail.createFormElemDiv = function (elem) {
    var d = _createElem('div');
    d.className = 'formElem';
    d.appendChild(elem);
    return d;
};

