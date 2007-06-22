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
dojo.provide("cosmo.service.translators.eim");

dojo.require("dojo.date.serialize");
dojo.require("dojo.lang.*");
dojo.require("dojo.json");
dojo.require("dojo.string");

dojo.require("cosmo.service.eim");
dojo.require("cosmo.model.*");
dojo.require("cosmo.service.translators.common");
dojo.require("cosmo.datetime.serialize");
dojo.require("cosmo.util.html");

dojo.declare("cosmo.service.translators.Eim", null, {
    
    initializer: function (){
        with (this.rruleConstants) {
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
        "alternate": "alternate", 
        "morse code": "mc", 
        "dav": "dav",
        "webcal": "webcal"
    },
    
    getUrls: function (xml){
            var urls = {};//item.getUrls();
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
        collection.setUrls(this.getUrls(atomXml));
        
        var selfUrl = collection.getUrls()['self'];
        if (!selfUrl.match(/.*ticket=.*/)) collection.setWriteable(true);
        else {
            var ticketElements = cosmo.util.html.getElementsByTagName(atomXml, "cosmo", "ticket");
            for (var i = 0; i < ticketElements.length; i++){
                var permission = ticketElements[i].getAttribute("cosmo:type");
                if (permission == "read-write") collection.setWriteable(true);
            }
        }
        
        return collection;
    },
          
    translateGetCollections: function (atomXml, kwArgs){
        kwArgs = kwArgs || {};
        var workspaces = atomXml.getElementsByTagName("workspace");
        var collections = [];
        for (var i = 0; i < workspaces.length; i++){
            var workspace = workspaces[i];
            
            var title = cosmo.util.html.getElementsByTagName(workspace, "atom", "title")[0];

            if (title.firstChild.nodeValue == "meta") continue;

            var collectionElements = workspace.getElementsByTagName("collection");
            
            for (var j = 0; j < collectionElements.length; j++){
                var collection = this.collectionXmlToCollection(collectionElements[j]);
                collection.href = collectionElements[j].getAttribute("href");
                this.setLazyLoader(collection, ["urls", "uid", "writeable"], kwArgs.lazyLoader);
                collections.push(collection);
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
            var collectionEl = this.getChildrenByClassName(subscriptionDiv, "collection", "div")[0];
            var collectionUidEl = this.getChildrenByClassName(collectionEl, "uuid", "span")[0];
            var collectionExistsEl = this.getChildrenByClassName(collectionEl, "exists", "span")[0];
            var uid = collectionUidEl.firstChild.nodeValue;
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
                collectionDeleted: collectionDeleted
            })

            subscription.setUrls(this.getUrls(entry));
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
        masterItem.setUrls(this.getUrls(entry));

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
            if (!masterItem) throw new cosmo.service.translators.ParseError(
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
            if (item.isMaster() || (item.isOccurrence() && item.hasModification())){
                item.setUrls(this.getUrls(entry));
            }
            return item;
    },
    
    recordSetToObject: function (/*Object*/ recordSet, kwArgs){
        kwArgs = kwArgs || {};
        //TODO
        /* We can probably optimize this by grabbing the
         * appropriate properties from the appropriate records
         * and passing them into the constructor. This will probably
         * be a little less elegant, and will require the creation of
         * more local variables, so we should play with this later.
         */
        var note = kwArgs.oldObject || new cosmo.model.Note(
         {
             uid: recordSet.uuid
         }
        );
        for (recordName in recordSet.records){
        with (cosmo.service.eim.constants){

           var record = recordSet.records[recordName]

           switch(recordName){

           case prefix.ITEM:
               note.initializeProperties(this.itemRecordToItemProps(record), {noDefaults: true})
               break;
           case prefix.NOTE:
               note.initializeProperties(this.noteRecordToNoteProps(record), {noDefaults: true})
               break;
           case prefix.MODBY:
               note.setModifiedBy(new cosmo.model.ModifiedBy(this.modbyRecordToModbyProps(record)));
               break;
           case prefix.EVENT:
               note.getStamp(prefix.EVENT, true, this.getEventStampProperties(record));
               break;
           case prefix.TASK:
              note.getStamp(prefix.TASK, true, this.getTaskStampProperties(record));
               break;
           case prefix.MAIL:
              note.getStamp(prefix.MAIL, true, this.getMailStampProperties(record));
              break;
           }
        }

        }
        return note;

    },
    
    /*
     * 
     */
    recordSetToModification: function (recordSet, masterItem, kwArgs){
        kwArgs = kwArgs || {};
        var uidParts = recordSet.uuid.split(":");
        
        var modifiedProperties = {};
        var modifiedStamps = {};
        var deletedStamps = {};
        for (stampName in masterItem._stamps){
            deletedStamps[stampName] = true;
        }

        for (recordName in recordSet.records){
            deletedStamps[recordName] = false;
            with (cosmo.service.eim.constants){
               var record = recordSet.records[recordName];
                
               switch(recordName){
    
               case prefix.ITEM:
                   dojo.lang.mixin(modifiedProperties, this.itemRecordToItemProps(record));
                   break;
               case prefix.NOTE:
                    dojo.lang.mixin(modifiedProperties, this.noteRecordToNoteProps(record));
                   break;
               case prefix.MODBY:
                    modifiedProperties.modifiedBy = new cosmo.model.ModifiedBy(this.modbyRecordToModbyProps(record));
                   break;
               case prefix.EVENT:
                   modifiedStamps[prefix.EVENT] = this.getEventStampProperties(record);
                   break;
               case prefix.TASK:
                   modifiedStamps[prefix.TASK] = this.getTaskStampProperties(record);
                   break;
               case prefix.MAIL:
                   modifiedStamps[prefix.MAIL] = this.getMailStampProperties(record);
                   break;
               }
            }
        }
        var recurrenceId = this.recurrenceIdToDate(uidParts[1], masterItem.getEventStamp().getStartDate());
        
        if (!dojo.lang.isEmpty(modifiedProperties)
            || !dojo.lang.isEmpty(modifiedStamps)){
            
            var mod = new cosmo.model.Modification(
                {
                    "recurrenceId": recurrenceId,
                    "modifiedProperties": modifiedProperties,
                    "modifiedStamps": modifiedStamps,
                    "deletedStamps": deletedStamps
                }
            );
            masterItem.addModification(mod);
        }
        
        return kwArgs.oldObject || masterItem.getNoteOccurrence(recurrenceId);
    },
    
    recurrenceIdToDate: function (/*String*/ rid, masterItemStartDate){
         return cosmo.datetime.fromIso8601(rid, masterItemStartDate.tzId);
    },

    subscriptionToAtomEntry: function (subscription){
         return ['<entry xmlns="http://www.w3.org/2005/Atom" xmlns:cosmo="http://osafoundation.org/cosmo/Atom">',
         '<content type="xhtml">',
          '<div xmlns="http://www.w3.org/1999/xhtml">',
            '<div class="local-subscription">',
              '<span class="name">', subscription.getDisplayName(), '</span>', 
              '<div class="collection">',
                '<span class="uuid">', subscription.getUid(), '</span>',
              '</div>',
              '<div class="ticket">',
                '<span class="key">', subscription.getTicketKey(), '</span>',
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
              '<span class="name">', escape(collection.getDisplayName()), '</span>', 
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
         '<title>', object.getDisplayName(), '</title>',
         '<id>urn:uuid:', this.getUid(object), '</id>',
         '<updated>', dojo.date.toRfc3339(new Date()), '</updated>',
         '<author><name>', cosmo.util.auth.getUsername(), '</name></author>',
         '<content type="application/eim+json">', dojo.json.serialize(this.objectToRecordSet(object)), '</content>',
         '</entry>'].join("");
    },
    
    getUid: function (/*cosmo.model.Note*/ note){
        if (note instanceof cosmo.model.NoteOccurrence){
            return note.getUid() + ":" + this.getRid(note.recurrenceId);
        } else {
            return note.getUid();
        }
    },
    
    getRid: function(/*cosmo.datetime.Date*/date){
        if (date.isFloating()) {
            return date.strftime(this.RID_FMT);
        } else {
            return  date.createDateForTimezone("utc").strftime(this.RID_FMT + "Z");
        }
    },

    objectToRecordSet: function (note){
        if (note instanceof cosmo.model.NoteOccurrence){
            return this.noteOccurrenceToRecordSet(note);
        } else if (note instanceof cosmo.model.Note){
            return this.noteToRecordSet(note);
        } else {
            throw new cosmo.service.translators.exception.ModelToRecordSetException(
                "note is neither a Note nor a NoteOccurrence, don't know how to translate."
            )
        }
    },
    
    addStampsToDelete: function (recordSet, note){
        var stampsToDelete = note.getStampsToDelete();
        if (stampsToDelete.length > 0){
            recordSet.deletedRecords = stampsToDelete;
            note.clearStampsToDelete();
        }
    },
    
    noteToRecordSet: function(note){
        var records = {
            item: this.noteToItemRecord(note),
            note: this.noteToNoteRecord(note),
            modby: this.noteToModbyRecord(note)
        };

        if (note.getEventStamp()) records.event = this.noteToEventRecord(note);
        if (note.getTaskStamp()) records.task = this.noteToTaskRecord(note);
        if (note.getMailStamp()) records.mail = this.noteToMailRecord(note);
        
        var recordSet =  {
            uuid: this.getUid(note),
            records: records
        };
        
        this.addStampsToDelete(recordSet, note);
        
        return recordSet;
    },

    noteOccurrenceToRecordSet: function(noteOccurrence){
        var modification = noteOccurrence.getMaster().getModification(noteOccurrence.recurrenceId);
        var records = {
            modby: this.noteToModbyRecord(noteOccurrence)
        }
        if (this.modificationHasItemModifications(modification))  
            records.item =  this.modifiedOccurrenceToItemRecord(noteOccurrence);
        else records.item = this.generateEmptyItem(noteOccurrence);

        if (this.modificationHasNoteModifications(modification))
            records.note = this.modifiedOccurrenceToNoteRecord(noteOccurrence);
        else records.note = this.generateEmptyNote(noteOccurrence);
        
        records.event = this.modifiedOccurrenceToEventRecord(noteOccurrence)
        
        if (modification.getModifiedStamps().task){
            records.task = this.modifiedOccurrenceToTaskRecord(modification)
        }
        if (modification.getModifiedStamps().mail){
            records.mail = this.modifiedOccurrenceToMailRecord(noteOccurrence)
        }

        var recordSet =  {
            uuid: this.getUid(noteOccurrence),
            records: records
        };
        
        this.addStampsToDelete(recordSet, noteOccurrence);
        
        return recordSet;
        
    },
    
    modificationHasItemModifications: function (modification){
        var props = modification.getModifiedProperties();
        return (props.displayName || props.triageRank || props.triageStatus || props.autoTriage)    
    },

    noteToItemRecord: function(note){
        var props = {};
        props.displayName = note.getDisplayName();
        props.rank = note.getRank();
        props.triageStatus = note.getTriageStatus();
        props.autoTriage = note.getAutoTriage();
        props.uuid = this.getUid(note);
        return this.propsToItemRecord(props);
    },
    
    modifiedOccurrenceToItemRecord: function(modifiedOccurrence){
        var modification = modifiedOccurrence.getMaster().getModification(modifiedOccurrence.recurrenceId)
        var props = modification.getModifiedProperties();
        props.uuid = this.getUid(modifiedOccurrence);
        var record = this.propsToItemRecord(props);
        var missingFields = [];
        if (record.fields.title == undefined) missingFields.push("title");
        if (record.fields.triage == undefined) missingFields.push("triage");
        record.missingFields = missingFields;
        return record;
    },
    
    generateEmptyItem: function(note){
        var record = this.propsToItemRecord({uuid: note.getUid()});
        record.missingFields = [
            "title",
            "triage",
            "hasBeenSent",
            "needsReply"
        ]
        return record;
    },
    
    propsToItemRecord: function(props){
        var fields = {};
        with (cosmo.service.eim.constants){
        
            if (props.displayName != undefined) fields.title = [type.TEXT, props.displayName];
            if (props.triageStatus || props.triageRank || props.autoTriage)
                fields.triage =  [type.TEXT, [props.triageStatus, this.fixTriageRank(props.rank), props.autoTriage? 1 : 0].join(" ")];
            
            return {
                prefix: prefix.ITEM,
                ns: ns.ITEM,
                key: {
                    uuid: [type.TEXT, props.uuid]
                },
                fields: fields
            }
        }
    
    },
    
    // Make sure triage rank ends in two decimals
    fixTriageRank: function(rank){
        rank = rank || "0";
        if (rank.toString().match(/d*\.\d\d/)) return rank;
        else return rank + ".00";
    },

    noteToNoteRecord: function(note){
        var props = {}
        props.body = note.getBody();
        props.uuid = this.getUid(note);
        return this.propsToNoteRecord(props);
    },
    
    modificationHasNoteModifications: function (modification){
        return !!modification.getModifiedProperties().body;
    },
    
    
    modifiedOccurrenceToNoteRecord: function(modifiedOccurrence){
        var modification = modifiedOccurrence.getMaster().getModification(modifiedOccurrence.recurrenceId)
        var props = modification.getModifiedProperties();
        props.uuid = this.getUid(modifiedOccurrence);
        var record = this.propsToNoteRecord(props);
        var missingFields = [];
        if (record.fields.body == undefined) missingFields.push("body");
        record.missingFields = missingFields;
        return record;
    },
    
    generateEmptyNote: function (note){
        var record = this.propsToItemRecord({uuid: note.getUid()});
        record.missingFields = [
            "body"
        ];
        return record;
    },
    
    propsToNoteRecord: function (props){
        with (cosmo.service.eim.constants){
            var fields = {};
            if (props.body != undefined) fields.body = [type.CLOB, props.body];
            return {
                prefix: prefix.NOTE,
                ns: ns.NOTE,
                key: {
                    uuid: [type.TEXT, props.uuid]
                },
                fields: fields
            }
        }
    },

    noteToMailRecord: function(note){
        var props = {};
        stamp = note.getMailStamp();
        props.messageId = stamp.getMessageId();
        props.headers = stamp.getHeaders();
        props.fromAddress = stamp.getFromAddress();
        props.toAddress = stamp.getToAddress();
        props.ccAddress = stamp.getCcAddress();
        props.bccAddress = stamp.getBccAddress();
        props.originators = stamp.getOriginators();
        props.dateSent = stamp.getDateSent();
        props.inReplyTo = stamp.getInReplyTo();
        props.references = stamp.getReferences();
        props.uuid = this.getUid(note);
        return this.propsToMailRecord(props);

    },

    modifiedOccurrenceToMailRecord: function(modifiedOccurrence){
        var modification = modifiedOccurrence.getMaster().getModification(modifiedOccurrence.recurrenceId);
        var props = modification.getModifiedStamps().mail || {};
        props.uuid = modifiedOccurrence.getUid();
        var record = this.propsToMailRecord(props);
        var missingFields = [];
        if (record.fields.messageId == undefined) missingFields.push("messageId");
        if (record.fields.headers == undefined) missingFields.push("headers");
        if (record.fields.fromAddress == undefined) missingFields.push("fromAddress");
        if (record.fields.toAddress == undefined) missingFields.push("toAddress");
        if (record.fields.ccAddress == undefined) missingFields.push("ccAddress");
        if (record.fields.bccAddress == undefined) missingFields.push("bccAddress");
        if (record.fields.originators == undefined) missingFields.push("originators");
        if (record.fields.dateSent == undefined) missingFields.push("dateSent");
        if (record.fields.inReplyTo == undefined) missingFields.push("inReplyTo");
        if (record.fields.references == undefined) missingFields.push("references");
        record.missingFields = missingFields;
        return record;
    },
    
    propsToMailRecord: function(props){
        with (cosmo.service.eim.constants){
            var fields = {};
            var missingFields = [];
            if (props.messageId != undefined) fields.messageId = [type.TEXT, props.messageId];
            if (props.headers != undefined) fields.headers = [type.CLOB, props.headers];
            if (props.fromAddress != undefined) fields.fromAddress = [type.TEXT, props.fromAddress];
            if (props.toAddress != undefined) fields.toAddress = [type.TEXT, props.toAddress];
            if (props.ccAddress != undefined) fields.ccAddress = [type.TEXT, props.ccAddress];
            if (props.bccAddress != undefined) fields.bccAddress = [type.TEXT, props.bccAddress];
            if (props.originators != undefined) fields.originators = [type.TEXT, props.originators];
            if (props.dateSent != undefined) fields.dateSent = [type.TEXT, props.dateSent];
            if (props.inReplyTo != undefined) fields.inReplyTo = [type.TEXT, props.inReplyTo];
            if (props.references != undefined) fields.references = [type.CLOB, props.references];
            
            return record = {
                prefix: prefix.MAIL,
                ns: ns.MAIL,
                key: {
                    uuid: [type.TEXT, props.uuid]
                },
                fields: fields
            }
            return record;
        }   
    },
    
    noteToEventRecord: function(note){
        var props = {};
        stamp = note.getEventStamp();
        props.allDay = stamp.getAllDay();
        props.anyTime = stamp.getAnyTime();
        props.startDate = stamp.getStartDate();
        props.rrule = stamp.getRrule();
        props.status = stamp.getStatus();
        props.location = stamp.getLocation();
        props.duration = stamp.getDuration();
        props.exdates = stamp.getExdates();
        props.uuid = this.getUid(note);
        return this.propsToEventRecord(props);

    },

    modifiedOccurrenceToEventRecord: function(modifiedOccurrence){
        var modification = modifiedOccurrence.getMaster().getModification(modifiedOccurrence.recurrenceId);
        var props = modification.getModifiedStamps().event || {};
        props.uuid = modifiedOccurrence.getUid();
        var record = this.propsToEventRecord(props);
        var missingFields = [];
        if (record.fields.dtstart == undefined) missingFields.push("dtstart");
        if (record.fields.status == undefined) missingFields.push("status");
        if (record.fields.location == undefined) missingFields.push("location");
        if (record.fields.duration == undefined) missingFields.push("duration");
        record.missingFields = missingFields;
        return record;
    },
    
    propsToEventRecord: function(props){
        with (cosmo.service.eim.constants){
            var fields = {};
            if (props.startDate != undefined) fields.dtstart = 
                [type.TEXT, this.dateToEimDtstart(props.startDate, props.allDay, props.anyTime)];
            if (props.status != undefined) fields.status = [type.TEXT, props.status];
            if (props.location != undefined) fields.location = [type.TEXT, props.location];
            if (props.duration != undefined) fields.duration = [type.TEXT, props.duration.toIso8601()];
            if (props.rrule != undefined) fields.rrule = [type.TEXT, this.rruleToICal(props.rrule)];
            if (props.exdates != undefined) fields.exdates = [type.TEXT, this.exdatesToEim(props.exdates)];
            
            return record = {
                prefix: prefix.EVENT,
                ns: ns.EVENT,
                key: {
                    uuid: [type.TEXT, props.uuid]
                },
                fields: fields
            }
        }

        
    },

    exdatesToEim: function(exdates){
        return ";VALUE=DATE-TIME:" + dojo.lang.map(
                exdates,
                function(date){return date.strftime("%Y%m%dT%H%M%S");}
            ).join(",");
    },
    
    dateToEimDtstart: function (start, allDay, anyTime){
        return [(anyTime? ";X-OSAF-ANYTIME=TRUE" : ""),
                ";VALUE=",
                ((allDay || anyTime)? "DATE" : "DATE-TIME"),
                ":",
                ((allDay || anyTime)?
                    start.strftime("%Y%m%d"):
                    start.strftime("%Y%m%dT%H%M%S"))
                ].join("");
        
    },

    noteToTaskRecord: function (note){

        var stamp = note.getTaskStamp();

        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.TASK,
                ns: ns.TASK,
                key: {
                    uuid: [type.TEXT, this.getUid(note)]
                },
                fields: {}

            }
        }

    },

    noteToModbyRecord: function(note){
        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.MODBY,
                ns: ns.MODBY,
                key:{
                    uuid: [type.TEXT, this.getUid(note)],
                    userid: [type.TEXT, note.getModifiedBy().getUserId() || 
                                        cosmo.util.auth.getUsername()],
                    action: [type.INTEGER, 100], //TODO: figure this out
                    timestamp: [type.DECIMAL, new Date().getTime()]
                }
            }
        }
    },

    getEventStampProperties: function (record){

        var properties = {};
        if (record.fields){
            if (record.fields.dtstart){
                properties.startDate = this.fromEimDate(record.fields.dtstart[1]);
                var dateParams = this.dateParamsFromEimDate(record.fields.dtstart[1]);
                if (dateParams.anyTime != undefined) properties.anyTime = dateParams.anyTime;
                if (dateParams.allDay != undefined) properties.allDay = dateParams.allDay;
            }
    
            if (record.fields.duration) properties.duration=
                    new cosmo.model.Duration(record.fields.duration[1]);
            if (record.fields.location) properties.location = record.fields.location[1];
            if (record.fields.rrule) properties.rrule = this.parseRRule(record.fields.rrule[1], properties.startDate);
            if (record.fields.exrule) properties.exrule = this.parseRRule(record.fields.exrule[1]), properties.startDate;
            if (record.fields.exdate) properties.exdates = this.parseExdate(record.fields.exdate[1]);
            if (record.fields.status) properties.status = record.fields.status[1];
        }
        return properties;

    },

    getTaskStampProperties: function (record){

        return {};
    },
 
    getMailStampProperties: function (record){
        var properties = {};
        if (record.fields){
            if (record.fields.messageId) properties.messageId = record.fields.messageId[1];
            if (record.fields.headers) properties.headers = record.fields.headers[1];
            if (record.fields.fromAddress) properties.fromAddress = record.fields.fromAddress[1];
            if (record.fields.toAddress) properties.toAddress = record.fields.toAddress[1];
            if (record.fields.ccAddress) properties.ccAddress = record.fields.ccAddress[1];
            if (record.fields.bccAddress) properties.bccAddress = record.fields.bccAddress[1];
            if (record.fields.originators) properties.originators = record.fields.originators[1]
            if (record.fields.dateSent) properties.dateSent = record.fields.dateSent[1]; //TODO: parse
            if (record.fields.inReplyTo) properties.inReplyTo = record.fields.inReplyTo[1];
            if (record.fields.references) properties.references = record.fields.references[1];
        }
        return properties;
    },
    
    parseList: function(listString){
       if (!listString) return listString;
       else return listString.split(",");
    },
    
    itemRecordToItemProps: function(record){
        var props = {};
        if (record.fields){
            if (record.fields.title) props.displayName = record.fields.title[1];
            if (record.fields.createdOn) props.creationDate = record.fields.createdOn[1];
            if (record.fields.triage) this.addTriageStringToItemProps(record.fields.triage[1], props);
        }
        return props;
    },

    noteRecordToNoteProps: function(record){
        var props = {};
        if (record.fields){
            if (record.fields.body) props.body = record.fields.body[1];
        }
        return props;
    },

    modbyRecordToModbyProps: function(record){
        var props = {};
        if (record.fields){
            if (record.fields.userid) props.userId = record.fields.userid[1];
            if (record.fields.timestamp) props.timeStamp = record.fields.timestamp[1];
            if (record.fields.action) props.action = record.fields.action[1];
        }
        return props;
    },

    fromEimDate: function (dateString){
        var dateParts = dateString.split(":");
        var dateParamList = dateParts[0].split(";");
        var dateParams = {};
        for (var i = 0; i < dateParamList.length; i++){
            var keyValue = dateParamList[i].split("=");
            dateParams[keyValue[0].toLowerCase()] = keyValue[1];
        }
        var tzId = dateParams['tzid'] || null;
        var jsDate = dojo.date.fromIso8601(dateParts[1]);
        var date = new cosmo.datetime.Date();
        date.tzId = tzId;

        date.setYear(jsDate.getFullYear());
        date.setMonth(jsDate.getMonth());
        date.setDate(jsDate.getDate());
        date.setHours(jsDate.getHours());
        date.setMinutes(jsDate.getMinutes());
        date.setSeconds(jsDate.getSeconds());
        date.setMilliseconds(0);
        return date;
    },

    addTriageStringToItemProps: function (triageString, props){
        if (dojo.string.trim(triageString) == "" || triageString == null){
            props.autoTriage = true;
            props.triageStatus = null;
            return;
        }
        var triageArray = triageString.split(" ");

        props.triageStatus = triageArray[0];

        props.rank = triageArray[1];

        /* This looks weird, but because of JS's weird casting stuff, it's necessary.
         * Try it if you don't believe me :) - travis@osafoundation.org
         */
        props.autoTriage = triageArray[2] == true;
    },

    dateParamsFromEimDate: function (dateString){
        var returnVal = {};
        var params = dateString.split(":")[0].split(";");
        for (var i = 0; i < params.length; i++){
            var param = params[i].split("=");
            if (param[0].toLowerCase() == "x-osaf-anytime") {
                returnVal.anyTime = true;
            }
            if (param[0].toLowerCase() == "value") {
                returnVal.value = param[1].toLowerCase();
            }
        }
        
        if ((returnVal.value == "date") && !returnVal.anyTime) returnVal.allDay = true;
        return returnVal;
    },
    
    rruleToICal: function (rrule){
        if (rrule.isSupported()){
            var recurrenceRuleList = [
               ";FREQ=",
                this.rruleFrequenciesToRruleConstants[rrule.getFrequency()]
             ]
             var endDate = rrule.getEndDate();
             if (endDate){
                recurrenceRuleList.push(";UNTIL=");
                var dateString = this._createRecurrenceEndDateString(rrule.getEndDate())
                recurrenceRuleList.push(dateString);
             }
            
            return recurrenceRuleList.join("");
        } 
        else {
            return rrulePropsToICal(rrule.getUnsupportedRule());
        }
    },
    
    _createRecurrenceEndDateString: function (date){
        date = date.clone();
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        date = date.createDateForTimezone("utc");
        return dojo.date.strftime(date, "%Y%m%dT%H%M%SZ")
    },

    rrlePropsToICal: function (rProps, startDate){
        var iCalProps = [];
        for (var key in rProps){
            iCalProps.push(key);
            iCalProps.push("=")
            if (dojo.lang.isArray(rProps[key])){
                iCalProps.push(rProps[key].join());
            }
            else if (rProps[key] instanceof cosmo.datetime.Date){
                var dateString = this._createRecurrenceEndDateString(rProps[key]);
                iCalProps.push(dateString);
            }
            else {
                iCalProps.push(rProps[key]);
            }
            iCalProps.push(";");
            return iCalProps.join("");
        }
    },

    parseRRule: function (rule, startDate){
        if (!rule) {
            return null;
        }
        return this.rPropsToRRule(this.parseRRuleToHash(rule), startDate);
    },
    
    parseExdate: function (exdate){
        if (!exdate) return null;
        return dojo.lang.map(
                exdate.split(":")[1].split(","),
                function (exdate, index) {return cosmo.datetime.fromIso8601(exdate)}
         );
    },

    //Snagged from dojo.cal.iCalendar
    parseRRuleToHash: function (rule){
        var rrule = {}
        var temp = rule.split(";");
        for (var y=0; y<temp.length; y++) {
            if (temp[y] != ""){
                var pair = temp[y].split("=");
                var key = pair[0].toLowerCase();
                var val = pair[1];
                if ((key == "freq") || (key=="interval") || (key=="until")) {
                    rrule[key]= val;
                } else {
                    var valArray = val.split(",");
                    rrule[key] = valArray;
                }
            }
        }
        return rrule;
    },

    rruleConstants: {
      SECONDLY: "SECONDLY",
      MINUTELY: "MINUTELY",
      HOURLY: "HOURLY",
      DAILY: "DAILY",
      MONTHLY:"MONTHLY",
      WEEKLY:  "WEEKLY",
      YEARLY: "YEARLY"
    },
    
    isRRuleUnsupported: function (recur){

        with (this.rruleConstants){

        if (recur.freq == SECONDLY
                || recur.freq == MINUTELY) {
            return true;
        }
        //If they specified a count, it's custom
        if (recur.count != undefined){
            return true;
        }

        if (recur.byyearday){
            return true;
        }

        if (recur.bymonthday){
            return true;
        }

        if (recur.bymonth){
            return true;
        }

        if (recur.byweekno){
            return true;
        }

        if (recur.byday){
            return true;
        }

        if (recur.byhour){
            return true;
        }

        if (recur.byminute){
            return true;
        }

        if (recur.bysecond){
            return true;
        }

        var interval = parseInt(recur.interval);

        //We don't support any interval except for "1" or none (-1)
        //with the exception of "2" for weekly events, in other words bi-weekly.
        if (!isNaN(interval) && interval != 1 ){

            //if this is not a weekly event, it's custom.
            if (recur.freq != WEEKLY){
               return true;
            }

            //so it IS A weekly event, but the value is not "2", so it's custom
            if (interval != 2){
                return true;
            }
        }
        }
        return false;
    },


    rPropsToRRule: function (rprops, startDate){
        if (this.isRRuleUnsupported(rprops)) {
            // TODO set something more readable?
            return new cosmo.model.RecurrenceRule({
                isSupported: false,
                unsupportedRule: rprops
            });
        } else {
            var RecurrenceRule = cosmo.model.RRULE_FREQUENCIES;
            var Recur = this.rruleConstants;
            var recurrenceRule = {}
            // Set frequency
            if (rprops.freq == Recur.WEEKLY) {
                if (rprops.interval == 1 || !rprops.interval){
                    recurrenceRule.frequency = RecurrenceRule.FREQUENCY_WEEKLY;
                }
                else if (rprops.interval == 2){
                    recurrenceRule.frequency = RecurrenceRule.FREQUENCY_BIWEEKLY;
                }
            }
            else if (rprops.freq == Recur.MONTHLY) {
                recurrenceRule.frequency = RecurrenceRule.FREQUENCY_MONTHLY;
            }
            else if (rprops.freq == Recur.DAILY) {
                recurrenceRule.frequency = RecurrenceRule.FREQUENCY_DAILY;
            }
            else if (rprops.freq == Recur.YEARLY) {
                recurrenceRule.frequency = RecurrenceRule.FREQUENCY_YEARLY;
            }

            // Set until date
            if (rprops.until) {
                var endDate = cosmo.datetime.fromIso8601(rprops.until);
                var tzId = startDate.tzId || (startDate.utc ? "utc" : null);
                endDate = endDate.createDateForTimezone(tzId);
                recurrenceRule.endDate = endDate;
            }
            
            recurrenceRule = new cosmo.model.RecurrenceRule(recurrenceRule);
            

            return recurrenceRule;
        }

    },
    
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

cosmo.service.translators.eim = new cosmo.service.translators.Eim();


