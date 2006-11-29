/*
 * Copyright 2006 Open Source Applications Foundation
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

dojo.provide("cosmo.datetime.timezone");
dojo.require("dojo.collections.Dictionary");

cosmo.datetime.HOURS_IN_DAY = 24;
cosmo.datetime.MINUTES_IN_HOUR = 60;
cosmo.datetime.SECONDS_IN_MINUTE = 60;

cosmo.datetime.timezone.REGIONS = ["Africa", "America", "Asia", "Antarctica", "Atlantic", "Australia", "Europe", "Indian", "Pacific" ];

cosmo.datetime.timezone._MONTH_MAP = { 'jan': 0, 'feb': 1, 'mar': 2, 'apr': 3,'may': 4, 'jun': 5,
                                       'jul': 6, 'aug': 7, 'sep': 8, 'oct': 9, 'nov': 10, 'dec': 11 };

cosmo.datetime.timezone._DAY_MAP =  {'sun': 0,'mon' :1, 'tue': 2, 'wed': 3, 'thu': 4, 'fri': 5, 'sat': 6 };
cosmo.datetime.timezone._RULE_OP_LAST = "LAST";
cosmo.datetime.timezone._RULE_OP_GTR = "GTR";
cosmo.datetime.timezone._RULE_OP_LESS = "LSS";

cosmo.datetime.timezone.Timezone = function(tzId, zoneItems){
    this.tzId = tzId;
    this.zoneItems = zoneItems || [];
};

cosmo.datetime.timezone.Timezone.prototype.toString = function(){
    var s = "Timezone: " + this.tzId + "\n";
    for (var x = 0; x < this.zoneItems.length; x++){
        s +=  this.zoneItems[x].toString(x == 0);
    }
    return s;
};

cosmo.datetime.timezone.Timezone.prototype.addZoneItem = function(zoneItem){
    this.zoneItems.push(zoneItem);
};

cosmo.datetime.timezone.Timezone.prototype.getOffsetInMinutes = function(/*Date*/ date){
    //first get the right ZoneItem
    var zoneItem = this._getZoneItemForDate(date);
    var originalOffset = zoneItem.offsetInMinutes;

    //if there is no rulename, just return the timezone's normal offset
    if (!zoneItem.ruleName){
        return originalOffset;
    }

    //now find the right rule for this date.
    var ruleSet = cosmo.datetime.timezone.getRuleSet(zoneItem.ruleName);
    var rule = ruleSet._getRuleForDate(date);

    if (rule == null){
        return zoneItem.offsetInMinutes;
    } else {
        return zoneItem.offsetInMinutes + rule.addMinutes;
    }
};

cosmo.datetime.timezone.Timezone.prototype.getAbbreviatedName = function(/*Date*/ date){
    var zoneItem = this._getZoneItemForDate(date);
    if (!zoneItem.ruleName){
        return zoneItem.format;
    }

    var ruleSet = cosmo.datetime.timezone.getRuleSet(zoneItem.ruleName);
    var rule = ruleSet._getRuleForDate(date);

    if (!rule || !rule.letter){
        return zoneItem.format;
    }

    return zoneItem.format.replace("%s", rule.letter);

}

cosmo.datetime.timezone.Timezone.prototype._getZoneItemForDate = function(/*Date*/ date){
    var compareDates = cosmo.datetime.timezone._compareDates;
    for (var x = 0; x < this.zoneItems.length; x++){
        var zoneItem = this.zoneItems[x];
        var untilDate = zoneItem.untilDate;
        if (!untilDate){
            return zoneItem;
        }

        if (compareDates(date, untilDate) <= 0){
            return zoneItem;
        }
    }
};

cosmo.datetime.timezone._compareDates = function(d1, d2){
    var getDateField = cosmo.datetime.timezone._getDateField;
    var fields = ["year", "month", "date", "hours", "minutes", "seconds"];

    for (var x = 0; x < fields.length; x++){
        var diff = getDateField(d1, fields[x]) - getDateField(d2, fields[x]);
        if (diff != 0) {
            return diff;
        }
    }

    //fell through, so they must be equal
    return 0;
};

cosmo.datetime.timezone._getLastDayForMonthAndYear = function(day, month, year){
    var lastDayOfMonth = new Date(year, month + 1,1, -24);
    var lastDayDay = lastDayOfMonth.getDay();
    var diff = (day > lastDayDay) ? (day - lastDayDay - 7) : (day - lastDayDay);
    return lastDayOfMonth.getDate() + diff;
};

cosmo.datetime.timezone._getDayGreaterThanNForMonthAndYear = function(n, day, month, year){
    var startingDate = new Date(year, month, n);
    var date = startingDate.getDate();
    var startingDay = startingDate.getDay();
    date += (day >= startingDay) ? day - startingDay : (day + 7) - startingDay;
    return date;
};

cosmo.datetime.timezone._getDayLessThanNForMonthAndYear = function(n, day, month, year){
    var startingDate = new Date(year, month, n);
    var date = startingDate.getDate();
    var startingDay = startingDate.getDay();
    date += (day <= startingDay) ? day - startingDay : (day - 7) - startingDay;
    return date;
};

cosmo.datetime.timezone._getDateField = function(date, field){
    if (field == "year"){
        return date.getFullYear ? date.getFullYear() : date.year;
    }
    if (field == "month"){
        var ret = date.getMonth ? date.getMonth() : date.month;
        return typeof(ret) == "undefined" ? 0 : ret;
    }
    if (field == "date"){
        var ret = date.getDate ? date.getDate() : date.date;
        return typeof(ret) == "undefined" ? 1 : ret;
    }
    if (field == "hours"){
        var ret = date.getHours ? date.getHours() : date.hours;
        return typeof(ret) == "undefined" ? 0 : ret;
    }
    if (field == "minutes"){
        var ret = date.getMinutes ? date.getMinutes() : date.minutes;
        return typeof(ret) == "undefined" ? 0 : ret;
    }
    if (field == "seconds"){
        var ret = date.getSeconds ? date.getSeconds() : date.seconds;
        return typeof(ret) == "undefined" ? 0 : ret;
    }
}

cosmo.datetime.timezone.ZoneItem = function(){
    this.offsetInMinutes = null;
    this.ruleName = null;
    this.format = null;
    this.untilDate = null;
};

cosmo.datetime.timezone._hasOrElse = function(obj, prop, orElse){
   return dojo.lang.has(obj, prop) && obj[prop] != null ? obj[prop] : orElse;
}

cosmo.datetime.timezone.ZoneItem.prototype.toString = function(/*boolean (optional)*/ showHeader){
    var hasOrElse = cosmo.datetime.timezone._hasOrElse;
    if (typeof(showHeader) == "undefined"){
        showHeader = true;
    }

    var s = "";
    if (showHeader){
        s += "Offset(Minutes)\tRule\tFormat\tUntilDate\n";
    }

    s += Math.round(this.offsetInMinutes) + "\t"
      + (hasOrElse(this,"ruleName", "-")) + "\t"
      + (hasOrElse(this,"format", "-")) + "\t";
      if (this.untilDate){
        s += this.untilDate.year + "/"
        + (this.untilDate.month + 1) + "/"
        + this.untilDate.date + " "
        + this.untilDate.hours + "h "
        + this.untilDate.minutes +"m "
        + this.untilDate.seconds + "s \n";
      }
    return s;
}

cosmo.datetime.timezone.RuleSet = function(name, rules){
    this.name = name;
    this.rules = rules || [];

};

cosmo.datetime.timezone.RuleSet.prototype.toString = function(){
    var s = "Rule: " + this.name+ "\n";
    for (var x = 0; x < this.rules.length; x++){
        s +=  this.rules[x].toString(x == 0);
    }
    return s;
};

cosmo.datetime.timezone.RuleSet.prototype.addRule = function(rule){
    this.rules.push(rule);
};

cosmo.datetime.timezone.RuleSet.prototype._getRulesForYear = function(year){
     var result = [];
     var rules = this.rules;
     for (var x = 0; x < this.rules.length;x++){
         var rule = rules[x];
         if ( (year >= rule.startYear) && (year <= rule.endYear) ){
             result.push(rule);
         }
     }
     return result;
};

cosmo.datetime.timezone.RuleSet.prototype._getRuleForDate = function(date){
    var rules = this._getRulesForYear(date.getFullYear());
    if (!rules || !rules.length){
        return null;
    }

    rules.sort(function(a,b){return a.startMonth - b.startMonth});

    var lastApplicableRule = null;
    if (rules.length > 1){
        lastApplicableRule = rules[rules.length - 1];
    }

    for (var x = 0; x < rules.length; x++){
        var rule = rules[x];
        if (rule._applicable(date)){
            lastApplicableRule = rule;
        }
    }
    return lastApplicableRule;
}

cosmo.datetime.timezone.Rule = function(){
    this.startYear = null;
    this.endYear = null;
    this.type = null;
    this.startMonth = null;
    this.startDate = null;
    this.startDay = null;;
    this.startOperator = null;
    this.startTime = null;
    this.addMinutes = null;
    this.letter = null;
}

cosmo.datetime.timezone.Rule.prototype.toString = function(/*boolean (optional)*/ showHeader){
    var hasOrElse = cosmo.datetime.timezone._hasOrElse

    if (typeof(showHeader) == "undefined"){
        showHeader = true;
    }

    var s = "";
    if (showHeader){
        s += "sYr\tenYr\tTp\tsMth\tsDte\tsDay\tsOp\tsTime\tAddMin\tLetter\n";
    }

    s += this.startYear + "\t"
      +  (hasOrElse(this, "endYear", "-")) + "\t"
      +  (hasOrElse(this, "type", "-")) + "\t"
      +  (hasOrElse(this, "startMonth", "-")) + "\t"
      +  (hasOrElse(this, "startDate", "-")) + "\t"
      +  (hasOrElse(this, "startDay", "-")) + "\t"
      +  (hasOrElse(this, "startOperator", "-")) + "\t";

    if (this.startTime){
      s += this.startTime.hours + "h "
        +  this.startTime.minutes + "m "
        +  this.startTime.seconds + "s" + "\t";
    }

    s += (hasOrElse(this, "addMinutes") ? Math.round(this.addMinutes): "-") + "\t";
    s += (hasOrElse(this, "letter", "-"));
    return s + "\n";
}

cosmo.datetime.timezone.Rule.prototype._applicable = function(date){
    var ruleStartDate  = this._getStartDateForYear(date.getFullYear());
    return cosmo.datetime.timezone._compareDates(date, ruleStartDate) >= 0 ;
}

cosmo.datetime.timezone.Rule.prototype._getStartDateForYear = function(year){
    var startDate = { year: year,
                      month: this.startMonth,
                      hours: this.startTime.hours,
                      minutes: this.startTime.minutes,
                      seconds: this.startTime.seconds };

    if (this.startDay != null){
        if (this.startOperator == cosmo.datetime.timezone._RULE_OP_LAST){
            var date = cosmo.datetime.timezone._getLastDayForMonthAndYear(this.startDay, this.startMonth, year);
            startDate.date = date;
        } else if (this.startOperator == cosmo.datetime.timezone._RULE_OP_GTR){
            var date = cosmo.datetime.timezone._getDayGreaterThanNForMonthAndYear(this.startDate, this.startDay, this.startMonth, year);
            startDate.date = date;
        } else {
            var date = cosmo.datetime.timezone._getDayLessThanNForMonthAndYear(this.startDate, this.startDay, this.startMonth, year);
            startDate.date = date;
        }
    } else {
        startDate.date = this.startDate;
    }

    return startDate;
}

cosmo.datetime.timezone.setTimezoneRegistry =  function(timezoneRegistry){
    cosmo.datetime.timezone._timezoneRegistry = timezoneRegistry;
}

cosmo.datetime.timezone.getTimezone = function(tzId){
    return this._timezoneRegistry.getTimezone(tzId);
}

cosmo.datetime.timezone.getRuleSet = function(ruleName){
    return this._timezoneRegistry.getRuleSet(ruleName);
}

cosmo.datetime.timezone.getTzIdsForRegion = function(region){
    return this._timezoneRegistry.getTzIdsForRegion(region);
}

cosmo.datetime.timezone.SimpleTimezoneRegistry = function(timezoneFileRoot){
    this.timezoneFileRoot = timezoneFileRoot || null;
    this._timezones = {};
    this._ruleSets = {};
    this._links = {};
    this._tzsByRegion = null;
};

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.init = function(files){
   dojo.lang.map(files, dojo.lang.hitch(this,this._parseUri));
};

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.addTimezone = function(timezone){
    this._timezones[timezone.tzId] = timezone;
};

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.addRuleSet = function(ruleset){
    this._ruleSets[ruleset.name] = ruleset;
};

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.addLink = function(oldName, newName){
    this._links[oldName] = newName;
}

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.getTimezone = function(tzid){
    return tz = this._timezones[tzid] || this._timezones[this._links[tzid]];
}

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.getRuleSet = function(ruleName){
    return this._ruleSets[ruleName];
}

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.getTzIdsForRegion = function(region){
    if (!this._tzsByRegion){
       this._initTzsByRegion();
    }

    return this._tzsByRegion[region];
}

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype._initTzsByRegion = function(){
    var tzsByRegion = {};
    var regions = cosmo.datetime.timezone.REGIONS;
    for (var x = 0; x < regions.length; x++ ){
        var region = regions[x];
        tzsByRegion[region] = [];
    }

    for (var tz in this._timezones){
        var region = tz.split("/")[0];
        var regionArray = tzsByRegion[region];
        if (regionArray){
            regionArray.push(tz);
        }
    }

    this._tzsByRegion = tzsByRegion;
}

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype._parseUri = function(uri){
    var content = dojo.hostenv.getText(this.timezoneFileRoot + "/" + uri);
    cosmo.datetime.timezone.parse(content, dojo.lang.hitch(this, this.addTimezone), dojo.lang.hitch(this, this.addRuleSet), dojo.lang.hitch(this, this.addLink));
};

cosmo.datetime.timezone.parse = function(str, timezoneCallback, rulesetCallback, linkCallback){
        var ruleSets = new dojo.collections.Dictionary();
        var zones = new dojo.collections.Dictionary();
        var links = {};
        var lines = str.split('\n');

        //the current zone
        var zone = null;

        for (var i = 0; i < lines.length; i++) {
            var line = lines[i];

            //Skip comments
            if (dojo.string.startsWith(dojo.string.trim(line), "#") || line.length < 3){
                continue;
            }

            if (line.match(/^\s/)) {
                //we do this because zone lines after the first don't have a zone in it.
                line = "Zone " + zone.tzId + line;
            }

            //in case there are any inline comments
            line = line.split("#")[0];

            //split on whitespace
            var arr = line.split(/\s+/);

            //is this a zone, a rule, a link or what?
            var lineType = arr.shift();
            switch(lineType) {
                case 'Zone':
                    var tzId = arr.shift();
                    var zoneItem = this._parseZoneLine(arr);
                    if (!zones.containsKey(tzId)) {
                        zone =  new cosmo.datetime.timezone.Timezone(tzId);
                        zones.add(tzId, zone);
                    } else {
                        zone = zones.item(tzId);
                    }
                    zone.addZoneItem(zoneItem);
                    break;
                case 'Rule':
                    var ruleName = arr.shift();
                    var rule = this._parseRuleLine(arr);
                    if (!ruleSets.containsKey(ruleName)) {
                        ruleSet = new cosmo.datetime.timezone.RuleSet(ruleName);
                        ruleSets.add(ruleName, ruleSet);
                    } else {
                        ruleSet = ruleSets.item(ruleName);
                    }
                    ruleSet.addRule(rule);
                    break;
                case 'Link':
                    var newName = arr.shift();
                    var oldName = arr.shift();
                    links[oldName] = newName;
                    break;
                case 'Leap':
                    break;
                default:
                    // Fail silently
                    break;
            }//end switch
        }//end for
        dojo.lang.map(zones.getValueList(),timezoneCallback);
        dojo.lang.map(ruleSets.getValueList(), rulesetCallback);
        for (var oldName in links){
            linkCallback(oldName, links[oldName])
        }
        return true;
    };//end function

cosmo.datetime.timezone._arrayPrinter = function(starter, arrayToPrint){
    var stringy = starter;
    for (var index = 0; index < arrayToPrint.length; index++){
        stringy += index + "->'" + arrayToPrint[index] + "'    ";
    }
};

cosmo.datetime.timezone._parseZoneLine = function(array){
    var zoneItem = new cosmo.datetime.timezone.ZoneItem();

    //The Format: zoneLine --> 0->'-10:30' 1->'-' 2->'HST' 3->'1933' 4->'Apr' 5->'30' 6->'2:00'
    //                    GMTOFF      RULE   FORMAT   UNTIL-YR  MONTH    DAY     TIME

    //first set the offset:
    zoneItem.offsetInMinutes = this._parseTimeIntoMinutes(array[0]);

    //set the rule name
    zoneItem.ruleName = array[1];

    //set the format.
    zoneItem.format = array[2];

    if (!array[3]){
        zoneItem.untilDate = null;
    } else {
        var until = {};
        until.year = array[3];
        if (array[4]){
           until.month = this._MONTH_MAP[array[4].substr(0,3).toLowerCase()];
           until.date = parseInt(array[5]);
        } else {
           until.month = 0;
           until.date = 1;
        }

        if (!array[6]){
            until.hours = 0;
            until.minutes = 0;
            until.seconds = 0;
        } else {
            var hms = this._parseTimeString(array[6]);
            until.hours = hms.hours;
            until.minutes = hms.minutes;
            until.seconds = hms.seconds;
        }
        zoneItem.untilDate = until;
    }
    return zoneItem;
};

cosmo.datetime.timezone._parseRuleLine = function(array){
    var rule = new cosmo.datetime.timezone.Rule();

    //The Format: DEBUG: Rule --> 0->'1942' 1->'only' 2->'-' 3->'Feb' 4->'9' 5->'2:00' 6->'1:00' 7->'W' 8->''
    //                            FROM      TO        TYPE   IN       ON     AT        SAVE      LETTER

    //set the start year
    rule.startYear = parseInt(array[0]) || (dojo.string.startsWith(array[0], "min") ? -99999 : 99999);

    //set the end Year
    rule.endYear = parseInt(array[1])
        || (dojo.string.startsWith(array[1], "o") ? rule.startYear : (dojo.string.startsWith(array[1], "min") ? -99999 : 99999));

    //set the type
    rule.type = array[2];

    //set the startmonth
    rule.startMonth = this._MONTH_MAP[array[3].substr(0,3).toLowerCase()];

    //set the start date
    var rawOn = array[4]; //the raw "ON" data
    var parsedOn = parseInt(rawOn);
    if (!isNaN(parsedOn)){
        rule.startDate = parseInt(rawOn);
    } else {
        if (dojo.string.startsWith(rawOn, "last")){
            rule.startDate = null;
            rule.startDay = this._DAY_MAP[rawOn.substr(4,3).toLowerCase()];
            rule.startOperator = this._RULE_OP_LAST;
        } else {
            rule.startOperator = (rawOn.indexOf(">") > -1) ? this._RULE_OP_GTR : (rawOn.indexOf("<") > -1 ? this._RULE_OP_LESS :null);
            rule.startDay = this._DAY_MAP[rawOn.substr(0,3).toLowerCase()];
            var parsedDate = parseInt(rawOn.substr(5, (rawOn.length-5)));
            rule.startDate = parsedDate;
        }
    }

    //set the startTime
    //TODO get that extra letter for wallclock, standard, etc.
    rule.startTime = this._parseTimeString(array[5]);

    //the amount of mintues to add
    rule.addMinutes = this._parseTimeIntoMinutes(array[6]);

    //the letter
    rule.letter = array[7];

    //TODO set properties...
    return rule;
};

cosmo.datetime.timezone._parseTimeString = function(str) {
    var pat = /(\d+)(?::0*(\d*))?(?::0*(\d*))?([wsugz])?$/;
    var matchArray =  str.match(pat);
    var result = {};
    result.hours = parseInt(matchArray[1]);
    result.minutes = matchArray[2] ? parseInt(matchArray[2]) : 0;
    result.seconds = matchArray[3] ? parseInt(matchArray[3]) : 0;
    result.negative = dojo.string.startsWith(str, "-");
    return result;
};

 cosmo.datetime.timezone._parseTimeIntoMinutes = function(str){
    var hms = this._parseTimeString(str);
    var millis = ((((hms.hours * cosmo.datetime.MINUTES_IN_HOUR) + hms.minutes) * cosmo.datetime.SECONDS_IN_MINUTE) + hms.seconds) * 1000;
    var minutes = millis/1000/cosmo.datetime.SECONDS_IN_MINUTE;
    minutes = minutes * (hms.negative ? -1 : 1);
    return minutes;
 };