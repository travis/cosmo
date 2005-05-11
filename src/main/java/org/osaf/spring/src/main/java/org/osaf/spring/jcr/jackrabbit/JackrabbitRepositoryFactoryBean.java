/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.spring.jcr.jackrabbit;

import javax.jcr.Repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A Spring {@link org.springframework.beans.factory.FactoryBean} that
 * provides a factory for JCR {@link javax.jcr.Repository} instances
 * provided by Jackrabbit.
 */
public class JackrabbitRepositoryFactoryBean
    implements FactoryBean, InitializingBean {
    private static final Log log =
        LogFactory.getLog(JackrabbitRepositoryFactoryBean.class);

    private String configFilePath;
    private String repositoryHomeDirPath;
    private Repository repository;
    private boolean singleton;

    /**
     */
    public JackrabbitRepositoryFactoryBean() {
        singleton = false;
    }

    /**
     * Return an instance of {@link javax.jcr.Repository}. Whether
     * the instance is a singleton or prototype is determined by the
     * singleton property.
     */
    public Object getObject()
        throws Exception {
        if (isSingleton() && repository != null) {
            return repository;
        }

        if (log.isDebugEnabled()) {
            log.debug("loading repository at " + repositoryHomeDirPath +
                      " with config " + configFilePath);
        }
        RepositoryConfig config =
            RepositoryConfig.create(configFilePath, repositoryHomeDirPath);
        repository = RepositoryImpl.create(config);
        log.info("loaded " +
                 repository.getDescriptor(Repository.REP_NAME_DESC) + " " +
                 repository.getDescriptor(Repository.REP_VERSION_DESC) +
                 " repository");

        return repository;
    }

    /**
     * Validates that the config file and repository home directory
     * paths have been set.
     */
    public void afterPropertiesSet()
        throws Exception {
        if (configFilePath == null) {
            throw new IllegalArgumentException("configFilePath must be set");
        }
        if (repositoryHomeDirPath == null) {
            throw new IllegalArgumentException("repositoryHomeDirPath must be set");
        }
    }

    /**
     * Return {@link javax.jcr.Repository}.
     */
    public Class getObjectType() {
        return Repository.class;
    }

    /**
     */
    public String getConfigFilePath() {
        return configFilePath;
    }

    /**
     */
    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    /**
     */
    public String getRepositoryHomeDirPath() {
        return repositoryHomeDirPath;
    }

    /**
     */
    public void setRepositoryHomeDirPath(String repositoryHomeDirPath) {
        this.repositoryHomeDirPath = repositoryHomeDirPath;
    }

    /**
     */
    public boolean isSingleton() {
        return singleton;
    }

    /**
     */
    public void setSingleton(boolean flag) {
        singleton = flag;
    }
}
