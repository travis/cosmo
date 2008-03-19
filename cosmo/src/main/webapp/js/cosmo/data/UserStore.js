dojo.provide("cosmo.data.UserStore");
dojo.require("cosmo.cmp");
dojo.require("dojo.DeferredList");

dojo.declare("cosmo.data.UserStore", null, {
    //	summary:
    //		A data store Chandler Server Users
    //	description:
    //		A data store for Chandler Server Users
    
    DEFAULT_START: 0,
    DEFAULT_COUNT: 25,

    constructor: function(/*object*/ args) {
    },
    
	getFeatures: function(){
		//	summary: 
		//		See dojo.data.api.Read.getFeatures()
		return {
			'dojo.data.api.Read': true,
			'dojo.data.api.Write': true,
			'dojo.data.api.Identity': true,
			'dojo.data.api.Notification': true
		};
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
            "username",
            "firstName",
            "lastName",
            "email",
            "dateCreated",
            "dateModified",
            "url",
            "locked",
            "administrator",
            "unactivated",
            "password"
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
        return !!(something && something.username && something.firstName && 
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
        var scope = kwArgs.scope || dojo.global;
        // If the item isn't loaded
        if (!this.isItemLoaded(kwArgs.item)){
            // And we have enough information to get the user
            if (kwArgs.item && kwArgs.item.username){
                // Get the user
                var d = cosmo.cmp.getUser(kwArgs.item.username);
                d.addCallback(dojo.hitch(this, function(user){
                    kwArgs.item._storeProp = this;
                    dojo.mixin(kwArgs.item, user)
                    if (kwArgs.onItem)
                        kwArgs.onItem.call(scope, kwArgs.item);
                    return kwArgs.item;
                }));
                d.addErrback(function(e){
                    if (kwArgs.onError)
                        kwArgs.onError.call(scope, e);
                    return e;
                });
                return d;
            } else {
                var e = new Error("Could not load user, don't have enough information")
                if (kwArgs.onError) kwArgs.onError.call(scope, e);
                else throw e;
            }
        }
	},

    SORT_ASCENDING: "ascending",
    SORT_DESCENDING: "descending",
    DEFAULT_SORT_TYPE: "username",
    DEFAULT_SORT_ORDER: "descending",

    fetch: function(/* Object */request){
        var count = request.count || this.DEFAULT_COUNT;
        var start = request.start || this.DEFAULT_START;
        var pageNumber = Math.floor(start / count) + 1;
        var query = request.query.q;

        // Sorting
        var sortOrder = this.DEFAULT_SORT_ORDER;
        var sortType = this.DEFAULT_SORT_TYPE;
        if (request.sort){
            var sort = request.sort[0] || {};
            sortType = sort.attribute || this.DEFAULT_SORT_TYPE;
            sortOrder = sort.descending? 
                this.SORT_DESCENDING : this.SORT_ASCENDING;
        }
        var scope = request.scope || dojo.global;
        var deferreds = [];
        if (request.onBegin) {
            var countD = cosmo.cmp.getUserCount(query);
            countD.addCallback(function(count){
                request.onBegin.call(scope, count, request);
            });
            deferreds.push(countD);
        }
        var getD = cosmo.cmp.getUsers(pageNumber, count, sortOrder, sortType, query);
        deferreds.push(getD);
        var r = this._deferredToFetchRequest(getD, request, scope)
        r._deferred = new dojo.DeferredList(deferreds);
        return r;
    },

    /* Given a dojo.Deferred and a dojo.data.api.Request, set up the request object 
       appropriately from the Deferred.
    */
    _deferredToFetchRequest: function(deferred, request, scope){
        request.abort = dojo.hitch(deferred, deferred.cancel);
        
        deferred.addCallback(dojo.hitch(this, function(result){
            var items = dojo.isArray(result)? result : [result];
            for (var i in items){
                items[i]._storeProp = this;
                if (request.onItem) request.onItem.call(scope, items[i], request);
            }
            if (request.onComplete) request.onComplete.call(scope, items, request);
            return result;
        }));

        deferred.addErrback(function(error){
            if (request.onError) request.onError.call(scope, error, request);
            return error;
        });
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

/* dojo.data.api.Identity */
    getIdentity: function(user){
        return user.username;
    },
    
    getIdentityAttributes: function(item){
        return ["username"];
    },

    fetchItemByIdentity: function(request){
        var d = cosmo.cmp.getUser(request.identity);
        var scope = request.scope || dojo.global;
        d.addCallback(dojo.hitch(scope, request.onItem));
        d.addErrback(dojo.hitch(scope, request.onError));
        return d;
    },

/* dojo.data.api.Write */
    /* Users to create on a call to save() */
    _newItems: [],
    
    /* Users to delete on a call to save() */
    _deletedItems: [],

    /* Users to modify on a call to save() */
    _modifiedItems: {},

	newItem: function(/* object? */ user){
        this._newItems.push(user);
        user._storeProp = this;
        this.onNew(user);
        return user;
	},
	
	deleteItem: function(/* item */ item){
        if (!this.isItem(item)) throw new Error("item is not a user from this store.");
		this._deletedItems.push(item);
        this.onDelete(item);
		return true; //boolean
	},
	
	setValue: function(/* item */ item, /* string */ attribute, /* string */ value){
        if (!this._isEditable(item, attribute, value)) return false;
        
        var oldValue = item[attribute];
        if (attribute == "unactivated" && oldValue && !value){
            item["doActivate"] = true;
        }
        else if (value != oldValue) {
            item[attribute] = value;
            this._modifiedItems[item.username] = item;
        }
        this.onSet(item, attribute, oldValue, value);
		return true; //boolean
	},
    
    _isEditable: function(item, attribute, value){
        if (item.username == "root"
            && (attribute == "username"
                || attribute == "firstName"
                || attribute == "lastName")) return false;
        return true;
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
        console.debug(this._modifiedItems);
		for(var i in this._modifiedItems){
            var user = this._modifiedItems[i];
            if (user.doActivate){deferreds.push(cosmo.cmp.activate(user.username));}
            deferreds.push(cosmo.cmp.modifyUser(user.username, user));
        }
        this._modifiedItems = {};
        console.debug("creating " + this._newItems.length + " users");
		for(var i = 0; i < this._newItems.length; i++){
            deferreds.push(cosmo.cmp.createUser(this._newItems[i]));
		}
        this._newItems = [];
        console.debug("deleting " + this._deletedItems.length + " users");
        if (this._deletedItems.length > 0)
            deferreds.push(cosmo.cmp.deleteUsers(
                dojo.map(this._deletedItems, function(user){return user.username})));
        this._deletedItems = [];
        var dl = new dojo.DeferredList(deferreds);
        var scope = keywordArgs.scope || dojo.global;
        
        if (keywordArgs.onComplete)
            dl.addCallback(dojo.hitch(scope, keywordArgs.onComplete));
        if (keywordArgs.onError)
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
		this._modifiedItems = {};
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
        for (var i in items){
            if (items[i].username == user.username){
                return i;
            }
        }
        return -1;
    },

    onSet: function(/* item */ item,
        /* attribute-name-string */ attribute,
        /* object | array */ oldValue,
        /* object | array */ newValue){},
        //    summary:
        //        This function is called any time an item is modified via setValue, setValues, unsetAttribute, etc. 
        //    description:
        //        This function is called any time an item is modified via setValue, setValues, unsetAttribute, etc. 
        //        Its purpose is to provide a hook point for those who wish to monitor actions on items in the store
        //        in a simple manner.  The general expected usage is to dojo.connect() to the store's
        //        implementation and be called after the store function is called.
        //
        //    item:
        //        The item being modified.
        //    attribute:
        //        The attribute being changed represented as a string name.
        //    oldValue:
        //        The old value of the attribute.  In the case of single value calls, such as setValue, unsetAttribute, etc,
        //        this value will be generally be an atomic value of some sort (string, int, etc, object).  In the case of
        //        multi-valued attributes, it will be an array.
        //    newValue:
        //        The new value of the attribute.  In the case of single value calls, such as setValue, this value will be
        //        generally be an atomic value of some sort (string, int, etc, object).  In the case of multi-valued attributes,
        //        it will be an array.  In the case of unsetAttribute, the new value will be 'undefined'.
        //
        //    returns:
        //        Nothing.
    onNew: function(/* item */ newItem, /*object?*/ parentInfo){},
        //    summary:
        //        This function is called any time a new item is created in the store.
        //        It is called immediately after the store newItem processing has completed.
        //    description:
        //        This function is called any time a new item is created in the store.
        //        It is called immediately after the store newItem processing has completed.
        //
        //    newItem:
        //        The item created.
        //    parentInfo:
        //        An optional javascript object that is passed when the item created was placed in the store
        //        hierarchy as a value f another item's attribute, instead of a root level item.  Note that if this
        //        function is invoked with a value for parentInfo, then onSet is not invoked stating the attribute of
        //        the parent item was modified.  This is to avoid getting two notification  events occurring when a new item
        //        with a parent is created.  The structure passed in is as follows:
        //        {
        //            item: someItem,                     //The parent item
        //            attribute:        "attribute-name-string",        //The attribute the new item was assigned to.
        //            oldValue: something       //Whatever was the previous value for the attribute. 
        //                                      //If it is a single-value attribute only, then this value will be a single value.
        //                                      //If it was a multi-valued attribute, then this will be an array of all the values minues the new one.
        //            newValue: something       //The new value of the attribute.  In the case of single value calls, such as setValue, this value will be
        //                                      //generally be an atomic value of some sort (string, int, etc, object).  In the case of multi-valued attributes,
        //                                      //it will be an array. 
        //        }
        //
        //    returns:
        //        Nothing.
        onDelete: function(/* item */ deletedItem){}
        //    summary:
        //        This function is called any time an item is deleted from the store.
        //        It is called immediately after the store deleteItem processing has completed.
        //    description:
        //        This function is called any time an item is deleted from the store.
        //        It is called immediately after the store deleteItem processing has completed.
        //
        //    deletedItem:
        //        The item deleted.
        //
        //    returns:
        //        Nothing.

});

