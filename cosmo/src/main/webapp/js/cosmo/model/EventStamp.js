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

dojo.provide("cosmo.model.EventStamp");
dojo.require("cosmo.model.util");
dojo.require("cosmo.model.Item");

cosmo.model.declareStamp("cosmo.model.EventStamp", "event",
    [ ["startDate", cosmo.datetime.Date, {}],
      ["duration", Number, {}],
      ["anytime", Boolean, {}],
      ["location", String, {}],
      ["rrule", cosmo.model.RecurrenceRule, {}],
      ["exdates", [Array, cosmo.datetime.Date], {}],
      ["status", String, {}],
    ],
    //mixins for master item stamps		 
    {
        initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
        },
	
	getEndDate: function getEndDate(){
            var duration = this.getDuration();
	    var endDate = this.getStartDate().clone();
	    endDate.add(dojo.date.dateParts.SECOND, duration);
	    return endDate;
	},

	setEndDate: function setEndDate(/*CosmoDate*/ endDate){
	    var duration = dojo.date.diff(this.getStartDate().toUTC(), endDate.toUTC(), 
		dojo.date.dateParts.SECOND);
	    this.setDuration(duration);
	}
    },
    //mixins for occurrence stamps
    {

	_masterPropertyGetters: {
	    startDate: function eventStampOcurrenceStartDateMasterPropertyGetter(){
		return this.recurrenceId;
	    }
	}

/*	getStartDate:  function occurrenceGetStartDate(){
	    var modificationProperty = this._getModifiedProperty("startDate");
	    if (modificationProperty){
		return modificationProperty;
	    }
	    return this.recurrenceId;
	}*/
	
    }
);