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

dojo.require("dojo.html.common");
dojo.require("dojo.event.*");
dojo.require("cosmo.app");
dojo.require("cosmo.app.pim");

cosmo.ui.resize_area.dragSize = null;

cosmo.ui.resize_area.ResizeAreaAdjacent = function (div) {
    this.div = div;
}

cosmo.ui.resize_area.ResizeArea = function (content, handle) {

    var self = this;

    this.id = content.id;
    this.contentDiv = content;
    this.handleDiv = handle;
    this.direction = 'down';
    this.dragSize = 0;
    this.adjacentArea = [];
    // Offset used to calc the local Y position of the cursor
    this.absTop = TOP_MENU_HEIGHT + CAL_TOP_NAV_HEIGHT;
    // Ensures the cursor sits in the middle of the
    // drag handle as you drag
    this.handleCenteringSpace = ALL_DAY_RESIZE_HANDLE_HEIGHT / 2;
    // Upward dragging constraint -- 2px fudge factor
    // This value is relative, local Y
    this.upConstraint = (DAY_LIST_DIV_HEIGHT - 2);
    // Downward dragging constraint -- 2px fudge factor
    // This value is relative, local Y
    this.downConstraint = (dojo.html.getViewport().height -
        this.absTop - 2);

    this.init = function(dir, off) {
        var dragSize =  cosmo.ui.resize_area.dragSize;
        dragSize = typeof dragSize == 'number' ?
            dragSize : ALL_DAY_RESIZE_AREA_HEIGHT;
        this.direction = dir ? dir : this.direction;
        this.dragSize = dragSize;
        dojo.event.connect(self.handleDiv, 'onmousedown',
            function () { cosmo.app.dragItem = self });
    };
    // Right now the only adjacent area is the timed canvas
    this.addAdjacent = function(node) {
        this.adjacentArea.push(
            new cosmo.ui.resize_area.ResizeAreaAdjacent(node));
    };
    this.doDrag = function () {
        this.resize();
    };
    this.resize = function() {
        var localY = yPos - this.absTop;
        // The context area is a sub-div that's got the day
        // list sitting up above it
        var contentAreaSize = localY - DAY_LIST_DIV_HEIGHT;
        // Pull the top of the resize handle up by half its
        // vertical size, so the cursor sits in the middle
        // while dragging
        var handleTop = localY - this.handleCenteringSpace;
        // Hide the contents during drag for IE6
        if (navigator.appVersion.indexOf('MSIE 6') > -1) {
            self.contentDiv.style.display = 'none';
        }
        if (handleTop > this.upConstraint &&
            handleTop < this.downConstraint) {
            this.contentDiv.style.height = contentAreaSize + 'px';
            // Save the size -- needed to hard-code the height
            // of the inner divs for IE6, done in the drop method
            this.dragSize = contentAreaSize;
            this.handleDiv.style.top = handleTop + 'px';
            for (var i = 0; i < this.adjacentArea.length; i++) {
                // Place the timed event canvas below the resize
                // handle
                var pos = (handleTop + ALL_DAY_RESIZE_HANDLE_HEIGHT + 1);
                // Size of the timed event canvas should be the bottom
                // local Y px of the browser window, minus the top local
                // Y pos of the timed event canvas
                var size = (this.downConstraint - pos);
                // There may be some overlap at the bottom -- don't
                // resize timed canvas below 0px
                size = size < 0 ? 0 : size;
                // Re-position and resize the timed canvas
                var div = this.adjacentArea[i].div;
                div.style.top = pos + 'px';
                div.style.height = size + 'px';
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
        // Store the current size for canvas re-rendering
        cosmo.ui.resize_area.dragSize =
            (this.dragSize - this.handleCenteringSpace);
    };
}

