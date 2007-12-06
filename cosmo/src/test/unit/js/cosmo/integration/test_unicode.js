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

dojo.provide("cosmotest.integration.test_unicode");

dojo.require("cosmotest.testutils");
dojo.require("cosmo.service.conduits.common");

function createUsername(i){
    return "t" + String.fromCharCode(i) + "est";
}

cosmotest.integration.test_unicode = {
    test_accountCreate: function(){
        var strings = cosmotest.integration.test_unicode.unicodeStrings;
        var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
        var lastDeferred = null;
        for (var i = 0x0020; i <= 0x0100; i = i + 0x0001){
            if (i == 0x003A) continue; // :
            if (i == 127) continue; // DEL
            if (i == 0x003B) continue; // TODO: Fix server
            if (i == 0x005C) continue; // TODO: Fix server
            if (i == 0x002F) continue; // TODO: Fix server

            
            var user;
            var un = createUsername(i);
            try{
                try{
                    user = cosmotest.testutils.createUser(un, i + "fooz");
                } catch (e){
                    dojo.debug(i.toString(16))
                }
                var collections = conduit.getCollections({sync:true}).results[0];
                jum.assertTrue("collections", !!collections);
                
                jum.assertTrue("collections length", collections.length > 0);
            } finally {
                cosmotest.testutils.cleanupUser({username: un});
            }
        }
    }
}
