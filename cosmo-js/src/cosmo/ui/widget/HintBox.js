dojo.provide("cosmo.ui.widget.HintBox");

dojo.require("dijit.form.ValidationTextBox");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.TimeTextBox");

dojo.declare(
	"cosmo.ui.widget._HintMixin", null,
	{
        hintMessage: "Hint",
        _hintShown: false,
		displayHint: function(isFocused){
            if (isFocused && this._hintShown){
                this.setValue('');
                dojo.addClass("hintText");
                this._hintShown = false;
            }
            else if (!isFocused && !this._hintShown){
                this.setValue(this.hintMessage);
                dojo.removeClass("hintText");
                this._hintShown = true;
            }
        }
    }
);

dojo.declare(
    "cosmo.ui.widget.HintBox",
    [dijit.form.ValidationTextBox, cosmo.ui.widget._HintMixin],
    {// ought to use dojo.connect for this, but test without it
        validate: function(isFocused) {
            this.displayHint(isFocused);
            return this.inherited(arguments);
        }
    }
);