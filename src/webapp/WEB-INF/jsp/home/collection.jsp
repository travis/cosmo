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

<div class="widgetBorder" style="width:460px; margin-top:24px;">
<div class="widgetContent" style="padding:8px;">

<div class="hd" style="margin-bottom:4px;">
  <fmt:message key="HomeDirectory.Collection.Title">
    <fmt:param value="${Collection.path}"/>
  </fmt:message>
</div>

<table cellpadding="3" cellspacing="1" border="0">
  <tr>
    <td class="mdLabel" style="text-align:right;">
      <fmt:message key="HomeDirectory.Collection.DisplayName"/>
    </td>
    <td class="mdData">
      ${Collection.displayName}
    </td>
  </tr>
</table>

</div>
</div>
