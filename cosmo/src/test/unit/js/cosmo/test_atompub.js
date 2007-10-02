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

cosmotest.test_atompub = {
    test_Service: function(){
        var service = new cosmo.atompub.Service(cosmotest.test_atompub.serviceTestDoc);
        xxx = service
        jum.assertEquals("workspaces not created", 2, service.workspaces.length)
    },

    test_Workspace: function(){
        
    },

    serviceTestDoc: cosmotest.util.toXMLDocument(
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
            '</service>')
}