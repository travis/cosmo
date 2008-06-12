/* * Copyright 2008 Open Source Applications Foundation *
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
 *      This module provides CRUD methods based on DOM storage:
 *      http://developer.mozilla.org/en/docs/DOM:Storage
 * description:
 *      TODO: fill this in
 */

dojo.provide("cosmo.storage.Dom");
dojo.require("dojo.cookie");

dojo.declare("cosmo.storage.Dom", null, {
    initStorage: function(){
        if(!this.storage) this.storage = globalStorage[this._getDomain()];
    },

    get: function(key){
        this.initStorage();
        var storageItem = this.storage.getItem(key);
        return storageItem? storageItem.value: storageItem;
    },

    put: function(key, value){
        this.initStorage();
        this.storage.setItem(key, value);
    },

    remove: function(key){
        this.initStorage();
        this.storage.removeItem(key);
    },

    isAvailable: function(){
        try {
            var s = globalStorage[this._getDomain()];
            return !!s;
        } catch(e) {
            return false;
        };
    },

    _getDomain: function(){
		// see: https://bugzilla.mozilla.org/show_bug.cgi?id=357323
		return ((location.hostname == "localhost" && dojo.isFF < 3) ? "localhost.localdomain" : location.hostname);
	}

});
