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
            var newItem = new cosmo.model.Note(
            {
                displayName: newItemDisplayName
            }
            );

            conduit.createItem(newItem, c0, {sync: true});
            
            var item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0][0];
            
            
            jum.assertEquals("new item display name", newItemDisplayName, item0.getDisplayName());
            
            
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

            var items = conduit.getItems(c0, {sync: true}).results[0];
            jum.assertTrue("items", !!items);
            jum.assertEquals("items length", 2, items.length);
            
            
            // Test deleteItem
            conduit.deleteItem(item0.getUid, {sync: true});
            
            items = conduit.getItems(c0, {sync: true}).results[0];
            jum.assertTrue("deleteItem: items", !!items);
            jum.assertEquals("deleteItem: items length", 1, items.length);
            
            
        }
        finally{
            cosmotest.service.conduits.test_conduits.cleanup(user);            
        }
    },
    
    test_Event: function(){
        try{
            var user = cosmotest.service.conduits.test_conduits.createTestAccount();
            
            var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
            var collections = conduit.getCollections({sync: true}).results[0];
            
            var c0 = collections[0];
            
            var newItem = new cosmo.model.Note(
            {
                displayName: "Blah blah blah"
            }
            );
            
            var startDate = new cosmo.datetime.Date();
            startDate.setMilliseconds(0);
            newItem.getEventStamp(true, {
                startDate: startDate,
                duration: new cosmo.model.Duration({hour: 1})
            });

            conduit.createItem(newItem, c0, {sync: true});
            
            var item0 = conduit.getItem(newItem.getUid(), {sync: true}).results[0][0];
            jum.assertTrue("start date", startDate.equals(item0.getEventStamp().getStartDate()));
            
            
        } finally{
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
       while (!success && i < 1000){
           var un = "user" + i;
           user.username = un;
           user.firstName = un;
           user.lastName = un;
           user.email = un + "@cosmotesting.osafoundation.org";
           
           cosmo.cmp.signup(user, {load: function(){success = true}, error: function(){i++}}, true);
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

