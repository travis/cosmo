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
dojo.provide("cosmo.tests.atompub");
dojo.require("cosmo.atompub");
dojo.require("dojox.data.dom");
cosmo.tests.atompub.feed1 = dojox.data.dom.createDocument(
    '<?xml version="1.0" encoding="utf-8"?>' +
    '<feed xmlns="http://www.w3.org/2005/Atom">' +
    '<title>Example Feed</title>' +
    '<subtitle>A subtitle.</subtitle>' +
    '<link href="http://example.org/feed/" rel="self"/>' +
    '<link href="http://example.org/"/>' +
    '<updated>2003-12-13T18:30:02Z</updated>' +
    '<author>' +
    '<name>John Doe</name>' +
    '<email>johndoe@example.com</email>' +
    '</author>' +
    '<id>urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6</id>' +
    '<entry>' +
    '<title>Atom-Powered Robots Run Amok</title>' +
    '<link href="http://example.org/2003/12/13/atom03"/>' +
    '<link href="http://example.org/2003/12/13/atom03/edit" rel="edit"/>' +
    '<id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>' +
    '<updated>2003-12-13T18:30:02Z</updated>' +
    '<summary>Some text.</summary>' +
    '</entry>' +
    '</feed>');

cosmo.tests.atompub.entry1 = dojo.query("entry", cosmo.tests.atompub.feed1)[0];
cosmo.tests.atompub.service1 = dojox.data.dom.createDocument(
    "<?xml version='1.0' encoding='UTF-8'?>" +
    '<service xmlns:atom="http://www.w3.org/2005/Atom" xmlns="http://www.w3.org/2007/app" xml:base="http://localhost:8080/chandler/atom/">' +
    '<workspace>' +
    '<atom:title type="text">home</atom:title>' +
    '</workspace>' +
    '<workspace>' +
    '<atom:title type="text">account</atom:title>' +
    '<collection href="user/travis/subscriptions">' +
    '<accept>application/atom+xml;type=entry</accept>' +
    '<atom:title type="text">subscriptions</atom:title>' +
    '</collection>' +
    '<collection href="user/travis/preferences">' +
    '<accept>application/atom+xml;type=entry</accept>' +
    '<atom:title type="text">preferences</atom:title>' +
    '</collection>' +
    '</workspace>' +
    '</service>');

doh.register("cosmo.tests.atompub",
	 [
        function getEditIri(t){
            t.is("http://example.org/2003/12/13/atom03/edit",
                 cosmo.atompub.getEditIri(cosmo.tests.atompub.entry1));
        },

        function testStockQueries(t){
            var e = cosmo.tests.atompub.entry1;
            t.is("urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a", cosmo.atompub.attr.id(e));
            t.is("Atom-Powered Robots Run Amok", cosmo.atompub.attr.title(e));
        },

        function testSetValue(t){
            var e = cosmo.tests.atompub.entry1;
            cosmo.atompub.set.id(e, "foo");
            t.is("foo", cosmo.atompub.attr.id(e));
        },
        function testServiceHelpers(t){
            var s = cosmo.tests.atompub.service1.documentElement;
            var homeColls = cosmo.atompub.getCollections(s, "home");
            t.is(0, homeColls.length);
            var accountColls = cosmo.atompub.getCollections(s, "account");
            t.is(2, accountColls.length);
        }
    ]);

