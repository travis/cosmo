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

dojo.require("dojo.io.cookie");
dojo.require("cosmo.util.cookie"); 
// dojo.io.cookie.destroyCookie doesn't appear to be working...

dojo.provide("cosmo.util.auth");

var COSMO_AUTH_COOKIE = "CosmoCred";

cosmo.util.auth = new function() {
    this.setUser = function (username, password){
        dojo.io.cookie.set(COSMO_AUTH_COOKIE, this.encode64(username + ":" + password), -1, "/");
    }

    this.clearAuth = function (){
        cosmo.util.cookie.destroy(COSMO_AUTH_COOKIE);
    }
    
    this.getCred = function(){
		return dojo.io.cookie.get(COSMO_AUTH_COOKIE);
    }
    
    this.getAuthorizedRequest = function(){
    	var req = {};
    	req.headers = {};
    	if (this.getCred()){
    	   	req.headers.Authorization =  "Basic " + this.getCred();
    	}
    	return req;
    }
    
	this.encode64 = function(inp){
	    var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + //all caps
	    "abcdefghijklmnopqrstuvwxyz" + //all lowercase
	    "0123456789+/="; // all numbers plus +/=
	
	    var out = ""; //This is the output
	    var chr1, chr2, chr3 = ""; //These are the 3 bytes to be encoded
	    var enc1, enc2, enc3, enc4 = ""; //These are the 4 encoded bytes
	    var i = 0; //Position counter
	
	    do { //Set up the loop here
	        chr1 = inp.charCodeAt(i++); //Grab the first byte
	        chr2 = inp.charCodeAt(i++); //Grab the second byte
	        chr3 = inp.charCodeAt(i++); //Grab the third byte
	
	        //Here is the actual base64 encode part.
	        //There really is only one way to do it.
	        enc1 = chr1 >> 2;
	        enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
	        enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
	        enc4 = chr3 & 63;
	
	        if (isNaN(chr2)) {
	        enc3 = enc4 = 64;
	        } else if (isNaN(chr3)) {
	        enc4 = 64;
	        }
	
	        //Lets spit out the 4 encoded bytes
	        out = out +
	                keyStr.charAt(enc1) +
	                keyStr.charAt(enc2) +
	                keyStr.charAt(enc3) +
	                keyStr.charAt(enc4);
	
	        // OK, now clean out the variables used.
	        chr1 = chr2 = chr3 = "";
	        enc1 = enc2 = enc3 = enc4 = "";
	
	    } while (i < inp.length); //And finish off the loop
	
	    //Now return the encoded values.
	    return out;
	}

}

