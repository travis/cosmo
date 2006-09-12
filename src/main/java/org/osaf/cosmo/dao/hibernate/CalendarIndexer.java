/*
 * Copyright (c) 2006 SimDesk Technologies, Inc.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * SimDesk Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with SimDesk Technologies.
 *
 * SIMDESK TECHNOLOGIES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
 * THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  SIMDESK TECHNOLOGIES
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.osaf.cosmo.dao.hibernate;

import net.fortuna.ical4j.model.Calendar;

import org.hibernate.Session;
import org.osaf.cosmo.model.CalendarItem;
import org.osaf.cosmo.model.Item;

/**
 * Interface for a Calendar Item indexer. A CalendarIndexer is responsible for
 * indexing the content of a calendar item. Since an item only stores basic
 * attributes, advanced indexing techniques will require data to be stored
 * elsewhere.
 * 
 * An indexer indexes the calendar content so that the DbItem can be found
 * quickly. The major usecase for an indexer will be for timerange indexes.
 * 
 */
public interface CalendarIndexer {

    public abstract void indexCalendarEvent(Session session, CalendarItem item,
            Calendar calendar);

}
