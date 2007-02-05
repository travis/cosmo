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


/*
 * Type Definitions for custom hibernate data types.
 */
@TypeDefs({
    @TypeDef(
        name="bytearray_blob",
        typeClass = org.springframework.orm.hibernate3.support.BlobByteArrayType.class
    ),

    @TypeDef(
            name="calendar_clob",
            typeClass = org.osaf.cosmo.hibernate.CalendarClobType.class
    ),
    
    @TypeDef(
            name="composite_calendar",
            typeClass = org.osaf.cosmo.hibernate.CalendarType.class
    ),
    
    @TypeDef(
            name="long_timestamp",
            typeClass = org.osaf.cosmo.hibernate.LongTimestampType.class
    ),
    
    @TypeDef(
            name="boolean_integer",
            typeClass = org.osaf.cosmo.hibernate.BooleanIntegerType.class
    )
})

/*
 * Named Queries
 */
@NamedQueries({
    // Item Queries
    @NamedQuery(name="item.by.parentId", query="from Item where parent.id=:parentid and isActive=true"),
    @NamedQuery(name="homeCollection.by.ownerId", query="from HomeCollectionItem where owner.id=:ownerid and isActive=true"),
    @NamedQuery(name="item.by.ownerId.parentId.name", query="from Item where owner.id=:ownerid and parent.id=:parentid and name=:name and isActive=true"),
    @NamedQuery(name="item.by.ownerId.nullParent.name", query="from Item where owner.id=:ownerid and parent.id is null and name=:name and isActive=true"),
    @NamedQuery(name="item.by.ownerId.nullParent.name.minusItem", query="from Item where id!=:itemid and owner.id=:ownerid and parent.id is null and name=:name and isActive=true"),
    @NamedQuery(name="item.by.ownerId.parentId.name.minusItem", query="from Item where id!=:itemid and owner.id=:ownerid and parent.id=:parentid and name=:name and isActive=true"),
    @NamedQuery(name="item.by.uid", query="from Item i where i.uid=:uid and isActive=true"),
    @NamedQuery(name="item.any.by.uid", query="from Item i where i.uid=:uid"),
    @NamedQuery(name="collectionItem.by.uid", query="from CollectionItem i where i.uid=:uid and isActive=true"),
    @NamedQuery(name="contentItem.by.uid", query="from ContentItem i where i.uid=:uid and i.isActive=true"),
    @NamedQuery(name="item.by.parent.name", query="from Item where parent=:parent and name=:name and isActive=true"),
    @NamedQuery(name="item.by.ownerName.name.nullParent", query="select i from Item i, User u where i.owner=u and u.username=:username and i.name=:name and i.parent is null and i.isActive=true"),
    @NamedQuery(name="item.by.ownerId.and.nullParent", query="select i from Item i where i.owner.id=:ownerid and i.parent is null and i.isActive=true"),

    // User Queries
    @NamedQuery(name="user.byUsername", query="from User where username=:username"),
    @NamedQuery(name="user.byUsername.ignorecase", query="from User where lower(username)=lower(:username)"),
    @NamedQuery(name="user.byEmail", query="from User where email=:email"),
    @NamedQuery(name="user.byEmail.ignorecase", query="from User where lower(email)=lower(:email)"),
    @NamedQuery(name="user.byUid", query="from User where uid=:uid"),
    @NamedQuery(name="user.byActivationId", query="from User where activationid=:activationId"),

    // Event Queries
    @NamedQuery(name="event.by.calendar.icaluid", query="select i from ContentItem i, CalendarPropertyIndex pi where pi.item.id=i.id and i.parent=:calendar and pi.name='icalendar:vcalendar-vevent_uid' and pi.value=:uid and i.isActive=true"),
    
    // Delete Queries
    @NamedQuery(name="delete.calendarPropertyIndex", query="delete from CalendarPropertyIndex where eventStamp=:eventStamp"),
    @NamedQuery(name="delete.calendarTimeRangeIndex", query="delete from CalendarTimeRangeIndex where eventStamp=:eventStamp")
    

})
package org.osaf.cosmo.model;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

