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

cosmo.model.declareStamp("cosmo.model.EventStamp", "event", "http://osafoundation.org/eim/event/0",
    [ ["startDate", cosmo.datetime.Date, {}],
      ["duration", cosmo.model.Duration, {}],
      ["anyTime", Boolean, {"default":false}],
      ["allDay", Boolean, {"default":false}],
      ["atTime", Boolean, {"default":false}],
      ["location", String, {"default":""}],
      ["rrule", cosmo.model.RecurrenceRule, {"default":null}],
      ["exdates", [Array, cosmo.datetime.Date], {"default": cosmo.model.NEW_ARRAY}],
      ["status", String, {}]
    ],
    //mixins for master item stamps		 
    {
        _preserveEndDate: true,
        
        getPreserveEndDate: function(){
            //summary: if true, end dates will be preserved when setting start dates. Otherwise, 
            //         duration is preserved.
            return this._preserveEndDate;
        },
        
        setPreserveEndDate: function(preserveEndDate){
            this._preserveEndDate = preserveEndDate;
        },
        
        initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
        },

        getEndDate: function (){
            var duration = this.getDuration();
            if (duration == null || this.getStartDate() == null){
                return null;
            }
            var endDate = this.getStartDate().clone();
            endDate.addDuration(duration);
            if (this.getAnyTime() || this.getAllDay()){
                endDate.add(dojo.date.dateParts.DAY, -1);
            }
            return endDate;
        },

        setEndDate: function (/*CosmoDate*/ endDate){
            endDate = endDate.clone();
            if (this.getAnyTime() || this.getAllDay()){
                endDate.add(dojo.date.dateParts.DAY, +1);
            }
            var duration = new cosmo.model.Duration(this.getStartDate(), endDate);
            this.setDuration(duration);
        },
    
        setStartDate: function (/*cosmo.datetime.Date*/ newStartDate){
           var endDate = this.getEndDate();
           var oldDate = this.getStartDate();
           this.__setProperty("startDate", newStartDate);
           
           if (this.getPreserveEndDate() && endDate != null){
               this.setEndDate(endDate);    
           }

           //if this event stamp is attached to an item, and already has a previous start date
           //we may have some updating to do
           if (this.item && oldDate){
               var diff = dojo.date.diff(oldDate.toUTC(), newStartDate.toUTC(), dojo.date.dateParts.SECOND);
               
               //if there are modifications, we need to move the recurrenceid's for all of them
               if (!dojo.lang.isEmpty(this.item._modifications)){
               
                   //first copy the modifications into a new hash
                   var mods = this.item._modifications;
                   var oldMods = {};
                   dojo.lang.mixin(oldMods, mods);
                   for (var x in mods){
                       delete mods[x];
                   }
                   for (var x in oldMods){
                       var mod = oldMods[x];
                       var rId = mod.getRecurrenceId().clone();
                       rId.add(dojo.date.dateParts.SECOND, diff);
                       mod.setRecurrenceId(rId);
                       this.item.addModification(mod);
                   }
               }
               
               //also, if there are exdates, we need to move the recurrenceid's for all of them too
               if (this._exdates && this._exdates.length > 0){
                   var oldExdates = this._exdates;
                   var newExdates = [];
                   for (var x = 0; x < oldExdates.length; x++){
                       var exdate = oldExdates[x];
                       exdate.add(dojo.date.dateParts.SECOND, diff);
                       newExdates.push(exdate);                   
                   }
                   this._exates = newExdates;
               }
           }
           
        },
        
       applyChange: function(propertyName, changeValue, type){
            //this handles the case of setting the master start date or end date 
            // from an occurrence
            if ( (propertyName == "startDate" || propertyName =="endDate") 
                    && type == "master" 
                    && this.isOccurrenceStamp()){
                var getterAndSetter = cosmo.model.util.getGetterAndSetterName(propertyName);
                var getterName = getterAndSetter[0];
                var setterName = getterAndSetter[1];
                
                var diff =  dojo.date.diff(this[getterName]().toUTC(), 
                            changeValue.toUTC(), 
                            dojo.date.dateParts.SECOND);

                var masterDate = this.getMaster().getEventStamp()[getterName]();
                var newDate = masterDate.clone();
                newDate.add(dojo.date.dateParts.SECOND, diff);
                this.getMaster().getEventStamp()[setterName](newDate);
                if (propertyName == "startDate"){
                    this.item.recurrenceId.add(dojo.date.dateParts.SECOND,diff);
                }
                return;  
            }
            this._inherited("applyChange", arguments);
        }
    },
    //mixins for occurrence stamps
    {
        __noOverride:{rrule:1},
        _masterPropertyGetters: {
            startDate: function (){
               return this.recurrenceId;
            }
        },
        
        //we don't want to inherit from the one from the master....
        setStartDate: function (newStartDate){
           this.__setProperty("startDate", newStartDate);
        }
    }
);