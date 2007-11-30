/*
 * Copyright 2007 Open Source Applications Foundation
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

dojo.provide("cosmo.ui.DetailFormConverter");
dojo.require("cosmo.model.Delta");
dojo.require("cosmo.model.exception");

dojo.declare("cosmo.ui.DetailFormConverter", null, {
   _item: null,

   initializer: function(item){
       this._item = item;
       this._errors = new cosmo.ui.ErrorList();
   },

   createDelta: function(){
      if (cosmo.ui.detail.item.data.getItemUid() != this._item.getItemUid()){
          throw new cosmo.model.exception.DetailItemNotDeltaItemException(
          "Item in detail view doesn't match item in Delta.")
      }

        var delta = new cosmo.model.Delta(this._item);

        //get the form values for the "main" section
        this._populateDelta(delta, "note", true);
        for (var x = 0; x < cosmo.ui.detail.itemStamps.length; x++){
            var stampName = cosmo.ui.detail.itemStamps[x].stampType;
            var hasFields = cosmo.ui.detail.itemStamps[x].hasBody;
            this._populateDelta(delta, stampName.toLowerCase(),hasFields);
        }

        if (cosmo.ui.detail.isStampEnabled("event")){
            this._populateAnyTimeAtTime(delta);
        }

        this._populateDeltaFromTriageWidget(delta);
        if (this._errors.isEmpty()){
            this._performInterPropertyValidations(delta);
        }

        this._removeRecurrenceChangeIfUnsupported(delta);
        delta.deltafy(true);
        return [delta, this._errors.toString()];
    },

    _populateDelta: function(delta, stampName, hasFields){
        var map =  this._stampPropertiesMaps[stampName];

        if (stampName != "note"){
            var enabled = cosmo.ui.detail.isStampEnabled(stampName);
            if (!enabled){
                delta.addDeletedStamp(stampName);
                return;
            } else {
                delta.addAddedStamp(stampName);
            }
        }

        if (!hasFields){
            return;
        }

        var form = stampName == "note" ? cosmo.ui.detail.getMainForm()
                                       : cosmo.ui.detail.getStampForm(stampName);

        for (var propertyName in map){
            var propertyInfo = map[propertyName];
            var valueAndErrors = this[propertyInfo.type + "Converter"](form, propertyInfo, propertyName);
            var value = valueAndErrors[0];
            var errors = valueAndErrors[1] || [];
            if (!errors.length && propertyInfo.validation){
                //we were able to convert the string from the form into a value, but
                //there are other validations to perform?
                var validationInfo = propertyInfo.validation;
                var err = this._validateValue(value, validationInfo, propertyName);
                if (err) {
                    errors.push(err);
                }
            }
            if (!errors.length){
                if (stampName == "note"){
                    delta.addProperty(propertyName, value);
                } else {
                    delta.addStampProperty(stampName, propertyName, value);
                }
            } else {
                this._errors.addErrors(errors);
            }
        }
    },

    _populateDeltaFromTriageWidget: function(delta){
        var formTriageStatus = parseInt(cosmo.app.pim.baseLayout.mainApp.rightSidebar.detailViewForm.markupBar.triageSection.currTriageStatus);
        if (formTriageStatus != this._item.getTriageStatus()){
            delta.addProperty("triageStatus", formTriageStatus);
            delta.addProperty("autoTriage", false);
        }
     },

     _getFormValue: cosmo.util.html.getFormValue,

    _stampPropertiesMaps: {
        note: {
            body: {type: "string",
                  field: "noteDescription"},

            displayName: { type : "string",
                           field: "noteTitle"
            }
        },

        event: {
            startDate: {
                type: "date",
                dateField: "startDate",
                timeField: "startTime",
                meridianField: "startMeridian",
                tzIdField: "tzId",
                tzRegionField: "tzRegion",
                allDayField: "eventAllDay",
                validation: [["required"]]
            },

            endDate: {
                type: "date",
                dateField: "endDate",
                timeField: "endTime",
                meridianField: "endMeridian",
                tzIdField: "tzId",
                tzRegionField: "tzRegion",
                allDayField: "eventAllDay",
                validation: [["required"]]
            },

            "location": {
                type: "string",
                field: "eventLocation"},

            status: {
                type: "string",
                field: "eventStatus"
            },

            allDay: {
                type: "boolean",
                field: "eventAllDay"
            },

            rrule: {
                type: "recurrenceRule",
                frequencyField: "recurrenceInterval",
                endDateField: "recurrenceEnd",
                tzIdField: "tzId"
            }
        },

        mail: {
            fromAddress: {
                  type: "string",
                  field: "mailFrom"},

            toAddress: {
                  type: "string",
                  field: "mailTo"},

            ccAddress: {
                  type: "string",
                  field: "mailCc"},

            bccAddress: {
                  type: "string",
                  field: "mailBcc"}
        },

        task: {
        }
    },

    stringConverter: function(form, info){
        return [this._getFormValue(form, info.field),null]
     },

     booleanConverter: function(form, info){
         return [this._getFormValue(form, info.field) == "1", null]
     },

    dateConverter: function(form, info, propertyName){
        //this code adapted from mde's original cal_form code.
        var dateFieldValue = this._getFormValue(form, info.dateField);
        dateFieldValue = dateFieldValue == "mm/dd/yyyy" ? null : dateFieldValue;
        var timeFieldValue = this._getFormValue(form, info.timeField);
        timeFieldValue = timeFieldValue == "hh:mm" ? null : timeFieldValue;
        var meridianFieldValue = this._getFormValue(form, info.meridianField);
        var tzIdFieldValue = this._getFormValue(form, info.tzIdField);
        var tzRegionFieldValue = this._getFormValue(form, info.tzRegionField);
        var allDayFieldValue = this._getFormValue(form, info.allDayField) == "1";
        var errors = [];

        if (!dateFieldValue){
            return [null,null];
        }

        if (tzRegionFieldValue && !tzIdFieldValue){
            errors.push(new cosmo.ui.Error(null, "App.Error.NoTzId"));
        }

        var err = cosmo.util.validate.dateFormat(dateFieldValue);
        if (err){
            errors.push(new cosmo.ui.Error(propertyName, null, err));
        } else {
            var jsDate  = new Date(dateFieldValue);
        }

        if (timeFieldValue){
            var err = cosmo.util.validate.timeFormat(timeFieldValue);
            if (err){
                errors.push(new cosmo.ui.Error(propertyName, null, err));
            }

            var err = cosmo.util.validate.required(meridianFieldValue);
            if (err){
                errors.push(new cosmo.ui.Error(propertyName, null, err));
            }
            var t = cosmo.datetime.util.parseTimeString(timeFieldValue);
            var h = cosmo.datetime.util.hrStd2Mil(t.hours, (meridianFieldValue == "pm"));
            var m = t.minutes;

            if (!errors.length) {
                jsDate.setHours(h, m);
            }
        }

        if (errors.length){
            return [null, errors];
        }

        var dt = new cosmo.datetime.Date();
        if (tzIdFieldValue && timeFieldValue){
            dt.tzId = tzIdFieldValue;
            dt.utc = false;
            dt.updateFromLocalDate(jsDate);
        } else {
            dt.tzId = null;
            dt.utc = false;
            dt.updateFromUTC(jsDate.getTime());
        }
        dt.utc = false;
        return [dt, null];
    },

    requiredValidator: function(value, propertyName){
        if (!value){
            var propertyDisplayName = _("Main.DetailForm." + propertyName);
            return new cosmo.ui.Error(null,
                                      null,
                                      '"' + propertyDisplayName +'" is a required field.');

        }
        return null;
    },

    recurrenceRuleConverter: function(form, info, propertyName){
        var propertyDisplayName = _("Main.DetailForm." + propertyName);
        var frequencyFieldValue = this._getFormValue(form, info.frequencyField);
        var endDateFieldValue = this._getFormValue(form, info.endDateField);
        endDateFieldValue = endDateFieldValue == "mm/dd/yyyy" ? null : endDateFieldValue;

        var errors = [];

        if (!frequencyFieldValue){
            return [null, null];
        }

        var endDate = null;
        if (endDateFieldValue){
            var err = cosmo.util.validate.dateFormat(endDateFieldValue);

            if (err) {
                return [null, [new cosmo.ui.Error(propertyName, null, "ending date field: " + err)]];
            }

            var jsDate= new Date(endDateFieldValue);
            endDate = new cosmo.datetime.Date(jsDate.getFullYear(), jsDate.getMonth(), jsDate.getDate());
            var tzIdFieldValue = this._getFormValue(form, info.tzIdField);
            if (tzIdFieldValue){
                endDate.tzId = tzIdFieldValue;
            }
        }

        return [new cosmo.model.RecurrenceRule({
            frequency: frequencyFieldValue,
            endDate: endDate
        }),null];

    },

    _validateValue: function(value, validationInfo, propertyName){
        return this[validationInfo[0]+"Validator"](value, propertyName, validationInfo[1]);
    },

    _performInterPropertyValidations: function (delta){
        var eventStampProperties = delta.getStampProperties("event");

        if(eventStampProperties){
            if(eventStampProperties.endDate
                && (eventStampProperties.startDate.getTime()
                > eventStampProperties.endDate.getTime())){

                this._errors.addError(new cosmo.ui.Error(null, null,
                                                         '"Starts" and "Ends" time fields: '
                                                         + 'Event cannot end before it starts.'));
                return;

            }
            var rrule = eventStampProperties.rrule;
            if (rrule && rrule.getEndDate()){
                if (rrule.getEndDate()
                    .before(delta.getNote().getMaster()
                            .getEventStamp().getStartDate())){
                    this._errors.addError(new cosmo.ui.Error(null, "App.Error.RecurEnd"));
                    return;
                }
            }
        }
    },

    _populateAnyTimeAtTime: function(delta){
        if (delta.getStampProperty("event", "allDay")){
               delta.addStampProperty("event", "anyTime", false)
               return "";
        }

        var form = cosmo.ui.detail.getStampForm("event");
        var startTimeFieldValue = this._getFormValue(form, "startTime");
        startTimeFieldValue = startTimeFieldValue == "hh:mm" ? null : startTimeFieldValue;
        var endTimeFieldValue = this._getFormValue(form, "endTime");
        endTimeFieldValue = endTimeFieldValue == "hh:mm" ? null : endTimeFieldValue;
        if (!startTimeFieldValue) {
           if (!endTimeFieldValue){
               delta.addStampProperty("event", "anyTime", true)
               delta.addStampProperty("event", "status", null);
           } else {
               this._errors.addError(new cosmo.ui.Error(null,"App.Error.NoEndTimeWithoutStartTime"));
           }
        } else {
           var deltaEndDate = delta.getStampProperty("event", "endDate");
           var deltaStartDate = delta.getStampProperty("event", "startDate");
           if (!endTimeFieldValue || (deltaStartDate && deltaStartDate.equals(deltaEndDate))){
               //this is attime, so kill duration, end time
               delta.removeStampProperty("event", "endDate");
               delta.addStampProperty("event", "duration", new cosmo.model.Duration(cosmo.model.ZERO_DURATION));
               delta.addStampProperty("event", "anyTime", false) //just in case.
               delta.addStampProperty("event", "status", null);
           } else {
               delta.addStampProperty("event", "anyTime", false) //just in case.
           }
        }
        return "";

    },
    _removeRecurrenceChangeIfUnsupported: function(delta){
        if (this._item.hasRecurrence() && delta.getStampProperty("event", "rrule") == null){
            var es = this._item.getEventStamp();
            if (!es.getRrule().isSupported()){
                delta.removeStampProperty("event", "rrule");
            }
        }
    }
});

//summary: an error
// param property: the name of the property with the error.
// param errorKey: the key to use when looking up the error in the i18n file
// param errorMessage: the localized error message
// param params: any parameters to use for variable replacement in i18n strings
//
// note that "errorKey" and "errorMessage" are mutually exclusive - but having at least
// one of them is required. All other properties are optional.
dojo.declare("cosmo.ui.Error", null, {
    initializer: function(property, errorKey, errorMessage, params) {
        this.property = property;
        this.errorKey = errorKey;
        this.errorMessage = errorMessage;
        this.params = params;
    },

    //summary: this exists so that we can use the error objects as keys
    //         in a hash
    toString: function(){
        var s = this.property + ";" + this.errorKey + ";"+this.errorMessage+";";
        dojo.lang.map(params, function(param){s += param + ";" });
        return s;
    }
});

dojo.declare("cosmo.ui.ErrorList", null, {
    _errorsList: null,
    _errorsMap: null,

    initializer: function(){
        this._errorsList  = [];
        this._errorsMap = {};
    },

    //summary: adds the given error object to the list
    //param: error: a cosmo.ui.Error
    addError: function(error){
        if (!error) {
            return;
        }
        if (this._errorsMap[error]){
            return;
        }

        this._errorsMap[error] = true;
        this._errorsList.push(error);
    },

    addErrors: function(/*Array*/ errors){
        var self = this;
        dojo.lang.map(errors, function(error){ self.addError(error)});
    },

    isEmpty: function(){
        return this._errorsList.length == 0;
    },

    toString: function() {
        var s = "";
        var self = this;
         dojo.lang.map(this._errorsList, function(error) {
            var property = error.property
                ? self._getPropertyDisplayName(error.property)
                : null;
            var errorMessage = error.errorMessage
                || _.apply(null, dojo.lang.unnest(error.errorKey, error.params));
            var message = property
                 ?  ("'" + property + "': "
                     + " " + errorMessage)
                 : errorMessage;

            s += message + "<br>";
        });
        return s;
    },

    _getPropertyDisplayName: function(propertyName){
        return _("Main.DetailForm." + propertyName);
    }


})
