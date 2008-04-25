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

dojo.provide("cosmo.tests.util");
dojo.require("cosmo.cmp");
dojo.require("cosmo.util.auth");
dojo.require("dojox.uuid.generateTimeBasedUuid");
cosmo.tests.util = {

    serverRunning: function(){
        var p = location.protocol;
        return (p == "http:" || p == "https:");
    },

    setupTestUser: function(){
        var d = this.createTestAccount();
        d.addCallback(function(user){
            cosmo.util.auth.setCred(user.username, user.password);
            return user;
        });
        d.addErrback(function(e){
            console.debug(e);
        });
        return d;
    },

    createTestAccount: function(){
        return this.createUser(
            dojox.uuid.generateTimeBasedUuid().slice(0, 8)
        );
    },

    createUser: function(username, email){
        cosmo.util.auth.clearAuth();
        var user = {
            password: "testing",
            username: username,
            firstName: username,
            lastName: username,
            email: email || username + "@cosmotesting.osafoundation.org"
        };
        var d = cosmo.cmp.signup(user);
        d.addCallback(function(){return user;});
        return d;
    },

    getCollection: function(user, projection){
        var urlD = this.getCollectionUrl(user);
        urlD.addCallback(function(url){
                             return dojo.xhrGet(dojo.mixin({url: url + projection, handleAs: "xml"}, cosmo.util.auth.getAuthorizedRequest()));
                         });
        return urlD;
    },

    getCollectionUrl: function(user){
        var userD = dojo.xhrGet(dojo.mixin({url: this.getUserServiceUrl(user), handleAs: "xml"},
                                           cosmo.util.auth.getAuthorizedRequest()));
        userD.addCallback(
            dojo.hitch(this, function(serviceXml){
                var collection = cosmo.atompub.getCollections(serviceXml.documentElement, "home")[0];
                if (collection) {
                    debugger
                    return collection;
                } else {
                    var createD = this.createCollection(user);
                    createD.addCallback(dojo.hitch(this, function(){
                        return createD.ioArgs.xhr.getResponseHeader("Location");
                    }));
                    return createD;
                }

            }));
        return userD;
    },

    createCollection: function(user){
        return dojo.rawXhrPost(dojo.mixin({url: this.getUserServiceUrl(user),
                                           postData: this.createCollectionRepresentation(),
                                           contentType: "application/xhtml+xml"
                                          },
                                   cosmo.util.auth.getAuthorizedRequest()));
    },

    createCollectionRepresentation: function(collection){
        collection = collection || {};
        return '<div class="collection"><span class="name">' + (collection.name || "Test collection") +
            '</span><span class="uuid">' + (collection.uuid || dojox.uuid.generateTimeBasedUuid()) + '</span></div>';
    },

    getUserServiceUrl: function(user){
        return dojo.moduleUrl("cosmo", "../../atom/user/" + encodeURIComponent(user.username));
    },

    defcon: function(dojoDeferred){
        var d2 = new doh.Deferred();
        dojoDeferred.addCallbacks(
            dojo.hitch(d2, d2.callback),
            dojo.hitch(d2, d2.errback));
        return d2;
    }
};
