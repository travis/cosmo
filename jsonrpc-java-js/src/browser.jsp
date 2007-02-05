<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.metaparadigm.jsonrpc.JSONRPCBridge" %>
<jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge" />
<jsp:useBean id="browser" scope="session"
     class="com.metaparadigm.jsonrpc.test.Browser" />
<%
   response.setDateHeader ("Expires", 0);
   JSONRPCBridge.registerObject("browser", browser);
   if(browser.userAgent == null)
	browser.userAgent = request.getHeader("User-Agent");
   String testCookies = request.getParameter("test-cookies");
   String testResult = request.getParameter("test-result");
   if(!browser.firstRun && !browser.passed && !browser.failed &&
	browser.userAgent != null &&
	testResult != null && testResult.equals("fail")) {
	browser.failUserAgent();
	response.sendRedirect("browser.jsp");
        return;
   }
  String onLoad;
  if(testCookies == null && browser.gotSession == false) onLoad = "testCookies()";
  else if(testCookies != null && browser.gotSession == false) onLoad = null;
  else if(browser.firstRun) onLoad = "testJSONRPC()";
  else onLoad = "decodeUserAgents()";
  String title = "JSON-RPC-Java Browser Compatibility";
  String head =
    "    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/browser.css\">\n" +
    "    <script type=\"text/javascript\" src=\"jsonrpc.js\"></script>\n" +
    "    <script type=\"text/javascript\" src=\"browser.js\"></script>\n";
%>
<%@ include file="header.jspinc" %>

    <h2>JSON-RPC-Java Browser Compatibility</h2>

<% if(testCookies == null && browser.gotSession == false) { %>
    <p>Testing to see if you have cookies enabled...</p>
<% } else if(testCookies != null && browser.gotSession == false) { %>
    <p>Cookies not enabled. Feed me, Feed me, I want cookies!</p>
<% } else if(browser.firstRun) { %>
    <p>Testing JSON-RPC-Java. The page will reload in one moment...</p>
    <p><em>Note:</em> You need a recent browser (post year 2000) with an ECMAScript 3rd Edition (ECMA-262) interpreter to run this page ie. Netscape JavaScript 1.5, Microsoft JScript 5.0 or any other conforming implementations.</p>
    <p>If your browser does not support this scripting standard, it will not be entered in the browser compatibility database (and this page will not reload).</p>
<% } else { %>
    <p>This page has just tested your browser for compatilibty with JSON-RPC (effectively testing the XMLHttpRequest object) and has recorded your user agent in the compatibility database (if it did not already exist).</p>
    <p>Your browser <strong><%= browser.passed ? "passed" : "failed" %></strong> the JSON-RPC test.</p>
<% if(browser.addNotify && browser.passed) { %>
    <p>Added "<em><%= browser.userAgent %></em>" to the good browser list.</p>
    <% browser.addNotify = false; %>
<% } else if(browser.addNotify && browser.failed) { %>
    <p>Added "<em><%= browser.userAgent %></em>" to the bad browser list.</p>
    <% browser.addNotify = false; %>
<% } else if(browser.userAgent == null) { %>
    <p><b>Wierd Science! Your browser didn't pass a User-Agent header.</b></p>
<% } else if(!browser.userAgent.equals(request.getHeader("User-Agent"))) { %>
    <p><b>Quit fooling with that user agent switcher!</b></p>
<% } else { %>
    <p>Your User-Agent: "<em><%= browser.userAgent %></em>" already exists in the browser compatiblity database.</p>
<% } %>

    <p><em>Note:</em> The heuristics to decode the browser name, version and platform are very simple and may misdetect in some caes.</p>

    <h2>Browsers that passed the test</h2>
    <table class="br_table">
      <thead>
       <tr>
        <th class="br_th" width="110"><div class="br_heading">Browser</div></th>
        <th class="br_th" width="80"><div class="plat_heading">Platform</div></th>
        <th class="br_th" width="100%"><div class="ua_heading">User Agent</div></th>
       </tr>
      </thead>
      <tbody id="good-browsers">
<%
        Iterator i;
	i = browser.getPassedUserAgents().iterator();
	while(i.hasNext()) {
		String userAgent = (String)i.next();
%>
<tr>
 <td class="br_td"><div class="br_cell">&nbsp;</div></td>
 <td class="br_td"><div class="plat_cell">&nbsp;</div></td>
 <% if(browser.userAgent != null && browser.userAgent.equals(userAgent)) { %>
 <td class="br_td"><div class="ua_cell"><b><%= userAgent %></b></div></td>
 <% } else { %>
 <td class="br_td"><div class="ua_cell"><%= userAgent %></div></td>
 <% } %>
</tr>
<%
        }
%>
      </tbody>
    </table>

    <h2>Browsers that failed the test</h2>
    <table class="br_table">
      <thead>
       <tr>
        <th class="br_th" width="110"><div class="br_heading">Browser</div></th>
        <th class="br_th" width="80"><div class="plat_heading">Platform</div></th>
        <th class="br_th" width="100%"><div class="ua_heading">User Agent</div></th>
       </tr>
      </thead>
      <tbody id="bad-browsers">
<%
	i = browser.getFailedUserAgents().iterator();
	while(i.hasNext()) {
		String userAgent = (String)i.next();
%>
<tr>
 <td class="br_td"><div class="br_cell">&nbsp;</div></td>
 <td class="br_td"><div class="plat_cell">&nbsp;</div></td>
 <% if(browser.userAgent != null && browser.userAgent.equals(userAgent)) { %>
 <td class="br_td"><div class="ua_cell"><b><%= userAgent %></b></div></td>
 <% } else { %>
 <td class="br_td"><div class="ua_cell"><%= userAgent %></div></td>
 <% } %>
</tr>
<%
        }
%>
      </tbody>
    </table>
<% } %>

<%@ include file="footer.jspinc" %>

<%
  if(testCookies != null && browser.gotSession == true)
      browser.firstRun = false;
  browser.gotSession = true;
%>
