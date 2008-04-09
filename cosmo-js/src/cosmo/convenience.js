if(!dojo._hasResource["cosmo.convenience"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["cosmo.convenience"] = true;
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

dojo.require("cosmo.util.i18n");

dojo.provide("cosmo.convenience");

dojo.global.$ = function (s) {
    return document.getElementById(s);
}
dojo.global._createElem = function (s) {
    return document.createElement(s);
}
dojo.global._createText = function (s) {
    return document.createTextNode(s);
}
dojo.global._ = dojo.hitch(cosmo.util.i18n, cosmo.util.i18n.getText);

}
