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

dojo.provide('cosmo.view.list.sort');

cosmo.view.list.sort.doSort = function (hash, columnName, direction) {
    var column = camelCase(columnName);
    function compare(a, b){return compareOnSortColumn(a, b, column);}
    hash.sort(compare, direction);
    return true;
};

function camelCase(s){
    return s.charAt(0).toLowerCase() + s.substr(1);
}

function compareOnSortColumn(a, b, column) {
    var valA = a.sort[column];
    var valB = b.sort[column];
    if (typeof valA == 'string') valA = valA.toLowerCase();
    if (typeof valB == 'string') valB = valB.toLowerCase();
    if (valA == valB){ // order of precedence column, title, uid
        if (column == 'title')    return compareOnSortColumn(a, b, 'uid');
        else if (column != 'uid') return compareOnSortColumn(a, b, 'title');
        else return 0;
    }
    return (valA > valB) ? 1 : -1;
}
