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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TaskStamp;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.TriageStatusUtil;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.DateUtil;
import org.springframework.context.MessageSource;

/**
 * A helper class that creates out of the box collections and items
 * for a new user account.
 */
public class OutOfTheBoxHelper {
    private static final Log log = LogFactory.getLog(OutOfTheBoxHelper.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    private ContentDao contentDao;
    private MessageSource messageSource;
    private EntityFactory entityFactory;

    /**
     * <p>
     * Creates a collection named like the user's full name. Inside
     * the collection, places a variety of welcome items.
     * </p>
     */
    public CollectionItem createOotbCollection(OutOfTheBoxContext context) {
        CollectionItem initial =
            contentDao.createCollection(context.getHomeCollection(),
                                        makeCollection(context));

        NoteItem welcome = (NoteItem)
            contentDao.createContent(initial, makeWelcomeItem(context));

        NoteItem tryOut = (NoteItem)
            contentDao.createContent(initial, makeTryOutItem(context));
        // tryOut note should sort below welcome note
        BigDecimal tryOutRank = tryOut.getTriageStatus().getRank().
            subtract(BigDecimal.valueOf(1, 2));
        tryOut.getTriageStatus().setRank(tryOutRank);

        NoteItem signUp = (NoteItem)
            contentDao.createContent(initial, makeSignUpItem(context));

        return initial;
    }

    private CollectionItem makeCollection(OutOfTheBoxContext context) {
        CollectionItem collection = entityFactory.createCollection();
        Locale locale = context.getLocale();
        User user = context.getUser();

        String name = _("Ootb.Collection.Name", locale, user.getFirstName(), 
                        user.getLastName(), user.getUsername());
        String displayName = _("Ootb.Collection.DisplayName",
                               locale, user.getFirstName(), 
                               user.getLastName(), user.getUsername());

        collection.setName(name);
        collection.setDisplayName(displayName);
        collection.setOwner(user);

        CalendarCollectionStamp ccs = entityFactory.createCalendarCollectionStamp(collection);
        collection.addStamp(ccs);

        return collection;
    }

    private NoteItem makeWelcomeItem(OutOfTheBoxContext context) {
        NoteItem item = entityFactory.createNote();
        TimeZone tz= context.getTimeZone();
        Locale locale = context.getLocale();
        User user = context.getUser();

        String name = _("Ootb.Welcome.Title", locale);
        String body = _("Ootb.Welcome.Body", locale);
        String from = _("Ootb.Welcome.From", locale);
        String sentBy = _("Ootb.Welcome.SentBy", locale);
        String to = _("Ootb.Welcome.To", locale, user.getFirstName(),
                      user.getLastName(), user.getUsername());

        item.setUid(contentDao.generateUid());
        item.setDisplayName(name);
        item.setOwner(user);
        item.setClientCreationDate(Calendar.getInstance(tz, locale).getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        TriageStatus triage = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(triage);
        item.setTriageStatus(triage);
        item.setLastModifiedBy(user.getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());
        item.setBody(body);

        Calendar start = Calendar.getInstance(tz, locale);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        DateTime startDate = (DateTime)
            Dates.getInstance(start.getTime(), Value.DATE_TIME);
        startDate.setTimeZone(vtz(tz.getID()));

        EventStamp es = entityFactory.createEventStamp(item);
        item.addStamp(es);
        es.createCalendar();
        es.setStartDate(startDate);
        es.setDuration(new Dur(0, 1, 0, 0));

        TaskStamp ts = entityFactory.createTaskStamp();
        item.addStamp(ts);

        MessageStamp ms = entityFactory.createMessageStamp();
        item.addStamp(ms);
        ms.setFrom(from);
        ms.setTo(to);
        ms.setOriginators(sentBy);
        ms.setDateSent(DateUtil.formatDate(MessageStamp.FORMAT_DATE_SENT,
                                           item.getClientCreationDate()));

        return item;
    }

    private NoteItem makeTryOutItem(OutOfTheBoxContext context) {
        NoteItem item = entityFactory.createNote();
        TimeZone tz= context.getTimeZone();
        Locale locale = context.getLocale();
        User user = context.getUser();

        String name = _("Ootb.TryOut.Title", locale);
        String body = _("Ootb.TryOut.Body", locale);

        TriageStatus triage = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(triage);
        triage.setCode(TriageStatus.CODE_LATER);

        item.setUid(contentDao.generateUid());
        item.setDisplayName(name);
        item.setOwner(user);
        item.setClientCreationDate(Calendar.getInstance(tz, locale).getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(triage);
        item.setLastModifiedBy(user.getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());
        item.setBody(body);

        Calendar start = Calendar.getInstance(tz, locale);
        start.add(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 10);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        DateTime startDate = (DateTime)
            Dates.getInstance(start.getTime(), Value.DATE_TIME);
        startDate.setTimeZone(vtz(tz.getID()));

        EventStamp es = entityFactory.createEventStamp(item);
        item.addStamp(es);
        es.createCalendar();
        es.setStartDate(startDate);
        es.setDuration(new Dur(0, 1, 0, 0));

        TaskStamp ts = entityFactory.createTaskStamp();
        item.addStamp(ts);

        return item;
    }

    private NoteItem makeSignUpItem(OutOfTheBoxContext context) {
        NoteItem item = entityFactory.createNote();
        TimeZone tz= context.getTimeZone();
        Locale locale = context.getLocale();
        User user = context.getUser();

        String name = _("Ootb.SignUp.Title", locale);

        TriageStatus triage = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(triage);
        triage.setCode(TriageStatus.CODE_DONE);

        item.setUid(contentDao.generateUid());
        item.setDisplayName(name);
        item.setOwner(user);
        item.setClientCreationDate(Calendar.getInstance(tz, locale).getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(triage);
        item.setLastModifiedBy(user.getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());

        TaskStamp ts = entityFactory.createTaskStamp();
        item.addStamp(ts);

        return item;
    }

    public void init() {
        if (contentDao == null)
            throw new IllegalStateException("contentDao is required");
        if (messageSource == null)
            throw new IllegalStateException("messageSource is required");
        if (entityFactory == null)
            throw new IllegalStateException("entityFactory is required");
    }
    
    

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public ContentDao getContentDao() {
        return contentDao;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private String _(String key,
                     Locale locale,
                     Object... params) {
        return messageSource.getMessage(key, params, locale);
    }

    private String _(String key,
                     Locale locale) {
        return _(key, locale, new Object[] {});
    }

    private net.fortuna.ical4j.model.TimeZone vtz(String tzid) {
        return TIMEZONE_REGISTRY.getTimeZone(tzid);
    }
}
