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

dojo.provide("cosmo.model.Delta");

dojo.declare("cosmo.model.Delta", null, {
    initializer: function(/*cosmo.model.Note | cosmo.model.NoteOccurrence */ note){
        this._stampProps = {}
        this._propertyProps = {};
        this._addedStamps = {};
        this._deletedStamps = {};
        this._note = note;
    },

    addStampProperty: function (stampName, propertyName, value){
        var stamp = this._getStamp(stampName);
        stamp[propertyName] = value;
    },
    
    addProperty: function (propertyName, value){
        this._propertyProps[propertyName] = value;
    },
    
    getProperty: function(propertyName){
       return this._propertyProps[propertyName];
    },
    
    getStampProperty: function(stampName, propertyName){
        var stamp = this._getStamp(stampName);
        return stamp[propertyName]; 
    },
    
    isPropertyChanged: function(propertyName){
        return typeof(this.getProperty(propertyName)) != "undefined";
    },
    
    isStampPropertyChanged: function(stampName, propertyName){
        return typeof(this.getStampProperty(stampName, propertyName)) != "undefined";
    },
    
    getAddedStamps: function(){
        return this._addedStamps;
    },
    
    addAddedStamp: function(stampName){
        this._addedStamps[stampName] = true;
    },
    
    getDeletedStamps: function(){
        return this._deletedStamps;
    },
    
    addDeletedStamp: function(stampName){
        this._deletedStamps[stampName] = true;
    },
    
    hasChanges: function(){
        if (!this._isEmpty(this._propertyProps)){
            return true;
        }
        
        for (var stampName in this._stampProps){
            if (!this._isEmpty(this._stampProps[stampName])){
                return true;
            }
        }
        
        if (!this._isEmpty(this._deletedStamps)){
            return true;
        }

        if (!this._isEmpty(this._addedStamps)){
            return true;
        }

        return false;
    },
    
    _isEmpty: function(object){
        for (var x in object){
            return false;
        }
        return true;
    },
      
    deltafy: function (){
        // summary: removes all properties which are the same as its note
        // description: removes all properties from the delta which are the same
        //              same as the properties in its note and its stamps,
        //              leaving you with just the delta, hence "deltafy"
        this._filterOutEqualProperties(this._note, this._propertyProps);
        for (var stampName in this._stampProps){
            var stamp = this._note.getStamp(stampName);
            if (stamp == null){
                continue;
            }
            var stampChanges = this._stampProps[stampName];

            if (stampChanges == null){
                continue;
            }
            this._filterOutEqualProperties(stamp, stampChanges);
        }        
    },
    
    getApplicableChangeTypes: function(){
        // summary: Returns the types of changes that you can apply with this delta
        // description: What is returned is a hash which contains "true" for each key
        //              that represents an applicable change type. Possible valid keys
        //              are:
        //                  "master" - you can apply this change to the master event,
        //                  thereby affecting all occurrences, if it has any.
        //
        //                  "occurrence" - you can apply this change to this one 
        //                  occurrence.
        //
        //                  "occurrenceAndFuture" - you can apply this change to this
        //                  occurrence and all future occurrences.
        if (!this._note.hasRecurrence()){
            return {master:true};
        }
         
        var eventStampDeleted = this._deletedStamps["event"];

        //if the eventStamp has been deleted, change can only apply to master. 
        if (eventStampDeleted){
            return {master:true};
        }
        
        var rruleChanged = this.isStampPropertyChanged("event", "rrule");
        //if recurrence rule has changed...
        
        if (rruleChanged){
            //if the rrule's been removed...
            if (this.getStampProperty("event", "rrule") == null){
                ///...the only applicable change is to the master event
                return {master:true};
            }
            
            var oldRule = this._note.getEventStamp().getRrule();
            var newRule = this.getStampProperty("event", "rrule");
            
            //the frequencies are the same, so therefore only the
            //end date has changed...
            if (oldRule.getFrequency() == newRule.getFrequency()){
                return {master:true};
            }            
            
            //the frequency has changed (and maybe end date), but is this
            //the first occurrence?
            if (this._note.isMaster() || this._note.isFirstOccurrence()){
                return {master:true};
            }
            
            //the frequency has changed, and this is NOT the first occurrence
            return {master:true, occurrenceAndFuture:true};
        }
        
        //if the startdate has moved beyond the recurrence interval...
        if (this._startDateMovedBeyondRecurrenceFrequency()){
            //...then changes can only be applied to the occurrence
            return {occurrence:true};
        }
        
        return { master:true, occurrenceAndFuture:true, occurrence:true};
    },
    
    applyChangeType: function(changeType){
        if (changeType == "master"){
            this.applyToMaster();
        } else if (changeType == "occurrenceAndFuture"){
            this.applyToOccurrenceAndFuture();
        } else if (changeType == "occurrence"){
            this.applyToOccurrence();
        } else {
            throw new Error("Invalid Change Type: " + changeType);
        }
    }, 
    
    applyToMaster: function(){
        this._apply("master");
        if (!this._note.hasRecurrence() &&this._needsAutoTriage()){
            this._note.autoTriage();
        }
    },

    applyToOccurrence: function(){
        this._apply("occurrence");
        if (this._needsAutoTriage()){
            this._note.autoTriage();
        }
    },

    applyToOccurrenceAndFuture: function(){
        var occurrence = this._note;
        var master = occurrence.getMaster();

        //create a new note from a clone of the old occurence's master.
        var newNote = master.clone();
        newNote.setUid(cosmo.model.uuidGenerator.generate());
        
        //set the new note's start date to the old occurrence's start date.
        newNote.getEventStamp().setStartDate(this._note.getEventStamp().getStartDate());
        
        //set the old note's rrule end date to just before the break.
        var newEndDate = occurrence.getEventStamp().getStartDate().clone();
        newEndDate.add(dojo.date.dateParts.HOUR, -1);
        var oldRrule = master.getEventStamp().getRrule();

        //we have to make a new RRule object, since we can't change them, since they
        //are immutable.
        var newRrule = new cosmo.model.RecurrenceRule({
            frequency: oldRrule.getFrequency(),
            endDate: newEndDate,
            isSupported: oldRrule.isSupported(),
            unsupportedRule: oldRrule.getUnsupportedRule()
        });
        master.getEventStamp().setRrule(newRrule);
        
        //now we have to go through the modifications in the original master 
        //event and get of any that occur on or past the split date from the original,
        //and get rid of any that occur before from the new one
        var splitUTC = newNote.getEventStamp().getStartDate();
        for (var rid in master._modifications){
            var mod = master._modifications[rid];
            var ridDate = mod.getRecurrenceId();
            var ridUTC = ridDate.toUTC();
            if (splitUTC >= ridUTC){
                delete master._modifications[rid];
            } else {
                delete newNote._modifications[rid];
            }
        }
        
        
        //now the split has happened and we can FINALLY apply the changes.
        //note that we apply a "master" type of delta to the new note
        this._apply("master", newNote);
        
        return newNote;

        //HACK - this might break with custom recurrence rules
    },
    
    
    _needsAutoTriage: function(){
        //summary: determines whether auto triage might be needed. 
        //descripiton: If any properties which might cause a triage change have been changed,
        //              return strue. Called by autoTriage().
        var note = this._note;
        
        if (!note.getAutoTriage() || !note.getEventStamp()){
            return false;
        }
        
        //if any of these properties change. we need to try autotriaging.
        var delta = this;
        return dojo.lang.some([
            ["event", "startDate"],
            ["event", "endDate"],
            ["event", "duration"],
            ["event", "anyDay"], 
            ["event", "allDay"], 
            ["event", "atTime"]
        ], 
        
        function(eventProp){
            return delta.isStampPropertyChanged(eventProp[0], eventProp[1]);
        });
        
    },
    

    _apply: function(type, note){
        note = note || this._note;
        for (var stampName in this._deletedStamps){
            note.removeStamp(stampName);
        }
        
        for (var stampName in this._addedStamps){
            note.getStamp(stampName, true);
        }
        
        this._applyProperties(note, this._propertyProps, type);
        
        for (var stampName in this._stampProps){
            var stampChanges = this._stampProps[stampName];
            if (stampChanges == null){
                continue;
            }
            
            //create the stamp if it doesn't exist yet
            var stamp = note.getStamp(stampName,true);
            
            if (stampName == "event"){
                this._applyPropertiesToEventStamp(stamp, stampChanges, type);
            } else {
                this._applyProperties(stamp, stampChanges, type);
            }
        }
    },
    
   _applyProperties: function(original,changes, type){
        for (var propName in changes){
           var changeValue = changes[propName];
           original.applyChange(propName, changeValue, type);
        }
    },
    
    _applyPropertiesToEventStamp: function(original, changes, type){
        //start date must be applied first so that duration can be calculate
        //properly
        if (changes["startDate"]){
           var changeValue = changes["startDate"];
           original.applyChange("startDate", changeValue, type);
           delete changes["startDate"];
        }
        this._applyProperties(original, changes, type);
    },
    
    _filterOutEqualProperties: function (original, changes){
        for (var propName in changes){
            var changeValue = changes[propName];
            if (!original.isChanged(propName, changeValue)){
                delete changes[propName];
            } 
        }
    },
    
    _getStamp: function (stampName){
        var stamp = this._stampProps[stampName];
        if (!stamp){
            stamp = {};
            this._stampProps[stampName] = stamp;
        }
        return stamp;        
    },
    
    _startDateMovedBeyondRecurrenceFrequency: function (){
        // summary: determines if the startDate was moved beyond the range of 
        // the recurrence interval.
        // description: If the start date is moved forward one ore more recurrence intervals,
        // true is returned.
        // history: Adapted from mde's original code in cosmo.view.cal
        if (!this.isStampPropertyChanged("event", "startDate")){
            return false;
        }

        var changedDate = this.getStampProperty("event", "startDate");
        var originalDate = this._note.getEventStamp().getStartDate();
        var frequency = this._note.getEventStamp().getRrule().getFrequency();
        
        var unit = this._ranges[frequency][0];
        var bound = this._ranges[frequency][1];
        var diff = cosmo.datetime.Date.diff(unit, originalDate, changedDate);
        return diff >= bound || diff <= (bound * -1)
    },
    
    _ranges: {
            'daily': [dojo.date.dateParts.DAY, 1],
            'weekly': [dojo.date.dateParts.WEEK, 1],
            'biweekly': [dojo.date.dateParts.WEEK, 2],
            'monthly': [dojo.date.dateParts.MONTH, 1],
            'yearly': [dojo.date.dateParts.YEAR, 1]
    }
});