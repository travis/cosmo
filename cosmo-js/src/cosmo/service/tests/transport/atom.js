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

dojo.provide("cosmo.service.tests.transport.atom");

dojo.require("cosmo.service.transport.Atom");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.tests.jum");
//Initialization.
//TODO - once Dojo implements setUp() and tearDown() move this code there.

doh.register("cosmo.service.tests.transport.atom", [
    function generateUri(){
        var at = new cosmo.service.transport.Atom();
        var u = at.generateUri("a/b", "/c", {d:"e"});
        jum.assertEquals("no query base uri generate unsuccessful", "a/b/c?d=e", u);

        u = at.generateUri("a/b?g=h", "/c", {d:"e"});
        jum.assertEquals("query base uri generate unsuccessful", "a/b/c?d=e&g=h", u);

    },
    function getUsernameForURI(){
        var at = new cosmo.service.transport.Atom();
        cosmo.util.auth.setUsername("!@#$%^&*()\"';?/+=-_<>,.");
        jum.assertEquals("not escaping correctly",
                         "!%40%23%24%25%5E%26*()%22'%3B%3F%2F%2B%3D-_%3C%3E%2C.",
                         at._getUsernameForURI());

    }
]);