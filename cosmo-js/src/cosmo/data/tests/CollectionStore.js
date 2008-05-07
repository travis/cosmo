/*
 * Copyright 2008 Open Source Applications Foundation
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
dojo.provide("cosmo.data.tests.CollectionStore");

dojo.require("cosmo.data.CollectionStore");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.service.conduits.common");
dojo.require("cosmo.tests.util");

doh.register("cosmo.data.tests.CollectionStore",
	[
        {
            name: "integrationTests",
            timeout: 10000,
            setUp: function(){
                this.serverRunning = cosmo.tests.util.serverRunning();
            },
            runTest: function(){
                if (this.serverRunning){
                    var d = cosmo.tests.util.setupTestUser();
                    d.addCallback(dojo.hitch(this, this.initStore));
                    d.addCallback(dojo.hitch(this, this.setupCollections));
                    d.addCallback(dojo.hitch(this, this.getCollections));

                    d.addErrback(function(e){console.log(e); return e;});
                    return cosmo.tests.util.defcon(d);
                }
            },

            initStore: function(){
                this.store = new cosmo.data.CollectionStore();
                return this.store;
            },

            setupCollections: function(){
                var s = cosmo.service.conduits.getAtomPlusEimConduit();
                return s.createCollection("foo");
            },

            getCollections: function(){
                var d = new dojo.Deferred();
                this.store.fetch({onComplete: dojo.hitch(d, d.callback), onError: dojo.hitch(d, d.errback)});
                d.addCallback(function(items){doh.is(1, items.length); return true;});
                return d;

            }
        }
    ]);


