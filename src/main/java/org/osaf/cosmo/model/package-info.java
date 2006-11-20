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
        typeClass = org.osaf.cosmo.hibernate.BinaryBlobType.class
    ),
    
    @TypeDef(
            name="calendar_clob",
            typeClass = org.osaf.cosmo.hibernate.CalendarClobType.class
    )
})

/*
 * Named Queries
 */
@NamedQueries({
    // Item Queries
    @NamedQuery(name="item.by.parentId", query="from Item where parent.id=:parentid"),
    @NamedQuery(name="homeCollection.by.ownerId", query="from HomeCollectionItem where owner.id=:ownerid"),
    @NamedQuery(name="item.by.ownerId.parentId.name", query="from Item where owner.id=:ownerid and parent.id=:parentid and name=:name"),
    @NamedQuery(name="item.by.ownerId.nullParent.name", query="from Item where owner.id=:ownerid and parent.id is null and name=:name"),
    @NamedQuery(name="item.by.ownerId.nullParent.name.minusItem", query="from Item where id!=:itemid and owner.id=:ownerid and parent.id is null and name=:name"),
    @NamedQuery(name="item.by.ownerId.parentId.name.minusItem", query="from Item where id!=:itemid and owner.id=:ownerid and parent.id=:parentid and name=:name"),
    @NamedQuery(name="item.by.uid", query="from Item i where i.uid=:uid"),
    @NamedQuery(name="collectionItem.by.uid", query="from CollectionItem i where i.uid=:uid"),
    @NamedQuery(name="contentItem.by.uid", query="from ContentItem i where i.uid=:uid"),
    @NamedQuery(name="calendarCollectionItem.by.uid", query="from CalendarCollectionItem i where i.uid=:uid"),
    @NamedQuery(name="calendarEventItem.by.uid", query="from CalendarEventItem i where i.uid=:uid"),
    @NamedQuery(name="item.by.parent.name", query="from Item where parent=:parent and name=:name"),
    @NamedQuery(name="item.by.ownerName.name.nullParent", query="select i from Item i, User u where i.owner=u and u.username=:username and i.name=:name and i.parent is null"),
    @NamedQuery(name="item.by.ownerId.and.nullParent", query="select i from Item i where i.owner.id=:ownerid and i.parent is null"),
    
    // User Queries
    @NamedQuery(name="user.byUsername", query="from User where username=:username"),
    @NamedQuery(name="user.byEmail", query="from User where email=:email"),
    @NamedQuery(name="user.byUid", query="from User where uid=:uid"),
       
    // Event Queries
    @NamedQuery(name="event.by.calendar.icaluid", query="select i from CalendarEventItem i, CalendarPropertyIndex pi where pi.item.id=i.id and i.parent=:calendar and pi.name='icalendar:vcalendar-vevent_uid' and pi.value=:uid")
               
})
package org.osaf.cosmo.model;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

