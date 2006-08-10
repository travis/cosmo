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

function ResizeAreaAdjacent(div, origPos, origSize) {
    this.div = div;
    this.origPos = origPos;
    this.origSize = origSize;
}

function ResizeArea(id, handleId) {
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
        this.contentDiv = document.getElementById(this.id);
        this.handleDiv = document.getElementById(this.handleId);
        this.direction = dir ? dir : this.direction;
        this.origSize = this.getHeight(this.contentDiv) + 
            this.getHeight(this.handleDiv);
        this.dragSize = this.origSize;
    };
    this.addAdjacent = function(id) {
        var div = document.getElementById(id);
        var incr = this.adjacentArea.length;
        this.adjacentArea[incr] = new ResizeAreaAdjacent(
            div, this.getAbsTop(div), this.getHeight(div));
    };
    this.setDragLimit = function() {
        this.dragLimit = this.adjacentArea[0].origPos + 
            this.adjacentArea[0].origSize - TOP_MENU_HEIGHT - 
            ALL_DAY_RESIZE_HANDLE_HEIGHT;
    };
    this.compareNumbers = function(a, b) { return a - b };
    this.resize = function() {
        var offset = this.contentDiv.offsetTop;
        var pos = yPos - TOP_MENU_HEIGHT;
        var size = (pos - offset);
        var div = null;
        if (pos > offset && pos < this.dragLimit) {
            this.contentDiv.style.height = size + 'px';
            this.dragSize = (size + ALL_DAY_RESIZE_HANDLE_HEIGHT);
            this.handleDiv.style.top = pos + 'px';
            for (var i = 0; i < this.adjacentArea.length; i++) {
                div = this.adjacentArea[i].div;
                div.style.top = (pos+8) + 'px';
                div.style.height = (((this.adjacentArea[i].origPos-yPos) + 
                    this.adjacentArea[i].origSize) - 8) + 'px';
            }
        }
    };
    this.drop = function() {
        // Do nothing
        // This function is called when ResizeArea is the draggable
        // So drop method must be defined here
    };
    this.getAbsTop = function(div) {
        return div.offsetTop + Cal.top;
    };
    this.getHeight = function(div) {
        return div.offsetHeight;
    };
    this.cleanup = function() {
        this.contentDiv = null;
        this.handleDiv = null;
        for (var i = 0; i < this.adjacentArea.length; i++) {
            this.adjacentArea[i].div = null;
        }
    };
}
