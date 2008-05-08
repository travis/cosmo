/*
 * Copyright 2008 Open Source Applications Foundation
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
dojo.provide("cosmo.auth.tests.wsse");
dojo.require("cosmo.auth.wsse");
dojo.require("dojo.cookie");
doh.register("cosmo.auth.tests.wsse",
	[
        function testGenerateDigest(t){
            t.is("quR/EWLAV4xLf9Zqyw4pDmfV9OY=", cosmo.auth.wsse.generatePasswordDigest("d36e316282959a9ed4c89851497a717f", "2003-12-15T14:43:07Z", "taadtaadpstcsm"));
        },

        function testGenerateCredentials(t){
            cosmo.auth.wsse.setUsername("bob");
            cosmo.auth.wsse.setPassword("taadtaadpstcsm");
            var cred = cosmo.auth.wsse.generateCredentials();
            t.is('WSSE profile="UsernameToken"', cred['Authorization']);
            t.t(!!cred['X-WSSE'].match(/Username="bob"/));
            t.t(!!cred['X-WSSE'].match(/PasswordDigest=".*"/));
            t.t(!!cred['X-WSSE'].match(/Nonce=".*"/));
            t.t(!!cred['X-WSSE'].match(/Created=".*"/));
        }

    ]);
