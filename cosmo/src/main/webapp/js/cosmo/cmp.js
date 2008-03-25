/* * Copyright 2006-2008 Open Source Applications Foundation *
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
 */

dojo.provide("cosmo.cmp");

dojo.require("cosmo.util.string");
dojo.require("cosmo.env");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.util.lang");

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
        _baseUrl: null,

        constructor: function(url){
            this._baseUrl = url;
        },

        /**
         * summary: Return request populated with attributes common to all CMP calls.
         */
        getDefaultCMPRequest: function (/*Object*/ ioArgs){
            ioArgs = ioArgs || {};
            var request = cosmo.util.auth.getAuthorizedRequest()
            request.load = ioArgs.load;
            request.handle =  ioArgs.handle;
            request.error = ioArgs.error;
            request.contentType =  'text/xml';
            request.sync = ioArgs.sync;
            // TODO: is this still needed?
            if (dojo.isIE) {
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
        getUsers: function (/*int*/ pageNumber,
                            /*int*/ pageSize,
                            /*String*/ sortOrder,
                            /*String*/ sortType,
                            /*String*/ query,
                            /*Object?*/ ioArgs) {
            ioArgs = this._wrapXMLHandlerFunctions(ioArgs, this.cmpUsersXMLToJSON);

            return this.getUsersXML(pageNumber, pageSize, sortOrder, sortType, query, ioArgs);
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
        getUsersXML: function (/*int*/ pageNumber,
                               /*int*/ pageSize,
                               /*String*/ sortOrder,
                               /*String*/ sortType,
                               /*String*/ search,
                               /*Object?*/ ioArgs) {
            var requestDict = this.getDefaultCMPRequest(ioArgs);
            var query = {};
            requestDict.url = this._baseUrl + "/users";
            query.pn = pageNumber || DEFAULT_PAGE_NUMBER;
            query.ps = pageSize || DEFAULT_PAGE_SIZE;
            query.so = sortOrder || DEFAULT_SORT_ORDER;
            query.st = sortType || DEFAULT_SORT_TYPE;
            if (search) query.q = search;
            requestDict.content = query;
            return dojo.xhrGet(requestDict);
        },

        /**
         * summary: Get the user representation for <code>username</code>
         * description: Return an Object representation of <code>username</code>.
         */
        getUser: function(/*String*/ username,
                          /*Object*/ ioArgs){
            ioArgs = this._wrapXMLHandlerFunctions(ioArgs, this.cmpUserXMLToJSON);

            return this.getUserXML(username, ioArgs);
        },

        /**
         * summary: Get the user representation for <code>username</code>
         * description: Return an XML representation of <code>username</code>.
         */
        getUserXML: function (/*String*/ username,
            /*Object*/ ioArgs){

            var requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.url = this._baseUrl + "/user/" +
                encodeURIComponent(username);

            return dojo.xhrGet(requestDict);
        },

        /**
         * summary: Create the user described by <code>userHash</code>
         * description: Create the user described by <code>userHash</code>.
         */
        createUser: function (userHash, ioArgs){
            var request_content = this.userHashToXML(userHash);
            

            requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.url = this._baseUrl + "/user/" +
                encodeURIComponent(userHash.username);

            requestDict.putData = request_content;
            return dojo.rawXhrPut(requestDict);
        },

        /**
         * summary: Modify <code>username</code>'s account
         * description: Update <code>username</code>'s account with
         *              the values in <code>userHash</code>
         */
        modifyUser: function (username, userHash, ioArgs){
            // If the user to be modified is the current user and
            // we're changing username or password, make
            // sure the current credentials will be changed on success
            if (dojo.isIE){
                ioArgs = this._wrap204Bandaid(ioArgs);
            }
            
            var request_content = this.userHashToXML(userHash);
            var requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.url = this._baseUrl + "/user/" +
                encodeURIComponent(username);
            requestDict.putData = request_content;
            var d = dojo.rawXhrPut(requestDict);
            if (username == cosmo.util.auth.getUsername() &&
                (userHash.password || userHash.username)){
                d.addCallback(this._changeCredCB(userHash.username, userHash.password));
            }
            return d;

        },

        /**
         * summary: Delete <code>username</code>
         * description: Delete <code>username</code>
         *
         */
        deleteUser: function (/*String*/ username,
            /*Object*/ ioArgs){

            if (dojo.isIE){
                ioArgs = this._wrap204Bandaid(ioArgs);
            }
            
            var requestDict = this.getDefaultCMPRequest(ioArgs);
            
            requestDict.url = this._baseUrl + "/user/" +
                encodeURIComponent(username);
            return dojo.xhrDelete(requestDict);

        },

        /**
         * summary: Delete the all users in <code>usernames</code>
         * description: Delete all the users whose usernames appear in
         *              <code>usernames</code>
         *
         */
        deleteUsers: function (/*String[]*/ usernames,
            /*Object*/ ioArgs){

            if (dojo.isIE){
                ioArgs = this._wrap204Bandaid(ioArgs);
            }
            
            var requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.url = this._baseUrl + "/user/delete";
            var usernameArgs = [];
            for (var i = 0; i < usernames.length; i++){
                usernameArgs[i] = "user=" +
                    encodeURIComponent(usernames[i]);
            }
            var requestContent = usernameArgs.join("&");
            
            requestDict.postData = requestContent;
            requestDict.contentType = "application/x-www-form-urlencoded";
            return dojo.rawXhrPost(requestDict);
        },

        /**
         * summary: Activate <code>username</code>'s account
         * description: Activate <code>username</code>'s account
         */
        activate: function (/*String*/ username,
            /*Object*/ ioArgs){

            if (dojo.isIE){
                ioArgs = this._wrap204Bandaid(ioArgs);
            }

            var requestDict = this.getDefaultCMPRequest(ioArgs);

            requestDict.url = this._baseUrl + "/activate/" + username;
            requestDict.postData = "id="+username;
            return dojo.rawXhrPost(requestDict);
        },

        /**
         * summary: Return the number of users on this server.
         * description: Return the number of users on this server.
         */
        getUserCount: function (/*String*/ search, /*Object*/ ioArgs){
        	var requestDict = this.getDefaultCMPRequest(ioArgs);
        	requestDict.url = this._baseUrl + "/users/count";
            if (search) requestDict.content = {q: search};

			var d = dojo.xhrGet(requestDict);
            d.addCallback(function(countString){
                return parseInt(countString);
            });
            return d;
        },

        /**
         * summary: Perform a HEAD request for <code>username</code>.
         * description: Perform a HEAD request for <code>username</code>. Will return
         *              a 404 if the user does not exist, and a 200
         *              if it does.
         */
        headUser: function (/*String*/ username,
            /*Object*/ ioArgs){
            var requestDict = this.getDefaultCMPRequest(ioArgs, true)
            requestDict.url = this._baseUrl + "/user/" +
                encodeURIComponent(username);

            return dojo.xhrHead(requestDict);
        },

        /*
         * Authenticated operations:
         *     user credentials must be provided with the request for
         *     these operations
         *
         */
        getAccount: function (/*Object*/ ioArgs){
            ioArgs = this._wrapXMLHandlerFunctions(ioArgs, this.cmpUserXMLToJSON);

            return this.getAccountXML(ioArgs);
        },

        /**
         * summary: Get the user representation for the current user.
         * description: Return an XML representation of the current user.
         */
        getAccountXML: function(/*Object*/ ioArgs){
            var requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.url = this._baseUrl + "/account";
            
            return dojo.xhrGet(requestDict);
        },

        /**
         * summary: Get the user representation for the current user.
         * description: Return an Object representation of the current user.
         */
        modifyAccount: function (/*Object*/ userHash,
            /*Object*/ ioArgs){
            // If the user is changing his password,
            // make sure to wrap this in the credential
            // change-on-success function
         
            if (dojo.isIE){
                ioArgs = this._wrap204Bandaid(ioArgs);
            }
            
            var requestContent = this.userHashToXML(userHash)
            
            var requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.url = this._baseUrl + "/account";
            requestDict.putData = requestContent;
            
            var d = dojo.rawXhrPut(requestDict);
            if (userHash.password){
                d.addCallback(this._changeCredCB(null, userHash.password));
            }
            return d;
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
            /*Object*/ ioArgs){
            var self = this;
            ioArgs = this._wrapXMLHandlerFunctions(ioArgs, this.cmpUserXMLToJSON);

            return this.getSignupXML(userHash, ioArgs);
        },

        /**
         * summary: Sign up the user represented by <code>userHash</code>
         * description: Sign up the user represented by <code>userHash</code>
         *              and return an XML representation of that user.
         */
        getSignupXML: function(/*Object*/ userHash,
            /*Object*/ ioArgs){
            var requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.url = this._baseUrl + "/signup";
            requestDict.putData = this.userHashToXML(userHash);
            return dojo.rawXhrPut(requestDict);
        },

        userHashToXML: function(/*Object*/ userHash){
            var isRoot = userHash.username == "root";
            return '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
                '<user xmlns="http://osafoundation.org/cosmo/CMP">' +
                (userHash.username && !isRoot? '<username>' + cosmo.util.string.escapeXml(userHash.username) + '</username>' : "") +
                (userHash.password? '<password>' + cosmo.util.string.escapeXml(userHash.password) + '</password>' : "") +
                (userHash.firstName && !isRoot? '<firstName>' + cosmo.util.string.escapeXml(userHash.firstName) + '</firstName>' : "") +
                (userHash.lastName && !isRoot? '<lastName>' + cosmo.util.string.escapeXml(userHash.lastName) + '</lastName>': "") +
                (userHash.email? '<email>' + cosmo.util.string.escapeXml(userHash.email) + '</email>': "") +
                (userHash.subscription? this._subscriptionToXML(userHash.subscription) : "") +
                (userHash.administrator && !isRoot? '<' + EL_ADMINISTRATOR + ' >true</' + EL_ADMINISTRATOR + '>' : "") +
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
            return '<subscription name="' + cosmo.util.string.escapeXml(name) + 
                '" ticket="' + ticket + '">' + 
                uuid + '</subscription>';
        },

        sendActivation: function(username, email, ioArgs){
            this._recover(this._baseUrl + "/account/activation/send", 
                          username, email, ioArgs);
        },

        recoverPassword: function(username, email, ioArgs){
            return this._recover(this._baseUrl + "/account/password/recover", 
                                 username, email, ioArgs);
        },

        _recover: function(url, username, email, ioArgs){
            if (dojo.isIE){
                ioArgs = this._wrap204Bandaid(ioArgs);
            }

            var requestContent = {};
            if (username) requestContent.username = username;
            if (email) requestContent.email = email;

            var requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.contentType = null;
            requestDict.url = url;
            requestDict.content = requestContent;
            return dojo.xhrPost(requestDict);
        },

        resetPassword: function(key, password, ioArgs){
            if (dojo.isIE){
                ioArgs = this._wrap204Bandaid(ioArgs);
            }

            var requestDict = this.getDefaultCMPRequest(ioArgs);
            requestDict.contentType = null;
            requestDict.url = this._baseUrl + "/account/password/reset/" + key;
            requestDict.content = {password: password};

            return dojo.xhrPost(requestDict);
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
            obj.locked = (dojo.trim(
                user.getElementsByTagName("locked")[0].firstChild.nodeValue) == "true");
            obj.administrator =	(dojo.trim(
                user.getElementsByTagName("administrator")[0].firstChild.nodeValue) == "true");
            obj.unactivated = (user.getElementsByTagName("unactivated").length > 0)?
                true : null;

            obj.homedirUrl = (user.getElementsByTagName("homedirUrl").length > 0)? 
                user.getElementsByTagName("homedirUrl")[0].firstChild.nodeValue: null;

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
            var handlerDict = cosmo.util.lang.shallowCopy(hDict) || {};
            if (handlerDict.load || !(handlerDict.load && handlerDict.handle)){
                var old_load = handlerDict.load;
                handlerDict.load = function(response, ioArgs){
                    var parsedCMPXML = xmlParseFunc.apply(self, [ioArgs.xhr.responseXML])
                    return old_load? old_load(parsedCMPXML, ioArgs) : parsedCMPXML;
                }
            }
			// Don't mess with "error". These responses shouldn't be XML.
			// Don't mess with "handle". This is a "don't mess with my stuff" handler.
			return handlerDict;
        },

        _wrap204Bandaid: function(hDict){
            var handlerDict = cosmo.util.lang.shallowCopy(hDict) || {};

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
            return function(response, ioArgs){
                if (dojo.isIE &&
                    evt.status == 1223){
                    ioArgs.xhr = {
                        status: 204,
                        statusText: "No Content"
                    };
                    handle204Func(response, ioArgs);
                } else {
                    originalFunc(response, ioArgs);
                }
            }
        },

        _changeCredCB: function (username, password){
            return function(result){
                if (username){
                    cosmo.util.auth.setUsername(username);
                }
                if (password){
                    cosmo.util.auth.setPassword(password);
                }
                return result;
            }
        } 
    }
);

cosmo.cmp = new cosmo.cmp.Cmp(cosmo.env.getFullUrl("Cmp"));

dojo.declare("cosmo.cmp.SubscriptionInfoMissingException", Error,  {
    name: null,
    ticket: null,
    uuid: null,
    constructor: function(name, ticket, uuid){
        this.name = name;
        this.ticket = ticket;
        this.uuid = uuid;
    },
    toString: function(){
        return "Subscription must have name, ticket and uuid.\nname: " + this.name +
            "\nticket: " + this.ticket + "\nuuid: " + this.uuid;
    }
});
