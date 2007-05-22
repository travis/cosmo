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
dojo.provide('cosmo.ui.event.listeners');

dojo.require('dojo.event.browser');
dojo.require('cosmo.ui.event.handlers');

// Event listeners
// ==============================
cosmo.ui.event.listeners.hookUpListeners = function (){
	document.ondblclick = cosmo.ui.event.handlers.dblClickHandler;
	document.onmousemove = cosmo.ui.event.handlers.mouseMoveHandler;
	document.onmousedown = cosmo.ui.event.handlers.mouseDownHandler;
	document.onmouseup = cosmo.ui.event.handlers.mouseUpHandler;
	document.onkeyup = cosmo.ui.event.handlers.keyUpHandler;

	dojo.event.browser.addListener(window, "onload", cosmo.app.init, false);
	dojo.event.browser.addListener(window, "onunload", cosmo.ui.event.handlers.cleanup, false);
	dojo.event.browser.addListener(window, "onresize", cosmo.ui.event.handlers.resize, false);
}
