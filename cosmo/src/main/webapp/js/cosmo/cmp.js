/* * Copyright 2006-2007 Open Source Applications Foundation *
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
 *      This module provides wrappers around dojo.io.bind to simplify using
 *      the Cosmo Management Protocol (CMP) from javascript.
 * description:
 *      For more information about CMP, please see:
 *      http://wiki.osafoundation.org/Projects/CosmoManagementProtocol
 *
 *      Most methods take handlerDicts identical to those required
 *      by dojo.io.bind.
 */

dojo.provide("cosmo.cmp");

dojo.require("dojo.io.*");
dojo.require("dojo.string");
dojo.require("cosmo.env");
dojo.require("cosmo.util.auth");

DEFAULT_PAGE_NUMBER = 1;
DEFAULT_PAGE_SIZE = 25;
DEFAULT_SORT_ORDER = "ascending";
DEFAULT_SORT_TYPE= "username";

EL_ADMINISTRATOR = "administrator";

cosmo.ROLE_ADMINISTRATOR = "administrator"
cosmo.ROLE_ANONYMOUS = "anonymous"
cosmo.ROLE_AUTHENTICATED = "authenticated"


dojo.declare("cosmo.cmp.Cmp", null,
    {
        /**
         * summary: Return request populated with attributes common to all CMP calls.
         */
        getDefaultCMPRequest: function (/*Object*/ handlerDict,
                                        /*boolean?*/ sync){

            var request = cosmo.util.auth.getAuthorizedRequest()

            request.load = handlerDict.load;
            request.handle =  handlerDict.handle;
            request.error = handlerDict.error;
            request.transport = "XMLHTTPTransport";
            request.contentType =  'text/xml';
            request.sync = sync;
            request.headers["Cache-Control"] = "no-cache";
            request.headers["Pragma"] = "no-cache";
            // Fight the dark powers of IE's evil caching mechanism
            if (document.all) {
                request.preventCache = true;
            }

            return request;
        },

        /*
         * Administrator operations:
         *     administrator user credentials must be provided with the
         *     request for these operations
         *
         */
        /**
         * summary: Get a list of the users on this Cosmo instance
         * description: Get an Array of Objects representing a list of
         *              users on this Cosmo instance. The composition of
         *              this list can be specified with the server side
         *              paging parameters <code>pageNumber</code>,
         *              <code>pageSize</code>, <code>sortOrder</code> and
         *              <code>sortType</code>.
         */
        getUsers: function (/*Object*/ handlerDict,
                            /*int*/ pageNumber,
                            /*int*/ pageSize,
                            /*String*/ sortOrder,
                            /*String*/ sortType,
                            /*String*/ query,
                            /*boolean?*/ sync) {
            handlerDict = this._wrapXMLHandlerFunctions(handlerDict, this.cmpUsersXMLToJSON);

            this.getUsersXML(handlerDict, pageNumber, pageSize, sortOrder, sortType, query, sync);
        },

        /**
         * summary: Get a list of the users on this Cosmo instance
         * description: Get an XML representation of a list of
         *              users on this Cosmo instance. The composition of
         *              this list can be specified with the server side
         *              paging parameters <code>pageNumber</code>,
         *              <code>pageSize</code>, <code>sortOrder</code> and
         *              <code>sortType</code>.
         */
        getUsersXML: function (/*Object*/ handlerDict,
                               /*int*/ pageNumber,
                               /*int*/ pageSize,
                               /*String*/ sortOrder,
                               /*String*/ sortType,
                               /*String*/ query,
                               /*boolean?*/ sync) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/users";

            if (pageNumber || pageSize || sortOrder || sortType){
                requestDict.url +=
                    "?pn=" + (pageNumber ? pageNumber : DEFAULT_PAGE_NUMBER).toString() +
                    "&ps=" + (pageSize ? pageSize : DEFAULT_PAGE_SIZE).toString() +
                    "&so=" + (sortOrder ? sortOrder : DEFAULT_SORT_ORDER) +
                    "&st=" + (sortType ? sortType : DEFAULT_SORT_TYPE);
            }
            if (query) {
                requestDict.url += "&q=" + escape(query);
            }
            requestDict.method = "GET";

            dojo.io.bind(requestDict);
        },

        /**
         * summary: Get the user representation for <code>username</code>
         * description: Return an Object representation of <code>username</code>.
         */
        getUser: function(/*String*/ username,
                          /*Object*/ handlerDict,
                          /*boolean?*/ sync) {
            handlerDict = this._wrapXMLHandlerFunctions(handlerDict, this.cmpUserXMLToJSON);

            this.getUserXML(username, handlerDict, sync);
        },

        /**
         * summary: Get the user representation for <code>username</code>
         * description: Return an XML representation of <code>username</code>.
         */
        getUserXML: function (/*String*/ username,
                              /*Object*/ handlerDict,
                              /*boolean?*/ sync) {

            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                encodeURIComponent(dojo.string.trim(username));
            requestDict.method = "GET";

            dojo.io.bind(requestDict);
        },

        /**
         * summary: Create the user described by <code>userHash</code>
         * description: Create the user described by <code>userHash</code>.
         */
        createUser: function (userHash, handlerDict, sync) {
            var request_content = this.userHashToXML(userHash);
            

                requestDict = this.getDefaultCMPRequest(handlerDict, sync)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                    encodeURIComponent(dojo.string.trim(userHash.username));
                requestDict.method = "POST";
                   requestDict.headers['X-Http-Method-Override'] = "PUT";
                requestDict.postContent = request_content;

                dojo.io.bind(requestDict);
        },

        /**
         * summary: Modify <code>username</code>'s account
         * description: Update <code>username</code>'s account with
         *              the values in <code>userHash</code>
         */
        modifyUser: function (username, userHash, handlerDict, sync) {
            // If the user to be modified is the current user and
            // we're changing username or password, make
            // sure the current credentials will be changed on success
            if (username == cosmo.util.auth.getUsername() &&
                (userHash.password || userHash.username)){
                
                var newCred = {};
                if (userHash.password){
                    newCred.password = userHash.password;
                }
                if (userHash.username){
                    newCred.username = userHash.username;
                }
                
                handlerDict = this._wrapChangeCredentialFunctions(
                    handlerDict, [204], newCred);
            }
            
            // Safari and IE don't understand 204s. Boo.
            if (navigator.userAgent.indexOf('Safari') > -1 ||
                document.all){
                handlerDict = this._wrap204Bandaid(handlerDict);
            }
            
            var request_content = this.userHashToXML(userHash);
            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                encodeURIComponent(dojo.string.trim(username));
            requestDict.method = "POST";
            requestDict.headers['X-Http-Method-Override'] = "PUT";
            requestDict.postContent = request_content;
            dojo.io.bind(requestDict);

        },

        /**
         * summary: Delete <code>username</code>
         * description: Delete <code>username</code>
         *
         */
        deleteUser: function (/*String*/ username,
                              /*Object*/ handlerDict,
                              /*boolean?*/ sync) {

                // Safari and IE don't understand 204s. Boo.
                if (navigator.userAgent.indexOf('Safari') > -1 ||
                    document.all){
                    handlerDict = this._wrap204Bandaid(handlerDict);
                }

                var requestDict = this.getDefaultCMPRequest(handlerDict, sync);

                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                                    encodeURIComponent(username);
                requestDict.method = "POST";
                requestDict.headers['X-Http-Method-Override'] = "DELETE";
                dojo.io.bind(requestDict);

        },

        /**
         * summary: Delete the all users in <code>usernames</code>
         * description: Delete all the users whose usernames appear in
         *              <code>usernames</code>
         *
         */
        deleteUsers: function (/*String[]*/ usernames,
                               /*Object*/ handlerDict,
                               /*boolean?*/ sync) {
                            // Safari and IE don't understand 204s. Boo.
                if (navigator.userAgent.indexOf('Safari') > -1 ||
                    document.all){
                    handlerDict = this._wrap204Bandaid(handlerDict);
                }

                var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/delete";
                requestDict.method = "POST";


                for (var i = 0; i < usernames.length; i++){
                    usernames[i] = "user=" +
                        encodeURIComponent(usernames[i]);
                }
                var requestContent = usernames.join("&");

                requestDict.postContent = requestContent;
                requestDict.contentType = "application/x-www-form-urlencoded";
                dojo.io.bind(requestDict);

        },

        /**
         * summary: Activate <code>username</code>'s account
         * description: Activate <code>username</code>'s account
         */
        activate: function (/*String*/ username,
                            /*Object*/ handlerDict,
                            /*boolean?*/ sync) {

            // Safari and IE don't understand 204s. Boo.
            if (navigator.userAgent.indexOf('Safari') > -1 ||
                document.all){
                handlerDict = this._wrap204Bandaid(handlerDict);
            }

            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);

            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/activate/" + username;
            requestDict.method = "POST";
            requestDict.postContent = "id="+username;
            dojo.io.bind(requestDict);
        },

        /**
         * summary: Return the number of users on this server.
         * description: Return the number of users on this server.
         */
        getUserCount: function (/*Object*/ handlerDict,
                                /*boolean?*/ sync){
        	var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
        	requestDict.url = cosmo.env.getBaseUrl() + "/cmp/users/count";
        	requestDict.method = "GET";
			dojo.io.bind(requestDict);
        },

        /**
         * summary: Perform a HEAD request for <code>username</code>.
         * description: Perform a HEAD request for <code>username</code>. Will return
         *              a 404 if the user does not exist, and a 200
         *              if it does.
         */
        headUser: function (/*String*/ username,
                            /*Object*/ handlerDict,
                            /*boolean?*/ sync){
                var requestDict = this.getDefaultCMPRequest(handlerDict, true)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                    encodeURIComponent(dojo.string.trim(username));
                requestDict.method = "HEAD"
                if (sync){
                    requestDict.async = false;
                }

                dojo.io.bind(requestDict);
        },

        /*
         * Authenticated operations:
         *     user credentials must be provided with the request for
         *     these operations
         *
         */
        getAccount: function (/*Object*/ handlerDict,
                              /*boolean?*/ sync) {
            handlerDict = this._wrapXMLHandlerFunctions(handlerDict, this.cmpUserXMLToJSON);

            this.getAccountXML(handlerDict, sync);
        },

        /**
         * summary: Get the user representation for the current user.
         * description: Return an XML representation of the current user.
         */
        getAccountXML: function(/*Object*/ handlerDict,
                                /*boolean?*/ sync) {
                var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/account";
                requestDict.method = "GET";

                dojo.io.bind(requestDict);
        },

        /**
         * summary: Get the user representation for the current user.
         * description: Return an Object representation of the current user.
         */
        modifyAccount: function (/*Object*/ userHash,
                                 /*Object*/ handlerDict,
                                 /*boolean?*/ sync) {
                // If the user is changing his password,
                // make sure to wrap this in the credential
                // change-on-success function
                if (userHash.password){
                    handlerDict = this._wrapChangeCredentialFunctions(handlerDict,
                                                        [204],
                                                        {password:userHash.password});
                }

                // Safari and IE don't understand 204s. Boo.
                if (navigator.userAgent.indexOf('Safari') > -1 ||
                    document.all){
                    handlerDict = this._wrap204Bandaid(handlerDict);
                }

                var requestContent = this.userHashToXML(userHash)

                var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/account";
                requestDict.method = "POST";
                requestDict.headers['X-Http-Method-Override'] = "PUT";
                requestDict.postContent = requestContent;


                dojo.io.bind(requestDict);

        },

        /*
         * Anonymous operations:
         *      Credentials should not be provided with this request
         */
        /**
         * summary: Sign up the user represented by <code>userHash</code>
         * description: Sign up the user represented by <code>userHash</code>
         *              and return an Object representation of that user.
         */
        signup: function (/*Object*/ userHash,
                          /*Object*/ handlerDict,
                          /*boolean?*/ sync) {
            var self = this;
            handlerDict = this._wrapXMLHandlerFunctions(handlerDict, this.cmpUserXMLToJSON);

            this.getSignupXML(userHash, handlerDict, sync);
        },

        /**
         * summary: Sign up the user represented by <code>userHash</code>
         * description: Sign up the user represented by <code>userHash</code>
         *              and return an XML representation of that user.
         */
        getSignupXML: function(/*Object*/ userHash,
                               /*Object*/ handlerDict,
                               /*boolean?*/ sync) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/signup";
            requestDict.method = "POST";
            requestDict.headers['X-Http-Method-Override'] = "PUT";
            requestDict.postContent = this.userHashToXML(userHash);
            dojo.io.bind(requestDict);
        },

        userHashToXML: function(/*Object*/ userHash){
            return '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
                '<user xmlns="http://osafoundation.org/cosmo/CMP">' +
                (userHash.username? '<username>' + dojo.string.escapeXml(userHash.username) + '</username>' : "") +
                (userHash.password? '<password>' + dojo.string.escapeXml(userHash.password) + '</password>' : "") +
                (userHash.firstName? '<firstName>' + dojo.string.escapeXml(userHash.firstName) + '</firstName>' : "") +
                (userHash.lastName? '<lastName>' + dojo.string.escapeXml(userHash.lastName) + '</lastName>': "") +
                (userHash.email? '<email>' + dojo.string.escapeXml(userHash.email) + '</email>': "") +
                (userHash.subscription? this._subscriptionToXML(userHash.subscription) : "") +
                (userHash.administrator? '<' + EL_ADMINISTRATOR + ' >true</' + EL_ADMINISTRATOR + '>' : "") +
                (userHash.locked?'<locked>true</locked>' : "") +
                '</user>';
        },

        _subscriptionToXML: function (/*Object*/ subscription){
            var name = subscription.name;
            var ticket = subscription.ticket;
            var uuid = subscription.uuid;
            if (!(name && ticket && uuid)){
                throw new cosmo.cmp.SubscriptionInfoMissingException(
                    name, ticket, uuid);
            }
            return '<subscription name="' + dojo.string.escapeXml(name) + 
                '" ticket="' + ticket + '">' + 
                uuid + '</subscription>';
        },

        sendActivation: function(username, email, handlerDict, sync){
            this._recover(cosmo.env.getFullUrl("Cmp") + "/account/activation/send", 
                         username, email, handlerDict, sync)
        },

        recoverPassword: function(username, email, handlerDict, sync){
            this._recover(cosmo.env.getFullUrl("Cmp") + "/account/password/recover", 
                         username, email, handlerDict, sync)
        },

        _recover: function(url, username, email, handlerDict, sync){
            // Safari and IE don't understand 204s. Boo.
            if (navigator.userAgent.indexOf('Safari') > -1 ||
                document.all){
                handlerDict = this._wrap204Bandaid(handlerDict);
            }

            var requestContent =
                username? "username=" + username : "" +
                username && email? "&" : "" +
                email? "email=" + email : "";

            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = url;
            requestDict.method = "POST";
            requestDict.contentType = "application/x-www-form-urlencoded";
            requestDict.postContent = requestContent;

            dojo.io.bind(requestDict);
        },

        resetPassword: function(key, password, handlerDict, sync){

            var requestContent = "password=" + password;

            // Safari and IE don't understand 204s. Boo.
            if (navigator.userAgent.indexOf('Safari') > -1 ||
                document.all){
                handlerDict = this._wrap204Bandaid(handlerDict);
            }

            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/account/password/reset/" + key;
            requestDict.method = "POST";
            requestDict.contentType = "application/x-www-form-urlencoded";
            requestDict.postContent = requestContent;

            dojo.io.bind(requestDict);
        },

        cmpUserXMLToJSON: function (/*Element*/ cmpUserXml){
            var user = cmpUserXml;
            
            var obj = {};
            obj.firstName = user.getElementsByTagName("firstName")[0].firstChild.nodeValue;
            obj.lastName = user.getElementsByTagName("lastName")[0].firstChild.nodeValue;
            obj.username = user.getElementsByTagName("username")[0].firstChild.nodeValue;
            obj.email = user.getElementsByTagName("email")[0].firstChild.nodeValue;
            obj.dateCreated = user.getElementsByTagName("created")[0].firstChild.nodeValue;
            obj.dateModified = user.getElementsByTagName("modified")[0].firstChild.nodeValue;
            obj.url = user.getElementsByTagName("url")[0].firstChild.nodeValue;
            obj.locked = (dojo.string.trim(
                user.getElementsByTagName("locked")[0].firstChild.nodeValue) == "true");
            obj.administrator =	(dojo.string.trim(
                user.getElementsByTagName("administrator")[0].firstChild.nodeValue) == "true");
            if (user.getElementsByTagName("unactivated").length > 0){
                obj.unactivated = true;
            }

            if (user.getElementsByTagName("homedirUrl").length > 0){
                obj.homedirUrl = user.getElementsByTagName("homedirUrl")[0].firstChild.nodeValue;
            }

            return obj;
        },

        cmpUsersXMLToJSON: function (cmpUsersXml){

            var users = cmpUsersXml.getElementsByTagName("user");
            var userList = [];

            for (i = 0; i < users.length; i++){
                userList[i] = this.cmpUserXMLToJSON(users[i]);
            }

            return userList;
        },

        _wrapXMLHandlerFunctions: function (/*Object*/ hDict,
                                            /*function*/ xmlParseFunc){
            var self = this;
            var handlerDict = dojo.lang.shallowCopy(hDict);
            if (handlerDict.load){
                var old_load = handlerDict.load;
                handlerDict.load = function(type, data, evt){
                    var parsedCMPXML = xmlParseFunc.apply(self, [evt.responseXML])
                    old_load(type, parsedCMPXML, evt);
                }
            }
			// Don't mess with "error". These responses shouldn't be XML.
			// Don't mess with "handle". This is a "don't mess with my stuff" handler.
			return handlerDict;
        },

        /*
         * statusCodes is a list of status codes to
         * call the cred change function on.
         */
        _wrapChangeCredentialFunctions: function(/*Object*/ hDict,
                                                 /*int[]*/ statusCodes,
                                                 /*Object*/ newCred){
            var self = this;
            var handlerDict = dojo.lang.shallowCopy(hDict);
            if (handlerDict.load){
                var oldLoad = handlerDict.load;
                handlerDict.load = function(type, data, evt){
                    self._changeCredIfStatusMatches(
                        evt.status, statusCodes, newCred);
                    oldLoad(type, data, evt);
                }
            }

            if (handlerDict.handle){
                var oldHandle = handlerDict.handle;
                handlerDict.handle = function(type, data, evt){
                    self._changeCredIfStatusMatches(
                        evt.status, statusCodes, newCred);
                    oldHandle(type, data, evt);
                }

            }

            return handlerDict;

        },

        _wrap204Bandaid: function(hDict){
            var handlerDict = dojo.lang.shallowCopy(hDict);

            if (handlerDict.load){
                handlerDict.load = this._204Bandaid(handlerDict.load);
            }
            if (handlerDict.error){
                handlerDict.error = this._204Bandaid(
                    handlerDict.error, handlerDict.load);
            }
            if (handlerDict.handle){
                handlerDict.handle = this._204Bandaid(handlerDict.handle);
            }

            return handlerDict;
        },

        _204Bandaid: function(originalFunc, handle204Func){
            // Use original function if handle204Func is not specified.
            handle204Func = handle204Func? handle204Func: originalFunc;
            return function(type, data, evt){
                if (navigator.userAgent.indexOf('Safari') > -1 &&
                    !evt.status) {

                    var newEvt = dojo.lang.shallowCopy(evt);
                    newEvt.status = 204;
                    newEvt.statusText = "No Content";
                    handle204Func('load', '', newEvt);

                }

                // If we're Internet Explorer
                else if (document.all &&
                         evt.status == 1223){
                    // apparently, shallow copying the XHR object in ie
                    // causes problems.
                    var newEvt = {};
                    newEvt.status = 204;
                    newEvt.statusText = "No Content";
                    handle204Func('load', '', newEvt);
                } else {
                    originalFunc(type, data, evt);
                }
            }
        },

        _changeCredIfStatusMatches: function (stat, statusCodes, newCred){
            for (var i = 0; i < statusCodes.length; i++){
                if (newCred.username){
                    cosmo.util.auth.setUsername(newCred.username);
                }
                if (newCred.password){
                    cosmo.util.auth.setPassword(newCred.password);
                }
            }
        }
    }
);

cosmo.cmp = new cosmo.cmp.Cmp();

dojo.declare("cosmo.cmp.SubscriptionInfoMissingException", Error, function(){}, {
    name: null,
    ticket: null,
    uuid: null,
    initializer: function(name, ticket, uuid){
        this.name = name;
        this.ticket = ticket;
        this.uuid = uuid;
    },
    toString: function(){
        return "Subscription must have name, ticket and uuid.\nname: " + this.name +
            "\nticket: " + this.ticket + "\nuuid: " + this.uuid;
    }
});
