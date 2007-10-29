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
 * A module that provides translators from data received from a
 * JSON-RPC service to cosmo.model.Object objects.
 */
dojo.provide("cosmo.service.translators.eim.common");

dojo.require("dojo.date.serialize");
dojo.require("dojo.lang.*");
dojo.require("dojo.json");
dojo.require("dojo.string");

dojo.require("cosmo.service.translators.eim.constants");
dojo.require("cosmo.model.*");
dojo.require("cosmo.service.translators.common");
dojo.require("cosmo.service.common");
dojo.require("cosmo.datetime.serialize");
dojo.require("cosmo.util.html");

dojo.declare("cosmo.service.translators.Eim", null, {
    COSMO_NS: "http://osafoundation.org/cosmo/Atom",
    
    initializer: function (urlCache){
        this.urlCache = urlCache;
        
        with (cosmo.service.translators.eim.constants.rrule){
        with (cosmo.model.RRULE_FREQUENCIES){
            this.rruleFrequenciesToRruleConstants = {};
            this.rruleFrequenciesToRruleConstants[FREQUENCY_DAILY] = DAILY;
            this.rruleFrequenciesToRruleConstants[FREQUENCY_WEEKLY] = WEEKLY;
            this.rruleFrequenciesToRruleConstants[FREQUENCY_BIWEEKLY] = WEEKLY + ";INTERVAL=2";
            this.rruleFrequenciesToRruleConstants[FREQUENCY_MONTHLY] = MONTHLY;
            this.rruleFrequenciesToRruleConstants[FREQUENCY_YEARLY] = YEARLY;
        }}
    },
    
    // a hash from link rels to useful url names
    urlNameHash: {
        "edit": "atom-edit",
        "self": "self", 
        "alternate": "pim", 
        "morse code": "mc", 
        "dav": "dav",
        "webcal": "webcal"
    },
    
    getUrls: function (xml){
            var urls = {};
            var links = cosmo.util.html.getElementsByTagName(xml, "link");
            for (var i = 0; i < links.length; i++){
                var link = links[i];
                var rel = link.getAttribute("rel");
                var href = link.getAttribute("href");
                urls[this.urlNameHash[rel] || rel] = href;
            }
            
            // Handle regular atom feed url differently.
            // This seems like kind of an ugly way to do this, but it works for now.
            if (urls['atom-edit']){
                var url = 
                   location.protocol + "//" + location.host + 
                   cosmo.env.getBaseUrl() + "/atom/" + urls['atom-edit'] 
                var urlParts = url.split("?");
                urlParts[0] = urlParts[0] + "/basic";
                url = urlParts.join("?");
                urls.atom = url;
            }
            
            return urls;
    },
    
    RID_FMT: "%Y%m%dT%H%M%S",

    // Wrap each of the specified property's getter with a function
    // that will call the lazy loader and then revert each
    // getter.
    setLazyLoader: function (object, propertyNames, loaderFunction){
        var oldGetters = {};
        var oldSetters = {};
        for (var i = 0; i < propertyNames.length; i++) {
            var propertyName = propertyNames[i];
            var getterName = "get" + dojo.string.capitalize(propertyName);
            var setterName = "set" + dojo.string.capitalize(propertyName);
            oldGetters[getterName] = object[getterName];
            oldSetters[setterName] = object[setterName];
            
            // This is needed to create a new scope so we can enclose "getter name"
            var createReplacerFunction = function(){
                
                var resetMethods = function(){
                    for (var oldGetterName in oldGetters){
                        object[oldGetterName] = oldGetters[oldGetterName];
                    }
                    for (var oldSetterName in oldSetters){
                        object[oldSetterName] = oldSetters[oldSetterName];
                    }
                }
                // Create a new variable to hold the current getter name
                // that won't get replaced during the next iteration
                // of the for loop.
                var myGetName = getterName;
                var mySetName = setterName;
                object[myGetName] = 
                    function(){   
                        // does the get and loads properties from new object into old object
                        resetMethods();
                        loaderFunction(object, propertyNames);
                        return object[myGetName]();
                    };
                object[mySetName] = 
                    function(val){   
                        // does the get and loads properties from new object into old object
                        resetMethods();
                        loaderFunction(object, propertyNames);
                        return object[mySetName](val);
                    };
            }
            createReplacerFunction();
        }
    },
    
    translateGetCollection: function (atomXml, oldCollection){

        var uid = atomXml.getElementsByTagName("id")[0].firstChild.nodeValue.substring(9);
        var displayName = cosmo.util.html.getElementsByTagName(atomXml, "title")[0].firstChild.nodeValue;
        var collection = oldCollection || new cosmo.model.Collection();
        collection.setUid(uid);
        collection.setDisplayName(displayName);
        var urls = this.getUrls(atomXml)
        collection.setUrls(urls);
        this.urlCache.setUrls(collection, urls);
        
        var selfUrl = collection.getUrls()['self'];
        if (!selfUrl.match(/.*ticket=.*/)) collection.setWriteable(true);
        else {
            var ticketElements = cosmo.util.html.getElementsByTagName(atomXml, "cosmo", "ticket");
            for (var i = 0; i < ticketElements.length; i++){
                var permission = this.getTicketType(ticketElements[i]);
                if (permission == "read-write") collection.setWriteable(true);
            }
        }
        
        return collection;
    },

    getTicketType: function (ticketEl){
        if (!(dojo.render.html.ie  && dojo.render.ver < 7)){
            return ticketEl.getAttributeNS(this.COSMO_NS, "type");
        } else {
            return ticketEl.getAttribute("cosmo:type");
        }
    },
          
    translateGetCollections: function (atomXml, kwArgs){
        kwArgs = kwArgs || {};
        var workspaces = atomXml.getElementsByTagName("workspace");
        var collections = [];
        for (var i = 0; i < workspaces.length; i++){
            var workspace = workspaces[i];
            
            var title = cosmo.util.html.getElementsByTagName(workspace, "atom", "title")[0];

            if (title.firstChild.nodeValue == "home"){
                var collectionElements = workspace.getElementsByTagName("collection");
                
                for (var j = 0; j < collectionElements.length; j++){
                    var collection = this.collectionXmlToCollection(collectionElements[j]);
                    collection.href = collectionElements[j].getAttribute("href");
                    this.setLazyLoader(collection, ["urls", "uid", "writeable"], kwArgs.lazyLoader);
                    collections.push(collection);
                }
            }
        }
        return collections;
    },
    
    translateGetSubscriptions: function (atomXml, kwArgs){
        // TODO: redo this with query api
        kwArgs = kwArgs || {};
        var entries = atomXml.getElementsByTagName("entry");
        var subscriptions = [];
        for (var i = 0; i < entries.length; i++){
            var entry = entries[i];
            var content = entry.getElementsByTagName("content")[0];
            var divWrapper = content.getElementsByTagName("div")[0];
            var subscriptionDiv = this.getChildrenByClassName(divWrapper, "local-subscription", "div")[0];
            var displayNameEl = this.getChildrenByClassName(subscriptionDiv, "name", "span")[0];
            var displayName = displayNameEl.firstChild.nodeValue;
            var ticketEl = this.getChildrenByClassName(subscriptionDiv, "ticket", "div")[0];
            var ticketKeyEl = this.getChildrenByClassName(ticketEl, "key", "span")[0];
            var ticket = ticketKeyEl.firstChild.nodeValue;
            var ticketExistsEl = this.getChildrenByClassName(ticketEl, "exists", "span")[0];
            var ticketDeleted = (ticketExistsEl.firstChild.nodeValue == "false");
            var collectionEl = this.getChildrenByClassName(subscriptionDiv, "collection", "div")[0];
            var collectionUidEl = this.getChildrenByClassName(collectionEl, "uuid", "span")[0];
            var uid = collectionUidEl.firstChild.nodeValue;
            var collectionExistsEl = this.getChildrenByClassName(collectionEl, "exists", "span")[0];
            var collectionDeleted = (collectionExistsEl.firstChild.nodeValue == "false");
            var collection = new cosmo.model.Collection({
                uid: uid
            });
            collection.href = "collection/" + uid + "?ticket=" + ticket;
            
            var subscription = new cosmo.model.Subscription({
                displayName: displayName,
                ticketKey: ticket,
                uid: uid,
                collection: collection,
                collectionDeleted: collectionDeleted,
                ticketDeleted: ticketDeleted
            })
            var urls = this.getUrls(entry)
            subscription.setUrls(urls);
            this.urlCache.setUrls(subscription, urls);
            this.setLazyLoader(collection, ["urls", "writeable"], kwArgs.lazyLoader);
            subscriptions.push(subscription);
        }
        return subscriptions;
    },
    
    translateGetPreferences: function(atomXml, kwArgs){
        kwArgs = kwArgs || {};
        var entries = atomXml.getElementsByTagName("entry");
        var preferences = {};
        for (var i = 0; i < entries.length; i++){
            var entry = entries[i];
            var content = entry.getElementsByTagName("content")[0];
            var wrapperDiv = content.getElementsByTagName("div")[0];
            var preference = this.preferenceXmlToPreference(this.getPreferenceDiv(wrapperDiv));
            preferences[preference[0]] = preference[1];
        }
        return preferences;
    },

    translateGetPreference: function(atomXml, kwArgs){
        kwArgs = kwArgs || {};

        var content = atomXml.getElementsByTagName("content")[0];
        var wrapperDiv = content.getElementsByTagName("div")[0];
        var preference = this.preferenceXmlToPreference(this.getPreferenceDiv(wrapperDiv));
        return preference[1];

    },
    
    // this is really weird, but ie7 appears to be having trouble with getAttribute
    // TODO: examine problem further
    getPreferenceDiv: function(xml){
        return this.getChildrenByClassName(xml, "preference", "div")[0];
    },
    
    getChildrenByClassName: function (xml, className, tagName){
        var nodes = xml.childNodes;
        var returnNodes = [];
        for (var i = 0; i < nodes.length; i++){
            var node = nodes[i];
            if ((node.nodeType != 1) || (tagName && tagName != node.tagName)) continue;
            var classNode = node.getAttributeNode("class")
            if (classNode && (classNode.nodeValue == className)){
                returnNodes.push(node);
            }
        }
        return returnNodes;
    },
    
    preferenceXmlToPreference: function(xml){
        var keyEl = this.getChildrenByClassName(xml, "key")[0];
        var key = keyEl.firstChild.nodeValue;
        var valueEl = this.getChildrenByClassName(xml, "value")[0];
        var value = valueEl.firstChild.nodeValue;
        
        // A little type converting
        if (value == "false") value = false;
        return [key,value];
    },
    
    collectionXmlToCollection: function (collectionXml){
        return collection = new cosmo.model.Collection(
            {
                displayName: cosmo.util.html.getElementsByTagName(collectionXml, "atom", "title")
                    [0].firstChild.nodeValue
            }
        );
    },
    
    translateExpandRecurringItem: function(atomXml){
        if (!atomXml){
            throw new cosmo.service.translators.ParseError("Cannot parse null, undefined, or false");
        }
        var entry = atomXml.getElementsByTagName("entry")[0];
        try {
            var contentEl = entry.getElementsByTagName("content")[0];
        } catch (e){
            throw new cosmo.service.translators.
               ParseError("Could not find content element for entry " + (i+1));
        }
        var content = cosmo.util.html.getElementTextContent(contentEl);
        
        var recordSets = dojo.json.evalJson(content);

        var masterItem = this.recordSetToObject(recordSets[0]);
        var urls = this.getUrls(entry)
        masterItem.setUrls(urls);
        this.urlCache.setUrls(masterItem, urls);

        var items = [];
        // All record sets after the first are occurrences
        for (var i = 1; i < recordSets.length; i++){
           var item = this.recordSetToModification(recordSets[i], masterItem)
           //TODO: remove this hack to get edit urls into modifications
           if (item.hasModification()){
              item.setUrls({"atom-edit": "item/" + this.getUid(item)});
           }
           items.push(item); 
        }
        return items;
    },
    
    translateGetItem: function(atomXml, kwArgs){
        if (!atomXml){
            throw new cosmo.service.translators.ParseError("Cannot parse null, undefined, or false");
        }
        
        var entry = atomXml.getElementsByTagName("entry")[0];
        return this.entryToItem(entry, null, kwArgs);
          
    },
    
    translateSaveCreateItem: function(atomXml, kwArgs){
        kwArgs = kwArgs || {};
        if (!atomXml){
            throw new cosmo.service.translators.ParseError("Cannot parse null, undefined, or false");
        }
        
        var entry = atomXml.getElementsByTagName("entry")[0];
        return this.entryToItem(entry, kwArgs.masterItem, kwArgs);
          
    },
    
    translateGetItems: function (atomXml){
        if (!atomXml){
            throw new cosmo.service.translators.ParseError("Cannot parse null, undefined, or false");
        }
        var entries = atomXml.getElementsByTagName("entry");
        return this.entriesToItems(entries);
    },
    
    translateGetDashboardItems: function (atomXml){
        if (!atomXml){
            throw new cosmo.service.translators.ParseError("Cannot parse null, undefined, or false");
        }
        return atomXml.getElementsByTagName("entry");
    },
    
    entriesToItems: function(entries){
        var items = {};
        var mods = {};
        for (var i = 0; i < entries.length; i++){
            var entry = entries[i];
            var uuid = this.getEntryUuid(entry);
            if (!uuid.split(":")[1]){
                items[uuid] = this.entryToItem(entry)
            }
            else {
                mods[uuid] = entry;
            }
        }
        
        // Remove the master events at the end, cause they're returned as occurrences
        var masterRemoveList = {};

        for (var uuid in mods){
            var masterUuid = uuid.split(":")[0];
            var masterItem = items[uuid.split(":")[0]];
            
            // Per Jeffrey's suggestion, fail silently here, logging 
            // an error message to the debug console.
            if (!masterItem) dojo.debug(
              "Could not find master event for modification " +
              "with uuid " + uuid);

            items[uuid] = this.entryToItem(mods[uuid], masterItem);
            masterRemoveList[masterUuid] = true;
        }
        for (var uuid in masterRemoveList){
            delete items[uuid];
        }
        
        var itemArray = [];
        for (var uid in items){
            itemArray.push(items[uid]);
        }
        return itemArray;
    },

    entryToItem: function (/*XMLElement*/entry, /*cosmo.model.Item*/ masterItem, kwArgs){
            var uuid = this.getEntryUuid(entry);
            var uuidParts = uuid.split(":");
            var uidParts = uuidParts.slice(2);
            try {
                var contentElement = entry.getElementsByTagName("content")[0];
            } catch (e){
                throw new cosmo.service.translators.
                   ParseError("Could not find content element for entry " + (i+1));
            }

            var content = cosmo.util.html.getElementTextContent(contentElement);

            var item;
            // If we have a second part to the uid, this entry is a
            // recurrence modification.
            if (masterItem){
                item = this.recordSetToModification(dojo.json.evalJson(content), masterItem, kwArgs); 
            }
            else {
                item = this.recordSetToObject(dojo.json.evalJson(content), kwArgs);
            }
            var urls = this.getUrls(entry)
            if (item.isMaster() || (item.isOccurrence() && item.hasModification())){
                item.setUrls(urls);
            }
            this.urlCache.setUrls(item, urls);
            return item;
    },
    
    
    /*
     * 
     */
    
    recurrenceIdToDate: function (/*String*/ rid, masterItemStartDate){
         return cosmo.datetime.fromIso8601(rid, masterItemStartDate.tzId);
    },

    subscriptionToAtomEntry: function (subscription){
         return ['<entry xmlns="http://www.w3.org/2005/Atom" xmlns:cosmo="http://osafoundation.org/cosmo/Atom">',
         '<content type="xhtml">',
          '<div xmlns="http://www.w3.org/1999/xhtml">',
            '<div class="local-subscription">',
                 '<span class="name">', dojo.string.escapeXml(subscription.getDisplayName()), '</span>', 
              '<div class="collection">',
                 '<span class="uuid">', dojo.string.escapeXml(subscription.getUid()), '</span>',
              '</div>',
              '<div class="ticket">',
                 '<span class="key">', dojo.string.escapeXml(subscription.getTicketKey()), '</span>',
              '</div>',
            '</div>',
          '</div>',
         '</content>',
         '</entry>'].join("");
    },
    
    collectionToSaveRepresentation: function(collection){
         return ['<entry xmlns="http://www.w3.org/2005/Atom" xmlns:cosmo="http://osafoundation.org/cosmo/Atom">',
         '<content type="xhtml">',
          '<div xmlns="http://www.w3.org/1999/xhtml">',
            '<div class="collection">',
                 '<span class="name">', dojo.string.escapeXml(collection.getDisplayName()), '</span>', 
            '</div>',
          '</div>',
         '</content>',
         '</entry>'].join("");
    },
    
    keyValToPreference: function(key, val){
        return this.createEntry({
            contentType: "xhtml",
            content: [
                '<div xmlns="http://www.w3.org/1999/xhtml">',
                    '<div class="preference">',
                        '<span class="key">', key, '</span>', '<span class="value">', val, '</span>',
                    '</div>',
                '</div>'].join("")
                
        });
    },
    
    createEntry: function(fields){
        var entryList = ['<entry xmlns="http://www.w3.org/2005/Atom">'];
        if (fields.title) entryList = 
            entryList.concat(['<title>', fields.title, '</title>']);
        if (fields.id) entryList = 
            entryList.concat(['<id>',  fields.id, '</id>']);
        if (fields.updated) entryList = 
            entryList.concat(['<updated>', fields.updated, '</updated>']);
        if (fields.authorName) entryList = 
            entryList.concat(['<author><name>', fields.authorName, '</name></author>']);
        if (fields.content) entryList = 
            entryList.concat(['<content type="', fields.contentType, '">', fields.content, '</content>']);
        entryList.push('</entry>');
        return entryList.join("");
    },

    itemToAtomEntry: function (object){
         return ['<entry xmlns="http://www.w3.org/2005/Atom">',
                 '<title>', dojo.string.escapeXml(object.getDisplayName()), '</title>',
                 '<id>urn:uuid:', dojo.string.escapeXml(this.getUid(object)), '</id>',
                 '<updated>', dojo.string.escapeXml(dojo.date.toRfc3339(new Date())), '</updated>',
                 '<author><name>', dojo.string.escapeXml(cosmo.util.auth.getUsername()), '</name></author>',
                 '<content type="text/eim+json"><![CDATA[', dojo.json.serialize(this.objectToRecordSet(object)), ']]></content>',
                 '</entry>'].join("");
    },
    
    getRid: dojo.lang.hitch(cosmo.service, cosmo.service.getRid),

    getEntryUuid: function (entry){
        try {
            var uuid = entry.getElementsByTagName("id")[0];
        } catch (e){
            throw new cosmo.service.translators.
               ParseError("Could not find id element for entry " + entry);
        }
        uuid = unescape(uuid.firstChild.nodeValue.substring(9));
        return uuid;
    }
});
