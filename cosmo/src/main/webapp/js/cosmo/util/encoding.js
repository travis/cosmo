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


dojo.provide("cosmo.util.encoding");

cosmo.util.encoding.base64Alphabet =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZ' +
    'abcdefghijklmnopqrstuvwxyz' +
    '0123456789+/='

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

}

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
}
/* UTF-8 functions grabbed from http://www.webtoolkit.info/javascript-base64.html */
cosmo.util.encoding._utf8_decode = function (utftext) {  
    var string = "";  
    var i = 0;  
    var c = c1 = c2 = 0;  
         while ( i < utftext.length ) {  
             c = utftext.charCodeAt(i);  
             if (c < 128) {  
            string += String.fromCharCode(c);  
            i++;  
        }  
        else if((c > 191) && (c < 224)) {  
            c2 = utftext.charCodeAt(i+1);  
            string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));  
            i += 2;  
        }  
        else {  
            c2 = utftext.charCodeAt(i+1);  
            c3 = utftext.charCodeAt(i+2);  
            string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));  
            i += 3;  
        }  
         }  
         return string;  
}  

cosmo.util.encoding._utf8_encode = function (string) {  
    string = string.replace(/\r\n/g,"\n");  
    var utftext = "";  
         for (var n = 0; n < string.length; n++) {  
             var c = string.charCodeAt(n);  
             if (c < 128) {  
            utftext += String.fromCharCode(c);  
        }  
        else if((c > 127) && (c < 2048)) {  
            utftext += String.fromCharCode((c >> 6) | 192);  
            utftext += String.fromCharCode((c & 63) | 128);  
        }  
        else {  
            utftext += String.fromCharCode((c >> 12) | 224);  
            utftext += String.fromCharCode(((c >> 6) & 63) | 128);  
            utftext += String.fromCharCode((c & 63) | 128);  
        }  
         }  
         return utftext;  
} 
