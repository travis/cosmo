/* * Copyright 2006 Open Source Applications Foundation *
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

dojo.require("dojo.io.*");
dojo.require("cosmo.env");
dojo.provide("cosmo.cmp");

parser = new dojo.xml.Parse();

DEFAULT_PAGE_NUMBER = 1;
DEFAULT_PAGE_SIZE = 25;
DEFAULT_SORT_ORDER = "ascending";
DEFAULT_SORT_TYPE= "username";

CMP_AUTH_COOKIE = "CmpCred";

EL_ADMINISTRATOR = "administrator";

cosmo.ROLE_ADMINISTRATOR = "administrator"
cosmo.ROLE_ANONYMOUS = "anonymous"
cosmo.ROLE_AUTHENTICATED = "authenticated"

function encode64(inp){
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


dojo.declare("cosmo.cmp.Cmp", null,
    {
        setUser: function (username, password){
            dojo.io.cookie.set(CMP_AUTH_COOKIE, encode64(username + ":" + password), -1, "/");
        },

        unsetUser: function (username, password){
            dojo.io.cookie.deleteCookie(CMP_AUTH_COOKIE);
        },

        getDefaultCMPRequest: function (handlerDict, sync){

            var request = {
                    load: handlerDict.load,
                    handle: handlerDict.handle,
                    error: handlerDict.error,
                    transport: "XMLHTTPTransport",
                    contentType: 'text/xml',
                    sync: sync,
                    headers: {}
            }
            var credentials = dojo.io.cookie.get(CMP_AUTH_COOKIE);

            if (credentials) {
                request.headers.Authorization = "Basic " + credentials;
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

        getUserXMLByActivationId: function (activationId, handlerDict, sync) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/activate/" + activationId;
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
            if (handlerDict.error != undefined){
                handlerDict.old_error = handlerDict.error
                handlerDict.error = function(type, data, evt){
                    handlerDict.old_error(type, self[newFunc](evt.responseXML, self), evt);
                }
            }
            if (handlerDict.handle != undefined){
                handlerDict.old_handle = handlerDict.handle
                handlerDict.handle = function(type, data, evt){
                    handlerDict.old_handle(type, self[newFunc](evt.responseXML, self), evt);
                }
            }
        },

        getUser: function(username, handlerDict, sync) {
            var self = this;
            this._wrapXMLHandlerFunctions(handlerDict, '_cmpUserXMLToJSON');

            this.getUserXML(username, handlerDict, sync);
        },

        getUserByActivationId: function(username, handlerDict, sync) {
            var self = this;
            this._wrapXMLHandlerFunctions(handlerDict, '_cmpUserXMLToJSON');

            this.getUserXMLByActivationId(username, handlerDict, sync);
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
                    request_content += '<' + EL_ADMINISTRATOR + ' />'
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
                        if (propName == EL_ADMINISTRATOR){
                            request_content += "<" + EL_ADMINISTRATOR + " />"
                        } else {
                            request_content += "<" + propName + ">" + userHash[propName] + "</" + propName + ">";
                        }
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

                      if (propName == EL_ADMINISTRATOR){
                        requestContent += "<" + EL_ADMINISTRATOR + " />"
                    } else {

                        requestContent += "<" + propName + ">" + userHash[propName] + "</" + propName + ">";
                    }

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

        activate: function (activationId, handlerDict, sync) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, sync);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/activate/" + activationId;
            requestDict.method = "POST";
            requestDict.postContent = "id="+activationId;
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

            obj.administrator = (user.getElementsByTagName("administrator").length > 0);
            if (user.getElementsByTagName("activationId").length > 0){
                obj.activationId = user.getElementsByTagName("activationId")[0].firstChild.nodeValue;
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