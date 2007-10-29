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

dojo.provide("cosmo.service.translators.eim.deserialize");

dojo.require("cosmo.service.translators.eim.common");
dojo.require("cosmo.service.translators.eim.constants");
dojo.require("cosmo.model.*");
dojo.require("cosmo.datetime.*");

cosmo.service.translators.eim.deserialize = {

    recordSetToObject: function (/*Object*/ recordSet, kwArgs){
        kwArgs = kwArgs || {};
        //TODO
        /* We can probably optimize this by grabbing the
         * appropriate properties from the appropriate records
         * and passing them into the constructor. This will probably
         * be a little less elegant, and will require the creation of
         * more local variables, so we should play with this later.
         */
        var note = kwArgs.oldObject || new cosmo.model.Note(
            {
                uid: recordSet.uuid
            }
        );
        for (recordName in recordSet.records){
        with (cosmo.service.translators.eim.constants){

            var record = recordSet.records[recordName];

            switch(recordName){
                
            case prefix.ITEM:
                note.initializeProperties(this.itemRecordToItemProps(record), {noDefaults: true})
                break;
            case prefix.NOTE:
                note.initializeProperties(this.noteRecordToNoteProps(record), {noDefaults: true})
                break;
            case prefix.MODBY:
                note.setModifiedBy(new cosmo.model.ModifiedBy(this.modbyRecordToModbyProps(record)));
                break;
            case prefix.EVENT:
                note.getStamp(prefix.EVENT, true, this.getEventStampProperties(record));
                break;
            case prefix.TASK:
                note.getStamp(prefix.TASK, true, this.getTaskStampProperties(record));
                break;
            case prefix.MAIL:
                note.getStamp(prefix.MAIL, true, this.getMailStampProperties(record));
                break;
            }
        }
            
        }
        return note;

    },

    recordSetToModification: function (recordSet, masterItem, kwArgs){
        kwArgs = kwArgs || {};
        var uidParts = recordSet.uuid.split(":");
        
        var modifiedProperties = {};
        var modifiedStamps = {};
        var deletedStamps = {};
        for (stampName in masterItem._stamps){
            deletedStamps[stampName] = true;
        }

        for (recordName in recordSet.records){
            deletedStamps[recordName] = false;
            with (cosmo.service.translators.eim.constants){
               var record = recordSet.records[recordName];
                
               switch(recordName){
    
               case prefix.ITEM:
                   dojo.lang.mixin(modifiedProperties, this.itemRecordToItemProps(record));
                   break;
               case prefix.NOTE:
                    dojo.lang.mixin(modifiedProperties, this.noteRecordToNoteProps(record));
                   break;
               case prefix.MODBY:
                    modifiedProperties.modifiedBy = new cosmo.model.ModifiedBy(this.modbyRecordToModbyProps(record));
                   break;
               case prefix.EVENT:
                   modifiedStamps[prefix.EVENT] = this.getEventStampProperties(record);
                   break;
               case prefix.TASK:
                   modifiedStamps[prefix.TASK] = this.getTaskStampProperties(record);
                   break;
               case prefix.MAIL:
                   modifiedStamps[prefix.MAIL] = this.getMailStampProperties(record);
                   break;
               }
            }
        }
        var recurrenceId = this.recurrenceIdToDate(uidParts[1], masterItem.getEventStamp().getStartDate());
        
        if (!dojo.lang.isEmpty(modifiedProperties)
            || !dojo.lang.isEmpty(modifiedStamps)){
            
            var mod = new cosmo.model.Modification(
                {
                    "recurrenceId": recurrenceId,
                    "modifiedProperties": modifiedProperties,
                    "modifiedStamps": modifiedStamps,
                    "deletedStamps": deletedStamps
                }
            );
            masterItem.addModification(mod);
        }
        
        return kwArgs.oldObject || masterItem.getNoteOccurrence(recurrenceId);
    },

    itemRecordToItemProps: function(record){
        var props = {};
        if (record.fields){
            if (record.fields.title) props.displayName = record.fields.title[1];
            if (record.fields.createdOn) props.creationDate = record.fields.createdOn[1]*1000;
            if (record.fields.triage) this.addTriageStringToItemProps(record.fields.triage[1], props);
        }
        return props;
    },

    noteRecordToNoteProps: function(record){
        var props = {};
        if (record.fields){
            if (record.fields.body) props.body = record.fields.body[1];
            if (record.fields.icalUid) props.icalUid = record.fields.icalUid[1];
        }
        return props;
    },

    modbyRecordToModbyProps: function(record){
        var props = {};
        if (record.key){
            if (record.key.userid) props.userId = record.key.userid[1];
            if (record.key.timestamp) props.timeStamp = record.key.timestamp[1] * 1000;
            if (record.key.action) props.action = record.key.action[1];
        }
        return props;
    },

    getEventStampProperties: function (record){

        var properties = {};
        if (record.fields){
            if (record.fields.dtstart){
                properties.startDate = this.fromEimDate(record.fields.dtstart[1]);
                var dateParams = this.dateParamsFromEimDate(record.fields.dtstart[1]);
                if (dateParams.anyTime !== undefined) properties.anyTime = dateParams.anyTime;
                if (dateParams.allDay !== undefined) properties.allDay = dateParams.allDay;
                // Only one of these properties can be true, and we
                // need to do this to ensure modifications don't inherit
                // their parents' anyTime.
                if (properties.anyTime) properties.allDay = false;
                if (properties.allDay) properties.anyTime = false;
                if (record.fields.lastPastOccurrence &&
                    record.fields.lastPastOccurrence[1]) {
                    properties.lastPastOccurrence = this.fromEimDate(record.fields.lastPastOccurrence[1]);
                    properties.lastPastOccurrence.tzId = properties.startDate.tzId;
                }
            }
            if (record.fields.duration) properties.duration =
                    new cosmo.model.Duration(record.fields.duration[1]);
            if (record.fields.location) properties.location = record.fields.location[1];
            if (record.fields.rrule) properties.rrule = this.parseRRule(record.fields.rrule[1], properties.startDate);
            if (record.fields.exrule) properties.exrule = this.parseRRule(record.fields.exrule[1]), properties.startDate;
            if (record.fields.exdate) properties.exdates = this.parseExdate(record.fields.exdate[1]);
            if (record.fields.status) properties.status = record.fields.status[1];
        }
        return properties;

    },

    getTaskStampProperties: function (record){
        return {};
    },
 
    getMailStampProperties: function (record){
        var properties = {};
        if (record.fields){
            if (record.fields.messageId) properties.messageId = record.fields.messageId[1];
            if (record.fields.headers) properties.headers = record.fields.headers[1];
            if (record.fields.fromAddress) properties.fromAddress = record.fields.fromAddress[1];
            if (record.fields.toAddress) properties.toAddress = record.fields.toAddress[1];
            if (record.fields.ccAddress) properties.ccAddress = record.fields.ccAddress[1];
            if (record.fields.bccAddress) properties.bccAddress = record.fields.bccAddress[1];
            if (record.fields.originators) properties.originators = record.fields.originators[1]
            if (record.fields.dateSent) properties.dateSent = record.fields.dateSent[1]; //TODO: parse
            if (record.fields.inReplyTo) properties.inReplyTo = record.fields.inReplyTo[1];
            if (record.fields.references) properties.references = record.fields.references[1];
        }
        return properties;
    },
    
    addTriageStringToItemProps: function (triageString, props){
        if (dojo.string.trim(triageString) == "" || triageString == null){
            props.autoTriage = true;
            props.triageStatus = null;
            return;
        }
        var triageArray = triageString.split(" ");

        props.triageStatus = parseInt(triageArray[0]);

        props.rank = triageArray[1];

        /* This looks weird, but because of JS's weird casting stuff, it's necessary.
         * Try it if you don't believe me :) - travis@osafoundation.org
         */
        props.autoTriage = triageArray[2] == true;
    },

    parseRRule: function (rule, startDate){
        if (!rule) {
            return null;
        }
        return this.rPropsToRRule(this.parseRRuleToHash(rule), startDate);
    },
    
    parseExdate: function (exdate){
        if (!exdate) return null;
        return dojo.lang.map(
                exdate.split(":")[1].split(","),
                function (exdate, index) {return cosmo.datetime.fromIso8601(exdate)}
         );
    },

    //Snagged from dojo.cal.iCalendar
    parseRRuleToHash: function (rule){
        var rrule = {}
        var temp = rule.split(";");
        for (var y=0; y<temp.length; y++) {
            if (temp[y] != ""){
                var pair = temp[y].split("=");
                var key = pair[0].toLowerCase();
                var val = pair[1];
                if ((key == "freq") || (key=="interval") || (key=="until")) {
                    rrule[key]= val;
                } else {
                    var valArray = val.split(",");
                    rrule[key] = valArray;
                }
            }
        }
        return rrule;
    },

    fromEimDate: function (dateString){
        var dateParts = dateString.split(":");
        var dateParamList = dateParts[0].split(";");
        var dateParams = {};
        for (var i = 0; i < dateParamList.length; i++){
            var keyValue = dateParamList[i].split("=");
            dateParams[keyValue[0].toLowerCase()] = keyValue[1];
        }
        var tzId = dateParams['tzid'] || null;
        var jsDate = dojo.date.fromIso8601(dateParts[1]);
        var date = new cosmo.datetime.Date();
        date.tzId = tzId;

        date.setYear(jsDate.getFullYear());
        date.setMonth(jsDate.getMonth());
        date.setDate(jsDate.getDate());
        date.setHours(jsDate.getHours());
        date.setMinutes(jsDate.getMinutes());
        date.setSeconds(jsDate.getSeconds());
        date.setMilliseconds(0);
        return date;
    },

    dateParamsFromEimDate: function (dateString){
        var returnVal = {};
        var params = dateString.split(":")[0].split(";");
        for (var i = 0; i < params.length; i++){
            var param = params[i].split("=");
            if (param[0].toLowerCase() == "x-osaf-anytime") {
                returnVal.anyTime = true;
            }
            if (param[0].toLowerCase() == "value") {
                returnVal.value = param[1].toLowerCase();
            }
        }
        
        if ((returnVal.value == "date") && !returnVal.anyTime) returnVal.allDay = true;
        return returnVal;
    },
    
    rPropsToRRule: function (rprops, startDate){
        if (this.isRRuleUnsupported(rprops)) {
            // TODO set something more readable?
            return new cosmo.model.RecurrenceRule({
                isSupported: false,
                unsupportedRule: rprops
            });
        } else {
            var RecurrenceRule = cosmo.model.RRULE_FREQUENCIES;
            var Recur = cosmo.service.translators.eim.constants.rrule;
            var recurrenceRule = {}
            // Set frequency
            if (rprops.freq == Recur.WEEKLY) {
                if (rprops.interval == 1 || !rprops.interval){
                    recurrenceRule.frequency = RecurrenceRule.FREQUENCY_WEEKLY;
                }
                else if (rprops.interval == 2){
                    recurrenceRule.frequency = RecurrenceRule.FREQUENCY_BIWEEKLY;
                }
            }
            else if (rprops.freq == Recur.MONTHLY) {
                recurrenceRule.frequency = RecurrenceRule.FREQUENCY_MONTHLY;
            }
            else if (rprops.freq == Recur.DAILY) {
                recurrenceRule.frequency = RecurrenceRule.FREQUENCY_DAILY;
            }
            else if (rprops.freq == Recur.YEARLY) {
                recurrenceRule.frequency = RecurrenceRule.FREQUENCY_YEARLY;
            }

            // Set until date
            if (rprops.until) {
                var endDate = cosmo.datetime.fromIso8601(rprops.until);
                var tzId = startDate.tzId || (startDate.utc ? "utc" : null);
                endDate = endDate.createDateForTimezone(tzId);
                endDate.setHours(0);
                endDate.setMinutes(0);
                endDate.setSeconds(0);
                recurrenceRule.endDate = endDate;
            }
            
            recurrenceRule = new cosmo.model.RecurrenceRule(recurrenceRule);
            

            return recurrenceRule;
        }

    },

    isRRuleUnsupported: function (recur){

        with (cosmo.service.translators.eim.constants.rrule){

        if (recur.freq == SECONDLY
                || recur.freq == MINUTELY) {
            return true;
        }
        //If they specified a count, it's custom
        if (recur.count != undefined){
            return true;
        }

        if (recur.byyearday){
            return true;
        }

        if (recur.bymonthday){
            return true;
        }

        if (recur.bymonth){
            return true;
        }

        if (recur.byweekno){
            return true;
        }

        if (recur.byday){
            return true;
        }

        if (recur.byhour){
            return true;
        }

        if (recur.byminute){
            return true;
        }

        if (recur.bysecond){
            return true;
        }

        var interval = parseInt(recur.interval);

        //We don't support any interval except for "1" or none (-1)
        //with the exception of "2" for weekly events, in other words bi-weekly.
        if (!isNaN(interval) && interval != 1 ){

            //if this is not a weekly event, it's custom.
            if (recur.freq != WEEKLY){
               return true;
            }

            //so it IS A weekly event, but the value is not "2", so it's custom
            if (interval != 2){
                return true;
            }
        }
        }
        return false;
    }
    
};
