/*
 * Copyright 2006-2008 Open Source Applications Foundation
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

dojo.provide("cosmo.view.loading");

dojo.require("cosmo.ui.ContentBox");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");

cosmo.view.loading.statusProcessing = false;

cosmo.view.loading.StatusMessage = function (p) {
    var self = this;
    var params = p || {};
    for (var n in params) { this[n] = params[n]; }

    this.width = 200;
    this.height = 80;

    this.renderSelf = function () {
        if (!this.hasBeenRendered) {
            this.domNode.innerHTML = _('App.Status.LoadingCollection');
            this.domNode.style.width = this.width + 'px';
            this.domNode.style.height = this.height + 'px';
            this.domNode.style.lineHeight = this.height + 'px';
            this.hide(1);
        }
        var left = ((this.parent.width - this.width) /  2);
        var top = ((this.parent.height - this.height) /  2);
        this.setPosition(left, top);
    };
    this.show = function () {
        if (cosmo.view.loading.statusProcessing) { return false; }
        cosmo.view.loading.statusProcessing = true;
        this.domNode.style.zIndex = 1000;
        dojo.fadeIn({node: this.domNode, duration: 1}).play();
    };
    this.hide = function (duration) {
        dojo.fadeOut({node: this.domNode, 
                      duration: duration || 500,
                      onEnd: dojo.hitch(this, function () {
                          cosmo.view.loading.statusProcessing = false;
                          this.domNode.style.zIndex = -1;
                      })
                     }).play();
    };

    dojo.subscribe('cosmo:calEventsLoadSuccess', 
                   dojo.hitch(this, function(){this.hide()}));
};

cosmo.view.loading.StatusMessage.prototype =
    new cosmo.ui.ContentBox();

