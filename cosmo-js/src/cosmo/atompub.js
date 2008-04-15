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

dojo.mixin(cosmo.atompub,
{
    getEditIri: function(entry){
        // dojo.query attribute query failing for xml documents
        // so do this manually
        var editLinks = dojo.query('link', entry).filter(function(link){return link.getAttribute("rel") == "edit";});
        if (editLinks.length < 1) return null;
        else return editLinks[0].getAttribute("href");
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