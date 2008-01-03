/*
 * Copyright 2007 Open Source Applications Foundation
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


dojo.provide("cosmo.util.color");
dojo.require("dojo.math");

//from dojo0.4
// Based on C Code in "Computer Graphics -- Principles and Practice,"
// Foley et al, 1996, p. 593.
//
// H = 0 to 255 (corresponding to 0..360 degrees around hexcone) 0 for S = 0
// S = 0 (shade of gray) to 255 (pure color)
// V = 0 (black) to 255 (white)

cosmo.util.color.hsv2rgb = function(/* int || Array */h, /* int */s, /* int */v, /* Object? */options){
    //	summary
    //	converts an HSV value set to RGB, ranges depending on optional options object.
    //	patch for options by Matthew Eernisse 	
    if (dojo.isArray(h)) {
	if(s){
	    options = s;
	}
	v = h[2] || 0;
	s = h[1] || 0;
	h = h[0] || 0;
    }

    var opt = {
	inputRange:  (options && options.inputRange)  ? options.inputRange : [255, 255, 255],
	outputRange: (options && options.outputRange) ? options.outputRange : 255
    };

    switch(opt.inputRange[0]) { 
	// 0.0-1.0 
    case 1: h = h * 360; break; 
	// 0-100 
    case 100: h = (h / 100) * 360; break; 
	// 0-360 
    case 360: h = h; break; 
	// 0-255 
    default: h = (h / 255) * 360; 
    } 
    if (h == 360){ h = 0;}

    //	no need to alter if inputRange[1] = 1
    switch(opt.inputRange[1]){
    case 100: s /= 100; break;
    case 255: s /= 255;
    }

    //	no need to alter if inputRange[1] = 1
    switch(opt.inputRange[2]){
    case 100: v /= 100; break;
    case 255: v /= 255;
    }

    var r = null;
    var g = null;
    var b = null;

    if (s == 0){
	// color is on black-and-white center line
	// achromatic: shades of gray
	r = v;
	g = v;
	b = v;
    }else{
	// chromatic color
	var hTemp = h / 60;		// h is now IN [0,6]
	var i = Math.floor(hTemp);	// largest integer <= h
	var f = hTemp - i;		// fractional part of h

	var p = v * (1 - s);
	var q = v * (1 - (s * f));
	var t = v * (1 - (s * (1 - f)));

	switch(i){
	case 0: r = v; g = t; b = p; break;
	case 1: r = q; g = v; b = p; break;
	case 2: r = p; g = v; b = t; break;
	case 3: r = p; g = q; b = v; break;
	case 4: r = t; g = p; b = v; break;
	case 5: r = v; g = p; b = q; break;
	}
    } 
    switch(opt.outputRange){
        var round = cosmo.util.color._round;
    case 1:
	r = round(r, 2);
	g = round(g, 2);
	b = round(b, 2);
	break;
    case 100:
	r = Math.round(r * 100);
	g = Math.round(g * 100);
	b = Math.round(b * 100);
	break;
    default:
	r = Math.round(r * 255);
	g = Math.round(g * 255);
	b = Math.round(b * 255);
    }

    return [r, g, b];
}

cosmo.util.color._round = function (number, places) { 
    if (!places) {
        var shift = 1;
    } else { 
        var shift = Math.pow(10, places);
    }
    return Math.round(number * shift) / shift; 
}