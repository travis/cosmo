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
dojo.provide('cosmo.view.contextMenu');

dojo.require('dojo.event.*');
dojo.require('cosmo.app.pim');
dojo.require('cosmo.ui.menu');

cosmo.view.contextMenu = new function () {
    var _this = this; // Scopage
    var _menuItem = cosmo.ui.menu.HierarchicalMenuItem;
    this.menu = null;
    this.menuItems = [
        new _menuItem({
          display: "Remove",
          handleClick: function () {
              var currView = cosmo.view.getCurrentView();
              var selItem = currView.canvasInstance.getSelectedItem();
              dojo.event.topic.publish('/calEvent',
                  { action: 'removeConfirm',
                    data: selItem });
          } })
        /*new _menuItem({
          display: "Add to Collection",
          items: [] }) */
    ];
    this.createMenu = function () {
        return cosmo.ui.menu.HierarchicalMenuManager.createContextMenu(
            'calCanvasContext', this.menuItems, { minWidth: 100, maxWidth: 200,
            doBeforeShowing: this._updateCollectionData });
    };
    this._updateCollectionData = function () {
        return false; // Shut off for now
        var collectionMenuItems = [];
        var createCollectiomMenuItem = function (id, coll) {
            var m = new _menuItem({
                display: coll.getDisplayName(),
                handleClick: function () { alert('Not yet implemented.'); } });
            collectionMenuItems.push(m);
        };
        cosmo.app.pim.collections.each(createCollectiomMenuItem);
        _this.menu.items[1].items = collectionMenuItems;
        _this.menu.updateDisplayData();
    };
};

