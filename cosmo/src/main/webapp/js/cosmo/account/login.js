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

dojo.provide('cosmo.account.login');

dojo.require("dojo.io.*");
dojo.require("cosmo.env");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.convenience");

cosmo.account.login.doLogin = function(un, pw, handlers){
    var postData = "j_username=" + encodeURIComponent(un) + 
        "&j_password=" + encodeURIComponent(pw);

    var _authSuccessCB = function(type, data, obj){
        if (data == cosmo.env.getBaseUrl() + "/loginfailed"){
            handlers.error(type, _('Login.Error.AuthFailed'), obj);
        } else {
            cosmo.util.auth.setCred(un, pw);
            handlers.load(type, data, obj);
        }
    }
    dojo.io.bind({
        url: cosmo.env.getFullUrl("Auth"),
        method: 'POST',
        postContent: postData,
        load: _authSuccessCB,
        contentType: "application/x-www-form-urlencoded; charset=utf-8",
        error: handlers.error
    });
};