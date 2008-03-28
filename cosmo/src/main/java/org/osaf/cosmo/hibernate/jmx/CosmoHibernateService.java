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

import org.hibernate.SessionFactory;
import org.hibernate.jmx.StatisticsService;

/**
 * Implementation of {@link CosmoHibernateServiceMBean}
 * @author bobbyrullo
 */
public class CosmoHibernateService extends StatisticsService implements CosmoHibernateServiceMBean {
    private SessionFactory sessionFactory;
    
    /**
     * This needs to be done because the {@link StatisticsService} does not expose 
     * getters to the session factory.
     */
    public void setSessionFactory(SessionFactory sf){
        this.sessionFactory = sf;
        super.setSessionFactory(sf);
    }

    public void evictEntity(String entityName) {
        try {
            this.sessionFactory.evictEntity(entityName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
