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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.misc.BASE64Encoder;

/**
 * Utility methods for WSSE token processing
 */
public class WsseUtils {
    
    /**
     * Calculate WSSE passwordDigest using the following algorithm:
     *  PasswordDigest = Base64 \ (SHA1 (Nonce + CreationTimestamp + Password))
     * @param password password or password equivalent
     * @param nonce cryptographically random string
     * @param created creation timestamp of the current time, in W3DTF format.
     * @return password digest computed from inputs
     */
    public static String calculatePasswordDigest(String password, String nonce, String created) {
        StringBuffer toDigest = new StringBuffer();
        toDigest.append(nonce);
        toDigest.append(created);
        toDigest.append(password);
        
        MessageDigest md = null;
        byte[] sha1Hash = null;
        
        try {
            md = MessageDigest.getInstance("sha1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Platform does not support sha1?", e);
        }
        
        try {
            sha1Hash = md.digest(toDigest.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Platform does not support UTF-8?", e);
        }
        
        BASE64Encoder b64Encoder = new BASE64Encoder();
        return b64Encoder.encode(sha1Hash);
    }
    
    /**
     * Parse WSSE Username token in the following format:
     * UsernameToken Username="bob", PasswordDigest="quR/EWLAV4xLf9Zqyw4pDmfV9OY=", 
     * Nonce="d36e316282959a9ed4c89851497a717f", Created="2003-12-15T14:43:07Z"
     * @param wsseToken string representation of token
     * @return parsed username token, null if token could not be parsed
     */
    public static UsernameToken parseWsseToken(String wsseToken) {
        
        String patternStr = "UsernameToken Username=\\\"(.*)\\\", PasswordDigest=\\\"(.*)\\\", Nonce=\\\"(.*)\\\", Created=\\\"(.*)\\\"";
        
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(wsseToken);
        boolean matchFound = matcher.find();
        
        if (matchFound && matcher.groupCount()==4) {
            String username = matcher.group(1);
            String passwordDigest = matcher.group(2);
            String nonce = matcher.group(3);
            String created = matcher.group(4);
            return new UsernameToken(username, nonce, passwordDigest, created);
        }

        return null;
    }
}
