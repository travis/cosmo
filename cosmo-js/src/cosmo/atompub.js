/* * Copyright 2008 Open Source Applications Foundation *
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
 *      This module provides utility functions for working with Atompub services
 * description:
 *      TODO: fill this in
 */
dojo.provide("cosmo.atompub");
dojo.require("dojox.data.dom");
(function(){
// XPath wrappers
var nsMap = {
        atom: "http://www.w3.org/2005/Atom",
        app: "http://www.w3.org/2007/app"
};
function xPathQueryFunc(query, node){
    return cosmo.xml.query(query, node, nsMap, "atom");
}

function noop(query){
    return function(node){
        xPathQueryFunc(query, node);
    };
}
function singleValue(query){
    return function(node){
       return xPathQueryFunc(query, node)[0];
    };
}
function singleTextValue(query){
    return function(node){
        var r = xPathQueryFunc(query, node)[0];
        return r? r.nodeValue : null;
    };
}
function setText(query){
    return function(entry, value){
        var node = xPathQueryFunc(query, entry)[0];
        node.nodeValue = value;
    };
}
function setNode(query){
    return function(entry, value){
        var node = xPathQueryFunc(query, entry)[0];
        node.parentNode.replaceChild(value, node);
    };
}

var xp = {
    "id": [singleTextValue, setText, "atom:id/text()"],
    "title": [singleTextValue, setText, "atom:title/text()"],
    "updated": [singleTextValue, setText, "atom:updated/text()"],
    "author": [noop, setNode, "atom:author"],
    "link": [noop, setNode, "atom:link"],
    "content": [singleValue, setNode, "atom:content"],
    "category" : [singleTextValue, setText, "atom:category/text()"],
    "contributor" : [singleTextValue, setText, "atom:contributor/text()"],
    "generator" : [singleTextValue, setText, "atom:generator/text()"],
    "icon" : [singleTextValue, setText, "atom:icon/text()"],
    "logo" : [singleTextValue, setText, "atom:logo/text()"],
    "rights" : [singleTextValue, setText, "atom:rights/text()"],
    "subtitle" : [singleTextValue, setText, "atom:subtitle/text()"]
};

dojo.mixin(cosmo.atompub,
{
    getEditIri: function(entry){
        var href = this.query("atom:link[@rel='edit']/@href", entry)[0];
        return href? href.value : null;
    },

    getEditLink: function(entry){
        return this.query("atom:link[@rel='edit']", entry)[0];
    },

    ns: nsMap,

    query: xPathQueryFunc,

    attr: function(){var r = {}; for (var k in xp) r[k] = xp[k][0](xp[k][2]); return r;}(),

    set: function(){var r = {}; for (var k in xp) r[k] = xp[k][1](xp[k][2]); return r;}(),

    newEntry: function(iri, entry, request){
        var entryString = (entry instanceof Element)? dojox.data.dom.innerXML(entry) : entry;
        var d = dojo.rawXhrPost(dojo.mixin({url: iri, postData: entryString,
                                            handleAs: "xml", contentType: "application/atom+xml"}, request));
        d.addCallback(dojo.hitch(this, this.updateEntry, entry));
        return d;
    },

    updateEntry: function(oldEntry, newEntry){
        var link = this.getEditLink(newEntry).cloneNode();
        oldEntry.appendChild(link);
        return oldEntry;
    },

    modifyEntry: function(entry, iri, request){
        var entryString = (entry instanceof Element)? dojox.data.dom.innerXML(entry) : entry;
        return dojo.rawXhrPut(dojo.mixin({url: this.getEditLink(entry), putData: entryString,
                                          handleAs: "xml", contentType: "application/atom+xml"}, request));
    },

    deleteEntry: function(request){
        return dojo.xhrDelete(dojo.mixin({url: this.getEditLink(entry), handleAs: "xml",
                                          contentType: "application/atom+xml"}, request));
    },

    // Service doc util functions
    getWorkspaces: function(serviceXml, title){
        var q = "app:workspace" + title? "[atom:title='" + title + "']" : "";
        return this.query(q, serviceXml);
    },

    getCollections: function(serviceXml, workspaceTitle, collectionTitle){
        var q = "app:workspace" + (workspaceTitle? "[atom:title='" + workspaceTitle + "']" : "") +
            "/app:collection" + (collectionTitle? "[atom:title='" + collectionTitle + "']": "");
        return this.query(q, serviceXml);
    }

});
}());