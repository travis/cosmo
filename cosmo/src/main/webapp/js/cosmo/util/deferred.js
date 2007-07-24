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

dojo.provide("cosmo.util.deferred");

cosmo.util.deferred = {
    //FIXME: don't think we actually use this    
    getFiredDeferred: function (result){
        var d = new dojo.Deferred();
        d.callback(result);
        return d;
    },
    
    //FIXME: don't think we actually use this    
    addStdErrback: function (deferred, msg){
        deferred.addErrback(function (e){
            dojo.debug(msg || _("Error.Deferred"));
            dojo.debug(e);
        });
    },
    
    getFirstError: function(deferredListResults){
        //summary: given the results list from a dojo.DeferredList callback,
        //         returns the first error in the result set it encounters, if any.
        for (var x = 0; x < deferredListResults.length; x++ ){
            if (!deferredListResults[x][0]){
                return deferredListResults[x][1];
            }  
        }
        return null;
    }    
}

