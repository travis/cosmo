/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

dojo.provide("cosmo.datetime.serialize");

dojo.require("dojo.date.serialize");

cosmo.datetime.fromIso8601 = function(/*String*/formattedString){
	// summary: returns a Date object based on an ISO 8601 formatted string (uses date and time)
	return new cosmo.datetime.Date(dojo.date.setIso8601(new Date(0, 0), formattedString));
};

cosmo.datetime.fromIso8601Date = function(/*String*/formattedString){
	// summary: returns a Date object based on an ISO 8601 formatted string (date only)
	return new cosmo.datetime.Date(dojo.date.setIso8601Date(new Date(0, 0), formattedString));
};

cosmo.datetime.fromIso8601Time = function(/*String*/formattedString){
	// summary: returns a Date object based on an ISO 8601 formatted string (date only)
	return new cosmo.datetime.Date(dojo.date.setIso8601Time(new Date(0, 0), formattedString));
};

cosmo.datetime.fromRfc3339 = function(/*String*/rfcDate){
    return new cosmo.datetime.Date(dojo.date.fromRfc3339(rfcDate));
}

cosmo.datetime.addIso8601Duration = function (/*cosmo.datetime.date*/date, /*String*/duration){

    var r = "^P(?:(?:([0-9\.]*)Y)?(?:([0-9\.]*)M)?(?:([0-9\.]*)D)?(?:T(?:([0-9\.]*)H)?(?:([0-9\.]*)M)?(?:([0-9\.]*)S)?)?)(?:([0-9/.]*)W)?$"
    var dateArray = duration.match(r).slice(1);
    with(cosmo.datetime.dateParts){
        dateArray[0]? date.add(YEAR, dateArray[0]) : null;
        dateArray[1]? date.add(MONTH, dateArray[1]) : null;
        dateArray[2]? date.add(DAY, dateArray[2]) : null;
        dateArray[3]? date.add(HOUR, dateArray[3]) : null;
        dateArray[4]? date.add(MINUTE, dateArray[4]) : null;
        dateArray[5]? date.add(SECOND, dateArray[5]) : null;
    }

    return date;
}

cosmo.datetime.getIso8601Duration = function(dt1, dt2){

   return "P" +// + this.Date.diff('yyyy', dt1, dt2) + "Y" +
          //this.Date.diff('m', dt1, dt2) + "M" +
          //this.Date.diff('d', dt1, dt2) + "D" +
          'T' +
          //this.Date.diff('h', dt1, dt2) + "H" +
          //this.Date.diff('n', dt1, dt2) + "M" +
          this.Date.diff('s', dt1, dt2) + "S";
}
