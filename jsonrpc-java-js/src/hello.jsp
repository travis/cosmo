<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
 "http://www.w3.org/TR/html4/loose.dtd">
<jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge" />
<jsp:useBean id="hello" scope="session"
     class="com.metaparadigm.jsonrpc.test.Hello" />
<% JSONRPCBridge.registerObject("hello", hello); %>
<html>
  <head>
    <script type="text/javascript" src="jsonrpc.js"></script>
    <script type="text/javascript" src="hello.js"></script>
    <title>JSON-RPC-Java Hello</title>
   </head>
   <body bgcolor="#ffffff" onLoad="onLoad()">
    <h2>JSON-RPC-Java Hello</h2>
    <p>The JSON-RPC-Java <em>Hello World</em> application.</p>
    <p>
      <strong>Who:</strong>
      <input type="text" id="who" size="30" value="Michael" />
      &nbsp;
      <input type="button" value="Say Hello" onclick="clickHello()" />
    </p>
  </body>
</html>
