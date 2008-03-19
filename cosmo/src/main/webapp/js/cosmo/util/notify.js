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

/**
 * Provides lightweight notifications a-la-enso.
 * Inspiration found here:
 * http://www.humanized.com/weblog/2006/09/11/monolog_boxes_and_transparent_messages/
 */

dojo.provide("cosmo.util.notify");

cosmo.util.notify = {

    MESSAGE_DIV_ID: "cosmoTransparentMessage",

    _listeners: [],
    _messageDiv: null,

    showMessage: function(/*String*/ message){
        console.log(message);
        var d = this._getDiv();
        d.innerHTML = message;
        d.style.zIndex = 1000;
        dojo.fadeIn({node: d, duration: 1}).play();
        this._connectListeners();
    },

    _connectListeners: function(){
        var hideEvents = ["onmousedown", "onkeydown", "onmousemove"];
        for (var i in hideEvents){
            this._listeners.push(dojo.connect(dojo.body(), hideEvents[i], cosmo.util.notify, "_hideMessage"));
        }
    },
    
    _disconnectListeners: function(){
        for (var i in this._listeners){
            dojo.disconnect(this._listeners[i]);
        }
        this._listeners = [];
    },
    
    _hideMessage: function(e){
        this._disconnectListeners();
        var d = this._getDiv()
        dojo.fadeOut({node: d,
                      onEnd: function(){d.style.zIndex = -1}}).play();
    },

    _getDiv: function(){
        return this._messageDiv || document.getElementById(this.MESSAGE_DIV_ID) ||
            this._createDiv();
    },
    
    _createDiv: function(){
        var d = document.createElement("div");
        d.id = this.MESSAGE_DIV_ID;
        dojo.fadeOut({node: d, duration: 1}).play();
        dojo.body().appendChild(d);
        this._messageDiv = d;
        return d;
    }
    
}