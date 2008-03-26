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
package org.osaf.cosmo.wsse;

import junit.framework.Assert;
import junit.framework.TestCase;

public class WsseUtilsTest extends TestCase {
    
    public void testParseWsseToken() throws Exception {
       String testStr = "UsernameToken Username=\"user\", PasswordDigest=\"pass\", Nonce=\"nonce\", Created=\"created\"";
       UsernameToken token = WsseUtils.parseWsseToken(testStr);
       Assert.assertNotNull(token);
       Assert.assertEquals("user", token.getUsername());
       Assert.assertEquals("pass", token.getPasswordDigest());
       Assert.assertEquals("nonce", token.getNonce());
       Assert.assertEquals("created", token.getCreated());
       
       testStr = "UsernameToken Username=\"user\", PasswordDigest=\"pass\", Nonce=\"nonce\"";
       token = WsseUtils.parseWsseToken(testStr);
       Assert.assertNull(token);
    }
    
}           
