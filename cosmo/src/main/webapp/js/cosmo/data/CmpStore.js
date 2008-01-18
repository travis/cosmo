dojo.provide("cosmo.data.UserStore");
dojo.require("cosmo.cmp");
dojo.require("dojo.data.api.Write");

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

	loadItem: function(/* object */ keywordArgs){
		//	summary:
		//		Load a user
		//	keywordArgs:
		//		object containing the args for loadItem.  See dojo.data.api.Read.loadItem()
        var scope keywordArgs.scope || dojo.global();
        try{
            keywordArgs.item._storeProp = this;
            if (keywordArgs.onItem){
                keywordArgs.onItem.call(scope, keywordArgs.item)
                                        
            }
        } catch (e){
            if (keywordArgs.onError){
                keywordArgs.onError.call(scope, e);
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
	},

	revert: function(){
		// summary:
		//	Invalidate changes (new and/or modified elements)
		// returns:
		//	True
		console.log("XmlStore.revert() _newItems=" + this._newItems.length);
		console.log("XmlStore.revert() _deletedItems=" + this._deletedItems.length);
		console.log("XmlStore.revert() _modifiedItems=" + this._modifiedItems.length);
		this._newItems = [];
		this._restoreItems(this._deletedItems);
		this._deletedItems = [];
		this._restoreItems(this._modifiedItems);
		this._modifiedItems = [];
		return true; //boolean
	},
	
	isDirty: function(/* item? */ item){
		//	summary:
		//		Check whether an item is new, modified or deleted
		//	description:
		//		If 'item' is specified, true is returned if the item is new,
		//		modified or deleted.
		//		Otherwise, true is returned if there are any new, modified
		//		or deleted items.
		//	item:
		//		An item (XML element) to check
		//	returns:
		//		True if an item or items are new, modified or deleted, otherwise
		//		false
		if (item) {
			var element = this._getRootElement(item.element);
			return (this._getItemIndex(this._newItems, element) >= 0 ||
				this._getItemIndex(this._deletedItems, element) >= 0 ||
				this._getItemIndex(this._modifiedItems, element) >= 0); //boolean
		}
		else {
			return (this._newItems.length > 0 ||
				this._deletedItems.length > 0 ||
				this._modifiedItems.length > 0); //boolean
		}
	},

	_saveItem: function(item, keywordArgs, method){
		if(method === "PUT"){
			url = this._getPutUrl(item);
		}else if(method === "DELETE"){
			url = this._getDeleteUrl(item);
		}else{ // POST
			url = this._getPostUrl(item);
		}
		if(!url){
			if(keywordArgs.onError){
				keywordArgs.onError.call(scope, new Error("No URL for saving content: " + postContent));
			}
			return;
		}

		var saveArgs = {
			url: url,
			method: (method || "POST"),
			contentType: "text/xml",
			handleAs: "xml"
		};
		var saveHander;
		if(method === "PUT"){
			saveArgs.putData = this._getPutContent(item);
			saveHandler = dojo.rawXhrPut(saveArgs);
		}else if(method === "DELETE"){
			saveHandler = dojo.xhrDelete(saveArgs);
		}else{ // POST
			saveArgs.postData = this._getPostContent(item);
			saveHandler = dojo.rawXhrPost(saveArgs);
		}
		var scope = (keywordArgs.scope || dojo.global);
		var self = this;
		saveHandler.addCallback(function(data){
			self._forgetItem(item);
			if(keywordArgs.onComplete){
				keywordArgs.onComplete.call(scope);
			}
		});
		saveHandler.addErrback(function(error){
			if(keywordArgs.onError){
				keywordArgs.onError.call(scope, error);
			}
		});
	},

	_getPostUrl: function(item){
		//	summary:
		//		Generate a URL for post
		//	description:
		//		This default implementation just returns '_url'.
		//		Sub-classes may override this method for the custom URL.
		//	item:
		//		An item to save
		//	returns:
		//		A post URL
		return this._url; //string
	},

	_getPutUrl: function(item){
		//	summary:
		//		Generate a URL for put
		//	description:
		//		This default implementation just returns '_url'.
		//		Sub-classes may override this method for the custom URL.
		//	item:
		//		An item to save
		//	returns:
		//		A put URL
		return this._url; //string
	},

	_getDeleteUrl: function(item){
		//	summary:
		//		Generate a URL for delete
		// 	description:
		//		This default implementation returns '_url' with '_keyAttribute'
		//		as a query string.
		//		Sub-classes may override this method for the custom URL based on
		//		changes (new, deleted, or modified).
		// 	item:
		//		An item to delete
		// 	returns:
		//		A delete URL
		if (!this._url) {
			return this._url; //string
		}
		var url = this._url;
		if (item && this._keyAttribute) {
			var value = this.getValue(item, this._keyAttribute);
			if (value) {
				url = url + '?' + this._keyAttribute + '=' + value;
			}
		}
		return url;	//string
	},

	_getPostContent: function(item){
		//	summary:
		//		Generate a content to post
		// 	description:
		//		This default implementation generates an XML document for one
		//		(the first only) new or modified element.
		//		Sub-classes may override this method for the custom post content
		//		generation.
		//	item:
		//		An item to save
		//	returns:
		//		A post content
		var element = item.element;
		var declaration = "<?xml version=\"1.0\"?>"; // FIXME: encoding?
		return declaration + dojox.data.dom.innerXML(element); //XML string
	},

	_getPutContent: function(item){
		//	summary:
		//		Generate a content to put
		// 	description:
		//		This default implementation generates an XML document for one
		//		(the first only) new or modified element.
		//		Sub-classes may override this method for the custom put content
		//		generation.
		//	item:
		//		An item to save
		//	returns:
		//		A post content
		var element = item.element;
		var declaration = "<?xml version=\"1.0\"?>"; // FIXME: encoding?
		return declaration + dojox.data.dom.innerXML(element); //XML string
	},

/* internal API */

	_getAttribute: function(tagName, attribute){
		if(this._attributeMap){
			var key = tagName + "." + attribute;
			var value = this._attributeMap[key];
			if(value){
				attribute = value;
			}else{ // look for global attribute
				value = this._attributeMap[attribute];
				if(value){
					attribute = value;
				}
			}
		}
		return attribute; //object
	},

	_getItem: function(element){
		return new dojox.data.XmlItem(element, this); //object
	},

	_getItemIndex: function(items, element){
		for(var i = 0; i < items.length; i++){
			if(items[i].element === element){
				return i; //int
			}
		}
		return -1; //int
	},

	_backupItem: function(item){
		var element = this._getRootElement(item.element);
		if(	this._getItemIndex(this._newItems, element) >= 0 ||
			this._getItemIndex(this._modifiedItems, element) >= 0){
			return; // new or already modified
		}
		if(element != item.element){
			item = this._getItem(element);
		}
		item._backup = element.cloneNode(true);
		this._modifiedItems.push(item);
	},

	_restoreItems: function(items){

		dojo.forEach(items,function(item){ 
			if(item._backup){
				item.element = item._backup;
				item._backup = null;
			}
		},this); 
	},

	_forgetItem: function(item){
		var element = item.element;
		var index = this._getItemIndex(this._newItems, element);
		if(index >= 0){
			this._newItems.splice(index, 1);
		}
		index = this._getItemIndex(this._deletedItems, element);
		if(index >= 0){
			this._deletedItems.splice(index, 1);
		}
		index = this._getItemIndex(this._modifiedItems, element);
		if(index >= 0){
			this._modifiedItems.splice(index, 1);
		}
	},

	_getDocument: function(element){
		if(element){
			return element.ownerDocument;  //DOMDocument
		}else if(!this._document){
			return dojox.data.dom.createDocument(); // DOMDocument
		}
	},

	_getRootElement: function(element){
		while(element.parentNode){
			element = element.parentNode;
		}
		return element; //DOMElement
	}

});


function getMyCollections(){
    var r = cosmo.util.auth.getAuthorizedRequest();

}