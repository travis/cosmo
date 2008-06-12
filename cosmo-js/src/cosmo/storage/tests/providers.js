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
dojo.provide("cosmo.storage.tests.providers");
dojo.require("cosmo.storage");
dojo.require("cosmo.storage.Dom");
dojo.require("cosmo.storage.Cookie");

(function(){

    function testProvider(t, s) {
        doh.debug("Provider: " + s.declaredClass);
        s.put("foo", "bar");
        t.t(typeof s.get("foo") == "string");
        t.is("bar", s.get("foo"));
        s.remove("foo");
        t.f(!!s.get("foo"));
    }
    doh.register("cosmo.storage.tests.providers",
        [
            function testDefaultProvider(t){
                testProvider(t, cosmo.storage.provider);
            }
        ]);

    var providers = [new cosmo.storage.Dom(),
                     new cosmo.storage.Cookie()];
    dojo.forEach(providers,
        function(p){
            if (p.isAvailable())
                doh.register("cosmo.storage.tests." + p.declaredClass,
                             [
                                 function(t){
                                     testProvider(t, p);
                                 }
                             ]
                            );
        });
})();


