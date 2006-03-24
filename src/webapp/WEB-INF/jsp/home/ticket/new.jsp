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
  <fmt:message key="HomeDirectory.Ticket.New.Title">
    <fmt:param value="${ticketForm.path}"/>
  </fmt:message>
</div>

<html:form method="POST" action="/home/ticket/grant">

  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Timeout
      </td>
      <td class="mdData">
        <div class="smData"><cosmo:errmsg property="timeout"/></div>
        <div>
        <html:text property="timeout" size="8" styleClass="textInput"/>
        seconds<br/>
        (leave blank for infinite)
        </div>
      </td>
    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Privileges
      </td>
      <td class="mdData">
        <div class="smData"><cosmo:errmsg property="readWrite"/></div>
        <div>
          <html:radio property="readWrite" value="false"/> Read
          <html:radio property="readWrite" value="true"/> Read/Write
        </div>
      </td>
    </tr>
  </table>
  
  <div style="margin-top:12px;">
    <div style="float:left;">
      <html:cancel styleClass="buttonInput">
        Cancel
      </html:cancel>
    </div>
    <div style="float:right;">
      <html:submit property="create" styleClass="buttonInput">
        Grant Ticket
      </html:submit>
    </div>
    <br style="clear:both;"/>
  </div>
  <html:hidden property="path"/>
</html:form>

</div>
</div>
