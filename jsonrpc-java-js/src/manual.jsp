<%!
  String title = "JSON-RPC-Java Manual";
  String head = "";
  String onLoad = null;
%>
<%@ include file="header.jspinc" %>

    <h2>JSON-RPC-Java Manual</h2>

    <p>This manual covers the architecture and implementation details of JSON-RPC-Java and contains a reference guide to the various components and interfaces. Please start with the <a href="tutorial.html">tutorial</a> if want to get started quickly.</p>

    <h2>Table of Contents</h2>

    <ul>
      <li><a href="#architecture">Architecture</a>
        <ul>
          <li><a href="#jsonrpcbridge">JSONRPCBridge</a></li>
          <li><a href="#global-bridge">Global bridge</a></li>
          <li><a href="#jsonrpcservlet">JSONRPCServlet</a></li>
        </ul>
      </li>
      <li><a href="#type-mapping">Type Mapping</a>
        <ul>
          <li><a href="#class-hinting">Class Hinting</a></li>
        </ul>
      </li>
      <li><a href="#javascript-client">JavaScript client</a>
        <ul>
          <li><a href="#synchronous-calls">Synchronous calls</a></li>
          <li><a href="#asynchronous-calls">Asynchronous calls</a></li>
          <li><a href="#exceptions">Exceptions</a></li>
        </ul>
      </li>
      <li><a href="#references">References</a>
        <ul>
          <li><a href="#opaque-references">Opaque References</a></li>
          <li><a href="#callable-references">Callable References</a></li>
        </ul>
      </li>
      <li><a href="#local-arg-resolvers">Local Argument Resolvers</a></li>
      <li><a href="#custom-serializers">Custom Serializers</a></li>
    </ul>

    <h2><a name="architecture">Architecture</a></h2>
    <p>JSON-RPC-Java consists of two main user visible components, the JSONRPCBridge and the JSONRPCServlet.</p>

    <h3><a name="jsonrpcbridge">JSONRPCBridge</a></h3>
    <p>The JSONRPCBridge is a per session object that holds references to objects that are exported to a specific client. It is passed JSON-RPC requests from the transport (JSONRPCServlet) and it then performs the unmarshalling of JSON objects to Java objects, performs the method invocation and then marshalls the method's Java object result back to JSON. Serializer objects perform the actual type conversion between Java and JavaScript objects.</p>
    <p>The JSONRPCBridge must be placed in a HttpSession object registered under the attribute "JSONRPCBridge" to allow the JSONRPCServlet to locate the bridge to make calls on the exported objects.</p>
    <p>The bridge is implemented as session specific for a number of reasons:</p>
    <ul>
      <li>to improve the security of the application</li>
      <li>export object methods to specific users</li>
      <li>hold references to objects returned to a specific client</li>
    </ul>
    <p>To export all instance methods of an object to a client:</p>
    <pre>bridge.registerObject("myObject", myObject);</pre>
    <p>To export all static methods of a class to a client:</p>
    <pre>bridge.registerClass("MyClass", com.example.MyClass.class);</pre>
    <p>If <code>registerObject</code> and <code>registerClass</code> are called multiple times with the same key, then the object is replaced with the new one</p>
    <h3><a name="global-bridge">Global bridge</a></h3>
    <p>There is a global bridge singleton object that allows exporting objects to all HTTP clients. This can be used for registering factory classes although care must be taken with authentication and security issues as these objects will be accessible to all clients. It can be fetched with <code>JSONRPCBridge.getGlobalBridge()</code>.</p>
    <p>To export all instance methods of an object to <b>all</b> clients:</p>
    <pre>JSONRPCBridge.getGlobalBridge().registerObject("myObject", myObject);</pre>
    <p>To export all static methods of a class to <b>all</b> clients:</p>
    <pre>JSONRPCBridge.getGlobalBridge().registerClass("MyClass", com.example.MyClass.class);</pre>

    <h3><a name="jsonrpcservlet">JSONRPCServlet</a></h3>
    <p>This servlet, the transport part of JSON-RPC-Java, handles JSON-RPC requests over HTTP and dispatches them to a JSONRPCBridge instance registered in the HttpSession.</p>
    <p>An instance of the JSONRPCBridge object is automatically placed in the HttpSession object registered under the attribute <code>"JSONRPCBridge"</code> by the JSONRPCServlet.</p>
    <p>The following would be used in your web.xml to export the servlet under the URI <code>"/JSON-RPC"</code> (this is the standard location):</p>
<pre>&lt;servlet&gt;
  &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
  &lt;servlet-class&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-class&gt;
&lt;/servlet&gt;
&lt;servlet-mapping&gt;
  &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
  &lt;url-pattern&gt;/JSON-RPC&lt;/url-pattern&gt;
&lt;/servlet-mapping&gt;</pre>
    <p><em>Please note:</em> due to relative mapping of URIs in your web container. You may need to set the URL in your client to: <code>"/&lt;web-app-name&gt;/JSON-RPC"</code>.

    <h2><a name="type-mapping">Type mapping</a></h2>
    <p>To allow JSON-RPC-Java to transparently unmarshall complex nested objects and with that, the usage of Java's container classes, JSON-RPC needs a mechanism to preserve type information.</p>
    <p>This comes from the combination of the JavaScript's typeless nature and the method that Java container classes gain their genericity (through the usage of a single base class 'Object', rather than using parameterized types such as C++ templates. <em>Note: Java 5.0 however supports these parameterized collection types but support is not yet included in JSON-RPC-Java for this</em>).<</p>
    <p>In the case of unmarshalling a JavaScript object into a Java method argument with a generic container interface (such as List, Map, Set, or their concrete counterparts), the need for additional type information is apparant. We have no type information on either side to work out the mapping from the contained type of JavaScript array to the contained type of the Java container class.</p>
    <p>With normal array method arguments ie. a Java method argument of class <code>Foo[]</code>, we know the items in the JavaScript array must (or should) be all be <code>class Foo</code>.</p>
    <p>With a Java method argument of class <code>ArrayList</code>, we only have <code>Object</code> as the contained type and no type information with a regular JavaScript object.</p>
    <p>This leads to the method that JSON-RPC-Java maintains it's transparent mapping - <em>'class hinting'</em>.</p>
    <h3><a name="class-hinting">Class Hinting</a></h3>
    <p>In the case of regular Java Beans an extra field <code>javaClass</code> is added that maps the typeless JavaScript object back to the Java class. The is used on the Java side during unmarshalling to ease the transparent mapping of the object back to it's Java Class when it is sent back to the server (although JSON-RPC-Java can in some cases map the objects without the additional type information if the mapping is unambiguos ie. if the object is not inside of a generic container class, then the method class signature can be used).</p>
    <p>For Java container classes such as the <code>List</code>,  we can't map them to a JavaScript native array as we would have nowhere to store the type hint. So Java container classes have a special type mapping from Java to JavaScript described here:</p>

    <h4>Bean</h4>
    <p>Java beans (objects conforming to get getProperty, setProperty, etc. syntax) map directy to a JavaScript Object with the additional of the string member <code>javaClass</code> containing the Java class name. The property names have the get or set prefix removed and the first letter lowercased. eg.</p>
<pre>{
  "javaClass": "com.example.MyBean",
  "someStringProperty": "foo",
  "someBooleanProperty": true,
  "someIntegerProperty": 10
}</pre>

    <h4>List</h4>
    <p>List (ArrayList, LinkedList, Vector) maps to a JavaScript object with a string member <code>javaClass</code> containing the Java class name and a nativeJavaScript array member <code>list</code> containing the data. eg.</p>
<pre>{
  "javaClass": "java.util.ArrayList",
  "list": [0, 1, 2, 3, 4]
}</pre>

    <h4>Map</h4>
    <p>Map (HashMap, TreeMap, LinkedHaspMap) maps to a JavaScript object with a string member <code>javaClass</code> containing the Java class name and a nativeJavaScript object containing the key value pairs. eg.</p>
<pre>{
  "javaClass": "java.util.HashMap",
  "map": {"foo key": "foo value"}
}</pre>
    <p>The Java string representation of the key object is used as the native JavaScript object type only supports strings as keys.</p>

    <h4>Set</h4>
    <p>Set (HashSet, TreeSet, LinkedHashSet) maps to a JavaScript object with a string member <code>javaClass</code> containing the Java class name and a native JavaScript object containing the set item string values as keys and set objects as values. eg.</p>
<pre>{
  "javaClass": "java.util.HashSet",
  "set": {"foo key": "foo key"}
}</pre>
    <p>The Java string representation of the key object is used as the native JavaScript object type only supports strings as keys.</p>

    <h2><a name="javascript-client">JavaScript Client</a></h2>
    <p>The JavaScript client <code>JSONRpcClient</code> constructs a transparent proxy providing method access to all methods on the JSON-RPC server.</p>
    <p>It is constructued like this:</p>
    <pre>var jsonrpc = new JSONRpcClient("/webapp/JSON-RPC/")</pre>
    <p>HTTP authentication can also be used</p>
    <pre>var jsonrpc = new JSONRpcClient("/webapp/JSON-RPC/", user, pass)</pre>
    <p>The consutrctor of the JSONRpcClient object queries the server using the internal method <code>system.listMethods</code> which returns an array with the list of object methods available on the server. Proxy delegating functions are then added to the new JSONRpcClient object with each of the method names on the server.</p>

    <h3><a name="synchronous-calls">Synchronous calls</a></h3>
    <p>A synchronous call can be made on one of the server methods by calling the associated object method on the JSONRpcClient object. eg. to call the method echo on the object exported with the name test, we would use:</p>
    <pre>jsonrpc.test.echo("hello");</pre>

    <h3><a name="asynchronous-calls">Asynchronous calls</a></h3>
    <p>Asynchronous calls are simply made by inserting a JavaScript callback function as the first argument.</p>
    <pre>jsonrpc.test.echo(cb, "hello");</pre>
    <p>Anonymous functions can also be used:</p>
    <pre>jsonrpc.test.echo(function (msg) { print(msg); }, "hello");</pre>
    <p>The callback function is passed two arguments:</p>
    <pre>function cb(result, exception) {
  if(exception) { alert(exception.message); }
  // do stuff here ...
}</pre>
    <p>The second argument to callback functions is required to capture exception information. You must be aware of the following when using async callback functions:</p>
    <ul>
      <li><code>result == null && exception != null</code> when an exception occured.</li>
      <li><code>result != null && exception == null</code> when the call completed successfully.</li>
    </ul>

    <h3><a name="exceptions">Exceptions</a></h3>
    <p>The JSONRpcClient constructor proxy methods can throw an exception of type <code>JSONRpcClient.Exception</code> (ie. <code>e.constructor == JSONRpcClient.Exception</code>). The <code>JSONRpcClient.Exception</code> object is derived from the JavaScript <code>Error</code> object and thus inherits its general properties such as <code>message</code>.</p>
    <p>Two types of exceptions are thrown from proxy methods on the JSONRpcClient object:</p>
    <ul>
      <li>Client exceptions - exceptions that occured during the remote communication with the JSON-RPC server.</li>
      <li>Java native exceptions - exceptions throw by the remote code.</li>
    </ul>
    <h3>Client Exceptions</h3>
    <p>Thrown if a communication error occurs, a method cannot be found. It has the following properties:</p>
    <ul>
      <li><code>e.name == "JSONRpcClientException"</code></li>
      <li><code>e.code</code> an integer error code containing either an HTTP status code or one of the following codes:
        <ul>
          <li><code>JSONRpcClient.Exception.CODE_ERR_PARSE = 590</code></li>
          <li><code>JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591</code></li>
          <li><code>JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592</code></li>
          <li><code>JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593</code></li>
        </ul>
      </li>
      <li><code>e.message</code> a string containing descriptive text of the exception.</li>
    </ul>
    <h3>Java native exceptions</h3>
    <p>Thrown if the remote Java method raises an exception.  It has the following properties:</p>
    <ul>
      <li><code>e.name == "&lt;class name of remote exception&gt;"</code></li>
      <li><code>e.code == JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION</code></li>
      <li><code>e.message</code> a string containing descriptive text of the exception.</li>
      <li><code>e.javaStack</code> a string containing the Java stack trace.</li>
    </ul>

    <h2><a name="references">References</a></h2>
    <p>JSON-RPC-Java has some basic ORB (Object Request Broker) functionality with the ability to pass objects by reference and keep these references in the user's session.</p>
    <p>Two types of references are handled: opaque references and callable references.</p>

    <h3><a name="opaque-references">Opaque References</a></h3>
    <p>Objects of classes registered as References will be returned as opaque reference objects to JavaScript instead of passed by value which is the default behaviour. When these opaque reference objects are passed to succussive Java method calls will then be reassociated back to the original Java object (great for security sensitive objects).</p>
    <p>A class can be registered as an opaque reference on the JSONRPCBridge as follows:</p>
    <pre>bridge.registerReference(com.metaparadigm.test.Foo.class)</pre>
    <p>A reference in JSON format looks like this:</p>
    <pre>{ "javaClass":"com.metaparadigm.test.Foo",
  "objectID":5535614,
  "JSONRPCType":"Reference" }</pre>
    <p>References could should be used for privileged objects that contain information that needs to be kept secure or complex types that are not required in the Javascript client but need to be passed as a reference in methods of exported objects.</p>

    <h3><a name="callable-references">Callable References</a></h3>
    <p>Objects of classes registered as Callable References will return dynamic proxies to allow invocation on the particular object instance in the server-side Java. There are extensions to the JSON-RPC protocol in the provided JSON-RPC JavaScript client for dynamic proxy creation support.</p>
    <p>A class can be registered as a callable reference on the JSONRPCBridge as follows:</p>
    <pre>bridge.registerCallableReference(com.metaparadigm.test.Bar)</pre>
    <p>A callable reference in JSON format looks list this:</p>
    <pre>{ "javaClass":"com.metaparadigm.test.Bar",
  "objectID":4827452,
  "JSONRPCType":"CallableReference" }</pre>
    <p>CallableReferences can be registered for classes that for instance are returned from factory classes as a convenient way to avoid having to manually export these objects returned from these factory methods.</p>
    <p><b>Note:</b> A limitation exists in the JSON-RPC client where only the top most object returned from a method can be made into a proxy.</p>

    <h2><a name="local-arg-resolvers">Local Argument Resolvers</a></h2>
    <p>LocalArgResolvers are classes that can resolve an argument from the exported method signatures on the server-side. The exported signature of methods that contain a class registered as a LocalArgResolver have that class removed. It does not need to be provided in call in the JSON-RPC client.</p>
    <p>The LocalArgResolver provides a mechanism to get access to the HttpServletRequest or HttpSession object associated with a request/method invocation. There are 3 LocalArgResolvers that are enabled by default:
      <ul>
        <li>JSONRPCBridgeServletArgResolver</li>
	<li>HttpSessionArgResolver</li>
	<li>HttpServletRequestArgResolver</li>
      </ul>
    </p>
    <p>Additional LocalArgResolvers can be created by implementing the <code>LocalArgResolver</code> interface and calling <code>JSONRPCBridge.registerLocalArgResolver()</code></p>
    <p>An example method that has JSONRPCBridge in the method signature:</p>
    <pre>public void setDebug(JSONRPCBridge bridge, boolean flag)
{
    bridge.setDebug(flag);
}</pre>
    <p>This can be called from JavaScript like this:</p>
    <pre>jsonserver.test.setDebug(true);</pre>
    <p>The JSONRPCBridge object associated with the users session will be resolved on the server-side and passed in to the remote method. Likewise you can add HttpSession or HttpServletRequest to your methods and have them filled in automatically.</p>
    
    <h2><a name="custom-serializers">Custom Serializers</a></h2>
    <p>...</p>

<%@ include file="footer.jspinc" %>
