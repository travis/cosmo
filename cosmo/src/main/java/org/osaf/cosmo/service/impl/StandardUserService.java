/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.service.impl;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.id.StringIdentifierGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.TaskStamp;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.OverlordDeletionException;
import org.osaf.cosmo.service.ServiceEvent;
import org.osaf.cosmo.service.ServiceListener;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.DateUtil;
import org.osaf.cosmo.util.PagedList;
import org.osaf.cosmo.util.PageCriteria;

/**
 * Standard implementation of {@link UserService}.
 */
public class StandardUserService extends BaseService implements UserService {
    private static final Log log = LogFactory.getLog(StandardUserService.class);

    /**
     * The service uses MD5 if no digest algorithm is explicitly set.
     */
    public static final String DEFAULT_DIGEST_ALGORITHM = "MD5";
    
    private MessageDigest digest;
    private String digestAlgorithm;
    private StringIdentifierGenerator passwordGenerator;
    private ContentDao contentDao;
    private UserDao userDao;

    // UserService methods

    /**
     * Returns an unordered set of all user accounts in the repository.
     */
    public Set<User> getUsers() {
        if (log.isDebugEnabled())
            log.debug("getting all users");
        return userDao.getUsers();
    }

    /**
     * Returns the sorted list of user accounts corresponding to the
     * given <code>PageCriteria</code>.
     *
     * @param pageCriteria the pagination criteria
     */
    public PagedList<User, User.SortType> getUsers(PageCriteria<User.SortType> pageCriteria) {
        if (log.isDebugEnabled())
            log.debug("getting users for criteria " + pageCriteria);
        return userDao.getUsers(pageCriteria);
    }

    /**
     * Returns the user account identified by the given username.
     *
     * @param username the username of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUser(String username) {
        if (log.isDebugEnabled())
            log.debug("getting user " + username);
        return userDao.getUser(username);
    }

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email the email address of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUserByEmail(String email) {
        if (log.isDebugEnabled())
            log.debug("getting user with email address " + email);
        return userDao.getUserByEmail(email);
    }

    /**
     * Returns the user account associated with the given activation id.
     *
     * @param activationId the activation id associated with the account to return
     *
     * @return the User associated with activationId
     *
     * @throws DataRetrievalFailureException if there is no user associated with this
     * activation id.
     */
    public User getUserByActivationId(String activationId) {
        if (log.isDebugEnabled())
            log.debug("getting user associated with activation id " +
                    activationId);

        return userDao.getUserByActivationId(activationId);
    }

    /**
     * Creates a user account in the repository. Digests the raw
     * password and uses the result to replace the raw
     * password. Returns a new instance of <code>User</code>
     * after saving the original one.
     *
     * @param user the account to create
     *
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User createUser(User user) {
        return createUser(user, new ServiceListener[] {});
    }

    /**
     * Creates a user account in the repository as per
     * {@link #createUser(User)}. Sends the <code>CREATE_USER</code>
     * event to each provided listener, providing the newly created
     * user and home collection as state.
     *
     * @param user the account to create
     * @param listeners an array of listeners to notify
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User createUser(User user,
                           ServiceListener[] listeners) {
        if (log.isDebugEnabled())
            log.debug("creating user " + user.getUsername());

        user.validateRawPassword();

        user.setPassword(digestPassword(user.getPassword()));
        user.setDateCreated(new Date());
        user.setDateModified(user.getDateCreated());

        fireBeforeEvent(new ServiceEvent("CREATE_USER", user), listeners);

        userDao.createUser(user);
        User newUser = userDao.getUser(user.getUsername());

        HomeCollectionItem home = contentDao.createRootItem(newUser);

        fireAfterEvent(new ServiceEvent("CREATE_USER", user, home), listeners);

        createOutOfTheBoxStuff(home);

        return newUser;
    }

    /**
     * Updates a user account that exists in the repository. If the
     * password has been changed, digests the raw new password and
     * uses the result to replace the stored password. Returns a new
     * instance of <code>User</code>  after saving the original one.
     *
     * @param user the account to update
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User updateUser(User user) {
        boolean isUsernameChanged = user.isUsernameChanged();
        if (log.isDebugEnabled()) {
            log.debug("updating user " + user.getOldUsername());
            if (isUsernameChanged)
                log.debug("... changing username to " + user.getUsername());
        }

        if (user.getPassword().length() < 32) {
            user.validateRawPassword();
            user.setPassword(digestPassword(user.getPassword()));
        }
        user.setDateModified(new Date());

        userDao.updateUser(user);

        User newUser = userDao.getUser(user.getUsername());

        if (isUsernameChanged) {
            if (log.isDebugEnabled())
                log.debug("renaming root item for user " +
                          newUser.getUsername());
            CollectionItem rootCollection = contentDao.getRootItem(newUser);
            rootCollection.setName(newUser.getUsername());
            contentDao.updateCollection(rootCollection);
        }

        return newUser;
    }

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    public void removeUser(String username) {
        if (log.isDebugEnabled())
            log.debug("removing user " + username);
        User user = userDao.getUser(username);
        removeUserAndItems(user);
    }

    /**
     * Removes a user account from the repository.
     *
     * @param user the account to remove
     */
    public void removeUser(User user) {
        if (log.isDebugEnabled())
            log.debug("removing user " + user.getUsername());
        removeUserAndItems(user);
    }

    /**
     * Removes a set of user accounts from the repository. Will not
     * remove the overlord.
     * @param users
     */
    public void removeUsers(Set<User> users) throws OverlordDeletionException{
        for (User user : users){
            if (user.isOverlord())
                throw new OverlordDeletionException();
            removeUserAndItems(user);
        }
        // Only log if all removes were successful
        if (log.isDebugEnabled()) {
            for (User user : users){
                log.debug("removing user " + user.getUsername());
            }
        }


    }

    /**
     * Removes the user accounts identified by the given usernames from
     * the repository. Will not remove overlord.
     * @param usernames
     */
    public void removeUsersByName(Set<String> usernames) throws OverlordDeletionException{
        for (String username : usernames){
            if (username.equals(User.USERNAME_OVERLORD))
                    throw new OverlordDeletionException();
            User user = userDao.getUser(username);
            removeUserAndItems(user);
        }
        // Only log if all removes were successful
        if (log.isDebugEnabled()) {
            for (String username : usernames){
                log.debug("removing user " + username);
            }
        }
    }

    /**
     * Generates a random password in a format suitable for
     * presentation as an authentication credential.
     */
    public String generatePassword() {
        String password = passwordGenerator.nextStringIdentifier();
        return password.length() <= User.PASSWORD_LEN_MAX ?
            password :
            password.substring(0, User.PASSWORD_LEN_MAX - 1);
    }

    // Service methods

    /**
     * Initializes the service, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        if (contentDao == null) {
            throw new IllegalStateException("contentDao is required");
        }
        if (userDao == null) {
            throw new IllegalStateException("userDao is required");
        }
        if (passwordGenerator == null) {
            throw new IllegalStateException("passwordGenerator is required");
        }
        if (digestAlgorithm == null) {
            digestAlgorithm = DEFAULT_DIGEST_ALGORITHM;
        }

        try {
            digest = MessageDigest.getInstance(digestAlgorithm);
        } catch (Exception e) {
            throw new RuntimeException("cannot get digest for algorithm " +
                                               digestAlgorithm, e);
        }
    }

    /**
     * Readies the service for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods

    /**
     * Digests the given password using the set message digest and hex
     * encodes it.
     */
    protected String digestPassword(String password) {
        if (password == null) {
            return password;
        }
        return new String(Hex.encodeHex(digest.digest(password.getBytes())));
    }

    /**
     */
    public MessageDigest getDigest() {
        return this.digest;
    }

    /**
     */
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    /**
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    /**
     */
    public StringIdentifierGenerator getPasswordGenerator() {
        return this.passwordGenerator;
    }

    /**
     */
    public void setPasswordGenerator(StringIdentifierGenerator generator) {
        this.passwordGenerator = generator;
    }

    /**
     */
    public ContentDao getContentDao() {
        return this.contentDao;
    }

    /**
     */
    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    /**
     */
    public UserDao getUserDao() {
        return this.userDao;
    }

    /**
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public PasswordRecovery getPasswordRecovery(String key) {
         PasswordRecovery passwordRecovery = userDao.getPasswordRecovery(key);
         
         if (passwordRecovery != null){
             if (passwordRecovery.hasExpired()){
                 userDao.deletePasswordRecovery(passwordRecovery);
             } else {
                 return passwordRecovery;
             }
         }
         return null;
     }
     
     public PasswordRecovery createPasswordRecovery(
                 PasswordRecovery passwordRecovery){
         
         userDao.createPasswordRecovery(passwordRecovery);
         
         return userDao.getPasswordRecovery(passwordRecovery.getKey());
     }
     
     public void deletePasswordRecovery(PasswordRecovery passwordRecovery){
         userDao.deletePasswordRecovery(passwordRecovery);
     }    

    /**
     * Remove all Items associated to User.
     * This is done by removing the HomeCollectionItem, which is the root
     * collection of all the user's items.
     */
    private void removeUserAndItems(User user) {
        if(user==null)
            return;
        HomeCollectionItem home = contentDao.getRootItem(user);
        contentDao.removeCollection(home);
        userDao.removeUser(user);
    }

    private void createOutOfTheBoxStuff(HomeCollectionItem home) {
        CollectionItem initial =
            contentDao.createCollection(home,
                                        makeInitialCollection(home.getOwner()));

        contentDao.createContent(initial, makeWelcomeItem(home.getOwner()));
        contentDao.createContent(initial, makeTryOutItem(home.getOwner()));
        contentDao.createContent(initial, makeSubscribeItem(home.getOwner()));
        contentDao.createContent(initial, makeSignUpItem(home.getOwner()));
    }

    private CollectionItem makeInitialCollection(User owner) {
        CollectionItem collection = new CollectionItem();

        // XXX i18n

        String fullName = owner.getFirstName() + " " + owner.getLastName();
        collection.setName(fullName);
        collection.setDisplayName(collection.getName());
        collection.setOwner(owner);

        CalendarCollectionStamp ccs = new CalendarCollectionStamp(collection);
        collection.addStamp(ccs);

        return collection;
    }

    private NoteItem makeWelcomeItem(User owner) {
        NoteItem item = new NoteItem();

        // XXX i18n
        // XXX timezone

        item.setUid(contentDao.generateUid());
        item.setName("Welcome to Chandler Server");
        item.setDisplayName(item.getName());
        item.setOwner(owner);
        item.setClientCreationDate(Calendar.getInstance().getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(TriageStatus.createInitialized());
        item.setLastModifiedBy(item.getOwner().getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());
        item.setBody("<INSERT COPY FROM SHEILA>");

        Calendar start = Calendar.getInstance();
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        EventStamp es = new EventStamp();
        item.addStamp(es);
        es.createCalendar();
        es.setStartDate(Dates.getInstance(start.getTime(), Value.DATE_TIME));
        es.setDuration(new Dur(0, 1, 0, 0));

        TaskStamp ts = new TaskStamp();
        item.addStamp(ts);

        MessageStamp ms = new MessageStamp();
        item.addStamp(ms);
        ms.setFrom("OSAF");
        ms.setTo(item.getOwner().getUsername());
        ms.setOriginators("OSAF");
        ms.setDateSent(DateUtil.formatDate(MessageStamp.FORMAT_DATE_SENT,
                                           item.getClientCreationDate()));

        return item;
    }

    private NoteItem makeTryOutItem(User owner) {
        NoteItem item = new NoteItem();

        // XXX i18n
        // XXX timezone

        item.setUid(contentDao.generateUid());
        item.setName("Try out Chandler Desktop");
        item.setDisplayName(item.getName());
        item.setOwner(owner);
        item.setClientCreationDate(Calendar.getInstance().getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(TriageStatus.createInitialized());
        item.setLastModifiedBy(item.getOwner().getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());
        item.setBody("<INSERT PRODUCT DESCRIPTION>\n<INSERT DOWNLOAD LINK>");

        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, 1);
        start.add(Calendar.HOUR_OF_DAY, 10);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        EventStamp es = new EventStamp();
        item.addStamp(es);
        es.createCalendar();
        es.setStartDate(Dates.getInstance(start.getTime(), Value.DATE_TIME));
        es.setDuration(new Dur(0, 1, 0, 0));

        TaskStamp ts = new TaskStamp();
        item.addStamp(ts);

        return item;
    }

    private NoteItem makeSubscribeItem(User owner) {
        NoteItem item = new NoteItem();

        // XXX i18n

        TriageStatus triage = TriageStatus.createInitialized();
        triage.setCode(TriageStatus.CODE_LATER);

        item.setUid(contentDao.generateUid());
        item.setName("Subscribe to a Chandler Desktop Collection");
        item.setDisplayName(item.getName());
        item.setOwner(owner);
        item.setClientCreationDate(Calendar.getInstance().getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(triage);
        item.setLastModifiedBy(item.getOwner().getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());
        item.setBody("Use the Dashboard and Calendar to manage notes, tasks, and events with others.");

        return item;
    }

    private NoteItem makeSignUpItem(User owner) {
        NoteItem item = new NoteItem();

        // XXX i18n

        TriageStatus triage = TriageStatus.createInitialized();
        triage.setCode(TriageStatus.CODE_DONE);

        item.setUid(contentDao.generateUid());
        item.setName("Sign up for a Chandler Server account");
        item.setDisplayName(item.getName());
        item.setOwner(owner);
        item.setClientCreationDate(Calendar.getInstance().getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(triage);
        item.setLastModifiedBy(item.getOwner().getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());

        TaskStamp ts = new TaskStamp();
        item.addStamp(ts);

        return item;
    }
}
