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

dojo.provide("cosmo.service.translators.eim.Serializer");

dojo.require("cosmo.service.translators.eim.common");

dojo.declare("cosmo.service.translators.eim.Serializer", {
    getUid: dojo.lang.hitch(cosmo.service, cosmo.service.getUid),
    
    objectToRecordSet: function (note){
        if (note instanceof cosmo.model.NoteOccurrence){
            return this.noteOccurrenceToRecordSet(note);
        } else if (note instanceof cosmo.model.Note){
            return this.noteToRecordSet(note);
        } else {
            throw new cosmo.service.translators.exception.ModelToRecordSetException(
                "note is neither a Note nor a NoteOccurrence, don't know how to translate."
            )
        }
    },

    noteOccurrenceToRecordSet: function(noteOccurrence){
        var modification = noteOccurrence.getMaster().getModification(noteOccurrence.recurrenceId);
        var records = {
            modby: this.noteToModbyRecord(noteOccurrence)
        }
        if (this.modificationHasItemModifications(modification))  
            records.item =  this.modifiedOccurrenceToItemRecord(noteOccurrence);
        else records.item = this.generateEmptyItem(noteOccurrence);

        if (this.modificationHasNoteModifications(modification))
            records.note = this.modifiedOccurrenceToNoteRecord(noteOccurrence);
        else records.note = this.generateEmptyNote(noteOccurrence);
        
        // There will always be an event stamp
        records.event = this.modifiedOccurrenceToEventRecord(noteOccurrence);
        
        if (modification.getModifiedStamps().task || noteOccurrence.getMaster().getTaskStamp()){
            records.task = this.noteToTaskRecord(noteOccurrence);
        } 
        if (modification.getModifiedStamps().mail || noteOccurrence.getMaster().getMailStamp()){
            records.mail = this.modifiedOccurrenceToMailRecord(noteOccurrence);
        }

        var recordSet =  {
            uuid: this.getUid(noteOccurrence),
            records: records
        };
        
        this.addStampsToDelete(recordSet, noteOccurrence);
        
        return recordSet;
        
    },
    
    noteToRecordSet: function(note){
        var records = {
            item: this.noteToItemRecord(note),
            note: this.noteToNoteRecord(note),
            modby: this.noteToModbyRecord(note)
        };

        if (note.getEventStamp()) records.event = this.noteToEventRecord(note);
        if (note.getTaskStamp()) records.task = this.noteToTaskRecord(note);
        if (note.getMailStamp()) records.mail = this.noteToMailRecord(note);
        
        var recordSet =  {
            uuid: this.getUid(note),
            records: records
        };
        
        this.addStampsToDelete(recordSet, note);
        
        return recordSet;
    },

    noteToModbyRecord: function(note){
        with (cosmo.service.translators.eim.constants){
            return {
                prefix: prefix.MODBY,
                ns: ns.MODBY,
                key:{
                    uuid: [type.TEXT, this.getUid(note)],
                    userid: [type.TEXT, note.getModifiedBy().getUserId() || 
                                        cosmo.util.auth.getUsername()],
                    action: [type.INTEGER, note.getModifiedBy().getAction()],
                    timestamp: [type.DECIMAL, new Date().getTime()/1000]
                }
            }
        }
    },

    modificationHasItemModifications: function (modification){
        var props = modification.getModifiedProperties();
        return (props.displayName || props.triageRank || props.triageStatus || props.autoTriage)    
    },

    noteToItemRecord: function(note){
        var props = {};
        props.displayName = note.getDisplayName();
        props.rank = note.getRank();
        props.triageStatus = note.getTriageStatus();
        props.autoTriage = note.getAutoTriage();
        props.creationDate = note.getCreationDate();
        props.uuid = this.getUid(note);
        return this.propsToItemRecord(props);
    },
    
    modifiedOccurrenceToItemRecord: function(modifiedOccurrence){
        var modification = modifiedOccurrence.getMaster().getModification(modifiedOccurrence.recurrenceId)
        var props = modification.getModifiedProperties();
        props.uuid = this.getUid(modifiedOccurrence);
        var record = this.propsToItemRecord(props);
        var missingFields = [];
        if (record.fields.title == undefined) missingFields.push("title");
        if (record.fields.triage == undefined) missingFields.push("triage");
        if (record.fields.createdOn == undefined) missingFields.push("createdOn");
        record.missingFields = missingFields;
        return record;
    },
    
    generateEmptyItem: function(note){
        var record = this.propsToItemRecord({uuid: note.getUid()});
        record.missingFields = [
            "title",
            "triage",
            "hasBeenSent",
            "needsReply"
        ]
        return record;
    },
    
    propsToItemRecord: function(props){
        var fields = {};
        with (cosmo.service.translators.eim.constants){
        
            if (props.displayName !== undefined) fields.title = [type.TEXT, props.displayName];
            if (props.creationDate !== undefined) fields.createdOn = [type.DECIMAL, props.creationDate/1000];
            if (props.triageStatus)
                fields.triage =  [type.TEXT, [props.triageStatus, this.fixTriageRank(props.rank), props.autoTriage? 1 : 0].join(" ")];
            
            return {
                prefix: prefix.ITEM,
                ns: ns.ITEM,
                key: {
                    uuid: [type.TEXT, props.uuid]
                },
                fields: fields
            }
        }
    
    },
    
    // Make sure triage rank ends in two decimals
    fixTriageRank: function(rank){
        rank = rank || "0";
        if (rank.toString().match(/d*\.\d\d/)) return rank;
        else return rank + ".00";
    },

    noteToNoteRecord: function(note){
        var props = {};
        var uid = this.getUid(note);
        props.body = note.getBody();
        props.icalUid = note.getIcalUid() || uid;
        props.uuid = uid;
        return this.propsToNoteRecord(props);
    },
    
    modificationHasNoteModifications: function (modification){
        var props = modification.getModifiedProperties();
        return (props.body || props.icalUid);
    },
    
    
    modifiedOccurrenceToNoteRecord: function(modifiedOccurrence){
        var modification = modifiedOccurrence.getMaster().getModification(modifiedOccurrence.recurrenceId)
        var props = modification.getModifiedProperties();
        props.uuid = this.getUid(modifiedOccurrence);
        var record = this.propsToNoteRecord(props);
        var missingFields = [];
        if (record.fields.body == undefined) missingFields.push("body");
        if (record.fields.icalUid == undefined) missingFields.push("icalUid");
        record.missingFields = missingFields;
        return record;
    },
    
    generateEmptyNote: function (note){
        var record = this.propsToNoteRecord({uuid: note.getUid()});
        record.missingFields = [
            "body",
            "icalUid"
        ];
        return record;
    },
    
    propsToNoteRecord: function (props){
        with (cosmo.service.translators.eim.constants){
            var fields = {};
            if (props.body !== undefined) fields.body = [type.CLOB, props.body];
            if (props.icalUid !== undefined) fields.icalUid = [type.TEXT, props.icalUid];
            return {
                prefix: prefix.NOTE,
                ns: ns.NOTE,
                key: {
                    uuid: [type.TEXT, props.uuid]
                },
                fields: fields
            }
        }
    },

    noteToMailRecord: function(note){
        var props = {};
        stamp = note.getMailStamp();
        props.messageId = stamp.getMessageId();
        props.headers = stamp.getHeaders();
        props.fromAddress = stamp.getFromAddress();
        props.toAddress = stamp.getToAddress();
        props.ccAddress = stamp.getCcAddress();
        props.bccAddress = stamp.getBccAddress();
        props.originators = stamp.getOriginators();
        props.dateSent = stamp.getDateSent();
        props.inReplyTo = stamp.getInReplyTo();
        props.references = stamp.getReferences();
        props.uuid = this.getUid(note);
        return this.propsToMailRecord(props);

    },

    modifiedOccurrenceToMailRecord: function(modifiedOccurrence){
        var modification = modifiedOccurrence.getMaster().getModification(modifiedOccurrence.recurrenceId);
        var props = modification.getModifiedStamps().mail || {};
        props.uuid = modifiedOccurrence.getUid();
        var record = this.propsToMailRecord(props);
        var missingFields = [];
        if (record.fields.messageId == undefined) missingFields.push("messageId");
        if (record.fields.headers == undefined) missingFields.push("headers");
        if (record.fields.fromAddress == undefined) missingFields.push("fromAddress");
        if (record.fields.toAddress == undefined) missingFields.push("toAddress");
        if (record.fields.ccAddress == undefined) missingFields.push("ccAddress");
        if (record.fields.bccAddress == undefined) missingFields.push("bccAddress");
        if (record.fields.originators == undefined) missingFields.push("originators");
        if (record.fields.dateSent == undefined) missingFields.push("dateSent");
        if (record.fields.inReplyTo == undefined) missingFields.push("inReplyTo");
        if (record.fields.references == undefined) missingFields.push("references");
        record.missingFields = missingFields;
        return record;
    },
    
    propsToMailRecord: function(props){
        with (cosmo.service.translators.eim.constants){
            var fields = {};
            var missingFields = [];
            if (props.messageId !== undefined) fields.messageId = [type.TEXT, props.messageId];
            if (props.headers !== undefined) fields.headers = [type.CLOB, props.headers];
            if (props.fromAddress !== undefined) fields.fromAddress = [type.TEXT, props.fromAddress];
            if (props.toAddress !== undefined) fields.toAddress = [type.TEXT, props.toAddress];
            if (props.ccAddress !== undefined) fields.ccAddress = [type.TEXT, props.ccAddress];
            if (props.bccAddress !== undefined) fields.bccAddress = [type.TEXT, props.bccAddress];
            if (props.originators !== undefined) fields.originators = [type.TEXT, props.originators];
            if (props.dateSent !== undefined) fields.dateSent = [type.TEXT, props.dateSent];
            if (props.inReplyTo !== undefined) fields.inReplyTo = [type.TEXT, props.inReplyTo];
            if (props.references !== undefined) fields.references = [type.CLOB, props.references];
            
            return record = {
                prefix: prefix.MAIL,
                ns: ns.MAIL,
                key: {
                    uuid: [type.TEXT, props.uuid]
                },
                fields: fields
            }
            return record;
        }   
    },
    
    noteToEventRecord: function(note){
        var props = {};
        stamp = note.getEventStamp();
        props.allDay = stamp.getAllDay();
        props.anyTime = stamp.getAnyTime();
        props.startDate = stamp.getStartDate();
        props.rrule = stamp.getRrule();
        props.status = stamp.getStatus();
        props.location = stamp.getLocation();
        props.duration = stamp.getDuration();
        props.exdates = stamp.getExdates();
        props.uuid = this.getUid(note);
        return this.propsToEventRecord(props);

    },

    modifiedOccurrenceToEventRecord: function(modifiedOccurrence){
        var modification = modifiedOccurrence.getMaster().getModification(modifiedOccurrence.recurrenceId);
        var props = modification.getModifiedStamps().event || {};
        if (props.allDay || props.anyTime) props.startDate = 
            modifiedOccurrence.getEventStamp().getStartDate();
        props.uuid = modifiedOccurrence.getUid();
        var record = this.propsToEventRecord(props);
        var missingFields = [];
        if (record.fields.dtstart == undefined) missingFields.push("dtstart");
        if (record.fields.status == undefined) missingFields.push("status");
        if (record.fields.location == undefined) missingFields.push("location");
        if (record.fields.duration == undefined) missingFields.push("duration");
        record.missingFields = missingFields;
        return record;
    },
    
    propsToEventRecord: function(props){
        with (cosmo.service.translators.eim.constants){
            var fields = {};
            if (props.startDate !== undefined) fields.dtstart = 
                [type.TEXT, this.dateToEimDtstart(props.startDate, props.allDay, props.anyTime)];
            if (props.status !== undefined) fields.status = [type.TEXT, props.status];
            if (props.location !== undefined) fields.location = [type.TEXT, props.location];
            if (props.duration !== undefined) fields.duration = [type.TEXT, props.duration == null? cosmo.model.ZERO_DURATION : props.duration.toIso8601()];
            if (props.rrule !== undefined) fields.rrule = [type.TEXT, this.rruleToICal(props.rrule)];
            if (props.exdates && props.exdates.length != 0) fields.exdate = 
                [type.TEXT, this.exdatesToEim(props.exdates, props.startDate, props.allDay, props.anyTime)];
            
            return record = {
                prefix: prefix.EVENT,
                ns: ns.EVENT,
                key: {
                    uuid: [type.TEXT, props.uuid]
                },
                fields: fields
            }
        }

        
    },

    addStampsToDelete: function (recordSet, note){
        var stampsToDelete = note.getStampsToDelete();
        if (stampsToDelete.length > 0){
            var recordsToDelete = [];
            for (var i = 0; i < stampsToDelete.length; i++){
                var stampPrefix = stampsToDelete[i];
                recordsToDelete.push([stampPrefix, cosmo.model.getStampMetaData(stampPrefix).namespace]);
                recordSet.deletedRecords = recordsToDelete;
            }
            note.clearStampsToDelete();
        }
    },

    exdatesToEim: function(exdates, start, allDay, anyTime){
        return this.datesToEim(exdates, start, allDay, anyTime);
    },
    
    dateToEimDtstart: function (start, allDay, anyTime){
        return (anyTime? ";X-OSAF-ANYTIME=TRUE" : "") +
               this.datesToEim([start], start, allDay, anyTime);
    },
    
    datesToEim: function (dates, start, allDay, anyTime){
          var date = [(start.tzId? ";TZID=" + start.tzId : ""),
                ";VALUE=",
                ((allDay || anyTime)? "DATE" : "DATE-TIME"),
                ":"].join("");
          var formatString = cosmo.service.getDateFormatString(allDay, anyTime);
          date += dojo.lang.map(
                  dates,
                  function(date){
                      return date.strftime(formatString);
                  }
              ).join(",");
          return date;
    },

    noteToTaskRecord: function (note){

        with (cosmo.service.translators.eim.constants){
            return {
                prefix: prefix.TASK,
                ns: ns.TASK,
                key: {
                    uuid: [type.TEXT, this.getUid(note)]
                },
                fields: {}

            }
        }

    },

    rruleToICal: function (rrule){
        if (rrule === null) return rrule;
        if (rrule.isSupported()){
            var recurrenceRuleList = [
               ";FREQ=",
                this.rruleFrequenciesToRruleConstants[rrule.getFrequency()]
             ]
             var endDate = rrule.getEndDate();
             if (endDate){
                recurrenceRuleList.push(";UNTIL=");
                var dateString = this._createRecurrenceEndDateString(rrule.getEndDate())
                recurrenceRuleList.push(dateString);
             }
            
            return recurrenceRuleList.join("");
        } 
        else {
            return rrulePropsToICal(rrule.getUnsupportedRule());
        }
    },
    
    rrulePropsToICal: function (rProps, startDate){
        var iCalProps = [];
        for (var key in rProps){
            iCalProps.push(key);
            iCalProps.push("=")
            if (dojo.lang.isArray(rProps[key])){
                iCalProps.push(rProps[key].join());
            }
            else if (rProps[key] instanceof cosmo.datetime.Date){
                var dateString = this._createRecurrenceEndDateString(rProps[key]);
                iCalProps.push(dateString);
            }
            else {
                iCalProps.push(rProps[key]);
            }
            iCalProps.push(";");
            return iCalProps.join("");
        }
    },

    _createRecurrenceEndDateString: function (date){
        date = date.clone();
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        date = date.createDateForTimezone("utc");
        return dojo.date.strftime(date, "%Y%m%dT%H%M%SZ")
    }
});
