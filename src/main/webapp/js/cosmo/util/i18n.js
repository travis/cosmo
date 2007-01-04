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
 * Provides i18n support.
 */

dojo.provide("cosmo.util.i18n");

cosmo.util.i18n.loadI18n = function(uri){
	var s = dojo.hostenv.getText(uri);
	cosmo.util.i18n._localtext = eval("(" + s + ")");
}

cosmo.util.i18n.init = function(uri){
    cosmo.util.i18n.loadI18n(uri);
}

function getText(str) {
    return cosmo.util.i18n._localtext[str] || "[[" + str + "]]";
}

cosmo.util.i18n.getText = getText
if (djConfig['i18nLocation']){
	cosmo.util.i18n.init(djConfig['i18nLocation']);
}
