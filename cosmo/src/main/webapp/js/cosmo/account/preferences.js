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

dojo.require("cosmo.app.pim");

cosmo.account.preferences = new function () {
	this.SHOW_ACCOUNT_BROWSER_LINK = 'UI.Show.AccountBrowserLink';
	this.LOGIN_URL = 'Login.Url';
	
    this.getPreference = function(key){
		return cosmo.app.pim.serv.getPreference(key);
    };

    this.setPreference = function(key, val){
		cosmo.app.pim.serv.setPreference(key, val);
		var preferences = {};
		preferences[key] = val;
        cosmo.topics.publish(cosmo.topics.PreferencesUpdatedMessage, [preferences])
    };

    this.deletePreference = function(key){
		cosmo.app.pim.serv.deletePreferences(key);
    };
    
    this.getPreferences = function(){
		return cosmo.app.pim.serv.getPreferences();
    };

};
