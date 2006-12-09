/*
 * Copyright 2006 Open Source Applications Foundation
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
 * @fileoverview ModalDialog -- modal dialog, including full-window
 * masking div to prevent user from interacting with underlying
 * doc when dialog is showing. Content area can be a string of
 * HTML or a DOM node to insert. If the title property is not
 * empty, the dialog has a rectangular title bar at the top.
 * the defaultAction prop should be a function object to be
 * executed by default from the Enter key if the dialog is being
 * displayed.
 * @author Bobby Rullo br@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.CollectionDetailsDialog");

dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.ui.widget.ModalDialog");

dojo.widget.defineWidget("cosmo.ui.widget.CollectionDetailsDialog", 
dojo.widget.HtmlWidget, {
        // Template stuff
        templatePath:dojo.uri.dojoUri(
            '../../cosmo/ui/widget/templates/CollectionDetailsDialog/CollectionDetailsDialog.html'),
        
        // Attach points

        // Instance methods
        
        // Lifecycle functions
        postMixInProperties: function () { } }
 );
