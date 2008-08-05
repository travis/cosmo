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
dojo.provide("cosmo.ui.widget.About");

dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");

dojo.declare("cosmo.ui.widget.About", [dijit._Widget, dijit._Templated], {
        templateString: '<span></span>',

        // Props from template or set in constructor

        // Localized strings
        strings: {
            license: _('About.License', '<a href="' + _('About.LicenseLink') + '">', '</a>'),
            info: _('About.Info', '<a href="' + _('About.InfoLink') + '">', '</a>')
        },

        // Attach points

        postCreate: function () {
            var node = this.domNode
            var main = null;
            var d = null;

            node.id = this.id;
            node.style.textAlign = 'center';
            node.style.margin = 'auto';
            node.style.width = '100%';
            node.style.height = "230px";
            node.style.overflowY = 'auto';

            // Image
            d = _createElem('div');
            var img = _createElem('img');
            img.src = cosmo.env.getImageUrl( _("About.LogoUri"));
            d.appendChild(img);
            node.appendChild(d);
            // Version
            d = _createElem('div');
            d.style.marginTop = '4px';
            d.innerHTML = _('About.Version', cosmo.env.getVersion());
            node.appendChild(d);
            // License text
            d = _createElem('div');
            d.style.marginTop = '12px';
            d.innerHTML = this.strings.license;
            node.appendChild(d);
            // Info text
            d = _createElem('div');
            d.style.marginTop = '2px';
            d.innerHTML = this.strings.info;
            node.appendChild(d);

            d = _createElem('div');
            d.className = "notices";
            var noticesDeferred = dojo.xhrGet({url: cosmo.env.getFullUrl("Notices")});
            noticesDeferred.addCallback(function(str){
                d.innerHTML = str;
            });
            node.appendChild(d);
        }
});



