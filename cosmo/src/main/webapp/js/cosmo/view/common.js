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

dojo.provide('cosmo.view.common');

dojo.require("cosmo.app.pim");

cosmo.view.viewBase = new function () {
  this.isCurrentView = function () {
      return (cosmo.app.pim.currentView == this.viewId);
  };
};

cosmo.view.canvasBase = new function () {
    this.getSelectedEvent = function () {
        var key = cosmo.app.pim.currentCollection.getUid();
        var id = this.selectedEventIdRegistry[key];
        return this.view.itemRegistry.getItem(id);
    };
    this.setSelectedEvent = function (ev) {
        var key = cosmo.app.pim.currentCollection.getUid();
        this.selectedEventIdRegistry[key] = ev.id;
        return true;
    };
    this.clearSelectedEvent = function (ev) {
        var key = cosmo.app.pim.currentCollection.getUid();
        this.selectedEventIdRegistry[key] = '';
        return true;
    };
    this.getSelectedEventId = function () {
        var key = cosmo.app.pim.currentCollection.getUid();
        var id = this.selectedEventIdRegistry[key];
        return id;
    }
};

