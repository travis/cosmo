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
 *      This module provides an implementation of WSSE authentication.
 *      For more information on WSSE, see
 *      http://www.xml.com/pub/a/2003/12/17/dive.html
 *      http://www.oasis-open.org/committees/wss/documents/WSS-Username-02-0223-merged.pdf
 * description:
 *      TODO: fill this in
 */

dojo.provide("cosmo.auth.wsse");
dojo.require("dojox.uuid.generateRandomUuid");
dojo.require("cosmo.util.sha1");
dojo.require("cosmo.util.encoding");
dojo.require("dojo.date.stamp");
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

     generateCredentials: function(username, password){
         var nonce = dojox.uuid.generateRandomUuid();
         var created = dojo.date.stamp.toISOString(new Date());
         return {
             nonce: nonce,
             created: created,
             digest: this.generatePasswordDigest(nonce, created, password)
         };
     }
});
