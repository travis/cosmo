/*
 * Copyright 2006-2008 Open Source Applications Foundation
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

dojo.provide("cosmotest.ui.widget.SharingDialog");

// cosmo/src/test/unit/js/ui-tester.html?widget=cosmo.ui.widget.SharingDialog&extraContextModule=cosmotest.ui.widget.SharingDialog&extraContext=cosmotest.ui.widget.SharingDialog.initContext

cosmotest.ui.widget.SharingDialog = {
    initContext: {
        store: collectionStore,
        collection: collection
    }
}
/*
        detailsXml: dojox.data.dom.createDocument(
            "<?xml version='1.0' encoding='UTF-8'?>" +
                '<feed xmlns:cosmo="http://osafoundation.org/cosmo/Atom" xmlns="http://www.w3.org/2005/Atom" xml:base="http://localhost:8080/chandler/atom/">' +
                '  <id>urn:uuid:dfdf46f0-fa03-11dc-9a86-aa3ec84a3586</id>' +
                '  <title type="text">Untitled</title>' +
                '  <updated>2008-03-25T00:40:05.908Z</updated>' +
                '  <generator uri="http://cosmo.osafoundation.org/" version="0.15-SNAPSHOT">Chandler Server</generator>' +
                '  <author>' +
                '    <name>travis</name>' +
                '    <uri>user/travis</uri>' +
                '  </author>' +
                '  <link rel="self" type="application/atom+xml" href="collection/dfdf46f0-fa03-11dc-9a86-aa3ec84a3586/details?ticket=jyc09uq9e0" />' +
                '  <link rel="edit" type="application/xhtml+xml" href="collection/dfdf46f0-fa03-11dc-9a86-aa3ec84a3586?ticket=jyc09uq9e0" />' +
                '  <link rel="morse code" type="application/eim+xml" href="http://localhost:8080/chandler/mc/collection/dfdf46f0-fa03-11dc-9a86-aa3ec84a3586?ticket=jyc09uq9e0" />' +
                '  <link rel="dav" type="text/xml" href="http://localhost:8080/chandler/dav/collection/dfdf46f0-fa03-11dc-9a86-aa3ec84a3586?ticket=jyc09uq9e0" />' +
                '  <link rel="webcal" type="text/calendar" href="http://localhost:8080/chandler/webcal/collection/dfdf46f0-fa03-11dc-9a86-aa3ec84a3586?ticket=jyc09uq9e0" />' +
                '  <link rel="alternate" type="text/html" href="http://localhost:8080/chandler/pim/collection/dfdf46f0-fa03-11dc-9a86-aa3ec84a3586?ticket=jyc09uq9e0" />' +
                '  <cosmo:ticket cosmo:type="read-only">jyc09uq9e0</cosmo:ticket>' +
                '</feed>')

*/