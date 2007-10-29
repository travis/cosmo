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

dojo.provide("cosmotest.integration.test_atompub")

dojo.require("cosmotest.util");

dojo.require("cosmo.atompub");
dojo.require("cosmo.env");
dojo.require("dojo.Deferred");

cosmotest.integration.test_atompub = {
    test_Service: function(){
        var ctx = {};
        cosmotest.util.asyncTest([
            function(user){
                return cosmo.atompub.initializeService("user/" + user.username);
            },

            function(service){
                ctx.service = service;
                return service.getWorkspacesWithTitle("home")[0].collections[0].getFeed();
            },

            function(feed){
                ctx.feed = feed;
                return feed.getLinks("self")[0].getResource(function(xml){console.log(xml)});
            },

            function(linkResource){
                return ctx.feed.getLinks("alternate")[0].getResource(function(xml){console.log(xml)});
            }
        ]);
    }
};