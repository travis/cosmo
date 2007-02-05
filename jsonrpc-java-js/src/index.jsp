<%!
  String title = "JSON-RPC-Java Demos";
  String head = "";
  String onLoad = null;
%>
<%@ include file="header.jspinc" %>

    <h2>Demos</h2>
    <p>These demos use a lightweight JavaScript JSON-RPC implementation (<a href="jsonrpc.js.txt">jsonrpc.js</a>) originally based on the JSON-RPC implementation in the <a href="http://jan.kollhof.net/projects/js/jsolait/">JavaScript&nbsp;o&nbsp;lait</a> library by <a href="http://jan.kollhof.net/">Jan-Klaas&nbsp;Kollhof</a> (the author of the <a href="http://json-rpc.org/specs.xhtml">JSON-RPC&nbsp;specification</a>). The lightweight JSON-RPC JavaScript client and the JSON-RPC-Java example code are included in the source distribution.</p>

    <h3><a href="browser.jsp">Browser Compatibility Database</a></h3>
    <p>Tests your browser for compatilibty with the JavaScript JSON-RPC client (ie. working XMLHttpRequest object).</p>
    <p><em>Please visit this page so we can record the test results for your User-Agent.</em></p>

    <h3><a href="hello.jsp">Hello</a></h3>
    <p>The JSON-RPC-Java <em>Hello World</em> application. See the simplest JSON-RPC-Java application in action.</p>
    <ul>
      <li>Demo source: <a href="Hello.java.txt">Hello.java</a>,
	<a href="hello.jsp.txt">hello.jsp</a> and
	<a href="hello.js.txt">hello.js</a></li>
    </ul>

    <h3><a href="test.jsp">Tests</a></h3>
    <p>Basic demo that performs a number of simple tests and shows the usage of a variety of different Java types.</p>
    <ul>
      <li>Demo source: <a href="Test.java.txt">Test.java</a>,
	<a href="test.jsp.txt">test.jsp</a> and
	<a href="test.js.txt">test.js</a></li>
    </ul>

    <h3><a href="unit.jsp">Unit Tests</a></h3>
    <p>Performs unit testing and profiling with both asynchronous and synchronous calls.</p>

    <h3><a href="unicode.jsp">Unicode Tests</a></h3>
    <p>Performs testing of sending and recieving unicode data.</p>

    <h3><a href="dict.jsp">Dictionary</a></h3>
    <p>Dictionary lookup demo using JSON-RPC. This demo implements a simple server side Java <a href="http://www.dict.org">DICT</a> protocol (<a href="http://www.dict.org/rfc2229.txt">RFC2229</a>) client that can make queries against a local dict server, and exposes its API via JSON-RPC-Java to a JavaScript/DHTML dictionary lookup GUI.</p>
    <p>To run the demo on your own machine, you'll need to run <a href="http://www.dict.org/announce.html">dictd</a> or another DICT protocol server.</p>
    <p>On debian you can run the following commands to set up a dict server with the same dictionaries as the demo server:</p>
    <pre>apt-get install dictd dict-foldoc dict-gcide dict-jargon dict-wn</pre>

<%@ include file="footer.jspinc" %>
