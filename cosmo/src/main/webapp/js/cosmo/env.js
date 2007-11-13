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

/**
 * To use this file, you must first provide it with the base URL for your application
 * by calling "setBaseUrl"
 *
 * @fileoverview provides information about the Cosmo environment.
 * @author Bobby Rullo br@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.env");

cosmo.env.OVERLORD_USERNAME = "root";

//private variable for storing environment information. Do not access directly,
//use methods below.
cosmo.env._cosmoConfig = {};
cosmo.env._NULL = {};
cosmo.env._FALSE_OR_ZERO = {};
cosmo.env._version = null;
cosmo.env._staticUrlPathCache = {};

cosmo.env._getCachePropGetterPopulator = function(propName, calculatorFunction ){
   var _calcy = calculatorFunction;

   return  function(){
   var prop = cosmo.env._cosmoConfig[propName];

   if (prop){
       //if we don't use these placeholders, then the preceding if statement will return
       //false, and we'll have to recalculate.
       if (prop == cosmo.env._NULL) {
           return null;
       }

       if (prop == cosmo.env._FALSE_OR_ZERO) {
           return false;
       }

       return prop;
   }

   prop = _calcy();

   if (!prop){
       if (prop == false) {
           cosmo.env._cosmoConfig[propName] = cosmo.env._FALSE_OR_ZERO;
       } else if (prop == null) {
           cosmo.env._cosmoConfig[propName] = cosmo.env._NULL;
       }
   } else {
       cosmo.env._cosmoConfig[propName] = prop;
   }

   return prop;
   };
}

/**
 * Returns the path to the cosmo script base, relative to the document NOT dojo
 */
cosmo.env.getCosmoBase = cosmo.env._getCachePropGetterPopulator("cosmoBase", function(){
    // "../.." is ugly but it works.
    var uri = dojo.hostenv.getBaseScriptUri() + "../../";
    cosmo.env._cosmoConfig["baseCosmoUri"] = uri;
    return uri;
});

/**
 * Returns the path to the widgets template directory , relative to the document NOT dojo.
 * In other words, not for use with dojo.uri.dojoUri(), which wants you to be relative to
 * dojo scripts. This is useful for stuff like css background urls which can't deal with
 * dojo relative uri's
 *
 * TODO - add an option for getting dojo-relative URI's
 */
cosmo.env.getTemplateBase = cosmo.env._getCachePropGetterPopulator("templateBase", function(){
//FIXME maybe this should go in our base widget (once we make one ;-) )
    var uri = cosmo.env.getCosmoBase() + "cosmo/ui/widget/templates/";
    return uri;
});

/**
 * Returns the baseURI of the application.
 */
cosmo.env.getBaseUrl = function(){
    var result = cosmo.env._cosmoConfig["baseUrl"];
    if (typeof(result) == "undefined"){
        throw new Error("You must setBaseUrl before calling this function");
    }
    return result;
}

/**
 * Sets the base url of the application. Provided by the server and set during bootstrap.
 * @param {String} baseUrl
 */
cosmo.env.setBaseUrl = function(baseUrl){
    cosmo.env._cosmoConfig["baseUrl"] = baseUrl;
}

/**
 * Sets the base url for static resources. Provided by the server and set during bootstrap.
 * @param {String} baseUrl
 */
cosmo.env.setStaticBaseUrlTemplate = function(staticBaseUrl){
    cosmo.env._cosmoConfig["staticBaseUrlTemplate"] = staticBaseUrl;
}

cosmo.env.getStaticBaseUrlTemplate = function(){
    return cosmo.env._cosmoConfig["staticBaseUrlTemplate"];
}

cosmo.env.setStaticBaseUrlRange = function(staticBaseUrlRange){
    if (staticBaseUrlRange){
        var parsedRange = staticBaseUrlRange.split("\.\.");
        this._cosmoConfig.rangeLow = parsedRange[0];
        this._cosmoConfig.rangeHigh = parsedRange[1];
    }
}

cosmo.env.getStaticBaseUrlRangeLow = function(){
    return cosmo.env._cosmoConfig["rangeLow"];
}

cosmo.env.getStaticBaseUrlRangeHigh = function(){
    return cosmo.env._cosmoConfig["rangeHigh"];
}

cosmo.env._doesHaveRange = function(){
    return !!cosmo.env._cosmoConfig["rangeLow"];    
}

cosmo.env._getNextSuffix = function(){
    var current = this._currentSuffix;
    var low = this.getStaticBaseUrlRangeLow();
    var high = this.getStaticBaseUrlRangeHigh();
    
    if (!current || (current == high)){
        current =  low;
    } else {
        current++;
    }
    this._currentSuffix = current;
    return current;
}

/**
 * Returns the url for a static resource. Uses a round-robin algorithm to 
 * distribute urls across various hosts.
 */
cosmo.env.getStaticUrl = function(resourcePath){
    var url = this.getStaticBaseUrlTemplate();
    if (!url){
        return this.getBaseUrl() + resourcePath;   
    }
    
    var cached = this._staticUrlPathCache[resourcePath];
    if (cached){
        return cached;
    }
    
    
    if (this._doesHaveRange()){
        var suffix = this._getNextSuffix();
        url = url.replace("*", suffix);
    }
    url += resourcePath;
    this._staticUrlPathCache[resourcePath] = url; 
    return url;
}

/**
 * Sets the number of seconds for server-side timeout. Provided
 * by the server somehow
 * @param {Number} timeoutSeconds (note: this number may be negative)
 * Negative number means effectively no timeout
 */
cosmo.env.setTimeoutSeconds = function(timeoutSeconds) {
    var s = timeoutSeconds || -1;
    if (s < 0) {
        cosmo.env._cosmoConfig["timeoutSeconds"] = -1;
    }
    else {
        cosmo.env._cosmoConfig["timeoutSeconds"] = s;
    }
}

cosmo.env.getImageUrl = function(image){
    s = cosmo.env.getStaticUrl('/templates' + TEMPLATE_DIRECTORY + '/images/' + image);
  return s;
}

cosmo.env.urls = {
    "Login": "/login",
    "Signup": "/login?signup=true",
    "ForgotPassword": "/account/password/recover",
    "Logout": "/logout",
    "Redirect": "/logout",
    "Auth": "/security_check",
    "Notices": function(){return _("About.NoticesUrl")},
    "Browse": "/browse",
    "UserList": '/admin/users',
    "TermsOfService": "/help/tos",
    "PrivacyPolicy": "/help/privacy",
    "About": "/help/about",
    "Dav": "/dav",
    "Cmp": "/cmp"
}

cosmo.env.getFullUrl = function (urlKey) {
    var url = this.urls[urlKey];
    if (typeof url == "function") url = url();
    return this.getBaseUrl() + url;
}

cosmo.env.getRedirectUrl = function(){
    dojo.deprecated("cosmo.env.getRedirectUrl", "please use cosmo.env.getFullUrl instead", "0.8");
    return cosmo.env.getBaseUrl() + '/logout';}

cosmo.env.getLoginRedirect = function(){
    dojo.deprecated("cosmo.env.getLoginRedirect", "please use cosmo.env.getFullUrl instead", "0.8");
    return cosmo.env.getBaseUrl() + "/login";
}

cosmo.env.getAuthProc = function(){
    dojo.deprecated("cosmo.env.getAuthProc", "please use cosmo.env.getFullUrl instead", "0.8");
    return cosmo.env.getBaseUrl() + "/security_check";
}

cosmo.env.getVersion = function(){
    return this._version;
}

cosmo.env.setVersion = function(version){
    this._version = version;
}

/**
 * Returns the number of seconds for the server-side timeout
 * -- -1 indicates no timeout set, infinite timeout
 */
cosmo.env.getTimeoutSeconds = function (){
    return cosmo.env._cosmoConfig["timeoutSeconds"];
}

/**
 * Convenience method to return the number of minutes
 * for the server-side timeout -- -1 indicates no timeout
 * set, infinite timeout
 */
cosmo.env.getTimeoutMinutes = function (){
    var s = cosmo.env._cosmoConfig["timeoutSeconds"];
    var ret = s == -1 ? -1 : s/60;
    return ret;
}

//initialize from djConfig
cosmo.env.setBaseUrl(djConfig['baseUrl']);
cosmo.env.setStaticBaseUrlTemplate(djConfig['staticBaseUrlTemplate']);
cosmo.env.setStaticBaseUrlRange(djConfig['staticBaseUrlRange']);


