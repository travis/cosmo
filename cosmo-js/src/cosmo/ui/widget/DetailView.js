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
dojo.require("cosmo.datetime.timezone");
dojo.require("cosmo.util.html");

dojo.requireLocalization("cosmo.ui.widget", "DetailView");

dojo.declare("cosmo.ui.widget.DetailView", [dijit._Widget, dijit._Templated], {
    templatePath: dojo.moduleUrl("cosmo", "ui/widget/templates/DetailView.html"),
    widgetsInTemplate: true,

    initItem: null,

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
    timezoneCitySelector: null,
    statusSelector: null,
    recurrenceSelector: null,
    untilInput: null,
    byline: null,
    removeButton: null,
    saveButton: null,

    //fields
    hasEvent: false,
    item: null,
    triage: null,
    starred: null,

    updateFromItem: function(item){
        this.titleInput.setValue(item.getDisplayName());
        this.notesInput.setValue(item.getBody());
        this.setTriageStatus(item.getTriageStatus());
        var eventStamp = item.getEventStamp();
        this.clearEventFields();
        if (eventStamp){
            this.enableEvent();
            this.updateFromEventStamp(eventStamp);
        } else {
            this.disableEvent();
        }
        if(item.getTaskStamp()) this.setStarred();
        else this.setUnstarred();
        this.item = item;
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
        if (stamp.getAllDay()){
            this.startTimeInput.setAttribute("disabled", true);
            this.endTimeInput.setAttribute("disabled", true);
        } else {
            this.startTimeInput.setValue(startDate);
            this.endTimeInput.setValue(endDate);
        }
        if (startDate.tzId){
            this.updateFromTimezone(cosmo.datetime.timezone.getTimezone(startDate.tzId));
        } else {
            this.clearTimezoneSelectors();
        }

        this.updateAllDay(stamp.getAllDay());
        this.statusSelector.value = stamp.getStatus();
        this.updateFromRrule(stamp.getRrule());
    },

    updateAllDay: function(allDay){
        this.allDayInput.setValue(allDay);
        if (allDay){
            this.timezoneRegionSelector.setAttribute("disabled", true);
            this.timezoneCitySelector.setAttribute("disabled", true);
            this.startTimeInput.setAttribute("disabled", true);
            this.endTimeInput.setAttribute("disabled", true);
        } else {
            this.timezoneRegionSelector.disabled = false;
            this.startTimeInput.setAttribute("disabled", false);
            this.endTimeInput.setAttribute("disabled", false);
        }
    },

    updateFromTimezone: function(tz){
        if (tz){
            var tzId = tz.tzId;
            var region = tzId.split("/")[0];
            this.updateFromTimezoneRegion(region);
            cosmo.util.html.setSelect(this.timezoneCitySelector, tzId);
        } else {
            this.clearTimezoneSelectors();
        }
    },

    updateFromTimezoneRegion: function(region){
        if (region){
            cosmo.util.html.setSelect(this.timezoneRegionSelector, region);
            cosmo.util.html.setSelectOptions(this.timezoneCitySelector, this.getTimezoneIdOptions(region));
            this.timezoneCitySelector.disabled = false;
        } else {
            this.clearTimezoneSelectors();
        }
    },

    clearTimezoneSelectors: function(){
        this.timezoneRegionSelector.value = "";
        this.timezoneCitySelector.value = "";
        this.timezoneCitySelector.setAttribute("disabled", true);
    },

    getTimezoneIdOptions: function(region){
        return [{text: this.l10n.noTzCity,
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
            var endDate = rrule.getEndDate();
            if (endDate) this.untilInput.setValue(endDate);
        } else {
            this.untilInput.setAttribute("disabled", true);
            this.recurrenceSelector.value = 'once';
        }
    },

    clearEventFields: function(){
        this.locationInput.setValue("");
        this.allDayInput.setValue("");
        this.startDateInput.setValue("");
        this.startTimeInput.setValue("");
        this.endDateInput.setValue("");
        this.endTimeInput.setValue("");
        this.timezoneRegionSelector.value = "";
        this.timezoneCitySelector.value = "";
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
        this.timezoneCitySelector.setAttribute("disabled", true);
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
        this.timezoneCitySelector.disabled = false;
        this.statusSelector.disabled = false;
        this.recurrenceSelector.disabled = false;
        this.untilInput.setAttribute("disabled", false);
    },

    toggleEvent: function(){
        this.hasEvent? this.disableEvent() : this.enableEvent();
    },

    enableEvent: function(){
        this.hasEvent = true;
        this.eventTitleSpan.innerHTML = this.l10n.removeFromCalendar;
        dojo.addClass(this.eventButton, "cosmoEventButtonSelected");
        dojo.fx.wipeIn({node: this.eventSection}).play();
        this.enableEventFields();
    },

    disableEvent: function(){
        this.hasEvent = false;
        this.eventTitleSpan.innerHTML = this.l10n.addToCalendar;
        dojo.removeClass(this.eventButton, "cosmoEventButtonSelected");
        dojo.fx.wipeOut({node: this.eventSection}).play();
        this.disableEventFields();
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
    },

    allDayOnChange: function(value){
        this.updateAllDay(value);
    },

    eventButtonOnClick: function(){
        this.toggleEvent();
    },

    mailMouseDown: function(){
        dojo.addClass(this.mailButton, "cosmoEmailButtonSelected");
        this.itemToEmail(this.item);
    },

    mailMouseUp: function(){
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

    // lifecycle functions
    constructor: function(){
        this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "DetailView");

    },

    postCreate: function(){
        if (this.initItem) this.updateFromItem(this.initItem);
    }
});