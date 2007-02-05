<%@
page contentType="text/html; charset=UTF-8" %><%@
page language="java" %><%@
page import="com.metaparadigm.jsonrpc.test.Test"
%><jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge"
/><jsp:useBean id="testObject" scope="session"
     class="com.metaparadigm.jsonrpc.test.Test"
/><%
   response.setDateHeader ("Expires", 0);
   JSONRPCBridge.enableReferences();
   JSONRPCBridge.registerObject("test", testObject);
   JSONRPCBridge.registerReference(Test.RefTest.class);
   JSONRPCBridge.registerCallableReference(Test.CallableRefTest.class);
%>
<%!
String title = "JSON-RPC-Java Tests";
String head = "    <script type=\"text/javascript\" src=\"jsonrpc.js\"></script>" +
              "    <script type=\"text/javascript\" src=\"test.js\"></script>";
String onLoad = "onLoad()";
%>
<%@ include file="header.jspinc" %>

    <h2>JSON-RPC-Java Tests</h2>

    <h3>Test Output</h3>
    <table cellpadding="4" cellspacing="0" width="100%" style="border: 1px solid #c0c0c0; border-collapse: collapse;">
      <tr>
       <td>
        <textarea wrap="off" id="result" cols="80" rows="24"></textarea>
       </td>
       <td valign="top">
        <h3>Tests</h3>
        <p><a href="javascript:doListMethods();">List Methods</a><br>
        <a href="javascript:doBasicTests();">Basic Tests</a><br>
        <a href="javascript:doReferenceTests();">Reference Tests</a><br>
        <a href="javascript:doContainerTests();">Container Tests</a><br>
        <a href="javascript:doExceptionTest();">Exception Test</a></p>
        <h3>Debug</h3>
        <p><a href="javascript:setDebug(true);">Debug On</a><br>
        <a href="javascript:setDebug(false);">Debug Off</a></p>
        <h3>Callbacks</h3>
        <p><a href="javascript:setCallback(true);">Callback On</a><br>
        <a href="javascript:setCallback(false);">Callback Off</a></p>
	<p><em><strong>Note:</strong> the debug and callback controls only affect debug output on the server side.</em></p>
       </td>
      </tr>
    </table>

<%@ include file="footer.jspinc" %>
