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

dojo.debug("Before you run cosmotest.service.translators.test_conduits " +
        "please make sure you are logged in and have a calendar with " +
        "at least one Item.");
cosmotest.service.conduits.test_conduits = {
    test_Atom: function(){
        
        // test getCollections
        var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
        var collections = conduit.getCollections({sync: true}).results[0];
        jum.assertTrue("collections", !!collections);
        jum.assertTrue("colllections length", collections.length > 0);
        
        var c0 = collections[1];
        
        // test getCollection
/*        var collectionDetails = conduit.getCollection(c0.getUid(), {sync: true});

        jum.assertTrue("collectionDetails", !!collectionDetails)
        dojo.debug(collectionDetails)
        return */
        
        var items = conduit.getItems(c0, {sync: true}).results[0];
        jum.assertTrue("items", !!items);
        jum.assertTrue("items length", items.length > 0);
        
        var item0 = items[0];
        item0.setDisplayName("New Display Name");
        
        conduit.saveItem(item0, {sync: true});
        
        var newItem = new cosmo.model.Note(
        {
            displayName: "Testing display name"
        }
        );
        conduit.createItem(newItem, c0, {sync: true});
    }
};

