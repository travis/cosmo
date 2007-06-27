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
 * @fileoverview Calendar events -- links the Lozenge to the CalItemData
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.view.list.ListItem");

dojo.require("cosmo.app.pim");
dojo.require("cosmo.model");
dojo.require("cosmo.view.BaseItem");
dojo.require('cosmo.view.list.common');

cosmo.view.list.ListItem = function () {
    this.sort = null;
    this.display = null;
    this.data = null;
}
cosmo.view.list.ListItem.prototype = new cosmo.view.BaseItem();

cosmo.view.list.ListItem.prototype.cancelSave = function () {
    this.restoreFromSnapshot();
};



