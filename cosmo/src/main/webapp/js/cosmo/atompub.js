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

/**
 * summary:
 *     A package for interacting with atompub services.
 *
 *     For more information about Atom Publishing Protocol, please see:
 *     http://bitworking.org/projects/atom/draft-ietf-atompub-protocol-17.html
 */


dojo.provide("cosmo.atompub");

dojo.declare("cosmo.atompub.AppElement", null, {
    initializer: function(xml){
        if (xml){
            this.fromXml(xml);
        }
    },

    fromXml: function(xml){
        for (var i in this.__elements__){
            var elementSpecification = this.__elements__[i];
            var elementName = elementSpecification[0];
            var objectName = elementSpecification[2] || elementName;
            var constructor = elementSpecification[1];
            this[objectName] = dojo.lang.map(
                xml.getElementsByTagName(elementName),
                function(xml){
                    return new constructor(xml);
                } 
            );
        }
    }
});

dojo.declare("cosmo.atompub.Collection", null, {
    
});

dojo.declare("cosmo.atompub.Feed", null, {
    
});

dojo.declare("cosmo.atompub.Entry", null, {
    
});

dojo.declare("cosmo.atompub.Workspace", null, {
    
});

dojo.declare("cosmo.atompub.Service", cosmo.atompub.AppElement, {
    __elements__: [
        ["workspace", cosmo.atompub.Workspace, "workspaces"]
    ]
    
});
