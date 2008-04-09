if(!dojo._hasResource["cosmo.ui.widget.SharingDialog"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["cosmo.ui.widget.SharingDialog"] = true;
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

dojo.provide("cosmo.ui.widget.SharingDialog");
dojo.require("dijit._Templated");
dojo.requireLocalization("cosmo.ui.widget", "SharingDialog", null, "ROOT");

dojo.declare(
    "cosmo.ui.widget.SharingDialog", [dijit._Widget, dijit._Templated],
    {
        store: null,
        collection: null,

        // collection or subscription object
        displayName: "",
        urls: null,

        templateString:"<div>\n<style type=\"text/css\">\n.linkDiv{\nwidth: 40px;\nheight: 20px;\ncolor: white;\nfont-weight: bolder;\npadding: 2px;\n}\n.linkDiv a:hover{\ntext-decoration: none;\n}\n.atomLinkDiv {\nbackground: orange;\n}\n.webcalLinkDiv {\nbackground: green;\n}\n.htmlLinkDiv {\nbackground: blue;\n}\n.davLinkDiv {\nbackground: red;\n}\n</style>\n<div id=\"${id}DisplayName\">${displayName}</div>\n<a href=\"${urls.atom}\"><span class=\"linkDiv atomLinkDiv\">${l10n.atom}</span></a>\n<a href=\"${urls.webcal}\"><span class=\"linkDiv webcalLinkDiv\">${l10n.webcal}</span></a>\n<a href=\"${urls.dav}\"><span class=\"linkDiv davLinkDiv\">${l10n.dav}</span></a>\n<a href=\"${urls.html}\"><span class=\"linkDiv htmlLinkDiv\">${l10n.html}</span></a>\n</div>\n\n",
        l10n: dojo.i18n.getLocalization("cosmo.ui.widget", "SharingDialog"),

        postMixInProperties: function(){
            var store = this.store;
            if (store){
                var collection = this.collection;
                this.displayName = store.getValue(collection, "displayName");
                this.urls = store.getValue(collection, "urls");
            }
        }
    }
);

}
