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

/**
 * A recurrence rule specifies how to repeat a given event.
 */
dojo.declare("cosmo.model.RecurrenceRule", null, {
    
    initializer: function(freq, endDate, isSupported){
       this._frequency = freq;
       this._endDate = endDate;
       this._isSupported = isSupported;
    },
    /**
     * Specifies how often to repeat this event.
     * Must be one of the frequency constants above.
     */
     _frequency: null,
    /**
     * The date to repeat this event until.
     * This will only be a Date, not a DateTime -- should
     * NOT include time info
     */
    _endDate: null,

    _isSupported: true,
    /**
     * For events not created in Cosmo that have more complex rules than Cosmo
     * allows, a text representation of the rule appears here but is not editable.
     */
    isSupported(): function(){
        return _isSupported;   
    },

    toString: genericToString,
    
    FREQUENCY_DAILY: "daily",
    FREQUENCY_WEEKLY: "weekly",
    FREQUENCY_BIWEEKLY: "biweekly",
    FREQUENCY_MONTHLY: "monthly",
    FREQUENCY_YEARLY: "yearly",

    clone: function(rule) {
        var ret = null;
        var arr = [];
        if (rule) {
            ret = new RecurrenceRule();
            ret.frequency = rule.frequency;
            ret.endDate = rule.endDate;
            ret.isSupported = rule.isSupported;
        }
        return ret;
    },
    equals: function(other){
        //TODO
        dojo.unimplemented();
    }
});

