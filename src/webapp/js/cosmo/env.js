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
 * @fileoverview provides information about the scooby environment.
 * @author Bobby Rullo br@osafoundation.org
 * @license Apache License 2.0
 */
 
dojo.provide("scooby.env");

//private variable for storing environment information. Do not access directly, 
//use methods below.
scooby.env._scoobyConfig = {};

scooby.env._NULL = {};
scooby.env._FALSE_OR_ZERO = {};
scooby.env._getCachePropGetterPopulator = function(propName, calculatorFunction ){
   var _calcy = calculatorFunction;
   
   return  function(){
   var prop = scooby.env._scoobyConfig[propName];
   
   if (prop){
       dojo.debug("got a cache hit: " + prop);       
       //if we don't use these placeholders, then the preceding if statement will return 
       //false, and we'll have to recalculate.
       if (prop == scooby.env._NULL) {
           return null; 
       }
       
       if (prop == scooby.env._FALSE_OR_ZERO) {
           return false; 
       }
              
       return prop;
   }
   
   
   prop = _calcy();
   dojo.debug("calculated property: " + prop);       

   if (!prop){
       if (prop == false) {
           scooby.env._scoobyConfig[propName] = scooby.env._FALSE_OR_ZERO;
       } else if (prop == null) {
           scooby.env._scoobyConfig[propName] = scooby.env._NULL;
       }     
   } else {
       scooby.env._scoobyConfig[propName] = prop;
   } 
   
   return prop;
   };
}

/**
 * Returns the path to the scooby script base, relative to the document NOT dojo
 */
scooby.env.getScoobyBase = scooby.env._getCachePropGetterPopulator("scoobyBase", function(){
    // "../.." is ugly but it works. 
    var uri = dojo.hostenv.getBaseScriptUri() + "../../";
    scooby.env._scoobyConfig["baseScoobyUri"] = uri;
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
scooby.env.getTemplateBase = scooby.env._getCachePropGetterPopulator("templateBase", function(){
//FIXME maybe this should go in our base widget (once we make one ;-) )
    var uri = scooby.env.getScoobyBase() + "cosmo/ui/widget/templates/";
    return uri;
});

/**
 * Returns the baseURI of the application.
 */
scooby.env.getBaseUrl = function(){
    var result = scooby.env._scoobyConfig["baseUrl"];
    if (typeof(result) == "undefined"){
        throw new Error("You must setBaseUrl before calling this function");
    }
	return result;
}

/**
 * Sets the base url of the application. Provided by the server somehow.
 * @param {String} baseUrl
 */
scooby.env.setBaseUrl = function(baseUrl){
    scooby.env._scoobyConfig["baseUrl"] = baseUrl;
}

scooby.env.getImagesUrl = function(){
	return scooby.env.getBaseUrl() + '/templates/default/images/';
}