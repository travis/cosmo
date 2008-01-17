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
package org.osaf.cosmo.mc.acegisecurity;

import org.acegisecurity.AccessDeniedException;

/**
 * <p>
 * An exception indicating that a principal was denied a privilege for a
 * morse code resource.
 * </p>
 */
public class MorseCodeAccessDeniedException extends AccessDeniedException {
    private String href;
   
    public MorseCodeAccessDeniedException(String href) {
        super(null);
        this.href = href;
    }

    public String getHref() {
        return href;
    }
}
