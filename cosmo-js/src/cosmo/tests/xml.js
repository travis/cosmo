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
dojo.provide("cosmo.tests.xml");
dojo.require("cosmo.xml");
dojo.require("dojox.data.dom");

cosmo.tests.xml.doc1 = dojox.data.dom.createDocument(
    '<?xml version="1.0" encoding="utf-8"?>' +
    '<cosmo xmlns="http://chandlerproject.org/ns/test/cosmo">' +
    '<foo></foo>' +
    '<foo></foo>' +
    '<foo></foo>' +
    '<foo></foo>' +
    '</cosmo>');
cosmo.tests.xml.doc1ns = {
    cosmo: "http://chandlerproject.org/ns/test/cosmo"
};

cosmo.tests.xml.atomFeed = dojox.data.dom.createDocument(
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
cosmo.tests.xml.atomns = {
    atom: "http://www.w3.org/2005/Atom"
};

cosmo.tests.xml.bookstore = dojox.data.dom.createDocument('<?xml version="1.0" encoding="ISO-8859-1"?>' +
    '<bookstore xmlns="http://www.w3schools.com/Xpath/xpath_examples.asp">' +
    '<book category="COOKING">' +
    '<title lang="en">Everyday Italian</title>' +
    '<author>Giada De Laurentiis</author>' +
    '<year>2005</year>' +
    '<price>30.00</price>' +
    '</book>' +
    '<book category="CHILDREN">' +
    '<title lang="en">Harry Potter</title>' +
    '<author>J K. Rowling</author>' +
    '<year>2005</year>' +
    '<price>29.99</price>' +
    '</book>' +
    '<book category="WEB">' +
    '<title lang="en">XQuery Kick Start</title>' +
    '<author>James McGovern</author>' +
    '<author>Per Bothner</author>' +
    '<author>Kurt Cagle</author>' +
    '<author>James Linn</author>' +
    '<author>Vaidyanathan Nagarajan</author>' +
    '<year>2003</year>' +
    '<price>49.99</price>' +
    '</book>' +
    '<book category="WEB">' +
    '<title lang="en">Learning XML</title>' +
    '<author>Erik T. Ray</author>' +
    '<year>2003</year>' +
    '<price>39.95</price>' +
    '</book>' +
    '</bookstore>');

cosmo.tests.xml.bookstoreNS =
    {
        bs: "http://www.w3schools.com/Xpath/xpath_examples.asp"
    };

doh.register("cosmo.tests.xPath",
	[
        function testElement(t){
            t.is(4, cosmo.xml.query("cosmo:foo", cosmo.tests.xml.doc1.firstChild, cosmo.tests.xml.doc1ns).length);
        },
        function testAttribute(t){
            t.is("http://example.org/2003/12/13/atom03/edit",
                 cosmo.xml.query("atom:link[@rel='edit']/@href", cosmo.tests.atompub.entry1, cosmo.tests.xml.atomns)[0].value);
        },

        // Test examples on w3 schools page
        function testW3(t){
            var ns = cosmo.tests.xml.bookstoreNS;
            var node = cosmo.tests.xml.bookstore.firstChild;
            t.is(4, cosmo.xml.query("/bs:bookstore/bs:book/bs:title", node, ns, "bs").length);
            var title1 = cosmo.xml.query("/bs:bookstore/bs:book[1]/bs:title", node, ns)[0];
            t.is("en", title1.getAttribute("lang"));
            t.is("Everyday Italian", title1.textContent);
            cosmo.xml.query("/bookstore/book/price/text()", node, ns)
            cosmo.xml.query("/bookstore/book[price>35]/price", node, ns)
            cosmo.xml.query("/bookstore/book[price>35]/title", node, ns)
        }
    ]);

