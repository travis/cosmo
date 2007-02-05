<%@
page contentType="text/html; charset=UTF-8" %><%@
page language="java" %><%@
page import="com.metaparadigm.jsonrpc.JSONRPCBridge" %><%@
page import="com.metaparadigm.dict.DictClient"
%><jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge"
/><jsp:useBean id="dict" scope="session"
     class="com.metaparadigm.dict.DictClient"
/> <%
   response.setDateHeader ("Expires", 0);
   //JSONRPCBridge.setDebug(true);
   JSONRPCBridge.registerObject("dict", dict);
%>
<%!
  String title = "JSON-RPC-Java Dictionary Demo";
  String head =
    "    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/dict.css\">\n" +
    "    <script type=\"text/javascript\" src=\"jsonrpc.js\"></script>\n" +
    "    <script type=\"text/javascript\" src=\"dict.js\"></script>\n";
  String onLoad = "onLoad()";
%>
<%@ include file="header.jspinc" %>

    <h2>JSON-RPC-Java Dictionary Client</h2>

    <p>
      <strong>Word:</strong>
      <input class="dict_word_input" id="word" type="text" size="25" />
      &nbsp;
      <strong>Strategy:</strong>
      <select class="dict_strategy_select" id="strategy">
        <option value=".">default</option>
      </select>
      &nbsp;
      <strong>Database:</strong>
      <select class="dict_database_select" id="database">
        <option value="*">all</option>
      </select> 
      &nbsp;
      <input id="auto" type="checkbox" /> Auto
      &nbsp;
      <input id="lookup" type="button" value="Lookup"
             onclick="matchWord()" />
    </p>
    <p><strong>Strategy:</strong> <span id="strategy_desc">Default</span></p>
    <p><strong>Database:</strong> <span id="database_desc">All</span></p>

    <table cellpadding="0" cellspacing="0" width="100%" style="border: 1px solid #c0c0c0; border-collapse: collapse;">
      <tr>
        <td width="150">
	  <select id="matches" size="24" class="dict_match_select">
            <option></option>
          </select>
        </td>
        <td id="definitions"><div></div></td>
      </tr>
    </table>

<%@ include file="footer.jspinc" %>
