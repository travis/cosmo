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

dojo.provide("cosmo.data.AtomStore");

dojo.require("dojo.data.api.Write");
dojo.require("cosmo.atompub");
dojo.require("dojo.DeferredList");

dojo.declare("cosmo.data.AtompubStore", dojo.data.api.Write,
{
    //	summary:
    //		A data store Atompub Collections
    //	description:
    //		A data store for Atompub Collections

    constructor: function(/*Object*/ keywordParameters) {
        // summary: constructor
        // example:
        this.xhrArgs = keywordParameters.xhrArgs;
        this.iri = keywordParameters.iri;
    },

    /* dojo.data.api.Read */

	loadItem: function(/* object */ kwArgs){
		//	summary:
		//		Load a user
		//	keywordArgs:
		//		object containing the args for loadItem.  See dojo.data.api.Read.loadItem()
        var scope = keywordArgs.scope || dojo.global();
        // If the item isn't loaded
        if (!this.isItemLoaded(kwArgs.item)){
	        throw new Error('Unimplemented API: dojo.data.api.Read.isItemLoaded');
        }
	},

    fetch: function(/* Object */request){
        //TODO: support paging

        var scope = request.scope || dojo.global();
        var getDeferred = dojo.xhrGet(dojo.mixin({url: this.iri}, this.xhrArgs));
        getDeferred.addCallback(dojo.hitch(
            this,
            function(result){
                var entries = dojo.query("entry", result);
                if (request.onBegin) request.onBegin.call(scope, entries.length, request);
                var items = entries.forEach(dojo.hitch(
                    this,
                    function(entry){
                        var item = this.entryProcessor(entry);
                        if (request.onItem) request.onItem.call(scope, item, request);
                        return item;
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
    _newEntries: [],

    /* Users to delete on a call to save() */
    _deletedEntries: [],

    /* Users to modify on a call to save() */
    _modifiedEntries: [],

	newItem: function(/* Object? */ item){
        var entry = this.entryGenerator(item);
        this._newEntries.push(entry);
        item._storeProp = this;
        return item;
	},

	deleteItem: function(/* item */ item){
        if (!this.isItem(item)) throw new Error(item  + " is not a user from this store.");
        var entry = this.entryGenerator(item);
		this._deletedEntries.push(entry);
		return true; //boolean
	},

	setValue: function(/* item */ item, /* string */ attribute, /* string */ value){
        item[attribute] = value;
        var entry = this.entryGenerator(item);
        this._modifiedEntries.push(entry);
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
        dojo.forEach(this._modifiedEntries, function(entry){
            deferreds.push(cosmo.atompub.modifyEntry(entry));
		});
        dojo.forEach(this._newEntries, function(entry){
            deferreds.push(cosmo.atompub.newEntry(iri, entry));
		});
        dojo.forEach(this._deletedEntries, function(entry){
            deferreds.push(cosmo.atompub.deleteEntry(entry));
		});
        var dl = new dojo.DeferredList(deferreds);
        var scope = keywordArgs.scope || dojo.global();
        dl.addCallback(dojo.hitch(scope, keywordArgs.onComplete));
        dl.addErrback(dojo.hitch(scope, keywordArgs.onError));
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

