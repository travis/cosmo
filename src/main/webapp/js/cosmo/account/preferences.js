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

dojo.provide("cosmo.account.preferences");

dojo.require("cosmo.ui.cal_main");

cosmo.account.preferences = new function () {
	this.SHOW_ACCOUNT_BROWSER_LINK = 'UI.Show.AccountBrowserLink';
	this.LOGIN_URL = 'Login.Url';
	
    this.getPreference = function(key){
		return cosmo.ui.cal_main.Cal.serv.getPreference(key);
    };

    this.setPreference = function(key, val){
		cosmo.ui.cal_main.Cal.serv.setPreference(key, val);
        cosmo.topics.publish(cosmo.topics.PreferencesUpdatedMessage, [{key:val}])
    };

    this.removePreference = function(key){
		cosmo.ui.cal_main.Cal.serv.removePreferences(key);
    };
    
    this.getPreferences = function(){
		return cosmo.ui.cal_main.Cal.serv.getPreferences();
    };

    this.setPreferences = function(prefs){
    	cosmo.ui.cal_main.Cal.serv.setPreferences({"javaClass":"java.util.HashMap", "map":prefs});
        cosmo.topics.publish(cosmo.topics.PreferencesUpdatedMessage, [prefs])
    };

    this.setMultiplePreferences = function(prefs){
    	cosmo.ui.cal_main.Cal.serv.setMultiplePreferences({"javaClass":"java.util.HashMap", "map":prefs});
        cosmo.topics.publish(cosmo.topics.PreferencesUpdatedMessage, [prefs]);
    };
    
};
