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

dojo.provide("cosmo.util.html");

cosmo.util.html.setSelectOptions = function(selectElement, options){
    while (selectElement.firstChild){
       selectElement.removeChild(selectElement.firstChild);
    }
    for (var i = 0; i < options.length; i++) {
        var opt = document.createElement('option');
        opt.value = options[i].value;
        opt.appendChild(document.createTextNode(options[i].text));
        selectElement.appendChild(opt);
    }
};

cosmo.util.html.setSelect = function(sel, val) {
    for (var i = 0; i < sel.options.length; i++) {
        if (sel.options[i].value == val) {
            sel.selectedIndex = i;
        }
    }
};
