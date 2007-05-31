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
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require('cosmo.convenience');
dojo.require('cosmo.datetime.timezone');

cosmo.ui.detail.DetailViewForm = function (p) {
    var self = this;
    var params = p || {};

    this.domNode = null;
    this.formNode = _createElem('form');

    for (var n in params) { this[n] = params[n]; }

    this.domNode.appendChild(this.formNode);
    var d = _createElem('div');
    var a = new cosmo.ui.detail.StampSection({ parent: this,
        domNode: d,
        stampType: 'Address', title: 'Address' });
    this.children = [a];
    this.formNode.appendChild(a.domNode);
    var d = _createElem('div');
    var a = new cosmo.ui.detail.StampSection({ parent: this,
        domNode: d,
        stampType: 'Event', title: 'Event' });
    this.children = [a];
    this.formNode.appendChild(a.domNode);
};

cosmo.ui.detail.DetailViewForm.prototype =
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
    this.iconNode = null; // Stamp icon
    this.bodyNode = null; // Body with form section
    this.enablerSwitch = null; // Checkbox for toggling disabled state
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
    // Disclosure triangle
    d = _createElem('div');
    d.className = 'expandoTriangle floatLeft';
    d.id = id + 'Expander';
    d.innerHTML = 'v';
    header.appendChild(d);
    // Stamp icon
    d = _createElem('div');
    d.className = 'expandoIcon floatLeft';
    d.id = id + 'StampIcon';
    d.innerHTML = '@';
    header.appendChild(d);
    this.iconNode = d;
    // Title
    d = _createElem('div');
    d.className = 'expandoTitle floatLeft';
    d.id = id + 'Title';
    d.innerHTML = this.title;
    header.appendChild(d);
    this.titleNode = d;
    // Enable/disable checkbox
    d = _createElem('div');
    d.id = id + 'Toggle';
    d.innerHTML = '<input type="checkbox" name="' + id + 'Toggle"/>';
    d.className = 'expandoEnableCheckbox';
    this.enablerSwitch = d.firstChild;
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

    //this.formSection.toggleEnabled(false);
    // Save rendered height
    this.bodyHeight = this.bodyNode.offsetHeight;
    // Attach events
    dojo.event.connect(self.enablerSwitch, 'onclick',
        self, 'toggleEnabled');
    dojo.event.connect(this.headerNode, 'onclick', self, 'toggleExpando');
}

cosmo.ui.detail.StampSection.prototype =
    new cosmo.ui.ContentBox();

cosmo.ui.detail.StampSection.prototype.toggleExpando = function (e) {
    var self = this;
    var s = '';
    if (!this.expanded) {
        this.expanded = true;
        dojo.lfx.wipeIn(this.bodyNode, 500).play();
        $(self.domNode.id + 'Expander').innerHTML = 'v'
    }
    else {
        this.expanded = false;
        dojo.lfx.wipeOut(this.bodyNode, 500).play();
        $(self.domNode.id + 'Expander').innerHTML = '>';
    }
}

cosmo.ui.detail.StampSection.prototype.toggleEnabled = function (e) {
    // Don't pass click event along to the expando
    e.stopPropagation();

    // Allow explicit enabled state to be passed
    if (typeof e == 'boolean') {
        this.enabled = e;
    }
    else {
        this.enabled = !this.enabled;
    }

    return;

    var s = this.titleNode.className;
    if (this.enabled) {
        fleegix.css.removeClass(this.titleNode, 'disabledText');
        fleegix.css.removeClass(this.iconNode, 'disabledText');
    }
    else {
        fleegix.css.addClass(this.titleNode, 'disabledText');
        fleegix.css.addClass(this.iconNode, 'disabledText');
    }
    this.form.toggleEnabled(this.enabled);
}


cosmo.ui.detail.StampFormElements = function () {
   this.enabled = false;
}

cosmo.ui.detail.StampFormElements.prototype.toggleEnabled = function (explicit) {
    // If passed explicitly, reset the enabled prop
    if (typeof explicit == 'boolean') {
        this.enabled = explicit;
    }
    else {
        this.enabled = !this.enabled;
    }

    return;

    var e = this.enabled;
    var f = this.formNode;
    for (var i = 0; i < f.elements.length; i++) {
        var elem = f.elements[i];
        elem.disabled = !e;
        if (!e) {
           elem.value = '';
        }
    }
    var tags = document.getElementsByTagName('td');
    if (this.enabled) {
        for (var i = 0; i < tags.length; i++) {
            var tag = tags[i];
            var className = tag.className;
            className = className.replace(', disabledText', '');
            tag.className = className;
        }
    }
    else {
        for (var i = 0; i < tags.length; i++) {
            var tag = tags[i];
            var className = tag.className;
            className += ', disabledText';
            tag.className = className;
        }
    }
};

cosmo.ui.detail.getFormElementsForStamp = function (stampType) {
    return new cosmo.ui.detail[stampType + 'FormElements']();
};
cosmo.ui.detail.AddressFormElements = function () {
    var _html = cosmo.util.html;
    var d = _createElem('div');
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
    d.style.padding = '12px 0px 24px 0px';
    d.appendChild(table);
    this.domNode = d;
    table.cellPadding = '0px';
    table.cellSpacing = '0px';
    table.appendChild(tbody);

    tbody.appendChild(addressRow('Fr', 'From'));
    tbody.appendChild(addressRow('To', 'To'));
    tbody.appendChild(addressRow('Cc', 'Cc'));
    this.toggleEnabled(false);
};
cosmo.ui.detail.AddressFormElements.prototype = new cosmo.ui.detail.StampFormElements();

cosmo.ui.detail.EventFormElements= function () {
    var _html = cosmo.util.html;
    var d = _createElem('div');

    var createDateTimeInputs = function (label, name) {
        var d = _createElem('div');
        var t = cosmo.ui.detail.createLabelDiv(_(
            'Main.DetailForm.' + label));
        d.appendChild(t);
        var elem = _html.createInput({ type: 'text',
            id: name + 'date',
            name: name + 'date',
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
            id: name + 'time',
            name:name + 'time',
            size: 5,
            maxlength: 5,
            value: '',
            className: 'inputText' });
        t.appendChild(elem);
        t.appendChild(_html.nbsp());
        var elem = _html.createInput({ type: 'radio',
            id: name + 'ap0',
            name: name + 'ap',
            value: 1 });
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
    d.style.padding = '8px 12px 24px 8px';
    this.domNode = d;

    // Event title
    var t = cosmo.ui.detail.createLabelDiv(_(
        'Main.DetailForm.Title'));
    d.appendChild(t);
    var elem = _html.createInput({ type: 'text',
        id: 'eventtitle',
        name: 'eventtitle',
        size: 28,
        maxlenght: 100,
        value: '',
        className: 'inputText' });
    var t =  cosmo.ui.detail.createFormElemDiv(elem);
    d.appendChild(t);
    // Location
    var t = cosmo.ui.detail.createLabelDiv(_(
        'Main.DetailForm.Location'));
    d.appendChild(t);
    var elem = _html.createInput({ type: 'text',
        id: 'eventlocation',
        name: 'eventlocation',
        size: 28,
        maxlength: 100,
        value: '',
        className: 'inputText' });
    var t =  cosmo.ui.detail.createFormElemDiv(elem);
    d.appendChild(t);
    // All-day checkbox
    var elem = _html.createInput({ type: 'checkbox',
        id: 'eventallday',
        name: 'eventallday',
        value: 'true' });
    var t =  cosmo.ui.detail.createFormElemDiv(elem);
    t.appendChild(_html.nbsp());
    t.appendChild(_createText('All day'));
    d.appendChild(t);
    // Event start
    var t = createDateTimeInputs('Starts', 'start');
    d.appendChild(t);
    // Event end
    var t = createDateTimeInputs('Ends', 'end');
    d.appendChild(t);
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
    d.appendChild(t);
    // Event status
    var t = cosmo.ui.detail.createLabelDiv(_(
        'Main.DetailForm.Status'));
    t.className += ' formElem';
    t.appendChild(_html.nbsp());
    var elem = _html.createSelect({ id: 'status',
        name: 'status',
        multi: false,
        options: getStatusOpt(),
        className: 'selectElem' });
    t.appendChild(_html.nbsp());
    t.appendChild(elem);
    d.appendChild(t);
    // Recurrence
    var t = cosmo.ui.detail.createLabelDiv(_(
        'Main.DetailForm.Occurs'));
    t.className += ' formElem';
    t.style.whiteSpace = 'nowrap';
    var elem = _html.createSelect({ id: 'recurrence',
        name: 'recurrence',
        multi: false,
        options: getRecurOpt(),
        className: 'selectElem' });
    t.appendChild(_html.nbsp());
    t.appendChild(elem);
    t.appendChild(_html.nbsp());
    t.appendChild(_createText(_('Main.DetailForm.Ending')));
    t.appendChild(_html.nbsp());
    var elem = _html.createInput({ type: 'text',
        id: 'recurend',
        name: 'recurend',
        size: 10,
        maxlength: 10,
        value: '',
        className: 'inputText' });
    t.appendChild(elem);
    d.appendChild(t);

    var t = cosmo.ui.detail.createLabelDiv(_(
        'Main.DetailForm.Description'));
    d.appendChild(t);
    var elem = _createElem('textarea');
    elem.className = 'inputText';
    elem.id = 'eventdescr';
    elem.name = 'eventdescr';
    elem.cols = '28';
    elem.rows = '4';
    elem.style.width = '220px';
    var t = cosmo.ui.detail.createFormElemDiv(elem);
    d.appendChild(t);

    this.toggleEnabled(false);
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

