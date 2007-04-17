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
package org.osaf.cosmo.service.account;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.StringIdentifierGenerator;
import org.osaf.cosmo.model.PasswordRecovery;

public abstract class PasswordRecoverer {
    private IdentifierGenerator idGenerator;

    /**
     * 
     * @param passwordRecovery
     */
    public abstract void sendRecovery(PasswordRecovery passwordRecovery,
                             PasswordRecoveryMessageContext context);
    
    /**
     * 
     * @return
     */
    public String createRecoveryKey() {
        return idGenerator.nextIdentifier().toString();
    }

    public IdentifierGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdentifierGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    
}
