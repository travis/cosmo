<%@
page contentType="text/html; charset=UTF-8" %><%@
page language="java" %><%@
page import="com.metaparadigm.jsonrpc.JSONRPCBridge" %><%@
page import="com.metaparadigm.jsonrpc.test.Test"
%><jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge"
/><jsp:useBean id="testObject" scope="session"
     class="com.metaparadigm.jsonrpc.test.Test"
/><%
   response.setDateHeader ("Expires", 0);
   //JSONRPCBridge.setDebug(true);
   JSONRPCBridge.registerObject("test", testObject);
%>
<%!
  String title = "JSON-RPC-Java Unit Tests";
  String head =
    "    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/unit.css\">\n" +
    "    <script type=\"text/javascript\" src=\"jsonrpc.js\"></script>\n" +
    "    <script type=\"text/javascript\" src=\"unit.js\"></script>\n";
  String onLoad = "onLoad()";
%>
<%@ include file="header.jspinc" %>

    <h2>JSON-RPC-Java Unit Tests</h2>

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <td align="left">
          <input type="button" value="Run Tests" onclick="runTests()" />
        </td>
        <td align="right">
          (
          <input type="checkbox" id="profile" />
          Profile
	  |
          <input type="checkbox" id="async" checked />
          Asynchronous
          )
          &nbsp;
          Max parallel async requests
          <input type="text" id="max_requests" value="8" size="2" />
        </td> 
      <tr>
    </table>

    <p></p>

    <table class="test_table" cellspacing="0">
      <thead>
       <tr>
        <th class="test_th" width="260"><div class="code_heading">Code</div></th>
        <th class="test_th" width="100%"><div class="result_heading">Result</div></th>
        <th class="test_th" width="32"><div class="pass_heading">Pass</div></th>
       </tr>
      </thead>
      <tbody id="tests"></tbody>
    </table>

<%@ include file="footer.jspinc" %>
