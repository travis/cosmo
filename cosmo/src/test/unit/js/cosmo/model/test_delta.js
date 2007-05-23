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
        
        var note = getSimpleEventNote();
        var stamp = note.getEventStamp();
        stamp.setStartDate(new cosmo.datetime.Date(2001,1,1,12,0));
        var delta = new cosmo.model.Delta(note);
        delta.addProperty("body","newBody" );
        delta.deltafy();
        
        //first ensure that only master gets returned for non-recurring events
        jum.assertTrue(setEquals({master:true}, delta.getApplicableChangeTypes()));
        
        //now ensure that all types are possible for a simple delta on a recurring item
        note.getEventStamp().setRrule(new cosmo.model.RecurrenceRule({
            frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY
        }));
        jum.assertTrue(setEquals({master:true, occurrence:true, occurrenceAndFuture:true}, 
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
