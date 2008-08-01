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
 *      This module provides an implementation of HTTP Basic authentication
 * description:
 *      This module provides an implementation of HTTP Basic authentication
 *      via a clean API that can be reused with other authentication mechanisms.
 *      To use it, first set authentication credentials using
 *      <code>cosmo.auth.basic.setUsername</code> and
 *      <code>cosmo.auth.basic.setPassword</code>.
 *      Next, get a Request object that can be passed to <code>dojo.xhrGet</code>
 *      with <code>cosmo.auth.basic.getAuthorizedRequest</code>.
 */

dojo.provide("cosmo.auth.basic");
dojo.require("cosmo.auth._base");
dojo.require("cosmo.util.encoding");
dojo.require("cosmo.storage");
(function(){
var COSMO_BASIC_AUTH_CRED="CosmoBasicCred";
dojo.mixin(cosmo.auth.basic, {
    storage: cosmo.storage.provider,

    setCred: function (username, password){
        this.storage.put(COSMO_BASIC_AUTH_CRED,
                         cosmo.util.encoding.toBase64(username + ":" + password));
    },

    getPassword: function(){

        var cred = this.getCred();

        if (cred){
            return cosmo.util.encoding.fromBase64(cred).split(":")[1];
        } else {
            return '';
        }
    },

    getUsername: function(){

        var cred = this.getCred();

        if (cred){
            return cosmo.util.encoding.fromBase64(cred).split(":")[0];
        } else {
            return '';
        }
    },

    setPassword: function(pass){
        this.setCred(this.getUsername(), pass);
    },

    setUsername: function(username){
        this.setCred(username, this.getPassword());
    },

    clearAuth: function (){
        this.storage.remove(COSMO_BASIC_AUTH_CRED);
    },

    getCred: function(){
		return this.storage.get(COSMO_BASIC_AUTH_CRED);
    },

    currentlyAuthenticated: function(){
        return !!this.getCred();
    },

    getAuthorizedRequest: function(request, kwArgs){
        kwArgs = kwArgs || {};
    	request = request || {};
        request.headers = request.headers || {};
        var cred = this.getCred();
        if (cred)
            request.headers.Authorization = "Basic " + cred;
    	return request;
    }

});
})();