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
 *      This module provides an implementation of WSSE Username Token authentication.
 *      For more information on WSSE, see
 *      http://www.xml.com/pub/a/2003/12/17/dive.html
 *      http://www.oasis-open.org/committees/wss/documents/WSS-Username-02-0223-merged.pdf
 * description:
 *      TODO: fill this in
 */

dojo.provide("cosmo.auth.wsse");
dojo.require("cosmo.auth._base");
dojo.require("cosmo.util.sha1");

dojo.require("dojo.date.stamp");
dojo.require("dojo.cookie");
(function(){
var COSMO_WSSE_PWDIGEST = "CosmoWssePasswordDigest";
var COSMO_WSSE_NONCE = "CosmoWsseNonce";
var COSMO_WSSE_CREATED = "CosmoWsseCreated";
var COSMO_WSSE_USERNAME = "CosmoWsseUsername";
dojo.mixin(cosmo.auth.wsse,
{
    /**
     * summary:
     *      Given a username and password, generate a password digest according to the WSSE spec.
     * description:
     *      (From the Mark Pilgrim article linked above)
     *      1. Start with 2 pieces of information: username and password.
     *      2. Create a nonce, which is a cryptographically random string.
     *      3. Create a "creation timestamp" of the current time, in W3DTF format.
     *      4. Create a password digest:
     *          PasswordDigest = Base64 \ (SHA1 (Nonce + CreationTimestamp + Password))
     */

    generatePasswordDigest: function(nonce, created, password){
        return cosmo.util.sha1.b64(nonce + created + password);
    },

    setPassword: function(/*String*/ password){
        var nonce = Math.floor(Math.random()*1000000);
        var created = dojo.date.stamp.toISOString(new Date());

        dojo.cookie(COSMO_WSSE_PWDIGEST, this.generatePasswordDigest(nonce, created, password), {path:"/"});
        dojo.cookie(COSMO_WSSE_NONCE, nonce, {path:"/"});
        dojo.cookie(COSMO_WSSE_CREATED, created, {path:"/"});
    },

    setUsername: function(/*String*/ username){
        dojo.cookie(COSMO_WSSE_USERNAME, username, {path:"/"});

    },

    getUsername: function(){
        return dojo.cookie(COSMO_WSSE_USERNAME);
    },

    clearAuth: function (){
        dojo.cookie(COSMO_WSSE_PWDIGEST, null, {expires: -1, path: "/"});
        dojo.cookie(COSMO_WSSE_NONCE, null, {expires: -1, path: "/"});
        dojo.cookie(COSMO_WSSE_CREATED, null, {expires: -1, path: "/"});
        dojo.cookie(COSMO_WSSE_USERNAME, null, {expires: -1, path: "/"});
    },

    currentlyAuthenticated: function(){
        return (!!dojo.cookie(COSMO_WSSE_PWDIGEST) &&
                !!dojo.cookie(COSMO_WSSE_NONCE) &&
                !!dojo.cookie(COSMO_WSSE_CREATED) &&
                !!dojo.cookie(COSMO_WSSE_USERNAME));

    },

    generateCredentials: function(){
        var digest = dojo.cookie(COSMO_WSSE_PWDIGEST);
        var nonce = dojo.cookie(COSMO_WSSE_NONCE);
        var created = dojo.cookie(COSMO_WSSE_CREATED);
        var username = dojo.cookie(COSMO_WSSE_USERNAME);
        if (digest && nonce && created && username)
            return {
                "Authorization": 'WSSE profile="UsernameToken"',
                "X-WSSE": ['UsernameToken Username="', username, '", PasswordDigest="', digest, '", Nonce="', nonce, '", Created="', created, '"'].join("")
            };
        else {
            throw new cosmo.auth.CredentialsNotSetError();
        }
    },

    getAuthorizedRequest: function(/*dojo.__IoArgs*/ request){
        request = request || {};
        request.headers = request.headers || {};
        dojo.mixin(request.headers, this.generateCredentials());
        return request;
    }
});
})();