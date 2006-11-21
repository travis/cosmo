dojo.provide("cosmo.ui.widget.ModalDialog");

dojo.require("dojo.widget.*");
dojo.require("dojo.html.*");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.ui.widget.ButtonPanel");
dojo.require("cosmo.ui.widget.Button");

dojo.widget.defineWidget("cosmo.ui.widget.ModalDialog", 
dojo.widget.HtmlWidget, {
        // Template stuff
        templatePath:dojo.uri.dojoUri(
            '../../cosmo/ui/widget/templates/ModalDialog/ModalDialog.html'),
        
        // Attach points
        containerNode: null,
        titleNode: null,
        promptNode: null,
        imageNode: null,
        contentNode: null,
        buttonPanelNode: null,
        
        INFO: 'info',
        ERROR: 'error',
        CONFIRM: 'confirm',
        title: '',
        prompt: '',
        content: null,
        btnsLeft: [],
        btnsCenter: [],
        btnsRight: [],
        defaultAction: null,
        isDisplayed: false,
        
        // Instance methods
        setTop: function (n) {
            var s = n.toString();
            s = s.indexOf('%') > -1 ? s : parseInt(s) + 'px';
            this.domNode.style.top = s;
        },
        setLeft: function (n) {
            var s = n.toString();
            s = s.indexOf('%') > -1 ? s : parseInt(s) + 'px';
            this.domNode.style.left = s;
        },
        setWidth: function (n) {
            var s = n.toString();
            s = s.indexOf('%') > -1 ? s : parseInt(s) + 'px';
            this.domNode.style.width = s;
        },
        setHeight: function (n) {
            var s = n.toString();
            s = s.indexOf('%') > -1 ? s : parseInt(s) + 'px';
            this.domNode.style.height = s; 
        },
        setContentAreaHeight: function () {
            var spacer = this.buttonPanelNode.offsetHeight;
            spacer += 32;
            if (this.title) {
                spacer += this.titleNode.offsetHeight;
            }
            if (this.prompt) {
                spacer += this.promptNode.offsetHeight;
            }
            this.contentNode.style.height = (this.domNode.offsetHeight - spacer) + 'px';
        },
        setTitle: function (title) {
            this.title = title || this.title;
            if (this.title) {
                this.titleNode.className = 'dialogTitle';
                this.titleNode.innerHTML = this.title;
            }
            else {
                this.titleNode.className = 'invisible';
                this.titleNode.innerHTML = '';
            }
            return true;
        },
        setPrompt: function (prompt) {
            this.prompt = prompt || this.prompt;
            if (this.prompt) {
                this.promptNode.className = 'dialogPrompt';
                this.promptNode.innerHTML = this.prompt;
            }
            else {
                this.promptNode.className = 'invisible';
                this.promptNode.innerHTML = '';
            }
            return true;
        },
        setContent: function (content) {
            this.content = content || this.content;
            // Content area
            if (typeof this.content == 'string') {
                this.contentNode.innerHTML = this.content;
            }
            else {
                var ch = this.contentNode.firstChild;
                while(ch) {
                    this.contentNode.removeChild(ch);
                    ch = this.contentNode.firstChild;
                }
                this.contentNode.appendChild(this.content);
            }
            return true;
        },
        setButtons: function (l, c, r) {
            this.btnsLeft = l || this.btnsLeft;
            this.btnsCenter = c || this.btnsCenter;
            this.btnsRight = r || this.btnsRight;
            var bDiv = this.buttonPanelNode;
            if (bDiv.firstChild) {
                bDiv.removeChild(bDiv.firstChild);
            };
            var panel = dojo.widget.createWidget(
                'ButtonPanel', { btnsLeft: this.btnsLeft, btnsCenter: this.btnsCenter,
                btnsRight: this.btnsRight }, bDiv, 'last');
            return true;
        },
        render: function () {
            return (this.setTitle() &&
                this.setPrompt() &&
                this.setContent() &&
                this.setButtons());
        },
        center: function () {
            var w = dojo.html.getViewportWidth();
            var h = dojo.html.getViewportHeight();
            this.setLeft(parseInt((w - this.width)/2));
            this.setTop(parseInt((h - this.height)/2));
            return true;
        },
        
        // Lifecycle crap
        postMixInProperties: function () {
            this.toggleObj =
                dojo.lfx.toggle[this.toggle] || dojo.lfx.toggle.plain;
            // Clone original show method
            this.showOrig = eval(this.show.valueOf());
            // Do sizing, positioning, content update
            // before calling stock Dojo show
            this.show = function (content, l, c, r, title, prompt) {
                // Accommodate either original multiple param or
                // object param input
                if (typeof arguments[0] == 'object') {
                    var o = arguments[0];
                    if (o.content) { this.content = o.content; }
                    if (o.btnsLeft) { l = o.btnsLeft; }
                    if (o.btnsCenter) { l = o.btnsCenter; }
                    if (o.btnsRight) { l = o.btnsRight; }
                    if (o.title) { this.title = o.title; }
                    if (o.prompt) { this.prompt = o.prompt; }
                }
                else {
                    this.content = content || this.content;
                    this.btnsLeft = l || this.btnsLeft;
                    this.btnsCenter = c || this.btnsCenter;
                    this.btnsRight = r || this.btnsRight;
                    this.title = title || this.title;
                    this.prompt = prompt || this.prompt;
                }
                // Sizing
                this.width = this.width || DIALOG_BOX_WIDTH;
                this.height = this.height || DIALOG_BOX_HEIGHT;
                this.setWidth(this.width);
                this.setHeight(this.height);
                
                // Don't display until rendered and centered
                if (this.render() && this.center()) { 
                    this.domNode.style.display = 'block';
                    // Have to measure for content area height once div is actually on the page
                    this.setContentAreaHeight();
                    // Call the original Dojo show method
                    dojo.lang.hitch(this, this.showOrig);
                    this.isDisplayed = true;
                }
            };
            // Clone original hide method
            this.hideOrig = eval(this.hide.valueOf());
            // Clear buttons and actually take the div off the page
            this.hide = function () {
                // Call the original Dojo hide method
                dojo.lang.hitch(this, this.hideOrig);
                this.content = null;
                this.btnsLeft = [];
                this.btnsCenter = [];
                this.btnsRight = [];
                this.width = null;
                this.height = null;
                this.domNode.style.display = 'none';
                this.isDisplayed = false;
            };
        },
        
        // Toggling visibility
        toggle: 'plain' } );
