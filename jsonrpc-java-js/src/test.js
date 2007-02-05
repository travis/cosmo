jsonurl = "JSON-RPC";
jsonrpc = null;


function clrscr()
{
    resultNode.value = "";
}

function print(s)
{
    resultNode.value += "" + s;
    resultNode.scrollTop = resultNode.scrollHeight;
}

function onLoad()
{
    resultNode = document.getElementById("result");

    try {
	jsonrpc = new JSONRpcClient(jsonurl);
    } catch(e) {
	if(e.message) alert(e.message);
	else alert(e);
    }
}

function doListMethods()
{
    clrscr();
    print("system.listMethods()\n\n");

    try {

	var rslt = jsonrpc.system.listMethods();
	for(var i=0; i < rslt.length; i++) {
	    print(rslt[i] + "\n");
	}

    } catch(e) {
	print("Exception: \n\n" + e);
    }
}

function doBasicTests()
{
    clrscr();
    print("Running tests\n\n");

    // Some test data
    var bools = [true,false,true];
    var waggle = { bang: "foo", baz: 9 };
    var wiggle = { foo: "bang", bar: 11 };
    var list = {"list":[20,21,22,23,24,25,26,27,28,29],
		"javaClass":"java.util.Vector"};

    try {

	print("test.voidFunction()");
	print(" returns " + jsonrpc.test.voidFunction() + "\n");

	print("test.echo(\"hello\")");
	print(" returns " + jsonrpc.test.echo("hello") + "\n");

	print("test.echo(1234)");
	print(" returns " + jsonrpc.test.echo(1234) + "\n");

	print("test.echo([1,2,3])");
	print(" returns " + jsonrpc.test.echo([1,2,3]) + "\n");

	print("test.echo([\"foo\", \"bar\", \"baz\"])");
	print(" returns " + jsonrpc.test.echo(["foo","bar","baz"]) + "\n");

	print("test.echo(" + toJSON(waggle) + ")");
	print(" returns " + toJSON(jsonrpc.test.echo(waggle)) + "\n");

	print("test.echo(" + toJSON(wiggle) + ")");
	print(" returns " + toJSON(jsonrpc.test.echo(wiggle)) + "\n");

	print("test.echoChar(\"c\")");
	print(" returns " + jsonrpc.test.echoChar("c") + "\n");

	print("test.echoIntegerObject(1234567890)");
	print(" returns " + jsonrpc.test.echoIntegerObject(1234567890) + "\n");

	print("test.echoLongObject(1099511627776)");
	print(" returns " + jsonrpc.test.echoLongObject(1099511627776) + "\n");

	print("test.echoFloatObject(3.3)");
	print(" returns " + jsonrpc.test.echoFloatObject(3.3) + "\n");

	print("test.echoDoubleObject(9.9)");
	print(" returns " + jsonrpc.test.echoDoubleObject(9.9) + "\n");

	print("test.echoBoolean(true)");
	print(" returns " + jsonrpc.test.echoBoolean(true) + "\n");

	print("test.echoBoolean(false)");
	print(" returns " + jsonrpc.test.echoBoolean(false) + "\n");

	print("test.echoByteArray(\"test test\")");
	print(" returns " + jsonrpc.test.echoByteArray("test test") + "\n");

	print("test.echoCharArray(\"test again\")");
	print(" returns " + jsonrpc.test.echoCharArray("test again") + "\n");

	print("test.echoBooleanArray(" + bools + ")");
	print(" returns " + jsonrpc.test.echoBooleanArray(bools) + "\n");

	print("test.echoList(" + toJSON(list) + ")");
	print(" returns " + toJSON(jsonrpc.test.echoList(list)) + "\n");

	print("test.concat(\"a\",\"b\")");
	print(" returns " + jsonrpc.test.concat("a","b") + "\n");

	print("test.anArray()");
	print(" returns " + jsonrpc.test.anArray() + "\n");

	print("test.anArrayList()");
	print(" returns " + toJSON(jsonrpc.test.anArrayList()) + "\n");

	print("test.aVector()");
	print(" returns " + toJSON(jsonrpc.test.aVector()) + "\n");

	print("test.aList()");
	print(" returns " + toJSON(jsonrpc.test.aList()) + "\n");
	
	print("test.aSet()");
	print(" returns " + toJSON(jsonrpc.test.aSet()) + "\n");

	print("test.aHashtable()");
	print(" returns " + toJSON(jsonrpc.test.aHashtable()) + "\n");

    } catch(e) {
	print(" Exception: \n\n" + e);
    }
}

function doReferenceTests()
{
    clrscr();
    print("Running Reference Tests\n\n");

    var rslt;
    var callableRef;
    var ref;

    try {

	print("var callableRef = test.getCallableRef()\n");
	callableRef = jsonrpc.test.getCallableRef();
	print("returns a CallableReference objectID=" +
	      callableRef.objectID + "\n\n");

	print("callableRef.ping()\n");
	rslt = callableRef.ping()
	    print("returns \"" + rslt + "\"\n\n");

	print("var ref = callableRef.getRef()\n");
	ref = callableRef.getRef();
	print("returns Reference objectID=" +
	      ref.objectID + "\n\n");

	print("callableRef.whatsInside(ref)\n");
	rslt = callableRef.whatsInside(ref);
	print("returns \"" + rslt + "\"\n\n");

    } catch(e) {
	print(" Exception: \n\n" + e);
    }
}

function doContainerTests()
{
    clrscr();
    print("Running Container tests\n\n");

    try {

	print("wigArrayList = test.aWiggleArrayList(2)\n");
	var wigArrayList = jsonrpc.test.aWiggleArrayList(2);
	print("returns " + wigArrayList + "\n\n");

	print("test.aWiggleArrayList(wigArrayList)\n");
	var rslt = jsonrpc.test.wigOrWag(wigArrayList);
	print("returns \"" + rslt + "\"\n\n");

    } catch(e) {
	print("Exception: \n\n" + e);
    }
}

function doExceptionTest()
{
    clrscr();
    print("Running Exception test\n\n");

    try {
	print("test.throwException()\n\n");
	jsonrpc.test.throwException();
    } catch(e) {
	print("e.toString()=" + e.toString() + "\n");
	print("e.name=" + e.name + "\n");
	print("e.message=" + e.message + "\n");
	print("e.javaStack=" + e.javaStack + "\n");
    }
}

function setDebug(flag)
{
    clrscr();
    try {
	print("test.setDebug(" + flag + ")\n\n");
	jsonrpc.test.setDebug(flag);
    } catch(e) {
	print("Exception: \n\n" + e);
    }
}

function setCallback(flag)
{
    clrscr();
    try {
	print("test.setCallback(" + flag + ")\n\n");
	jsonrpc.test.setCallback(flag);
    } catch(e) {
	print("Exception: \n\n" + e);
    }
}
