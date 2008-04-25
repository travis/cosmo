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

dojo.provide("cosmo.data.AtompubStore");

dojo.require("dojo.data.api.Write");
dojo.require("cosmo.atompub");
dojo.require("dojo.DeferredList");
dojo.require("dojox.data.dom");
dojo.require("dojox.uuid");

dojo.declare("cosmo.data.AtompubStore", dojo.data.api.Write,
{
    //	summary:
    //		A data store Atompub Collections
    //	description:
    //		A data store for Atompub Collections

    constructor: function(/*Object*/ keywordParameters) {
        // summary: constructor
        // example:
        keywordParameters = keywordParameters || {};
        this.xhrArgs = keywordParameters.xhrArgs;
        this.iri = keywordParameters.iri;
        this.contentProcessors = this.contentProcessors || {};
        var kwCP = keywordParameters.contentProcessors;
        if (kwCP) for (var type in kwCP) this.contentProcessors[type] = kwCP[type];
    },

    /* dojo.data.api.Read */

	getValue: function(	/* item */ item,
						/* attribute-name-string */ attribute,
						/* value? */ defaultValue){
        return this.getValues(item, attribute)[0] || defaultValue;
    },

	getValues: function getValues(/* item */ item,
						/* attribute-name-string */ attribute){
        var result = this._callOnContentProcessor("getValues", item, attribute);
        if (!result) result = this._getValuesFromEntry(item, attribute);
        return result || [];
    },

    _getContent: function(item){
        return cosmo.atompub.attr.content(item);
    },

    _getValuesFromEntry: function(/* item */ item,
                                  /* attribute-name-string */ attribute){
        var f = cosmo.atompub.attr[attribute];
        return f? [f(item)] : null;
    },

    getAttributes: function(/* item */ item){
        var attrs = [];
        for (var key in cosmo.atompub.attr){
            var val = cosmo.atompub.attr[key]();
            if (val) attrs.push(key);
        }
        return attrs.concat(this._callOnContentProcessor("getAttributes", item));
    },

	hasAttribute: function(	/* item */ item,
							/* attribute-name-string */ attribute){
        if (this.getValues(item, attribute).length > 0) return true;
        else return this._callOnContentProcessor("hasAttribute", item, attribute);
	},

    _callOnContentProcessor: function(funcName, item, attribute){
        var content = cosmo.atompub.attr.content(item);
        return content?
            this.contentProcessors[content.getAttribute("type") || "text"][funcName](content, attribute):
            null;
    },

	containsValue: function(/* item */ item,
							/* attribute-name-string */ attribute,
							/* anything */ value){
        var values = this.getValues(item, attribute);
        for (var i = 0; i < values.length; i++){
            if (values[i] == value) return true;
        }
        return false;
	},

	isItem: function(/* anything */ something){
        return something.nodeName == "entry";
	},

	isItemLoaded: function(/* anything */ something) {
		return !!cosmo.atompub.getEditIri(something);
	},

	loadItem: function(/* object */ kwArgs){
		//	summary:
		//		Load a user
		//	keywordArgs:
		//		object containing the args for loadItem.  See dojo.data.api.Read.loadItem()

        //TODO: load item from server
        var scope = keywordArgs.scope || dojo.global;
        // If the item isn't loaded
        if (!this.isItemLoaded(kwArgs.item)){
	        throw new Error('Unimplemented API: dojo.data.api.Read.isItemLoaded');
        }
	},

    fetch: function(/* Object */request){
        //TODO: support paging

        var scope = request.scope || dojo.global;
        var getDeferred = dojo.xhrGet(dojo.mixin({url: this.iri, handleAs: "xml"}, this.xhrArgs));
        getDeferred.addCallback(dojo.hitch(
            this,
            function(result){
                var entries = cosmo.atompub.query("atom:entry", result.documentElement);
                if (request.onBegin) request.onBegin.call(scope, entries.length, request);
                var items = entries.forEach(dojo.hitch(
                    this,
                    function(entry){
                        if (request.onItem) request.onItem.call(scope, entry, request);
                        return entry;
                    }));
                return items;
            }));
        getDeferred.addCallback(dojo.hitch(
            this,
            function(items){
                if (request.onComplete) request.onComplete.call(scope, items, request);
                return items;
            }));
        request.abort = dojo.hitch(getDeferred, getDeferred.cancel);
        return request;
    },

	close: function(/*dojo.data.api.Request || keywordArgs || null */ request){
		 //	summary:
		 //		See dojo.data.api.Read.close()
	},

/* dojo.data.api.Write */
    /* Entries to create on a call to save() */
    _newEntries: {},

    /* Users to delete on a call to save() */
    _deletedEntries: {},

    /* Users to modify on a call to save() */
    _modifiedEntries: {},

	newItem: function(/* Object? */ properties){
        var entry = this._generateEntry(properties);
        var id = this.getValue(entry, "id");
		this._newEntries[id] = entry;
        return entry;
	},

    _generateEntry: function(item){
        return dojox.data.dom.createDocument([
            '<?xml version="1.0" encoding="utf-8"?>',
            "<entry xmlns='http://www.w3.org/2005/Atom'>",
            "<id>", (item.id || this.getEntryId(item) || "urn:uuid:" + dojox.uuid.generateTimeBasedUuid()), "</id>",
            "<title>", (item.title || this.getEntryTitle(item) || "New Entry"), "</title>",
            this.generateContent(item),
            "</entry>"
            ].join("")).documentElement;

    },

    getEntryId: function(item){
        return null;
    },

    getEntryTitle: function(item){
        return null;
    },

	deleteItem: function(/* item */ item){
        if (!this.isItem(item)) throw new Error(item  + " is not an item from this store.");
		this._deletedEntries[this.getValue(item, "id")] = item;
		return true; //boolean
	},

	setValue: function(/* item */ item, /* string */ attribute, /* string */ value){
        var setFunc = cosmo.atompub.set[attribute];

        // Set value
        cosmo.atompub.setValue(item, attribute, value);

        // Store for save
        var id = this.getValue(item, "id");
        if (this._newEntries[id]) this._newEntries[id] = item;
        else this._modifiedEntries[id] = item;
		return true; //boolean
	},

//	setValues: function(/* item */ item, /* string */ attribute, /* array */ values){
//
//		return true; //boolean
//	},
//
//	unsetAttribute: function(/* item */ item, /* attribute || string */ attribute){
//
//	},

	save: function(/* object */ keywordArgs){
		//	summary:
        //      Save new/modified/deleted users
		// 	description:
		// 	keywordArgs:
		//		An object for callbacks
		if(!keywordArgs){
			keywordArgs = {};
		}
        var deferreds = [];
        var iri = this.iri;
        var xhrArgs = this.xhrArgs;
        for (var id in this._modifiedEntries){
            var entry = this._modifiedEntries[id];
            deferreds.push(cosmo.atompub.modifyEntry(entry, xhrArgs));
            delete this._modifiedEntries[id];
		}
        for (var id in this._newEntries){
            var entry = this._newEntries[id];
            deferreds.push(cosmo.atompub.newEntry(iri, entry, xhrArgs));
            delete this._newEntries[id];
		}
        for (var id in this._deletedEntries){
            var entry = this._deletedEntries[id];
            deferreds.push(cosmo.atompub.deleteEntry(entry, xhrArgs));
            delete this._deletedEntries[id];
		}
        var dl = new dojo.DeferredList(deferreds);
        var scope = keywordArgs.scope || dojo.global;
        if (keywordArgs.onComplete) dl.addCallback(dojo.hitch(scope, keywordArgs.onComplete));
        if (keywordArgs.onError) dl.addErrback(dojo.hitch(scope, keywordArgs.onError));
        return dl;
	},

	revert: function(){
		// summary:
		//	Invalidate changes (new and/or modified elements)
		// returns:
		//	True
		console.log("AtompubStore.revert() _newEntries=",  this._newEntries.length);
		console.log("AtompubStore.revert() _deletedEntries=", this._deletedEntries.length);
		console.log("AtompubStore.revert() _modifiedEntries=", this._modifiedEntries.length);
		this._newEntries = [];
		this._restoreEntries(this._deletedEntries);
		this._deletedEntries = [];
		this._restoreEntries(this._modifiedEntries);
		this._modifiedEntries = [];
		return true; //boolean
	},

    _restoreEntries: function(items){
        // Since we're not doing any internal cacheing, this is a no-op
    },

	isDirty: function(/* object? */ item){
		//	summary:
		//		Check whether an item is new, modified or deleted
		//	description:
		//		If 'item' is specified, true is returned if the item is new,
		//		modified or deleted.
		//		Otherwise, true is returned if there are any new, modified
		//		or deleted items.
		//	item:
        //      A user hash to check.
		//	returns:
		//		True if an item or items are new, modified or deleted, otherwise
		//		false
		if (item) {
            return this._isDirtyById(this.getValue(item, "id"));
		}
		else {
			return (this._newEntries.length > 0 ||
				this._deletedEntries.length > 0 ||
				this._modifiedEntries.length > 0); //boolean
		}
	},

    _isDirtyById: function(id){
        return (_containsEntryWithId(this._newEntries, id) ||
                _containsEntryWithId(this._modifiedEntries, id) ||
                _containsEntryWithId(this._deletedEntries, id));
    },

    _containsEntryWithId: function(list, id){
        for (var i = 0; i < list.length; i++){
            if (this.getValue(list[i], "id")) return true;
        }
        return false;
    }
});

