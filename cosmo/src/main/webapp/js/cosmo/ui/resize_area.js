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

dojo.provide("cosmo.ui.resize_area");

dojo.require("cosmo.app");
dojo.require("cosmo.app.pim");

cosmo.ui.resize_area.ResizeAreaAdjacent = function (div, origPos, origSize) {
    this.div = div;
    this.origPos = origPos;
    this.origSize = origSize;
}
ResizeAreaAdjacent = cosmo.ui.resize_area.ResizeAreaAdjacent;

cosmo.ui.resize_area.ResizeArea = function (id, handleId) {

    var self = this;

    this.id = id;
    this.handleId = handleId;
    this.contentDiv = null;
    this.handleDiv = null;
    this.direction = 'down';
    this.size = 0;
    this.dragLimit = 0;
    this.origSize = 0;
    this.dragSize = 0;
    this.adjacentArea = [];

    this.init = function(dir, off) {
        self.contentDiv = document.getElementById(this.id);
        self.handleDiv = document.getElementById(this.handleId);
        self.handleDiv.onmousedown = function() { cosmo.app.dragItem = self };
        self.direction = dir ? dir : this.direction;
        self.origSize = this.getHeight(this.contentDiv) +
            self.getHeight(this.handleDiv);
        self.dragSize = self.origSize;
        if (navigator.appVersion.indexOf('MSIE 6') > -1) {
            self.contentDiv.style.height = this.dragSize + 'px';
        }
    };
    this.addAdjacent = function(id) {
        var div = document.getElementById(id);
        var incr = this.adjacentArea.length;
        this.adjacentArea[incr] = new ResizeAreaAdjacent(
            div, this.getAbsTop(div), this.getHeight(div));
    };
    this.setDragLimit = function() {
        /*
        this.dragLimit = this.adjacentArea[0].origPos +
            this.adjacentArea[0].origSize - TOP_MENU_HEIGHT -
            ALL_DAY_RESIZE_HANDLE_HEIGHT;
        */
        // FIXME
        this.dragLimit = 9999999;
    };
    this.compareNumbers = function(a, b) { return a - b };
    this.doDrag = function () {
        this.resize();
    };
    this.resize = function() {
        var offset = this.contentDiv.offsetTop;
        var pos = yPos - TOP_MENU_HEIGHT;
        var size = (pos - offset);
        var div = null;
        if (navigator.appVersion.indexOf('MSIE 6') > -1) {
            self.contentDiv.style.display = 'none';
        }
        if (pos > offset && pos < this.dragLimit) {
            this.contentDiv.style.height = size + 'px';
            this.dragSize = (size + ALL_DAY_RESIZE_HANDLE_HEIGHT);
            this.handleDiv.style.top = pos + 'px';
            for (var i = 0; i < this.adjacentArea.length; i++) {
                div = this.adjacentArea[i].div;
                div.style.top = (pos+8) + 'px';
                div.style.height = (((this.adjacentArea[i].origPos-yPos) +
                    this.adjacentArea[i].origSize) - 8 + TOP_MENU_HEIGHT) + 'px';
            }
        }
    };
    this.drop = function() {
        // IE6 -- workaround z-index issue by actually truncating
        // inner content div height to visible height of area
        if (navigator.appVersion.indexOf('MSIE 6') > -1) {
            self.contentDiv.style.height = this.dragSize + 'px';
            self.contentDiv.style.display= 'block';
        }
    };
    this.getAbsTop = function(div) {
        return div.offsetTop + cosmo.app.pim.top;
    };
    this.getHeight = function(div) {
        return div.offsetHeight;
    };
    this.cleanup = function() {
        self.contentDiv = null;
        self.handleDiv.onmousedown = null;
        self.handleDiv = null;
        for (var i = 0; i < self.adjacentArea.length; i++) {
            self.adjacentArea[i].div = null;
        }
    };
}

