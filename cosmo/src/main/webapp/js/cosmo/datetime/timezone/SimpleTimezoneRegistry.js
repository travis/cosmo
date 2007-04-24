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

dojo.provide("cosmo.datetime.timezone.SimpleTimezoneRegistry");

dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.timezone");

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

    return this._tzsByRegion[region] || [];
}

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype._addTzToTzsByRegion = function(tzId){
        var region = tzId.split("/")[0];
        var regionArray = this._tzsByRegion[region];
        if (regionArray){
            regionArray.push(tzId);
        }
}

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype._initTzsByRegion = function(){
    var tzsByRegion = {};
    this._tzsByRegion = tzsByRegion;

    var regions = cosmo.datetime.timezone.REGIONS;
    for (var x = 0; x < regions.length; x++ ){
        var region = regions[x];
        tzsByRegion[region] = [];
    }

    for (var tz in this._timezones){
        this._addTzToTzsByRegion(tz);
    }
}

cosmo.datetime.timezone.SimpleTimezoneRegistry.prototype._parseUri = function(uri){
    var content = dojo.hostenv.getText(this.timezoneFileRoot + "/" + uri);
    cosmo.datetime.timezone.parse(content, dojo.lang.hitch(this, this.addTimezone), dojo.lang.hitch(this, this.addRuleSet), dojo.lang.hitch(this, this.addLink));
};
