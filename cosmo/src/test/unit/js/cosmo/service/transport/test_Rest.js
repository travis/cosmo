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

dojo.provide("cosmotest.service.transport.test_Rest");

dojo.require("cosmo.service.transport.Rest");

//Initialization.
//TODO - once Dojo implements setUp() and tearDown() move this code there.

cosmotest.service.transport.test_Rest = {
    test_getDefaultRequest: function (){
        dojo.require("dojo.Deferred");
        dojo.require("cosmo.util.auth");

        cosmo.util.auth.clearAuth();

        var t = new cosmo.service.transport.Rest();
        var request = t.getDefaultRequest(new dojo.Deferred(), {});
        jum.assertEquals("default content type", "text/xml", request.contentType);
        jum.assertEquals("default sync", false, request.sync);
        jum.assertEquals("default transport", "XMLHTTPTransport", request.transport);
        var headers = request.headers
        jum.assertEquals("default auth", undefined, headers["Authorization"])
        
        cosmo.util.auth.setCred("foo", "bar");
        request = t.getDefaultRequest(new dojo.Deferred(), {});
        headers = request.headers
        jum.assertEquals("foo:bar auth", "Basic Zm9vOmJhcg==", headers["Authorization"]);
        
    },  

    test_queryHashToString: function (){
        var t = new cosmo.service.transport.Rest();
        jum.assertEquals("hash1",
                         "?foo=bar&baz=bet",
                         t.queryHashToString({"foo": "bar", "baz": "bet"}));
        jum.assertEquals("hash_numbers",
                         "?foo=1&baz=2",
                         t.queryHashToString({"foo": 1, "baz": 2}));
        jum.assertEquals("hash_date",
                         "?foo=2007-09-02T15%3A34%3A00-07%3A00",
                         t.queryHashToString({"foo": "2007-09-02T15:34:00-07:00"}));
        jum.assertEquals("hash$",
                         "?foo=%24",
                         t.queryHashToString({"foo": "$"}));
        jum.assertEquals("hash&",
                         "?foo=%26",
                         t.queryHashToString({"foo": "&"}));
        jum.assertEquals("hash`",
                         "?foo=%60",
                         t.queryHashToString({"foo": "`"}));
        jum.assertEquals("hash]",
                         "?foo=%5D",
                         t.queryHashToString({"foo": "]"}));
        jum.assertEquals("hash[",
                         "?foo=%5B",
                         t.queryHashToString({"foo": "["}));
        jum.assertEquals("hash^",
                         "?foo=%5E",
                         t.queryHashToString({"foo": "^"}));
        jum.assertEquals("hash\\",
                         "?foo=%5C",
                         t.queryHashToString({"foo": "\\"}));
        jum.assertEquals("hash|",
                         "?foo=%7C",
                         t.queryHashToString({"foo": "|"}));
        jum.assertEquals("hash}",
                         "?foo=%7D",
                         t.queryHashToString({"foo": "}"}));
        jum.assertEquals("hash{",
                         "?foo=%7B",
                         t.queryHashToString({"foo": "{"}));
        jum.assertEquals("hash%",
                         "?foo=%25",
                         t.queryHashToString({"foo": "%"}));
        jum.assertEquals("hash#",
                         "?foo=%23",
                         t.queryHashToString({"foo": "#"}));
        jum.assertEquals("hash>",
                         "?foo=%3E",
                         t.queryHashToString({"foo": ">"}));
        jum.assertEquals("hash<",
                         "?foo=%3C",
                         t.queryHashToString({"foo": "<"}));
        jum.assertEquals('hash"',
                         "?foo=%22",
                         t.queryHashToString({"foo": '"'}));
        jum.assertEquals("hash ",
                         "?foo=%20",
                         t.queryHashToString({"foo": " "}));
        jum.assertEquals("hash@",
                         "?foo=%40",
                         t.queryHashToString({"foo": "@"}));
        jum.assertEquals("hash?",
                         "?foo=%3F",
                         t.queryHashToString({"foo": "?"}));
        jum.assertEquals("hash=",
                         "?foo=%3D",
                         t.queryHashToString({"foo": "="}));
        jum.assertEquals("hash;",
                         "?foo=%3B",
                         t.queryHashToString({"foo": ";"}));
        jum.assertEquals("hash:",
                         "?foo=%3A",
                         t.queryHashToString({"foo": ":"}));
        jum.assertEquals("hash/",
                         "?foo=%2F",
                         t.queryHashToString({"foo": "/"}));
        jum.assertEquals("hash,",
                         "?foo=%2C",
                         t.queryHashToString({"foo": ","}));
        jum.assertEquals("hash+",
                         "?foo=%2B",
                         t.queryHashToString({"foo": "+"}));
    }
}