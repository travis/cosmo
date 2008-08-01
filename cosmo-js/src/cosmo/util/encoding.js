/*
 * Copyright 2007-2008 Open Source Applications Foundation
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
 * summary:
 *      This module provides encoding utility functions.
 * description:
 *      This module provides encoding utility functions, in particular
 *      a unicode compatible base64 encoding implementation.
 */
dojo.provide("cosmo.util.encoding");

cosmo.util.encoding.base64Alphabet =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZ' +
    'abcdefghijklmnopqrstuvwxyz' +
    '0123456789+/=';

cosmo.util.encoding.toBase64 = function(string){
    string = cosmo.util.encoding._utf8_encode(string);
    var i = 0;
    var char1, char2, char3;
    var enc1, enc2, enc3, enc4;

    var out = '';

    var alpha = cosmo.util.encoding.base64Alphabet;

    do {
        char1 = string.charCodeAt(i++);
        char2 = string.charCodeAt(i++);
        char3 = string.charCodeAt(i++);

        enc1  = char1 >> 2;
        enc2 = ((char1 & 3) << 4) | char2 >> 4;
        enc3 = ((char2 & 15) << 2) | char3 >> 6;
        enc4 = char3 & 63;

        if (isNaN(char2)){
            enc3 = enc4 = 64;
        } else if (isNaN(char3)){
            enc4 = 64;
        }

        out = out +
              alpha.charAt(enc1) +
              alpha.charAt(enc2) +
              alpha.charAt(enc3) +
              alpha.charAt(enc4);
    } while (i < string.length);

    return out;

};

cosmo.util.encoding.fromBase64 = function(string){
    var i = 0;
    var char1, char2, char3;
    var enc1, enc2, enc3, enc4;
    string = string.replace(/[^A-Za-z0-9\+\/\=]/g, "");

    var out = "";

    var alpha = cosmo.util.encoding.base64Alphabet;

    do {
        enc1 = alpha.indexOf(string.charAt(i++));
        enc2 = alpha.indexOf(string.charAt(i++));
        enc3 = alpha.indexOf(string.charAt(i++));
        enc4 = alpha.indexOf(string.charAt(i++));

        char1 = (enc1 << 2) | (enc2 >> 4);
        char2 = ((enc2 & 15) << 4) | (enc3 >> 2);
        char3 = ((enc3 & 3) << 6) | enc4;

        out = out + String.fromCharCode(char1);

        if (enc3 != 64) {
            out = out + String.fromCharCode(char2);
        }
        if (enc4 != 64) {
            out = out + String.fromCharCode(char3);
        }

    } while (i < string.length);

    return cosmo.util.encoding._utf8_decode(out);
};

/* UTF-8 conversion functions per http://ecmanaut.blogspot.com/2006/07/encoding-decoding-utf8-in-javascript.html */
cosmo.util.encoding._utf8_encode = function(s) {
    return unescape(encodeURIComponent(s));
};

cosmo.util.encoding._utf8_decode = function(s) {
  return decodeURIComponent(escape(s));
};
