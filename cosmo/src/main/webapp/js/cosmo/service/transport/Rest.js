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
                                     /*Object*/ kwArgs){
            
            // Add error for transport layer problems
            deferred.addErrback(function(e) { dojo.debug("Transport Error: "); 
                                              dojo.debug(e)
                                              return e;});
            var request = cosmo.util.auth.getAuthorizedRequest({addCredentials: !kwArgs.ticketKey});

            request.load = this.resultCallback(deferred);
            request.error = this.errorCallback(deferred);
            request.transport = "XMLHTTPTransport";
            request.contentType =  'text/xml';
            request.sync = kwArgs.sync;
            request.headers["Cache-Control"] = "no-cache";
            request.headers["Pragma"] = "no-cache";
            // Fight the dark powers of IE's evil caching mechanism
            //if (document.all) {
                request.preventCache = true;
            //}

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
    				    obj = xhr.responseXML || obj;
    				    if (dojo.render.html.ie) {
    				        var response = xhr.responseText;
    				        response = response.replace(/xmlns:xml.*=".*"/, "");
    				        obj = new ActiveXObject("Microsoft.XMLDOM");
                            if (!obj.loadXML(response)){
    		                   dojo.debug(obj.parseError.reason)
                            }
    				    }
    					deferredRequestHandler.callback(obj, xhr);
    				}
    			}
    		);
    		return tf;
    	},
    	
    	queryHashToString: function(/*Object*/ queryHash){
    	    var queryList = [];
    	    for (var key in queryHash){
                queryList.push(key + "=" + queryHash[key]);
    	    }
    	    if (queryList.length > 0){
    	        return "?" + queryList.join("&");
    	    }
    	    else return "";
    	}
    }
);
