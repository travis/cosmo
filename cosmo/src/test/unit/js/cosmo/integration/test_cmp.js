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

dojo.provide("cosmotest.integration.test_cmp");

dojo.require("cosmotest.testutils");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.cmp");
dojo.require("dojox.uuid.generateRandomUuid");

USERNAME_ROOT = "root";
PASSWORD_ROOT = "cosmo";

doh.registerGroup(
    "cosmotest.integration.test_cmp",
    [
        // the async text fixture
        {
            name: "getUsers",
            timeout: 2000, // 2 seconds, defaults to half a second
            setUp: function(){
            },
            runTest: function(){
                var d = cosmo.cmp.getUsers();
                d.addCallback(function(userList){
                    doh.assertEqual(
                        cosmotest.integration.test_cmp._initUserCount,
                        userList.length);
                    return userList;
                });
                return d;
                
            }
        },
        // the async text fixture
        {
            name: "createUser",
            timeout: 2000, // 2 seconds, defaults to half a second
            setUp: function(){
            },
            runTest: function(){
                var u = dojox.uuid.generateRandomUuid().slice(0, 8);
                var d = cosmo.cmp.createUser(
                    {username: u,
                     email: u + "@example.com",
                     password: u,
                     firstName: u,
                     lastName: u
                    }
                );
                d.addCallback(function(userList){
                    doh.assertEqual(
                        cosmotest.integration.test_cmp._initUserCount,
                        userList.length);
                    return userList;
                });
                return d;
                
            }
        }

    ],
    function(){ //setUp
        cosmo.util.auth.setCred(USERNAME_ROOT, PASSWORD_ROOT);
        var d = cosmo.cmp.getUserCount({sync: true});
        d.addCallback(function(count){
            cosmotest.integration.test_cmp._initUserCount = parseInt(count);
        });
    },
    function(){ //tearDown
    }
);
