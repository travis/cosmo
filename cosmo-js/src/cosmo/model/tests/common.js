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

dojo.provide("cosmotest.model.test_common")
dojo.require("cosmo.model.common");

cosmotest.model.test_common = {
    test_duration: function (){
        var duration = new cosmo.model.Duration({day:1, month:2});
        jum.assertEquals(1, duration.getDay());
        jum.assertEquals(2, duration.getMonth());
        jum.assertEquals("P2M1D", duration.toIso8601());

        duration = new cosmo.model.Duration("PT4H5M6S");
        jum.assertEquals(4, duration.getHour());
        jum.assertEquals(5, duration.getMinute());
        jum.assertEquals(6, duration.getSecond());
        jum.assertEquals("PT4H5M6S", duration.toIso8601());
        
        var date1 = new cosmo.datetime.Date(2000,0,1);
        var date2 = new cosmo.datetime.Date(2000,0,2);
        duration = new cosmo.model.Duration(date1, date2);
        jum.assertEquals(1, duration.getDay());
        jum.assertEquals("P1D", duration.toIso8601());

        date1 = new cosmo.datetime.Date(2000,0,1,12);
        date2 = new cosmo.datetime.Date(2000,0,2,14);
        duration = new cosmo.model.Duration(date1, date2);
        jum.assertEquals(1, duration.getDay());
        jum.assertEquals(2, duration.getHour());
        jum.assertEquals("P1DT2H", duration.toIso8601());

        date1 = new cosmo.datetime.Date(2000,0,1,12);
        date2 = new cosmo.datetime.Date(2000,0,2,11);
        duration = new cosmo.model.Duration(date1, date2);
        jum.assertEquals(0, duration.getDay());
        jum.assertEquals(23, duration.getHour());
        jum.assertEquals("PT23H", duration.toIso8601());
    }
}


