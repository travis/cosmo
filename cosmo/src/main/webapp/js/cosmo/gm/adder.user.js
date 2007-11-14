// ==UserScript==
// @name          GreaseDoggy
// @namespace     http://www.osafoundation.org/greasemonkey
// @description   
// @include       *
// ==/UserScript==

//edit this 
const COLLECTIONS = [["my collection", "http://url_to_post_to"],
                     ["my other collection", "http://another_url"]];
 
const imgData = src="data:;base64,AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAQAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB1dXU5pqamt8jIyOPU1NTttra24JeXl8RxcXGjHBwcaAAAAD8AAAAhAAAAAwAAAAAAAAAAAAAAAAAAAAC2traCv8bj/erq6v/p6en/6Ojo/+vr6//u7u7/8fHx/+rq6vqenp7TFRUViQAAAHsAAABQAAAAEQAAAACtra1A9vb2/RxH9P/a2+D/5ubm/+bm5v/m5ub/oa7g/yxT7P+Xo9b/7u7u/9/f3/ZUVFSnAAAAgAAAAHsAAAAay8vLr+/v7/9GZ+v/hZTT/+fn5//p6en/6enp/+np6f/m5+n/Rmju/5ai0v/t7e3/8PDw/WVlZbEAAACAAAAALdTU1Mfv7+//kKLq/wY3/P+VodH/5OTk/+vr6//r6+v/6+vr/8zS7f8OPfj/wsbU/+/v7//x8fH8goKCUAAAAADHx8ek8vLy/9zg7v8COf7/CD37/zth5v9Xdd7/dIra/56q1P+vt9P/BDv9/1Nx3f/n5+f/9vb2/+np6ecAAAAAra2tQPb29v/x8fH/G1H2/wA///8AP///AD///wA///8AP///AD///wA///8EQv3/sbrV//Ly8v/5+fn/wsLCWAAAAADPz8+9g57j/wZM/P8ASP//AEj//w9T//9Eef//XYv//1+M//9Lfv//Mmz//2GG4f/p6en/+fn5/8/Pz5sARNUSBE3qtG2T4++nvvL/Vojy/0R//P97pf//gKj//4Co//+AqP//gKj//4Co//97ofP/4eHh//r6+v/Q0NCiAFPoegBGxCulpaUSzc3NuPj4+P/3+Pr/ts74/4ev+P9/rP7/gK3//4Ct//90pv//UY76/9vb2//l5eX/wMDAXQBb41EAAAAAAAAAAAAAAAC+vr5q8fHx9vv7+//8/Pz/9Pj9/7HO9/9Fj/3/Bmr//wBm//85f+r/fZ7Q3QBEqhIAAAAEAAAAAAAAAAAAAAAAAAAAALu7ux/Q0NCl6urq6vr6+v/6+vr/frLz/wBx//8Acf//AHH//wBx//8AZeShAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAv7+/VKa2x1wEeffpAHv//wB4+eMAcuyfAG3jNgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAHrsXgB35h4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//8AAMAPAACAAQAAAAAAAAAAAAAAAQAAAAEAAAAAAACAAAAAAAAAAAAAAABwAAAA+AAAAP+AAAD/5wAA//8AAA==";

function getEventNodes(){
    xpExpr = "//*[@class='vevent']";
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
    var image = document.createElement('img');
    image.src = imgData;
    image.border = 0;
    span.appendChild(image);
    node.appendChild(span);
    span.appendChild(createSelect(node));
};

function createSelect(node){
    var sel = document.createElement('select');
    var option = document.createElement("option");
    option.value = null;
    option.appendChild(document.createTextNode("Add To Chandler Server..."));
    sel.appendChild(option);
    for (var x = 0; x < COLLECTIONS.length; x++){
        var option = document.createElement("option");
        option.value = COLLECTIONS[x][1];
        option.appendChild(document.createTextNode(COLLECTIONS[x][0]));
        sel.appendChild(option);
    }
    var onChangeFunction = function(){
        var url = sel.options.item(sel.selectedIndex).value;
        addEvent(node, url);
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

function hCalToBase64(xml){
    return toBase64((new XMLSerializer()).serializeToString(filterHCal(xml)));
}

function filterHCal(xml){
    return xml;
}

var base64Alphabet =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZ' +
    'abcdefghijklmnopqrstuvwxyz' +
    '0123456789+/=';

function toBase64(string){
    string = _utf8_encode(string);
    var i = 0;
    var char1, char2, char3;
    var enc1, enc2, enc3, enc4;

    var out = '';

    var alpha = base64Alphabet;

    do {
        char1 = string.charCodeAt(i++);
        char2 = string.charCodeAt(i++);
        char3 = string.charCodeAt(i++);

        enc1  = char1 >> 2;
        enc2 = ((char1 & 3) << 4) | char2 >> 4;
        enc3 = ((char2 & 15) << 2) | char3 >> 6;
        enc4 = char3 & 63;

        if (isNaN(char2)){
            enc3 = enc4 = 64;
        } else if (isNaN(char3)){
            enc4 = 64;
        }

        out = out +
              alpha.charAt(enc1) +
              alpha.charAt(enc2) +
              alpha.charAt(enc3) +
              alpha.charAt(enc4);
    } while (i < string.length);

    return out;

}

function _utf8_encode(string) {  
    string = string.replace(/\r\n/g,"\n");  
    var utftext = "";  
         for (var n = 0; n < string.length; n++) {  
             var c = string.charCodeAt(n);  
             if (c < 128) {  
            utftext += String.fromCharCode(c);  
        }  
        else if((c > 127) && (c < 2048)) {  
            utftext += String.fromCharCode((c >> 6) | 192);  
            utftext += String.fromCharCode((c & 63) | 128);  
        }  
        else {  
            utftext += String.fromCharCode((c >> 12) | 224);  
            utftext += String.fromCharCode(((c >> 6) & 63) | 128);  
            utftext += String.fromCharCode((c & 63) | 128);  
        }  
         }  
         return utftext;  
} 


