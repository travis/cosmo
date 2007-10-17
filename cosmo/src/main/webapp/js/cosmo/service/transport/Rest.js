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
dojo.require("cosmo.ui.conf");

dojo.declare("cosmo.service.transport.Rest", null,
    {
        translator: null,

        initializer: function (translator){

        },
        
        methodIsSupported: {
            'get': true,
            'post': true,
            'head': true
        },
        
        METHOD_GET: "GET",
        METHOD_PUT: "PUT",
        METHOD_POST: "POST",
        METHOD_DELETE: "DELETE",
        METHOD_HEAD: "HEAD",
        
        /**
         * summary: Return request populated with attributes common to all CMP calls.
         */
        getDefaultRequest: function (/*dojo.Deferred*/deferred,
                                     /*Object*/ r,
                                     /*Object*/ kwArgs){
            kwArgs = kwArgs || {};
            if (r.url){
                if (!!r.url.match(/.*ticket=.*/)){
                    kwArgs.noAuth = true;
                }
            }
            // Add error fo transport layer problems
            
            var request = cosmo.util.auth.getAuthorizedRequest(r, kwArgs);
            
            if (deferred){
                if (!kwArgs.noErr){
                    deferred.addErrback(function(e) { dojo.debug("Transport Error: "); 
                                                      dojo.debug(e);
                                                      return e;});
                }
                request.load = request.load || this.resultCallback(deferred);
                request.error = request.error || this.errorCallback(deferred);
            }
            request.transport = request.transport || "XMLHTTPTransport";
            request.contentType = request.contentType || 'text/xml';
            request.sync = kwArgs.sync || r.sync || false;
            request.headers = request.headers || {};
            if (request.method){
                if (!this.methodIsSupported[request.method.toLowerCase()]){
                    request.headers['X-Http-Method-Override'] = request.method;
                    request.method = 'POST';
                }
            }

            return request
        },

        errorCallback: function(/* dojo.Deferred */ deferredRequestHandler){
            // summary
            // create callback that calls the Deferreds errback method
            return function(type, e, xhr){
                // Workaround to not choke on 204s
                if ((dojo.render.html.safari &&
                    !xhr.status) || (dojo.render.html.ie &&
                         xhr.status == 1223)){
                    xhr = {};
                    xhr.status = 204;
                    xhr.statusText = "No Content";
                    xhr.responseText = "";

                    deferredRequestHandler.callback("", xhr);

                } else {
                    // Turns out that when we get a 204 in IE it raises an error
                    // that causes Dojo to call this function with a fake
                    // 404 response (they return {status: 404}. Unfortunately, 
                    // the xhr still does return with a 1223, and the 
                    // Deferred's load methods get called twice, raising a fatal error.
                    // This works around this, but is very tightly coupled to the Dojo
                    // implementation. 
                    // TODO: find a better way to do this
                    if (deferredRequestHandler.fired == -1){
                        var err = new Error(e.message);
                        err.xhr = xhr;
                        deferredRequestHandler.errback(err);
                    }
                }
            }
        },
        
        resultCallback: function(/* dojo.Deferred */ deferredRequestHandler){
            // summary
            // create callback that calls the Deferred's callback method
            var tf = dojo.lang.hitch(this,
                function(type, obj, xhr){
                    if (obj && obj["error"] != null) {
                        var err = new Error(obj.error);
                        err.id = obj.id;
                        err.xhr = xhr;
                        deferredRequestHandler.errback(err);
                    } else {
                        obj = xhr.responseXML || obj;
                        deferredRequestHandler.callback(obj, xhr);
                    }
                }
            );
            return tf;
        },
        
        putText: function (text, url, kwArgs){
            var deferred = new dojo.Deferred();
            var r = this.getDefaultRequest(deferred, kwArgs);

            r.contentType = "application/atom+xml";
            r.url = url;

            r.postContent = text;
            r.method = "POST";
            r.headers['X-Http-Method-Override'] = "PUT";
    
            dojo.io.bind(r);
            return deferred;
            
        },
        
        postText: function (text, url, kwArgs){
            var deferred = new dojo.Deferred();
            var r = this.getDefaultRequest(deferred, kwArgs);

            r.contentType = "application/atom+xml";
            r.url = url;

            r.postContent = text;
            r.method = "POST";
    
            dojo.io.bind(r);
            return deferred;
            
        },
        
        queryHashToString: function(/*Object*/ queryHash){
            var queryList = [];
            for (var key in queryHash){
                queryList.push(key + "=" + encodeURIComponent(queryHash[key]));
            }
            if (queryList.length > 0){
                return "?" + queryList.join("&");
            }
            else return "";
        },
        
        bind: function (r, kwArgs) {
            kwArgs = kwArgs || {};
            var deferred = new dojo.Deferred();
            var request = this.getDefaultRequest(deferred, r, kwArgs);
            dojo.lang.mixin(request, r);
            this.addStandardErrorHandling(deferred, request.url, request.postContent, request.method);
            dojo.io.bind(request);
            return deferred;
        },
        
        addErrorCodeToExceptionErrback: function(deferred, responseCode, exception){
            deferred.addErrback(function (err){
                if (err.statusCode == responseCode){
                    err = new exception(err);
                }
                return err;
            });
        },

        addStandardErrorHandling: function (deferred, url, postContent, method){
            deferred.addErrback(function (err) {
                if (err.xhr.status == 404){
                    return new cosmo.service.exception.ResourceNotFoundException(url);
                }
                
                if (err.xhr.status >= 400 &&  err.xhr.status <= 599){
                    return new cosmo.service.exception.ServerSideError({
                        url: url, 
                        statusCode: err.xhr.status,
                        responseContent: err.xhr.responseText,
                        postContent: postContent,
                        method: method
                    });
                }
                return err;
            });
        }
        
    }
);
