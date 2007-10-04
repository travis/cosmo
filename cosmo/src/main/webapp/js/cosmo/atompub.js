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

/**
 * summary:
 *     A package for interacting with atompub services.
 *
 *     For more information about Atom Publishing Protocol, please see:
 *     http://bitworking.org/projects/atom/draft-ietf-atompub-protocol-17.html
 *
 *     cosmo.atompub.AppElement defines a number of functions designed
 *     to read special properties __elements__, __relements__ and __attributes__
 *     of its descendents.
 *
 *
 */

dojo.provide("cosmo.atompub");

dojo.require("cosmo.service.transport.Rest");

dojo.declare("cosmo.atompub.AppElement", null, {
    __elements__: [],
    __relements__: [],
    __attributes__: [],
    initializer: function(xml, service){
        this.service = service;
        if (xml){
            this.fromXml(xml);
        }
    },

    _processRepeatableElementSpecification: function (xml, elementSpecification){
        var elementName = elementSpecification[0];
        var objectName = elementSpecification[2] || elementName;
        var constructor = elementSpecification[1];
        dojo.debug("Processing repeatable element: " + elementName);
        this[objectName] = dojo.lang.map(
            xml.getElementsByTagName(elementName),
            function(xml){
                return new constructor(xml, this.service);
            } 
        );
    },

    _processElementSpecification: function (xml, elementSpecification){
        var elementName = elementSpecification[0];
        var objectName = elementSpecification[2] || elementName;
        var constructor = elementSpecification[1];
        dojo.debug("Processing element: " + elementName);
        var el = xml.getElementsByTagName(elementName)[0];
        if (el){
            this[objectName] = new constructor(el, this.service);
        }
    },

    _processAttributeSpecification: function (xml, elementSpecification){
        var elementName = elementSpecification[0];
        var objectName = elementSpecification[2] || elementName;
        var constructor = elementSpecification[1];
        dojo.debug("Processing attribute: " + elementName);
        var el = xml.getAttribute(elementName);
        if (el){
            this[objectName] = new constructor(el, this.service);
        }
    },

    fromXml: function(xml){
        dojo.lang.map(this.__relements__, dojo.lang.curry(this, "_processRepeatableElementSpecification", xml));
        dojo.lang.map(this.__elements__, dojo.lang.curry(this,  "_processElementSpecification", xml));
        dojo.lang.map(this.__attributes__, dojo.lang.curry(this,  "_processAttributeSpecification", xml));
    }
});

dojo.declare("cosmo.atompub.Accept", null, {});
dojo.declare("cosmo.atompub.Categories", null, {});
dojo.declare("cosmo.atompub.Title", null, {});

dojo.declare("cosmo.atompub.Collection", cosmo.atompub.AppElement, {
    __attributes__: [
        ["href", String]
    ],

    __elements__: [
        ["title", cosmo.atompub.Title]
    ],
    
    __relements__: [
        ["accept", cosmo.atompub.Accept, "accepts"],
        ["categories", cosmo.atompub.Categories, "categoryLists"]
    ],
    
    getFeed: function(){
        var feedDeferred = this.service.bind({
            url: this.href
        });
        
        feedDeferred.addCallback(dojo.lang.hitch(this, function(xml){return new cosmo.atompub.Feed(xml, this.service)}));
    }
    
});

dojo.declare("cosmo.atompub.Feed", cosmo.atompub.AppElement, {
    __elements__: [
        ["title", cosmo.atompub.Title],
        ["generator", cosmo.atompub.Generator],
        ["icon", cosmo.atompub.Icon],
        ["id", cosmo.atompub.Id],
        ["logo", cosmo.atompub.Logo],
        ["rights", cosmo.atompub.Rights],
        ["subtitle", cosmo.atompub.Subtitle],
        ["updated", cosmo.atompub.Updated]
    ],

    __relements__: [
        ["author", cosmo.atompub.Author, "authors"],
        ["category", cosmo.atompub.Category, "categories"],
        ["contributor", cosmo.atompub.Contributor, "contributors"],
        ["link", cosmo.atompub.Link, "link"]
    ]
});

dojo.declare("cosmo.atompub.Entry", cosmo.atompub.AppElement, {
    
});

dojo.declare("cosmo.atompub.Workspace", cosmo.atompub.AppElement, {
    __elements__: [
        ["title", cosmo.atompub.Title]
    ],

    __relements__: [
        ["collection", cosmo.atompub.Collection, "collections"]
    ]
});

dojo.declare("cosmo.atompub.Service", cosmo.atompub.AppElement, {
    __relements__: [
        ["workspace", cosmo.atompub.Workspace, "workspaces"]
    ]
});

/* Factory method for initializing a cosmo.atompub.Service instance from a
   url.
*/
cosmo.atompub.initializeService = function(url){
    var service = new cosmo.service.transport.Rest();
    appServiceDeferred = service.bind({
        url: url
    });
    appServiceDeferred.addCallback(function(xml){
        return new cosmo.atompub.Service(xml, service);
    });
}