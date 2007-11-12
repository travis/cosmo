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

dojo.provide("cosmo.util.debug");

//TODO better place for this?
cosmo.util.debug.aliasToDeprecatedFuncion = function (currentFunction, deprecatedName, version){
    var f = function (){
        dojo.deprecated(deprecatedName, null, version);
        return currentFunction.apply(this, arguments);
    }
    var p = currentFunction.prototype; // to help dojo builder
    eval("window." + deprecatedName +  " = f");
    eval("window." + deprecatedName + ".prototype = p");
};

cosmo.util.debug.genericToString = function (){
    var str = "";
    for (var propName in this){
        var prop = this[propName];
        if (typeof prop != "function"){
            str += propName + ": '" + prop + "'; ";
        }
    }
    return str;
};

cosmo.util.debug.dumpIntoPopup = function (o) {
    var errorWin;
    var str = '';
    if (typeof o != 'string') {
        for (var p in o) {
            str += p + ': ' + o[p] + ' (' + typeof o[p] + ')<br/>';
        }
    }
    else {
        str = o;
    }
    // Create new window and display error
    try {
      errorWin = window.open('', 'errorWin');
      errorWin.document.body.innerHTML = str;
    }
    // If pop-up gets blocked, inform user
    catch(e) {
      alert('An error occurred, but the error message cannot be' +
      ' displayed because of your browser\'s pop-up blocker.\n' +
      'Please allow pop-ups from this Web site.');
    }
};

Timer = function (functionName) {
   this.functionName = functionName;
   var now = new Date();
   this.startTime = now.getTime();
   this.endTime = 0;
   dojo.debug("Start function '" + functionName + "'");
};

Timer.prototype.end = function (message){
    message = message || this.functionName;
    var now = new Date();
    this.endTime = now.getTime();
    var elapsedTime = this.endTime - this.startTime;
    dojo.debug("End function '" + message + "'; elapsedTime: "
        + elapsedTime + "ms");
};

Timer.prototype.toString = cosmo.util.debug.genericToString;

function timeFunction(object, functionName){
   var oldMethod = object[functionName];
   object[functionName] = function (){
       var timer = new Timer(functionName);
       var result = oldMethod.apply(object, arguments);
       timer.end();
       return result;
   }
};

//deprecate old version sans namespace
genericToString = function (){
    dojo.deprecated("genericToString", "Use cosmo.util.debug.genericToString instead", "Version 0.6 Final Release");
    return cosmo.util.debug.genericToString.apply(this, arguments);
};
