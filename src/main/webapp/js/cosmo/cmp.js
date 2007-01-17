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

        getDefaultCMPRequest: function (handlerDict, sync){
			
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


        /**
         * All of these methods use handlerDicts. handlerDicts should specify handlers
         * identical to those used by dojo.io.bind
         */

        getUserXML: function (username, handlerDict, sync) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" + encodeURIComponent(username);
            requestDict.method = "GET";

            dojo.io.bind(requestDict);
        },

        getUsersXML: function (handlerDict, pageNumber, pageSize, sortOrder, sortType, sync) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/users";

            if (pageNumber || pageSize || sortOrder || sortType){
                requestDict.url +=
                    "?pn=" + (pageNumber ? pageNumber : DEFAULT_PAGE_NUMBER).toString() +
                    "&ps=" + (pageSize ? pageSize : DEFAULT_PAGE_SIZE).toString() +
                    "&so=" + (sortOrder ? sortOrder : DEFAULT_SORT_ORDER) +
                    "&st=" + (sortType ? sortType : DEFAULT_SORT_TYPE);
            }
            requestDict.method = "GET";

            dojo.io.bind(requestDict);
        },

        getAccountXML: function(handlerDict, sync) {
                var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/account";
                requestDict.method = "GET";

                dojo.io.bind(requestDict);
        },

        /**
         * These functions are sugar for getting the XML information and parsing
         * into a nice javascript object.
         */
        _wrapXMLHandlerFunctions: function (handlerDict, newFunc){
            var self = this;

            if (handlerDict.load != undefined){
                handlerDict.old_load = handlerDict.load
                handlerDict.load = function(type, data, evt){

                    handlerDict.old_load(type, self[newFunc](evt.responseXML, self), evt);
                }
            }
			// Don't mess with "error". These responses shouldn't be XML.
			// Don't mess with "handle". This is a "don't mess with my stuff" handler.
        },

        getUser: function(username, handlerDict, sync) {
            var self = this;
            this._wrapXMLHandlerFunctions(handlerDict, '_cmpUserXMLToJSON');

            this.getUserXML(username, handlerDict, sync);
        },

        getUsers: function (handlerDict, pageNumber, pageSize, sortOrder, sortType, sync) {
            var self = this;
            this._wrapXMLHandlerFunctions(handlerDict, '_cmpUsersXMLToJSON');

            this.getUsersXML(handlerDict, pageNumber, pageSize, sortOrder, sortType, sync);
        },

        getAccount: function (handlerDict, sync) {
            var self = this;
            this._wrapXMLHandlerFunctions(handlerDict, '_cmpUserXMLToJSON');

            this.getAccountXML(handlerDict, sync);
        },

        headUser: function (username, handlerDict, sync){
                var requestDict = this.getDefaultCMPRequest(handlerDict, true)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" + encodeURIComponent(username)
                requestDict.method = "HEAD"
                if (sync){
                    requestDict.async = false;
                }

                dojo.io.bind(requestDict)
        },

        createUser: function (userHash, handlerDict, sync) {
                var request_content = '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
                                '<user xmlns="http://osafoundation.org/cosmo/CMP">' +
                                '<username>' + userHash.username + '</username>' +
                                '<password>' + userHash.password + '</password>' +
                                '<firstName>' + userHash.firstName + '</firstName>' +
                                '<lastName>' + userHash.lastName + '</lastName>' +
                                '<email>' + userHash.email + '</email>'

                if (userHash.administrator) {
                    request_content += '<' + EL_ADMINISTRATOR + ' >true</' + 
                    	EL_ADMINISTRATOR + '>'
                }

                request_content += '</user>'

                requestDict = this.getDefaultCMPRequest(handlerDict, true, sync)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" + encodeURIComponent(userHash.username)
                requestDict.method = "POST"
                   requestDict.headers['X-Http-Method-Override'] = "PUT"
                requestDict.postContent = request_content

                dojo.io.bind(requestDict)
        },

        modifyUser: function (username, userHash, handlerDict, sync) {
                var request_content = '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
                        '<user xmlns="http://osafoundation.org/cosmo/CMP">'
                for (propName in userHash) {
                     request_content += "<" + propName + ">" + userHash[propName] + "</" + propName + ">";

                }
                request_content += "</user>"

                var requestDict = this.getDefaultCMPRequest(handlerDict, sync)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                                    encodeURIComponent(username)
                requestDict.method = "POST"
                   requestDict.headers['X-Http-Method-Override'] = "PUT"
                requestDict.postContent = request_content

                dojo.io.bind(requestDict)

        },

        deleteUser: function (username, handlerDict, sync) {
                var requestDict = this.getDefaultCMPRequest(handlerDict, sync)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                                    encodeURIComponent(username)// + "/delete"
                requestDict.method = "POST"
                requestDict.headers['X-Http-Method-Override'] = "DELETE"
                dojo.io.bind(requestDict)

        },

        deleteUsers: function (usernames, handlerDict, sync) {
                var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/delete";
                requestDict.method = "POST";


                for (var i = 0; i < usernames.length; i++){
                    usernames[i] = "user=" + encodeURIComponent(usernames[i]);
                }
                var requestContent = usernames.join("&")

                requestDict.postContent = requestContent;
                requestDict.contentType = "application/x-www-form-urlencoded"

                dojo.io.bind(requestDict);

        },

        modifyAccount: function (userHash, handlerDict, sync) {
                var requestContent = '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
                        '<user xmlns="http://osafoundation.org/cosmo/CMP">'

                for (propName in userHash) {

                    requestContent += "<" + propName + ">" + userHash[propName] + "</" + propName + ">";
                }
                requestContent += "</user>"

                var requestDict = this.getDefaultCMPRequest(handlerDict, sync)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/account"
                requestDict.method = "POST"
                requestDict.headers['X-Http-Method-Override'] = "PUT"
                requestDict.postContent = requestContent

                dojo.io.bind(requestDict)

        },

        getSignupXML: function(userHash, handlerDict, sync) {
            var request_content = '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
                            '<user xmlns="http://osafoundation.org/cosmo/CMP">' +
                            '<username>' + userHash.username + '</username>' +
                            '<password>' + userHash.password + '</password>' +
                            '<firstName>' + userHash.firstName + '</firstName>' +
                            '<lastName>' + userHash.lastName + '</lastName>' +
                            '<email>' + userHash.email + '</email>' +
                            '</user>'

            var requestDict = this.getDefaultCMPRequest(handlerDict, sync)
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/signup"
            requestDict.method = "POST"
            requestDict.headers['X-Http-Method-Override'] = "PUT"
            requestDict.postContent = request_content

            dojo.io.bind(requestDict);
        },

        signup: function (userHash, handlerDict, sync) {
            var self = this;
            this._wrapXMLHandlerFunctions(handlerDict, '_cmpUserXMLToJSON');

            this.getSignupXML(userHash, handlerDict, sync);
        },

        activate: function (username, handlerDict, sync) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/activate/" + username;
            requestDict.method = "POST";
            requestDict.postContent = "id="+username;
            dojo.io.bind(requestDict);
        },
        
        getUserCount: function (handlerDict, sync){
        	var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
        	requestDict.url = cosmo.env.getBaseUrl() + "/cmp/users/count";
        	requestDict.method = "GET";
			dojo.io.bind(requestDict);
        },

        _cmpUserXMLToJSON: function (cmpUserXml){
            var user = cmpUserXml;
            var obj = {};
            obj.firstName = user.getElementsByTagName("firstName")[0].firstChild.nodeValue;
            obj.lastName = user.getElementsByTagName("lastName")[0].firstChild.nodeValue;
            obj.username = user.getElementsByTagName("username")[0].firstChild.nodeValue;
            obj.email = user.getElementsByTagName("email")[0].firstChild.nodeValue;
            obj.dateCreated = user.getElementsByTagName("created")[0].firstChild.nodeValue;
            obj.dateModified = user.getElementsByTagName("modified")[0].firstChild.nodeValue;
            obj.url = user.getElementsByTagName("url")[0].firstChild.nodeValue;
            
            obj.administrator = 
            	(
            	dojo.string.trim(
            		user.getElementsByTagName("administrator")[0].firstChild.nodeValue) ==
            	"true"
            	);
            if (user.getElementsByTagName("unactivated").length > 0){
                obj.unactivated = true;
            }

            if (user.getElementsByTagName("homedirUrl").length > 0){
                obj.homedirUrl = user.getElementsByTagName("homedirUrl")[0].firstChild.nodeValue;
            }

            return obj;
        },

        _cmpUsersXMLToJSON: function (cmpUsersXml){

            var users = cmpUsersXml.getElementsByTagName("user");
            var userList = [];

            for (i = 0; i < users.length; i++){
                userList[i] = this._cmpUserXMLToJSON(users[i]);
            }

            return userList;
        }


    }

)

cosmo.cmp.cmpProxy = new cosmo.cmp.Cmp();

//Cmp is a singleton
cosmo.cmp.Cmp = null;
