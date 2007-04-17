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

dojo.require("cosmoTest.rpc.MockJsonService");
dojo.require("cosmoTest.service.conduits.*");
dojo.require("cosmo.service.conduits.jsonrpc");
function test_conduit(){
    var s = new cosmoTest.rpc.MockJsonService();
    s.processSmd(cosmo.rpc.JsonService.defaultSMD);
    var currentUserConduit = new cosmo.service.conduits.jsonrpc.CurrentUserConduit(s);
    cosmoTest.service.conduits.testAbstractConduit(currentUserConduit);
    cosmoTest.service.conduits.testAbstractCurrentUserConduit(currentUserConduit);
}
