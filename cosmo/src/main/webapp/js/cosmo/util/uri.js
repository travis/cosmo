/*
 * Copyright 2007 Open Source Applications Foundation
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
 * @fileoverview uri utility methods
 * @author Travis Vachon mailto:travis@osafoundation.org
 * @license Apache License 2.0
 */

/**
 * @function
 */

dojo.provide("cosmo.util.uri");

cosmo.util.uri.parseQueryString = function (queryString){
	var query = queryString.substring(1);
	var vars = query.split("&");
	var queryDict = {};
	for (var i=0;i<vars.length;i++){
		var pair = vars[i].split("=");
		var key = pair[0];
		var value = pair[1];
		if (!queryDict[key]){
			queryDict[key] = [];
		}
		queryDict[key].push(value);
	
	}
	return queryDict;
}
