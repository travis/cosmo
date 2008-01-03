dojo.provide("cosmotest.ui.testloader");
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.html");

cosmotest.ui.button = function(output){
    dojo.require("cosmo.ui.widget.Button");
    var button = new cosmo.ui.widget.Button({
        text: "Test Button"
    },output);
}

cosmotest.ui.testloader.tests = [
    [ "button", cosmotest.ui.button],
    [ "button1", cosmotest.ui.button]
]

cosmotest.ui.testloader.init = function(){
    var tests = cosmotest.ui.testloader.tests;
    this._form  = $("selection_form");
    this._output = $("output");
    var testNameSet = this.getTestNameSet();

    for (var x=0; x < tests.length; x++){
        var input = cosmo.util.html.createInput({
            type: "checkbox", 
            name: "testbox", 
            checked: testNameSet[tests[x][0]],
            value: x                  
        },this._form);
        this._form.appendChild(document.createTextNode(tests[x][0]))
        this._form.appendChild(document.createElement("br"));
    }
    
    self = this;
    document.onkeyup = function (e) {
        e = !e ? window.event : e;
        
        if (String.fromCharCode(e.keyCode) == 'R'){
            location = self.createLink();
        }

        if (String.fromCharCode(e.keyCode) == 'T'){
            self.runTests();
        }

        if (String.fromCharCode(e.keyCode) == 'U'){
            uncheckAll();
        }

        if (String.fromCharCode(e.keyCode) == 'A'){
            checkAll();
        }

        if (String.fromCharCode(e.keyCode) == 'C'){
            clearOutput();
        }
    }
}

cosmotest.ui.testloader.getSelectedTests = function(){
    var form = this._form;
    var nodeList = form.testbox;
    var selected = [];
    for (var x = 0; x < form.testbox.length; x++){
        if (form.testbox.item(x).checked){
            selected.push(this.tests[x]);
        }
    }
    return selected;
}

cosmotest.ui.testloader.runTests = function(){
    var tests = this.getSelectedTests();
    for (var x = 0; x < tests.length; x++){
        var test = tests[x];
        var testDiv = document.createElement("div");
        var testDivLabel = document.createElement("div");
        var testDivOutput = document.createElement("div");
        
        testDiv.id = test[0] + "_div";
        testDivLabel.id = test[0] + "_label";
        testDivOutput.id = test[0] + "_output";
        testDiv.appendChild(testDivLabel);
        testDiv.appendChild(testDivOutput);

        testDivLabel.appendChild(document.createTextNode(test[0] + ":"))
        this._output.appendChild(testDiv);
        test[1](testDivOutput);
    }
}

cosmotest.ui.testloader.createLink = function(){
    var tests = this.getSelectedTests();
    var qs = "?tests=";
    dojo.map(tests, function(test){
        qs += test[0] + ",";
    }) 
    var link = location.href.split("?")[0] + qs;
    return link;
}

cosmotest.ui.testloader.getTestNameSet = function(){
    var testNames = location.href.split("=")[1].split(",");
    var testNameSet = {};
    dojo.map(testNames, function(testName){
        testNameSet[testName] = {};
    });
    return testNameSet;
}
