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
     displayNameTitle: "Double click to change display name",

     subscribeHead: "Subscribe with...",

     atom: "Feed Reader",
     iCal: "Apple iCal",
     dav: "CalDAV",
     chandler: "Chandler Desktop",
     html: "Link",

     feedReaderLabel: "Feed Reader",
     chandlerLabel: "Chandler Desktop",
     iCalLabel: "Apple iCal",
     webLabel: "Web",
     davLabel: "CalDAV",

     chandlerInstructions: "<a href='${mc}' onclick='return false'>${mc}</a>"
                           + "<div style='margin: 1em 0em 0em 2em'><ol>"
                           + "<li>From Chandler Desktop, go to <b>Share>>Subscribe</b></li>"
                           + "<li>Paste the above link into the <b>URL:</b> field</li>"
                           + "<li>Click <b>[Subscribe]</b></li>"
                           + "</ol></div>",
     feedReaderInstructions: "<a href='${atom}' target='_blank'>${atom}</a>"
                             + "<p>Click on the above link to subscribe with web based feed readers like Google Reader or Bloglines, <br/>"
                             + "or copy and paste it into your favorite feed reader.</p>",
     iCalInstructions: "<a href='${webcalProtocol}' target='_blank'>${webcal}</a>"
         +"<p>Click on the above link to subscribe <b>view-only</b> to this collection with Apple iCal. "
         + "If that fails,<br/> from Apple iCal, go to <b>Calendar>>Subscribe...</b> and paste this link "
         + "into the <b>URL:</b> field.</p>"
         + "<p><b>Apple iCal 3.x (Leopard) Users:</b>  Sync your account and gain <b>view and edit</b> "
         + "access to<br/> all of your calendars from Apple iCal. See <a href='http://chandlerproject.org/Projects/GetStarted#Sync%20with%20Apple%20iCal,%20Sunbird...'>instructions</a>.</p>",

     davInstructions: "<a href='${dav}' onclick='return false'>${dav}</a>"
         + "<p>Paste the above link into a CalDAV client that supports individual collection subscriptions.</p>"
         + "<p>To sync and edit all collections in your account see "
         + "<a href=http://chandlerproject.org/Projects/UsingOtherAppsWithChandlerHub>instructions</a>.</p>",

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
     inviteReadWrite: "View and Edit",

     download: "Download",

     dismiss: "Done"
 })
