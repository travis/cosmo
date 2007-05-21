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
    
    deltafy: function (){
        // summary: removes all properties which are the same as its note
        // description: removes all properties from the delta which are the same
        //              same as the properties in its note and its stamps,
        //              leaving you with just the delta, hence "deltafy"
        this._filterOutEqualProperties(this._note, this._propertyProps);
        for (var stampName in this._stampProps){
            var stamp = this._note.getStamp(stampName);
            var stampChanges = this._stampProps[stamp];
            this._filterOutEqualProperties(stamp, stampChanges);
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
    }
});