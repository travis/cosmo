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

dojo.provide("cosmotest.testutils");

dojo.require("cosmo.cmp");
dojo.require("cosmo.util.auth");

cosmotest.testutils = {
    init: function initCosmoTests(/*Array*/ testModules){
        var alltests = jum_new_alltests();
        for (var x = 0; x < testModules.length; x++){
            var moduleName = testModules[x];
            dojo.require(moduleName);
            var module = dojo.evalObjPath(moduleName);
            var functionNames = this.getFunctionNames(module);
            for (var y = 0; y < functionNames.length; y++){
                var functionName = functionNames[y];
                jum_add_test(alltests, moduleName, functionName.split("_")[1], functionName, dojo.evalObjPath(moduleName +"." +functionName) );
            }         
            
        }
        jum.setTests(alltests);
    },
    
    getFunctionNames: function getFunctionNames(scope){
        return jum_get_object_function_names(scope);
    },

    createTestAccount: function(){
        return cosmotest.testutils.createUser("User0");
    },

    createUser: function(username, email){
        cosmo.util.auth.clearAuth();
        var user = {
            password: "testing",
            username: username,
            firstName: username,
            lastName: username,
            email: (email || username) + "@cosmotesting.osafoundation.org"
        };
        cosmo.cmp.signup(user, {
            load: function(type, data, evt){
                cosmo.util.auth.setCred(user.username, user.password);
            },
            error: function(e){
                dojo.debug(e);
            }}, true);
        return user;
    },
    
    cleanupUser: function(user){
        cosmo.util.auth.setCred("root", "cosmo");
        cosmo.cmp.deleteUser(user.username, {handle: function(){}}, true);
        cosmo.util.auth.clearAuth();
    }
}
