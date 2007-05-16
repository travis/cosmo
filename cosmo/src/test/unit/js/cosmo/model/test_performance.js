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

dojo.provide("cosmotest.model.test_performance")
dojo.require("cosmo.model.Item");

function twoGets(item){
    item.getDisplayName();
    item.getDisplayName();   
}

function saveInVar(item){
    var x = item.getDisplayName();
}

cosmotest.model.test_performance = {
    /*
     * This test is meant to be run in the firebug profiler. It should
     * demonstrate which of the two functions above runs faster
     */
    test_twoGetsVsSaveInVar: function (){
        var item = new cosmo.model.Item({displayName: "foo"});
        twoGets(item);
        saveInVar(item);
    }
}


