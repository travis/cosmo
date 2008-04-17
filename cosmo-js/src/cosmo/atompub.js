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
var xpFunctions = {
    "id": singleTextValue("atom:id/text()"),
    "title": singleTextValue("atom:title/text()"),
    "updated": singleTextValue("atom:updated/text()"),
    "author": noop("atom:author"),
    "link": noop("atom:link"),
    "content": singleValue("atom:content"),
    "category" : singleTextValue("atom:category/text()"),
    "contributor" : singleTextValue("atom:contributor/text()"),
    "generator" : singleTextValue("atom:generator/text()"),
    "icon" : singleTextValue("atom:icon/text()"),
    "logo" : singleTextValue("atom:logo/text()"),
    "rights" : singleTextValue("atom:rights/text()"),
    "subtitle" : singleTextValue("atom:subtitle/text()")
};

dojo.mixin(cosmo.atompub,
{
    getEditIri: function(entry){
        var href = this.query("atom:link[@rel='edit']/@href", entry)[0];
        return href? href.value : null;
    },

    ns: nsMap,

    query: xPathQueryFunc,

    attr: xpFunctions,

    newEntry: function(iri, entry){
        if (entry instanceof Element) entry = dojox.data.dom.innerXML(entry);
        return dojo.xhrRawPost({url: iri, postData: entry});
    },

    modifyEntry: function(entry){
        return dojo.xhrRawPut({url: this.getEditLink(entry), putData: entry});
    },

    deleteEntry: function(){
        return dojo.xhrDelete({url: this.getEditLink(entry)});
    }
});
}());