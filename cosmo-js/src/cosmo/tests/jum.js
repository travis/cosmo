/*
 * Copyright 2008 Open Source Applications Foundation
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
dojo.provide("cosmo.tests.jum");

(function(){
// Object to provide compatibility with old jum framework
var JUM = function(){
};

function _JUM_first_arg_string_func(n, name){
    return function(){
        var args = arguments;
        var s = null;
        if (arguments.length == n){
            args = arguments;
        } else if (arguments.length == n + 1){
            s = arguments[0];
            args = Array.prototype.slice.apply(arguments, [1]);
        }
        try {
            return doh[name].apply(doh, args);
        } catch (e){
            console.log("Test failure message was: " + s);
            throw e;
        }
    };
}

JUM.prototype = {
    assertTrue: _JUM_first_arg_string_func(1, "assertTrue"),
    assertFalse: _JUM_first_arg_string_func(1, "assertFalse"),
    assertEquals: _JUM_first_arg_string_func(2, "assertEqual")
}

dojo.global.jum = new JUM();
})();