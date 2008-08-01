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

/**
 * summary:
 *   This module provides convenience functions for working with dojo.Deferred objects.
 * description:
 *   This module provides various convenience functions for working
 *   with dojo.Deferred objects, mostly related to adding error handling callbacks
 *   and extracting errors.
 */


dojo.provide("cosmo.util.deferred");
dojo.require("cosmo.app");

cosmo.util.deferred = {
    getFiredDeferred: function (result){
        // summary:
        //   Return a pre-fired deferred.
        // description:
        //   Return a pre-fired deferred that returns <code>result</code>.
        //   This can be useful when building apis that may or may not return
        //   syncronous results.
        // returns:
        //   dojo.Deferred object fired with <code>result</code>
        var d = new dojo.Deferred();
        d.callback(result);
        return d;
    },

    addStdErrback: function (deferred, primaryMessage, secondaryMessage){
        // summary:
        //   Add a generic errback.
        // description:
        //   Add an errback that will simply pop up a modal dialog containing
        //   the error message. Not particularly user friendly and should be avoided.
        // returns:
        //   null
        deferred.addErrback(function (e){
            cosmo.app.showErr(primaryMessage || _("Error.DeferredPrimary"),
                              secondaryMessage || _("Error.DeferredSecondary"), e);
        });
    },

    addStdDLCallback: function(deferredList, primaryMessage, secondaryMessage){
        // summary:
        //   Add callback to deferredList to display error messages from component deferreds.
        // description:
        //   Add callback to deferredList to display error messages from component deferreds.
        //   Especially useful because deferredLists swallow error messages.
        // returns:
        //   null
        deferredList.addCallback(function (dResultList){
            for (var i = 0; i < dResultList.length; i++){
                var result = dResultList[i];
                if (!result[0]){
                    cosmo.app.showErr(primaryMessage || _("Error.DeferredPrimary"),
                              secondaryMessage || _("Error.DeferredSecondary"), result[1]);
                }
            }
            return dResultList;
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

