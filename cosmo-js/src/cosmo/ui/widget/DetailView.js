/*
 * Copyright 2008 Open Source Applications Foundation
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
dojo.provide("cosmo.ui.widget.DetailView");
dojo.require("dojo.fx");

dojo.require("dijit._Templated");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.Textarea");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.TimeTextBox");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.Button");

dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.Delta");
dojo.require("cosmo.datetime.timezone");
dojo.require("cosmo.util.html");
dojo.require("cosmo.ui.Error");
dojo.require("cosmo.ui.widget.HintBox");

dojo.requireLocalization("cosmo.ui.widget", "DetailView");
(function(){
dojo.declare("cosmo.ui.widget.DetailView", [dijit._Widget, dijit._Templated], {
    templatePath: dojo.moduleUrl("cosmo", "ui/widget/templates/DetailView.html"),
    widgetsInTemplate: true,

    initItem: null,
    initializing: false,
    recursionCount: 0,
    preserveEventFields: false,

    // Attach points
    triageButtons: null,
    nowButton: null,
    laterButton: null,
    doneButton: null,
    starButton: null,
    mailButton: null,
    titleInput: null,
    notesInput: null,
    eventTitleSpan: null,
    eventButton: null,
    eventSection: null,
    locationInput: null,
    allDayInput: null,
    startDateInput: null,
    startTimeInput: null,
    endDateInput: null,
    endTimeInput: null,
    timezoneRegionSelector: null,
    timezoneIdSelector: null,
    statusSelector: null,
    recurrenceSelector: null,
    untilInput: null,
    byline: null,
    removeButton: null,
    saveButton: null,
    containerNode: null,

    //fields
    event: false,
    item: null,
    itemWrapper: null,
    triage: null,
    starred: null,
    eventButtonDisabled: true,
    starButtonDisabled: true,
    mailButtonDisabled: true,
    triageButtonsDisabled: true,
    readOnly: false,

    updateFromItemWrapper: function(itemWrapper){
        this.itemWrapper = itemWrapper;
        this.updateFromItem(itemWrapper.data);
    },

    updateFromItem: function(item){
        this.initializing = true;
        this.enable();
        this.titleInput.setValue(item.getDisplayName());
        this.notesInput.setValue(item.getBody());
        this.setTriageStatus(item.getTriageStatus());
        var eventStamp = item.getEventStamp();
        if (eventStamp){
            this.updateFromEventStamp(eventStamp);
            this.preserveEventFields = true;
            this.enableEvent();
        } else {
            this.preserveEventFields = false;
            this.disableEvent();
        }
        if(item.getTaskStamp()) this.setStarred();
        else this.setUnstarred();
        this.item = item;
        if (!cosmo.app.pim.getSelectedCollectionWriteable()) this.setReadOnly();
        this.initializing = false;
        this.updateVisibility();
    },

    disable: function(){
        this.titleInput.setAttribute("disabled", true);
        this.notesInput.setAttribute("disabled", true);
        this.saveButton.setAttribute("disabled", true);
        this.removeButton.setAttribute("disabled", true);
        this.eventButtonDisabled = true;
        this.starButtonDisabled = true;
        this.mailButtonDisabled = true;
        this.triageButtonsDisabled = true;
        this.disableEventFields();
    },

    enable: function(){
        this.titleInput.setAttribute("disabled", false);
        this.notesInput.setAttribute("disabled", false);
        this.saveButton.setAttribute("disabled", false);
        this.removeButton.setAttribute("disabled", false);
        this.eventButtonDisabled = false;
        this.starButtonDisabled = false;
        this.mailButtonDisabled = false;
        this.triageButtonsDisabled = false;
    },

    clearSelected: function(){
        this.item = null;
        this.clearFields();
        this.disable();
        this.hideEvent();
        this.hideDetailView();
    },

    hideDetailView: function(){
        dojo.addClass(this.domNode, "cosmoDetailHidden");
    },

    showDetailView: function(){
        dojo.removeClass(this.domNode, "cosmoDetailHidden");
    },

    clearFields: function(){
        this.clearEventFields();
        this.clearTriage();
        this.titleInput.setValue("");
        this.notesInput.setValue("");
        this.setUnstarred();
    },

    clearTriage: function(){
        dojo.removeClass(this.nowButton, "cosmoTriageNowButtonSelected");
        dojo.removeClass(this.laterButton, "cosmoTriageLaterButtonSelected");
        dojo.removeClass(this.doneButton, "cosmoTriageDoneButtonSelected");
    },

    setTriageNow: function(){
        this.clearTriage();
        this.triage = cosmo.model.TRIAGE_NOW;
        dojo.addClass(this.nowButton, "cosmoTriageNowButtonSelected");
    },

    setTriageLater: function(){
        this.clearTriage();
        this.triage = cosmo.model.TRIAGE_LATER;
        dojo.addClass(this.laterButton, "cosmoTriageLaterButtonSelected");
    },

    setTriageDone: function(){
        this.clearTriage();
        this.triage = cosmo.model.TRIAGE_DONE;
        dojo.addClass(this.doneButton, "cosmoTriageDoneButtonSelected");
    },

    setTriageStatus: function(status){
        switch(status){
            case cosmo.model.TRIAGE_NOW:
                this.setTriageNow();
                break;
            case cosmo.model.TRIAGE_LATER:
                this.setTriageLater();
                break;
            case cosmo.model.TRIAGE_DONE:
                this.setTriageDone();
                break;
            default:
                throw new Error("Triage must be now later or done.");
        }
    },

    toggleStarred: function(){
        this.starred? this.setUnstarred() : this.setStarred();
    },

    setStarred: function(){
        this.starred = true;
        dojo.addClass(this.starButton, "cosmoTaskButtonSelected");
    },

    setUnstarred: function(){
        this.starred = false;
        dojo.removeClass(this.starButton, "cosmoTaskButtonSelected");
    },

    /* Event Stamp functions */

    updateFromEventStamp: function(stamp){
        this.locationInput.setValue(stamp.getLocation());

        var startDate = stamp.getStartDate();
        var endDate = stamp.getEndDate();
        this.startDateInput.setValue(startDate);
        this.endDateInput.setValue(endDate);
        this.updateAllDay(stamp.getAllDay());
        if (!(stamp.getAnyTime() || stamp.getAllDay())){
            this.startTimeInput.setValue(startDate);
            this.endTimeInput.setValue(endDate);
        } else {
            this.startTimeInput.setValue(null);
            this.endTimeInput.setValue(null);
        }
        if (startDate.tzId){
            this.updateFromTimezone(cosmo.datetime.timezone.getTimezone(startDate.tzId));
        } else {
            this.clearTimezoneSelectors();
        }

        this.statusSelector.value = stamp.getStatus();
        this.updateFromRrule(stamp.getRrule());
    },

    updateAllDay: function(allDay){
        this.allDayInput.setValue(allDay);
    },

    updateFromTimezone: function(tz){
        if (tz){
            var tzId = tz.tzId;
            var region = tzId.split("/")[0];
            this.updateFromTimezoneRegion(region);
            cosmo.util.html.setSelect(this.timezoneIdSelector, tzId);
        } else {
            this.clearTimezoneSelectors();
        }
    },

    updateFromTimezoneRegion: function(region){
        if (region){
            cosmo.util.html.setSelect(this.timezoneRegionSelector, region);
            cosmo.util.html.setSelectOptions(this.timezoneIdSelector, this.getTimezoneIdOptions(region));
        }
        this.setTimezoneSelectorVisibility();
    },

    setTimezoneSelectorVisibility: function(){
        if (this.timezoneRegionSelector.value)
            this.showTimezoneSelectors();
        else
            this.clearTimezoneSelectors();
    },

    showTimezoneSelectors: function(){
        dojo.removeClass(this.timezoneIdSelector, "cosmoDetailHidden");
        dojo.removeClass(this.timezoneRegionSelector, "expandWidth");
    },

    clearTimezoneSelectors: function(){
        this.timezoneRegionSelector.value = "";
        this.timezoneIdSelector.value = "";
        dojo.addClass(this.timezoneIdSelector, "cosmoDetailHidden");
        dojo.addClass(this.timezoneRegionSelector, "expandWidth");
    },

    getTimezoneIdOptions: function(region){
        return [{text: this.l10n.noTzId,
                 value: "" }
               ].concat(dojo.map(cosmo.datetime.timezone.getTzIdsForRegion(region),
                   function(id){
                       return {
                           text: id.substr(
                               id.indexOf("/") + 1).replace(/_/g," "),
                               value: id
                       };
                   }));
    },

    updateFromRrule: function(rrule){
        if (rrule){
            if (rrule.isSupported()){
                this.recurrenceSelector.value = rrule.getFrequency();
            } else {
                this.recurrenceSelector.value = 'custom';
            }
            this.untilInput.setAttribute("disabled", false);
            this.untilInput.setValue(rrule.getEndDate());
        } else {
            this.untilInput.setValue(null);
            this.untilInput.setAttribute("disabled", true);
            this.recurrenceSelector.value = 'once';
        }
    },

    clearEventFields: function(){
        this.locationInput.setValue("");
        this.allDayInput.setValue(false);
        this.startDateInput.setValue("");
        this.startTimeInput.setValue("");
        this.endDateInput.setValue("");
        this.endTimeInput.setValue("");
        this.clearTimezoneSelectors();
        this.statusSelector.value = "";
        this.recurrenceSelector.value = "";
        this.untilInput.setValue("");
    },

    disableEventFields: function(){
        this.locationInput.setAttribute("disabled", true);
        this.allDayInput.setAttribute("disabled", true);
        this.startDateInput.setAttribute("disabled", true);
        this.startTimeInput.setAttribute("disabled", true);
        this.endDateInput.setAttribute("disabled", true);
        this.endTimeInput.setAttribute("disabled", true);
        this.timezoneRegionSelector.setAttribute("disabled", true);
        this.timezoneIdSelector.setAttribute("disabled", true);
        this.statusSelector.setAttribute("disabled", true);
        this.recurrenceSelector.setAttribute("disabled", true);
        this.untilInput.setAttribute("disabled", true);
    },

    enableEventFields: function(){
        this.locationInput.setAttribute("disabled", false);
        this.allDayInput.setAttribute("disabled", false);
        this.startDateInput.setAttribute("disabled", false);
        this.startTimeInput.setAttribute("disabled", false);
        this.endDateInput.setAttribute("disabled", false);
        this.endTimeInput.setAttribute("disabled", false);
        this.timezoneRegionSelector.disabled = false;
        this.timezoneIdSelector.disabled = false;
        this.statusSelector.disabled = false;
        this.recurrenceSelector.disabled = false;
        this.untilInput.setAttribute("disabled", false);
    },

    toggleEvent: function(){
        this.event? this.disableEvent() : this.enableEvent();
    },

    enableEvent: function(fast){
        this.event = true;
        this.eventTitleSpan.innerHTML = this.l10n.removeFromCalendar;
        if (!dojo.hasClass(this.eventButton, "cosmoEventButtonSelected")) this.showEvent(fast);
        this.enableEventFields();
        if (!this.preserveEventFields)
            // zero out event fields if they weren't set recently for this item
            this.clearEventFields();
        this.preserveEventFields = true;
    },

    showEvent: function(fast){
        dojo.addClass(this.eventButton, "cosmoEventButtonSelected");
        if (!fast)
            dojo.fx.wipeIn({node: this.eventSection}).play();
        else this.eventSection.style.display="block";
    },

    disableEvent: function(fast){
        this.event = false;
        this.eventTitleSpan.innerHTML = this.l10n.addToCalendar;
        if (dojo.hasClass(this.eventButton, "cosmoEventButtonSelected")) this.hideEvent(fast);
        this.disableEventFields();
    },

    hideEvent: function(fast){
        dojo.removeClass(this.eventButton, "cosmoEventButtonSelected");
        if (!fast)
            dojo.fx.wipeOut({node: this.eventSection}).play();
        else this.eventSection.style.display="none";
    },

    setReadOnly: function(){
        this.readOnly = true;
        dojo.addClass(this.domNode, "cosmoDetailViewReadOnly");
        this.disable();
    },

    setReadWrite: function(){
        this.readOnly = false;
        dojo.removeClass(this.domNode, "cosmoDetailViewReadOnly");
        this.enable();
    },

    toggleReadOnly: function(){
        this.readOnly? this.setReadWrite() : this.setReadOnly();
    },

    // event handlers
    tzRegionOnChange: function(e){
        this.updateFromTimezoneRegion(e.target.value);
    },

    rruleOnChange: function(e){
        var frequency = e.target.value;
        if (frequency == "once") this.updateFromRecurrenceRule(null);
        else if (frequency == "custom") {/*TODO??*/}
        else this.updateFromRrule(new cosmo.model.RecurrenceRule({frequency: frequency}));
        this.updateVisibility();
    },

    eventButtonOnClick: function(){
        if (!this.eventButtonDisabled){
            this.toggleEvent();
            this.updateVisibility();
        }
    },

    triageNowOnClick: function(){
        if (!this.triageButtonsDisabled)
            this.setTriageNow();
    },

    triageLaterOnClick: function(){
        if (!this.triageButtonsDisabled)
            this.setTriageLater();
    },

    triageDoneOnClick: function(){
        if (!this.triageButtonsDisabled)
            this.setTriageDone();
    },

    starButtonOnClick: function(){
        if (!this.starButtonDisabled)
            this.toggleStarred();
    },

    mailMouseDown: function(){
        if (!this.mailButtonDisabled){
            dojo.addClass(this.mailButton, "cosmoEmailButtonSelected");
            this.itemToEmail(this.item);
        }
    },

    mailMouseUp: function(){
        if (!this.mailButtonDisabled)
            dojo.removeClass(this.mailButton, "cosmoEmailButtonSelected");
    },

    itemToEmail: function(item){
        var timeFormat = this.l10n.emailTimeFormat;
        var subject = item.getDisplayName();
        var body = [this.l10n.emailTitle , item.getDisplayName() , "%0d%0a"];
        var eventStamp = item.getEventStamp();
        if (eventStamp){
            var startDate = eventStamp.getStartDate();
            var endDate = eventStamp.getEndDate();

            if (startDate.tzId) {
                body = body.concat([
                    this.l10n.emailTimezone, startDate.tzId , "%0d%0a"]);
            }
            body = body.concat([
                       this.l10n.emailStarts , dojox.date.posix.strftime(startDate, timeFormat) , "%0d%0a" ,
                       this.l10n.emailEnds , dojox.date.posix.strftime(endDate, timeFormat) , "%0d%0a"]);
            if (eventStamp.getAllDay()) {
                body.push(this.l10n.emailAllDay + "%0d%0a");
            }

            if (eventStamp.getRrule()) {
                var rrule = eventStamp.getRrule();
                body = body.concat([this.l10n.emailRecurs ,
                rrule.getFrequency()]);
                if (rrule.getEndDate()) {
                    body = body.concat([this.l10n.emailEndingOn ,
                    dojox.date.posix.strftime(rrule.getEndDate(), timeFormat)]);
                }
                body.push(".%0d%0a");
            }
            if (eventStamp.getStatus()) {
                body.concat([this.l10n.emailStatus , eventStamp.getStatus() , "%0d%0a"]);
            }
        }
        if (item.getBody()) {
            body = body.concat([ ,
                        this.l10n.emailDescription , item.getBody(), "%0d%0a"]);
        }
        var s = "mailto:?subject=" + subject + "&body=" + body.join("");
        location = s;
    },

    getDelta: function(){
        return getDetailViewDelta(this);
    },

    // lifecycle functions
    constructor: function(){
        this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "DetailView");

    },

    postCreate: function(){
        this.hideEvent();
        if (this.initItem) this.updateFromItem(this.initItem);
        else this.hideDetailView();

        if (this.readOnly) this.setReadOnly();

        var updateItems = dojo.hitch(this, function(cmd){
            var itemWrapper = cmd.data;
            if (itemWrapper) {
                var item = itemWrapper.data;
                if (item) this.showDetailView();
                // Only update the values in the form if
                // the item has actually changed -- note that
                // in the cal, when navigating off the week
                // where the selected item is displayed, the
               // selected item will in the selectedItemCache
                if (item != this.item) {
                    this.updateFromItemWrapper(itemWrapper);
                    // For brand-new items auto-focus the Title field
                    // to allow people to change the placeholder
                    // 'New Event' text quickly
                    if (cmd.saveType == 'new') {
                        this.titleInput.focus();
                    }
                }
            }
        });
        dojo.subscribe('cosmo:calEventsDisplaySuccess', updateItems);
        dojo.subscribe('cosmo:calNoItems', dojo.hitch(this, function(){this.clearSelected();}));
        dojo.subscribe('cosmo:calSetSelected', updateItems);
        dojo.subscribe('cosmo:calClearSelected', dojo.hitch(this, function(){this.clearSelected();}));
        dojo.subscribe('cosmo:calSaveSuccess', dojo.hitch(this, function(cmd){
            if (cmd.saveType != 'new') {
                this.updateFromItemWrapper(cmd.data);
            }
        }));
        dojo.subscribe('cosmo:calSaveFromForm', dojo.hitch(this, function(){
            this.saveItem();
        }));
    },

    // getters
    getTriage: function(){
        return this.triage;
    },

    getBody: function(){
        return this.notesInput.getValue();
    },

    getDisplayName: function(){
        return this.titleInput.getValue();
    },

    getDateTime: function(dateField, timeField){
        var dateFieldValue = dateField.getValue();
        if (!dateFieldValue) return null;
        var timeFieldValue = this.getAllDay() ? null : timeField.getValue();
        if (timeFieldValue){
            dateFieldValue.setHours(timeFieldValue.getHours(), timeFieldValue.getMinutes());
        }
        var tzIdFieldValue = this.timezoneIdSelector.value;
        var dt = new cosmo.datetime.Date();
        if (tzIdFieldValue && timeFieldValue){
            dt.tzId = tzIdFieldValue;
            dt.utc = false;
            dt.updateFromLocalDate(dateFieldValue);
        } else {
            dt.tzId = null;
            dt.utc = false;
            dt.updateFromUTC(dateFieldValue.getTime());
        }
        return dt;
    },

    getStartDateTime: function(){
        return this.getDateTime(this.startDateInput, this.startTimeInput);
    },

    getEndDateTime: function(){
        return this.getDateTime(this.endDateInput, this.endTimeInput);
    },

    getLocation: function(){
        return this.locationInput.getValue();
    },

    getStatus: function(){
        return this.statusSelector.value;
    },

    getAllDay: function(){
        return this.allDayInput.checked;
    },

    getAnyTime: function(){
        return !this.getAllDay() && !this.startTimeInput.getValue() && !this.endTimeInput.getValue();
    },

    getRrule: function(){
        var frequencyFieldValue = this.recurrenceSelector.value;
        var endDateFieldValue = this.untilInput.getValue();
        if (frequencyFieldValue == "once") return null;
        else if (frequencyFieldValue == "custom"){
            return this.item.getEventStamp().getRrule();
        } else {
            var endDate;
            if (endDateFieldValue){
                endDate = new cosmo.datetime.Date(endDateFieldValue.getFullYear(),
                endDateFieldValue.getMonth(), endDateFieldValue.getDate());
                var tzIdFieldValue = this.timezoneIdSelector.value;
                if (tzIdFieldValue){
                    endDate.tzId = tzIdFieldValue;
                }
            }
            return new cosmo.model.RecurrenceRule({
                frequency: frequencyFieldValue,
                endDate: endDate});
        }
    },

    getEventVisibility: function(){
        /* Return a dictionary, keys are event related fields, values are
         * true if the field should be visible/set in deltas.
         */
        var dict = {timezone: false, status: false, until: false, time: false};
        if (this.isEvent()){
            if (this.recurrenceSelector.value != 'once')
                dict.until = true;
            if (this.getAllDay())
                dict.status = true;
            else {
                dict.time = true;
                if (!this.getAnyTime()) {
                    dict.timezone = true;
                    var start = this.getStartDateTime();
                    if (!start || !start.equals(this.getEndDateTime()))
                        dict.status = true;
                }
            }
        }
        return dict;
    },

    updateVisibility: function(){
        if (this.recursionCount > 0 || this.initializing) return;
        this.recursionCount += 1;
        function fade(node, visible){
            function setClass(){
                var apply = (visible) ? dojo.removeClass : dojo.addClass;
                apply(node, "cosmoDetailHidden");
            }
            if (visible){
                setClass();
                return dojo.fadeIn({node:node});
            } else {
                return dojo.fadeOut({node:node, onEnd: setClass});
            }
            // return a dummy animation to keep dojo.fx.combine happy
            return new dojo._Animation;
        }


        var dict = this.getEventVisibility();
        this.setTimezoneSelectorVisibility();
        dojo.fx.combine([
            fade(this.timezoneContainer,        dict.timezone),
            fade(this.startTimeInput.domNode,   dict.time),
            fade(this.endTimeInput.domNode,     dict.time),
            fade(this.untilInput.domNode,       dict.until),
            fade(this.statusSelector,           dict.status)
        ]).play();
        this.recursionCount -= 1;
    },

    updateStartDate: function(){
        if (!this.endDateInput.getValue())
            this.endDateInput.setValue(this.startDateInput.getValue());
        this.updateVisibility();
    },

    updateStartTime: function(){
        if (!this.endTimeInput.getValue())
            this.endTimeInput.setValue(this.startTimeInput.getValue());
        this.updateVisibility();
    },

    isEvent: function(){
        return this.event;
    },

    isStarred: function(){
        return this.starred;
    },

    // validation

    validate: function(dv){
        var e = new cosmo.ui.ErrorList();
        if (this.isEvent()){
            if (this.timezoneRegionSelector.value && !this.timezoneIdSelector.value)
                e.addError(new cosmo.ui.Error(null, "App.Error.NoTzId"));
            if (!this.startTimeInput.getValue() && this.endTimeInput.getValue())
                e.addError(new cosmo.ui.Error(null,"App.Error.NoEndTimeWithoutStartTime"));
            if (!this.startDateInput.getValue()) e.addError(new cosmo.ui.Error(null, null, this.l10n.startDateRequired));
            if (!this.endDateInput.getValue()) e.addError(new cosmo.ui.Error(null, null, this.l10n.endDateRequired));
        }
        return e;
    },

    // CRUD
    saveItem: function(){
        var error = this.validate().toString();
        var delta = this.getDelta();
        if (error){
            cosmo.app.showErr(_('Main.DetailForm.Error'), error);
            return;
        } else {
            if (!delta.hasChanges()){
                return;
            }

            this.itemWrapper.makeSnapshot();
            dojo.publish('cosmo:calSaveConfirm', [{delta: delta, data: this.itemWrapper }]);
        }
    },

    removeItem: function(){
        dojo.publish('cosmo:calRemoveConfirm', [{data: this.itemWrapper }]);
    },

    // legacy
    //TODO: deprecate

    render: function(){
        if (this.item){
//             It's actively counterproductive to update, it causes
//             data to be lost if there have been edits.  This doesn't seem
//             necessary, so don't do it
//             this.updateFromItem(this.item);
        } else {
            this.clearFields();
            this.disable();
            this.hideEvent();
        }
    }
});


// To delta functions
function getDetailViewDelta(detailView){
    var delta = new cosmo.model.Delta(detailView.item);

    populateDeltaTriage(detailView, delta);
    populateDeltaNoteFields(detailView, delta);

    if (detailView.isEvent()){
        delta.addAddedStamp("event");
        populateDeltaEventFields(detailView, delta);
    } else {
        delta.addDeletedStamp("event");
    }

    if (detailView.isStarred()){
        delta.addAddedStamp("task");
    } else {
        delta.addDeletedStamp("task");
    }
    delta.deltafy(true);
    return delta;
}

function populateDeltaNoteFields(dv, delta){
    delta.addProperty("displayName", dv.getDisplayName());
    delta.addProperty("body", dv.getBody());
}

function populateDeltaEventFields(dv, delta){
    delta.addStampProperty("event", "startDate", dv.getStartDateTime());
    delta.addStampProperty("event", "endDate", dv.getEndDateTime());
    delta.addStampProperty("event", "location", dv.getLocation());
    var status = dv.getEventVisibility().status ? dv.getStatus() : null;
    delta.addStampProperty("event", "status", status);
    delta.addStampProperty("event", "allDay", dv.getAllDay());
    var rrule = dv.getRrule();
    if (!rrule || (rrule && rrule.isSupported())) delta.addStampProperty("event", "rrule", rrule);
    populateDeltaAnytimeAtTime(dv, delta);
}

function populateDeltaAnytimeAtTime(dv, delta){
    if (dv.getAllDay()){
        delta.addStampProperty("event", "anyTime", false);
    } else if (dv.getAnyTime()){
        delta.addStampProperty("event", "anyTime", true);
    } else if (!dv.endTimeInput.getValue() || dv.getStartDateTime().equals(dv.getEndDateTime())){
        //this is attime, so kill duration, end time
        delta.removeStampProperty("event", "endDate");
        delta.addStampProperty("event", "duration", new cosmo.model.Duration(cosmo.model.ZERO_DURATION));
        delta.addStampProperty("event", "anyTime", false);
    } else {
        delta.addStampProperty("event", "anyTime", false);
    }
}

function populateDeltaTriage(dv, delta){
    var triageStatus = dv.getTriage();
    if (triageStatus != dv.item.getTriageStatus()){
        delta.addProperty("triageStatus", triageStatus);
        delta.addProperty("autoTriage", false);
    }
}
})();


dojo.declare("cosmo.ui.widget.DetailView.StyledTextArea", [dijit.form.Textarea], {
    postCreate: function() {
        this.inherited('postCreate', arguments);
        if (dojo.isFF && dojo.isFF < 3){
            var w = this.iframe.contentWindow;
            if (!w || !w.document) return;
            w.document.body.style.fontFamily = '"Lucida Grande", "Verdana", "Arial", sans-serif';
            w.document.body.style.fontSize   = '11px';
        }
    }
});