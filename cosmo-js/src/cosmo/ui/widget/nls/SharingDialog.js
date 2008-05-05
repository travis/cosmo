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
     subscribeHead: "Subscribe with...",

     atom: "Feed Reader",
     iCal: "Apple iCal",
     dav: "CalDAV",
     chandler: "Chandler Desktop",
     html: "Link here",

     feedReaderLabel: "Feed Reader",
     chandlerLabel: "Chandler Desktop",
     iCalLabel: "Apple iCal",
     webLabel: "Web",
     davLabel: "CalDAV",

     chandlerInstructions: "<a href='${mc}' onclick='return false'>${mc}</a>"
                           + "<ol><li>From Chandler Desktop, go to <b>Share>>Subscribe</b></li>"
                           + "<li>Paste the above link into the <b>URL:</b> field</li>"
                           + "<li>Click <b>[Subscribe]</b></li></ol>",
     feedReaderInstructions: "<a href='${atom}' target='_blank'>${atom}</a>"
                             + "<p>Click on the above link to subscribe with web based feed readers like Google Reader or Bloglines, "
                             + "or copy and paste it into your favorite feed reader.</p>",
     iCalInstructions: "<a href='${webcalProtocol}' target='_blank'>${webcal}</a>" +
         "<p>Click on the above link to subscribe to this collection using any version of iCal.</p>" +
         "<p>If that does not work, copy the above URL to the clipboard and paste it into the \"Subscribe\" dialog in iCal.</p>" +
         "<p>If you are using iCal 3 and would like to sync and edit collections, see the settings dialog for more information.</p>",

     davInstructions: "<a href='${dav}' onclick='return false'>${dav}</a>"
                      + "<p>Paste the above link into a CalDAV client that supports individual collection subscriptions.</p>"
                      + "<p>To sync and edit all collections in your account using CalDAV, see the CalDAV account setup intructions"
                      + "in the settings dialog.</p>",

     chandlerInstructionsTitle: "Subscribe with Chandler Desktop",
     feedReaderInstructionsTitle: "Subscribe with a feed reader",
     iCalInstructionsTitle: "Subscribe with Apple iCal",
     davInstructionsTitle: "Subscribe with a CalDAV client",

     htmlLinkTitle: "Link to this collection",
     atomSubscribeTitle: "Feed reader URL",
     iCalSubscribeTitle: "Apple iCal",
     davSubscribeTitle: "CalDAV Subscribe Instructions",
     chandlerSubscribeTitle: "Chandler Desktop Subscribe Instructions",

     invite: "Invite",
     inviteInstructions: "Give out the URLs below to invite others to subscribe.",
     inviteReadOnly: "View-only",
     inviteReadWrite: "View and edit",

     download: "Download",

     deleteCollection: "Delete",
     deleteFailed: "Attempt to delete ${collectionName} failed.",

     dismiss: "Done"
 })
