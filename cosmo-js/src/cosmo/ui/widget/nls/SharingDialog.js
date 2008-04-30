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
     subscribeHead: "Subscribe",

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
     confirmDelete: "Are you sure you want to delete ${collectionName}? <br/>This will delete all data in this collection.",
     deleteFailed: "Attempt to delete ${collectionName} failed.",

     dismiss: "Done"
 })

/*Main.DeleteCollection.Confirm=
Main.DeleteCollection.Failed=

Main.DeleteSubscription.Confirm=Are you sure you want to unsubscribe from {0}?

Main.CollectionDetails.NameLabel=Name
Main.CollectionDetails.ChangeName=Change Name
Main.CollectionDetails.CalendarLabel=Calendar
Main.CollectionDetails.CollectionAddress=URL
Main.CollectionDetails.For.ics=For .ics Calendar
Main.CollectionDetails.For.atom=For Feed Reader
Main.CollectionDetails.For.chandler=For Chandler Desktop
Main.CollectionDetails.Instructions.1=Copy and paste the URL below into the appropriate application.
Main.CollectionDetails.Instructions.2=(You can hold down the right mouse button to 'copy link location'.)
Main.CollectionDetails.Close=Close
Main.CollectionDetails.SelectYourClient=Subscribe with
Main.CollectionDetails.Delete=Delete
Main.CollectionDetail.Tooltip=View details for this collection.

Main.CollectionDetails.Client.Chandler=Chandler Desktop
Main.CollectionDetails.Client.Outlook=Outlook
Main.CollectionDetails.Client.Evolution=Evolution
Main.CollectionDetails.Client.iCal=Apple iCal
Main.CollectionDetails.Client.Sunbird=Sunbird
Main.CollectionDetails.Client.FeedReader=Feed Reader
Main.CollectionDetails.Client.Download=Download calendar and tasks
Main.CollectionDetails.Client.Other=Other...

Main.CollectionDetails.Instructions.Chandler.1=1. Copy URL to the clipboard.
Main.CollectionDetails.Instructions.Chandler.2=2. Start up Chandler. Go to the 'Share' menu and select 'Subscribe...'.
Main.CollectionDetails.Instructions.Chandler.3=3. Paste the URL into the 'URL' field.
Main.CollectionDetails.Instructions.Chandler.4=4. Click 'Subscribe'.
Main.CollectionDetails.Instructions.icalNotSupported.1=Unfortunately Chandler Server only supports Https connections, which Apple iCal cannot currently access.
Main.CollectionDetails.Instructions.FeedReader.1=Subscribe with the URL below.
Main.CollectionDetails.Instructions.Download.1=Save the task and events in this collection to an .ics file on your desktop. Import the file into your favorite calendar application.
Main.CollectionDetails.na=N/A
Main.CollectionDetails.webcal=WebCal
Main.CollectionDetails.caldav=CalDAV
Main.CollectionDetails.atom=Atom
Main.CollectionDetails.protocolInstructions=Subscribe with the appropriate URL below.
Main.CollectionDetails.Save=Save
Main.CollectionDetails.Help=Help
Main.CollectionDetails.ClickHere=Click Here
Main.CollectionDetails.HelpLink=http://chandlerproject.org/Projects/SubscribeToChandlerServer
Main.CollectionDetails.ChandlerPlugDownload=http://chandlerproject.org/download
Main.CollectionDetails.ChandlerPlug=Don't Have Chandler Desktop? {0}Download here{1}.
Main.CollectionDetails.LinkImageToolTip=Link to '{0}'

*/
