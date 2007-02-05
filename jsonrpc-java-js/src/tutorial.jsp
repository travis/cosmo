<%!
  String title = "JSON-RPC-Java Tutorial";
  String head = "";
  String onLoad = null;
%>
<%@ include file="header.jspinc" %>

    <h2>JSON-RPC-Java Tutorial</h2>

    <p>This tutorial briefly describes how to build JSON-RPC-Java and then get your JavaScript client code to call methods in a Java server application using JSON-RPC-Java and the included JavaScript JSON-RPC client.</p>

    <h2>Table Of Contents</h2>
    <ul>
      <li><a href="#requirements">Requirements</a></li>
      <li><a href="#building">Building JSON-RPC-Java</a></li>
      <li><a href="#demos">Building and installing JSON-RPC-Java Demos</a></li>
      <li><a href="#jsonrpcservlet">The JSONRPCServlet</a></li>
      <li><a href="#webxml">Adding the JSONRPCServlet to your web.xml</a></li>
      <li><a href="#jsonrpcbridge">The JSONRPCBridge</a></li>
      <li><a href="#example-jsp">Using the JSONRPCBridge in a JSP page</a></li>
      <li><a href="#example-servlet">Using the JSONRPCBridge in a Servlet</a></li>
      <li><a href="#example-js">Example JavaScript client application</a></li>
    </ul>

    <h2><a name="requirements">Requirements</a></h2>
    <ul>
      <li>ant 1.6 or later (to build)</li>
      <li>A Java Servlet container (such Apache Tomcat, the Tomcat service in JBoss, etc.).</li>
    </ul>

    <h2><a name="building">Building JSON-RPC-Java</a></h2>

    <p>Edit <code>build.xml</code> in the top directory of the unpacked JSON-RPC-Java distribution and set the location of your tomcat installation. eg.</p>
    <pre>&lt;property name="tomcat" location="/opt/jakarta-tomcat-5.5.7"/&gt;</pre>

    <p>Then build <code>jsonrpc.jar</code> by running ant in the same directory.</p>
    <pre>ant</pre>

    <p>This will build <code>jsonrpc.jar</code> which contains the JSONRPCServlet (recieves the JSON-RPC requests; see section on adding this to your <code>web.xml</code>) and the JSONRPCBridge bean (decodes and dispatches the requests to your Java code; see sections on adding this to your JSP or servlet).</p>

    <h2><a name="demos">Building and installing JSON-RPC-Java Demos</a></h2>

    <p>To build the demo WAR file (Web Application Archive) <code>jsonrpc.war</code>:</p>
    <pre>ant test.dist</pre>

    <p>To install into your <code>${tomcat}/webapps</code> directory (tomcat location needs to be set <code>build.xml</code>)</p>
    <pre>ant install</pre>

    <p>You should now be able to access your the demos on your local machine by pointing your browser at: <code>http://localhost:8080/jsonrpc/</code></p>

    <h2><a name="jsonrpcservlet">The JSONRPCServlet</a></h2>
    <p>This servlet, the transport part of JSON-RPC-Java, handles JSON-RPC requests over HTTP and dispatches them to a JSONRPCBridge instance registered in the HttpSession object.</p>

    <h2><a name="webxml">Adding the JSONRPCServlet to your web.xml</a></h2>
    <p>Use the following web.xml (or add the servlet and servlet-mapping to your existing one):</p>
    <pre><?xml version="1.0" encoding="ISO-8859-1"?>
&lt;!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
  "http://java.sun.com/dtd/web-app_2_3.dtd"&gt;
&lt;web-app&gt;
  &lt;servlet&gt;
    &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
    &lt;servlet-class&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-class&gt;
  &lt;/servlet&gt;
  &lt;servlet-mapping&gt;
    &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
    &lt;url-pattern&gt;/JSON-RPC&lt;/url-pattern&gt;
  &lt;/servlet-mapping&gt;
&lt;/web-app&gt;</pre>

    <h2><a name="jsonrpcbridge">The JSONRPCBridge</a></h2>
    <p>The JSONRPCBridge holds references to exported objects and decodes and dispatches method calls to them.</p>
    <p>An instance of the JSONRPCBridge object needs to be placed in a HttpSession object registered under the attribute "JSONRPCBridge" to allow the JSONRPCServlet to locate the bridge.</p>
    <p>The bridge is implemented as session specific to improve the security of applications by allowing exporting of object methods only to specific users. In this way you can export privileged objects to users after they have gone through your application authentication mechanism. This is a key part of the security mechanism of JSON-RPC-Java.</p>
    <p>To use the bridge to allow calling of Java methods you can put an instance of the bridge in a HttpSession in JSP using the usebean tag or in a Servlet using the HttpSesison API.</p>

    <h2><a name="example-jsp">Using the JSONRPCBridge in a JSP page</a></h2>
<p>Put a session scoped instance of the bridge into the HttpSession. eg.</p>
<pre>...
&lt;jsp:useBean id="JSONRPCBridge" scope="session"
   class="com.metaparadigm.jsonrpc.JSONRPCBridge" /&gt;
...</pre>
<p>Then export the object you wish to call methods on. eg.</p>
<pre>...
&lt;% JSONRPCBridge.registerObject("myTestObject", aTestObject); %&gt;
...</pre>
<p>You will also need to emit the HTML to source the jsonrpc.js JavaScript code and your own client code. Add something like this to your header section:</p>
<pre>...
  &lt;script type="text/javascript" src="jsonrpc.js"&gt;
  &lt;script type="text/javascript" src="myapp.js"&gt;
...</pre>
<p>That is all that is requied to make available all methods of the object as myTestObject.&lt;methodnames&gt; to JSON-RPC clients.</p>
<p>Take a look at the example <a href="hello.jsp.txt">hello.jsp</a> for more details.</p>

    <h2><a name="example-servlet">Using the JSONRPCBridge in a Servlet</a></h2>
<p>Put an instance of the bridge into the HttpSession. eg. in your service() method add the following code:</p>
<pre>...
// Find the JSONRPCBridge for this session or create one
// if it doesn't exist. Note the bridge must be named "JSONRPCBridge"
// in the HttpSession for the JSONRPCServlet to find it.
HttpSession session = request.getSession();
JSONRPCBridge json_bridge = null;
json_bridge = (JSONRPCBridge) session.getAttribute("JSONRPCBridge");
if(json_bridge == null) {
    json_bridge = new JSONRPCBridge();
    session.setAttribute("JSONRPCBridge", json_bridge);
}
...</pre>
<p>Then export the object you wish to call methods on. eg.</p>
<pre>...
json_bridge.registerObject("myTestObject", aTestObject);
...</pre>
<p>You will also need to emit the HTML to source the jsonrpc.js JavaScript code and your own client code.  Add something like this to your header section:</p>
<pre>...
out.println("&lt;script type=\"text/javascript\" src=\"jsonrpc.js\"&gt;&lt;/script&gt;");
out.println("&lt;script type=\"text/javascript\" src=\"myapp.js\"&gt;&lt;/script&gt;"); 
...</pre>
<p>That is all that is requied to make available all methods of the object as myTestObject.&lt;methodnames&gt; to JSON-RPC clients.</p>

    <h2><a name="example-js">Example JavaScript client application</a></h2>
    <p>The following example shows the simplest JSON-RPC client application<p>
<pre>onLoad = function()
{
    try {

	jsonrpc = new JSONRpcClient("/&lt;your webapp name here&gt;/JSON-RPC");

        // Call a Java method on the server
        var result = jsonrpc.myTestObject.myFunction("hello");
        alert(result);

    } catch(e) {
	alert(e);
    }
}</pre>
<p>Take a look at the example <a href="hello.js.txt">hello.js</a> for more details</p>

<%@ include file="footer.jspinc" %>
