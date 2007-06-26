/*
 * Copyright 2006 Open Source Applications Foundation
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

dojo.provide("cosmo.util.auth");

dojo.require("dojo.io.cookie");
dojo.require("cosmo.util.cookie");
dojo.require("cosmo.util.encoding");
// dojo.io.cookie.destroyCookie doesn't appear to be working...

var COSMO_AUTH_COOKIE = "CosmoCred";

cosmo.util.auth = new function() {
    this.setCred = function (username, password){
        dojo.io.cookie.set(COSMO_AUTH_COOKIE,
            cosmo.util.encoding.toBase64(
                username + ":" + password), -1, "/");
    }

    this.getPassword = function(){

        var cred = this.getCred();

        if (cred){
            return cosmo.util.encoding.fromBase64(cred).split(":")[1];
        } else {
            return '';
        }
    }

    this.getUsername = function(){

        var cred = this.getCred();

        if (cred){
            return cosmo.util.encoding.fromBase64(cred).split(":")[0];
        } else {
            return '';
        }
    }

    this.setPassword = function(pass){
        this.setCred(this.getUsername(), pass);
    }

    this.setUsername = function(username){
        this.setCred(username, this.getPassword());
    }

    this.clearAuth = function (){
        cosmo.util.cookie.destroy(COSMO_AUTH_COOKIE);
    }

    this.getCred = function(){
		return dojo.io.cookie.get(COSMO_AUTH_COOKIE);
    }
    
    this.currentlyAuthenticated = function(){
        return !!this.getCred();
    }

    this.getAuthorizedRequest = function(request, kwArgs){
        kwArgs = kwArgs || {};
    	request = request || {};
        request.headers = request.headers || {};
        
    	if (this.getCred() && !kwArgs.noAuth){
    	   	request.headers.Authorization =  request.headers.Authorization || "Basic " + this.getCred();
    	}
    	return request;
    }

}

