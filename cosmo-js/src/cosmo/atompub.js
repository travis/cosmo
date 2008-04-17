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
// Put this in closure scope to make it wicked frickin fast.
var ns = {
    atom: "http://www.w3.org/2005/Atom",
    app: "http://www.w3.org/2007/app"
};
function nsResolver(pre){
    return ns[pre] || ns.atom;
}
var ieSelectionNS = "xmlns:atom='" + ns.atom +
    "' xmlns:app='" + ns.app + "'";

dojo.mixin(cosmo.atompub,
{
    getEditIri: function(entry){
        var href = this.evalXPath("atom:link[@rel='edit']/@href", entry)[0];
        if (href) return href.value;
        else return null;
    },

    evalXPath: function(query, node){
        return cosmo.xml.query(query, node, ns, "atom");
    },

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