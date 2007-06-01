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

dojo.require("cosmo.service.conduits.common");
dojo.require("cosmo.cmp");
dojo.require("cosmo.util.auth");

cosmotest.service.conduits.test_conduits = {
    test_Note: function(){
        try{

            var user = cosmotest.service.conduits.test_conduits.createTestAccount();
            
            // test getCollections
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            var collections = conduit.getCollections({sync: true}).results[0];
            jum.assertTrue("collections", !!collections);
            jum.assertTrue("collections length", collections.length > 0);
            
            var c0 = collections[0];
            
            // test getCollection
            var collectionDetails = conduit.getCollection(c0.getUid(), {sync: true});
    
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

            var item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0][0];
            
            jum.assertEquals("new item display name", newItemDisplayName, item0.getDisplayName());
            jum.assertEquals("triage status", newItemTriageStatus, item0.getTriageStatus());
            jum.assertEquals("triage rank", newItemTriageRank, item0.getRank());
            jum.assertEquals("auto triage", newItemAutoTriage, item0.getAutoTriage());
            jum.assertEquals("body", newItemBody, item0.getBody());

            // Test saveItem
            var item0DisplayName = "New Display Name";
            item0.setDisplayName(item0DisplayName);
            
            conduit.saveItem(item0, {sync: true});

            item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0][0];
     
            jum.assertEquals("item display name", item0DisplayName, item0.getDisplayName());

            // Test getItems
            conduit.createItem(new cosmo.model.Note(
            {
                displayName: "Testing display name 2"
            }
            ), c0, {sync: true});

            var items = conduit.getItems(c0, {}, {sync: true}).results[0];
            jum.assertTrue("items", !!items);
            jum.assertEquals("items length", 2, items.length);
            
            
            // Test deleteItem 
            /*
            conduit.deleteItem(item0.getUid, {sync: true});
            
            items = conduit.getItems(c0, {sync: true}).results[0];
            jum.assertTrue("deleteItem: items", !!items);
            jum.assertEquals("deleteItem: items length", 1, items.length);*/
            
            
        }
        finally{
            cosmotest.service.conduits.test_conduits.cleanup(user);            
        }
    },
    
    test_Event: function(){
        try {
            var user = cosmotest.service.conduits.test_conduits.createTestAccount();
            
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
            
            var item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0][0];
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

            item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0][0];
            jum.assertTrue("post-anytime start date", startDate.equals(item0.getEventStamp().getStartDate()));
            jum.assertEquals("location", "My place", item0.getEventStamp().getLocation());
            jum.assertTrue("anytime", item0.getEventStamp().getAnyTime());
            
            item0.getEventStamp().setAllDay(true);
            item0.getEventStamp().setAnyTime(false);
            conduit.saveItem(item0, {sync: true});
            item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0][0];
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
            jum.assertEquals("wrong nmber of occurrences", 7, item0Occurrences.length);
            
            var item4 = item0Occurrences[3];
            var item4Rid = item4.recurrenceId;
            var item4Modification = new cosmo.model.Modification({
                recurrenceId: item4.recurrenceId,
                modifiedProperties: {displayName: "Ze Modification"}
            });
            item4.getMaster().addModification(item4Modification);
            jum.assertEquals("modification display name wrong", "Ze Modification", item4.getDisplayName());
            conduit.createItem(item4, c0, {sync:true});
            
            // Make sure changes stuck
            item0Occurrences = conduit.getItems(c0, 
               {start: new cosmo.datetime.Date(2007, 5, 10),
                end: new cosmo.datetime.Date(2007, 5, 17)}, 
               {sync: true}
            ).results[0];
            
            item4 = item0Occurrences[0].getMaster().getNoteOccurrence(item4Rid);
//            jum.assertEquals("modfication display name didn't save", "Ze Modificaiton", item4.getDisplayName());
            

            
        } finally {
           cosmotest.service.conduits.test_conduits.cleanup(user);            
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
                  cosmotest.service.conduits.test_conduits.cleanup(user);
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

