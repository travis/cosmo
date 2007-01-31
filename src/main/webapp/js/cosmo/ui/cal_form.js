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

/**
 * @object The form for all UI-form-elements on the page
 */
 
dojo.require("dojo.string");
dojo.require("dojo.lang");
dojo.require("dojo.event.common");
dojo.require("dojo.event.topic");
dojo.require("cosmo.util.html");
dojo.require("cosmo.ui.event.handlers");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.validate");
dojo.require("cosmo.model");
dojo.require("cosmo.view.cal.canvas");
dojo.require("cosmo.convenience");
dojo.require("cosmo.ui.minical");

dojo.provide("cosmo.ui.cal_form");

cosmo.ui.cal_form.CalForm = function () {

    var self = this;
    var saveButton = null;
    var removeButton = null;
    var _html = cosmo.util.html;
    var topBarHeight = 18;

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
            // process will update lozenge position and size
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

    this.handlePub = function (cmd) {
        var act = cmd.action;
        var ev = cmd.data;
        switch (act) {
            case 'eventsLoadPrepare':
                self.clear();
                break;
            case 'eventsDisplaySuccess':
                self.updateFromEvent(ev);
                self.setButtons(true, true);
                toggleReadOnlyIcon();
                break;
            case 'saveFromForm':
                saveCalEvent(ev);
                break;
            case 'setSelected':
                var c = cosmo.view.cal.canvas;
                self.updateFromEvent(ev);
                self.setButtons(true, true);
                break;
            case 'saveSuccess':
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
                break;
            case 'saveFailed':
                self.setButtons(true, true);
                break;
            default:
                // Do nothing
                break;
        }
    };
    
    function toggleReadOnlyIcon() {
        var icon = $('readOnlyIcon');
        if (Cal.currentCollection.privileges.write) {
            icon.style.display = 'none'; 
        }
        else {
            icon.style.display = 'block'; 
        }
    }
    
    // The actual form DOM elem -- form.form is redundant, so
    // changing this to formElem would be a Good Thing
    this.form = $('calForm');

    /**
     * Holdover from when we used object-literal notation
     * TO-DO: Move all this stuff into the main constructor function
     */
    this.init = function () {
        this.appendElements();
        this.createButtons(true, true);
    };
    this.appendElements = function () {
        var info = $('eventInfoDiv');
        var cont = _createElem('div');
        var d = _createElem('div');
        var topBar = _createElem('div');
        var elem = null;
        var elemFloat = null;
        var formElem = null;
        
        cont.id = 'eventInfoDivContent';
        topBar.style.height = topBarHeight + 'px';

        // Read-only item icon
        elem = _createElem('div');
        elem.id = 'readOnlyIcon';
        elem.appendChild(_html.nbsp());
        var i = _createElem('img');
        i.src = cosmo.env.getImagesUrl() + 'read_only_item.png';
        i.alt = _('Main.DetailForm.ReadOnly');
        i.title = _('Main.DetailForm.ReadOnly');
        elem.appendChild(i);
        elem.className = 'floatRight';
        elem.style.display = 'none';
        topBar.appendChild(elem);
        
        // 'E-mail this event' link
        elem = _createElem('div');
        elem.id = 'emailThisEventDiv';
        this.mailtoLink = _createElem("a");
        this.mailtoLink.href = "#";
        this.mailtoLink.id = "emailThisEventLink";
        this.mailtoLink.appendChild(
            _createText(_('Main.DetailForm.EMail')));
        elem.appendChild(this.mailtoLink);
        elem.className = 'floatRight';
        elem.style.height =  topBarHeight + 'px';
        elem.style.lineHeight =  topBarHeight + 'px';
        elem.style.verticalAlign = 'middle';
        topBar.appendChild(elem);
        
        elem = _createElem('div');
        elem.className = 'clearBoth';
        topBar.appendChild(elem);
        
        d.appendChild(topBar);

        // Event title
        this.createLabel(_(
            'Main.DetailForm.Title'), d);
        elem = _createElem('div');
        elem.className = 'formElem';
        _html.createInput('text', 'eventtitle', 'eventtitle',
            28, 100, null, 'inputText', elem);
        d.appendChild(elem);

        // Event location
        this.createLabel(_(
            'Main.DetailForm.Location'), d);
        elem = _createElem('div');
        elem.className = 'formElem';
        _html.createInput('text', 'eventlocation', 'eventlocation',
            28, 100, null, 'inputText', elem);
        d.appendChild(elem);

        // All-day checkbox
        elem = _createElem('div');
        elem.className = 'formElem';
        _html.createInput('checkbox', 'eventallday', 'eventallday',
            null, null, 'true', null, elem);
        _html.appendNbsp(elem);
        _html.appendNbsp(elem);
        elem.appendChild(_createText('All day'));
        d.appendChild(elem);

        // Start
        this.createDateTimeInputs('Starts', 'start', d);

        // End
        this.createDateTimeInputs('Ends', 'end', d);

        //Timezones!
        this.createTimezoneInputs(d);

        // Event status
        this.createLabel('Status', d);
        elem = _createElem('div');
        elem.className = 'formElem';
        _html.createSelect('status', 'status', null, null,
            this.getStatusOpt(), 'selectElem', elem);
        d.appendChild(elem);

        // Recurrence
        // ------------------------
        // Recurrence date
        this.createLabel('Occurs', d);
        elem = _createElem('div');
        elem.className = 'formElem';
        _html.createSelect('recurrence', 'recurrence', null, null,
            this.getRecurOpt(), 'selectElem', elem);

        _html.appendNbsp(elem);
        elem.appendChild(_createText('ending'));
        _html.appendNbsp(elem);

        // Recurrence ending
        elem.className = 'formElem';
        _html.createInput('text', 'recurend', 'recurend',
            10, 10, null, 'inputText', elem);
        d.appendChild(elem);

        // Details textarea
        this.createLabel(_(
            'Main.DetailForm.Description'), d);
        elem = _createElem('div');
        elem.className = 'formElem';
        formElem = _createElem('textarea');
        formElem.className = 'inputText';
        formElem.id = 'eventdescr';
        formElem.name = 'eventdescr';
        formElem.cols = '28';
        formElem.rows = '4';
        formElem.style.width = '220px';
        elem.appendChild(formElem);
        d.appendChild(elem);

        // Div elements for Remove and Save buttons
        elem = _createElem('div');
        elem.id = 'eventDetailSave';
        elem.className = 'floatRight';
        d.appendChild(elem);

        elem = _createElem('div');
        elem.className = 'floatRight';
        _html.appendNbsp(elem);
        d.appendChild(elem);

        elem = _createElem('div');
        elem.id = 'eventDetailRemove';
        elem.className = 'floatRight';
        d.appendChild(elem);

        elem = _createElem('div');
        elem.className = 'clearAll';
        d.appendChild(elem);

        cont.appendChild(d);
        info.appendChild(cont);

        return true;
    };
    this.createDateTimeInputs = function (label, name, d) {
        var elem = null;
        this.createLabel(_(
            'Main.DetailForm.' + label), d);
        elem = _createElem('div');
        elem.className = 'formElem';
        elem.style.whiteSpace = 'nowrap';
        _html.createInput('text', name + 'date', name + 'date',
            10, 10, null, 'inputText', elem);
        _html.appendNbsp(elem);
        elem.appendChild(_createText(
            _('Main.DetailForm.At')));
        _html.appendNbsp(elem);
        _html.createInput('text', name + 'time', name + 'time',
            5, 5, null, 'inputText', elem);
        _html.appendNbsp(elem);
        _html.appendNbsp(elem);
        _html.createInput('radio', name + 'ap', name + 'ap', null,
            null, 1, null, elem);
        _html.appendNbsp(elem);
        elem.appendChild(_createText(
            _('App.AM')));
        _html.appendNbsp(elem);
        _html.appendNbsp(elem);
        _html.createInput('radio', name + 'ap', name + 'ap', null,
            null, 2, null, elem);
        _html.appendNbsp(elem);
        elem.appendChild(_createText(
            _('App.PM')));
        d.appendChild(elem);
    };
    
    this.createTimezoneInputs = function (d){
        var elem = null;

        //create the main label
        this.createLabel(_(
            'Main.DetailForm.Timezone'), d);
        elem = _createElem('div');
        elem.className = 'formElem';
        elem.style.whiteSpace = 'nowrap';

        //create the region selector
        var sel = _html.createSelect('tzRegion', 'tzRegion', null, 
            false, this.getTimezoneOptions(), 'selectElem');
        sel.style.width = '90px';
        elem.appendChild(sel);
        _html.appendNbsp(elem);
        sel = _html.createSelect('tzId', 'tzId', null, 
            false, this.getTimezoneSelectorOptions(null), 'selectElem');
        // Limit width -- these options can be very long
        sel.style.width = '144px';
        elem.appendChild(sel);
        d.appendChild(elem);
    };

    this.getTimezoneSelectorOptions = function (region){
        var tzIds = region ? cosmo.datetime.timezone.getTzIdsForRegion(region).sort() : null;
        var options = [{ 
            text: _("Main.DetailForm.TimezoneSelector.None"), 
            value: "" }];
        if (tzIds){
            dojo.lang.map(tzIds, function (tzId) {
                //Strip off the Region, turn underscores into spaces for display
                options.push({text:tzId.substr(tzId.indexOf("/") + 1).replace(/_/g," "), value:tzId});
            });
        }
        return options;
    };
    
    this.setMailtoLink = function (event) {
        var timeFormat=_("Sidebar.Email.TimeFormat");

        var subject = Cal.currentCollection.displayName + ": " + event.data.title;
        var body = [_("Sidebar.Email.Title") , event.data.title , "%0d%0a"];
        
        if (event.data.start.tzId){
            body = body.concat([
             _("Sidebar.Email.Timezone")  , event.data.start.tzId , "%0d%0a"]);
        }
        body = body.concat([
             _("Sidebar.Email.Starts") , event.data.start.strftime(timeFormat) , "%0d%0a" , 
             _("Sidebar.Email.Ends") , event.data.end.strftime(timeFormat) , "%0d%0a"]);
        if (event.data.allDay) {
            body.push(_("Sidebar.Email.AllDay") + "%0d%0a");
        } 

        if (event.data.recurrenceRule){
            body = body.concat([_("Sidebar.Email.Recurs") , 
                event.data.recurrenceRule.frequency]);
            if (event.data.recurrenceRule.endDate){
                body = body.concat([_("Sidebar.Email.EndingOn") , 
                    event.data.recurrenceRule.endDate.strftime(timeFormat)]);
            }
            body.push(".%0d%0a");
            
        }
        if (event.data.status){
            body.concat([_("Sidebar.Email.Status") , event.data.status , "%0d%0a"]);
        }
        if (event.data.description){
            body = body.concat([ ,
                _("Sidebar.Email.Description") , event.data.description , "%0d%0a"]);
        }
        this.mailtoLink.setAttribute("href", "mailto:?subject=" + subject + "&body=" + body.join(""));
    };

    this.populateTimezoneSelector = function (region){
        var options = this.getTimezoneSelectorOptions(region);
        _html.setSelectOptions(this.form.tzId, options);
    };

    this.handleRegionChanged = function (event){
        self.populateTimezoneSelector(event.target.value);
    };

    this.createLabel = function (str, d) {
        var elem = _createElem('div');
        elem.className = 'labelTextVert';
        elem.appendChild(_createText((str)));
        if (d) {
            d.appendChild(elem);
            return true;
        }
        else {
            return elem;
        }
    };
    this.setTextInput = function (textbox,
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
    this.createButtons = function (enableRemove, enableSave) {
        var checkElem = null;
        var f = null;

        f = enableRemove ? removeCalEvent : null;
        removeButton = new Button('removeButton', 74,
            f, _('App.Button.Remove'));
        f = enableSave ? saveCalEvent : null;
        saveButton = new Button('savebutton', 74,
            f, _('App.Button.Save'));

        checkElem = $('removeButton');
        if (checkElem) {
            checkElem.parentNode.removeChild(checkElem);
        }
        checkElem = $('savebutton');
        if (checkElem) {
            checkElem.parentNode.removeChild(checkElem);
        }
        $('eventDetailRemove').appendChild(removeButton.domNode);
        $('eventDetailSave').appendChild(saveButton.domNode);
    };
    /**
     *
     */
    this.setButtons = function (r, s) {
        rem = r;
        sav = s;
        if (!Cal.currentCollection.privileges.write) {
            rem = false;
            sav = false;
        }
        removeButton.setEnabled(rem);
        saveButton.setEnabled(sav);
    };
    this.getRecurOpt = function () {
        var recurOpt = [];
        var opt = null;
        var str = '';

        opt = new Object();
        opt.text = 'Once';
        opt.value = '';
        recurOpt.push(opt);
        for (var i in RecurrenceRuleFrequency) {
            opt = new Object();
            str = RecurrenceRuleFrequency[i];
            opt.text = dojo.string.capitalize(str);
            opt.value = str;
            recurOpt.push(opt);
        }
        return recurOpt;
    };

    this.getStatusOpt = function () {
        var statusOpt = [];
        var opt = null;
        var str = '';

        for (var i in EventStatus) {
            opt = new Object();
            str = EventStatus[i];
            if(str == EventStatus.FYI) {
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

    this.getTimezoneOptions = function (){
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

    /**
     * Update the event's CalEventData obj from the values in the form
     * Called when clicking the Save button or hitting Enter
     * BANDAID: Currently still building native JS Date objects from
     * the values in the form and then gettting ScoobyDates for the
     * event based on those -- should be building ScoobyDates directly
     * from the form
     */
    this.updateEvent = function (ev) {
        var form = this.form;
        var startDate = '';
        var endDate = '';
        var startTime = '';
        var endTime = '';
        var title = '';
        var evtLocation = '';
        var descr = '';
        var status = '';
        var allDay = false;
        var recur = null;
        var rE = '';
        var h = 0;
        var m = 0;
        var err = '';
        var errMsg = '';
        var e = null;
        var tzId = null;
        // Pull new values out of the event info form
        startDate = form.startdate.value;
        endDate = form.enddate.value;
        startTime = form.starttime.value;
        endTime = form.endtime.value;
        title = form.eventtitle.value;
        evtLocation = form.eventlocation.value;
        descr = form.eventdescr.value;
        status = form.status.value;
        allDay = form.eventallday.checked ? true : false;
        recur = form.recurrence.value;
        rE = form.recurend.value != 'mm/dd/yyyy' ?
            form.recurend.value : '';
        tzId = form.tzId.value || null;

        // Error checking
        // =======================
        if (!title) {
            errMsg += '"Title" is a required field.\n'
        }
        err = cosmo.util.validate.dateFormat(startDate);
        if (err) {
            errMsg += '"Starts" date field: ' + err;
            errMsg += '\n';
        }
        err = cosmo.util.validate.dateFormat(endDate);
        if (err) {
            errMsg += '"Ends" date field: ' + err;
            errMsg += '\n';
        }
        // cosmo.util.validate.times for normal events
        // Normal events will have at least a start time
        if (startTime) {
            var meridian = null;
            var basicDateValidationError = false;
            err = cosmo.util.validate.timeFormat(startTime);
            if (err) {
                basicDateValidationError = true;
                errMsg += '"Starts" time field: ' + err;
                errMsg += '\n';
            }
            meridian = cosmo.util.html.getRadioButtonSetValue(
                form.startap);
            err = cosmo.util.validate.required(meridian); 
            if (err) {
                basicDateValidationError = true;
                errMsg += '"Starts" AM/PM field: ' + err;
                errMsg += '\n';
            }
            err = cosmo.util.validate.required(endTime) || 
                cosmo.util.validate.timeFormat(endTime);
            if (err) {
                basicDateValidationError = true;
                errMsg += '"Ends" time field: ' + err;
                errMsg += '\n';
            }
            meridian = cosmo.util.html.getRadioButtonSetValue(
                form.endap);
            err = cosmo.util.validate.required(meridian); 
            if (err) {
                basicDateValidationError = true;
                errMsg += '"Ends" AM/PM field: ' + err;
                errMsg += '\n';
            }
        }
        // cosmo.util.validate.recurrence end date if it's there
        if (recur && rE) {
            err = cosmo.util.validate.dateFormat(rE);
            if (err) {
                errMsg += '"Occurs" ending date field: ' + err;
                errMsg += '\n';
            }
        }

        // Calc military datetimes from form entries
        startDate = new Date(startDate);
        if (startTime) {
            h = Cal.extractHourFromTime(startTime);
            h = hrStd2Mil(h, form.startap[1].checked);
            m = Cal.extractMinutesFromTime(startTime);
            startDate.setHours(h, m);
        }
        endDate = new Date(endDate);
        if (endTime) {
            h = Cal.extractHourFromTime(endTime);
            h = hrStd2Mil(h, form.endap[1].checked);
            m = Cal.extractMinutesFromTime(endTime);
            endDate.setHours(h, m);
        }
        // Validate that start is before end
        if (!basicDateValidationError && (startDate.getTime() > endDate.getTime())) {
            errMsg += '"Starts" and "Ends" time fields: ';
            errMsg += 'Event cannot end before it starts.';
            errMsg += '\n';
        }

        // Display error or update form and submit
        // =======================
        // Err condition
        if (errMsg) {
            errMsg = errMsg.replace(/\n/g, '<br/>');
            e = new ScoobyServiceClientException();
            e.message = errMsg;
            cosmo.app.showErr(_('Main.DetailForm.Error'), e);
            return false;
        }
        // All okey-dokey -- submit
        else {
            // Set event properties
            // ==============
            var d = ev.data;

            // cosmo.datetime.Date with timezones
            if (tzId) {
                d.start.tzId = tzId;
                d.start.updateFromLocalDate(startDate);
                d.end.tzId = tzId;
                d.end.updateFromLocalDate(endDate);
            }
            // Floating cosmo.datetime.Date 
            else {
                d.start.tzId = null;
                d.start.updateFromUTC(startDate.getTime());
                d.start.utc = false;
                d.end.tzId = null;
                d.end.updateFromUTC(endDate.getTime());
                d.end.utc = false;
            }
            d.title = title;
            d.location = evtLocation;
            d.description = descr || null;
            d.allDay = allDay;
            d.anyTime = (!startTime && !endTime && !allDay) ? true : false;
            d.status = status;
            
            var rule = d.recurrenceRule;
            // Set to no recurrence
            if (!recur) {
                d.recurrenceRule = null;
            }
            else {
                var recurEnd = null;
                if (rE) {
                    rE = new Date(rE);
                    recurEnd = new ScoobyDate(rE.getFullYear(), rE.getMonth(), rE.getDate());
                }
                if (rule) {
                    rule.frequency = recur;
                }
                else {
                   rule = new RecurrenceRule();
                   rule.frequency = recur;
                   d.recurrenceRule = rule;
                }
                d.recurrenceRule.endDate = recurEnd;
            }
            return true;
        }
    };
    /**
     * Update values displayed in the form from the properties in the
     * CalEventData obj for the event.
     */
    this.updateFromEvent = function (ev) {
        var form = this.form;
        var recur = ev.data.recurrenceRule;
        var status = ev.data.status;
        form.eventtitle.value = ev.data.title;
        form.eventlocation.value = ev.data.location;
        form.eventdescr.value = ev.data.description ?
            ev.data.description : '';
        form.status.value = ev.data.statusBar ? ev.data.status : '';
        form.startdate.value = ev.data.start.strftime('%m/%d/%Y');
        form.enddate.value = ev.data.end.strftime('%m/%d/%Y');
        form.eventallday.checked = ev.data.allDay ? true : false;
        // Set mailto link
        this.setMailtoLink(ev);
        if (ev.data.allDay || ev.data.anyTime) {
            this.setTimeElem(null, 'start');
            this.setTimeElem(null, 'end');
        }
        else {
            this.setTimeElem(ev.data.start, 'start');
            this.setTimeElem(ev.data.end, 'end');
        }
        var enable = ev.data.allDay ? false : true;
        this.enableDisableTimeElem('start', enable);
        this.enableDisableTimeElem('end', enable);

        if(status) {
            _html.setSelect(this.form.status, status);
        } else {
            _html.setSelect(this.form.status, "CONFIRMED");
        }

        if (recur) {
            _html.setSelect(this.form.recurrence, recur.frequency);
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
            _html.setSelect(this.form.recurrence, '');
            this.setTextInput(form.recurend, 'mm/dd/yyyy', true, true);
        }

        if (ev.data.start.tzId){
            var timezone = cosmo.datetime.timezone.getTimezone(ev.data.start.tzId);
            if (!timezone){
                self.clearTimezone();
            } else {
                //we use this tzid in case the event has a "legacy" tzId,
                //like "US/Pacific" as opposed to "America/Los_angeles"
                var tzId = timezone.tzId;
                var region = tzId.split("/")[0];
                _html.setSelect(this.form.tzRegion, region);
                self.populateTimezoneSelector(region);
                _html.setSelect(this.form.tzId, tzId);
            }
        } else {
            self.clearTimezone();
        }

        // All-day, anytime events cannot have a timezone, normal events can
        if (ev.data.allDay || ev.data.anyTime) {
            this.form.tzRegion.disabled = true;
            this.form.tzId.disabled = true;
        }
        else {
            this.form.tzRegion.disabled = false;
            this.form.tzId.disabled = false;
        }
    };

    this.setRecurEnd = function () {
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
    this.setTimeElem = function (time, name) {
        var form = this.form;
        var timeElem = null;
        var meridianElem = null;
        var strtime = '';

        timeElem = form[name + 'time'];
        meridianElem = form[name + 'ap'];
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
            timeElem.value = strtime;
        }
        else {
            meridianElem[1].checked = false;
            meridianElem[0].checked = false;
            timeElem.value = '';
        }

    };
    this.enableDisableTimeElem = function (name, enable) {
        var form = this.form;
        var timeElem = form[name + 'time'];
        var meridianElem = form[name + 'ap'];
        meridianElem[1].disabled = !enable;
        meridianElem[0].disabled = !enable;
        timeElem.disabled = !enable;
    };
    /**
     * Reloading the page in some browsers preserves form information
     * This method empties the event info form of any entered values.
     */
    this.clear = function () {
        var form = this.form;
        // Update info in event form
        form.eventtitle.value = '';
        form.eventlocation.value = '';
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
        self.clearTimezone();
        this.setTextInput(form.recurend, 'mm/dd/yyyy', true, true);
        return true;
    };

    this.clearTimezone = function (){
        this.form.tzRegion.selectedIndex = 0;
        self.populateTimezoneSelector();
    }

    /**
     * Toggle an event from 'normal' (HasTime) to 'all-day' (NoTime)
     */
    this.toggleLozengeType = function () {
        var allDay = this.form.eventallday.checked ? true : false;
        var setDate = new Date();
        setDate.setMinutes(0);
        if (allDay) {
            this.setTimeElem(null, 'start');
            this.setTimeElem(null, 'end');
        }
        else {
            this.enableDisableTimeElem('start', true);
            this.enableDisableTimeElem('end', true);
        }
    };
    /**
     * Add event listeners to text inputs and textareas to
     * suppress listener for Delete and Enter keys while typing
     * in these form fields
     * Also adds event listener to 'all-day' checkbox to toggle
     * event type
     */
    this.setEventListeners = function () {
        var self = this;
        var allDayCheck = $('eventallday');
        var form = Cal.calForm.form;
        
        // Add dummy function event listener so form doesn't
        // submit on Enter keypress in Safari
        form.onsubmit = function () { return false; };
        
        // Focus handlers for text entry
        dojo.event.connect(form.eventtitle, 'onfocus', cosmo.util.html, 'handleTextInputFocus');
        dojo.event.connect(form.eventlocation, 'onfocus', cosmo.util.html, 'handleTextInputFocus');
        dojo.event.connect(form.startdate, 'onfocus', cosmo.util.html, 'handleTextInputFocus');
        dojo.event.connect(form.starttime, 'onfocus', cosmo.util.html, 'handleTextInputFocus');
        dojo.event.connect(form.enddate, 'onfocus', cosmo.util.html, 'handleTextInputFocus');
        dojo.event.connect(form.endtime, 'onfocus', cosmo.util.html, 'handleTextInputFocus');
        
        // Recurrence
        dojo.event.connect(form.recurrence, 'onchange', self, 'setRecurEnd');
        dojo.event.connect(form.recurend, 'onfocus', cosmo.util.html, 'handleTextInputFocus');
        
        // All-day event / normal event toggling
        dojo.event.connect(allDayCheck, 'onclick', cosmo.ui.cal_main.Cal.calForm, 'toggleLozengeType');
        
        var regionSelectorElement = $("tzRegion");
        dojo.event.connect(regionSelectorElement, "onchange", this.handleRegionChanged);
        
        dojo.event.topic.subscribe(cosmo.topics.CollectionUpdatedMessage.topicName, Cal, Cal.handleCollectionUpdated);
        dojo.event.topic.subscribe(cosmo.topics.SubscriptionUpdatedMessage.topicName, Cal, Cal.handleSubscriptionUpdated);
        dojo.event.topic.subscribe(cosmo.topics.ModalDialogDisplayed.topicName, Cal, Cal.handleModalDialogDisplayed);
        dojo.event.topic.subscribe(cosmo.topics.ModalDialogDismissed.topicName, Cal, Cal.handleModalDialogDismissed);

    };
    this.addJumpToDate = function (dMain) {
        var top = parseInt(cosmo.ui.minical.MiniCal.displayContext.style.top);
        var d = null;

        // place the div just above minical
        top -= 28;
        dMain.style.top = top + 'px';
        var dc = _createElem('div');
        dMain.appendChild(dc);

        d = _createElem('div');
        d.className = 'floatLeft';
        d.style.paddingTop = '3px';
        d.appendChild(_createText(_('Main.GoTo')));
        dc.appendChild(d);

        d = _createElem('div');
        d.className = 'floatLeft';
        _html.appendNbsp(d);
        dc.appendChild(d);

        d = _createElem('div');
        d.className = 'formElem floatLeft';
        dc.appendChild(d);
        _html.createInput('text', 'jumpto', 'jumpto',
            10, 10, null, 'inputText', d);
        self.setTextInput(self.form.jumpto, 'mm/dd/yyyy', true, false);
        dojo.event.connect(self.form.jumpto, 'onfocus', cosmo.util.html, 'handleTextInputFocus');
        
        d = _createElem('div');
        d.className = 'floatLeft';
        _html.appendNbsp(d);
        _html.appendNbsp(d);
        dc.appendChild(d);

        d = _createElem('div');
        d.className = 'floatLeft';
        dc.appendChild(d);
        butJump = new Button('jumpToButton', 32, Cal.calForm.goJumpToDate,
                _('App.Button.Go'), true);
        d.appendChild(butJump.domNode);

        d = _createElem('div');
        d.className = 'clearAll';
        dc.appendChild(d);

        // Do some hokey calculations and pixel positioning
        // to center this stuff -- so CSS is better than
        // tables HOW exactly?
        var wInner = dc.offsetWidth;
        var wOuter = LEFT_SIDEBAR_WIDTH;
        var lOffset = Math.round((wOuter - wInner)/2);
        dc.style.position = 'absolute';
        dc.style.width = wInner + 'px';
        dc.style.left = lOffset + 'px';
        dMain.style.width = LEFT_SIDEBAR_WIDTH + 'px';
    };
    this.goJumpToDate = function () {
        var e = null;
        var err = '';
        var val = self.form.jumpto.value;
        err = cosmo.util.validate.dateFormat(val);
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
            cosmo.app.showErr(_('Main.Error.GoToDate'), e);
            return false;
        }
        // All okey-dokey -- submit
        else {
            var d = new Date(val);
            dojo.event.topic.publish('/calEvent', { 
                action: 'loadCollection', data: { goTo: d } 
            }); 
        }
    };
    
}
CalForm = cosmo.ui.cal_form.CalForm;
