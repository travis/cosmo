/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockAuditableObject;
import org.osaf.cosmo.model.mock.MockUser;
import org.osaf.cosmo.util.ArrayPagedList;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;

/**
 * Mock implementation of {@link UserDao} useful for testing.
 */
public class MockUserDao implements UserDao {
    private static final Log log = LogFactory.getLog(MockUserDao.class);

    static int idseq = 0;

    private HashMap usernameIdx;
    private HashMap emailIdx;
    private HashMap uidIdx;
    private HashMap activationIdIdx;
    private HashMap<String, PasswordRecovery> passwordRecoveryIdx;

    private VersionFourGenerator idGenerator = new VersionFourGenerator();

    /**
     */
    public MockUserDao() {
        usernameIdx = new HashMap();
        emailIdx = new HashMap();
        uidIdx = new HashMap();
        activationIdIdx = new HashMap();
        passwordRecoveryIdx = new HashMap<String, PasswordRecovery>();

        // add overlord user
        MockUser overlord = new MockUser();
        overlord.setUsername(User.USERNAME_OVERLORD);
        overlord.setFirstName("Cosmo");
        overlord.setLastName("Administrator");
        overlord.setPassword("32a8bd4d676f4fef0920c7da8db2bad7");
        overlord.setEmail("root@localhost");
        overlord.setAdmin(true);
        overlord.setCreationDate(new Date());
        overlord.setModifiedDate(new Date());
        createUser(overlord);
    }

    // UserDao methods

    /**
     */
    public Set getUsers() {
        Set tmp = new HashSet();
        for (Iterator i=usernameIdx.values().iterator(); i.hasNext();) {
            tmp.add(i.next());
        }
        return tmp;
    }

    /**
     */
    public PagedList getUsers(PageCriteria pageCriteria) {
        List list = new ArrayList();
        for (Iterator i=usernameIdx.values().iterator(); i.hasNext();) {
            list.add(i.next());
        }
        PagedList tmp = new ArrayPagedList(pageCriteria, list);
        return tmp;
    }

    /**
     */
    public User getUser(String username) {
        if (username == null)
            return null;
        return (User) usernameIdx.get(username);
    }

    /**
     */
    public User getUserByUid(String uid) {
        if (uid == null) 
            return null;
        return (User) uidIdx.get(uid);
    }

    /**
     */
    public User getUserByActivationId(String id) {
        if (id == null)
            return null;
        return (User) activationIdIdx.get(id);
    }

    /**
     */
    public User getUserByEmail(String email) {
        if (email == null)
            return null;
        return (User) emailIdx.get(email);
    }

    /**
     */
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }

        user.setUid(idGenerator.nextIdentifier().toString());
        
        // Set create/modified date, etag for User and associated subscriptions
        // and perferences.
        ((MockAuditableObject) user).setModifiedDate(new Date());
        ((MockAuditableObject) user).setCreationDate(new Date());
        ((MockAuditableObject) user).setEntityTag(((MockAuditableObject) user)
                .calculateEntityTag());
        
        for(CollectionSubscription cs: user.getCollectionSubscriptions()) {
            ((MockAuditableObject) cs).setEntityTag(((MockAuditableObject) cs)
                    .calculateEntityTag());
            ((MockAuditableObject) cs).setModifiedDate(new Date());
            ((MockAuditableObject) cs).setCreationDate(new Date());
        }
        
        for(Preference p: user.getPreferences()) {
            ((MockAuditableObject) p).setEntityTag(((MockAuditableObject) p)
                    .calculateEntityTag());
            ((MockAuditableObject) p).setModifiedDate(new Date());
            ((MockAuditableObject) p).setCreationDate(new Date());
        }
            
        user.validate();
        if (usernameIdx.containsKey(user.getUsername())) {
            throw new DuplicateUsernameException("username in use");
        }
        if (emailIdx.containsKey(user.getEmail())) {
            throw new DuplicateEmailException("email in use");
        }
        
        usernameIdx.put(user.getUsername(), user);
        emailIdx.put(user.getEmail(), user);
        uidIdx.put(user.getUid(), user);
        activationIdIdx.put(user.getActivationId(), user);
        return user;
    }

    /**
     */
    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }
        
        // Update modified date, etag for User and associated subscriptions
        // and preferences.
        ((MockAuditableObject) user).setModifiedDate(new Date());
        ((MockAuditableObject) user).setEntityTag(((MockAuditableObject) user)
                .calculateEntityTag());
        
        for(CollectionSubscription cs: user.getCollectionSubscriptions()) {
            ((MockAuditableObject) cs).setEntityTag(((MockAuditableObject) cs)
                    .calculateEntityTag());
            ((MockAuditableObject) cs).setModifiedDate(new Date());
            if(cs.getCreationDate()==null)
                ((MockAuditableObject) cs).setCreationDate(new Date());
        }
        
        for(Preference p: user.getPreferences()) {
            ((MockAuditableObject) p).setEntityTag(((MockAuditableObject) p)
                    .calculateEntityTag());
            ((MockAuditableObject) p).setModifiedDate(new Date());
            if(p.getCreationDate()==null)
                ((MockAuditableObject) p).setCreationDate(new Date());
        }
        
        user.validate();
        String key = user.isUsernameChanged() ?
            user.getOldUsername() :
            user.getUsername();
        if (! usernameIdx.containsKey(key)) {
            throw new IllegalArgumentException("user not found");
        }
        if (user.isUsernameChanged() &&
            usernameIdx.containsKey(user.getUsername())) {
            throw new DuplicateUsernameException("username in use");
        }
        if (user.isEmailChanged() && emailIdx.containsKey(user.getEmail())) {
            throw new DuplicateEmailException("email in use");
        }
        usernameIdx.put(user.getUsername(), user);
        if (user.isUsernameChanged()) {
            usernameIdx.remove(user.getOldUsername());
        }
        emailIdx.put(user.getEmail(), user);
        if (user.isEmailChanged()) {
            emailIdx.remove(user.getOldEmail());
        }
        return user;
    }

    /**
     */
    public void removeUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null username");
        }
        if (usernameIdx.containsKey(username)) {
            User user = (User) usernameIdx.get(username);
            usernameIdx.remove(username);
            emailIdx.remove(user.getEmail());
        }
    }

    /**
     */
    public void removeUser(User user) {
        if (user == null)
            return;
        usernameIdx.remove(user.getUsername());
        emailIdx.remove(user.getEmail());
    }

    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
    }

    public void createPasswordRecovery(PasswordRecovery passwordRecovery) {
        passwordRecoveryIdx.put(passwordRecovery.getKey(), passwordRecovery);
        
    }

    public void deletePasswordRecovery(PasswordRecovery passwordRecovery) {
        passwordRecoveryIdx.remove(passwordRecovery.getKey());
        
    }

    public PasswordRecovery getPasswordRecovery(String key) {
        return passwordRecoveryIdx.get(key);
    }

}
