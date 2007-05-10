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
 
dojo.provide("cosmo.ui.timeout");

cosmo.ui.timeout.lastActionTime = new Date();

// Timeout in milliseconds
cosmo.ui.timeout.TIMEOUT = 30*1000*60;

cosmo.ui.timeout.timedOut = function timedOut(){
    var now = new Date();
    return (now.getTime() > this.lastActionTime + this.TIMEOUT);
}

cosmo.ui.timeout.updateLastActionTime = function updateLastActionTime(){
    this.lastActionTime = new Date();
}