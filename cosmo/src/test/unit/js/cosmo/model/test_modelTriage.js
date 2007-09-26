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

dojo.provide("cosmotest.model.test_modelTriage");
dojo.require("cosmo.model.util");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.EventStamp");

dojo.lang.mixin(cosmotest.model.test_modelTriage,{
    test_getSetTriage : function(){
        var note = new cosmo.model.Note();
        note.setTriageStatus(cosmo.model.TRIAGE_LATER);
        jum.assertEquals("Should be LATER", cosmo.model.TRIAGE_LATER, note.getTriageStatus());
        jum.assertEquals("Should be TRUE - auto triage defaults to true", true, note.getAutoTriage());
        note.setAutoTriage(false);

        var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date());
        jum.assertEquals("Should be NOW - occurrences do not inherit triage from the master item.", cosmo.model.TRIAGE_NOW, occurrence.getTriageStatus());
        jum.assertEquals("Should be TRUE - occurrences do not inherit autoTriage from the master item.", true, occurrence.getAutoTriage());

        occurrence.setTriageStatus(cosmo.model.TRIAGE_DONE);
        jum.assertEquals("Should be DONE - set on the modification.", cosmo.model.TRIAGE_DONE, occurrence.getTriageStatus());
        
        //sanity check - make sure master is still the same
        jum.assertEquals("Should be LATER", cosmo.model.TRIAGE_LATER, note.getTriageStatus());
    }, 
    
    test_implicitTriageOfOccurrences: function(){
        var note = new cosmo.model.Note();
        var eventStamp = note.getEventStamp(true);
        var duration = new cosmo.model.Duration({hour:1});
        eventStamp.setDuration(duration);
        
        var now = (new Date()).getTime();
        
        var startDate = new cosmo.datetime.Date();

        //let's make an occurrence that starts half an hour before now and lasts an hour.
        var halfAnHourBeforeNow = now - (1000 * 60 * 30);  
        startDate.updateFromUTC(halfAnHourBeforeNow);
        var nowOccurrence = note.getNoteOccurrence(startDate);
        jum.assertEquals("event is happening now", cosmo.model.TRIAGE_NOW, nowOccurrence.getTriageStatus());

        //let's make an occurrence that starts half an hour after now and lasts an hour.
        var halfAnHourAfterNow = now + (1000 * 60 * 30);  
        startDate.updateFromUTC(halfAnHourAfterNow);
        var laterOccurrence = note.getNoteOccurrence(startDate);
        jum.assertEquals("event is happening later", cosmo.model.TRIAGE_LATER, laterOccurrence.getTriageStatus());

        //let's make an occurrence that starts two hours before now and lasts an hour.
        var twoHoursBeforeNow = now - (1000 * 60 * 2 * 60);
        startDate.updateFromUTC(twoHoursBeforeNow);
        var doneOccurrence = note.getNoteOccurrence(startDate);
        jum.assertEquals("event happened", cosmo.model.TRIAGE_DONE, doneOccurrence.getTriageStatus());

        //now let's assume Chandler synced and set lastPastOccurrence
        startDate.updateFromUTC(halfAnHourBeforeNow);
        eventStamp.setLastPastOccurrence(startDate);
        jum.assertEquals("event is happening now, but Chandler marked done", 
                         cosmo.model.TRIAGE_DONE, nowOccurrence.getTriageStatus());
        
        startDate.updateFromUTC(halfAnHourAfterNow);
        eventStamp.setLastPastOccurrence(startDate);
        jum.assertEquals("event is happening later and Chandler synced", cosmo.model.TRIAGE_LATER, laterOccurrence.getTriageStatus());

        startDate.updateFromUTC(twoHoursBeforeNow);
        eventStamp.setLastPastOccurrence(startDate);
        jum.assertEquals("event happened and Chandler synced", cosmo.model.TRIAGE_DONE, doneOccurrence.getTriageStatus());
    },
    
    test_autotriage: function(){
        var now = (new Date()).getTime();
        var note = new cosmo.model.Note();
        var triaged = note.autoTriage();
        jum.assertEquals("note w/ no event stamp should not auto triage", false, triaged);
        
        var eventStamp = note.getEventStamp(true);
        eventStamp.setDuration(new cosmo.model.Duration({hour:1}));
        var startDate = new cosmo.datetime.Date(); 

        var twoHoursBeforeNow = now - (1000 * 60 * 2 * 60);
        startDate.updateFromUTC(twoHoursBeforeNow);
        eventStamp.setStartDate(startDate);
        triaged = note.autoTriage();
        jum.assertEquals("note in the past should auto-triage to DONE", true, triaged);
        jum.assertEquals("note in the past should auto-triage to DONE", cosmo.model.TRIAGE_DONE, note.getTriageStatus());
    }
});
