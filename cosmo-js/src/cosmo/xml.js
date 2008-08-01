/* * Copyright 2008 Open Source Applications Foundation *
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

 /**
 * summary:
 *   This module provides utility functions for working with XML
 * description:
 *   This module provides utility functions for working with XML. Currently it provides
 *   a cross-browser xpath api as well as a method for getting the current xml base property.
 */

dojo.provide("cosmo.xml");

dojo.mixin(cosmo.xml,
{
    query: function xpathQuery(/*String*/query, node, nsMap, nsDefault){
        // summary:
        //     Accepts an XPath query string (http://www.w3.org/TR/xpath)
        //     and returns a dojo.NodeList of nodes that match.
        var result = new dojo.NodeList();
        var queryResult;
        if (dojo.isIE){
            var ns = "";
            for (var pre in nsMap) ns += "xmlns:" + pre + "='" + nsMap[pre] + "' ";
            node.ownerDocument.setProperty("SelectionLanguage", "XPath");
            node.ownerDocument.setProperty("SelectionNamespaces", ns);
            queryResult = node.selectNodes(query);
            for (var i = 0; i < queryResult.length; i++) result.push(queryResult[i]);
            return result;
        } else {
            queryResult = node.ownerDocument.evaluate(query, node,
                function(pre){return nsMap[pre] || (nsDefault? nsMap[nsDefault] : null);},
                XPathResult.ANY_TYPE, null);
            var value = queryResult.iterateNext();
            while(value){
                result.push(value);
                value = queryResult.iterateNext();
            }
            return result;
        }
    },

    getBaseUri: function(node){
        return node.baseURI || this._findBaseUri(node);
    },

    // Assumes node.baseURI doesn't exist and tries harder to find it
    _findBaseUri: function(node){
        return node.ownerDocument.documentElement.getAttribute("xml:base");
    }
});
