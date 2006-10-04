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
 *
 * Does some somewhat hackish stuff to suppress inut from the 
 * keyboard when user is typing in form fields
 */

/**
 * @object The form for all UI-form-elements on the page
 */
function CalForm() {
    
    var self = this;
    
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');
    
    function saveCalEvent() {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        // Give timeout check in onclick handler a chance to work
        if (Cal.isTimedOut()) {
            return false;
        }
        // Make backup snapshot
        selEv.makeSnapshot();
        // Update CalEvent obj
        if (self.updateEvent(selEv)) {
            // Save the changes to the backend -- handler for remote save
            // process will update block position and size
            // ==========================
            dojo.event.topic.publish('/calEvent', { 'action': 'saveConfirm', 'data': selEv });
        }
    };
    
    function removeCalEvent() {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        if (Cal.isTimedOut()) {
            return false;
        }
        dojo.event.topic.publish('/calEvent', { 'action': 'removeConfirm', 'data': selEv });
    }
    
    this.handlePub = function(cmd) {
        var act = cmd.action;
        var ev = cmd.data;
        switch (act) {
            case 'eventsDisplaySuccess':
                self.updateFromEvent(ev);
                self.setButtons(true, true);
                break;
            case 'saveFromForm':
                saveCalEvent(ev);
                break;
            case 'setSelected':
                self.setButtons(true, true);
                break;
            case 'saveSuccess':
                // Changes have placed the saved event off-canvas
                if (cmd.qualifier == 'offCanvas') {
                    self.setButtons(false, false);
                    self.clear();
                }
                // Saved event is still in view
                else {
                    self.updateFromEvent(ev);
                    self.setButtons(true, true);
                }
                break;
            case 'saveFailed':
                self.setButtons(true, true);
                break;
            default:
                // Do nothing
                break;
        }
    };
    
    // The actual form DOM elem -- form.form is redundant, so
    // changing this to formElem would be a Good Thing
    this.form = document.getElementById('calForm');
    // If a text input currently has focus -- should disable 
    // listener for Delete button
    this.detailTextInputHasFocus = false;
    // If a textare currently has focus -- should disable
    // listner for Delete and Enter buttons
    this.detailTextAreaHasFocus = false;
    this.jumpToTextInputHasFocus = false;
    
    /**
     * Holdover from when we used object-literal notation
     * TO-DO: Move all this stuff into the main constructor function
     */
    this.init = function() {
        this.appendElements();
        this.setButtons(true, true);
    };
    this.appendElements = function() {
        var info = document.getElementById('eventInfoDiv');
        var cont = document.createElement('div');
        var d = document.createElement('div');
        var elem = null;
        var elemFloat = null;
        var formElem = null;
        
        cont.id = 'eventInfoDivContent';
        cont.style.padding = '8px';
       
        // Event title
        this.createLabel(getText(
            'Main.DetailForm.Title'), d);
        elem = document.createElement('div');
        elem.className = 'formElem';
        this.createInput('text', 'eventtitle', 'eventtitle', 
            28, 100, null, 'inputText', elem); 
        d.appendChild(elem);
       
        // All-day checkbox
        elem = document.createElement('div');
        elem.className = 'formElem';
        this.createInput('checkbox', 'eventallday', 'eventallday',
            null, null, 'true', null, elem);
        this.createNbsp(elem);
        this.createNbsp(elem);
        elem.appendChild(document.createTextNode('All day'));
        d.appendChild(elem);
        
        // Start
        this.createDateTimeInputs('Starts', 'start', d);

        // End
        this.createDateTimeInputs('Ends', 'end', d);

        // Event status
        this.createLabel('Status', d);
        elem = document.createElement('div');
        elem.className = 'formElem';
        this.createSelect('status', 'status', null, null,
        this.getStatusOpt(), 'selectElem', elem);
        d.appendChild(elem);

        // Recurrence
        // ------------------------
        // Recurrence date
        this.createLabel('Occurs', d);
        elem = document.createElement('div');
        elem.className = 'formElem';
        this.createSelect('recurrence', 'recurrence', null, null,
        this.getRecurOpt(), 'selectElem', elem);

        this.createNbsp(elem);
        elem.appendChild(document.createTextNode('ending'));
        this.createNbsp(elem);

        // Recurrence ending
        elem.className = 'formElem';
        this.createInput('text', 'recurend', 'recurend',
            10, 10, null, 'inputText', elem);
        d.appendChild(elem);

        // Details textarea
        this.createLabel(getText(
            'Main.DetailForm.Description'), d);
        elem = document.createElement('div');
        elem.className = 'formElem';
        formElem = document.createElement('textarea');
        formElem.className = 'inputText';
        formElem.id = 'eventdescr';
        formElem.name = 'eventdescr';
        formElem.cols = '28';
        formElem.rows = '4';
        formElem.style.width = '220px';
        elem.appendChild(formElem);
        d.appendChild(elem);

        // Div elements for Remove and Save buttons
        elem = document.createElement('div');
        elem.id = 'eventDetailSave';
        elem.className = 'floatRight';
        d.appendChild(elem);
        
        elem = document.createElement('div');
        elem.className = 'floatRight';
        this.createNbsp(elem);
        d.appendChild(elem);
        
        elem = document.createElement('div');
        elem.id = 'eventDetailRemove';
        elem.className = 'floatRight';
        d.appendChild(elem);
        
        elem = document.createElement('div');
        elem.className = 'clearAll';
        d.appendChild(elem);

        cont.appendChild(d);
        info.appendChild(cont);

        return true;
    };
    this.createDateTimeInputs = function(label, name, d) {
        var elem = null;
        this.createLabel(getText(
            'Main.DetailForm.' + label), d);
        elem = document.createElement('div');
        elem.className = 'formElem';
        elem.style.whiteSpace = 'nowrap';
        this.createInput('text', name + 'date', name + 'date',
            10, 10, null, 'inputText', elem);
        this.createNbsp(elem);
        elem.appendChild(document.createTextNode(
            getText('Main.DetailForm.At')));
        this.createNbsp(elem);
        this.createInput('text', name + 'time', name + 'time',
            5, 5, null, 'inputText', elem);
        this.createNbsp(elem);
        this.createNbsp(elem);
        this.createInput('radio', name + 'ap', name + 'ap', null,
            null, 1, null, elem);
        this.createNbsp(elem);
        elem.appendChild(document.createTextNode(
            getText('App.AM')));
        this.createNbsp(elem);
        this.createNbsp(elem);
        this.createInput('radio', name + 'ap', name + 'ap', null,
            null, 2, null, elem);
        this.createNbsp(elem);
        elem.appendChild(document.createTextNode(
            getText('App.PM')));
        d.appendChild(elem);
    };
    this.createLabel = function(str, d) {
        var elem = document.createElement('div');
        elem.className = 'labelTextVert';
        elem.appendChild(document.createTextNode((str)));
        if (d) {
            d.appendChild(elem);
            return true;
        }
        else {
            return elem;
        }
    };
    this.createInput = function(type, id, name,
        size, maxlength, value, className, elem) {
        
        var formElem = null;
        var str = '';
        
        // IE falls down on DOM-method-generated 
        // radio buttons and checkboxes
        // Old-skool with conditional branching and innerHTML
        if (document.all && (type == 'radio' || type == 'checkbox')) {
            str = '<input type="' + type + '"' +
                ' name="' + name + '"' +
                ' id ="' + id + '"';
            if (size) {
                str += ' size="' + size + '"';
            }
            if (maxlength) {
                str += ' maxlength="' + maxlength + '"';
            }
            if (className) {
                str += ' class="' + className + '"';
            }
            str += '>';
            if (elem) {
                elem.innerHTML += str;
            }
            else {
                formElem = document.createElement('span');
                formElem.innerHTML = str;
                return formElem.firstChild;
            }
        }
        // Standards-compliant browsers -- all intputs
        // IE -- everything but radio button and checkbox
        else {
            formElem = document.createElement('input');
            
            formElem.type = type;
            formElem.name = name;
            formElem.id = id;
            if (size) {
                formElem.size = size;
            }
            if (maxlength) {
                //formElem.maxlength = maxlength; // Setting the prop directly is broken in FF
                formElem.setAttribute('maxlength', maxlength);
            }
            if (value) {
                formElem.value = value;
            }
            if (className) {
                formElem.className = className;
            }
            if (elem) {
                elem.appendChild(formElem);
                return true;
            }
            else {
                return formElem;
            }
        }
    };
    this.createNbsp = function(elem) {
        elem.appendChild(document.createTextNode('\u00A0'));
    };
    /**
     * Inserts the select box for choosing from multiple calendars
     * Only actually called if multiple calendars exist
     */
    this.addCalSelector = function(calendars) {
        var leftSidebarDiv = document.getElementById('leftSidebarDiv');
        var calSelectNav = document.getElementById('calSelectNav');
        var calSelectElemDiv = document.createElement('div');
        var calSelectElem = document.createElement('select');

        calSelectElem.id = 'calSelectElem';
        calSelectElem.name = 'calSelectElem';

        for (var i = 0; i < calendars.length; i++) {
            var calOpt = document.createElement('option');
            calOpt.value = i;
            calOpt.appendChild(document.createTextNode(calendars[i].name));
            calSelectElem.appendChild(calOpt);
        }
        calSelectElem.className = 'selectElem';
        calSelectElemDiv.appendChild(calSelectElem);

        calSelectNav.appendChild(calSelectElemDiv);
        leftSidebarDiv.appendChild(calSelectNav);
    };
    this.createSelect = function(id, name, size, multi, options, className,
        elem) {
        var sel = document.createElement('select');
        sel.id = id
        sel.name = name;
        if (size) {
            sel.size = size;
        }
        if (multi) {
            sel.multiple = 'multiple';
        }
        if (className) {
            sel.className = className;
        }
        for (var i = 0; i < options.length; i++) {
            var opt = document.createElement('option');
            opt.value = options[i].value;
            opt.appendChild(document.createTextNode(options[i].text));
            sel.appendChild(opt);
        }
        if (elem) {
            elem.appendChild(sel);
            return true;
        }
        else {
            return sel;
        }
    };
    this.setSelect = function(name, val) {
        var sel = this.form[name];
        for (var i = 0; i < sel.options.length; i++) {
            if (sel.options[i].value == val) {
                sel.selectedIndex = i;
            }
        }
    };
    this.emptyTextInput = function(param) {
        if (!param || !param.id) {
            param = param || window.event;
            textbox = getSrcElem(param);
        }
        else {
            textbox = param;
        }
        if (textbox.className == 'inputTextDim') {
            textbox.className = 'inputText';
            textbox.value = '';
        }
    };
    this.setTextInput = function(textbox,
        setText, prompt, disabled) {
        textbox.className = prompt? 'inputTextDim' : 'inputText';
        textbox.value = setText;
        textbox.disabled = disabled;

    };
    /**
     * Set up the buttons for the form -- called initially on setup
     * Also called when editing/removing events to toggle button state
     * to enabled/disabled appropriately
     */
    this.setButtons = function(enableRemove, enableSave) {
        var butRemove = null;
        var butSave = null;
        var checkElem = null;
        if (!enableRemove) {
            butRemove = new Button('removeButton', 74, null,
                getText('App.Button.Remove'));
        }
        else {
            butRemove = new Button('removeButton', 74,
                removeCalEvent, getText('App.Button.Remove'));
        }
        if (!enableSave) {
            butSave = new Button('savebutton', 74, null,
                getText('App.Button.Save'));
        }
        else {
            butSave = new Button('savebutton', 74,
                saveCalEvent, getText('App.Button.Save'));
        }

        checkElem = document.getElementById('removeButton');
        if (checkElem) {
            checkElem.parentNode.removeChild(checkElem);
        }
        checkElem = document.getElementById('savebutton');
        if (checkElem) {
            checkElem.parentNode.removeChild(checkElem);
        }
        document.getElementById('eventDetailRemove').appendChild(butRemove.domNode);
        document.getElementById('eventDetailSave').appendChild(butSave.domNode);
    };
    this.getRecurOpt = function() {
        var recurOpt = [];
        var recurTempl = Cal.recurTemplate;
        var opt = null;
        var str = '';

        opt = new Object();
        opt.text = 'Once';
        opt.value = '';
        recurOpt.push(opt);
        for (var i = 0; i < recurTempl.options.length; i++) {
            opt = new Object();
            str = recurTempl[recurTempl.options[i]];
            opt.text = Text.uppercaseFirst(str);
            opt.value = str;
            recurOpt.push(opt);
        }
        return recurOpt;
    };

    this.getStatusOpt = function() {
        var statusOpt = [];
        var statusTempl = Cal.statusTemplate;
        var opt = null;
        var str = '';

        opt = new Object();
        for (var i = 0; i < statusTempl.options.length; i++) {
            opt = new Object();
            str = statusTempl[statusTempl.options[i]];
            if(str == 'fyi') {
                opt.text = str.toUpperCase();
                opt.value = "CANCELLED";
            } else {
                opt.text = Text.uppercaseFirst(str);
                opt.value = str.toUpperCase();
            }
            statusOpt.push(opt);
        }
        return statusOpt;
    };
    /**
     * Update the event's CalEventData obj from the values in the form
     * Called when clicking the Save button or hitting Enter
     * BANDAID: Currently still building native JS Date objects from
     * the values in the form and then gettting ScoobyDates for the
     * event based on those -- should be building ScoobyDates directly
     * from the form
     */
    this.updateEvent = function(ev) {
        var form = this.form;
        var startDate = '';
        var endDate = '';
        var startTime = '';
        var endTime = '';
        var title = '';
        var descr = '';
        var status = '';
        var allDay = false;
        var h = 0;
        var m = 0;
        var err = '';
        var errMsg = '';
        var e = null;

        // Pull new values out of the event info form
        startDate = form.startdate.value;
        endDate = form.enddate.value;
        startTime = form.starttime.value;
        endTime = form.endtime.value;
        title = form.eventtitle.value;
        descr = form.eventdescr.value;
        status = form.status.value;
        allDay = form.eventallday.checked ? true : false;

        // Error checking
        // =======================
        if (!title) {
            errMsg += '"Title" is a required field.\n'
        }
        err = Validate.dateFormat(startDate);
        if (err) {
            errMsg += '"Starts" date field:\n' + err;
            errMsg += '\n';
        }
        err = Validate.dateFormat(endDate);
        if (err) {
            errMsg += '"Ends" date field:\n' + err;
            errMsg += '\n';
        }
        // Validate times for normal events
        if (!allDay) {
            err = Validate.timeFormat(startTime);
            if (err) {
                errMsg += '"Starts" time field:\n' + err;
                errMsg += '\n';
            }
            err = Validate.timeFormat(endTime);
            if (err) {
                errMsg += '"Ends" time field:\n' + err;
                errMsg += '\n';
            }
        }

        // Display error or update form and submit
        // =======================
        // Err condition
        if (errMsg) {
            errMsg = errMsg.replace(/\n/g, '<br/>');
            e = new ScoobyServiceClientException();
            e.message = errMsg;
            Cal.showErr('Error in Event Detail Form input', e);
            return false;
        }
        // All okey-dokey -- submit
        else {
            // Calc military datetimes from form entries
            startDate = new Date(startDate);
            if (!allDay) {
                h = Cal.extractHourFromTime(startTime);
                h = hrStd2Mil(h, form.startap[1].checked);
                m = Cal.extractMinutesFromTime(startTime);
                startDate.setHours(h, m);
            }
            endDate = new Date(endDate);
            if (!allDay) {
                h = Cal.extractHourFromTime(endTime);
                h = hrStd2Mil(h, form.endap[1].checked);
                m = Cal.extractMinutesFromTime(endTime);
                endDate.setHours(h, m);
            }

            // Set event properties
            // ==============
            // ScoobyDate with timezones
            if (ev.data.start.timezone) {
                ev.data.start.updateFromLocalDate(startDate);
                ev.data.end.updateFromLocalDate(endDate);
            }
            // Floating ScoobyDates
            else {
                ev.data.start.updateFromUTC(startDate.getTime());
                ev.data.end.updateFromUTC(endDate.getTime());
            }
            ev.data.title = title;
            ev.data.description = descr;
            ev.data.allDay = allDay;
            ev.data.status = status;
            return true;
        }
    };
    /**
     * Update values displayed in the form from the properties in the
     * CalEventData obj for the event.
     */
    this.updateFromEvent = function(ev) {
        var form = this.form;
        var recur = ev.data.recurrenceRule;
        var status = ev.data.status;
        form.eventtitle.value = ev.data.title;
        form.eventdescr.value = ev.data.description ?
            ev.data.description : '';
        form.status.value = ev.data.statusBar ? ev.data.status : '';
        form.startdate.value = ev.data.start.strftime('%m/%d/%Y');
        form.enddate.value = ev.data.end.strftime('%m/%d/%Y');
        form.eventallday.checked = ev.data.allDay ? true : false;
        if (ev.data.allDay) {
            this.setTimeElem(null, 'start');
            this.setTimeElem(null, 'end');
        }
        else {
            this.setTimeElem(ev.data.start, 'start');
            this.setTimeElem(ev.data.end, 'end');
        }

        if(status) {
            this.setSelect("status", status);
        } else {
            this.setSelect("status", "CONFIRMED");
        }

        if (recur) {
            this.setSelect('recurrence', recur.frequency);
            form.recurend.disabled = false;
            if (recur.endDate) {
                this.setTextInput(form.recurend,
                    recur.endDate.strftime('%m/%d/%Y'), false, false);
            }
            else {
                this.setTextInput(form.recurend, 'mm/dd/yyyy', true, false);
            }
        }
        else {
            this.setSelect('recurrence', '');
            this.setTextInput(form.recurend, 'mm/dd/yyyy', true, true);
        }
    };
    this.setRecurEnd = function() {
        var self = Cal.calForm
        var form = self.form;
        if (form.recurrence.selectedIndex == 0) {
            self.setTextInput(form.recurend, 'mm/dd/yyyy', true, true);
        }
        else if (form.recurend.disabled) {
            self.setTextInput(form.recurend, 'mm/dd/yyyy', true, false);
        }
    }
    /**
     * Set time fields in the event detail form inputs based on the
     * properties of CalEventDate obj for the selected event
     * @param time Date object with times set
     * @param name Name of form element to set (e.g., 'start' or 'end')
     */
    this.setTimeElem = function(time, name) {
        var form = this.form;
        var timeElem = null;
        var meridianElem = null;
        var strtime = '';
        
        timeElem = form[name + 'time'];
        meridianElem = form[name + 'ap'];
        if (time) {
            strtime = time.strftime('%I:%M');
            meridianElem[1].disabled = false;
            meridianElem[0].disabled = false;
            timeElem.disabled = false;
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
            timeElem.value = strtime;
        }
        else {
            meridianElem[1].checked = false;
            meridianElem[0].checked = false;
            timeElem.value = '';
            meridianElem[1].disabled = true;
            meridianElem[0].disabled = true;
            timeElem.disabled = true;
        }

    };
    /**
     * Reloading the page in some browsers preserves form information
     * This method empties the event info form of any entered values. 
     */
    this.clear = function() {
        var form = this.form;
        // Update info in event form
        form.eventtitle.value = '';
        form.eventdescr.value = '';
        form.startdate.value = '';
        form.starttime.value = '';
        for (var i = 0; i < form.startap.length; i++) {
            form.startap[0].checked = false;
        }
        form.enddate.value = '';
        form.endtime.value = '';
        for (var i = 0; i < form.endap.length; i++) {
            form.endap[0].checked = false;
        }
        form.eventallday.checked = false;
        form.recurrence.selectedIndex = 0;
        form.status.selectedIndex = 0;
        this.setTextInput(form.recurend, 'mm/dd/yyyy', true, true);
        return true;
    };
    /**
     * Toggle an event from 'normal' (HasTime) to 'all-day' (NoTime)
     */
    this.toggleBlockType = function() {
        var allDay = this.form.eventallday.checked ? true : false;
        var setDate = new Date();
        setDate.setMinutes(0);
        if (allDay) {
            this.setTimeElem(null, 'start');
            this.setTimeElem(null, 'end');
        }
        else {
            setDate.setHours(8);
            this.setTimeElem(setDate, 'start');
            setDate.setHours(9);
            this.setTimeElem(setDate, 'end');
        }
    };
    /**
     * Add event listeners to text inputs and textareas to
     * suppress listener for Delete and Enter keys while typing
     * in these form fields
     * Also adds event listener to 'all-day' checkbox to toggle
     * event type
     */
    this.setEventListeners = function() {
        var inputs = document.getElementsByTagName('input');
        var descrTxt = document.getElementById('eventdescr');
        var allDayCheck = document.getElementById('eventallday');
        var form = Cal.calForm.form;
    
        // Add dummy function event listener so form doesn't
        // submit on Enter keypress in Safari
        form.onsubmit = function() { return false; };
        
        // Cal selector
        if (form.calSelectElem) {
            form.calSelectElem.onchange = Cal.goSelCalMask;
        }
        
        // All text inputs
        for (var i=0; i < inputs.length; i++) {
            if (inputs[i].className == 'inputText') {
                inputs[i].onfocus = function() { Cal.calForm.detailTextInputHasFocus = true; };
                inputs[i].onblur = function() { Cal.calForm.detailTextInputHasFocus = false; };
            }
        }
        
        // Recurrence
        form.recurrence.onchange = Cal.calForm.setRecurEnd;
        form.recurend.onclick = Cal.calForm.emptyTextInput;
        
        // Description textarea
        descrTxt.onfocus = function() {
            Cal.calForm.detailTextInputHasFocus = true;
            Cal.calForm.textAreaHasFocus = true;
        };
        descrTxt.onblur = function() {
            Cal.calForm.detailTextInputHasFocus = false;
            Cal.calForm.textAreaHasFocus = false;
        };

        // All-day event / normal event toggling
        allDayCheck.onclick = function() { Cal.calForm.toggleBlockType() };

        descrTxt = null; // Set DOM-node-ref to null to avoid IE memleak
    };
    this.addJumpToDate = function(dc) {
        var top = parseInt(MiniCal.displayContext.style.top);
        var d = null;
        
        // place the div just above minical
        top -= 28;
        dc.style.top = top + 'px';
        
        d = document.createElement('div');
        d.className = 'floatLeft';
        d.style.paddingTop = '3px';
        d.appendChild(document.createTextNode(getText('Main.GoTo')));
        dc.appendChild(d);
        
        d = document.createElement('div');
        d.className = 'floatLeft';
        self.createNbsp(d);
        dc.appendChild(d);
        
        d = document.createElement('div');
        d.className = 'formElem floatLeft';
        dc.appendChild(d);
        self.createInput('text', 'jumpto', 'jumpto',
            8, 10, null, 'inputText', d);
        self.setTextInput(self.form.jumpto, 'mm/dd/yyyy', true, false);
        self.form.jumpto.onclick = Cal.calForm.emptyTextInput;
        self.form.jumpto.onfocus = function() { Cal.calForm.jumpToTextInputHasFocus = true; };
        self.form.jumpto.onblur = function() { Cal.calForm.jumpToTextInputHasFocus = false; };
        
        d = document.createElement('div');
        d.className = 'floatLeft';
        self.createNbsp(d);
        self.createNbsp(d);
        dc.appendChild(d);
        
        d = document.createElement('div');
        d.className = 'floatLeft';
        dc.appendChild(d);
        butJump = new Button('jumpToButton', 32, Cal.calForm.goJumpToDate,
                getText('App.Button.Go'), true);
        d.appendChild(butJump.domNode);
        
        d = document.createElement('div');
        d.className = 'clearAll';
        dc.appendChild(d);
    };
    this.goJumpToDate = function() {
        var e = null;
        var err = '';
        var val = self.form.jumpto.value;
        err = Validate.dateFormat(val);
        if (err) {
            err += '\n';
        }
        // Display error or update form and submit
        // =======================
        // Err condition
        if (err) {
            err = err.replace(/\n/g, '<br/>');
            e = new ScoobyServiceClientException();
            e.message = err;
            Cal.showErr(getText('Main.Error.GoToDate'), e);
            return false;
        }
        // All okey-dokey -- submit
        else {
           Cal.goViewQueryDate(val); 
        }
    };
}
