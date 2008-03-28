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

package org.osaf.cosmo.hibernate.jmx;

import org.hibernate.jmx.StatisticsServiceMBean;

/**
 * Extends the out-of-the box MBean for more hibernate related tasks
 * @author bobbyrullo
 */
public interface CosmoHibernateServiceMBean extends StatisticsServiceMBean {
    
    /**
     * Evicts of all of the instances with the given entity name from the 
     * second level cache. 
     * @see org.hibernate.SessionFactory#evictEntity(String entityName)
     * @param entityName
     */
    public void evictEntity(String entityName);
}
