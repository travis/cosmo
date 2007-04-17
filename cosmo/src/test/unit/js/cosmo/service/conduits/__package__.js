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
 
dojo.provide("cosmoTest.service.conduits.*");
 
cosmoTest.service.conduits = {
    testAbstractConduit: function(conduit){
        
        var collectionUid = 'b6c2ac54-38b0-42c1-a874-d77b66b16d6b';
        conduit.getCollection(collectionUid);
        conduit.getCollection(collectionUid, {sync:true});
       
       
        conduit.getItems(collection, startTime, endTime);
        conduit.getItems(collection, startTime, endTime, {sync:true});

        conduit.saveItem(item);
        conduit.saveItem(item, {sync:true});
 
        conduit.removeItem(collection, item);
        conduit.removeItem(collection, item, {sync:true});

        conduit.getRecurrenceRules(events);
        conduit.getRecurrenceRules(events, {sync:true});

        conduit.saveRecurrenceRule(event, recurrenceRule);
        conduit.saveRecurrenceRule(event, recurrenceRule, {sync:true});

        conduit.expandEvents(events, startTime, endTime);
        conduit.expandEvents(events, startTime, endTime, {sync:true});

        conduit.saveNewEventBreakRecurrence(event, originalEventUid, originalEventEndDate);
        conduit.saveNewEventBreakRecurrence(event, originalEventUid, originalEventEndDate, {sync:true})

    },
    
    testAbstract
    
    testAbstractCurrentUserConduit: function(conduit){
        getCollections();
        getCollections(kwArgs);
    
        getPreference(/*String*/ key);
        getPreference(/*String*/ key, /*Hash*/ kwArgs);

        setPreference(/*String*/ key, /*String*/ value);
        setPreference(/*String*/ key, /*String*/ value, /*Hash*/ kwArgs);

        removePreference(/*String*/ key);
        removePreference(/*String*/ key, /*Hash*/ kwArgs);

        getPreferences();
        getPreferences(/*Hash*/ kwArgs);

        setPreferences(/*Hash<String, String>*/ preferences);
        setPreferences(/*Hash<String, String>*/ preferences, /*Hash*/ kwArgs);

        setMultiplePreferences(/*Hash<String, String>*/ preferences);
        setMultiplePreferences(/*Hash<String, String>*/ preferences, /*Hash*/ kwArgs);
        
    }
    
    
}