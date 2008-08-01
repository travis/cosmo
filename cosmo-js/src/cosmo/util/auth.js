/*
 * Copyright 2008 Open Source Applications Foundation
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

/**
 * summary:
 *      This module provides authentication utility functions.
 * description:
 *      This module provides authentication utility functions, and
 *      is designed to be used by end users. A new authentication backend
 *      can be used by setting the file local variable "am"
 */

dojo.provide("cosmo.util.auth");
dojo.require("cosmo.auth.basic");

(function(){
var am = cosmo.auth.basic;
dojo.mixin(cosmo.util.auth, {
    setCred: function (username, password){
        am.setUsername(username);
        am.setPassword(password);
    },

    setPassword: function(password){
        am.setPassword(password);
    },

    setUsername: function(username){
        am.setUsername(username);
    },

    getUsername: function(){
        return am.getUsername();
    },

    clearAuth: function (){
        am.clearAuth();
    },

    currentlyAuthenticated: function(){
        return am.currentlyAuthenticated();
    },

    getAuthorizedRequest: function(request, kwArgs){
        kwArgs = kwArgs || {};
    	if (!kwArgs.noAuth){
            return am.getAuthorizedRequest();
    	} else return {headers: {}};
    }
});
}());
