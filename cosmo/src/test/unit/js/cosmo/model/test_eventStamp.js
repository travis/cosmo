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

dojo.require("cosmo.model.util");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.EventStamp");

test_getSetEndDate = function(){
    var note = getSimpleEventNote();
    var stamp = note.getEventStamp();
    var startDate = new cosmo.datetime.Date(2007,0,1);
    var endDate = new cosmo.datetime.Date(2007,0,2);
    stamp.setStartDate(startDate);
    stamp.setEndDate(endDate);
    jum.assertTrue(endDate.equals(stamp.getEndDate()));
    //assert that the duration is one day, in seconds
    jum.assertEquals(24 * 60 * 60, stamp.getDuration());
    
    //let's try that again, now with timezones!
    //1/1/2007 1pm
    startDate = new cosmo.datetime.Date(2007,0,1,13,0);
    startDate.tzId = "America/New_York";
    endDate = new cosmo.datetime.Date(2007,0,1,14,0);
    endDate.tzId = "America/New_York";

    stamp.setStartDate(startDate);
    stamp.setEndDate(endDate); 
    jum.assertTrue(endDate.equals(stamp.getEndDate()));
    jum.assertEquals(60 * 60, stamp.getDuration()); 
}

test_occurenceInheritance = function(){
    var note = getSimpleEventNote();
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
    jum.assertTrue(occurrenceDate.equals(occurStamp.getStartDate()));

    //TODO - test overriding start, end etc.
}

getSimpleEventNote = function(){
    var note = new cosmo.model.Note({
        body: "body",
        uid: "123",
        displayName: "display"
    });

    var eventStamp = note.getEventStamp(true);
    return note;
}



