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
 *     http://www.atomenabled.org/developers/protocol/atom-protocol-spec.php
 *
 *     cosmo.atompub.AppElement defines a number of functions designed
 *     to read special properties __elements__ and __attributes__
 *     of its descendents.
 *
 *
 */

dojo.provide("cosmo.atompub");

dojo.require("cosmo.service.transport.Rest");
dojo.require("dojo.lang.*");

dojo.declare("cosmo.atompub.ContentParserNotDefined", Error, {
    type: null,
    initializer: function(type){
        this.type = type;
    }
});

dojo.declare("cosmo.atompub.AppElement", null, {
    __elements__: {},
    __attributes__: {},
    textContent: null,

    initializer: function(xml, service){
        this.service = service;
        this._prepopulateListElements(this.__elements__);
        this._prepopulateListElements(this.__attributes__);
        if (xml){
            this.fromXml(xml);
        }
    },
    
    _prepopulateListElements: function(specificationList){
        for (var name in specificationList){
            var listName = specificationList[name][1];
            if (listName){
                this[listName] = [];
            }
        }
    },

    toString: function (){
        var sList = [];
        var names = this.getElementNames();
        for (var i in names){
            var name = names[i];
            sList.push(name);
            var object = this[name];
            sList.push((object && object.toString)? object.toString() : object);
            sList.push("\n");
        }
        return sList.join(" ");
    },

    getElementNames: function (){
        var names = [];
        for (var name in this.__elements__){
            var elementSpecification = this.__elements__[name];
            var label = elementSpecification[1] || name;
            names.push(label);
        }
        return names;
    },
    
    _processElement: function processEl(el){
        var name = el.localName || el.tagName;
        dojo.debug("Processing element: " + name);
        var elementSpecification = this.__elements__[name];
        if (elementSpecification){
            var listName = elementSpecification[1];
            var constructor = elementSpecification[0];
            this._addObject(new constructor(el, this.service),
                            name, listName);
        }
    },
    
    _processAttribute: function processAttr(el){
        var name = el.localName || el.nodeName;
        var elementSpecification = this.__attributes__[name];
        if (elementSpecification){
            var listName = elementSpecification[1];
            var constructor = elementSpecification[0];
            this._addObject(new constructor(el.nodeValue),
                            name, listName);
        }
    },

    _addObject: function addObj(object, name, listName){
        if (listName) this[listName].push(object);
        else if (name) this[name] = object;
    },
        
    fromXml: function fromXml(xml){
        this.textContent = xml.textContent;
        dojo.lang.map(xml.childNodes, dojo.lang.hitch(this,  "_processElement"));
        dojo.lang.map(xml.attributes, dojo.lang.hitch(this,  "_processAttribute"));
    }
});
dojo.declare("cosmo.atompub.TextElement", null, {
    initializer: function(xml){
        this.value = xml.textContent;
    },
    
    toString: function(){
        return this.value;
    }

});
dojo.declare("cosmo.atompub.Accept", null, {});
dojo.declare("cosmo.atompub.Categories", null, {});
dojo.declare("cosmo.atompub.Title", cosmo.atompub.TextElement, {});

dojo.declare("cosmo.atompub.Collection", cosmo.atompub.AppElement, {
    __attributes__: {
        "href": [String]
    },

    __elements__: {
        "title": [cosmo.atompub.Title],
        "accept": [cosmo.atompub.Accept, "accepts"],
        "categories": [cosmo.atompub.Categories, "categoryLists"]
    },
    
    getFeed: function(){
        var feedDeferred = this.service.bind({
            url: this.href
        });
        
        feedDeferred.addCallback(dojo.lang.hitch(this, function(xml){return new cosmo.atompub.Feed(xml.documentElement, this.service)}));
        return feedDeferred;
    }
    
});

dojo.declare("cosmo.atompub.Generator", null, {});
dojo.declare("cosmo.atompub.Icon", null, {});
dojo.declare("cosmo.atompub.Id", cosmo.atompub.TextElement, {});
dojo.declare("cosmo.atompub.Logo", null, {});
dojo.declare("cosmo.atompub.Rights", null, {});
dojo.declare("cosmo.atompub.Subtitle", null, {});
dojo.declare("cosmo.atompub.Updated", null, {});
dojo.declare("cosmo.atompub.Name", cosmo.atompub.TextElement, {});
dojo.declare("cosmo.atompub.Uri", null, {});
dojo.declare("cosmo.atompub.Email", null, {});

dojo.declare("cosmo.atompub.Person",  cosmo.atompub.AppElement, {
    __elements__: {
        "name": [cosmo.atompub.Name],
        "uri": [cosmo.atompub.Uri],
        "email": [cosmo.atompub.Email]
    }
});
dojo.declare("cosmo.atompub.Author",  cosmo.atompub.Person, {});
dojo.declare("cosmo.atompub.Category", cosmo.atompub.TextElement, {});
dojo.declare("cosmo.atompub.Contributor", null, {});
dojo.declare("cosmo.atompub.Link", cosmo.atompub.AppElement, {
    __attributes__: {
        "href": [String],
        "type": [String],
        "rel": [String]
    },

    toString: function(){
        return this.rel + ": " + this.href
    }
});

dojo.declare("cosmo.atompub.Content", cosmo.atompub.AppElement, {
    __attributes__: {
        "type": [String]
    },

    contentParsers: {},

    getParsedContent: function getParsedContent(contentParsers){
        contentParsers = contentParsers || this.contentParsers;
        var parser = contentParsers[this.type.value];
        if (!parser) throw new cosmo.atompub.ContentParserNotDefined(this.type.value);
        else {
            return parser(this.textContent);
        }
    }
});

dojo.declare("cosmo.atompub.Published", null, {});
dojo.declare("cosmo.atompub.Source", null, {});
dojo.declare("cosmo.atompub.Summary", cosmo.atompub.TextElement, {});

dojo.declare("cosmo.atompub.Entry", cosmo.atompub.AppElement, {
    __elements__: {
        "title": [cosmo.atompub.Title],
        "updated": [cosmo.atompub.Updated],
        "rights": [cosmo.atompub.Rights],
        "content": [cosmo.atompub.Content],
        "published": [cosmo.atompub.Published],
        "source": [cosmo.atompub.Source],
        "summary": [cosmo.atompub.Summary],
        "id": [cosmo.atompub.Id],
        "author": [cosmo.atompub.Author, "authors"],
        "category": [cosmo.atompub.Category, "categories"],
        "contributor": [cosmo.atompub.Contributor, "contributors"],
        "link": [cosmo.atompub.Link, "links"]
    }
});

dojo.declare("cosmo.atompub.Feed", cosmo.atompub.AppElement, {
    __elements__: {
        "title": [cosmo.atompub.Title],
        "generator": [cosmo.atompub.Generator],
        "icon": [cosmo.atompub.Icon],
        "id": [cosmo.atompub.Id],
        "logo": [cosmo.atompub.Logo],
        "rights": [cosmo.atompub.Rights],
        "subtitle": [cosmo.atompub.Subtitle],
        "updated": [cosmo.atompub.Updated],
        "author": [cosmo.atompub.Author, "authors"],
        "category": [cosmo.atompub.Category, "categories"],
        "contributor": [cosmo.atompub.Contributor, "contributors"],
        "link": [cosmo.atompub.Link, "links"],
        "entry": [cosmo.atompub.Entry, "entries"]
    }
});

dojo.declare("cosmo.atompub.Workspace", cosmo.atompub.AppElement, {
    __elements__: {
        "title": [cosmo.atompub.Title],
        "collection": [cosmo.atompub.Collection, "collections"]
    }
});

dojo.declare("cosmo.atompub.Service", cosmo.atompub.AppElement, {
    __elements__: {
        "workspace": [cosmo.atompub.Workspace, "workspaces"]
    },
    
    /**
     * Return a list of workspaces whose title matches <code>title</code>.
     */
    getWorkspacesWithTitle: function(title){
        return dojo.lang.filter(this.workspaces, 
                                function(workspace){
                                    return workspace.title.value == title
                                });
    }
});

/* Factory method for initializing a cosmo.atompub.Service instance from a
   url.
*/
cosmo.atompub.initializeService = function(url){
    var service = new cosmo.service.transport.Rest(cosmo.env.getBaseUrl() + "/atom/");
    appServiceDeferred = service.bind({
        url: url
    });
    appServiceDeferred.addCallback(function(xml){
        return new cosmo.atompub.Service(xml.documentElement, service);
    });
    return appServiceDeferred;
}