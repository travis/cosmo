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

dojo.provide("cosmo.service.common");
dojo.require("cosmo.model.Item");

dojo.declare("cosmo.service.UrlCache", null, 
   {
       initializer: function(){
           this.urlCache = {};    
       },
       
       urlCache: null,
       
       setUrl: function(item, rel, url){
           this.urlCache[cosmo.service.getUid(item) + rel] = url;
       },
       
       getUrl: function(item, rel){
           return this.urlCache[cosmo.service.getUid(item) + rel];
       },
       
       setUrls: function(item, links){
           for (rel in links){
               this.setUrl(item, rel, links[rel]);
           }
       }
   }
);

cosmo.service.getUid = function (/*cosmo.model.Note*/ note){
        if (note instanceof cosmo.model.NoteOccurrence){
            var masterEvent = note.getMaster().getEventStamp();
            return note.getUid() + ":" + this.getRid(note.recurrenceId, 
                                             masterEvent.getAllDay(), masterEvent.getAnyTime());
        } else {
            return note.getUid();
        }
}

cosmo.service.getRid = function(/*cosmo.datetime.Date*/date, allDay, anyTime){
        var ridFormat = this.getDateFormatString(allDay, anyTime)
        if (date.isFloating()) {
            return date.strftime(ridFormat);
        } else {
            return  date.createDateForTimezone("utc").strftime(ridFormat + "Z");
        }
}

cosmo.service.getDateFormatString = function (allDay, anyTime){
        return (allDay || anyTime) ? "%Y%m%d": "%Y%m%dT%H%M%S";
}