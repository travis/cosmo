<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
<cosmo:dialogLayout prefix="HomeDirectory.Ticket.New.">

<%@ include file="/WEB-INF/jsp/pim/dojo.jsp" %>
<script language="JavaScript">
dojo.require("dojo.io");

function magicForm() {
  var x = dojo.io.FormBind({
    // reference your form
    formNode: document.forms[0],
	
    load: function(load, data, e) {
      // what to do when the form finishes
      // for example, populate a DIV:
      alert("handling")
    }
  });
}
	
dojo.addOnLoad(magicForm);


</script>
<div class="widgetBorder" style="width:460px; margin-top:24px;">
<div class="widgetContent" style="padding:8px;">

<div class="hd" style="margin-bottom:4px;">
  <fmt:message key="HomeDirectory.Ticket.New.Title">
    <fmt:param value="${ticketForm.path}"/>
  </fmt:message>
</div>

<form method="POST">

  <table cellpadding="3" cellspacing="1" border="0">
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Timeout
      </td>


      <td class="mdData">
        <div class="smData"><cosmo:errmsg property="timeout"/></div>
        <div>
    	  <spring:bind path="ticket.timeout">
        <input type="text" name="timeout" size="8" styleClass="textInput"/>

        seconds<br/>
        (leave blank for infinite)
	      </spring:bind>
        </div>
      </td>

    </tr>
    <tr>
      <td class="mdLabel" style="text-align:right;">
        Privileges
      </td>
      
      <td class="mdData">
        <div class="smData"><cosmo:errmsg property="privileges"/></div>
        <div>

		<spring:bind path="ticket.privileges">
          <input type="radio" name="privileges" value="ro"/> Read
          <input type="radio" name="privileges" value="rw"/> Read/Write
          <input type="radio" name="privileges" value="fb"/> Free-Busy
	    </spring:bind>
        </div>
      </td>
	
    </tr>
  </table>
    <div style="float:left;">
      <button name="_cancel.x" styleClass="buttonInput">
		Cancel
	  </button>
    </div>
  
    <div style="float:right;">
      <input type="submit" styleClass="buttonInput" value="Grant Ticket" />

    </div>
    <br style="clear:both;"/>
  </div>
  <input type="hidden" property="path"/>
</form>

</div>
</div>
</cosmo:dialogLayout>