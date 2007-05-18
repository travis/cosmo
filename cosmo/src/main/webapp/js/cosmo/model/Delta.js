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
    initializer: function(){
        this._stampProps = {}
        this._propertyProps = {};
    },
    
    addStampProperty: function (stampName, propertyName, value){
        var stamp = this._getStamp(stampName);
        stamp[propertyName] = value;
    },
    
    addProperty: function (propertyName, value){
        this._propertyProps[propertyName] = value;
    },
    
    deltafy: function (/*cosmo.model.Note*/ note){
        // summary: removes all properties which are the same as given note
        // description: removes all properties from the delta which are the same
        //              same as the properties in the given note and its stamps,
        //              leaving you with just the delta, hence "deltafy"
        this._filterOutEqualProperties(note, this._propertyProps);
        //XXX
        
    },
    
    _filterOutEqualProperties: function (original, changes){
        for (var propName in changes){
            var changeValue = changes[propName];
            var origValue = this._getPropertyUsingGetter(original, propName);
            if (cosmo.model.equals(changeValue, origValue)){
                delete changes[propName];
            } 
        }
    },
    
    _getPropertyUsingGetter: function (object, propertyName){
        
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