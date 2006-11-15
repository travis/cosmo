dojo.provide("cosmo.ui.widget.ModalDialog");

dojo.require("cosmo.ui.widget.ButtonPanel");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.env");
dojo.require("dojo.widget.*");

dojo.widget.defineWidget("cosmo.ui.widget.ModalDialog", 
dojo.widget.HtmlWidget, {
        // Template stuff
        templatePath:dojo.uri.dojoUri(
            '../../cosmo/ui/widget/templates/ModalDialog/ModalDialog.html'),
        
        // Attach points
        fauxPopImageDiv: null,
        fauxPopContentDiv: null,
        fauxPopTextDiv: null,
        fauxPopButtonDiv: null,
        
        INFO: 'info',
        ERROR: 'error',
        CONFIRM: 'confirm',
        msg: '',
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
        center: function () {
            var w = dojo.html.getViewportWidth();
            var h = dojo.html.getViewportHeight();
            this.setLeft(parseInt((w - this.width)/2));
            this.setTop(parseInt((h - this.height)/2));
        },
        
        // Lifecycle crap
        postMixInProperties: function () {
            this.toggleObj =
                dojo.lfx.toggle[this.toggle] || dojo.lfx.toggle.plain;
            // Clone original show method
            this.showOrig = eval(this.show.valueOf());
            // Do sizing, positioning, content update
            // before calling stock Dojo show
            this.show = function (msg, l, c, r) {
                // Accommodate either original multiple param or
                // object param input
                if (typeof arguments[0] == 'object') {
                    var o = arguments[0];
                    if (o.msg) { msg = o.msg; }
                    if (o.btnsLeft) { l = o.btnsLeft; }
                    if (o.btnsCenter) { l = o.btnsCenter; }
                    if (o.btnsRight) { l = o.btnsRight; }
                }
                var bDiv = this.fauxPopButtonDiv;
                this.msg = msg || this.msg;
                this.btnsLeft = l || this.btnsLeft;
                this.btnsCenter = c || this.btnsCenter;
                this.btnsRight = r || this.btnsRight;
                this.width = this.width || DIALOG_BOX_WIDTH;
                this.height = this.height || DIALOG_BOX_HEIGHT;
                this.setWidth(this.width);
                this.setHeight(this.height);
                // Content area
                if (typeof this.msg == 'string') {
                    this.fauxPopTextDiv.innerHTML = this.msg;
                }
                else {
                    while(this.fauxPopTextDiv.firstChild) {
                        this.fauxPopTextDiv.removeChild(this.fauxPopTextDiv.firstChild);
                        this.fauxPopTextDiv.appendChild(msg);
                    }
                }
                // Modal dialog box
                if (bDiv.firstChild) {
                    bDiv.removeChild(bDiv.firstChild);
                };
                var panel = dojo.widget.createWidget(
                    'ButtonPanel', { btnsLeft: this.btnsLeft, btnsCenter: this.btnsCenter,
                    btnsRight: this.btnsRight }, this.fauxPopButtonDiv, 'last');
                this.center();
                this.domNode.style.display = 'block';
                // Call the original Dojo show method
                dojo.lang.hitch(this, this.showOrig);
                this.isDisplayed = true;
            };
            // Clone original hide method
            this.hideOrig = eval(this.hide.valueOf());
            // Clear buttons and actually take the div off the page
            this.hide = function () {
                // Call the original Dojo hide method
                dojo.lang.hitch(this, this.hideOrig);
                this.msg = '';
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
