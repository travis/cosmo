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

dojo.declare("cosmo.ui.DetailFormConverter", null, {
   _item: null,
   
   initializer: function(item){
       this._item = item;
   },
   
   createDelta: function(){
        var delta = new cosmo.model.Delta(this._item);
        var errorMessage = "";
        
        //get the form values for the "main" section
        errorMessage += this._populateDelta(delta, "note", true);
        for (var x = 0; x < cosmo.ui.detail.itemStamps.length; x++){
            var stampName = cosmo.ui.detail.itemStamps[x].stampType;
            var hasFields = cosmo.ui.detail.itemStamps[x].hasBody;
            errorMessage += this._populateDelta(delta, stampName.toLowerCase(),hasFields);
        }

        errorMessage += this._populateAnyTimeAtTime(delta);
        this._populateDeltaFromTriageWidget(delta);
        if (!errorMessage){
            errorMessage += this._performInterPropertyValidations(delta);
        }
        
        delta.deltafy(true);
        return [delta, errorMessage];  
    },
    
    getStampForm: function(stampName){
        //summary: returns the form object for the given stamp name
        stampName = stampName.toLowerCase();
        return cosmo.app.pim.baseLayout.mainApp.rightSidebar
                   .detailViewForm[stampName +"Section"].formSection.formNode;
    },
    
    getMainForm: function(){
        return cosmo.app.pim.baseLayout.mainApp.rightSidebar.detailViewForm.mainSection.formNode;
    },
    
    _populateDelta: function(delta, stampName, hasFields){
        var map =  this._stampPropertiesMaps[stampName];
        var errors = "";
        
        if (stampName != "note"){
            var enabled = this._isStampEnabled(stampName);
            if (!enabled){
                delta.addDeletedStamp(stampName);
                return errors;
            } else {
                delta.addAddedStamp(stampName);
            }
        }
        
        if (!hasFields){
            return errors;
        }

        var form = stampName == "note" ? this.getMainForm() : this.getStampForm(stampName);
        
        for (var propertyName in map){
            var propertyInfo = map[propertyName];
            var valueAndError = this[propertyInfo.type + "Converter"](form, propertyInfo, propertyName);
            var value = valueAndError[0];
            var error = valueAndError[1];
            if (!error && propertyInfo.validation){
                //we were able to convert the string from the form into a value, but
                //there are other validations to perform?
                var validationInfo = propertyInfo.validation;
                error = this._validateValue(value, validationInfo, propertyName);
            }
            if (!error){
                if (stampName == "note"){
                    delta.addProperty(propertyName, value);
                } else {
                    delta.addStampProperty(stampName, propertyName, value);
                }
            } else {
                errors += error;
            }
        }
        return errors;
    },
    
    _populateDeltaFromTriageWidget: function(delta){
        var formTriageStatus = parseInt(cosmo.app.pim.baseLayout.mainApp.rightSidebar.detailViewForm.markupBar.triageSection.currTriageStatus);
        if (formTriageStatus != this._item.getTriageStatus()){
            delta.addProperty("triageStatus", formTriageStatus);
            delta.addProperty("autoTriage", false);
        }
     },
    
    _isStampEnabled: function(stampName){
        var checkBox = $("section"+ this._upperFirstChar(stampName) +"EnableToggle");
        return checkBox.checked;
    },
            
    _getFormValue:  function(form, fieldName){
        var element = form[fieldName];
        var type = null;
        if (element.type){
            type = element.type
        } else if (element.length){
            type = element[0].type;
        }
        switch(type){
            case "text":
                return element.value;
                break;
            case "textarea":
                return element.value;
                break;
            case "radio":
                return cosmo.util.html.getRadioButtonSetValue(element);
                break;
            case "select-one":
                return cosmo.util.html.getSelectValue(element);
                break;
            case "checkbox":
                return element.checked ? "1" : "0";
                break;
            default: 
                alert(type);
                return "";
                break;
        }
    },
    
    _upperFirstChar: function(str){
        return str.charAt(0).toUpperCase() + str.substr(1,str.length -1 );
    },
    
    _stampPropertiesMaps: {
        note: {
            body: {type: "string",
                  field: "noteDescription"},

            displayName: { type : "string",
                           field: "noteTitle",
                      validation: [["required"]]
            }
        },
        
        event: {
            startDate: {
                type: "date",
                dateField: "startDate",
                timeField: "startTime",
                meridianField: "startMeridian",
                tzIdField: "tzId",
                allDayField: "eventAllDay"                
            },

            endDate: {
                type: "date",
                dateField: "endDate",
                timeField: "endTime",
                meridianField: "endMeridian",
                tzIdField: "tzId",
                allDayField: "eventAllDay"             
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
        var propertyDisplayName = _("Main.DetailForm." + propertyName);
        var dateFieldValue = this._getFormValue(form, info.dateField);
        var timeFieldValue = this._getFormValue(form, info.timeField);
        var meridianFieldValue = this._getFormValue(form, info.meridianField);
        var tzIdFieldValue = this._getFormValue(form, info.tzIdField);
        var allDayFieldValue = this._getFormValue(form, info.allDayField) == "1";
        
        var errMsg = ""
        
        if (timeFieldValue){
            var err = cosmo.util.validate.timeFormat(timeFieldValue);
            if (err){
                errMsg += '"'+propertyDisplayName+'" time field: ' + err;
                errMsg += '\n';
            }
        }

        if(!allDayFieldValue){  
            var err = cosmo.util.validate.required(meridianFieldValue);
            if (err){
                errMsg += '"'+propertyDisplayName+'" AM/PM field: ' + err;
                errMsg += '\n';
            }
        }
        
        var jsDate  = new Date(dateFieldValue);
        if (timeFieldValue) {
            var t = cosmo.datetime.util.parseTimeString(timeFieldValue);
            var h = cosmo.datetime.util.hrStd2Mil(t.hours, (meridianFieldValue == "pm"));
            var m = t.minutes;
            jsDate.setHours(h, m);
        }

        if (errMsg){
            return [null, errMsg];
        } 
        
        var date = new cosmo.datetime.Date();
        if (tzIdFieldValue && !allDayFieldValue){
            date.tzId = tzIdFieldValue;
            date.utc = false;
            date.updateFromLocalDate(jsDate);
        } else {
            date.tzId = null;
            date.utc = false;
            date.updateFromUTC(jsDate.getTime());            
        }
        date.utc = false;
        return [date, null];
    }, 
    
    requiredValidator: function(value, propertyName){
        var propertyDisplayName = _("Main.DetailForm." + propertyName);
        if (!value){
            return '"'+propertyDisplayName+'" is a required field.\n'
        }
        return "";
    },
    
    recurrenceRuleConverter: function(form, info, propertyName){
        var propertyDisplayName = _("Main.DetailForm." + propertyName);
        var frequencyFieldValue = this._getFormValue(form, info.frequencyField);
        var endDateFieldValue = this._getFormValue(form, info.endDateField);
        endDateFieldValue = endDateFieldValue == "mm/dd/yyyy" ? null : endDateFieldValue;
        
        var errMsg = "";
        
        if (!frequencyFieldValue){
            return [null, null];
        }
        
        var endDate = null;
        if (endDateFieldValue){
            var err = cosmo.util.validate.dateFormat(endDateFieldValue);
            if (err) {
                errMsg += '"'+propertyDisplayName+'" ending date field: ' + err;
                errMsg += '\n';
                return [null, errMsg];
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
        var errMsg = "";
        
        if(eventStampProperties){
            if(eventStampProperties.endDate 
                && (eventStampProperties.startDate.getTime() 
                > eventStampProperties.endDate.getTime())){
                errMsg += '"Starts" and "Ends" time fields: ';
                errMsg += 'Event cannot end before it starts.';
                errMsg += '\n';                
            }    
        }
        
        return errMsg;
    }, 
    _populateAnyTimeAtTime: function(delta){
        if (delta.getStampProperty("event", "allDay")){
               delta.addStampProperty("event", "anyTime", false) 
               return "";
        }
        
        var form = this.getStampForm("event");
        var startTimeFieldValue = this._getFormValue(form, "startTime");
        var endTimeFieldValue = this._getFormValue(form, "endTime");
        if (!startTimeFieldValue) {
           if (!endTimeFieldValue){
               delta.addStampProperty("event", "anyTime", true)
           } else {
               return _("App.Error.NoEndTimeWithoutStartTime");
           }
        } else {
           if (!endTimeFieldValue){
               //this is attime, so kill duration, end time
               delta.removeStampProperty("event", "endDate");
               delta.addStampProperty("event", "duration", null);
               delta.addStampProperty("event", "anyTime", false) //just in case.
           } else {
               delta.addStampProperty("event", "anyTime", false) //just in case.
           }
        }
        return "";
    }
    
});