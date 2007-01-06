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

dojo.require("cosmo.topics");

function test_publish(){
   var success = false;
   
   var myMessage = null;
   function f(/*cosmo.topics.CollectionUpdatedMessage*/ message){
       success = true;
       myMessage = message;
   }
   
   dojo.event.topic.subscribe(cosmo.topics.CollectionUpdatedMessage.topicName,null, f);
   cosmo.topics.publish(cosmo.topics.CollectionUpdatedMessage, [{cid: "123456"}]);
   
   while (success == false){
   }
   
   jum.assertEquals("123456", myMessage.collection.cid);
   
   
}
