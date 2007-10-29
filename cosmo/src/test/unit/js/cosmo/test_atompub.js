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
        var nullContent = entry1.deserializeContent();
        jum.assertTrue("entry1 content wrong", nullContent == null); 
        
        var contentList = feed.deserializeEntryContents();
        jum.assertEquals("feed entry contents wrong length", 1, contentList.length);
        jum.assertEquals("feed content wrong", [null], contentList); 

        var feedDocString = feed.toXmlDocumentString();
        jum.assertEquals("feed serialization wrong", 
                         cosmotest.test_atompub.feed1Serialization,
                         feedDocString);
    },

    test_Feed2: function(){
        var feed = new cosmo.atompub.Feed(cosmotest.test_atompub.feedDoc2.documentElement);
        jum.assertTrue("feed text wrong", feed.text == null);
        jum.assertEquals("title wrong", "dive into mark", feed.title.text);
        jum.assertEquals("title type wrong", "text", feed.title.type);
        jum.assertEquals("subtitle wrong", 'A <em>lot</em> of effort ' +
                         'went into making this effortless', feed.subtitle.text);
        jum.assertEquals("subtitle type wrong", "html", feed.subtitle.type);
        //TODO: test for updated
        jum.assertEquals("id wrong", "tag:example.org,2003:3",
                         feed.id.text);
        jum.assertEquals("link0 rel wrong", "alternate", feed.links[0].rel);
        jum.assertEquals("link0 type wrong", "text/html", feed.links[0].type);
        jum.assertEquals("link0 hreflang wrong", "en", feed.links[0].hreflang);
        jum.assertEquals("link0 href wrong", "http://example.org/", feed.links[0].href);
        jum.assertEquals("link1 rel wrong", "self", feed.links[1].rel);
        jum.assertEquals("link1 type wrong", "application/atom+xml", feed.links[1].type);
        jum.assertEquals("link1 href wrong", "http://example.org/feed.atom", feed.links[1].href);
        jum.assertEquals("getLinks 1 wrong", feed.getLinks('alternate')[0], feed.links[0]); 
        jum.assertEquals("getLinks 2 wrong", feed.getLinks('self')[0], feed.links[1]); 
        jum.assertEquals("rights wrong", "Copyright (c) 2003, Mark Pilgrim", feed.rights.text);
        jum.assertEquals("generator wrong", "Example Toolkit", feed.generator.text);
        jum.assertEquals("generator uri wrong", "http://www.example.com/", feed.generator.uri);
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
        jum.assertEquals("getLinks entry1-1 wrong", 
                         entry1.getLinks('alternate')[0], entry1.links[0]); 
        jum.assertEquals("getLinks entry1-2 wrong", 
                         entry1.getLinks('enclosure')[0], entry1.links[1]); 

        //TODO: test for updated
        //TODO: test for published
        jum.assertEquals("author name wrong", "Mark Pilgrim", entry1.authors[0].name.text);
        jum.assertEquals("author uri wrong", "http://example.org/", entry1.authors[0].uri.text);
        jum.assertEquals("author email wrong", "f8dy@example.com", entry1.authors[0].email.text);
        jum.assertEquals("contributor0 name wrong", "Sam Ruby", entry1.contributors[0].name.text);
        jum.assertEquals("contributor1 name wrong", "Joe Gregorio", entry1.contributors[1].name.text);
        var content = entry1.content;
        jum.assertEquals("content type wrong", "xhtml", content.type);
        var xhtml0 = content.deserializeContent();

        var xhtml1 = entry1.deserializeContent();
        
        var xhtmlList = feed.deserializeEntryContents();
        jum.assertEquals("feed entry contents wrong length", 1, xhtmlList.length);
        var feedDocString = feed.toXmlDocumentString();
        jum.assertEquals("feed serialization wrong", 
                         cosmotest.test_atompub.feed2Serialization,
                         feedDocString);
    },
 
    test_Entry1: function(){
        var entry = new cosmo.atompub.Entry(cosmotest.test_atompub.entryDoc1.documentElement);
        jum.assertEquals("title wrong", "HTML Content", entry.title.text);
        jum.assertEquals("id wrong", "html-content", entry.id.text);
        //TODO: updated
        var content = entry.content;
        jum.assertEquals("content type wrong", "html", content.type);
        var html0 = content.deserializeContent();
        jum.assertEquals("html0 content wrong", "<b>awesome</b>", html0);

        var html1 = entry.deserializeContent();
        jum.assertEquals("html1 content wrong", "<b>awesome</b>", html1);
        var entryDocString = entry.toXmlDocumentString();
        jum.assertEquals("entry serialization wrong", 
                         cosmotest.test_atompub.entry1Serialization,
                         entryDocString);
    },
    test_Entry2: function(){
        var entry = new cosmo.atompub.Entry(cosmotest.test_atompub.entryDoc2.documentElement);
        jum.assertEquals("title wrong", "link test", entry.title.text);
        jum.assertEquals("id wrong", "link-test", entry.id.text);
        //TODO: updated
        var content = entry.content;
        jum.assertEquals("content type wrong", "text", content.type);
        var text0 = content.deserializeContent();
        jum.assertEquals("text0 content wrong", "link test", text0);
        var text1 = entry.deserializeContent();
        jum.assertEquals("text1 content wrong", "link test", text1);
        //var entryDocString = entry.toXmlDocumentString();
        //jum.assertEquals("entry serialization wrong", 
        //cosmotest.test_atompub.entry1Serialization,
        //entryDocString);
        jum.assertEquals("getLinks link 0 wrong", 
                         entry.getLinks('alternate')[0], entry.links[0]); 
        jum.assertEquals("getLinks link 1 wrong", 
                         entry.getLinks('alternate')[1], entry.links[1]); 
        
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
            'A &lt;em&gt;lot&lt;/em&gt; of effort ' +
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
            '</feed>'),

    entryDoc1: cosmotest.util.toXMLDocument(
        '<?xml version="1.0" encoding="utf-8"?>' +
            '<entry>' +
            '<title>HTML Content</title>' +
            '<id>html-content</id>' +
            '<updated>2007-10-25T12:29:29Z</updated>' +
            '<content type="html">' +
            '&lt;b&gt;awesome&lt;/b&gt;' +
            '</content>' +
            '</entry>'),

    entryDoc2: cosmotest.util.toXMLDocument(
        '<?xml version="1.0" encoding="utf-8"?>' +
            '<entry>' +
            '<title>link test</title>' +
            '<id>link-test</id>' +
            '<updated>2007-10-25T12:29:29Z</updated>' +
            '<link rel="alternate" type="text/html" ' +
            'href="http://example.org/cosmo/html"/>' +
            '<link rel="alternate" type="text/eim-json" ' +
            'href="http://example.org/cosmo/eim-json"/>' +
            '<content type="text">' +
            "link test" + 
            '</content>' +
            '</entry>'),

    feed1Serialization: '<?xml version="1.0" encoding=\'utf-8\'?><feed><title>Example Feed</title><id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id><updated>2003-12-13T18:30:02Z</updated><author> <name>John Doe</name></author><link href="http://example.org/"/><entry><title>Atom-Powered Robots Run Amok</title><updated>2003-12-13T18:30:02Z</updated><summary>Some text.</summary><id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id><link href="http://example.org/2003/12/13/atom03"/></entry></feed>',
    feed2Serialization: '<?xml version="1.0" encoding=\'utf-8\'?><feed><title type="text">dive into mark</title><generator uri="http://www.example.com/" version="1.0">Example Toolkit</generator><id>tag:example.org,2003:3</id><rights>Copyright (c) 2003, Mark Pilgrim</rights><subtitle type="html">A &lt;em&gt;lot&lt;/em&gt; of effort went into making this effortless</subtitle><updated>2005-07-31T12:29:29Z</updated><link href="http://example.org/" rel="alternate" type="text/html" hreflang="en"/><link href="http://example.org/feed.atom" rel="self" type="application/atom+xml"/><entry><title>Atom draft-07 snapshot</title><updated>2005-07-31T12:29:29Z</updated><content type="xhtml"><content xmlns="http://www.w3.org/2005/Atom" type="xhtml" xml:lang="en" xml:base="http://diveintomark.org/"><div xmlns="http://www.w3.org/1999/xhtml"><p><i>[Update: The Atom draft is finished.]</i></p></div></content></content><published>2003-12-13T08:29:29-04:00</published><id>tag:example.org,2003:3.2397</id><author><name>Mark Pilgrim</name><uri>http://example.org/</uri><email>f8dy@example.com</email></author><contributor><name>Sam Ruby</name></contributor><contributor><name>Joe Gregorio</name></contributor><link href="http://example.org/2005/04/02/atom" rel="alternate" type="text/html"/><link href="http://example.org/audio/ph34r_my_podcast.mp3" rel="enclosure" type="audio/mpeg" length="1337"/></entry></feed>',

    entry1Serialization: '<?xml version="1.0" encoding=\'utf-8\'?><entry><title>HTML Content</title><updated>2007-10-25T12:29:29Z</updated><content type="html">&lt;b&gt;awesome&lt;/b&gt;</content><id>html-content</id></entry>'
}
