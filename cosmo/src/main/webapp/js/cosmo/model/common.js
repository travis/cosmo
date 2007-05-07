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
dojo.provide("cosmo.model.common");

dojo.require("cosmo.model.util");

/**
 * A recurrence rule specifies how to repeat a given event.
 */
cosmo.model.declare("cosmo.model.RecurrenceRule", null, 
    [["frequency", {"default": null}],
     ["endDate", {"default": null} ],
     ["isSupported", {"default": true},
     ["unsupportedRule", {"default": null}] ]
    ], 
    {
    initializer: function(kwArgs){
        this.initializeProperties(kwArgs);
    },

    clone: function(rule) {
        //TODO
        dojo.unimplemented();
    },
    equals: function(other){
        //TODO
        dojo.unimplemented();
    }
    },
    {
    immutable: true
    });

dojo.lang.mixin(cosmo.model.RecurrenceRule, {
    FREQUENCY_DAILY: "daily",
    FREQUENCY_WEEKLY: "weekly",
    FREQUENCY_BIWEEKLY: "biweekly",
    FREQUENCY_MONTHLY: "monthly",
    FREQUENCY_YEARLY: "yearly"
})

