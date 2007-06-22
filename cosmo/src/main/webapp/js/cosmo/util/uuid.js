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

dojo.provide("cosmo.util.uuid");

dojo.require("dojo.uuid.RandomGenerator");

dojo.declare("cosmo.util.uuid.RandomGenerator", dojo.uuid.RandomGenerator,
    {
        generate: function (type){
            /*
              UUID                   = time-low "-" time-mid "-"
                                       time-high-and-version "-"
                                       clock-seq-and-reserved
                                       clock-seq-low "-" node
              time-low               = 4hexOctet
              time-mid               = 2hexOctet
              time-high-and-version  = 2hexOctet
              clock-seq-and-reserved = hexOctet
              clock-seq-low          = hexOctet
              node                   = 6hexOctet
              hexOctet               = hexDigit hexDigit
              hexDigit =
                    "0" / "1" / "2" / "3" / "4" / "5" / "6" / "7" / "8" / "9" /
                    "a" / "b" / "c" / "d" / "e" / "f" /
                    "A" / "B" / "C" / "D" / "E" / "F"
              
               4.4.  Algorithms for Creating a UUID from Truly Random or
                     Pseudo-Random Numbers

               The version 4 UUID is meant for generating UUIDs from truly-random or
               pseudo-random numbers.
            
               The algorithm is as follows:
            
               o  Set the two most significant bits (bits 6 and 7) of the
                  clock_seq_hi_and_reserved to zero and one, respectively.
            
               o  Set the four most significant bits (bits 12 through 15) of the
                  time_hi_and_version field to the 4-bit version number from
                  Section 4.1.3.
            
               o  Set all the other bits to randomly (or pseudo-randomly) chosen
                  values.
             */
            
            var timeLow = Math.floor(Math.random() * 0x100000000);
            var timeMid = Math.floor(Math.random() * 0x10000);
            var timeHighAndVersion = Math.floor(Math.random() * 0x10000);
            var clockSeq = Math.floor(Math.random() * 0x10000);
            var node = Math.floor(Math.random() * 0x1000000000000);
            
            timeHighAndVersion &= 0x0FFF; // 0000 1111 1111 1111
            timeHighAndVersion |= 0x4000; // 0100 0000 1111 1111
            
            clockSeq &= 0x3FFF; // 0011 1111 1111 1111
            clockSeq |= 0x8000; // 1000 0000 0000 0000
            
            return [
            this.fixLength(timeLow.toString(16), 8),
            this.fixLength(timeMid.toString(16), 4),
            this.fixLength(timeHighAndVersion.toString(16), 4),
            this.fixLength(clockSeq.toString(16), 4), 
            this.fixLength(node.toString(16), 12)
            ].join("-");
            
               
        },
        
        fixLength: function(string, len){
            return "000000000000".substring(12 - (len - string.length)) + string;
        }
    }
    );
    
