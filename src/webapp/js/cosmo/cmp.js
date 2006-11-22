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

        getDefaultCMPRequest: function (handlerDict){

            var request = {
                    load: handlerDict.load,
                    handle: handlerDict.handle,
                    error: handlerDict.error,
                    transport: "XMLHTTPTransport",
                    contentType: 'text/xml',
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

        getUserXML: function (username, handlerDict) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, true);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" + encodeURIComponent(username);
            requestDict.method = "GET";

            dojo.io.bind(requestDict);
        },

        getUserXMLByActivationId: function (activationId, handlerDict) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, true);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/activate/" + activationId;
            requestDict.method = "GET";

            dojo.io.bind(requestDict);
        },

        getUsersXML: function (handlerDict, pageNumber, pageSize, sortOrder, sortType) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, true);
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

        getAccountXML: function(handlerDict) {
                var requestDict = this.getDefaultCMPRequest(handlerDict, true);
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/account";
                requestDict.method = "GET";

                dojo.io.bind(requestDict);
        },

        /**
         * These functions are sugar for getting the XML information and parsing
         * into a nice javascript object.
         */

        getUser: function(username, handlerDict) {
                handlerDict.old_load = handlerDict.load;
                handlerDict.load = function(type, data, evt) {

                        handlerDict.old_load(type, parser.parseElement(data.firstChild), evt);
                }
                this.getUserXML(username, handlerDict);
        },

        getUsers: function (handlerDict, pageNumber, pageSize, sortOrder, sortType) {
                handlerDict.old_load = handlerDict.load;
                handlerDict.load = function(type, data, evt) {
                        handlerDict.old_load(type, parser.parseElement(data.firstChild), evt);
                }
                this.getUsersXML(handlerDict, pageNumber, pageSize, sortOrder, sortType);
        },

        getAccount: function (handlerDict) {
                handlerDict.old_load = handlerDict.load;
                handlerDict.load = function(type, data, evt) {
                        handlerDict.old_load(type, parser.parseElement(data.firstChild), evt);
                }
                this.getAccountXML(handlerDict);
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

        createUser: function (userHash, handlerDict) {
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

                requestDict = this.getDefaultCMPRequest(handlerDict, true)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" + encodeURIComponent(userHash.username)
                requestDict.method = "POST"
                   requestDict.headers['X-Http-Method-Override'] = "PUT"
                requestDict.postContent = request_content

                dojo.io.bind(requestDict)
        },

        modifyUser: function (username, userHash, handlerDict) {
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

                var requestDict = this.getDefaultCMPRequest(handlerDict)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                                    encodeURIComponent(username)
                requestDict.method = "POST"
                   requestDict.headers['X-Http-Method-Override'] = "PUT"
                requestDict.postContent = request_content

                dojo.io.bind(requestDict)

        },

        deleteUser: function (username, handlerDict) {
                var requestDict = this.getDefaultCMPRequest(handlerDict)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/user/" +
                                    encodeURIComponent(username)// + "/delete"
                requestDict.method = "POST"
                requestDict.headers['X-Http-Method-Override'] = "DELETE"
                dojo.io.bind(requestDict)

        },

        deleteUsers: function (usernames, handlerDict) {
                var requestDict = this.getDefaultCMPRequest(handlerDict);
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

        modifyAccount: function (userHash, handlerDict) {
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

                var requestDict = this.getDefaultCMPRequest(handlerDict)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/account"
                requestDict.method = "POST"
                requestDict.headers['X-Http-Method-Override'] = "PUT"
                requestDict.postContent = requestContent

                dojo.io.bind(requestDict)

        },

        signup: function (userHash, handlerDict) {
                var request_content = '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
                                '<user xmlns="http://osafoundation.org/cosmo/CMP">' +
                                '<username>' + userHash.username + '</username>' +
                                '<password>' + userHash.password + '</password>' +
                                '<firstName>' + userHash.firstName + '</firstName>' +
                                '<lastName>' + userHash.lastName + '</lastName>' +
                                '<email>' + userHash.email + '</email>' +
                                '</user>'

                var requestDict = this.getDefaultCMPRequest(handlerDict)
                requestDict.url = cosmo.env.getBaseUrl() + "/cmp/signup"
                requestDict.method = "POST"
                requestDict.headers['X-Http-Method-Override'] = "PUT"
                requestDict.postContent = request_content

                dojo.io.bind(requestDict)
        },

        activate: function (activationId, handlerDict) {
            var requestDict = this.getDefaultCMPRequest(handlerDict, true);
            requestDict.url = cosmo.env.getBaseUrl() + "/cmp/activate/" + activationId;
            requestDict.method = "POST";

            dojo.io.bind(requestDict);
        }


    }
)

cosmo.cmp.cmpProxy = new cosmo.cmp.Cmp();

//Cmp is a singleton
cosmo.cmp.Cmp = null;