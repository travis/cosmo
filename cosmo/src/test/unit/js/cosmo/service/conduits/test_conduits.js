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

dojo.provide("cosmotest.service.conduits.test_conduits");

dojo.require("cosmotest.testutils");
dojo.require("cosmo.service.conduits.common");
dojo.require("cosmo.cmp");
dojo.require("cosmo.util.auth");
dojo.require("dojo.lang.*");

cosmotest.service.conduits.test_conduits = {
    test_Note: function(){
        try{
            var user = cosmotest.testutils.createTestAccount();
            // number of starting items;
            var startingItems = 3;
            // test getCollections
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            var collections = conduit.getCollections({sync: true}).results[0];
            jum.assertTrue("collections", !!collections);
            jum.assertTrue("collections length", collections.length > 0);
            var c0 = collections[0];
            // test lazy loading
            jum.assertTrue("lazy loading on get broken", !!c0.getUrls())
            
            collections = conduit.getCollections({sync: true}).results[0];
            c0 = collections[0];
            
            c0.setDisplayName("bork bork bork");
            jum.assertEquals("lazy loading before set broken", "bork bork bork", c0.getDisplayName());
            
           // test getCollection
            var collectionDetails = conduit.getCollection("collection/" + c0.getUid(), {sync: true});
            jum.assertTrue("collectionDetails", !!collectionDetails)

            // Test createItem
            var newItemDisplayName = "Testing display name";
            var newItemBody = "Testing message body";
            var newItemTriageStatus = 100;
            var newItemTriageRank = -12345.67;
            var newItemAutoTriage = 1;            
            var newItem = new cosmo.model.Note(
            {
                displayName: newItemDisplayName,
                triageStatus: newItemTriageStatus,
                rank: newItemTriageRank,
                autoTriage: newItemAutoTriage,
                body: newItemBody
            }
            );
            
            conduit.createItem(newItem, c0, {sync: true});
            
            var items = conduit.getItems(c0, {}, {sync: true}).results[0];

            jum.assertTrue("items", !!items);
            jum.assertEquals("items length", startingItems + 1, items.length);

            jum.assertTrue("no edit link on item", !!newItem.getUrls()['atom-edit']);

            var item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0];
            
            jum.assertEquals("new item display name", newItemDisplayName, item0.getDisplayName());
            jum.assertEquals("triage status", newItemTriageStatus, item0.getTriageStatus());
            jum.assertEquals("triage rank", newItemTriageRank, item0.getRank());
            jum.assertEquals("auto triage", newItemAutoTriage, item0.getAutoTriage());
            jum.assertEquals("body", newItemBody, item0.getBody());

            // Test saveItem
            var item0DisplayName = "New Display Name";
            item0.setDisplayName(item0DisplayName);

            conduit.saveItem(item0, {sync: true});
            item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0];
     
            jum.assertEquals("item display name", item0DisplayName, item0.getDisplayName());
            
            // Test getItems
            conduit.createItem(new cosmo.model.Note(
            {
                displayName: "Testing display name 2"
            }
            ), c0, {sync: true});

            var items = conduit.getItems(c0, {}, {sync: true}).results[0];

            jum.assertTrue("items", !!items);
            jum.assertEquals("items length", startingItems + 2, items.length);
            
            // Test deleteItem 

            conduit.deleteItem(item0, {sync: true});
            
            items = conduit.getItems(c0, {}, {sync: true}).results[0];
            jum.assertTrue("deleteItem: items", !!items);
            jum.assertEquals("deleteItem: items length", startingItems + 1, items.length);
            
            
            // Test dashboard projections
            var nowItem = new cosmo.model.Note(
            {
                displayName: "Now Item",
                triageStatus: 100,
                rank: 0,
                autoTriage: 0,
                body: "Now Item"
            }
            );
            
            conduit.createItem(nowItem, c0, {sync: true});

            // Test dashboard projections
            var laterItem = new cosmo.model.Note(
            {
                displayName: "Later Item",
                triageStatus: 200,
                rank: 0,
                autoTriage: 0,
                body: "Later Item"
            }
            );
            
            conduit.createItem(laterItem, c0, {sync: true});

            // Test dashboard projections
            var doneItem = new cosmo.model.Note(
            {
                displayName: "Done Item",
                triageStatus: 300,
                rank: 0,
                autoTriage: 0,
                body: "Done Item"
            }
            );
            
            conduit.createItem(doneItem, c0, {sync: true});

            var itemsDeferred = conduit.getDashboardItems(c0, {sync: true});
            itemsDeferred.addCallback(function(dashboardItems){
                jum.assertEquals("dashboard items length wrong", 7, dashboardItems.length);
            });
            
            // TODO add asserts

            // Unicode tests
            var unicodeItem = new cosmo.model.Note(
            {
                displayName: "åß∂ƒ©˙∆˚¬…\u2028\u2029",
                body: "åß∂ƒ©˙∆˚¬…\u2028\u2029"
            });
            var unicodeDeferred = conduit.createItem(unicodeItem, c0, {sync: true});
            var serverUnicodeItemDeferred = conduit.getItem(unicodeItem.getUid(), 
                {sync: true});
            serverUnicodeItemDeferred.addCallback(function(serverUnicodeItem){
                jum.assertEquals("unicode displayName wrong", 
                                 unicodeItem.getDisplayName(), serverUnicodeItem.getDisplayName());
                jum.assertEquals("unicode body wrong", 
                                 unicodeItem.getBody(), serverUnicodeItem.getBody());
            });
        }
        finally{
            cosmotest.testutils.cleanupUser(user);            
        }
    },
    
    test_Event: function(){
        try {
            var user = cosmotest.testutils.createTestAccount();
            
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            var collections = conduit.getCollections({sync: true}).results[0];
            
            var c0 = collections[0];
            
            var newItem = new cosmo.model.Note(
            {
                displayName: "Blah blah blah"
            }
            );
            
            var startDate = new cosmo.datetime.Date(2007, 5, 10, 12, 30, 45);
            startDate.setMilliseconds(0);

            var duration = new cosmo.model.Duration({hour: 1});
            var loc = "Wherever";
            var stat = "CONFIRMED";
            newItem.getEventStamp(true, {
                startDate: startDate,
                duration: duration,
                location: loc,
                status: stat
            });

            conduit.createItem(newItem, c0, {sync: true});

            var item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0];
            jum.assertTrue("start date", startDate.equals(item0.getEventStamp().getStartDate()));
            jum.assertTrue("duration", duration.equals(item0.getEventStamp().getDuration()));
            jum.assertEquals("location", loc, item0.getEventStamp().getLocation());
            jum.assertEquals("status", stat, item0.getEventStamp().getStatus());
            
            item0.getEventStamp().setAnyTime(true);
            item0.getEventStamp().setLocation("My place");
            conduit.saveItem(item0, {sync: true});
            
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);

            item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0];
            jum.assertTrue("post-anytime start date", startDate.equals(item0.getEventStamp().getStartDate()));
            jum.assertEquals("location", "My place", item0.getEventStamp().getLocation());
            jum.assertTrue("anytime", item0.getEventStamp().getAnyTime());
            
            item0.getEventStamp().setAllDay(true);
            item0.getEventStamp().setAnyTime(false);
            conduit.saveItem(item0, {sync: true});
            item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0];
            jum.assertTrue("post-allday start date", startDate.equals(item0.getEventStamp().getStartDate()));
            jum.assertTrue("allday", item0.getEventStamp().getAllDay());
            
            // Test recurrence
            item0.getEventStamp().setAllDay(false);
            item0.getEventStamp().setRrule(
               new cosmo.model.RecurrenceRule({frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY})
            );
            conduit.saveItem(item0, {sync: true});

            var item0Occurrences = conduit.getItems(c0, 
               {start: new cosmo.datetime.Date(2007, 5, 10),
                end: new cosmo.datetime.Date(2007, 5, 17)}, 
               {sync: true}
            ).results[0];
            jum.assertTrue("no rrule", !!item0Occurrences[0].getMaster().getEventStamp().getRrule())            
            jum.assertEquals("wrong number of occurrences", 7, item0Occurrences.length);
            //TODO
            item0Occurrences = conduit.expandRecurringItem(item0, 
               new cosmo.datetime.Date(2007, 5, 10),
               new cosmo.datetime.Date(2007, 5, 17), 
               {sync: true}
            ).results[0];
            jum.assertEquals("wrong number of occurrences", 7, item0Occurrences.length);

            // Test dashboard projection for collection
            var deferredItemsDeferred = conduit.getDashboardItems(c0, {sync: true});
            deferredItemsDeferred.addCallback(function(dashboardItems){
                jum.assertEquals("dashboard items length wrong", 5, dashboardItems.length);
            });

            
            var item4 = item0Occurrences[3];
            var item4Rid = item4.recurrenceId;
            var newDisplayName = "Ze New Name"
            var item4Modification = new cosmo.model.Modification({
                recurrenceId: item4.recurrenceId,
                modifiedProperties: {displayName: newDisplayName}
            });
            item4.getMaster().addModification(item4Modification);
            jum.assertEquals("modification display name wrong", newDisplayName, item4.getDisplayName());
            
            conduit.createItem(item4, c0, {sync:true});
            // Make sure changes stuck
            item0Occurrences = conduit.getItems(c0, 
               {start: new cosmo.datetime.Date(2007, 5, 10),
                end: new cosmo.datetime.Date(2007, 5, 17)}, 
               {sync: true}
            ).results[0];

            item4 = item0Occurrences[0].getMaster().getNoteOccurrence(item4Rid);

            jum.assertEquals("modification display name didn't save", newDisplayName, item4.getDisplayName());


            var anotherNewDisplayName = "Another new name";
            item4.setDisplayName(anotherNewDisplayName);
            conduit.saveItem(item4, {sync: true});
            
            // Make sure changes stuck
            item0Occurrences = conduit.getItems(c0, 
               {start: new cosmo.datetime.Date(2007, 5, 10),
                end: new cosmo.datetime.Date(2007, 5, 17)}, 
               {sync: true}
            ).results[0];
            
            item4 = item0Occurrences[0].getMaster().getNoteOccurrence(item4Rid);
            jum.assertEquals("modification display name didn't save", anotherNewDisplayName, item4.getDisplayName());


            conduit.deleteItem(item4, {sync: true});
            
            // Make sure changes stuck
            item0Occurrences = conduit.getItems(c0, 
               {start: new cosmo.datetime.Date(2007, 5, 10),
                end: new cosmo.datetime.Date(2007, 5, 17)}, 
               {sync: true}
            ).results[0];
            
            item4 = item0Occurrences[0].getMaster().getNoteOccurrence(item4Rid);
            jum.assertEquals("modification deletion didn't work", item4.getMaster().getDisplayName(), item4.getDisplayName());
            
            // Test for bug 9693
            item4.getMaster().getTaskStamp(true);
            conduit.saveItem(item4.getMaster(), {sync: true});
            
            jum.assertTrue("task stamping didn't work", !!item4.getMaster().getTaskStamp());

            item4.setDisplayName("booshark");
            conduit.createItem(item4, c0, {sync:true});
            
            jum.assertTrue("bug 9693", !!item4.getTaskStamp());
            
            // Test stamp deletion
            var masterUid = item4.getMaster().getUid()
            item4.getMaster().removeStamp('event');
            jum.assertTrue("Data model stamp removal didn't work", !item4.getMaster().getStamp('event'))
            conduit.saveItem(item4.getMaster(), {sync: true});
            
            var masterItem = conduit.getItem(masterUid, {sync: true}).results[0];

            jum.assertTrue("Removing stamp didn't work", !masterItem.getStamp('event'));

        } finally {
           cosmotest.testutils.cleanupUser(user);            
        }
    },
    
    test_ThisAndFutureCal: function(){
        try {
            var user = cosmotest.testutils.createTestAccount();
            
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            var collections = conduit.getCollections({sync: true}).results[0];
            
            var c0 = collections[0];
            
            var newItem = new cosmo.model.Note(
            {
                displayName: "Blah blah blah"
            }
            );
            
            var startDate = new cosmo.datetime.Date(2007, 5, 10, 12, 30, 45);
            startDate.setMilliseconds(0);

            var duration = new cosmo.model.Duration({hour: 1});
            var loc = "Wherever";
            var stat = "CONFIRMED";
            newItem.getEventStamp(true, {
                startDate: startDate,
                duration: duration,
                location: loc,
                status: stat,
                rrule: new cosmo.model.RecurrenceRule({frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY})
            });

            conduit.createItem(newItem, c0, {sync: true});

            var item0Occurrences = conduit.getItems(c0, 
               {start: new cosmo.datetime.Date(2007, 5, 10),
                end: new cosmo.datetime.Date(2007, 5, 17)}, 
               {sync: true}
            ).results[0];
            jum.assertTrue("no rrule", !!item0Occurrences[0].getMaster().getEventStamp().getRrule())            
            jum.assertEquals("wrong number of occurrences", 7, item0Occurrences.length);
            
            var occurrenceToBreakOn = 
                item0Occurrences[0].getMaster().getNoteOccurrence(new cosmo.datetime.Date(2007, 5, 13, 12, 30, 45));
            var newMaster = occurrenceToBreakOn.getMaster().clone();
            newMaster.setDisplayName("Bop bop a lee bop");
            newMaster.getEventStamp().setStartDate(occurrenceToBreakOn.getEventStamp().getStartDate());
            dojo.require("cosmo.util.uuid");
            var gen = new cosmo.util.uuid.RandomGenerator()
            var newUid = gen.generate();
            newMaster.setUid(newUid);
            conduit.saveThisAndFuture(occurrenceToBreakOn, newMaster, {sync: true})
            
            var newMasterOccurrences = conduit.expandRecurringItem(newMaster, 
               new cosmo.datetime.Date(2007, 5, 10),
               new cosmo.datetime.Date(2007, 5, 17), 
               {sync: true}).results[0];
            jum.assertEquals("wrong number of newMaster occurrences", 4, newMasterOccurrences.length);
            
            var item0Occurrences = conduit.expandRecurringItem(occurrenceToBreakOn.getMaster(), 
               new cosmo.datetime.Date(2007, 5, 10),
               new cosmo.datetime.Date(2007, 5, 17), 
               {sync: true}
            ).results[0];
            jum.assertEquals("wrong number of old item occurrences", 3, item0Occurrences.length);

        } finally {
           cosmotest.testutils.cleanupUser(user);            
        }
    },

    test_ThisAndFutureDash: function(){
        try {
            var user = cosmotest.testutils.createTestAccount();
            
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            var collections = conduit.getCollections({sync: true}).results[0];
            
            var c0 = collections[0];
            
            var newItem = new cosmo.model.Note(
            {
                displayName: "Blah blah blah"
            }
            );
            
            var startDate = new cosmo.datetime.Date(2007, 5, 10, 12, 30, 45);
            startDate.setMilliseconds(0);

            var duration = new cosmo.model.Duration({hour: 1});
            var loc = "Wherever";
            var stat = "CONFIRMED";
            newItem.getEventStamp(true, {
                startDate: startDate,
                duration: duration,
                location: loc,
                status: stat,
                rrule: new cosmo.model.RecurrenceRule({frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY})
            });

            conduit.createItem(newItem, c0, {sync: true});

            console.log("futz");
            var c0Occurrences = conduit.getDashboardItems(c0, 
               {sync: true}
            ).results[0];
            var items = dojo.lang.filter(c0Occurrences, function(item){return item.getUid() == newItem.getUid()});
            var item = dojo.lang.filter(items, function(item){return item.getTriageStatus() == 200})[0];
            var newMaster = item.getMaster().clone();
            newMaster.setDisplayName("Bop bop a lee bop");
            newMaster.getEventStamp().setStartDate(item.getEventStamp().getStartDate());
            dojo.require("cosmo.util.uuid");
            var gen = new cosmo.util.uuid.RandomGenerator()
            var newUid = gen.generate();
            newMaster.setUid(newUid);
            conduit.saveThisAndFuture(item, newMaster, {sync: true});
            conduit.getDashboardItems(newMaster);

        } finally {
           cosmotest.testutils.cleanupUser(user);            
        }
    },
    
    test_Mail: function(){
        try {
            var user = cosmotest.testutils.createTestAccount();
            
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            var collections = conduit.getCollections({sync: true}).results[0];
            
            var c0 = collections[0];
            
            var newItem = new cosmo.model.Note(
            {
                displayName: "Test Message"
            }
            );
            var messageId = "12345";
            var heads = "headers headers headers";
            var to = ["foo@bar.com","bar@foo.com"];
            var cc = ["moo@cow.com"];
            var bcc = ["loo@loo.net"];
            var from = ["mom@mom.com"];
            var originators = ["me", "mom"];
            var dateSent = "date foo";
            var inReplyTo = "nothing";
            var references = "farf";
            newItem.getMailStamp(true, {
                messageId: messageId,
                headers: heads,
                toAddress: to,
                ccAddress: cc,
                bccAddress: bcc,
                fromAddress: from,
                originators: originators,
                dateSent: dateSent,
                inReplyTo: inReplyTo,
                references: references
                
            });
            conduit.createItem(newItem, c0, {sync: true});

            var item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0];
            var mStamp = item0.getMailStamp();
            jum.assertEquals("messageId doesn't match", messageId, mStamp.getMessageId());
            jum.assertEquals("headers doesn't match", heads, mStamp.getHeaders());
            //TODO
            //failing in ie, apparently lists that look the same aren't enough there
/*            jum.assertEquals("to doesn't match", to, mStamp.getToAddress());
            jum.assertEquals("cc doesn't match", cc, mStamp.getCcAddress());
            jum.assertEquals("bcc doesn't match", bcc, mStamp.getBccAddress());
            jum.assertEquals("from doesn't match", from, mStamp.getFromAddress());
            jum.assertEquals("originators doesn't match", originators, mStamp.getOriginators());*/
            jum.assertEquals("dateSent doesn't match", dateSent, mStamp.getDateSent());
            jum.assertEquals("inReplyTo doesn't match", inReplyTo, mStamp.getInReplyTo());
            jum.assertEquals("references doesn't match", references, mStamp.getReferences());
    
        } finally {
           cosmotest.testutils.cleanupUser(user);            
        }
    },
    
    test_Preferences: function(){
        try {
            var user = cosmotest.testutils.createTestAccount();
            
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            
            var preferences = conduit.getPreferences({sync: true}).results[0];
            jum.assertTrue("preferences object not starting empty", dojo.lang.isEmpty(preferences));
            
            conduit.setPreference("foo", "bar", {sync: true});
            
            preferences = conduit.getPreferences({sync: true}).results[0];
            jum.assertEquals("preference foo wrong in getPreferences", "bar", preferences.foo);
            
            var foo = conduit.getPreference("foo", {sync: true}).results[0];
            jum.assertEquals("preference foo wrong in getPreference", "bar", foo);
            
            conduit.setPreference("foo", "baz", {sync: true});
            
            foo = conduit.getPreference("foo", {sync: true}).results[0];
            jum.assertEquals("preference foo wrong in getPreference", "baz", foo);
            
            conduit.deletePreference("foo", {sync: true});
            
            var preferences = conduit.getPreferences({sync: true}).results[0];
            jum.assertTrue("deletePreference failed", dojo.lang.isEmpty(preferences));
            
    
        } finally {
           cosmotest.testutils.cleanupUser(user);            
        }
    },
    
    test_Collections: function(){
        try {
            var user = cosmotest.testutils.createTestAccount();
            
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            
            var c0 = conduit.getCollections({sync: true}).results[0][0];
            c0.setDisplayName("foobar");
            
            conduit.saveCollection(c0, {sync: true});
            
            c0 = conduit.getCollections({sync: true}).results[0][0];
            jum.assertEquals("collection name didn't save", "foobar", c0.getDisplayName())
    
        } finally {
           cosmotest.testutils.cleanupUser(user);            
        }
    },
    
    createTestAccount: function(){
       cosmo.util.auth.clearAuth();
       var user = {
           password: "testing"
       };
       var success = false;
       
       var i = 0;
       while (!success && i < 10){
           var un = "user0";
           user.username = un;
           user.firstName = un;
           user.lastName = un;
           user.email = un + "@cosmotesting.osafoundation.org";
           
           cosmo.cmp.signup(user, {
               load: function(){success = true}, 
               error: function(){
                  cosmotest.testutils.cleanupUser(user);
                  i++;
           }}, true);
       }
       cosmo.util.auth.setCred(user.username, user.password);
       
       return user;
       
    },
    
    cleanup: function(user){
        cosmo.util.auth.setCred("root", "cosmo");
        cosmo.cmp.deleteUser(user.username, {handle: function(){}}, true);
        cosmo.util.auth.clearAuth();
    }
};

