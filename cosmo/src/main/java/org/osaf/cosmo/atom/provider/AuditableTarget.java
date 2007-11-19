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
package org.osaf.cosmo.atom.provider;

import java.util.Date;

import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.util.EntityTag;

/**
 * A <code>Target</code> that represents an
 * <code>AuditableObject</code>. Such a target exposes an entity tag
 * and a last modified date.
 *
 * @see Target
 * @see MockAuditableObject
 */
public interface AuditableTarget extends Target {

    public EntityTag getEntityTag();

    public Date getLastModified();
}
