dojo.provide("cosmo.ui.widget.About");

dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");


dojo.widget.defineWidget("cosmo.ui.widget.About", dojo.widget.HtmlWidget,
    {
        templateString: '<span></span>',

        // Props from template or set in constructor

        // Localized strings
        strings: {
            version: _('About.Version', cosmo.env.getVersion()),
            license: _('About.License', '<a href="http://www.apache.org/licenses/LICENSE-2.0">', '</a>'),
            info: _('About.Info', '<a href="http://cosmo.osafoundation.org/">', '</a>')
        },

        // Attach points

        fillInTemplate: function () {
            var node = this.domNode
            var main = null;
            var d = null;

            node.id = this.widgetId;
            node.style.textAlign = 'center';
            node.style.margin = 'auto';
            node.style.width = '100%';

            // Image
            d = _createElem('div');
            d.style.paddingTop = '16px';
            var img = _createElem('img');
            img.src = cosmo.env.getImagesUrl() + _("App.LogoUri");
            d.appendChild(img);
            node.appendChild(d);
            // Version
            d = _createElem('div');
            d.style.marginTop = '-4px';
            d.innerHTML = this.strings.version;
            node.appendChild(d);
            // License text
            d = _createElem('div');
            d.style.marginTop = '24px';
            d.innerHTML = this.strings.license;
            node.appendChild(d);
            // Info text
            d = _createElem('div');
            d.style.marginTop = '12px';
            d.innerHTML = this.strings.info;
            node.appendChild(d);
        },
        postCreate: function () {
        }
    },
    "html");


