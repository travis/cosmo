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
package org.osaf.cosmo.cmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.server.StatusSnapshot;

/**
 * A resource view of a {@link StatusSnapshot}. This is a read-only
 * resource.
 */
public class StatusSnapshotResource
    implements CmpResource, OutputsPlainText {
    private static final Log log =
        LogFactory.getLog(StatusSnapshotResource.class);

    private StatusSnapshot snapshot;

    /**
     * Constructs a resource that represents the given
     * {@link StatusSnapshot}.
     */
    public StatusSnapshotResource(StatusSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    // CmpResource methods

    /**
     * Returns the <code>StatusSnapshot</code> that backs this
     * resource.
     */
    public Object getEntity() {
        return snapshot;
    }


    // OutputsPlainText methods

    /**
     * Returns a plain text representation of the snapshot.
     *
     * This method simply delegates to {@link StatusSnapshot#toString()}.
     */
    public String toText() {
        return snapshot.toString();
    }
}
