/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.util;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test StringPropertyUtils
 *
 */
public class StringPropertyUtilsTest extends TestCase {
    
    public void testGetChildKeys() throws Exception {
        String[] testKeys = {"a.b.c", "a.b.d", "a.b.d.foo", "a.e.f.g.h.i"};
        
        String[] childKeys = StringPropertyUtils.getChildKeys("a", testKeys);
        Assert.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "b");
        verifyContains(childKeys, "e");
        
        childKeys = StringPropertyUtils.getChildKeys("a.", testKeys);
        Assert.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "b");
        verifyContains(childKeys, "e");
        
        childKeys = StringPropertyUtils.getChildKeys("a.b", testKeys);
        Assert.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "c");
        verifyContains(childKeys, "d");
        
        childKeys = StringPropertyUtils.getChildKeys("a.b.d", testKeys);
        Assert.assertEquals(1, childKeys.length);
        verifyContains(childKeys, "foo");

        childKeys = StringPropertyUtils.getChildKeys("a.b.d.foo", testKeys);
        Assert.assertEquals(0, childKeys.length);
        
        childKeys = StringPropertyUtils.getChildKeys("ldksf", testKeys);
        Assert.assertEquals(0, childKeys.length);
       
    }
    
    private void verifyContains(String[] strs, String str) {
        for(String s: strs)
            if(s.equals(str))
                return;
        
        Assert.fail("String " + str + " not found");
    }
    
}           
