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

dojo.provide("cosmotest.util.test_html");

dojo.require("cosmo.util.html");
dojo.require("dojo.dom");

cosmotest.util.test_html = {
    test_getElementsByTagName: function (){
        var xmlDoc = cosmotest.util.test_html.getTestXml();
        yyy = xmlDoc
        //Just make sure it returns something
        var workspace = cosmo.util.html.getElementsByTagName(xmlDoc, "workspace");
        jum.assertTrue("workspace", !!workspace);
        jum.assertEquals("workspace", 1, workspace.length);
        workspace = workspace[0];
        
        var collection = cosmo.util.html.getElementsByTagName(workspace, "collection");
        jum.assertTrue("collection", !!collection);
        jum.assertEquals("collection", 1, collection.length);

        collection = collection[0];
        jum.assertEquals("atom:title", 1, cosmo.util.html.getElementsByTagName(collection, "atom", "title").length);
        jum.assertEquals("accept", 1, cosmo.util.html.getElementsByTagName(collection, "accept").length);
    },
    
    getTestXml: function (){
        var x = '<?xml version=\'1.0\' encoding=\'UTF-8\'?>' +
                '<service xmlns:atom="http://www.w3.org/2005/Atom" xmlns="http://purl.org/atom/app#" xml:base="http://localhost:8080/cosmo/atom/">' +
                    '<workspace>' +
                        '<atom:title type="text">home</atom:title>' +
                        '<collection href="collection/a63180a8-8898-4650-a6e2-7518d5ab3e5d">' +
                            '<accept>entry</accept>' +
                            '<atom:title type="text">Default Collection</atom:title>' +
                        '</collection>' +
                    '</workspace>' +
                '</service>'
        return dojo.dom.createDocumentFromText(x);
        
    }
}
  