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

dojo.provide("cosmotest.integration.test_atompub")

dojo.require("cosmotest.util");

dojo.require("cosmo.atompub");

cosmotest.integration.test_atompub = {
    test_Service: function(){
        var service = new cosmo.atompub.Service(cosmotest.integration.test_atompub.serviceTestDoc);
        jum.assertEquals("workspaces not created", 2, service.workspaces.length);
        jum.assertEquals("first workspace wrong number of collections", 2, service.workspaces[0].collections.length);
        jum.assertEquals("first workspace first collection href", "http://example.org/blog/main", service.workspaces[0].collections[0].href);
    }
}