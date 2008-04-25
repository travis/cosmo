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

dojo.provide("cosmo.data.TicketStore");

dojo.require("cosmo.data.AtompubStore");
dojo.require("cosmo.xml");
(function(){

var xmlns = {xhtml :"http://www.w3.org/1999/xhtml"};

dojo.declare("cosmo.data.TicketProcessor", null,
{
    attr: {
        type: function(node){
            var title = cosmo.xml.query("descendant::*[@class='ticket']/*[@class='type']/@title", node, xmlns)[0];
            return title? title.value : null;
        },
        timeout: function(node){
            var title = cosmo.xml.query("descendant::*[@class='ticket']/*[@class='timeout']/@title", node, xmlns)[0];
            return title? title.value : null;
        },
        key: function(node){
            var n = cosmo.xml.query("descendant::*[@class='ticket']/*[@class='key']/text()", node)[0];
            return n? n.nodeValue : null;
        }
    },

    getValues: function(/* item */ item,
                        /* attribute-name-string */ attribute){
        var f = this.attr[attribute];
        return f? [f(item)] : null;
    },

    getAttributes: function(/* item */ item){
        var attrs = [];
        for (var key in this.attr){
            var val = this.attr[key]();
            if (val) attrs.push(key);
        }
        return attrs;
    },

	hasAttribute: function(	/* item */ item,
							/* attribute-name-string */ attribute){
        if (this.getValues(item, attribute).length > 0) return true;
        else return false;
    },

	setValue: function(/* item */ item, /* string */ attribute, /* string */ value){
    }
});
}());

dojo.declare("cosmo.data.TicketStore", cosmo.data.AtompubStore,
{
    contentProcessors: {"xhtml": new cosmo.data.TicketProcessor()},

    generateContent: function(item){
        return ['<content type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><div class="ticket"><span class="key">',
                item.key ,'</span><span class="type" title="',
                item.type, '"></span>',
                (item.timeout? '<span class="type" title="' + item.timeout + '"></span>' : ""),
                '</div></div></content>'].join("");
    },

    getEntryId: function(item){
        return "urn:uuid:" + item.key;
    },

    getEntryTitle: function(item){
        return item.key;
    }

});

