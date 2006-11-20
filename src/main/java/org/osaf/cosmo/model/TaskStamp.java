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
package org.osaf.cosmo.model;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


/**
 * Represents a Task Stamp.
 */
@Entity
@Table(name="task_stamp")
@PrimaryKeyJoinColumn(name="stampid")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TaskStamp extends Stamp implements
        java.io.Serializable {


    /**
     * 
     */
    private static final long serialVersionUID = -6197756070431706553L;

    /** default constructor */
    public TaskStamp() {
        setType("task");
    }

    public Stamp copy() {
        TaskStamp stamp = new TaskStamp();
        return stamp;
    }
}
