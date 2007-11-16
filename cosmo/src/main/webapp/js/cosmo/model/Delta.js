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
    
    removeStampProperty: function(stampName, propertyName){
        delete this._getStamp(stampName)[propertyName];
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
    
    getNote: function(){
        return this._note;  
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
    
    getStampProperties: function(stampName){
       var stamp = this._getStamp(stampName);
       if (!stamp){
           return null;
       }
       
       if (!this._isEmpty(stamp)){
           return stamp;
       }
       
       return null;
    },
    
    _isEmpty: function(object){
        for (var x in object){
            return false;
        }
        return true;
    },
      
    deltafy: function ( /*boolean?*/ looseStringComparisons){
        // summary: removes all properties which are the same as its note
        // description: removes all properties from the delta which are the same
        //              same as the properties in its note and its stamps,
        //              leaving you with just the delta, hence "deltafy"
        this._filterOutEqualProperties(this._note, this._propertyProps, looseStringComparisons);

        //if eventStamp startDate and endDate are present, convert endDate to duration
        if (this._stampProps["event"]){
            var eventStamp = this._stampProps["event"];
            if (eventStamp["startDate"] && eventStamp["endDate"]){
                var duration = null;
                var noteEStamp = this._note.getEventStamp();
                var doAddOneDayToDuration = false;
                var currentStateAllDay = null;
                var currentStateAnyTime = null;
                if (noteEStamp){
                    currentStateAllDay = noteEStamp.getAllDay();
                    currentStateAnyTime = noteEStamp.getAnyTime();

                }
                var deltaAllDay = eventStamp["allDay"];
                var deltaAnyTime = eventStamp["anyTime"];

                //note: the below logic looks weird, because it seems that deltaAllXXX is false,
                //that's the only way to get to the right-hand side of the ||, but then why do 
                //we check inside as well? And why write "deltaAllDay != false" 
                //as opposed to just "deltaAllDay"? The answer is because deltaAllDay could be "undefined" 
                //in which case things are ok, and we want to eval the right hand side, and undefined != false 
                var resultantAllDay = deltaAllDay 
                                    || (currentStateAllDay && (deltaAllDay != false));
                var resultantAnyTime = deltaAnyTime 
                                    || (currentStateAnyTime && (deltaAnyTime != false));
                doAddOneDayToDuration = resultantAllDay || resultantAnyTime;                              

                if (doAddOneDayToDuration){
                    var diff = cosmo.datetime.Date.diff(dojo.date.dateParts.DAY,
                        eventStamp["startDate"], eventStamp["endDate"]) + 1;
                    duration = new cosmo.model.Duration({day:diff} );
                } else {
                    duration = new cosmo.model.Duration(eventStamp["startDate"], eventStamp["endDate"] );
                }
                eventStamp["duration"] = duration;
                delete eventStamp["endDate"];
            }    
        }
        
        for (var stampName in this._stampProps){
            var stamp = this._note.getStamp(stampName);
            if (stamp == null){
                continue;
            }
            var stampChanges = this._stampProps[stampName];

            if (stampChanges == null){
                continue;
            }
            this._filterOutEqualProperties(stamp, stampChanges, looseStringComparisons);
        }
        
        for (var stampName in this._addedStamps){
            //if the note actually already has this stamp, no need for it to be in added stamps
            if (this._note.getStamp(stampName)){
                delete this._addedStamps[stampName];
            }    
        }
        
        for (var stampName in this._deletedStamps){
            //if the note doesn't have this stamp, no need for it to be in deleted stamps
            if (!this._note.getStamp(stampName)){
                delete this._deletedStamps[stampName];
            }    
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
         
        //triage status can only be applied to a single occurrence
        if (this.getProperty("triageStatus")){
            return {occurrence:true};
        }
        
        if (this._hasSeriesOnlyStampChange()){
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
        
        //if this is the first occurrence
        if (this._note.isMaster() || this._note.isFirstOccurrence()){
            //...then we can't do thisAndFuture
            return { master:true, occurrence:true};
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
        if (!this._note.hasRecurrence() && this._needsAutoTriage()){
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
        newNote.getEventStamp().setStartDate(this._note.recurrenceId);
        
        //set the old note's rrule end date to just before the break.
        var newEndDate = occurrence.getEventStamp().getStartDate().clone();
        newEndDate.add(dojo.date.dateParts.DAY, -1);
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
    
    toString: function(){
        var string = "note properties: \n";
        string += this._propsToString(this._propertyProps, "note");
        for (var stamp in this._stampProps){
            string += stamp + " properties: \n";
            string += this._propsToString(this._stampProps[stamp], stamp);
        }
        string += "Added Stamps: \n"
        for (var stamp in this._addedStamps){
            if (this._addedStamps[stamp]){
              string += "    " + stamp + "\n";
            }
        }

        string += "Deleted Stamps: \n"
        for (var stamp in this._deletedStamps){
            if (this._deletedStamps[stamp]){
              string += "    " + stamp + "\n";
            }
        }
        return string;
    },
    
    _propsToString: function(props, stampName){
        var string = "";
        for (var propName in props){
            string += "    " + propName + ": \n";
            string += "       ORIG:" + this._getOriginalValue(stampName, propName) + "\n";
            string += "     CHANGE:" + props[propName] + "\n";
        }
        return string;
    },
    
    _getOriginalValue: function(stampName, propName){
        var original = null;
        if (stampName && stampName != "note"){
            var stamp = this._note.getStamp(stampName);
            if (stamp){
                original = 
                    stamp[cosmo.model.util.getGetterAndSetterName(propName)[0]]();
            } else {
                original = undefined;
            }
        } else {
            original = this._note[cosmo.model.util.getGetterAndSetterName(propName)[0]]();
        }
        
        return original;
        
    },
    _needsAutoTriage: function(){
        //summary: determines whether auto triage might be needed. 
        //descripiton: If any properties which might cause a triage change have been changed,
        //              returns true. Called by autoTriage().
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
        
        this._applyStampAdditionsAndDeletions(type, note);
        
        this._applyProperties(note, this._propertyProps, type);
        
        for (var stampName in this._stampProps){
            var stampChanges = this._stampProps[stampName];
            if (stampChanges == null || this._isEmpty(stampChanges)){
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
    
    _applyStampAdditionsAndDeletions: function(type, note){
        note = type == "master" ? note.getMaster() : note;
        for (var stampName in this._deletedStamps){
            note.removeStamp(stampName);
        }

        for (var stampName in this._addedStamps){
            note.getStamp(stampName, true);
        }
    },
    
   _applyProperties: function(original,changes, type){
        for (var propName in changes){
           var changeValue = changes[propName];
           original.applyChange(propName, changeValue, type);
        }
    },
    
    _applyPropertiesToEventStamp: function(original, changes, type){
        //start date must be applied first so that duration can be calculated
        //properly
        changes = dojo.lang.shallowCopy(changes, false);
        if (changes["startDate"]){
           var changeValue = changes["startDate"];
           original.applyChange("startDate", changeValue, type);
           delete changes["startDate"];
        }
        
        if (changes["allDay"]){
           var changeValue = changes["allDay"];
           original.applyChange("allDay", changeValue, type);
           delete changes["allDay"];
        }
        
        this._applyProperties(original, changes, type);
    },
    
    _filterOutEqualProperties: function (original, changes, looseStringComparisons){
        for (var propName in changes){
            var changeValue = changes[propName];
            if (!original.isChanged(propName, changeValue, looseStringComparisons)){
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

    _hasSeriesOnlyStampChange: function(){
        var result = false;
        dojo.lang.map([this._addedStamps, this._deletedStamps, this._stampProps],
                      function(list){
                          for (var stampName in list){
                              if (cosmo.model.getStampMetaData(stampName).seriesOnly){
                                  result = true;
                              }}});
        return result;
    },
    
    _ranges: {
            'daily': [dojo.date.dateParts.DAY, 1],
            'weekly': [dojo.date.dateParts.WEEK, 1],
            'biweekly': [dojo.date.dateParts.WEEK, 2],
            'monthly': [dojo.date.dateParts.MONTH, 1],
            'yearly': [dojo.date.dateParts.YEAR, 1]
    }
});