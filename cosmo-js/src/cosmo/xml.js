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
 *      This module provides utility functions for working with XML
 * description:
 *      TODO: fill this in
 */

dojo.provide("cosmo.xml");

dojo.mixin(cosmo.xml,
{
    query: function(/*String*/query, node, nsMap, nsDefault){
        // summary:
        //     Accepts an XPath query string (http://www.w3.org/TR/xpath)
        //     and returns a dojo.NodeList of nodes that match.
        if (dojo.isIE){
            var ns = "";
            for (var pre in nsMap) ns.append("xmlns:" + pre + "='" + nsMap[pre] + "'");
            node.ownerDocument.setProperty("SelectionLanguage", "XPath");
            node.ownerDocument.setProperty("SelectionNamespaces", ns);
            return new dojo.NodeList(node.selectNodes(query));
        } else {
            var queryResult = node.ownerDocument.evaluate(query, node,
                function(pre){return nsMap[pre] || (nsDefault? nsMap[nsDefault] : null);},
                XPathResult.ANY_TYPE, null);
            var result = new dojo.NodeList();
            var value = queryResult.iterateNext();
            while(value){
                result.push(value);
                value = queryResult.iterateNext();
            }
            return result;
        }
    }
});
