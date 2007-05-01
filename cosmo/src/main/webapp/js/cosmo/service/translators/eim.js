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

dojo.require("cosmo.model.Item");
dojo.require("cosmo.service.translators.common");

dojo.declare("cosmo.service.translators.Eim", null, {



    responseToObject: function atomPlusEimResponseToObject(atomXml){
        if (!atomXml){
            throw new ParseError("Cannot parse null, undefined, or false");
        }
        var entries = atomXml.getElementsByTagName("entry");
        var items = [];
        for (var i = 0; i < entries.length; i++){
            var c = entries[i].getElementsByTagName("content")[0];
            var content = c.firstChild.nodeValue;
            var item = this.recordSetToObject(eval("(" + content + ")"))

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

            items.push(item);
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

    recordSetToObject: function (recordSet){
        //TODO
        /* We can probably optimize this by grabbing the
         * appropriate properties from the appropriate records
         * and passing them into the constructor. This will probably
         * be a little less elegant, and will require the creation of
         * more local variables, so we should play with this later.
         * */

        var note = new cosmo.model.Note(
         {
             uid: recordSet.uuid,

         }
        );

        for (recordName in recordSet.records){
        with (cosmo.service.eim.constants){

           var record = recordSet.records[recordName]

           if (recordName == prefix.ITEM){
               this.addItemRecord(record, note);
           }

           if (recordName == prefix.NOTE){
               this.addNoteRecord(record, note);
               description = record.fields.body[1];
           }

           if (recordName == prefix.EVENT){
               note.addStamp(this.eventRecordToEventStamp(record));
           }
           }
        }

        return note;

    },


    eventRecordToEventStamp: function (record){

        var dateParams = this.dateParamsFromEimDate(record.fields.dtstart[1]);

        var stamp = new cosmo.model.EventStamp(
         {
            startDate: this.fromEimDate(record.fields.dtstart[1]),
            duration: record.fields.duration[1], //TODO
            anytime: dateParams.anyTime,
            location: record.fields.location[1],
            rrule: null, //TODO
            exdates: null, //TODO
            status: record.fields.status[1]
         }
        );
        return stamp;
    },

    addItemRecord: function (record, object){
        object.setDisplayName(record.fields.title[1]);
        object.setCreationDate(record.fields.createdOn[1]);
        //object.setModifiedDate(null) //TODO;

        this.addTriage(record.fields.triage[1], object);
    },

    addNoteRecord: function (record, object){
        object.setBody(record.fields.body[1]);
    },

    fromEimDate: function (dateString){
        var date = dateString.split(":")[1];
        return new cosmo.datetime.Date(dojo.date.fromIso8601(date));

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
        return returnVal;
    },

    anyTimeFromEimDate: function(dateString){

        return false;

    }


});

cosmo.service.translators.eim = new cosmo.service.translators.Eim();

dojo.date.addIso8601Duration = function (date, duration){

    var r = "^P(?:(?:([0-9\.]*)Y)?(?:([0-9\.]*)M)?(?:([0-9\.]*)D)?(?:T(?:([0-9\.]*)H)?(?:([0-9\.]*)M)?(?:([0-9\.]*)S)?)?)(?:([0-9/.]*)W)?$"
    var dateArray = duration.match(r).slice(1);
    with(cosmo.datetime.dateParts){
        dateArray[0]? date.add(YEAR, dateArray[0]) : null;
        dateArray[1]? date.add(MONTH, dateArray[1]) : null;
        dateArray[2]? date.add(DAY, dateArray[2]) : null;
        dateArray[3]? date.add(HOUR, dateArray[3]) : null;
        dateArray[4]? date.add(MINUTE, dateArray[4]) : null;
        dateArray[5]? date.add(SECOND, dateArray[5]) : null;
    }

    return date;
}

cosmo.datetime.getIso8601Duration = function(dt1, dt2){

   return "P" +// + this.Date.diff('yyyy', dt1, dt2) + "Y" +
          //this.Date.diff('m', dt1, dt2) + "M" +
          //this.Date.diff('d', dt1, dt2) + "D" +
          'T' +
          //this.Date.diff('h', dt1, dt2) + "H" +
          //this.Date.diff('n', dt1, dt2) + "M" +
          this.Date.diff('s', dt1, dt2) + "S";
}

