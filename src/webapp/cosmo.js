/*
 * Copyright 2005 Open Source Applications Foundation
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

var conH;
var winH;
var footerH = 48;
var browserH = 88;

/* -- browser detect -- */

var isIE = false;
if (navigator.appName.indexOf("Microsoft") != -1) {
    isIE = true;
}

function setFoot() {
    if (isIE) {
        conH = document.body.scrollHeight;
        winH = document.body.clientHeight;
    }
    else {
        conH = document.height;
        winH = window.innerHeight;
    }

    var myHeight = winH - conH - footerH + browserH;
    if (myHeight > 60) {
        var footerSpacer = document.getElementById("footerSpacer");
        footerSpacer.setAttribute('height', myHeight);
    }
}
