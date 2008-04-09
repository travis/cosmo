if(!dojo._hasResource['cosmo.account.login']){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource['cosmo.account.login'] = true;
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

dojo.require("cosmo.env");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.convenience");

cosmo.account.login.doLogin = function(un, pw){
    var postData = 
        {"j_username": encodeURIComponent(un), 
         "j_password": encodeURIComponent(pw)};

    var d = dojo.xhrPost({
        url: cosmo.env.getFullUrl("Auth"),
        content: postData
    });
    d.addCallback(function(url){
        if (url.indexOf("/loginfailed") > -1){
            throw new Error(_('Login.Error.AuthFailed'));
        } else {
            cosmo.util.auth.setCred(un, pw);
            return url;
        }
    });
    return d;
};

}
