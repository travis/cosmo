function redirectSelf(f)
{
    var loc = location.href;
    var qind = loc.indexOf("?");
    if(qind > 0) loc = loc.substring(0, qind);
    var hind = loc.indexOf("#");
    if(hind > 0) loc = loc.substring(0, hind);
    if(f) loc = loc.concat("?", f);
    location.href = loc;
    return false;
}

function testCookies()
{
    redirectSelf("test-cookies=");
}

function testJSONRPC()
{
    try {
	jsonrpc = new JSONRpcClient("JSON-RPC");
	jsonrpc.browser.passUserAgent();
	redirectSelf();
    } catch(e) {
	redirectSelf("test-result=fail");
    }
}

function decodeOneUserAgent(ua)
{
    var d = {};
    var m;

    d.browser = "";
    d.version = "";
    d.platform = "";

    if(ua.match(/Mac OS X/)) d.platform = "Mac OS X";
    else if(ua.match(/Mac_PowerPC/)) d.platform = "Mac OS";
    else if(ua.match(/Macintosh/)) d.platform = "Mac OS";
    else if(ua.match(/Linux/)) d.platform = "Linux";
    else if(ua.match(/FreeBSD/)) d.platform = "FreeBSD";
    else if(ua.match(/(Win95|Windows 95)/)) d.platform = "Win95";
    else if(ua.match(/(Win98|Windows 98)/)) d.platform = "Win98";
    else if(ua.match(/Win 9x/)) d.platform = "Win9x";
    else if(ua.match(/(WinNT4\.0|Windows NT 4\.0)/)) d.platform = "WinNT4.0";
    else if(ua.match(/(WinNT5\.0|Windows NT 5\.0)/)) d.platform = "Win2000";
    else if(ua.match(/(WinNT5\.1|Windows NT 5\.1)/)) d.platform = "WinXP";
    else if(ua.match(/(WinNT5\.1|Windows NT 5\.2)/)) d.platform = "Win2003";
    else if(ua.match(/Windows CE/)) d.platform = "WinCE";
    else if(ua.match(/Symbian OS/)) d.platform = "Symbian";
    if(ua.match(/PalmOS/)) d.platform = "PalmOS";

    if((m = ua.match(/Firefox\/([^\s$]+)/))) {
	d.browser = "Firefox";
	d.version = m[1];
    } else if((m = ua.match(/Galeon[;\/]\s?([^\s;$]+)/))) {
	d.browser = "Galeon";
	d.version = m[1];
    } else if((m = ua.match(/Epiphany\/([^\s$]+)/))) {
	d.browser = "Epiphany";
	d.version = m[1];
    } else if((m = ua.match(/Netscape\/([^\s$]+)/))) {
	d.browser = "Netscape";
	d.version = m[1];
    } else if((m = ua.match(/K-Meleon\/([^\s$]+)/))) {
	d.browser = "K-Meleon";
	d.version = m[1];
    } else if((m = ua.match(/Camino\/([^\s$]+)/))) {
	d.browser = "Camino";
	d.version = m[1];
    } else if((m = ua.match(/Safari\/([^\s$]+)/))) {
	d.browser = "Safari";
	d.version = m[1];
    } else if((m = ua.match(/NetFront\/([^\s$]+)/))) {
	d.browser = "NetFront";
	d.version = m[1];
    } else if((m = ua.match(/Opera[\/\s]([^\s$]+)/))) {
	d.browser = "Opera";
	d.version = m[1];
    } else if((m = ua.match(/Konqueror\/([^\s$;]+)/))) {
	d.browser = "Konqueror";
	d.version = m[1];
    } else if((m = ua.match(/WebPro/))) {
	d.browser = "WebPro";
	d.version = "";
    } else if((m = ua.match(/Mozilla\/5\.0.*rv:([^\)]*).*Gecko/))) {
	d.browser = "Mozilla";
	d.version = m[1];
    } else if((m = ua.match(/MSIE ([^;]+);/)) &&
	      (d.platform.indexOf("Win") >= 0 ||
	       d.platform.indexOf("Mac") >= 0)) {
	d.browser = "MSIE";
	d.version = m[1];
    }

    return d;
}

function decodeUserAgentTable(t)
{
    for(var i = 0; i < t.rows.length; i++) {
	var uaNode = t.rows[i].cells[2].childNodes[0].childNodes[0];
	var bold = false;
	if(uaNode.nodeName == "B") {
	    uaNode = uaNode.childNodes[0];
	    bold = true;
	}
	if(uaNode.nodeName != "#text") continue;
	var ua = uaNode.nodeValue;
	var decoded = decodeOneUserAgent(ua);
	if(bold) {
	    t.rows[i].cells[0].innerHTML = "<b>" + decoded.browser + " " + 
		decoded.version + "</b>";
	    t.rows[i].cells[1].innerHTML = "<b>" + decoded.platform + "</b>";
	} else {
	    t.rows[i].cells[0].innerHTML = decoded.browser + " " +
		decoded.version;
	    t.rows[i].cells[1].innerHTML = decoded.platform;
	}
    }
}

function decodeUserAgents()
{
    try {
	var goodTbody = document.getElementById("good-browsers");
	var badTbody = document.getElementById("bad-browsers");
	decodeUserAgentTable(goodTbody);
	decodeUserAgentTable(badTbody);
    } catch(e) {}
}
