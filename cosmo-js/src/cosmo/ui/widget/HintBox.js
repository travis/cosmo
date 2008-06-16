dojo.provide("cosmo.ui.widget.HintBox");

dojo.require("dijit.form.ValidationTextBox");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.TimeTextBox");

dojo.declare(
	"cosmo.ui.widget._HintMixin", null,
	{
        hintMessage: "Hint",
        _hintShown: false,
		updateHint: function(isFocused){
            if (isFocused && this._hintShown){
                this.hideHint();
            }
            else if (!isFocused && this._isEmpty(this.textbox.value)){
                this.showHint();
            }
        },
        hideHint: function(){
            // can't use setValue, or this will recurse
            this.textbox.value = '';
            dojo.removeClass(this.textbox, "hintText");
            this._hintShown = false;
        },
        showHint: function(){
            this.textbox.value = this.hintMessage;
            dojo.addClass(this.textbox, "hintText");
            this._hintShown = true;
        },
        validate: function(isFocused) {
            this.updateHint(isFocused);
            return this.inherited(arguments);
        },
        setValue: function(){
            this.hideHint();
            this.inherited(arguments);
        },
        getValue: function(){
            if (this._hintShown) return "";
            else return this.inherited(arguments);
        }
    }
);

dojo.declare(
    "cosmo.ui.widget.HintBox",
    [dijit.form.ValidationTextBox, cosmo.ui.widget._HintMixin]);

dojo.declare(
    "cosmo.ui.widget.DateHintBox",
    [dijit.form.DateTextBox, cosmo.ui.widget._HintMixin]);

dojo.declare(
    "cosmo.ui.widget.TimeHintBox",
    [dijit.form.TimeTextBox, cosmo.ui.widget._HintMixin]);