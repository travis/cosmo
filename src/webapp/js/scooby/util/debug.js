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
  * Various functions to be used during development and debugging.
  *
  */
  
function genericToString(){ 
    var str = ""; 
    for (var propName in this){ 
        var prop = this[propName]; 
        if (typeof prop != "function"){ 
            str += propName + ": '" + prop + "'; "; 
        } 
    } 
    return str; 
}

Timer = function(functionName) {
   this.functionName = functionName;
   var now = new Date();
   this.startTime = now.getTime();
   this.endTime = 0;
   log.debug("Start function '" + functionName + "'");
}

Timer.prototype.end = function(){
    var now = new Date();
    this.endTime = now.getTime();
    var elapsedTime = this.endTime - this.startTime;
    log.debug("End function '" + this.functionName + "'; elapsedTime: " 
        + elapsedTime + "ms");
}

Timer.prototype.toString = genericToString;

function timeFunction(object, functionName){
   var oldMethod = object[functionName];
   object[functionName] = function(){
       var timer = new Timer(functionName);
       var result = oldMethod.apply(object, arguments);
       timer.end();
       return result;
   }
}
