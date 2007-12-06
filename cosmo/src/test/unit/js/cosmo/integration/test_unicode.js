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

dojo.provide("cosmotest.integration.test_unicode");

dojo.require("cosmotest.testutils");
dojo.require("cosmo.service.conduits.common");

cosmotest.integration.test_unicode = {
    test_accountCreate: function(){
        var strings = cosmotest.integration.test_unicode.unicodeStrings;
        var conduit = cosmo.service.conduits.getAtomPlusEimConduit();
        for (var i in strings){
            cosmotest.testutils.createUser(strings[i]);
            var collections = conduit.getCollections({sync: true}).results[0];
            jum.assertTrue("collections", !!collections);
            jum.assertTrue("collections length", collections.length > 0);
            cosmotest.testutils.cleanupUser(strings[i]);
        }
    },
    
    unicodeStrings: [
              "test", 
              "test2", 
              "\u0080est", //Latin-1 Supplement 0080
              "\u0100est", //Latin Extended-A 0100
              "\u0180est", //Latin Extended-B 0180
              "\u0250est", //IPA Extensions 0250
              "\u0280est", //Spacing Modifier Letters 0280
              "\u0300est", //Combining Diacritical Marks 0300
              "\u0370est", //Greek and Coptic 0370
              "\u0400est", //Cyrillic 0400
              "\u0500est", //Cyrillic Supplement 0500
              "\u0530est", //Armenian 0530
              "\u1400est", //Canadian Aboriginal Syllabics 1400
              "\u2020est", //General Punctuation 2020
              "\u2F00est", //Kangxi Radicals 2F00
              "\u30A0est", //Katakana 30A0
              "\uA700est" //Modifier Tone Letters A700

/* TODO: JavaScript keeps these as utf-16 bytes, so when we convert to utf-8 and
   then base64 everything goes nuts. Solution is to modify the utf-8 conversion routine
   to detect these chars and translate appropriately.
              "\uD800\uDD40est", //Ancient Greek Numbers 10140
              "\uD834\uDD00est", //Musical Symbols 1D100
              "\uD834\uDF60est", //Counting Rod Numerals 1D360
              "\uD87E\uDC00est" //CJK Compat. Ideographs Supp. 2F800*/
    ]
}
