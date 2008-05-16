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

dojo.provide("cosmo.model.tests.model");
dojo.require("cosmo.model.util");
dojo.require("cosmo.model.common");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.EventStamp");

doh.register("cosmo.model.tests.model", [
    function declareStamp (){
        cosmo.model.declareStamp("TestStamp", "test", "namespace",
                                 [["testString", String, {"default" : "def"}],
                                  ["testArrayOfNumbers", [Array, Number], {"default" : function(){return [1,2,3];}}]],
                                 {
                                     constructor: function(kwArgs){
                                         this.initializeProperties(kwArgs);
                                     }});

        jum.assertFalse(typeof(TestStamp) == "undefined");
        var s = new TestStamp();
        jum.assertEquals(s.getTestString(), "def");
        jum.assertEquals([1,2,3].toString(), s.getTestArrayOfNumbers().toString());
        var attr = s.stampMetaData.getAttribute("testArrayOfNumbers");
        jum.assertEquals(attr.type[0], Array);
        jum.assertEquals(attr.type[1], Number);
    },

    function declareSeriesOnlyStamp(){
        cosmo.model.declareStamp("SeriesOnlyStamp", "seriesOnlyStamp", "namespace",
                                 [["testString", String, {}]],
                                 {
                                     initializer: function(kwArgs){
                                         this.initializeProperties(kwArgs);
                                     }},null,true);

        var note = new cosmo.model.Note();
        var occ = note.getNoteOccurrence( new cosmo.datetime.Date(2000,0,1));
        var stamp = occ.getStamp("seriesOnlyStamp", true);
        jum.assertTrue("stamp exists!", stamp != null);
        stamp.setTestString("seriesonly");

        stamp = note.getStamp("seriesOnlyStamp");
        jum.assertEquals("note stamp is the same as occ stamp", stamp.getTestString(), "seriesonly");
    },

    function addGetRemoveStamp (){
        var note = new cosmo.model.Note();
        var stamp = note.getStamp("event", true);
        stamp.setLocation("loco");
        stamp = null;
        stamp = note.getStamp("event");
        jum.assertEquals("loco", stamp.getLocation());
        note.removeStamp("event");
        stamp = null;
        stamp = note.getStamp("event");
        jum.assertTrue(stamp == null);

    },

    function addGetRemoveModification (){
    },

    function getEventStampGetTaskStamp (){
        var note = new cosmo.model.Note();
        var event = note.getEventStamp(true);
        var task = note.getTaskStamp(true);
        event.setLocation("loco");
        event = null;
        task = null;
        event = note.getEventStamp();
        task = note.getTaskStamp();
        jum.assertEquals("event", event.stampMetaData.stampName);
        jum.assertEquals("task", task.stampMetaData.stampName);
    },

    function noteOccurrence (){
        var note = new cosmo.model.Note({
            body: "body",
            uid: "123",
            displayName: "display"
        });

        var date = new cosmo.datetime.Date(2000,0,1);
        var noteOccurrence = note.getNoteOccurrence(date);

        jum.assertEquals(note.getBody(), noteOccurrence.getBody());

        noteOccurrence.setBody("new");
        jum.assertEquals(noteOccurrence.getBody(), "new");

        var mod = note.getModification(date);
        jum.assertTrue(mod != null);
        jum.assertEquals(mod.getModifiedProperties()["body"],"new");

        noteOccurrence.setDisplayName("newdis");
        jum.assertEquals(mod.getModifiedProperties()["body"], "new");
        jum.assertEquals(mod.getModifiedProperties()["displayName"], "newdis");
        jum.assertEquals(noteOccurrence.getDisplayName(), "newdis");
        jum.assertEquals(noteOccurrence.getUid(), "123");

    },

    function noteOccurrenceStamping(){
        var note = new cosmo.model.Note();
        jum.assertFalse("Master note should not have a task stamp", note.getTaskStamp());
        var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date());
        var taskStamp = occurrence.getTaskStamp(true);
        jum.assertTrue("Task stamp should have been returned", taskStamp);
        jum.assertTrue("Task belongs to occurrence", occurrence.getTaskStamp());
        jum.assertFalse("Master note should still not have a task stamp", note.getTaskStamp());
    },

    function equals (){
        var equals = cosmo.model.util.equals;
        jum.assertTrue(equals(1,1));
        var date1 = new cosmo.datetime.Date(2001,1,1);
        var date2 = new cosmo.datetime.Date(2001,1,1);
        jum.assertTrue(equals(date1,date2));

        date1.setMonth(2);
        jum.assertFalse(equals(date1,date2));

        date1 = new cosmo.datetime.Date(2001,1,1);
        date2 = new cosmo.datetime.Date(2001,1,1);

        jum.assertTrue(equals(date1,date2));
        date1.tzId= "blah";
        jum.assertFalse(equals(date1,date2));

        jum.assertFalse(equals(date1, null));
        jum.assertFalse(equals(null, date1));
        var caught = false;
        try {
            equals(1, "1");
        } catch (e){
            //operands must be of the same type
            caught = true;
        }
        jum.assertTrue(caught);

        // Test for handling weird line break char insertion in IE.
        if (dojo.isIE){
            jum.assertTrue(equals("One\r\nTwo", "One\nTwo"));
            jum.assertTrue(equals("One\nTwo", "One\r\nTwo"));
        }

    },

    function stampInheritance (){
        var note = new cosmo.model.Note({
            body: "body",
            uid: "123",
            displayName: "display"
        });
        var date = new cosmo.datetime.Date(2000,0,1);

        var eventStamp  = note.getEventStamp(true);
        eventStamp.setLocation("loco");
        var noteOccurrence = note.getNoteOccurrence(date);

        var occurStamp = noteOccurrence.getEventStamp();

        jum.assertEquals(occurStamp.getLocation(), "loco");
        jum.assertEquals(eventStamp.getLocation(), "loco");

        occurStamp.setLocation("Yo");
        jum.assertEquals(occurStamp.getLocation(), "Yo");
    },

    function hasRecurrence(){
        var note = new cosmo.model.Note({
            body: "body",
            uid: "123",
            displayName: "display"
        });
        jum.assertFalse(note.hasRecurrence());
        var stamp = note.getEventStamp(true);
        stamp.setRrule(new cosmo.model.RecurrenceRule({frequency:cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY}));
        jum.assertTrue(note.hasRecurrence());
        var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date());
        jum.assertTrue(occurrence.hasRecurrence());
    },

    function stampMetaData(){
        jum.assertEquals("event", cosmo.model.getStampMetaData("event").stampName);
    },

    function addressRecurring(){
        var note = new cosmo.model.Note({
            body: "body",
            uid: "123",
            displayName: "display"
        });
        var stamp = note.getEventStamp(true);
        stamp.setRrule(new cosmo.model.RecurrenceRule({frequency:cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY}));
        var occurrence = note.getNoteOccurrence(new cosmo.datetime.Date());
        stamp = occurrence.getMailStamp(true);
        stamp.setFromAddress("from");
        jum.assertTrue(stamp.getFromAddress() == "from");
    },

    function addDuration(){
        var date = new cosmo.datetime.Date(2000,0,1,12,0,0);
        var duration = new cosmo.model.Duration({year:1});
        date.addDuration(duration);
        jum.assertTrue(date.equals(new cosmo.datetime.Date(2001,0,1,12,0,0)));

        date = new cosmo.datetime.Date(2000,0,1,12,0,0);
        duration = new cosmo.model.Duration("P1W");
        date.addDuration(duration);
        jum.assertTrue(date.equals(new cosmo.datetime.Date(2000,0,8,12,0,0)));
    }

]);
