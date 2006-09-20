/*
 * Copyright 2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

dojo.require(cosmo.env);
var uiStyles = '';
var uiPref = []; 
var arr = [];
var repl = null;
var dynRules = [];

dojo.io.bind({
    url: cosmo.env.getBaseUrl() + '/templates' + TEMPLATE_DIRECTORY + '/ui.css',
    sync: true,
    load: function(type, data, evt) { doStyles(type, data, evt); },
    error: function(type, error) { alert(error.message); },
    mimetype: "text/plain"
});

// FiXME: Refactor with objects
function doStyles(type, data, evt) {
    var uiStyles = data;

    // Remove comments
    uiStyles = uiStyles.replace(/\/\/.*/g, '');

    pat = /(\$\S+)(\s*=\s*)(.+)\n/g;
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

