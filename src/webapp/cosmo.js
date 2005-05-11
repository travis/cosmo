
var conH;
var winH;
var footerH = 48;
var browserH = 88;

/* -- browser detect -- */

var isIE = false;
if (navigator.appName.indexOf("Microsoft") != -1) {
    isIE = true;
}

function setFoot() {
    if (isIE) {
        conH = document.body.scrollHeight;
        winH = document.body.clientHeight;
    }
    else {
        conH = document.height;
        winH = window.innerHeight;
    }

    var myHeight = winH - conH - footerH + browserH;
    if (myHeight > 60) {
        var footerSpacer = document.getElementById("footerSpacer");
        footerSpacer.setAttribute('height', myHeight);
    }
}
