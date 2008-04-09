// ==UserScript==
// @name          GreaseDoggy
// @namespace     http://www.osafoundation.org/greasemonkey
// @description   
// @include       *
// ==/UserScript==

//edit this 
const COLLECTIONS = [["My Calendar", "https://hub.chandlerproject.org/atom/collection/2a37a04d-d0f9-44c1-a3e2-5380aff81245"]];
// two collection example
// const COLLECTIONS = [["my collection", "http://collection_url"], ["my other collection", "http://another_url"]];
 
const imgData = src="data:;base64,AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAQAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB1dXU5pqamt8jIyOPU1NTttra24JeXl8RxcXGjHBwcaAAAAD8AAAAhAAAAAwAAAAAAAAAAAAAAAAAAAAC2traCv8bj/erq6v/p6en/6Ojo/+vr6//u7u7/8fHx/+rq6vqenp7TFRUViQAAAHsAAABQAAAAEQAAAACtra1A9vb2/RxH9P/a2+D/5ubm/+bm5v/m5ub/oa7g/yxT7P+Xo9b/7u7u/9/f3/ZUVFSnAAAAgAAAAHsAAAAay8vLr+/v7/9GZ+v/hZTT/+fn5//p6en/6enp/+np6f/m5+n/Rmju/5ai0v/t7e3/8PDw/WVlZbEAAACAAAAALdTU1Mfv7+//kKLq/wY3/P+VodH/5OTk/+vr6//r6+v/6+vr/8zS7f8OPfj/wsbU/+/v7//x8fH8goKCUAAAAADHx8ek8vLy/9zg7v8COf7/CD37/zth5v9Xdd7/dIra/56q1P+vt9P/BDv9/1Nx3f/n5+f/9vb2/+np6ecAAAAAra2tQPb29v/x8fH/G1H2/wA///8AP///AD///wA///8AP///AD///wA///8EQv3/sbrV//Ly8v/5+fn/wsLCWAAAAADPz8+9g57j/wZM/P8ASP//AEj//w9T//9Eef//XYv//1+M//9Lfv//Mmz//2GG4f/p6en/+fn5/8/Pz5sARNUSBE3qtG2T4++nvvL/Vojy/0R//P97pf//gKj//4Co//+AqP//gKj//4Co//97ofP/4eHh//r6+v/Q0NCiAFPoegBGxCulpaUSzc3NuPj4+P/3+Pr/ts74/4ev+P9/rP7/gK3//4Ct//90pv//UY76/9vb2//l5eX/wMDAXQBb41EAAAAAAAAAAAAAAAC+vr5q8fHx9vv7+//8/Pz/9Pj9/7HO9/9Fj/3/Bmr//wBm//85f+r/fZ7Q3QBEqhIAAAAEAAAAAAAAAAAAAAAAAAAAALu7ux/Q0NCl6urq6vr6+v/6+vr/frLz/wBx//8Acf//AHH//wBx//8AZeShAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAv7+/VKa2x1wEeffpAHv//wB4+eMAcuyfAG3jNgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAHrsXgB35h4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//8AAMAPAACAAQAAAAAAAAAAAAAAAQAAAAEAAAAAAACAAAAAAAAAAAAAAABwAAAA+AAAAP+AAAD/5wAA//8AAA==";

function getEventNodes(){
    xpExpr = "//*[contains(@class, 'vevent')]";
    var xpResult = document.evaluate(xpExpr, document, null, XPathResult.ANY_TYPE,null);
    var nodes = [];
    
    if (!xpResult){
        return nodes;
    }

    var nextNode = xpResult.iterateNext();
    while (nextNode){
        nodes.push(nextNode);
        nextNode = xpResult.iterateNext();
    }
    return nodes;
}

function addSelect(node) {
    var span = document.createElement('span');
    span.className = "gdogSelect";
    var image = createCosmoIcon();
    span.appendChild(image);
    node.appendChild(span);
    span.appendChild(createSelect(node));
};

function createCosmoIcon(){
    var image = document.createElement('img');
    image.src = imgData;
    image.border = 0;
    return image;
}

function addAddAllLink(){
    var addAllDiv = document.createElement("div");
    var image = createCosmoIcon();
    addAllDiv.appendChild(image);
    addAllDiv.appendChild(createSelect());
    document.body.appendChild(document.createElement("br"))
    document.body.appendChild(addAllDiv);
}

// summary: creates a select box to put on a given node
// description: Uses the COLLECTIONS variable to populate the select box.
//              Adds an "onchange" so that when the select item is changed
//              the node/vevent is posted to the selected collection.
//
//              If no node is given, a similar select box is created, the difference
//              being that the onchange will cause ALL events on the page to be posted
//              to the specified URL.
function createSelect(node){
    var sel = document.createElement('select');
    var option = document.createElement("option");
    option.value = null;
    option.appendChild(document.createTextNode(node ? "Add To Chandler Server..." 
                                               : "Add All Events To Chandler Server"));
    sel.appendChild(option);
    for (var x = 0; x < COLLECTIONS.length; x++){
        var option = document.createElement("option");
        option.value = COLLECTIONS[x][1];
        option.appendChild(document.createTextNode(COLLECTIONS[x][0]));
        sel.appendChild(option);
    }
    var onChangeFunction = function(){
        var url = sel.options.item(sel.selectedIndex).value;
        if (node){
            addEvent(node, url);
        } else {
            addAllEvents(url);
        }
        sel.selectedIndex = 0;
    }
    sel.addEventListener("change", onChangeFunction, true);

    return sel;
}

var nodes = getEventNodes();
if (nodes){
    for (var x = 0; x < nodes.length; x++){
        addSelect(nodes[x]);
    }
    if (nodes.length > 0){
        addAddAllLink();
    }
}

function addEvent(xml, url){
    GM_xmlhttpRequest({
        method: "POST",
        url: url,
        headers: {
            "Content-Type": "application/xhtml+xml"
        },
        data: hCalToBase64(xml),
        onload: function(details) {
            alert([
                details.status,
                details.statusText,
                details.readyState,
                details.responseHeaders,
                details.responseText
            ].join("\n"))
        },
        onerror: function(details) {
            alert("error: " + [
                details.status,
                details.statusText,
                details.readyState,
                details.responseHeaders,
                details.responseText
            ].join("\n"))
        }
    });
}

function addAllEvents(){
    alert("not implemented yet!")
}

function hCalToBase64(xml){
    return (new XMLSerializer()).serializeToString(filterHCal(xml));
}

function filterHCal(xml){
    var newXml = xml.cloneNode(true);
    var z = newXml.childNodes
    for (var i in newXml.childNodes){
        var node = newXml.childNodes[i];
        console.log(node.className);
        if (node.className == "gdogSelect"){
            newXml.removeChild(node);
        }
    }
    return newXml;
}

