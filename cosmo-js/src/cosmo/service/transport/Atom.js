/* * Copyright 2006-2007 Open Source Applications Foundation *
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

 /**
 * summary:
 *      This module provides wrappers around dojo.io.bind to simplify using
 *      the Cosmo Management Protocol (CMP) from javascript.
 * description:
 *      For more information about CMP, please see:
 *      http://wiki.osafoundation.org/Projects/CosmoManagementProtocol
 *
 *      Most methods take handlerDicts identical to those required
 *      by dojo.io.bind.
 */
dojo.provide("cosmo.service.transport.Atom");

dojo.require("cosmo.env");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.service.transport.Rest");
dojo.require("cosmo.service.exception");

dojo.declare("cosmo.service.transport.Atom", cosmo.service.transport.Rest, {
    constructor: function(urlCache){
        this.urlCache = urlCache;
    },

    EDIT_LINK: "atom",
    DETACHED_LINK: "detached",
    PROJECTION_FULL_EIM_JSON: "/full/eim-json",

    CONTENT_TYPE_ATOM: "application/atom+xml",

    _getUsernameForURI: function(){
        return encodeURIComponent(cosmo.util.auth.getUsername());
    },

    getAndCheckEditLink: function(item, kwArgs){
        kwArgs = kwArgs || {};
        var editLink = item.getUrls()[this.EDIT_LINK];
        if (!editLink) {
            throw new cosmo.service.exception.ClientSideError(
                "Could not find edit link for item with uuid " +
                item.getUid() + ": " + item.toString()
            );
        }
        return this.generateUri(editLink.uri,  kwArgs.noProjection ? "" :
                                kwArgs.projection || this.PROJECTION_FULL_EIM_JSON);
    },

    getAtomBase: function () {
        return cosmo.env.getBaseUrl() + "/atom";
    },

    generateUri: function(base, projection, queryHash){
        queryHash = queryHash || {};
        var queryIndex = base.indexOf("?");
        if (queryIndex > -1) {
            var queryString = base.substring(queryIndex + 1);
            var params = dojo.queryToObject(queryString);
            dojo.mixin(queryHash, params);
        } else {
            queryIndex = base.length;
        }

        return base.substring(0, queryIndex) + projection + this.queryHashToString(queryHash);
    },

    createCollection: function(name, kwArgs){
        var r = this.getDefaultRequest(
            this.getAtomBase() + "/user/" + this._getUsernameForURI(),
            kwArgs);
        r.postData =  ["<?xml version='1.0' encoding='UTF8'?>",
                       '<div class="collection">',
                       '<span class="name">', cosmo.util.string.escapeXml(name), '</span>',
                       '<span class="uuid">', dojox.uuid.generateTimeBasedUuid(), '</span>',
                       '</div>'].join("");
        r.contentType = "application/xhtml+xml";
        return dojo.rawXhrPost(r);
    },

    deleteCollection: function(collection, kwArgs){
        var r = this.getDefaultRequest(
            collection.getUrl("atom"),
            kwArgs);
        return dojo.xhrDelete(r);
    },

    getCollection: function(collectionUrl, kwArgs){
        return dojo.xhrGet(this.getDefaultRequest(
            this.generateUri(collectionUrl , "/details", {}),
            kwArgs
        ));
    },

    getCollections: function(kwArgs){
        var r = this.getDefaultRequest(
            this.getAtomBase() + "/user/" + this._getUsernameForURI(),
            kwArgs
        );
        return dojo.xhrGet(r);
    },

    getSubscriptions: function (kwArgs){
        return dojo.xhrGet(this.getDefaultRequest(
            this.getAtomBase() + "/user/" +
                this._getUsernameForURI() + "/subscriptions",
            kwArgs
        ));
    },

    getItems: function (collection, searchCrit, kwArgs){
        if (collection instanceof cosmo.model.Subscription){
            collection = collection.getCollection();
        }

        var query = this._generateSearchQuery(searchCrit);
        var editLink = this.getAndCheckEditLink(collection, searchCrit);

        return dojo.xhrGet(this.getDefaultRequest(
            this.generateUri(editLink, "", query),
            kwArgs
        ));
    },

    saveItem: function (item, postContent, kwArgs){
        var r = this.getDefaultRequest(this.getAndCheckEditLink(item), kwArgs);
        r.putData = postContent;
        r.contentType = this.CONTENT_TYPE_ATOM;
        var deferred = dojo.rawXhrPut(r);
        this.addErrorCodeToExceptionErrback(deferred, 423, cosmo.service.exception.CollectionLockedException);
        return deferred;
    },

    saveThisAndFuture: function (oldOccurrence, postContent, kwArgs){
        var r = this.getDefaultRequest(this.getAtomBase() + "/" + this.urlCache.getUrl(oldOccurrence, this.DETACHED_LINK), kwArgs);
        r.postData = postContent;
        r.contentType = this.CONTENT_TYPE_ATOM;
        var deferred = dojo.rawXhrPost(r);
        this.addErrorCodeToExceptionErrback(deferred, 423, cosmo.service.exception.CollectionLockedException);
        return deferred;
    },

    getItem: function(uid, kwArgs){
        var query = this._generateSearchQuery(kwArgs);
        return dojo.xhrGet(this.getDefaultRequest(
            this.getAtomBase() + "/item/" +
                uid + "/full/eim-json" + this.queryHashToString(query),
            kwArgs
        ));

    },

    expandRecurringItem: function(item, searchCrit, kwArgs){
        var query = this._generateSearchQuery(searchCrit);
        var projection = searchCrit.projection || "/full/eim-json";
        var expandedLink = item.getUrls()['expanded'];
        return dojo.xhrGet(this.getDefaultRequest(
        this.generateUri(expandedLink.uri, "", query),
            kwArgs
        ));
    },

    createItem: function(item, postContent, collection, kwArgs){
        if (collection instanceof cosmo.model.Subscription){
            collection = collection.getCollection();
        }
        var editLink = this.getAndCheckEditLink(collection);
        var r = this.getDefaultRequest(editLink, kwArgs);
        r.postData = postContent;
        r.contentType = this.CONTENT_TYPE_ATOM;
        var deferred = dojo.rawXhrPost(r);
        this.addErrorCodeToExceptionErrback(deferred, 423, cosmo.service.exception.CollectionLockedException);
        return deferred;
    },

    createSubscription: function(subscription, postContent, kwArgs){
        var r = this.getDefaultRequest(this.getAtomBase() + "/user/" +
                                       this._getUsernameForURI() + "/subscriptions", kwArgs);
        r.postData = postContent;
        r.contentType = this.CONTENT_TYPE_ATOM;
        var deferred = dojo.rawXhrPost(r);
        this.addErrorCodeToExceptionErrback(deferred, 409, cosmo.service.exception.ConflictException);
        return deferred;
    },

    saveSubscription: function(subscription, postContent, kwArgs){
        var r = this.getDefaultRequest(this.getAndCheckEditLink(subscription), kwArgs);
        r.putData = postContent;
        r.contentType = this.CONTENT_TYPE_ATOM;
        return dojo.rawXhrPut(r);
    },

    saveCollection: function(collection, postContent, kwArgs){
        var r = this.getDefaultRequest(this.getAndCheckEditLink(collection), kwArgs);
        r.contentType = "application/xhtml+xml";
        r.putData = postContent;
        return dojo.rawXhrPut(r, kwArgs);
    },

    deleteItem: function(item, kwArgs){
        var editLink = this.getAndCheckEditLink(item, kwArgs);
        var deferred = dojo.xhrDelete(editLink, kwArgs);
        this.addErrorCodeToExceptionErrback(deferred, 423, cosmo.service.exception.CollectionLockedException);
        return deferred;
    },

    deleteSubscription: function(subscription, kwArgs){
        kwArgs = kwArgs || {};
        kwArgs.noProjection = true;
        return this.deleteItem(subscription, kwArgs);
    },

    removeItem: function(item, collection, kwArgs){
        var editLink = this.getAndCheckEditLink(item);
        var query = {uuid: collection.getUid()};
        var deferred = dojo.xhrDelete(this.getDefaultRequest(
            this.generateUri(editLink, "", query),
            kwArgs
        ));
        this.addErrorCodeToExceptionErrback(deferred, 423, cosmo.service.exception.CollectionLockedException);
        return deferred;
    },

    checkIfPrefExists: function (key, kwArgs){
        kwArgs = kwArgs || {};
        kwArgs = cosmo.util.lang.shallowCopy(kwArgs);
        kwArgs.noErr = true;
        return dojo.xhr('HEAD', this.getDefaultRequest(
            this.getAtomBase() + "/user/" + this._getUsernameForURI() + "/preference/"+ key,
            kwArgs));
    },

    // This is not the right way to do this. We shouldn't be guessing urls,
    // but since the design does not currently support processing the entry in this
    // layer (and thus does not have access to the urls returned by
    // getting the preference collection) I believe this is the best option for the moment.
    //
    // Once this layer is redesigned, we should redo this. If we get a chance, this might be something
    // to improve before preview.
    setPreference: function (key, val, postContent, kwArgs){
        var existsDeferred = this.checkIfPrefExists(key, kwArgs);

        // If exists returned a 200
        existsDeferred.addCallback(dojo.hitch(this, function (){
            var r = this.getDefaultRequest(
                this.getAtomBase() + "/user/" + this._getUsernameForURI() + "/preference/" + key,
                kwArgs
            );
            r.putData = postContent;
            r.contentType = this.CONTENT_TYPE_ATOM;
            return dojo.rawXhrPut(r);
        }));

        // If exists returned a 404
        existsDeferred.addErrback(dojo.hitch(this, function (){
            var r = this.getDefaultRequest(
                this.getAtomBase() + "/user/" + this._getUsernameForURI() + "/preferences",
                kwArgs
            );
            r.postData = postContent;
            r.contentType = this.CONTENT_TYPE_ATOM;
            return dojo.rawXhrPost(r);
        }));
       return existsDeferred;
    },

    getPreferences: function (kwArgs){
        return dojo.xhrGet(this.getDefaultRequest(
            this.getAtomBase() + "/user/" + this._getUsernameForURI() + "/preferences",
            kwArgs));
    },

    getPreference: function (key, kwArgs){
        return dojo.xhrGet(this.getDefaultRequest(
            this.getAtomBase() + "/user/" + this._getUsernameForURI() + "/preference/" + key,
            kwArgs));
    },

    deletePreference: function(key, kwArgs){
        return dojo.xhrDelete(this.getDefaultRequest(
            this.getAtomBase() + "/user/" + this._getUsernameForURI() + "/preference/" + key,
            kwArgs));
    },

    _generateSearchQuery: function(/*Object*/searchCrit){
        var ret = {};
        if (!searchCrit) return ret;
        if (searchCrit.start) {
            ret["start"] = dojo.date.stamp.toISOString(searchCrit.start);
        }
        if (searchCrit.end) {
            ret["end"] = dojo.date.stamp.toISOString(searchCrit.end);
        }
        return ret;
    }
});
