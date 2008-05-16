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

dojo.provide("cosmotest.util.test_i18n");

dojo.require("cosmo.util.i18n");

cosmotest.util.test_i18n = {
    test_getText: function (){
        cosmo.util.i18n._localtext = {
            "A": "nothing",
            "B": "zero {0}",
            "C": "zero {0} one {1}",
            "D": "one {1}",
            "E": "one {1} zero {0}",
            "F": "one {1} zero {0} one {1}"
        }
        
        jum.assertEquals("A", "nothing", cosmo.util.i18n.getText("A"));
        jum.assertEquals("B", "zero 0", cosmo.util.i18n.getText("B", 0));
        jum.assertEquals("C", "zero 0 one 1", cosmo.util.i18n.getText("C", 0, 1));
        jum.assertEquals("D", "one 1", cosmo.util.i18n.getText("D", 0, 1));
        jum.assertEquals("E", "one 1 zero 0", cosmo.util.i18n.getText("E", 0, 1));
        jum.assertEquals("F", "one 1 zero 0 one 1", cosmo.util.i18n.getText("F", 0, 1));
    }
}
  