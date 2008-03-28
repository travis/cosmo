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

dojo.provide("cosmo.ui.widget.SharingDialog");
dojo.require("dijit._Templated");
dojo.requireLocalization("cosmo.ui.widget", "SharingDialog");

dojo.declare(
    "cosmo.ui.widget.SharingDialog", [dijit._Widget, dijit._Templated], 
    {
        store: null,
        collection: null,

        // collection or subscription object
        displayName: "",
        urls: null,

        templatePath: dojo.moduleUrl("cosmo", 'ui/widget/templates/SharingDialog.html'),
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
