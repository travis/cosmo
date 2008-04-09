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

/**
 * @fileoverview This is an implementation for resizable items uzing a  function to define
 * the scaling of an absolutely positioned elements between two "keyframe" prototypes.
 * This works in two forms: declarative and method based
 * @author Jeremy Epstein mailto:eggfree@eggfree.net
 * @license Apache License 2.0
 *
 * Some examples -- format is (screenwidth, screenheight, left, top, right, botton)
 * 125px wide, left-aligned
 * min=(125,125,0,0,125,125), max=(1600,1200,0,0,125,1200)
 * 250px wide, right-aligned
 * min(300,300,50,0,300,300), max(1600,1200,1350,0,1600,1200)
 */
//temporarily disabling dojo to isolate it from code handling
dojo.provide("cosmo.ui.resize");
/**
 * viewports is the collection and controller of an individual viewport it also contains the
 * utility functions used by all viewports
 */

/**
 * This is a handy event wrapper so I don't have to rely on anyone else. Feel free to link this
 * to an alternate event system of your choice.
 */
cosmo.ui.resize.EventWrapper = new function () {
    var activeListeners = {};
    var self = this;
    /**
     * returns mouse position relative to application window.
     * @param {Event} e
     */
    self.isLeftClick = function (e) {
        if (window.event)
            return (window.event.button == 1)
        return (e.button == 0)
    }

    self.mouseX = function (e) {
        if (!e)
            return window.event.clientX
        return e.clientX
    }
    /**
     * returns mouse position relative to application window.
     * @param {Event} e
     */
    self.mouseY = function (e) {
        if (!e)
            return window.event.clientY
        return e.clientY
    }

    self.cleanupListeners = function () {

        for (listener in activeListeners)
            activeListener[listener].element.detachEvent(activeListener[event]);
    }

    self.listen = function (element, eventType, func) {
        if (typeof (element.attachEvent) != "undefined")
            element.attachEvent("on"+eventType, func);
        else
            element.addEventListener(eventType, func, false);
    }

    self.stopListen = function (element,eventType,func) {
        if(typeof(element.detachEvent) != "undefined")
            element.detachEvent("on"+eventType, func)
        else
            element.removeEventListener(eventType, func,false)
    }
    self.cancelBubble = function (event) {
        if (typeof (window.event) != "undefined") {
            event.cancelBubble = true;
            event.returnValue = false
        }
        else {
            event.stopPropagation();
            event.preventDefault();
        }
    }
}


/**
 * this is the main viewport controller/manager
 */
cosmo.ui.resize.Viewports = new function () {

            var self = this;
        var viewports = {};
        var handles = {};
        var Ev = cosmo.ui.resize.EventWrapper
        /**
         * Remove Entry    this function removes an entry from an object
         * @param obj    an object you want to remove an entry from
         * @param strKey    key value indexing the object in the array.
         */
        self.removeEntry = function (obj, strKey) {
            var tmpObj = {}
            for(entry in obj)
                if(entry != strKey)
                    tmpObj[entry] = obj[entry]
            obj = null;
            obj = tmpObj;
        }

        /**
         * This function derives a "blended" value to determine one length width etc...
         * @param {int} a
         * @param {int} b
         * @param {int} c
         * @param {int} d
         * @param {int} e
         */
        self.getBlend = function (a,b,c,d,e) {
            /* this function applies a simple transform to extract a value*/
            //return Math.min( c,Math.max((c-a)/(d-b)*(e-a)+ a,0) )
                var baseWidth = Math.max(Math.min(e,d),b)
            var retVal = a;
            if (d-b != 0) {
                 retVal = (c-a)/(d-b) *(baseWidth-b)+ a
            }
            return retVal
        }

        /**
         * This method returns an array of objects with a given editType Value.
         * @param {string} s_type
         */
        self.getElementsByEditType = function (s_type) {
            var validTypes = ["DIV","IMG","TABLE","A"]
            var returnEls = []
            for (var v = 0; v < validTypes.length; v++) {
                var tmpEls = document.getElementsByTagName(validTypes[v])
                for (var i=0; i < tmpEls.length ; i++)
                    if (tmpEls[i].getAttribute("type") == s_type)
                        returnEls.push(tmpEls[i]);
            }
            return returnEls
        }

        /**
         * initializes viewports and handles from markup
         */
        self.initialize = function () {
            var el
            var viewportEls = self.getElementsByEditType("viewport")
            for (var i=0;i <viewportEls.length; i++)
                 new cosmo.ui.resize.Viewport(viewportEls[i]);

            var handle_els = self.getElementsByEditType("handle")
            for (var i = 0;i < handle_els.length; i++)
                 new cosmo.ui.resize.Handle(handle_els[i]);

            self.resize()
            Ev.listen(window,"resize", self.resize)

            //window.onresize = function () {alert("hi")}//self.resize
        }


        /**
         * adds viewport to controller and scaling queue
         * @param {Object} viewport
         */
        self.addViewport = function (viewport) {
            if(typeof(viewports[viewport.id])!= "undefined") {
                throw("there is already a viewport with id" + viewport.id + ".\n please choose another name")
            }
            viewports[viewport.id] = viewport
        }

        /**
         * removes viewport from controller an queue-- shoud cleanup.
         * @param {string} viewportId  guid of viewport
         */
        self.removeViewport = function (viewportId) {
            self.removeEntry(viewports,viewportId)
        }

        self.getViewport = function (viewportId) {
            return viewports[viewportId]
        }

        /**
         * executes resize when bound to window resize.
         */
        self.resize = function () {

            // declare variables
            var windowWidth;
            var windowHeight;
            // obtain current window sizes
            if (typeof(document.all) == "undefined") {
                windowWidth = window.innerWidth;
                windowHeight = window.innerHeight;
            }
            else {
                windowWidth = document.documentElement.clientWidth;
                windowHeight = document.documentElement.clientHeight;
             }

            var old_overflow = document.body.style.overflow
            //iterate through each viewport and scale
            // I removed shadow calculations as that is handled separately
                for (viewport in viewports)
                viewports[viewport].resize(windowHeight,windowWidth);
            document.body.style.overflow = "hidden"


        }

        /**
         * Utility funciton to parse an integer array.
         * @param     strArray array serialized to string
         * @return   array of integers
         */
        self.parseIntArray = function (strArray) {
            var tmpArr = strArray.split(",")
            var rtnArr = []
            for(var i =0;i<tmpArr.length;i++) {
                var al = parseInt(tmpArr[i])
                if(!isNaN(al))
                    rtnArr.push(al);
            }
            return rtnArr
        }
}

/**
 *  the viewport forms the basic resize unit. A viewport is a block element
 * absolutely positioned.
 * @param    {dom element}el        a valid dom element (not an id)
 * @return    {cosmo.ui.resize.viewport} self    object for pulling Id
 */
cosmo.ui.resize.Viewport = function (el) {

        var self = this;
        self.id = el.getAttribute("id");
        /* public variables these work with model factories for
         * choosing render methods and are intended for interpretation by IDEs
         * like APTANA
         */
        self.SHADOW = "dropShadow";
        self.NO_SHADOW = "noshadow";
        self.DIALOG = "dialog";
        self.FRAME = "frame";

        /* private variables */
        var minSize = null;
        var maxSize = null;
        var resizeChildren = {};
        var element = el;

        /* 'inherited' functions*/
        var removeEntry = cosmo.ui.resize.Viewports.removeEntry
        var parseIntArray = cosmo.ui.resize.Viewports.parseIntArray
        var getBlend = cosmo.ui.resize.Viewports.getBlend

        /*setters and getters*/
        this.setMinSize = function (array) {minSize = array}
        this.setMaxSize = function (array) {maxSize = array}
        this.getMinSize = function () {return minSize}
        this.getMaxSize = function () {return maxSize}


        /* constants for use */
        var SWIDTH = 0;
        var SHEIGHT = SWIDTH + 1;
        var LT = SHEIGHT + 1;
        var TP = LT + 1;
        var RT = TP + 1;
        var BM = RT + 1;

        /* set minSizealue*/
        if (element.getAttribute("min")!= null)
            minSize = parseIntArray(element.getAttribute("min"));

        if (element.getAttribute("max")!= null)
            maxSize = parseIntArray(element.getAttribute("max"));

         /* public functions*/
        self.addResize = function (s_key, method) {resizeChildren[s_key] = method}
        self.removeResize = function (entry) {removeEntry(resizeChildren,entry)}
        self.cleanup = function () {
            arrMin = null;
            arrMax = null;
            resizeChildren = null;
            resizeEl = null;
            cosmo.ui.resize.viewports.removeViewport(self.id)
        }

        /**
         * Rescales the viewport with the default algorithm
         * @param {int} windowHeight
         * @param {int} windowWidth
         */
        self.resize = function (windowHeight, windowWidth) {
             var inFrameDimensions = null;
            var l;
            var t;
            var w;
            var h;

            if (minSize[SWIDTH] > 0) {
                l = getBlend(minSize[LT],minSize[SWIDTH],maxSize[LT],maxSize[SWIDTH],windowWidth);
                    t = getBlend(minSize[TP],minSize[SHEIGHT],maxSize[TP],maxSize[SHEIGHT],windowHeight);
                    w = getBlend(minSize[RT],minSize[SWIDTH],maxSize[RT],maxSize[SWIDTH],windowWidth) - l;
                    h = getBlend(minSize[BM],minSize[SHEIGHT],maxSize[BM],maxSize[SHEIGHT],windowHeight)- t;
                inFrameDimensions = self.keepInFrame(l,t,w,h);
                if (inFrameDimensions != null) {
                   l = inFrameDimensions.left;
                    t = inFrameDimensions.top;
                    w = inFrameDimensions.width;
                    h = inFrameDimensions.height;
                    }
                h = Math.max(0,h);
                w = Math.max(0,w);
                    with (element.style) {
                        left = l + "px";
                        top = t + "px";
                        width = w  + "px";
                        height = h  + "px";
                        if (clip)
                                clip = (typeof (document.all) != "undefined" ? "auto":"rect(2px auto auto 2px)");
                    }
                element.style.display="block"

                //SHADOW, VISIBILITY ETC..
                for (method in resizeChildren) {
                    if (typeof(resizeChildren[method]) == "function") {
                        resizeChildren[method](l,t,w,h);
                    }
                }
                self.paintShadow(element,element.id);

            }

        }
        //initialization complete, add to scaling controller.
        cosmo.ui.resize.Viewports.addViewport(self)
            self.paintShadow = function () {}
        self.keepInFrame = function () {return null}
        return self
    }

cosmo.ui.resize.Handles = new function () {
    var self = this
    var handles = {}
    self.directions = {"top":3,"left":2,"bottom":5,"right":4}
    self.collapse_vectors = {"top":-1,"left":-1,"bottom":1,"right":1}

    /**
     *
     * @param {cosmo.ui.resize.handle} handle
     */
    this.addHandle = function (handle) {
        handles[handle.id] = handle
    }

    this.removeHandle = function (handle) {
        cosmo.ui.resize.viewports.removeEntry(handles,handle.id)

    }

}
/**
 * a handle is a dom element used to change the parameters of a viewport.
 * @param {domElement} el
 */
cosmo.ui.resize.Handle = function (el) {
    var aViewports = el.getAttribute("viewports");
    var self = this
    var handle = this
    var     Ev = cosmo.ui.resize.EventWrapper
    var Controller = cosmo.ui.resize.Handles
    self.canCollapse = false;
    if (el.getAttribute("collapse") != null) {
        self.canCollapse = true;
        arrCollapse = el.getAttribute("collapse").split(',');
        self.parentView = cosmo.ui.resize.Viewports.getViewport(arrCollapse[0]);
        self.vector = arrCollapse[1];
    }

    /**
     * Programmatically add a viewport
     * @param {String} viewportId
     * @param {String} direction top, left, bottom or right. If you
     * want a handle to scale a viewport in two directions, you need to make two entries.
     */
    self.addViewport = function (viewportId,direction) {
        self.viewports.push({"viewport":cosmo.ui.resize.Viewports.getViewport(viewportId),"direction":Controller.directions[direction]})
    }

    /**
     * Programmatically add a collapse
     * @param {String} viewportId
     * @param {String} direction a given handle can collapse a viewport in one direction only.
     */
    self.addCollapse = function (viewportId,direction) {
        self.canCollapse = true;
        self.parentView = cosmo.ui.resize.Viewports.getViewport(viewportId);
        self.vector = direction;
    }

    self.viewports = [];
    if(aViewports != null) {
        var affectedEls = aViewports.split(';');
        for (var i = 0; i + 1 < affectedEls.length; i++) {
            el_attr = affectedEls[i].split(':')
            self.viewports.push({"viewport":cosmo.ui.resize.Viewports.getViewport(el_attr[0]),"direction":Controller.directions[el_attr[1]]})
        }
    }
    self.start = null;
    self.current = null;
    self.old = null;
    self.isCollapse = false;
    self.handle = el;
    self.startDrag = function (event) {
            if(Ev.isLeftClick(event)) {
                var self = handle
                self.start = {"x":Ev.mouseX(event),"y":Ev.mouseY(event)};
                self.current = {"x":Ev.mouseX(event),"y":Ev.mouseY(event)};
                self.old = {"x":Ev.mouseX(event),"y":Ev.mouseY(event)};
                    self.isDragging = false;
                Ev.listen(document, "mousemove",self.update );
                    Ev.listen(document, "mouseup", self.endDrag);
                Ev.stopListen(self.handle, "mousedown", self.startDrag)
                Ev.cancelBubble(event);

            }
        };
    self.update = function (event) {
            var self = handle
            self.isDragging = true;
            self.isCollapse = false;
            self.current = {"x":Ev.mouseX(event),"y":Ev.mouseY(event)};
            var differenceX = self.current.x - self.old.x;
            var differenceY = self.current.y - self.old.y;
            self.old.x = self.current.x
            self.old.y = self.current.y
            for (var i = 0; i <self.viewports.length; i++) {
                var direction = self.viewports[i].direction
                var minSize = self.viewports[i].viewport.getMinSize()
                var maxSize = self.viewports[i].viewport.getMaxSize()
                if (direction % 2 == 0) {
                    minSize[direction] += differenceX
                    maxSize[direction] += differenceX
                }
                else {
                    minSize[direction]+= differenceY
                    maxSize[direction]+= differenceY
                }
            }
            cosmo.ui.resize.Viewports.resize()
            Ev.cancelBubble(event);
        };
        /* remove observers*/
    self.endDrag = function (event) {
            var self = handle
            Ev.stopListen(document, "mouseup", self.endDrag)
            Ev.stopListen(document, "mousemove", self.update)
            Ev.listen(self.handle, "mousedown", self.startDrag);
            if (!self.isDragging)
                handle.collapse();
            self.isDragging = false;
        };


    /*  this method collapses in a specific direction. this is similar to the
        resize handle how this works:
        c_direction determines which numerical section is modified, and how.*/
    self.collapse = function () {
        var self = handle
        if (!self.canCollapse)
            return;
        //var toggle = 1
        var hX = self.handle.offsetWidth //+self.handle.offsetLeft;
        var hY = self.handle.offsetHeight //+self.handle.offsetTop;
        var V = Controller.collapse_vectors[self.vector]
        var minSize
        var maxSize
        var deltaXmin
        var deltaXMax
        var deltaYmin
        var deltaYMax
        if (self.isCollapse == false) {
            self.minSize = self.parentView.getMinSize().concat([]);
            self.maxSize = self.parentView.getMaxSize().concat([]);
            minSize = self.minSize
            maxSize = self.maxSize
            deltaXmin = V *(minSize[4] - minSize[2] - hX);
            deltaXmax = V *(maxSize[4] - maxSize[2] - hX);
            deltaYmin = V *(minSize[5] - minSize[3] - hY);
            deltaYmax = V *(maxSize[5] - maxSize[3] - hY);
            self.isCollapse = true;
        }
        else {
            minSize = self.minSize
            maxSize = self.maxSize
            deltaXmin = -V *(minSize[4] - minSize[2] - hX);
            deltaXmax = -V *(maxSize[4] - maxSize[2] - hX);
            deltaYmin = -V *(minSize[5] - minSize[3] - hY);
            deltaYmax = -V *(maxSize[5] - maxSize[3] - hY);
            self.isCollapse = false;

        }
        for (var i = 0; i <self.viewports.length; i++) {
            minSize = self.viewports[i].viewport.getMinSize()
            maxSize = self.viewports[i].viewport.getMaxSize()
            var direction = self.viewports[i].direction
            if (direction % 2 == 0) {
                minSize[direction] += deltaXmin;
                maxSize[direction] += deltaXmax;
            }
            else {
                minSize[direction] += deltaYmin;
                maxSize[direction] += deltaYmax;
            }
            self.viewports[i].viewport.setMinSize(minSize)
            self.viewports[i].viewport.setMaxSize(maxSize)
        }

        cosmo.ui.resize.Viewports.resize();
    }

    Ev.listen(self.handle, "mousedown", self.startDrag);
}




    //self.listen(window,"unload",self.cleanupListeners)

