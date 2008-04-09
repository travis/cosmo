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
 * @fileoverview Text -- text-processing functions
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

/**
 * @object Text -- object to provide some basic text processing
 * features, uppercasing, formatting number, etc.
 */

Text = new function() {
    dojo.deprecated("cosmo.util.text",
    "cosmo.util.text no longer needed", "0.6");

    this.uppercaseFirst = function(str) {
        first = str.substr(0, 1).toUpperCase();
        return first + str.substr(1);
    }
}

Text.constructor = null;
