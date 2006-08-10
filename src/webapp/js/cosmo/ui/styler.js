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

Styler = new function() {
    this.styles = [];
    this.maxIndex = 0;
    this.addStyle = function(name) {
        var dynStyle = document.createElement('style');
        dynStyle.type = 'text/css';
        dynStyle.media = 'all';
        document.getElementsByTagName("head")[0].appendChild(dynStyle);
        this.styles[name] = new StyleWrapper(document.styleSheets[this.maxIndex]);
        this.maxIndex++;
    };
    this.doOldDocumentDotWriteHack = function(rules) {
        var styleStr = '';
        styleStr += '<style type="text/css">\n';
        for (var i = 0; i < rules.length; i++) {
            styleStr += rules[i] + '\n';
        }
        styleStr += '</style>\n';
        document.write(styleStr);
    }
}

function StyleWrapper(sheet) {
    this.sheet = sheet;
    this.getRules = function() {
        var rules = document.all ? this.sheet.rules : this.sheet.cssRules;
        return rules;
    };
    this.splitRule = new RegExp('\s*(\{)\s*');
    this.rules = this.getRules();
    this.addRule = function(rule, index) {
        if (document.all) {
            var ruleItem = rule.replace(/\s*}/, '');
            var ruleItemArray = ruleItem.split(this.splitRule);
            this.sheet.addRule(ruleItemArray[0], ruleItemArray[1], index);
        }
        else {
            this.sheet.insertRule(rule, index);
        }
    }
    this.loadRules = function(rules) {
        for (var i = 0; i < rules.length; i++) {
            this.addRule(rules[i]);
        }
    }
}