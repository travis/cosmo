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

Log = new function() {
    dojo.deprecated("cosmo.util.log",
    "cosmo.util.log no longer needed", "0.6");


    // No-console browsers
    if (typeof console == 'undefined') {
         this.print = function() {
            alert(arguments[0]);
         }
    }
    else {
        this.print = function() {
            console.log(arguments);
        }
    }
}

