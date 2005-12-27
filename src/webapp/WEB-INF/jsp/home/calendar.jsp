<%--
/*
 * Copyright 2005 Open Source Applications Foundation
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
--%>

<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<div class="hd" style="margin-top: 12px;">
  <fmt:message key="HomeDirectory.Calendar.Title">
    <fmt:param value="${Collection.path}"/>
  </fmt:message>
</div>

<div class="vevent" style="margin-top:12px;">
Here is where the list of events goes.
</div>

<p>
Note 1: Values may be in a different language or encoding than the rest
of the page (default UTF-8 US English).
</p>

<p>
Note 2: hCalendar spec doesn't seem to address including timezones for
<strong>dtstart</strong> and <strong>dtend</strong>.
</p>

<div class="hd" style="margin-top: 12px;">
  Original iCalendar
</div>

<pre>
${Calendar}
</pre>
