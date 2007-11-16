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

dojo.provide("cosmotest.model.test_delta");
dojo.require("cosmo.model.util");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.EventStamp");


dojo.lang.mixin(cosmotest.model.test_delta, {
    test_simpleDelta : function(){
        var note = getSimpleEventNote();
        var delta = new cosmo.model.Delta(note);
        delta.addProperty("body", "body");
        delta.addProperty("displayName", "new");
        delta.addStampProperty("fake", "property", "proper");
        delta.addStampProperty("fake", "nullProperty", null);

        jum.assertEquals("new", delta.getProperty("displayName"));
        jum.assertFalse(delta.getProperty("body") == undefined);

        delta.deltafy();

        jum.assertEquals("new", delta.getProperty("displayName"));
        jum.assertTrue(delta.getProperty("body") == undefined);
        jum.assertFalse(delta.isPropertyChanged("body"));
        
        jum.assertTrue("The 'property' property should be changed in 'fake' stamp.",delta.isStampPropertyChanged("fake", "property"));
        jum.assertTrue(delta.isStampPropertyChanged("fake", "nullProperty"));
        jum.assertFalse(delta.isStampPropertyChanged("fake", "notAProp"));
        
        var stamp = note.getEventStamp(true);
        stamp.setRrule(new cosmo.model.RecurrenceRule({
            frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY
        }));
        delta.addStampProperty("event", "rrule",new cosmo.model.RecurrenceRule({
            frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY
        }));
        delta.deltafy();        
        jum.assertFalse("RRule should not be changed",delta.isStampPropertyChanged("event", "rrule"));
        
    },

    test_getApplicableChangeTypes: function(){
        
        var note = getSimpleEventNote();
        var stamp = note.getEventStamp();
        stamp.setStartDate(new cosmo.datetime.Date(2001,1,1,12,0));
        var delta = new cosmo.model.Delta(note);
        delta.addProperty("body","newBody" );
        delta.deltafy();
        
        //first ensure that only master gets returned for non-recurring events
        jum.assertTrue("master only", setEquals({master:true}, delta.getApplicableChangeTypes()));
        
        //now ensure that all types are possible for a simple delta on a recurring item
        note.getEventStamp().setRrule(new cosmo.model.RecurrenceRule({
            frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY
        }));
        jum.assertTrue("simple recurring delta", setEquals({master:true, occurrence:true}, 
            delta.getApplicableChangeTypes()));
        
        //now the case where we remove the recurrence rule
        delta.addStampProperty("event", "rrule", null);
        delta.deltafy();
        jum.assertTrue("only master allowed when removing the recurrence rule.",setEquals({master:true}, delta.getApplicableChangeTypes()));
        
        //now the case where we modify the recurrence rule, but only the endDate (not frequency)
        delta.addStampProperty("event", "rrule", new cosmo.model.RecurrenceRule({
            frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY,
            endDate: new cosmo.datetime.Date(2000,1,1)}));
        delta.deltafy();
        jum.assertTrue("only master allowed when recurrence end date is modified, but not the frequency", 
            setEquals({master:true}, delta.getApplicableChangeTypes()));
        
        //the frequency has changed, and this is the master event
        delta.addStampProperty("event", "rrule", new cosmo.model.RecurrenceRule({
            frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_WEEKLY
        }));
        delta.deltafy();
        jum.assertTrue("only master allowed when freq.is modified, and is master", 
            setEquals({master:true}, delta.getApplicableChangeTypes()));

        //the frequency has changed, and this is the first occurrence
        var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date(2001,1,1,12,0));
        delta = new cosmo.model.Delta(occurrence);
        delta.addStampProperty("event", "rrule", new cosmo.model.RecurrenceRule({
            frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_WEEKLY
        }));
        jum.assertTrue("only master allowed when freq.is modified, and is first occurrence", 
            setEquals({master:true}, delta.getApplicableChangeTypes()));
        
        //the frequency has changed, and this is NOT first occurrence
        var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date(2001,1,2,12,0));
        delta = new cosmo.model.Delta(occurrence);
        delta.addStampProperty("event", "rrule", new cosmo.model.RecurrenceRule({
            frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_WEEKLY
        }));
        jum.assertTrue("'master' and 'occurrenceAndFuture' are allowed when rrule frequency has changed and it is NOT the first occurrence", 
            setEquals({master:true, occurrenceAndFuture:true}, delta.getApplicableChangeTypes()));
        
        //the start date has moved beyond the recurrence interval
        var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date(2001,1,2,12,0));
        delta = new cosmo.model.Delta(occurrence);
        delta.addStampProperty("event", "startDate", new cosmo.datetime.Date(2001,1,4,12,0));
        jum.assertTrue("only 'occurrence' allowed when you move the start date beyond the recurrence interval range", 
            setEquals({occurrence:true}, delta.getApplicableChangeTypes()));
        
        //sanity check: move the start date just a little, well within recurrence interval
        var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date(2001,1,2,12,0));
        delta = new cosmo.model.Delta(occurrence);
        delta.addStampProperty("event", "startDate", new cosmo.datetime.Date(2001,1,2,13,0));
        jum.assertTrue("just moving the start date up a tad, ALL change types should be allowed.", 
            setEquals({ master:true, occurrenceAndFuture:true, occurrence:true}, delta.getApplicableChangeTypes()));
   },
 
   test_applyDeltaToMaster: function(){
       //creates a basic event, recurs daily, starts on 1/1/2000 12pm
       var getBaseNote = cosmotest.model.test_delta.getBaseNote;

      //first test that setting a properties on the base object and stamps works.
       var note = getBaseNote();
       var delta = new cosmo.model.Delta(note);
       delta.addProperty("body", "newBody");
       delta.addStampProperty("event", "location", "loco");
       delta.deltafy();
       delta.applyToMaster();
       jum.assertEquals("Applying a master change to a master object.", "newBody",  note.getBody() );     
       jum.assertEquals("Applying a master change to a master object.", "loco",  note.getEventStamp().getLocation() );     
   },
   
   //test adding stamp to master (from a occurrence delta)

   //test adding stamp to occurrence
   test_addStampToOccurrence: function(){
       var getBaseNote = cosmotest.model.test_delta.getBaseNote;
       var note = getBaseNote();
       var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date(2000,0,10,12,0));
       
       var delta = new cosmo.model.Delta(occurrence);
       delta.addAddedStamp("task");
       delta.deltafy();
       delta.applyToOccurrence();
       jum.assertTrue(occurrence.getTaskStamp());
   },
   
   //test editing start date on master, from an occurrence delta
   test_applyStartDateToMasterFromOccurrence: function(){
       var getBaseNote = cosmotest.model.test_delta.getBaseNote;
       var note = getBaseNote();
       var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date(2000,0,10,12,0));
       //sanity check
       jum.assertTrue((new cosmo.datetime.Date(2000,0,10,12,0))
            .equals(occurrence.getEventStamp().getStartDate()));
       var delta = new cosmo.model.Delta(occurrence);
       delta.addStampProperty("event", "startDate", new cosmo.datetime.Date(2000,0,10,13,0));
       delta.deltafy(occurrence);
       delta.applyToMaster();
       jum.assertTrue((new cosmo.datetime.Date(2000,0,1,13,0))
            .equals(note.getEventStamp().getStartDate()));
       
       
       //now let's try changing the timezone.
       var note = getBaseNote();
       var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date(2000,0,10,12,0));
       var newStartDate = new cosmo.datetime.Date(2000,0,10,13,0);
       newStartDate.tzId = "America/Los_Angeles";
       var delta = new cosmo.model.Delta(occurrence);
       delta.addStampProperty("event", "startDate", newStartDate);
       delta.deltafy(occurrence);
       delta.applyToMaster();
       var newMasterDate = new cosmo.datetime.Date(2000,0,1,13,0);
       newMasterDate.tzId = "America/Los_Angeles";
       jum.assertTrue("Timezone change failed.", newMasterDate
            .equals(note.getEventStamp().getStartDate()));
       
       
   },
   
   test_applyDeltaToOccurrenceTimeChanges: function(){
        var getBaseNote = cosmotest.model.test_delta.getBaseNote;
       //let's try changing start date AND end date on an occurrence
       var note = getBaseNote();
       var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date(2000,0,10,12,0));
       
       //we'll move it up one hour.
       var newStartDate = new cosmo.datetime.Date(2000,0,10,13,0);
       var newEndDate = new cosmo.datetime.Date(2000,0,10,14,0);  
       var delta = new cosmo.model.Delta(occurrence);
       delta.addStampProperty("event", "startDate", newStartDate);
       delta.addStampProperty("event", "endDate", newEndDate);
       delta.deltafy();
       delta.applyToOccurrence();
       jum.assertEquals("Start date, set correctly",
         occurrence.getEventStamp().getStartDate(), new cosmo.datetime.Date(2000,0,10,13,0));
       jum.assertEquals("End Date, set correctly",
         occurrence.getEventStamp().getEndDate(), new cosmo.datetime.Date(2000,0,10,14,0));
       
       //move it back an hour, but set the endDate the same - should be deltafy'd out
       var newStartDate = new cosmo.datetime.Date(2000,0,10,12,0);
       var newEndDate = new cosmo.datetime.Date(2000,0,10,14,0);  
       var delta = new cosmo.model.Delta(occurrence);
       delta.addStampProperty("event", "startDate", newStartDate);
       delta.addStampProperty("event", "endDate", newEndDate);
       delta.deltafy();
       delta.applyToOccurrence();
       jum.assertEquals("Start date, set correctly after second change",
         occurrence.getEventStamp().getStartDate(), new cosmo.datetime.Date(2000,0,10,12,0));
       jum.assertEquals("End Date, set correctly after second change",
         occurrence.getEventStamp().getEndDate(), new cosmo.datetime.Date(2000,0,10,14,0));
       
   },
   
   test_addTaskStampToMaster: function(){
       //added for bug 9885
       var note = new cosmo.model.Note();
       var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date());
       var delta = new cosmo.model.Delta(occurrence);
       delta.addAddedStamp("task");
       delta.applyToMaster();
       jum.assertTrue("Note should have task stamp", !!note.getTaskStamp());
   },
   
   test_makeItemAllDay: function(){
       //added for bug 10549
       
       //first try without the item having an EventStamp before the delta.
       var note = new cosmo.model.Note();
       var delta = new cosmo.model.Delta(note);
       delta.addAddedStamp("event");
       delta.addStampProperty("event", "allDay", true);
       delta.addStampProperty("event", "startDate", new cosmo.datetime.Date(2007,0,1));
       delta.addStampProperty("event", "endDate", new cosmo.datetime.Date(2007,0,1));
       delta.deltafy();
       var stampDuration = delta.getStampProperty("event", "duration")
       jum.assertTrue("stamp duration is one day", stampDuration.getDay() == 1 
           && stampDuration.getMultiplier() == 1);
       delta.applyToMaster();
       var noteDuration = note.getEventStamp().getDuration();
       jum.assertTrue("note duration is one day", noteDuration.getDay() == 1 
           && noteDuration.getMultiplier() == 1);
       jum.assertTrue("end date is properly set", note.getEventStamp().getEndDate().equals(note.getEventStamp().getStartDate()));

       var note = new cosmo.model.Note();
       var es = note.getEventStamp(true);
       es.setStartDate(new cosmo.datetime.Date(2007,0,1,12,0))
       es.setEndDate(new cosmo.datetime.Date(2007,0,1,12,30))
       var delta = new cosmo.model.Delta(note);
       delta.addStampProperty("event", "allDay", true);
       delta.addStampProperty("event", "startDate", new cosmo.datetime.Date(2007,0,1));
       delta.addStampProperty("event", "endDate", new cosmo.datetime.Date(2007,0,1));
       delta.deltafy();
       var stampDuration = delta.getStampProperty("event", "duration")
       jum.assertTrue("stamp duration is one day", stampDuration.getDay() == 1 
           && stampDuration.getMultiplier() == 1);
       delta.applyToMaster();
       var noteDuration = note.getEventStamp().getDuration();
       jum.assertTrue("note duration is one day", noteDuration.getDay() == 1 
           && noteDuration.getMultiplier() == 1);
       jum.assertTrue("end date is properly set", note.getEventStamp().getEndDate().equals(note.getEventStamp().getStartDate()));

   },
   
    test_seriesOnlyDelta: function(){
        var note = new cosmo.model.Note();
        var delta = new cosmo.model.Delta(note);
        delta.addStampProperty("mail", "blah");
        delta.deltafy();
        var changes = delta.getApplicableChangeTypes();
        jum.assertTrue("Master only for series only stamp", setEquals(changes, {master:true}))
    },
 
  //TODO - test editing end date on master, from an occurrence delta
   
   //creates a basic event, recurs daily, starts on 1/1/2000 12pm
   getBaseNote: function(){
       var note = getSimpleEventNote();
       var stamp = note.getEventStamp(true);
       stamp.setStartDate(new cosmo.datetime.Date(2000,0,1,12,0));
       stamp.setEndDate(new cosmo.datetime.Date(2000,0,1,13,0));
       stamp.setRrule(new cosmo.model.RecurrenceRule({
           frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY
       }));
       return note;
   }
});


function getSimpleEventNote(){
    var note = new cosmo.model.Note({
        body: "body",
        uid: "123",
        displayName: "display"
    });

    var eventStamp = note.getEventStamp(true);
    return note;
}

function setEquals(set1, set2){
     var types = ["occurrence", "master", "occurrenceAndFuture"]
     for (var x = 0; x < types.length; x++ ){
         var type = types[x];
         if (!!set1[type] != !!set2[type]){
             return false;
         }
     }
     return true;
}
        