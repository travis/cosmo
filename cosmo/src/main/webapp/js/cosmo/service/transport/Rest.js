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

dojo.provide("cosmo.service.transport.Rest");

dojo.require("dojo.io.*");
dojo.require("dojo.string");
dojo.require("dojo.Deferred")
dojo.require("cosmo.env");
dojo.require("cosmo.util.auth");

dojo.declare("cosmo.service.transport.Rest", null,
    {
        translator: null,
        
        initializer: function (translator){
            
        },
        
        /**
         * summary: Return request populated with attributes common to all CMP calls.
         */
        getDefaultRequest: function (/*dojo.Deferred*/deferred, 
                                     /*boolean?*/ sync){
			
            var request = cosmo.util.auth.getAuthorizedRequest();

            request.load = this.resultCallback(deferred);
            request.error = this.errorCallback(deferred);
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
        
        errorCallback: function(/* dojo.Deferred */ deferredRequestHandler){
    		// summary
    		// create callback that calls the Deferreds errback method
    		return function(type, e, xhr){

                // Workaround to not choke on 204s		    
    		    if ((dojo.render.safari && 
                    !xhr.status) || (dojo.render.ie && 
                         evt.status == 1223)){
    		        
    		        xhr = {};
                    xhr.status = 204;
                    xhr.statusText = "No Content";
                    xhr.responseText = "";
                    
                    deferredRequestHandler.callback("", xhr);
                    
                } else {
        			deferredRequestHandler.errback(new Error(e.message), xhr);
                }
    		}
    	},
    
    	resultCallback: function(/* dojo.Deferred */ deferredRequestHandler){
    		// summary
    		// create callback that calls the Deferred's callback method
    		var tf = dojo.lang.hitch(this, 
    			function(type, obj, xhr){
    				if (obj["error"]!=null) {
    					var err = new Error(obj.error);
    					err.id = obj.id;
    					deferredRequestHandler.errback(err, xhr);
    				} else {
    					deferredRequestHandler.callback(obj, xhr); 
    				}
    			}
    		);
    		return tf;
    	}
    }
);
