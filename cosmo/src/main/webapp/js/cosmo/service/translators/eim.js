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
dojo.require("dojo.json");

dojo.require("cosmo.service.eim");
dojo.require("cosmo.model.*");
dojo.require("cosmo.service.translators.common");
dojo.require("cosmo.datetime.serialize");
dojo.require("cosmo.util.html");

dojo.declare("cosmo.service.translators.Eim", null, {
    
    initializer: function (){
        with (this.rruleConstants) {
        with (cosmo.model.RRULE_FREQUENCIES){
            this.rruleFrequenciesToRruleConstants =
            {
                FREQUENCY_DAILY: DAILY,
                FREQUENCY_WEEKLY: WEEKLY,
                FREQUENCY_BIWEEKLY: WEEKLY + ";INTERVAL=2",
                FREQUENCY_MONTHLY: MONTHLY,
                FREQUENCY_YEARLY: YEARLY
            }     
        }}
    },
    
    translateGetCollection: function (atomXml){
        var collection = new cosmo.model.CollectionDetails(
            {uid: atomXml.getElementsByTagName("id")[0].firstChild.nodeValue.substring(9)
            }
            );
        return collection;
        
    },
          
    translateGetCollections: function (atomXml, kwArgs){
        var workspaces = atomXml.getElementsByTagName("workspace");
        var collections = [];
        for (var i = 0; i < workspaces.length; i++){
            var workspace = workspaces[i];
            
            var title = cosmo.util.html.getElementsByTagName(workspace, "atom", "title")[0];

            if (title.firstChild.nodeValue == "meta") continue;

            var collectionElements = workspace.getElementsByTagName("collection");
            
            for (var j = 0; j < collectionElements.length; j++){
                var collection = this.collectionXmlToCollection(collectionElements[j]);
                collection.getDetails = kwArgs.getDetails;
                collections.push(collection);
            }
        }
        return collections;
    },
    
    collectionXmlToCollection: function (collectionXml){
        return collection = new cosmo.model.Collection(
            {
                //TODO: replace this with the correct uid grabbing code
                uid: collectionXml.getAttribute("href").split("/")[1],
                displayName: cosmo.util.html.getElementsByTagName(collectionXml, "atom", "title")
                    [0].firstChild.nodeValue
            }
        );
    },

    translateGetItems: function (atomXml){
        if (!atomXml){
            throw new cosmo.service.translators.ParseError("Cannot parse null, undefined, or false");
        }
        var entries = atomXml.getElementsByTagName("entry");

        var items = {};
        var mods = {};
        for (var i = 0; i < entries.length; i++){
            var entry = entries[i];
            try {
                var uuid = entry.getElementsByTagName("id")[0];
            } catch (e){
                throw new cosmo.service.translators.
                   ParseError("Could not find id element for entry " + (i+1));
            }
            uuid = uuid.firstChild.nodeValue.substring(9);
            if (!uuid.split("::")[1]){
                items[uuid] = this.entryToItem(entry, uuid)
            }
            else {
                mods[uuid] = entry;
            }
        }

        for (var uuid in mods){
            var masterItem = items[uuid.split("::")[0]];
            if (!masterItem) throw new 
              cosmo.service.translators.ParseError(
              "Could not find master event for modification " +
              "with uuid " + uuid);
              
            items[uuid] = this.entryToItem(mods[uuid], uuid, masterItem);
            //this.addModificationRecordSet(items, eval("(" + mods[i] + ")"));
        }
        var itemArray = [];
        for (var uid in items){
            itemArray.push(items[uid]);
        }

        return itemArray;
    },
    
    entryToItem: function (/*XMLElement*/entry, /*String*/uuid, 
        /*cosmo.model.Item*/ masterItem){
            var uuidParts = uuid.split("::");
            var uidParts = uuidParts[0].split(":");
            try {
                var c = entry.getElementsByTagName("content")[0];
            } catch (e){
                throw new cosmo.service.translators.
                   ParseError("Could not find content element for entry " + (i+1));
            }
            var content = c.firstChild.nodeValue;
            var item;
            // If we have a second part to the uid, this entry is a
            // recurrence modification.
            if (masterItem){
                item = this.recordSetToModification(eval("(" + content + ")"), masterItem); 
            }
            else {
                item = this.recordSetToObject(eval("(" + content + ")"))
            }
            
            var links = cosmo.util.html.getElementsByTagName(entry, "link");
            for (var j = 0; j < links.length; j++){
                var link = links[j];
                if (link.getAttribute('rel') == 'edit'){
                    item.editLink = link.getAttribute('href');
                };
            }

            return item;
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
    
    /*
     * 
     */
    recordSetToModification: function (recordSet, masterItem){
        var uidParts = recordSet.uuid.split("::");

        var modifiedProperties = {};
        var modifiedStamps = {};
        
        for (recordName in recordSet.records){
            with (cosmo.service.eim.constants){
    
               var record = recordSet.records[recordName];
    
               switch(recordName){
    
               case prefix.ITEM:
               case prefix.NOTE:
                   for (propertyName in record.fields){
                       modifiedProperties[propertyName] = record.fields[propertyName][1];
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
        
        var recurrenceId = this.recurrenceIdToDate(uidParts[1])
        
        if (!dojo.lang.isEmpty(modifiedProperties)
            || !dojo.lang.isEmpty(modifiedStamps)){
            var mod = new cosmo.model.Modification(
                {
                    "recurrenceId": recurrenceId,
                    "modifiedProperties": modifiedProperties,
                    "modifiedStamps": modifiedStamps
                }
            );
    
            masterItem.addModification(mod);
        }
        return masterItem.getNoteOccurrence(recurrenceId);
    },
    
    recurrenceIdToDate: function (/*String*/ rid){
         return cosmo.datetime.fromIso8601(rid.split(":")[1]);
    },

    itemToAtomEntry: function (object){
         var jsonObject = this.noteToRecordSet(object);
         return '<entry xmlns="http://www.w3.org/2005/Atom">' +
         '<title>' + object.getDisplayName() + '</title>' +
         '<id>urn:uuid:' + object.getUid() + '</id>' +
         '<updated>' + dojo.date.toRfc3339(new Date()) + '</updated>' +
         '<author><name>' + cosmo.util.auth.getUsername() + '</name></author>' +
         '<content type="application/eim+json">' + dojo.json.serialize(jsonObject) + '</content>' +
         '</entry>'
    },

    noteToRecordSet: function (note){
        var records = {
            item: this.noteToItemRecord(note),
            note: this.noteToNoteRecord(note),
            modby: this.noteToModbyRecord(note)
        };

        if (note.getEventStamp()) records.event = this.noteToEventRecord(note);
        
        return {
            uuid: note.getUid(),
            records: records
        }

    },

    noteToItemRecord: function(note){
        var title = note.getDisplayName();
        var triageRank = note.getRank();
        var triageStatus = note.getTriageStatus();
        var autoTriage = note.getAutoTriage();
        var fields = {};
        with (cosmo.service.eim.constants){
        
            if (title) fields.title = [type.TEXT, title];
            if (triageRank || triageRank || autoTriage)
                fields.triage =  [type.TEXT, [triageStatus, triageRank, autoTriage? 1 : 0].join(" ")];
            
            return {
                prefix: prefix.ITEM,
                ns: ns.ITEM,
                keys: {
                    uuid: [type.TEXT, note.getUid()]
                },
                fields: fields
            }
        }
    },

    noteToNoteRecord: function(note){
        var body = note.getBody();
        with (cosmo.service.eim.constants){
            var fields = {};
            if (body) fields.body = [type.CLOB, body];
            return {
                prefix: prefix.NOTE,
                ns: ns.NOTE,
                keys: {
                    uuid: [type.TEXT, note.getUid()]
                },
                fields: fields
            }
        }
    },

    noteToEventRecord: function(note){

        var stamp = note.getEventStamp();
        var allDay = stamp.getAllDay();
        var anyTime = stamp.getAnyTime();
        var start = stamp.getStartDate();
        var rrule = stamp.getRrule();
        var stat = stamp.getStatus();
        var loc = stamp.getLocation();
        var duration = stamp.getDuration();

        with (cosmo.service.eim.constants){
            var fields = {};
            if (start) fields.dtstart = 
                [type.TEXT, this.dateToEimDtstart(start, allDay, anyTime)];
            if (rrule) fields.rrule = [type.TEXT, rrule.toString()];
            if (stat) fields.status = [type.TEXT, stat];
            if (loc) fields.location = [type.TEXT, loc];
            if (duration) fields.duration = [type.TEXT, duration.toIso8601()];
            if (rrule) fields.rrule = [type.TEXT, this.rruleToICal(rrule)];
            
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
    
    dateToEimDtstart: function (start, allDay, anyTime){
        return [";VALUE=",
                ((allDay || anyTime)? "DATE" : "DATE-TIME"),
                (anyTime? ";X-OSAF-ANYTIME=TRUE" : ""),
                ":",
                ((allDay || anyTime)?
                    start.strftime("%Y%m%d"):
                    start.strftime("%Y%m%dT%H%M%S"))
                ].join("");
        
    },

    noteToTaskRecord: function (note){

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

    getEventStampProperties: function (record){

        var dateParams = this.dateParamsFromEimDate(record.fields.dtstart[1]);

         return {
            startDate: record.fields.dtstart? this.fromEimDate(record.fields.dtstart[1]): undefined,
            duration: record.fields.duration?
                new cosmo.model.Duration(record.fields.duration[1]) : undefined,
            anyTime: dateParams.anyTime,
            allDay: dateParams.allDay,
            location: record.fields.location? record.fields.location[1] : undefined,
            rrule: record.fields.rrule? this.parseRRule(record.fields.rrule[1]): undefined,
            exdates: null, //TODO
            status: record.fields.status? record.fields.status[1]: undefined
         }
    },

    getTaskStampProperties: function (record){

        return {};
    },

    addItemRecord: function (record, object){
        if (record.fields.title) object.setDisplayName(record.fields.title[1]);
        if (record.fields.createdOn) object.setCreationDate(record.fields.createdOn[1]);

        if (record.fields.triage) this.addTriage(record.fields.triage[1], object);
    },

    addNoteRecord: function (record, object){
        if (record.fields.body) object.setBody(record.fields.body[1]);
    },

    fromEimDate: function (dateString){
        var date = dateString.split(":")[1];
        return cosmo.datetime.fromIso8601(date);
    },

    addTriage: function (triageString, object){
        var triageArray = triageString.split(" ");

        object.setTriageStatus(triageArray[0]);

        object.setRank(triageArray[1]);

        /* This looks weird, but because of JS's weird casting stuff, it's necessary.
         * Try it if you don't believe me :) - travis@osafoundation.org
         */
        object.setAutoTriage(triageArray[2] == true);
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
    
    rruleToICal: function (rrule){
        if (rrule.isSupported()){
            return [
                ";FREQ=",
                this.rruleFrequenciesToRruleConstants[rrule.getFrequency()],
                ";UNTIL",
                dojo.date.strftime(rrule.getEndDate().getUTCDateProxy(), "%Y%m%dT%H%M%SZ")
            ].join("");
        } 
        else {
            return rrulePropsToICal(rrule.getUnsupportedRule());
        }
    },

    rrlePropsToICal: function (rProps){
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

    parseRRule: function (rule){
        if (!rule) {
            return null;
        }
        return this.rPropsToRRule(this.parseRRuleToHash(rule));
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

    rruleConstants: {
      SECONDLY: "SECONDLY",
      MINUTELY: "MINUTELY",
      HOURLY: "HOURLY",
      DAILY: "DAILY",
      MONTHLY:"MONTHLY",
      WEEKLY:  "WEEKLY",
      YEARLY: "YEARLY"
    },
    
    isRRuleUnsupported: function (recur){

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


    rPropsToRRule: function (rprops){
        if (this.isRRuleUnsupported(rprops)) {
            // TODO set something more readable?
            return new cosmo.model.RecurrenceRule({
                isSupported: false,
                unsupportedRule: rprops
            });
        } else {
            var RecurrenceRule = cosmo.model.RRULE_FREQUENCIES;
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


