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

dojo.provide("cosmotest.model.test_eventStamp")
dojo.require("cosmo.model.util");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.EventStamp");

cosmotest.model.test_eventStamp = {
    test_getSetEndDate: function(){
        var note = cosmotest.model.test_eventStamp.getSimpleEventNote();
        var stamp = note.getEventStamp();
        var startDate = new cosmo.datetime.Date(2007,0,1);
        var endDate = new cosmo.datetime.Date(2007,0,2);
        stamp.setStartDate(startDate);
        stamp.setEndDate(endDate);
        jum.assertTrue(endDate.equals(stamp.getEndDate()));
        //assert that the duration is one day, in seconds
        jum.assertTrue(stamp.getDuration().equals(new cosmo.model.Duration({day:1})));
        
        //let's try that again, now with timezones!
        //1/1/2007 1pm
        startDate = new cosmo.datetime.Date(2007,0,1,13,0);
        startDate.tzId = "America/New_York";
        endDate = new cosmo.datetime.Date(2007,0,1,14,0);
        endDate.tzId = "America/New_York";
    
        stamp.setStartDate(startDate);
        stamp.setEndDate(endDate); 
        jum.assertTrue(endDate.equals(stamp.getEndDate()));
        jum.assertTrue(stamp.getDuration().equals(new cosmo.model.Duration({hour:1}))); 
    },
    
    test_occurenceInheritance: function(){
        var note = cosmotest.model.test_eventStamp.getSimpleEventNote();
        var startDate = new cosmo.datetime.Date(2007,0,1,13,0);
        var endDate = new cosmo.datetime.Date(2007,0,1,14,0);
        var stamp = note.getEventStamp(true);
        stamp.setStartDate(startDate);
        stamp.setEndDate(endDate);
    
        //Let's pretend that this note occurs weekly, so we'll get the next
        //occurrence, one that starts one week later.
        var occurrenceDate = new cosmo.datetime.Date(2007,0,8,13,0);
        var occurrence = note.getNoteOccurrence(occurrenceDate);
        var occurStamp = occurrence.getEventStamp();
        jum.assertTrue("Occurence start date is inherited.", occurrenceDate.equals(occurStamp.getStartDate()));
    
        //test overriding start on an occurrence
        var newOccurrenceStart = new cosmo.datetime.Date(2007,0,8,14,0);
        occurStamp.setStartDate(newOccurrenceStart);
        jum.assertTrue("Occurrence start date is overridden", newOccurrenceStart.equals(occurStamp.getStartDate()));
        
        //see if the enddate is set properly
        jum.assertEquals("Occurrence end date has changed properly", new cosmo.datetime.Date(2007,0,8,15,0),occurStamp.getEndDate());
        
        //make sure the original stamp didn't get messed with
        jum.assertTrue(stamp.getStartDate().equals(new cosmo.datetime.Date(2007,0,1,13,0)));
        
    },
    
    //make sure that recurrenceid's get updated properly.
    test_changingMasterStart: function(){
        var note = cosmotest.model.test_eventStamp.getSimpleEventNote();
        var startDate = new cosmo.datetime.Date(2007,0,1,13,0);
        var endDate = new cosmo.datetime.Date(2007,0,1,14,0);
        var stamp = note.getEventStamp(true);
        stamp.setStartDate(startDate);
        stamp.setEndDate(endDate);

        //Let's pretend that this note occurs weekly, so we'll get the next
        //occurrence, one that starts one week later.
        var occurrenceDate = new cosmo.datetime.Date(2007,0,8,13,0);
        var occurrence = note.getNoteOccurrence(occurrenceDate);
        var occurStamp = occurrence.getEventStamp();    
        occurStamp.setLocation("modlocation");
        
        //move the master startdate up one hour!
        var newStartDate = new cosmo.datetime.Date(2007,0,1,14,0);
        stamp.setStartDate(newStartDate);
        
        //let's make sure the recurrenceId changed! 
        //there should not be a modification for the old occurrence date
        var modification = note.getModification(occurrenceDate);
        jum.assertTrue(modification == null);
    
        //but there should be one on the 8th at 14:00
        var newOccurrenceDate = new cosmo.datetime.Date(2007,0,8,14,0);
        modification = note.getModification(newOccurrenceDate);
        jum.assertTrue(modification != null);

    
    },
    
    getSimpleEventNote: function(){
        var note = new cosmo.model.Note({
            body: "body",
            uid: "123",
            displayName: "display"
        });
    
        var eventStamp = note.getEventStamp(true);
        return note;
    }
}


