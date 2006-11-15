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

cosmo.datetime.timezone.Timezone.prototype.getTimezoneOffset = function(/*Date*/ date){
    //first get the right ZoneItem
    var zoneItem = this._getZoneItemForDate(date);
};

cosmo.datetime.timezone.Timezone.prototype._getZoneItemForDate = function(/*date*/ date){

};

cosmo.datetime.timezone.ZoneItem = function(){
    this.offsetInMinutes = null;
    this.ruleName = null;
    this.format = null;
    this.untilDate = null;
};

cosmo.datetime.timezone.ZoneItem.prototype.toString = function(/*boolean (optional)*/ showHeader){
    if (typeof(showHeader) == "undefined"){
        showHeader = true;
    }

    var s = "";
    if (showHeader){
        s += "Offset(Minutes)\tRule\tFormat\tUntilDate\n";
    }

    s += Math.round(this.offsetInMinutes) + "\t"
      + (this.ruleName || "") + "\t"
      + (this.format || "") + "\t";
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

cosmo.datetime.timezone.RuleSet.prototype.addRule = function(rule){
    this.rules.push(rule);
};

cosmo.datetime.timezone.Rule = function(){
    this.startYear = null;
    this.endYear = null;
    this.type = null;
    this.startMonth = null;
    this.startDate = null;
    this.startDate = null;
    this.startDay = null;;
    this.startOperator = null;
    this.startTime = null;
    this.addMinutes = null;
    this.letter = null;
}

cosmo.datetime.timezone.setTimezoneRegistry =  function(timezoneRegistry){
    cosmo.datetime.timezone._timezoneRegistry = timezoneRegistry;
}

cosmo.datetime.timezone.SimpleTimezoneRegistry = function(timezoneFileRoot){
    this.timezoneFileRoot = timezoneFileRoot || null;
    this._timezones = {};
    this._ruleSets = {};
};

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.init = function(files){
   dojo.lang.map(files, dojo.lang.hitch(this,this._parseUri));
};

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype._parseUri = function(uri){
    var content = dojo.hostenv.getText(this.timezoneFileRoot + "/" + uri);
    cosmo.datetime.timezone.parse(content, dojo.lang.hitch(this, this.addTimezone), dojo.lang.hitch(this, this.addRuleSet));
};

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.addTimezone = function(timezone){
    this._timezones[timezone.tzId] = timezone;
};

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype.addRuleSet = function(ruleset){
    this._ruleSets[ruleset.name] = ruleset;
};

cosmo.datetime.timezone.parse = function(str, timezoneCallback, rulesetCallback){
        var ruleSets = new dojo.collections.Dictionary();
        var zones = new dojo.collections.Dictionary();
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
                    // Shouldn't exist
                    /**TODO Handle This
                    if (self.zones[arr[1]]) {
                        alert('Error with Link ' + arr[1]);
                    }
                    self.zones[arr[1]] = arr[0];
                    **/
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
            until.hours = 23;
            until.minutes = 59;
            until.seconds = 59;
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
    if (dojo.lang.isNumber(rawOn)){
        rule.startDate = parseInt(rawOn);
    } else {
        if (dojo.string.startsWith("last")){
            rule.startDate = null;
            rule.startDay = this._DAY_MAP[rawOn.substr(4,3).toLowerCase()];
            rule.startOperator = this._RULE_OP_LAST;
        } else {
            rule.startOperator = (rawOn.indexOf(">") > -1) ? this._RULE_OP_GTR : this._RULE_OP_LESS;
            rule.startDay = this._DAY_MAP[rawOn.substr(0,3).toLowerCase()];
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


/*
cosmo.datetime.TimezoneParser.MONTH_MAP =
    var monthMap = { 'jan': 0, 'feb': 1, 'mar': 2, 'apr': 3,'may': 4, 'jun': 5,
        'jul': 6, 'aug': 7, 'sep': 8, 'oct': 9, 'nov': 10, 'dec': 11 };

cosmo.datetime.TimezoneParser.DAY_MAP =  {'sun': 0,'mon' :1, 'tue': 2, 'wed': 3, 'thu': 4, 'fri': 5, 'sat': 6 };



fleegix = {};
fleegix.tz = new function() {

    this.zoneAreas = { AFRICA: 'africa', ANTARCTICA: 'antarctica',
        ASIA: 'asia', AUSTRALASIA: 'australasia', BACKWARD: 'backward',
        ETCETERA: 'etcetera', EUROPE: 'europe', NORTHAMERICA: 'northamerica',
        PACIFICNEW: 'pacificnew', SOUTHAMERICA: 'southamerica',
        SYSTEMV: 'systemv' };

    var self = this;
    var monthMap = { 'jan': 0, 'feb': 1, 'mar': 2, 'apr': 3,'may': 4, 'jun': 5,
        'jul': 6, 'aug': 7, 'sep': 8, 'oct': 9, 'nov': 10, 'dec': 11 }
    var dayMap = {'sun': 0,'mon' :1, 'tue': 2, 'wed': 3, 'thu': 4, 'fri': 5, 'sat': 6 }
    var zoneKeys = {
        'Africa': this.zoneAreas.AFRICA,
        'Indian': this.zoneAreas.AFRICA,
        'Antarctica': this.zoneAreas.ANTARCTICA,
        'Asia': this.zoneAreas.ASIA,
        'Pacific': this.zoneAreas.AUSTRALASIA,
        'Australia': this.zoneAreas.AUSTRALASIA,
        'Etc': this.zoneAreas.ETCETERA,
        'EST': this.zoneAreas.NORTHAMERICA,
        'MST': this.zoneAreas.NORTHAMERICA,
        'HST':this.zoneAreas.NORTHAMERICA,
        'EST5EDT': this.zoneAreas.NORTHAMERICA,
        'CST6CDT': this.zoneAreas.NORTHAMERICA,
        'MST7MDT': this.zoneAreas.NORTHAMERICA,
        'PST8PDT': this.zoneAreas.NORTHAMERICA,
        'America': function() {
            var ret = [];
            if (!this.loadedZoneAreas[this.zoneAreas.NORTHAMERICA]) {
                ret.push(this.zoneAreas.NORTHAMERICA);
            }
            if (!this.loadedZoneAreas[this.zoneAreas.SOUTHAMERICA]) {
                ret.push(this.zoneAreas.SOUTHAMERICA);
            u}
            return ret;
        },
        'WET': this.zoneAreas.EUROPE,
        'CET': this.zoneAreas.EUROPE,
        'MET': this.zoneAreas.EUROPE,
        'EET': this.zoneAreas.EUROPE,
        'Europe': this.zoneAreas.EUROPE,
        'SystemV': this.zoneAreas.SYSTEMV
    };
    var zoneExceptions = {
        'Pacific/Honolulu': this.zoneAreas.NORTHAMERICA,
        'Pacific/Easter': this.zoneAreas.SOUTHAMERICA,
        'Pacific/Galapagos': this.zoneAreas.SOUTHAMERICA,
        'America/Danmarkshavn': this.zoneAreas.EUROPE,
        'America/Scoresbysund': this.zoneAreas.EUROPE,
        'America/Godthab': this.zoneAreas.EUROPE,
        'America/Thule': this.zoneAreas.EUROPE,
        'Indian/Kerguelen': this.zoneAreas.ANTARCTICA,
        'Indian/Chagos': this.zoneAreas.ASIA,
        'Indian/Maldives': this.zoneAreas.ASIA,
        'Indian/Christmas': this.zoneAreas.AUSTRALASIA,
        'Indian/Cocos': this.zoneAreas.AUSTRALASIA,
        'Europe/Nicosia': this.zoneAreas.ASIA,
        'Pacific/Easter': this.zoneAreas.SOUTHAMERICA,
        'Africa/Ceuta': this.zoneAreas.EUROPE,
        'Asia/Yekaterinburg': this.zoneAreas.EUROPE,
        'Asia/Omsk': this.zoneAreas.EUROPE,
        'Asia/Novosibirsk': this.zoneAreas.EUROPE,
        'Asia/Krasnoyarsk': this.zoneAreas.EUROPE,
        'Asia/Irkutsk': this.zoneAreas.EUROPE,
        'Asia/Yakutsk': this.zoneAreas.EUROPE,
        'Asia/Vladivostok': this.zoneAreas.EUROPE,
        'Asia/Sakhalin': this.zoneAreas.EUROPE,
        'Asia/Magadan': this.zoneAreas.EUROPE,
        'Asia/Kamchatka': this.zoneAreas.EUROPE,
        'Asia/Anadyr': this.zoneAreas.EUROPE,
        'Asia/Istanbul': this.zoneAreas.EUROPE
    };
    var loadedZoneAreas = {};

    function parseTimeString(str) {
        var pat = /(\d+)(?::0*(\d*))?(?::0*(\d*))?([wsugz])?$/;
        var hms = str.match(pat);
        hms[1] = parseInt(hms[1]);
        hms[2] = hms[2] ? parseInt(hms[2]) : 0;
        hms[3] = hms[3] ? parseInt(hms[3]) : 0;
        return hms;
    }
    function getZone(dt) {
        var timezone = dt.timezone;
        var zones = self.zones[timezone];
        while (typeof(zones) == "string") {
            timezone = zones;
            zones = self.zones[timezone];
            if (!zones) {
                alert('Cannot figure out the timezone ' + timezone);
            }
        }
        for(var i = 0; i < zones.length; i++) {
            var z = zones[i];
            if (!z[3]) { break; }
            var yea = parseInt(z[3]);
            var mon = 11;
            var dat = 31;
            if (z[4]) {
                mon = monthMap[z[4].substr(0, 3).toLowerCase()];
                dat = parseInt(z[5]);
            }
            var t = z[6] ? z[6] : '23:59:59';
            t = parseTimeString(t);
            var d = Date.UTC(yea, mon, dat, t[1], t[2], t[3]);
            if (dt.getTime() < d) { break; }
        }
        if (i == zones.length) {
           alert('No DST for ' + timezone);
        }
        // Get basic offset
        else {
            return zones[i];
        }

    }
    function getBasicOffset(z) {
        var off = parseTimeString(z[0]);
        var adj = z[0].indexOf('-') == 0 ? -1 : 1
        off = adj * (((off[1] * 60 + off[2]) *60 + off[3]) * 1000);
        return -off/60/1000;
    }
    function getRule(dt, str) {
        var currRule = null;
        var year = dt.getUTCFullYear();
        var rules = self.rules[str];
        var ruleHits = [];
        if (!rules) { return null; }
        for (var i = 0; i < rules.length; i++) {
            r = rules[i];
            if ((r[1] < (year - 1)) ||
                (r[0] < (year - 1) && r[1] == 'only') ||
                (r[0] > year)) {
                continue;
            };
            var mon = monthMap[r[3].substr(0, 3).toLowerCase()];
            var day = r[4];

            if (isNaN(day)) {
                if (day.substr(0, 4) == 'last') {
                    var day = dayMap[day.substr(4,3).toLowerCase()];
                    var t = parseTimeString(r[5]);
                    // Last day of the month at the desired time of day
                    var d = new Date(Date.UTC(dt.getUTCFullYear(), mon+1, 1, t[1]-24, t[2], t[3]));
                    var dtDay = d.getUTCDay();
                    var diff = (day > dtDay) ? (day - dtDay - 7) : (day - dtDay);
                    // Set it to the final day of the correct weekday that month
                    d.setUTCDate(d.getUTCDate() + diff);
                    if (dt < d) {
                        // If no match found, check the previous year if rule allows
                        if (r[0] < year) {
                            d.setUTCFullYear(d.getUTCFullYear()-1);
                            if (dt >= d) {
                                ruleHits.push({ 'rule': r, 'date': d });
                            }
                        }
                    }
                    else {
                        ruleHits.push({ 'rule': r, 'date': d });
                    }
                }
                else {
                    day = dayMap[day.substr(0, 3).toLowerCase()];
                    if (day != 'undefined') {
                        if(r[4].substr(3, 2) == '>=') {
                            var t = parseTimeString(r[5]);
                            // The stated date of the month
                            var d = new Date(Date.UTC(dt.getUTCFullYear(), mon,
                                parseInt(r[4].substr(5)), t[1], t[2], t[3]));
                            var dtDay = d.getUTCDay();
                            var diff = (day < dtDay) ? (day - dtDay + 7) : (day - dtDay);
                            // Set to the first correct weekday after the stated date
                            d.setUTCDate(d.getUTCDate() + diff);
                            if (dt < d) {
                                // If no match found, check the previous year if rule allows
                                if (r[0] < year) {
                                    d.setUTCFullYear(d.getUTCFullYear()-1);
                                    if (dt >= d) {
                                        ruleHits.push({ 'rule': r, 'date': d });
                                    }
                                }
                            }
                            else {
                                ruleHits.push({ 'rule': r, 'date': d });
                            }
                        }
                        else if (day.substr(3, 2) == '<=') {
                            var t = parseTimeString(r[5]);
                            // The stated date of the month
                            var d = new Date(Date.UTC(dt.getUTCFullYear(), mon,
                                parseInt(r[4].substr(5)), t[1], t[2], t[3]));
                            var dtDay = d.getUTCDay();
                            var diff = (day > dtDay) ? (day - dtDay - 7) : (day - dtDay);
                            // Set to first correct weekday before the stated date
                            d.setUTCDate(d.getUTCDate() + diff);
                            if (dt < d) {
                                // If no match found, check the previous year if rule allows
                                if (r[0] < year) {
                                    d.setUTCFullYear(d.getUTCFullYear()-1);
                                    if (dt >= d) {
                                        ruleHits.push({ 'rule': r, 'date': d });
                                    }
                                }
                            }
                            else {
                                ruleHits.push({ 'rule': r, 'date': d });
                            }
                        }
                    }
                }
            }
            else {
                var t = parseTimeString(r[5]);
                var d = new Date(Date.UTC(dt.getUTCFullYear(), mon, day, t[1], t[2], t[3]));
                d.setUTCHours(d.getUTCHours() - 24*((7 - day + d.getUTCDay()) % 7));
                if (dt < d) {
                    continue;
                }
                else {
                    ruleHits.push({ 'rule': r, 'date': d });
                }
            }
        }
        f = function(a, b) { return (a.date.getTime() >= b.date.getTime()) ?  1 : -1; }
        ruleHits.sort(f);
        currRule = ruleHits.pop().rule;
        return currRule;
    }
    function getAdjustedOffset(off, rule) {
        var save = rule[6];
        var t = parseTimeString(save);
        var adj = save.indexOf('-') == 0 ? -1 : 1;
        var ret = (adj*(((t[1] *60 + t[2]) * 60 + t[3]) * 1000));
        ret = ret/60/1000;
        ret -= off
        ret = -Math.ceil(ret);
        return ret;
    }

    this.files = ['africa', 'antarctica', 'asia', 'australasia',
        'backward', 'etcetera', 'europe',
        'northamerica', 'pacificnew', 'southamerica'];
    this.zones = {};
    this.rules = {};
    this.parseZones = function(str) {
        var s = '';
        var lines = str.split('\n');
        var arr = [];
        var chunk = '';
        var zone = null;
        var rule = null;
        for (var i = 0; i < lines.length; i++) {
            l = lines[i];
            if (l.match(/^\s/)) {
                l = "Zone " + zone + l;
            }
            l = l.split("#")[0];
            if (l.length > 3) {
                arr = l.split(/\s+/);
                chunk = arr.shift();
                switch(chunk) {
                    case 'Zone':
                        zone = arr.shift();
                        if (!self.zones[zone]) { self.zones[zone] = [] }
                        self.zones[zone].push(arr);
                        break;
                    case 'Rule':
                        rule = arr.shift();
                        if (!self.rules[rule]) { self.rules[rule] = [] }
                        self.rules[rule].push(arr);
                        break;
                    case 'Link':
                        // Shouldn't exist
                        if (self.zones[arr[1]]) { alert('Error with Link ' + arr[1]); }
                        self.zones[arr[1]] = arr[0];
                        break;
                    case 'Leap':
                        break;
                    default:
                        // Fail silently
                        break;
                }
            }
        }
        return true;
    };
    this.getOffset = function(dt) {
        var zone = getZone(dt);
        var rule = getRule(dt, zone[1]);
        var off = getBasicOffset(zone);
        if (rule) {
            off = getAdjustedOffset(off, rule);
        }
        return off;
    }
}

function print(str) {
  var printDiv = document.getElementById('printDiv');
  printDiv.innerHTML += str;
}

function println(str) {
  print(str + '<br/>');
}

function init() {
    var url = 'tz/' + 'northamerica.txt';
    var f = setup;
    fleegix.xhr.doGet(f, url);
}

function setup(str) {
    var s = '';
    s += '<div style="padding-bottom:8px;">';
    s += '<select id="tzName" name="tzName">';
    if (fleegix.tz.parseZones(str)) {
        for (var i in fleegix.tz.zones) {
            if (i.indexOf('America') > -1 || i.indexOf('Pacific') > -1) {
                s += '<option value="' + i + '">' + i + '</option>'
            }
        }
        s += '</select>';
        s += '</div>';
        $('selectElemDiv').innerHTML += s;
    }
}

window.onload = init;

function getTZOffset() {
    var form = $('tzForm');
    var str = form.inputDate.value;
    var pat = /^(\d{1,2})(\/|-)(\d{1,2})\2(\d{4})$/;
    var mat = str.match(pat);
    if (!mat) {
        alert('Please enter a date in a valid format.');
        return false;
    }
    var dt = new Date(str);
    var tz = form.tzName.value;
    dt = new Date(Date.UTC(dt.getFullYear(), dt.getMonth(), dt.getDate()));
    dt.timezone = tz;
    var off = fleegix.tz.getOffset(dt);
    println(off);
}*/
