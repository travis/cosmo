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

dojo.provide("cosmo.util.popup");

cosmo.util.popup = new function () {
    var self = this;
    // Reference to the pop-up window
    this.win = null;
    /**
     * Opens a new pop-up window -- optional final
     * param allows turning on the scrollbars for the window
     */
    this.open = function (url, w, h, scroller) {
        var scr = scroller ? 1 : 0;
        if(!self.win || self.win.closed) {
            self.win = null;    
            self.win = window.open(url, 'thePopupWin', 'width=' + w + ',height=' + 
                h + ',location=0,menubar=0,resizable=1,scrollbars=' + scr + 
                ',status=0,titlebar=1,toolbar=0');
        }
        else {	    
            self.win.focus(); 
            self.win.document.location = url;
        }
    };
    /**
     * Sends the opener of the window to a new location
     * and closes the pop up.     
     */
    this.goURLMainWin = function (url) {
        location = url;
        self.win.window.close();
    };
}

