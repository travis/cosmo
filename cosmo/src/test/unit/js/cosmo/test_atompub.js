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

dojo.provide("cosmotest.test_atompub")

dojo.require("cosmotest.util");

dojo.require("cosmo.atompub");
dojo.require("dojo.debug");

cosmotest.test_atompub = {
    test_Service: function(){
        var service = new cosmo.atompub.Service(cosmotest.test_atompub.serviceDoc.documentElement);
        jum.assertEquals("workspaces not created", 2, service.workspaces.length);
        jum.assertEquals("first workspace wrong number of collections", 2, service.workspaces[0].collections.length);
        jum.assertEquals("first workspace first collection href", "http://example.org/blog/main", service.workspaces[0].collections[0].href);
    },

    test_Workspace: function(){
        
    },

    test_Feed1: function(){
        var feed = new cosmo.atompub.Feed(cosmotest.test_atompub.feedDoc1.documentElement);
        jum.assertEquals("title wrong", "Example Feed", feed.title.text);
        jum.assertEquals("link href wrong", "http://example.org/", feed.links[0].href);
        // TODO: test for updated
        jum.assertEquals("author wrong", "John Doe", feed.authors[0].name.text);
        jum.assertEquals("id wrong", "urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6", 
                         feed.id.text);
        var entry1 = feed.entries[0];
        jum.assertEquals("entry1 link wrong", "http://example.org/2003/12/13/atom03",
                         entry1.links[0].href);
        jum.assertEquals("entry1 id wrong", "urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a",
                         entry1.id.text);
        //TODO: test for updated
        jum.assertEquals("entry1 summary wrong", "Some text.",
                         entry1.summary.text);
    },

    test_Feed2: function(){
        dojo.debug("feed2");
        console.log(cosmotest.test_atompub.feedDoc2)
        console.log(cosmotest.test_atompub.feedDoc2.documentElement)
        var feed = new cosmo.atompub.Feed(cosmotest.test_atompub.feedDoc2.documentElement);
        jum.assertEquals("title wrong", "dive into mark", feed.title.text);
        jum.assertEquals("title type wrong", "text", feed.title.type);
        jum.assertEquals("subtitle wrong", 'A <em>lot</em> of effort' +
                         'went into making this effortless', feed.subtitle.text);
        jum.assertEquals("subtitle type wrong", "html", feed.subtitle.type);
        //TODO: test for updated
        jum.assertEquals("id wrong", "tag:example.org,2003:3",
                         feed.id.text);
        jum.assertEquals("link0 rel wrong", "alternate", feed.links[0].rel);
        dojo.debug("foo");
        jum.assertEquals("link0 type wrong", "text/html", feed.links[0].type);
        jum.assertEquals("link0 hreflang wrong", "en", feed.links[0].hreflang);
        jum.assertEquals("link0 href wrong", "http://example.org/", feed.links[0].href);
        jum.assertEquals("link1 rel wrong", "self", feed.links[1].rel);
        jum.assertEquals("link1 type wrong", "application/atom+xml", feed.links[1].type);
        jum.assertEquals("link1 href wrong", "http://example.org/feed.atom", feed.links[1].href);
        
        jum.assertEquals("rights wrong", "Copyright (c) 2003, Mark Pilgrim", feed.rights.text);
        jum.assertEquals("generator wrong", "Example Toolkit", feed.generator.text);
        jum.assertEquals("generator uri wrong", "http://www.example.com", feed.generator.uri);
        jum.assertEquals("generator version wrong", "1.0", feed.generator.version);
        var entry1 = feed.entries[0];
        jum.assertEquals("entry1 title wrong", "Atom draft-07 snapshot",
                         entry1.title.text);
        jum.assertEquals("entry1 link0 href wrong", "http://example.org/2005/04/02/atom",
                         entry1.links[0].href);
        jum.assertEquals("entry1 link0 rel wrong", "alternate",
                         entry1.links[0].rel);
        jum.assertEquals("entry1 link0 type wrong", "text/html",
                         entry1.links[0].type);
        jum.assertEquals("entry1 link1 href wrong", "http://example.org/audio/ph34r_my_podcast.mp3",
                         entry1.links[1].href);
        jum.assertEquals("entry1 link1 rel wrong", "enclosure",
                         entry1.links[1].rel);
        jum.assertEquals("entry1 link1 type wrong", "audio/mpeg",
                         entry1.links[1].type);
        jum.assertEquals("entry1 link1 length wrong", "1337",
                         entry1.links[1].length);
        jum.assertEquals("entry1 id wrong", "tag:example.org,2003:3.2397",
                         entry1.id.text);
        //TODO: test for updated
        //TODO: test for published
        jum.assertEquals("author name wrong", "Mark Pilgrim", entry1.authors[0].name.text);
        jum.assertEquals("author uri wrong", "http://example.org/", entry1.authors[0].uri.text);
        jum.assertEquals("author email wrong", "f8dy@example.com", entry1.authors[0].email.text);
        jum.assertEquals("contributor0 name wrong", "Sam Ruby", entry1.contributors[0].name.text);
        jum.assertEquals("contributor1 name wrong", "Joe Gregorio", entry1.contributors[1].name.text);
        //TODO: content
    },

    serviceDoc: cosmotest.util.toXMLDocument(
        '<?xml version="1.0" encoding=\'utf-8\'?>' +
            '<service xmlns="http://www.w3.org/2007/app" ' +
            'xmlns:atom="http://www.w3.org/2005/Atom">' +
            '<workspace>' +
            '<atom:title>Main Site</atom:title>' +
            '<collection ' +
            'href="http://example.org/blog/main" >' +
            '<atom:title>My Blog Entries</atom:title>' +
            '<categories ' +
            'href="http://example.com/cats/forMain.cats" />' +
            '</collection>' +
            '<collection ' +
            'href="http://example.org/blog/pic" >' +
            '<atom:title>Pictures</atom:title>' +
            '<accept>image/png</accept>' +
            '<accept>image/jpeg</accept>' +
            '<accept>image/gif</accept>' +
            '</collection>' +
            '</workspace>' +
            '<workspace>' +
            '<atom:title>Sidebar Blog</atom:title>' +
            '<collection ' +
            'href="http://example.org/sidebar/list" >' +
            '<atom:title>Remaindered Links</atom:title>' +
            '<accept>application/atom+xml;type=entry</accept>' +
            '<categories fixed="yes">' +
            '<atom:category ' +
            'scheme="http://example.org/extra-cats/" ' +
            'term="joke" />' +
            '<atom:category ' +
            'scheme="http://example.org/extra-cats/" ' +
            'term="serious" />' +
            '</categories>' +
            '</collection>' +
            '</workspace>' +
            '</service>'),

    feedDoc1: cosmotest.util.toXMLDocument(
        '<?xml version="1.0" encoding="utf-8"?>' + 
            '<feed xmlns="http://www.w3.org/2005/Atom">' +
            '<title>Example Feed</title> ' +
            '<link href="http://example.org/"/>' +
            '<updated>2003-12-13T18:30:02Z</updated>' +
            '<author> ' +
            '<name>John Doe</name>' +
            '</author> ' +
            '<id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>' +
            '<entry>' +
            '<title>Atom-Powered Robots Run Amok</title>' +
            '<link href="http://example.org/2003/12/13/atom03"/>' +
            '<id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>' +
            '<updated>2003-12-13T18:30:02Z</updated>' +
            '<summary>Some text.</summary>' +
            '</entry>' +
            '</feed>'),

    feedDoc2: cosmotest.util.toXMLDocument(
        '<?xml version="1.0" encoding="utf-8"?>' +
            '<feed xmlns="http://www.w3.org/2005/Atom">' +
            '<title type="text">dive into mark</title>' +
            '<subtitle type="html">' +
            'A &lt;em&gt;lot&lt;/em&gt; of effort' +
            'went into making this effortless' +
            '</subtitle>' +
            '<updated>2005-07-31T12:29:29Z</updated>' +
            '<id>tag:example.org,2003:3</id>' +
            '<link rel="alternate" type="text/html" ' +
            'hreflang="en" href="http://example.org/"/>' +
            '<link rel="self" type="application/atom+xml" ' +
            'href="http://example.org/feed.atom"/>' +
            '<rights>Copyright (c) 2003, Mark Pilgrim</rights>' +
            '<generator uri="http://www.example.com/" version="1.0">' +
            'Example Toolkit' +
            '</generator>' +
            '<entry>' +
            '<title>Atom draft-07 snapshot</title>' +
            '<link rel="alternate" type="text/html" ' +
            'href="http://example.org/2005/04/02/atom"/>' +
            '<link rel="enclosure" type="audio/mpeg" length="1337" ' +
            'href="http://example.org/audio/ph34r_my_podcast.mp3"/>' +
            '<id>tag:example.org,2003:3.2397</id>' +
            '<updated>2005-07-31T12:29:29Z</updated>' +
            '<published>2003-12-13T08:29:29-04:00</published>' +
            '<author>' +
            '<name>Mark Pilgrim</name>' +
            '<uri>http://example.org/</uri>' +
            '<email>f8dy@example.com</email>' +
            '</author>' +
            '<contributor>' +
            '<name>Sam Ruby</name>' +
            '</contributor>' +
            '<contributor>' +
            '<name>Joe Gregorio</name>' +
            '</contributor>' +
            '<content type="xhtml" xml:lang="en" ' +
            'xml:base="http://diveintomark.org/">' +
            '<div xmlns="http://www.w3.org/1999/xhtml">' +
            '<p><i>[Update: The Atom draft is finished.]</i></p>' +
            '</div>' +
            '</content>' +
            '</entry>' +
            '</feed>')
}