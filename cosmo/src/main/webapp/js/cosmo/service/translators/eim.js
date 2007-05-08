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

/**
 * A module that provides translators from data received from a
 * JSON-RPC service to cosmo.model.Object objects.
 */
dojo.provide("cosmo.service.translators.eim");

dojo.require("dojo.date.serialize");
dojo.require("dojo.lang.*");

dojo.require("cosmo.service.eim");
dojo.require("cosmo.model.common");
dojo.require("cosmo.service.translators.common");
dojo.require("cosmo.datetime.serialize");

dojo.declare("cosmo.service.translators.Eim", null, {

    responseToObject: function atomPlusEimResponseToObject(atomXml){
        if (!atomXml){
            throw new cosmo.service.translators.ParseError("Cannot parse null, undefined, or false");
        }
        var entries = atomXml.getElementsByTagName("entry");
        var items = {};
        var mods = [];
        for (var i = 0; i < entries.length; i++){
            var entry = entries[i];
            try {
                var uuid = entry.getElementsByTagName("id")[0];
            } catch (e){
                throw new cosmo.service.translators.
                   ParseError("Could not find id element for entry " + (i+1));
            }
            uuid = uuid.firstChild.nodeValue;
            var uuidParts = uuid.split("::");
            var uidParts = uuidParts[0].split(":");
            try {
                var c = entry.getElementsByTagName("content")[0];
            } catch (e){
                throw new cosmo.service.translators.
                   ParseError("Could not find content element for entry " + (i+1));
            }
            var content = c.firstChild.nodeValue;

            // If we have a second part to the uid, this entry is a
            // recurrence modification.
            if (!uuidParts[1]){
                var item = this.recordSetToObject(eval("(" + content + ")"))
            } else {
                mods.push(content);
            }

            var links;
            // Safari doesn't find paging links with non NS call
            if (dojo.render.html.safari){
               links = entries[i].getElementsByTagNameNS('*', "link");
            } else {
               links = entries[i].getElementsByTagName("link");
            }

            for (var j = 0; j < links.length; j++){
                var link = links[j];
                if (link.getAttribute('rel') == 'edit'){
                    item.editLink = link.getAttribute('href');
                };
            }

            items[uidParts[2]] = item;
        }

        for (var i = 0; i < mods.length; i++){
            this.addModificationRecordSet(items, eval("(" + mods[i] + ")"));
        }

        return items;
    },

    objectToAtomEntry: function(object){

         var jsonObject = this.objectToRecordSet(object);

         return '<entry xmlns="http://www.w3.org/2005/Atom">' +
         '<title>' + object.data.title + '</title>' +
         '<id>urn:uuid:' + object.data.id + '</id>' +
         '<updated>' + dojo.date.toRfc3339(new Date()) + '</updated>' +
         '<author><name>' + cosmo.util.auth.getUsername() + '</name></author>' +
         '<content type="application/eim+json">' + dojo.json.serialize(jsonObject) + '</content>' +
         '</entry>'
    },

    objectToRecordSet: function(obj){

        var object = obj.data;
        with (cosmo.service.eim.constants){
            return {
                uuid: object.id,
                records: {
                    item: this.objectToItemRecord(object),
                    note: this.objectToNoteRecord(object),
                    event: this.objectToEventRecord(object),
                    modby: this.objectToModbyRecord(object)
                }
            }
        }
    },

    noteToItemRecord: function(note){

        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.ITEM,
                ns: ns.ITEM,
                keys: {
                    uuid: [type.TEXT, note.getUid()]
                },
                fields: {
                    title: [type.TEXT, note.getDisplayName() || ""]
                }
            }
        }
    },

    noteToNoteRecord: function(note){

        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.NOTE,
                ns: ns.NOTE,
                keys: {
                    uuid: [type.TEXT, note.getUid()]
                },
                fields: {

                    body: [type.CLOB, note.getBody() || ""]
                }
            }
        }
    },

    noteToEventRecord: function(note){

        var stamp = note.getEventStamp();

        with (cosmo.service.eim.constants){
            var fields = {
               dtstart: [type.TEXT, ";VALUE=" +
                        (object.allDay || object.anyTime? "DATE" : "DATE-TIME") +
                        (object.anyTime? ";X-OSAF-ANYTIME=TRUE" : "") +
                        ":" +
                        (object.allDay || stamp.getAnytime()?
                         object.start.strftime("%Y%m%d"):
                         object.start.strftime("%Y%m%dT%H%M%S"))],

                anytime: [type.INTEGER, stamp.getAnytime()],
                rrule: [type.TEXT, stamp.getRrule()? stamp.getRrule().toString() : null],
                status: [type.TEXT, stamp.getStatus() || null],
                location: [type.TEXT, stamp.getLocation() || null]

            };
            if (!(object.allDay || object.anyTime)){
                fields.duration =
                    [type.TEXT,
                     cosmo.datetime.getIso8601Duration(
                         object.start, object.end)
                    ]
            }


            return {
                prefix: prefix.EVENT,
                ns: ns.EVENT,
                keys: {
                    uuid: [type.TEXT, note.getUid()]
                },
                fields: fields

            }
        }

    },

    noteToTaskRecord: function(note){

        var stamp = note.getTaskStamp();

        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.TASK,
                ns: ns.TASK,
                keys: {
                    uuid: [type.TEXT, note.getUid()]
                },
                fields: {}

            }
        }

    },

    noteToModbyRecord: function(note){
        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.MODBY,
                ns: ns.MODBY,
                keys:{
                    uuid: [type.TEXT, note.getUid()],
                    userid: [type.TEXT, ""],
                    action: [type.INTEGER, 100], //TODO: figure this out
                    timestamp: [type.DECIMAL, new Date().getTime()]
                }
            }
        }
    },

    /*
     * Add modification record set to the appropriate member of the "items" hash.
     */
    addModificationRecordSetToItems: function(items, recordSet){
        var uidParts = recordSet.uuid.split("::");
        if (!items[uidParts[0]]){
            throw new cosmo.service.translators.
               ParseError("No item found with uid " + uidParts[0] +
                           ". Cannot apply modification with recurrence" +
                           " date " + uidParts[1]);
        }

        var item = items[uidParts[0]];

        var modifiedProperties = {};
        var modifiedStamps = {};

        for (recordName in recordSet.records){
        with (cosmo.service.eim.constants){

           var record = recordSet.records[recordName];

           switch(recordName){

           case prefix.ITEM:
           case prefix.NOTE:
               for (propertyName in record.fields){
                   modifiedProperties[propertyName] = record.fields[propertyName][1]
               }
               break;
           case prefix.EVENT:
               modifiedStamps[prefix.EVENT] = this.getEventStampProperties(record);
               break;
           case prefix.TASK:
               modifiedStamps[prefix.TASK] = this.getTaskStampProperties(record);
               break;
           }
        }
        }

        var mod = new cosmo.model.Modification(
            {
                "recurrenceId": uidParts[1],
                "modifiedProperties": modifiedProperties,
                "modifiedStamps": modifiedStamps
            }
        );

        item.addModification(mod);
    },

    recordSetToObject: function (/*Object*/ recordSet){
        //TODO
        /* We can probably optimize this by grabbing the
         * appropriate properties from the appropriate records
         * and passing them into the constructor. This will probably
         * be a little less elegant, and will require the creation of
         * more local variables, so we should play with this later.
         */

        var note = new cosmo.model.Note(
         {
             uid: recordSet.uuid
         }
        );

        for (recordName in recordSet.records){
        with (cosmo.service.eim.constants){

           var record = recordSet.records[recordName]

           switch(recordName){

           case prefix.ITEM:
               this.addItemRecord(record, note);
               break;
           case prefix.NOTE:
               this.addNoteRecord(record, note);
               break;
           case prefix.EVENT:
               note.getStamp(prefix.EVENT, true, this.getEventStampProperties(record));
               break;
           case prefix.TASK:
               note.getStamp(prefix.TASK, true, this.getTaskStampProperties(record));
               break;
           }
        }
        }
        return note;

    },

    getEventStampProperties: function getEventStampProperties(record){

        var dateParams = this.dateParamsFromEimDate(record.fields.dtstart[1]);

         return {
            startDate: record.fields.dtstart? this.fromEimDate(record.fields.dtstart[1]): undefined,
            duration: record.fields.duration?
                cosmo.datetime.parseIso8601Duration(record.fields.duration[1]) : undefined,
            anytime: dateParams.anyTime,
            location: record.fields.location? record.fields.location[1] : undefined,
            rrule: record.fields.rrule? this.parseRRule(record.fields.location[1]): undefined,
            exdates: null, //TODO
            status: record.fields.status? record.fields.status[1]: undefined
         }
    },

    getTaskStampProperties: function getTaskStampProperties(record){

        return {};
    },

    addItemRecord: function addItemRecord(record, object){
        if (record.fields.title) object.setDisplayName(record.fields.title[1]);
        if (record.fields.createdOn) object.setCreationDate(record.fields.createdOn[1]);

        if (record.fields.triage) this.addTriage(record.fields.triage[1], object);
    },

    addNoteRecord: function addNoteRecord(record, object){
        if (record.fields.body) object.setBody(record.fields.body[1]);
    },

    fromEimDate: function fromEimDate(dateString){
        var date = dateString.split(":")[1];
        return cosmo.datetime.fromIso8601(date);
    },

    addTriage: function addTriage(triageString, object){
        var triageArray = triageString.split(" ");

        object.setTriageStatus(triageArray[0]);

        object.setRank(triageArray[1]);

        /* This looks weird, but because of JS's weird casting stuff, it's necessary.
         * Try it if you don't believe me :) - travis@osafoundation.org
         */
        object.setAutoTriage(triageArray[2] == true);
    },

    dateParamsFromEimDate: function dateParams(dateString){
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
        return returnVal;
    },

    anyTimeFromEimDate: function(dateString){

        return false;

    },

    rruleToICal: function rruleToICal(rrule){
        if (rrule.isUnsupported()){
            return rrulePropsToICal(rrule.getUnsupportedRule());
        } 
        else {
            //TODO
            dojo.unimplemented("rrule to ical not implemented for supported rules");
        }
    },

    rrlePropsToICal: function rrulePropsToICal(rProps){
        var iCalProps = [];
        for (var key in rProps){
            iCalProps.push(key);
            iCalProps.push("=")
            if (dojo.lang.isArray(rProps[key])){
                iCalProps.push(rProps[key].join());
            }
            else if (rProps[key] instanceof cosmo.datetime.Date){
                iCalProps.push(
                    dojo.date.strftime(rProps[key].getUTCDateProxy(), "%Y%m%dT%H%M%SZ")
                );
            }
            else {
                iCalProps.push(rProps[key]);
            }
            iCalProps.push(";");
            return iCalProps.join("");
        }
    },

    parseRRule: function parseRRule(rule){
        if (!rule) {
            return null;
        }
        return this.rPropsToRRule(this.parseRRuleToHash(rule));
    },

    //Snagged from dojo.cal.iCalendar
    parseRRuleToHash: function parseRRuleToHash(rule){
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

    rruleConstants: {
      SECONDLY: "SECONDLY",
      MINUTELY: "MINUTELY",
      HOURLY: "HOURLY",
      DAILY: "DAILY",
      MONTHLY:"MONTHLY",
      WEEKLY:  "WEEKLY",
      YEARLY: "YEARLY"
    },

    isRRuleUnsupported: function isRRuleUnsupported(recur){

        with (this.rruleConstants){

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
    },


    rPropsToRRule: function rPropsToRRule(rprops){
        if (this.isRRuleUnsupported(rprops)) {
            // TODO set something more readable?
            return new cosmo.model.RecurrenceRule({
                isSupported: false,
                unsupportedRule: rprops
            });
        } else {
            var RecurrenceRule = cosmo.model.RecurrenceRule;
            var Recur = this.rruleConstants;
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
                recurrenceRule.endDate = cosmo.datetime.fromIso8601(rprops.until);
            }
            
            recurrenceRule = new cosmo.model.RecurrenceRule(recurrenceRule);
            

            return recurrenceRule;
        }

    }


});

cosmo.service.translators.eim = new cosmo.service.translators.Eim();


