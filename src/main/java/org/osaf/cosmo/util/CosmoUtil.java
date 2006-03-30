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

package org.osaf.cosmo.util;

import java.io.IOException;
import java.util.Iterator;
import java.io.StringReader;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.component.VTimeZone;

public class CosmoUtil {
    public static VTimeZone validateVtimezone(String vt)
                          throws ValidationException {

        if (vt == null) return null;

            Calendar calendar;

            try {
                calendar = validateCalendar(vt);
            } catch (ValidationException e) {
                throw new ValidationException("The timezone element " + "icalendar text is not" +
                                              " valid: " + e.getMessage());
            }

            boolean found = false;
            VTimeZone tz = null;

            Iterator it = calendar.getComponents().iterator();

            while (it.hasNext()) {
                Object next = it.next();
                if (!(next instanceof VTimeZone)) 
                    throw new ValidationException("timezone can only contain a VTIMEZONE");
                if (found)
                    throw new ValidationException("timezone must contain a single " +
                                                  "VTIMEZONE more than one found");

                tz = (VTimeZone) next;
                found = true;
            }

            if (!found)
                throw new ValidationException("timezone must contain a single " +
                                              "VTIMEZONE none found");

            return tz;
    }

    public static Calendar validateCalendar(String icalendar)
                        throws ValidationException {
        try {
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(new StringReader(icalendar));
            calendar.validate(true);

            return calendar;

        } catch (IOException e) {
            throw new ValidationException(e.getMessage());
        } catch (ParserException e) {
            throw new ValidationException(e.getMessage());
        }
    }


    public static String resourceNodeToString(Node parentNode, String parentPath,
                                              String childPath) {

        if (! parentPath.endsWith("/")) parentPath += "/";

        String relPath = (childPath.split(parentPath))[1];

        try {
            Node n = parentNode.getNode(relPath);
            return nodeToString(n);

        } catch(Exception e) {
            return e.getMessage();
        }
    }

    public static String nodeToString(Node n) {
        StringBuffer buffer = new StringBuffer();

        try {
            buffer.append("Node: ").append(n.getName())
                  .append("  Type: ").append( n.getPrimaryNodeType().getName());

            NodeType[] nt = n.getMixinNodeTypes();

            if (nt.length > 0) {
                buffer.append("\nMixin Types: ");

                for (int j = 0; j < nt.length; j++)
                    buffer.append(" | ").append(nt[j].getName());
            }

            buffer.append("\n--------------------\n");

            PropertyIterator i = n.getProperties();

            while(i.hasNext()) {
                Property p = i.nextProperty();
                buffer.append("\t").append(p.getName()).append(" = \"")
                      .append(p.getString()).append("\"\n");
            }
        } catch (Exception e) {
            buffer.append(e.getMessage());
        }

        return buffer.toString();
    }


    public static String propertiesToString(DavPropertySet propertySet) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Properties:\n------------------\n");

        try {
            Iterator it = propertySet.iterator();

            while(it.hasNext()) {
                DavProperty d = (DavProperty) it.next();
                buffer.append("\t").append(d.getName()).append(" - \"")
                      .append(d.getValue().toString()).append("\"\n");
            }
        } catch (Exception e) {
            buffer.append(e.getMessage());
        }

        return buffer.toString();
    }
}

