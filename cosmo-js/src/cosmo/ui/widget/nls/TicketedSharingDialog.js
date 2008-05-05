/*
 * Copyright 2006-2008 Open Source Applications Foundation
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
({
     iCalInstructions: "<a href='${webcalProtocol}' target='_blank'>${webcal}</a>"
         +"<p>Click on the above link to subscribe <b>view-only</b> to this collection with Apple iCal. "
         + "If that fails, from Apple iCal, <br/> go to <b>Calendar>>Subscribe...</b> and paste this link "
         + "into the <b>URL:</b> field.</p>",

     davInstructions: "<a href='${dav}' onclick='return false'>${dav}</a>"
         + "<p>Paste the above link into a CalDAV client that supports individual collection subscriptions.</p>"
 })
