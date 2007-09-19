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
dojo.require("cosmo.model.Item"); //TODO: remove this if possible
dojo.require("cosmo.datetime.serialize");

/**
 * A recurrence rule specifies how to repeat a given event.
 */
cosmo.model.declare("cosmo.model.RecurrenceRule", null, 
    [["frequency", {"default": null}],
     ["endDate", {"default": null} ],
     ["isSupported", {"default": true}],
     ["unsupportedRule", {"default": null}]
    ], 
    {
        initializer: function (kwArgs){
            this.initializeProperties(kwArgs);
        },
        
        isSupported: function (){
            return this.getIsSupported();  
        }
    },
    {
        immutable: true
    });

cosmo.model.RRULE_FREQUENCIES = {
    FREQUENCY_DAILY: "daily",
    FREQUENCY_WEEKLY: "weekly",
    FREQUENCY_BIWEEKLY: "biweekly",
    FREQUENCY_MONTHLY: "monthly",
    FREQUENCY_YEARLY: "yearly"
};

cosmo.model.ZERO_DURATION = "PT0S";
cosmo.model.declare("cosmo.model.Duration", null, 
    [["year",   {"default":0} ],
     ["month",  {"default":0} ],
     ["week",   {"default":0} ],
     ["day",    {"default":0} ],
     ["hour",   {"default":0} ],
     ["second", {"default":0} ],
     ["minute", {"default":0} ],
     ["multiplier", {"default":1}]
     ],
    {
        initializer:function(){
            //summary: create a new Duration using either the difference between two dates
            //         or kwArgs for the properties or a string with a iso8601 duration
            var kwArgs = null;
            if (arguments[0] instanceof cosmo.datetime.Date){
                var date1 = arguments[0];
                var date2 = arguments[1];
                kwArgs = cosmo.datetime.getDuration(date1,date2);
            } else if (typeof arguments[0] == "string"){
                kwArgs = cosmo.datetime.parseIso8601Duration(arguments[0]);
            } else {
                //arg[0] had better be an object!
                kwArgs = arguments[0];
            }
            
            this.initializeProperties(kwArgs);
            
        },
        toIso8601: function (){
            // This violates iso 8601, but if the desktop does it, we will for now
            var multiplier = this._multiplier;
            var durationString = [
                    multiplier < 0? "-" : "", 
                    "P",
                    this._year ? multiplier * this._year + "Y" : "",
                    this._month ? multiplier * this._month + "M" : "",
                    this._week ? multiplier * this._week + "W" : "",
                    this._day ? multiplier * this._day + "D" : "",
                    (this._hour || this._second || this._minute)? "T" : "",
                    this._hour ? multiplier * this._hour + "H" : "",
                    this._minute ? multiplier * this._minute + "M" : "",
                    this._second ? multiplier * this._second + "S" : ""].join("");
            if (durationString == "P") return cosmo.model.ZERO_DURATION;
            else return durationString;
                    
        },
        isZero: function(){
            //summary: returns true if this represents a duration of no time.
            if (this.getMultiplier() == 0){
                return true;
            }
            
            if (this.getYear() == 0 && this.getMonth() == 0 
             && this.getWeek() == 0 && this.getDay() == 0
             && this.getHour() == 0 && this.getMinute() == 0
             && this.getSecond() == 0 ){
                return true;
            }
            
            return false;
        }
    }, 
    {
        immutable: true
    });
    
cosmo.model.EventStatus = {
    CONFIRMED: "CONFIRMED",
    TENTATIVE: "TENTATIVE",
    FYI: "CANCELLED"
};
