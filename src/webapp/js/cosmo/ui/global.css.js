dojo.require(scooby.env);
var uiPrefReq = new Ajax();
var uiStyles = '';
var uiPref = []; 
var arr = [];
var repl = null;
var dynRules = [];

uiPrefReq.async = false;
uiStyles = uiPrefReq.doGet(scooby.env.getBaseUrl() + '/templates/' + TEMPLATE_DIRECTORY + '/ui.css');
if (uiStyles.status != 200) {
    alert('Could not load stylesheet.');
}
else {
    doStyles(uiStyles.responseText);
}

// FiXME: Refactor with objects
function doStyles(str) {
    var uiStyles = str;

    // Remove comments
    uiStyles = uiStyles.replace(/\/\/.*/g, '');

    pat = /(\$\S+)(\s*=\s*)(\S+)(\s*)\n/g;
    while (arr = pat.exec(uiStyles)) {
        repl = new Object();
        repl.rule = arr[0];
        repl.name = arr[1];
        repl.val = arr[3];
        dynRules.push(repl);
    }
    for (var i = 0; i < dynRules.length; i++) {
        var pat = new RegExp('\\' + dynRules[i].name, 'gi');
        uiStyles = uiStyles.replace(dynRules[i].rule, '');
        uiStyles = uiStyles.replace(pat, dynRules[i].val);
    }
    
    // Replace line breaks with spaces
    uiStyles = uiStyles.replace(/\n/g, ' ');
    // Replace multiple spaces with single spaces
    uiStyles = uiStyles.replace(/\s+/g, ' ');
    // Replace multi-line comments
    uiStyles = uiStyles.replace(/\/\*.*?\*\//g, '');
    // Add line break after each style declaration
    uiStyles = uiStyles.replace(/}/g, '}\n');
    // Trim
    uiStyles = uiStyles.replace(/^\s+/g, '').replace(/\s+$/g, '');

    //Log.print(uiStyles);

    // Split into array of styles
    uiStyles = uiStyles.split(/\n/g);

    // Safari can't deal with DOM methods for styles
    if (navigator.userAgent.indexOf('Safari') > -1) {
        Styler.doOldDocumentDotWriteHack(uiStyles);
    }
    else {
        // Create stylesheet and load styles
        // ===================
        Styler.addStyle('global');
        Styler.styles['global'].loadRules(uiStyles);
    }
}


uiPrefReq = null;

