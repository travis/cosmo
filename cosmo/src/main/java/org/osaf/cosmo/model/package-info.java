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
    @NamedQuery(name="homeCollection.by.ownerId", query="from HomeCollectionItem where owner.id=:ownerid"),
    @NamedQuery(name="item.by.ownerId.parentId.name", query="select item from Item item join item.parents parent where item.owner.id=:ownerid and parent.id=:parentid and item.name=:name"),
    @NamedQuery(name="item.by.ownerId.nullParent.name", query="select item from Item item where item.owner.id=:ownerid and size(item.parents)=0 and item.name=:name"),
    @NamedQuery(name="item.by.ownerId.nullParent.name.minusItem", query="select item from Item item where item.id!=:itemid and item.owner.id=:ownerid and size(item.parents)=0 and item.name=:name"),
    @NamedQuery(name="item.by.ownerId.parentId.name.minusItem", query="select item from Item item join item.parents parent where item.id!=:itemid and item.owner.id=:ownerid and parent.id=:parentid and item.name=:name"),
    @NamedQuery(name="item.by.uid", query="from Item i where i.uid=:uid"),
    @NamedQuery(name="itemid.by.uid", query="select i.id from Item i where i.uid=:uid"),
    @NamedQuery(name="collectionItem.by.uid", query="from CollectionItem i where i.uid=:uid"),
    @NamedQuery(name="contentItem.by.uid", query="from ContentItem i where i.uid=:uid"),
    @NamedQuery(name="item.by.parent.name", query="select item from Item item join item.parents parent where parent=:parent and item.name=:name"),
    @NamedQuery(name="item.by.ownerName.name.nullParent", query="select i from Item i, User u where i.owner=u and u.username=:username and i.name=:name and size(i.parents)=0"),
    @NamedQuery(name="item.by.ownerId.and.nullParent", query="select i from Item i where i.owner.id=:ownerid and size(i.parents)=0"),
    @NamedQuery(name="contentItem.by.parent.timestamp", query="select item from ContentItem item left join fetch item.stamps left join fetch item.attributes left join fetch item.tombstones join item.parents parent where parent=:parent and item.modifiedDate>:timestamp"),
    @NamedQuery(name="contentItem.by.parent", query="select item from ContentItem item left join fetch item.stamps left join fetch item.attributes left join fetch item.tombstones join item.parents parent where parent=:parent"),
    
    // User Queries
    @NamedQuery(name="user.byUsername", query="from User where username=:username"),
    @NamedQuery(name="user.byUsername.ignorecase", query="from User where lower(username)=lower(:username)"),
    @NamedQuery(name="user.byEmail", query="from User where email=:email"),
    @NamedQuery(name="user.byEmail.ignorecase", query="from User where lower(email)=lower(:email)"),
    @NamedQuery(name="user.byUsernameOrEmail.ignorecase.ingoreId", query="from User where id!=:userid and (lower(username)=lower(:username) or lower(email)=lower(:email))"),
    @NamedQuery(name="user.byUid", query="from User where uid=:uid"),
    @NamedQuery(name="user.byActivationId", query="from User where activationid=:activationId"),

    
    // Password Recovery entity query
    @NamedQuery(name="passwordRecovery.byKey", query="from PasswordRecovery where key=:key"),
    @NamedQuery(name="passwordRecovery.delete.byUser", query="delete from PasswordRecovery where user=:user"),
    
    // Event Queries
    @NamedQuery(name="event.by.calendar.icaluid", query="select i from NoteItem i join i.parents parent where parent=:calendar and i.icalUid=:uid")
    
})
package org.osaf.cosmo.model;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

