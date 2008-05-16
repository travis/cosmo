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

dojo.provide("cosmotest.cmp.test_cmp")
dojo.require("cosmo.cmp");

cosmotest.cmp.test_cmp = {
    test_userXML: function(){
        var user1 = {
            username: "test1",
            password: "password1",
            firstName: "test1",
            lastName: "mctest1",
            email: "test1@example.com"
        };

        var user1XML = '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
            '<user xmlns="http://osafoundation.org/cosmo/CMP">' +
            '<username>test1</username>' +
            '<password>password1</password>' +
            '<firstName>test1</firstName>' +
            '<lastName>mctest1</lastName>' +
            '<email>test1@example.com</email>' +
            '</user>';

        jum.assertEquals("user 1 wrong", user1XML, cosmo.cmp.userHashToXML(user1));

        var user2 = {
            username: "test2",
            password: "password2",
            firstName: "test2",
            lastName: "mctest2",
            email: "test2@example.com",
            subscription: {
                name: "test subscription",
                ticket: "09876",
                uuid: "0123456789"
            }
        };

        var user2XML = '<?xml version="1.0" encoding="utf-8" ?>\r\n' +
            '<user xmlns="http://osafoundation.org/cosmo/CMP">' +
            '<username>test2</username>' +
            '<password>password2</password>' +
            '<firstName>test2</firstName>' +
            '<lastName>mctest2</lastName>' +
            '<email>test2@example.com</email>' +
            '<subscription name="test subscription" ticket="09876">0123456789</subscription>' + 
            '</user>';

        jum.assertEquals("user 2 wrong", user2XML, cosmo.cmp.userHashToXML(user2));

        user2.subscription.name = null;

        try {
            cosmo.cmp.userHashToXML(user2);
            jum.assertTrue("null name didn't throw exception", false);
        } catch (e1){
            jum.assertTrue("e1 name not null", e1.name == null);
            jum.assertEquals("e1 ticket wrong", e1.ticket, "09876");
            jum.assertEquals("e1 uuid wrong", e1.uuid, "0123456789");
        }

        user2.subscription.name = "test subscription";
        user2.subscription.ticket = null;

        try {
            cosmo.cmp.userHashToXML(user2);
            jum.assertTrue("null ticket didn't throw exception", false);
        } catch (e2){
            jum.assertTrue("e2 ticket not null", e2.ticket == null);
            jum.assertEquals("e2 name wrong", e2.name, "test subscription");
            jum.assertEquals("e2 uuid wrong", e2.uuid, "0123456789");
        }

        user2.subscription.ticket = "09876";
        user2.subscription.uuid = null;

        try {
            cosmo.cmp.userHashToXML(user2);
            jum.assertTrue("null uuid didn't throw exception", false);
        } catch (e3){
            jum.assertTrue("e3 uuid not null", e3.uuid == null);
            jum.assertEquals("e3 name wrong", e3.name, "test subscription");
            jum.assertEquals("e3 ticket wrong", e3.ticket, "09876");
        }
    }
}


