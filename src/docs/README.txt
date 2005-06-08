
                            Cosmo Sharing Server 0.2


   Copyright 2005 Open Source Applications Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


WHAT IS COSMO?
==============

Cosmo is intended for people who want to share files, calendars,
events, tasks, contacts, and things of that nature between multiple
machines and multiple people. It speaks WebDAV and eventually also
CalDAV. It allows individual resources to be completely private,
accessible to specific persons, or available to the entire world. It
includes an HTML user interface for administration and for individual
account signup and management.

Cosmo is a sharing server. It's smarter than a garden-variety WebDAV
server, because it understands certain characteristics of some of the
content that can be stored within it (like calendars, for
instance). This allows the server to present multiple equivalent views
of the same resource for different clients (ex. monolithic iCalendar
calendar for Apple's iCal, individual iCalendar calendars and events
for CalDAV clients, HTML indexes for web browsers).

Cosmo is not a web-based personal information manager. It is a server
that such a web application could use to access people's stored
information via standard Internet protocols. In fact, a web-based PIM
is the purview of a separate OSAF project (currently code-named
"Scooby").

Cosmo is definitely not a full-on content management system. There is
no support for features such as content editing or workflow. Cosmo
simply acts as a mediator between many different types of clients and
a content repository.


MORE INFO
=========

Instructions for building Cosmo from source and for installing and
running Cosmo are found in BUILD.txt and INSTALL.txt respectively.

Notes and known issues for the current release are found in
RELEASE-NOTES.txt.

Legal information is found in LICENSE.txt.

Cosmo's project page is at
<http://wiki.osafoundation.org/bin/view/Journal/CosmoTempHome>.

Issues are tracked at <http://bugzilla.osafoundation.org/>.

Feel free to ask questions and report problems to
cosmo@osafoundation.org.
