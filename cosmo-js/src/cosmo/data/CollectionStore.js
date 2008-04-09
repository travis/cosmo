if(!dojo._hasResource["cosmo.data.UserStore"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["cosmo.data.UserStore"] = true;
dojo.provide("cosmo.data.UserStore");
dojo.require("cosmo.cmp");
dojo.require("dojo.data.api.Write");
dojo.require("dojo.DeferredList");

dojo.declare("cosmo.data.UserStore", dojo.data.api.Write, {
    //	summary:
    //		A data store Chandler Server Users
    //	description:
    //		A data store for Chandler Server Users
    
    DEFAULT_START: 0,
    DEFAULT_COUNT: 25,

    constructor: function(/*object*/ args) {
        this._url = args.url;
    },
    
    /* dojo.data.api.Read */
    
    getValue: function(/* Object */ user, /* attribute-name-string */ attribute, /* value? */ defaultValue){
        //	summary:
        //		Return an attribute value
        //	description:
		//		'item' must be a hash representing a user
		//	item:
		//		A hash representing a user
		//	attribute:
        //      The name of an attribute to return
		//	defaultValue:
		//		A default value
		//	returns:
		//		An attribute value found, otherwise 'defaultValue'

        return user[attribute] || defaultValue;
	},

	getValues: function(/* Object */ user, /* attribute-name-string */ attribute){
		//	summary:
		//		Return an array of attribute values
        //	description:
		//		'item' must be a hash representing a user
		//	item:
		//		A hash representing a user
		//	attribute:
        //      The name of an attribute to return
		//	returns:
		//		An array of attribute values found, otherwise an empty array
        var value = user[attribute];
        return value? [value] : [];
	},

	getAttributes: function(/* Object */ user) {
		//	summary:
		//		Return an array of attribute names
		//  user:
        //      A hash representing the user
        //	returns:
		//		An array of attributes found
        return [
            user.username,
            user.firstName,
            user.lastName,
            user.email
        ];
	},

	hasAttribute: function(/* Object */ user, /* attribute */ attribute){
		//	summary:
		//		Check whether a user has the attribute
		//  user:
        //      A hash representing the user
		//	attribute:
        //      The name of an attribute to return
		//	returns:
		//		True if the element has the attribute, otherwise false
		return (this.getValue(item, attribute) !== undefined); //boolean
	},

	containsValue: function(/* Object */ user, /* attribute || attribute-name-string */ attribute, /* anything */ value){
		//	summary:
		//		Check whether the attribute values contain the value
		//	user:
        //      The user to check
		//	attribute:
        //      The name of the attribute to check.
		//	returns:
		//		True if the attribute values contain the value, otherwise false
        return value == this.getValue(item, attribute);
	},

	isItem: function(/* anything */ something){
		//	summary:
		//		Check whether the object is an item (XML element)
		//	item:
		//		An object to check
		// 	returns:
		//		True if the object is an user hash, false otherwise
        return (something && something.name && something.firstName && 
                something.lastName && something.email && 
                (something._storeProp == this));
	},

	isItemLoaded: function(/* anything */ something){
		//	summary:
		//		Check whether the object is a user hash and loaded
		//	item:
		//		An object to check
		//	returns:
		//		True if the object is a user hash, otherwise false
		return this.isItem(something); //boolean
	},

	loadItem: function(/* object */ kwArgs){
		//	summary:
		//		Load a user
		//	keywordArgs:
		//		object containing the args for loadItem.  See dojo.data.api.Read.loadItem()
        var scope = keywordArgs.scope || dojo.global();
        // If the item isn't loaded
        if (!this.isItemLoaded(kwArgs.item)){
            // And we have enough information to get the user
            if (kwArgs.item && kwArgs.item.username){
                // Get the user
                var d = cosmo.cmp.getUser(kwArgs.item.username);
                d.addCallback(function(user){
                    kwArgs.item._storeProp = this;
                    dojo.mixin(kwArgs.item, user)
                    if (kwArgs.onItem)
                        kwArgs.onItem.call(scope, kwArgs.item);
                    return kwArgs.item;
                });
                d.addErrback(function(e){
                    if (kwArgs.onError)
                        kwArgs.onError.call(scope, e);
                    return e;
                });
                return d;
            } else {
                kwArgs.onError.call(
                    scope, new Error("Could not load user, don't have enough information"));
            }
	},

    SORT_ASCENDING: "ascending",
    SORT_DESCENDING: "descending",
    DEFAULT_SORT_TYPE: "username",
    DEFAULT_SORT_ORDER: "descending",

    fetch: function(/* Object */request){
        var count = request.count || this.DEFAULT_COUNT;
        var start = request.start || this.DEFAULT_START;
        var pageNumber = Math.floor(start / count);
        var query = request.query;

        // Sorting
        var sortOrder = this.DEFAULT_SORT_ORDER;
        var sortType = this.DEFAULT_SORT_TYPE;
        if (request.sort){
            var sort = request.sort[0] || {};
            sortType = sort.attribute || this.DEFAULT_SORT_TYPE;
            sortOrder = sort.descending? 
                this.SORT_DESCENDING : this.SORT_ASCENDING;
        }
        return this._deferredToFetchRequest(
            cosmo.cmp.getUsers(pageNumber, count, sortOrder, sortType, query)
            request);
    },

    /* Given a dojo.Deferred and a dojo.data.api.Request, set up the request object 
       appropriately from the Deferred.
    */
    _deferredToFetchRequest: function(deferred, request){
        var scope = request.scope || dojo.global();

        request.abort = dojo.hitch(deferred, deferred.cancel);
        
        deferred.addCallback(function(result){
            var items = dojo.isArray(result)? result : [result];
            if (request.onBegin) request.onBegin.call(scope, items.length, request);
            for (var i in items){
                items[i]._storeProp = this;
                if (request.onItem) request.onItem.call(scope, items[i], request);
            }
            if (request.onComplete) request.onComplete.call(scope, items, request);
            return result;
        });

        deferred.addErrback(function(error){
            if (request.onError) request.onError.call(scope, error, request);
            return error;
        });
        request._deferred = deferred;
        return request;
    },

	getLabel: function(/* item */ item){
		//	summary: 
		//		See dojo.data.api.Read.getLabel()
		if(this.isItem(item)){
            return "User: " + item.username;
		}
		return undefined; //undefined
	},

	getLabelAttributes: function(/* item */ item){
		//	summary: 
		//		See dojo.data.api.Read.getLabelAttributes()
		if(this.isItem(item)){
            return ["username"];
		}
        return null;
	},

	close: function(/*dojo.data.api.Request || keywordArgs || null */ request){
		 //	summary: 
		 //		See dojo.data.api.Read.close()
	},

/* dojo.data.api.Write */
    /* Users to create on a call to save() */
    _newItems: [],
    
    /* Users to delete on a call to save() */
    _deletedItems: [],

    /* Users to modify on a call to save() */
    _modifiedItems: [],

	newItem: function(/* object? */ user){
        this._newItems.push(user);
        user._storeProp = this;
        return user;
	},
	
	deleteItem: function(/* item */ item){
        if (!this.isItem(item)) throw new Error(item  + " is not a user from this store.");
		this._deletedItems.push(item);
		return true; //boolean
	},
	
	setValue: function(/* item */ item, /* string */ attribute, /* string */ value){
        item[attribute] = value;
        this._modifiedItems.push(item);
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
		for(var i = 0; i < this._modifiedItems.length; i++){
            deferreds.push(cosmo.cmp.modifyUser(this._modifiedItems[i]));
		}
		for(var i = 0; i < this._newItems.length; i++){
            deferreds.push(cosmo.cmp.createUser(this._newItems[i]));
		}
        deferreds.push(cosmo.cmp.deleteUsers(
            dojo.map(this._deletedItems, function(user){return user.username})));
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
		console.log("CmpStore.revert() _newItems=" + this._newItems.length);
		console.log("CmpStore.revert() _deletedItems=" + this._deletedItems.length);
		console.log("CmpStore.revert() _modifiedItems=" + this._modifiedItems.length);
		this._newItems = [];
		this._restoreItems(this._deletedItems);
		this._deletedItems = [];
		this._restoreItems(this._modifiedItems);
		this._modifiedItems = [];
		return true; //boolean
	},

    _restoreItems: function(items){
        // Since we're not doing any internal cacheing, this is a no-op
    },
	
	isDirty: function(/* object? */ user){
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
            
			return (this._getItemIndex(this._newItems, user) >= 0 ||
				this._getItemIndex(this._deletedItems, user) >= 0 ||
				this._getItemIndex(this._modifiedItems, user) >= 0); //boolean
		}
		else {
			return (this._newItems.length > 0 ||
				this._deletedItems.length > 0 ||
				this._modifiedItems.length > 0); //boolean
		}
	},
    
    _getItemIndex: function(items, user){
        for (var i = 0; i < items.length; i++){
            if (items[i].username == user.username){
                return i;
            }
        }
        return -1;
    }
});


}
