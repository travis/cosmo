/*
 * Copyright 2007 Open Source Applications Foundation
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
dojo.provide("cosmo.service.tickler");

cosmo.service.tickler.wrapService = function(service){
    var ctr = function(){};
    ctr.prototype = service;
    var tickledService = new ctr();
    
    this._wrapMethod(service, tickledService, "getItems");
    this._wrapMethod(service, tickledService, "getDashboardItems");

    return tickledService;   
}

cosmo.service.tickler._wrapMethod = function(service, wrappedService, methodName){
    wrappedService[methodName] = function(){
        var deferred = service[methodName].apply(service, arguments);
        innerDeferred = null;
        deferred.addCallback(function(items){
            for (var x = 0; x < items.length; x++){
                var item = items[x];
                if (item.tickle()){
                    if (innerDeferred == null){
                        innerDeferred = service.saveItem(item);
                    } else {
                        addCallback = function(){
                            var closedOverItem = item;
                            innerDeferred.addCallback(function(x){
                                return service.saveItem(closedOverItem);
                            });
                        }
                        addCallback();
                    }
                } 
            }
            return items;
        });
        
        return deferred;    
    }
}
 