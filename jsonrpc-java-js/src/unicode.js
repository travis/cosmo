jsonurl = "JSON-RPC";
jsonrpc = null;

function onLoad()
{
    testTableBody = document.getElementById("tests");

    try {
	jsonrpc = new JSONRpcClient(jsonurl);
	jsonrpc.unicode.getTests(processTests);
    } catch(e) {
	if(e.message) alert(e.message);
	else alert(e);
    }
}

function processTests(result, e)
{
    if(e) {
	alert(e);
	return;
    }
    var tests = result;
    jsonrpc.unicode.compareTests(processCompares, tests);
    for(var key in tests.map) {
	var row = testTableBody.insertRow(testTableBody.rows.length);
	var dcell = row.insertCell(row.cells.length);
	var rcell = row.insertCell(row.cells.length);
	var scell = row.insertCell(row.cells.length);
	var pcell = row.insertCell(row.cells.length);
	dcell.innerHTML = "<div class=\"desc_cell\">" +
	    tests.map[key].description + "</div>";
	dcell.className = "test_td";
	rcell.className = "test_td";
	rcell.innerHTML = "<div class=\"recv_cell\">" +
	    tests.map[key].data + "</div>";
	scell.className = "test_td";
	scell.id = "send." + key;
	pcell.className = "test_td";
	pcell.id = "pass." + key;
    }
}

function processCompares(result, e)
{
    if(e) {
	alert(e);
	return;
    }
    var compares = result;
    for(var key in compares.map) {
	if(typeof compares.map[key] != "object") continue;
	scell = document.getElementById("send." + key);
	pcell = document.getElementById("pass." + key);
	scell.innerHTML = "<div class=\"echo_cell\">" +
	    compares.map[key].data + "</div>";
	if(compares.map[key].compares)
	    pcell.innerHTML = "<div class=\"pass_cell\">pass</div>";
	else
	    pcell.innerHTML = "<div class=\"fail_cell\">fail</pass>";
    }
}

