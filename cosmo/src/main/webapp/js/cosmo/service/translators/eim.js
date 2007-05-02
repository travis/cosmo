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

dojo.require("cosmo.service.eim");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.EventStamp");
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
                //rrule: [type.TEXT, ""],//FIXME
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
         * */

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
            duration: record.fields.duration? record.fields.duration[1]: undefined, //TODO
            anytime: dateParams.anyTime,
            location: record.fields.location? record.fields.location[1]: undefined,
            rrule: null, //TODO
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

    }


});

cosmo.service.translators.eim = new cosmo.service.translators.Eim();


