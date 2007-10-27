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
dojo.require("dojo.dom");
dojo.require("dojo.lang.*");

dojo.declare("cosmo.atompub.ContentDeserializerNotDefined", Error, {
    type: null,
    initializer: function(type){
        this.type = type;
    },
    toString: function(){
        return "Content deserializer not defined for type " + this.type;
    }
});

dojo.declare("cosmo.atompub.ContentSerializerNotDefined", Error, {
    type: null,
    initializer: function(type){
        this.type = type;
    },
    toString: function(){
        return "Content serializer not defined for type " + this.type;
    }
});

dojo.declare("cosmo.atompub.AppElement", null, {
    __elements__: {},
    __attributes__: {},
    text: null,
    xml: null,
    initializer: function(xml, service){
        this.service = service;
        this._prepopulateListElements(this.__elements__);
        this._prepopulateListElements(this.__attributes__);
        if (xml){
            this.fromXml(xml);
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

    fromXml: function fromXml(xml){
        // if first child is text node
        if (xml.firstChild && (xml.firstChild.nodeType == 3)){
            this.text = xml.firstChild.data;
        }
        this.xml = xml;
        dojo.lang.map(xml.childNodes, dojo.lang.hitch(this,  "_processElement"));
        dojo.lang.map(xml.attributes, dojo.lang.hitch(this,  "_processAttribute"));
    },
    
    toXmlString: function toXmlString(){
        var content = this.serializeContent? this.serializeContent() : (this.text || "");
        var subordinateText =  content +
            this._elementsToXmlString();
        return "<" + this.xmlName + this._attributesToXmlString() + 
            ((subordinateText == "")? "/>": 
             ">" + subordinateText + "</" + this.xmlName + ">" );
    },

    toXmlDocumentString: function txds(){
        return '<?xml version="1.0" encoding=\'utf-8\'?>' +
            this.toXmlString();
    },

    _prepopulateListElements: function(specificationList){
        for (var name in specificationList){
            var listName = specificationList[name][1];
            if (listName){
                this[listName] = [];
            }
        }
    },

    _processElement: function processEl(el){
        var name = el.localName || el.tagName;
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
        
    _attributeToXmlString: function a2xmls(name, value){
        return " " + name + "=" + '"' + value + '"';
    },

    _attributesToXmlString: function(){
        var strings = [];
        for (var name in this.__attributes__){
            var elementSpec = this.__attributes__[name];
            var listName = elementSpec[1];
            if (listName){
                strings =
                    dojo.lang.map(this[listName], this, 
                                  function(value){
                                      return this._attributeToXmlString(name, value);
                                  }).join("");
            } else if (this[name]){
                strings.push(this._attributeToXmlString(name, this[name]));
            }
        }
        return strings.join("");
    },

    _elementsToXmlString: function(){
        var strings = [];
        for (var name in this.__elements__){
            var elementSpec = this.__elements__[name];
            var listName = elementSpec[1];
            if (listName){
                strings.push(dojo.lang.map(this[listName], this, 
                                     function(el){
                                         return el? el.toXmlString() : "";
                                     }).join(""));
            } else if (this[name]){
                strings.push(this[name].toXmlString());
            }
        }
        return strings.join("");
    }
});

dojo.declare("cosmo.atompub.TextConstruct", cosmo.atompub.AppElement, {
    __attributes__: {
        "type" : [String]
    },

    contentDeserializers: {
        text: function(xml){return xml.textContent},
        html: function(xml){return xml.textContent},
        xhtml: function(xml){return xml.firstChild}
    },

    contentSerializers: {
        text: function(content){return content.text},
        html: function(content){return dojo.string.escapeXml(content.text)},
        xhtml: function(content){return dojo.dom.innerXML(content.xml)}
    },

    serializeContent: function serializeContent(contentSerializers){
        contentSerializers = contentSerializers || {};
        var serializer = contentSerializers[this.type] 
            || this.contentSerializers[this.type]
            || this.contentSerializers["text"];
        return serializer.serialize? serializer.serialize(this) : serializer(this);

    },

    deserializeContent: function deserializeContent(contentDeserializers){
        contentDeserializers = contentDeserializers || {};
        var deserializer = contentDeserializers[this.type] || this.contentDeserializers[this.type];
        if (!deserializer) throw new cosmo.atompub.ContentDeserializerNotDefined(this.type);
        else {
            return deserializer.deserialize? deserializer.deserialize(this.xml) : 
                deserializer(this.xml);
        }
    }
});

dojo.declare("cosmo.atompub.DateConstruct", cosmo.atompub.AppElement, {
    __attributes__: {
        "type" : [String]
    }
});

dojo.declare("cosmo.atompub.Accept", cosmo.atompub.AppElement, {
    xmlName: "accept"
});
dojo.declare("cosmo.atompub.Categories", cosmo.atompub.AppElement, {
    xmlName: "categories",
    __attributes__: {
        "term": [String],
        "scheme": [String],
        "label": [String],
        "fixed": [String],
        "scheme": [String],
        "href": [String]
    }
});
dojo.declare("cosmo.atompub.Title", cosmo.atompub.TextConstruct, {
    xmlName: "title"
});

dojo.declare("cosmo.atompub.Collection", cosmo.atompub.AppElement, {
    xmlName: "collection",
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

dojo.declare("cosmo.atompub.Generator", cosmo.atompub.AppElement, {
    xmlName: "generator",
    __attributes__: {
        uri: [String],
        version: [String]
    }
});
dojo.declare("cosmo.atompub.Icon", cosmo.atompub.AppElement, {
    xmlName: "icon",
    __elements__: {
        "uri": [cosmo.atompub.Uri]
    }
});
dojo.declare("cosmo.atompub.Id", cosmo.atompub.AppElement, {
    xmlName: "id"
});
dojo.declare("cosmo.atompub.Logo", cosmo.atompub.AppElement, {
    xmlName: "logo",
    __elements__: {
        "uri": [cosmo.atompub.Uri]
    }
});
dojo.declare("cosmo.atompub.Rights", cosmo.atompub.TextConstruct, {
    xmlName: "rights"
});
dojo.declare("cosmo.atompub.Subtitle", cosmo.atompub.TextConstruct, {
    xmlName: "subtitle"
});
dojo.declare("cosmo.atompub.Updated", cosmo.atompub.DateConstruct, {
    xmlName: "updated"
});
dojo.declare("cosmo.atompub.Name", cosmo.atompub.TextConstruct, {
    xmlName: "name"
});
dojo.declare("cosmo.atompub.Uri", cosmo.atompub.AppElement, {
    xmlName: "uri"
});
dojo.declare("cosmo.atompub.Email", cosmo.atompub.AppElement, {
    xmlName: "email"
});

dojo.declare("cosmo.atompub.Person",  cosmo.atompub.AppElement, {
    xmlName: "person",
    __elements__: {
        "name": [cosmo.atompub.Name],
        "uri": [cosmo.atompub.Uri],
        "email": [cosmo.atompub.Email]
    }
});

dojo.declare("cosmo.atompub.Author",  cosmo.atompub.Person, {
    xmlName: "author"
});
dojo.declare("cosmo.atompub.Category", cosmo.atompub.AppElement, {
    xmlName: "category"
});
dojo.declare("cosmo.atompub.Contributor", cosmo.atompub.Person, {
    xmlName: "contributor"
});
dojo.declare("cosmo.atompub.Link", cosmo.atompub.AppElement, {
    xmlName: "link",
    __attributes__: {
        "href": [String],
        "rel": [String],
        "type": [String],
        "hreflang": [String],
        "title": [String],
        "length": [String]
    },

    toString: function(){
        return this.rel + ": " + this.href
    }
});

dojo.declare("cosmo.atompub.Content", cosmo.atompub.TextConstruct, {
    xmlName: "content",
    __attributes__: {
        "type": [String],
        "src": [String]
    }
});

dojo.declare("cosmo.atompub.Published", cosmo.atompub.DateConstruct, {
    xmlName: "published"
});
dojo.declare("cosmo.atompub.Summary", cosmo.atompub.TextConstruct, {
    xmlName: "summary"
});

dojo.declare("cosmo.atompub.Source", cosmo.atompub.AppElement, {
    xmlName: "source",
    "title": [cosmo.atompub.Title],
    "updated": [cosmo.atompub.Updated],
    "rights": [cosmo.atompub.Rights],
    "id": [cosmo.atompub.Id],
    "generator": [cosmo.atompub.Generator],
    "icon": [cosmo.atompub.Icon],
    "logo": [cosmo.atompub.Logo],
    "subtitle": [cosmo.atompub.Subtitle],
    "author": [cosmo.atompub.Author, "authors"],
    "category": [cosmo.atompub.Category, "categories"],
    "contributor": [cosmo.atompub.Contributor, "contributors"],
    "link": [cosmo.atompub.Link, "links"]

});

dojo.declare("cosmo.atompub.Entry", cosmo.atompub.AppElement, {
    xmlName: "entry",
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
    },

    deserializeContent: function (contentDeserializers){
        if (this.content) return this.content.deserializeContent(contentDeserializers);
        else return null;
    }
});

dojo.declare("cosmo.atompub.Feed", cosmo.atompub.AppElement, {
    xmlName: "feed",
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
    },
    
    deserializeEntryContents: function(contentDeserializers){
        return dojo.lang.map(this.entries, this, 
                             function (entry){
                                 return entry.deserializeContent(contentDeserializers);
                             });
    }
});

dojo.declare("cosmo.atompub.Workspace", cosmo.atompub.AppElement, {
    xmlName: "workspace",
    __elements__: {
        "title": [cosmo.atompub.Title],
        "collection": [cosmo.atompub.Collection, "collections"]
    }
});

dojo.declare("cosmo.atompub.Service", cosmo.atompub.AppElement, {
    xmlName: "service",
    __elements__: {
        "workspace": [cosmo.atompub.Workspace, "workspaces"]
    },
    
    /**
     * Return a list of workspaces whose title matches <code>title</code>.
     */
    getWorkspacesWithTitle: function(title){
        return dojo.lang.filter(this.workspaces, 
                                function(workspace){
                                    return workspace.title.text == title
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
