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
    
    isApplicableToMaster: function(){
        return true;        
    }, 
    
    isApplicableToThisAndFuture: function(){
       if (!note.hasRecurrence()){
           return false;
       } 
    }, 
    
    isApplicableToOccurence: function(){
       if (!note.hasRecurrence()){
           return false;
       } 
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