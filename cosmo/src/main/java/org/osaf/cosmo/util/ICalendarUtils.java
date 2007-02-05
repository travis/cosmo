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

package org.osaf.cosmo.util;

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterFactoryImpl;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.TzId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ICalendarUtils {
    private static final Log log = LogFactory.getLog(ICalendarUtils.class);

    private static java.util.TimeZone J_TZ_GMT = TimeZone.getTimeZone("GMT");

    /**
     * Creates an iCal4J DateTime. The values for year, month, day, hour,
     * minutes, seconds and milliseconds should be set way that you specify them
     * in a java.util.Calendar - which means zero indexed months for example
     * (eg. January is '0').
     *
     * Note that the TimeZone is not a java.util.TimeZone but an iCal4JTimeZone
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minutes
     * @param seconds
     * @param milliseconds
     * @param tz
     * @param utc
     */
    public static DateTime createDateTime(int year, int month, int day,
            int hour, int minutes, int seconds, int milliseconds, TimeZone tz,
            boolean utc) {
        DateTime dateTime = new DateTime();
        setFields(dateTime, year, month, day, hour, minutes, seconds,
                milliseconds, tz, utc);
        return dateTime;
    }

    public static DateTime createDateTime(int year, int month, int day,
            int hour, int minutes, TimeZone tz, boolean utc) {
        DateTime dateTime = new DateTime();
        setFields(dateTime, year, month, day, hour, minutes, 0, 0, tz, utc);
        return dateTime;

    }

    public static Date createDate(int year, int month, int day) {
        Date date = new Date();
        setFields(date, year, month, day, 0, 0, 0, 0, null, false);
        return date;

    }

    public static Date createDateTime(int year, int month, int day,
            TimeZone tz, boolean utc) {
        DateTime date = new DateTime();
        setFields(date, year, month, day, 0, 0, 0, 0, tz, utc);
        return date;

    }

    public static VEvent getFirstEvent(net.fortuna.ical4j.model.Calendar calendar){
        return (VEvent) calendar.getComponents().getComponent(Component.VEVENT);
    }

    public static String getSummaryValue(VEvent event){
        return getPropertyValue(event, Property.SUMMARY);
    }

    public static String getUIDValue(Component component){
        return getPropertyValue(component, Property.UID);
    }

    public static String getPropertyValue(Component component, String propertyName){
        Property property = component.getProperties().getProperty(propertyName);
        return property == null ? null : property.getValue();
    }

    private static void setFields(Date date, int year, int month, int day,
            int hour, int minutes, int seconds, int milliseconds, TimeZone tz,
            boolean utc){

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.setTimeZone(tz == null ? J_TZ_GMT : tz);
        if (date instanceof DateTime){
            if (utc) {
               ((DateTime)date).setUtc(utc);
            } else if (tz != null){
                ((DateTime)date).setTimeZone(tz);
            }
            calendar.set(Calendar.HOUR, hour);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, seconds);
            calendar.set(Calendar.MILLISECOND, milliseconds);
        }
        date.setTime(calendar.getTimeInMillis());
    }

    public static boolean hasProperty(Component c, String propName){
        PropertyList l = c.getProperties().getProperties(propName);
        return l != null && l.size() > 0;
    }


    /**
     * Returns the first master in the calendar - that is, one without a RECURRENCE-ID
     * @param calendar
     */
    public static VEvent getMasterEvent(net.fortuna.ical4j.model.Calendar calendar){
        ComponentList events = calendar.getComponents().getComponents(Component.VEVENT);
        for (Iterator i = events.iterator();i.hasNext(); ){
            VEvent vEvent = (VEvent) i.next();
            if (!hasProperty(vEvent, Property.RECURRENCE_ID)){
                return vEvent;
            }
        }
        return null;
    }

    /**
     * Returns the "master" VEvent - one that does not have a RECURRENCE-ID
     *
     * @param uid
     */
    public static VEvent getMasterEvent(net.fortuna.ical4j.model.Calendar calendar, String uid){
        ComponentList clist = calendar.getComponents().getComponents(Component.VEVENT);
        for (Object o : clist){
            VEvent curEvent = (VEvent) o;
            String curUid = getUIDValue(curEvent);
            if (uid.equals(curUid) && !hasProperty(curEvent, Property.RECURRENCE_ID) ){
                return curEvent;
            }
        }
        return null;
    }

    public static void addOrReplaceProperty(Component component, Property property){
        removeProperty(component, property.getName());

        component.getProperties().add(property);
    }

    public static void removeProperty(Component component, String propertyName) {
        Property oldProp = component.getProperties().getProperty(propertyName);
        if (oldProp != null){
            component.getProperties().remove(oldProp);
        }
    }

    /**
     * Sets a DateProperty with the given Date. Also sets the VALUE param to DATE to
     * indicate that this is a DATE not a DATETIME
     * @param dateProperty
     * @param date
     */
    public static void setDate(DateProperty dateProperty,
            net.fortuna.ical4j.model.Date date) {
        Parameter valueParam = dateProperty.getParameters().getParameter(
                Parameter.VALUE);
        ParameterFactoryImpl paramFactory = ParameterFactoryImpl.getInstance();

        try {
            valueParam = paramFactory.createParameter(Parameter.VALUE,
                    Value.DATE.getValue());
        } catch (URISyntaxException urise) {
            throw new RuntimeException(urise);
        }

        dateProperty.getParameters().add(valueParam);
        dateProperty.setDate(date);
    }

    /**
     * Sets a DateProperty with the given DateTime.
     * @param dateProperty
     * @param dateTime
     */
    public static void setDateTime(DateProperty dateProperty, DateTime dateTime) {
        Parameter valueParam = dateProperty.getParameters().getParameter(
                Parameter.VALUE);
        if (valueParam != null) {
            dateProperty.getParameters().remove(valueParam);
        }

        dateProperty.setDate(dateTime);
    }

    public static String getTZId(DateProperty dateProperty){
        Parameter tzid = dateProperty.getParameters().getParameter(Parameter.TZID);
        return tzid == null ? null : tzid.getValue();
    }

    public static String getTZId(DateListProperty dateListProperty) {
        Parameter tzid = dateListProperty.getParameters().getParameter(
                Parameter.TZID);
        return tzid == null ? null : tzid.getValue();
    }

    public static String getTZId(VTimeZone vtz){
       TzId tzid =  (TzId) vtz.getProperties().getProperty(Property.TZID);
       return tzid.getValue();
    }

   public static VTimeZone getVTimeZone(String tzid, net.fortuna.ical4j.model.Calendar calendar){
        ComponentList list = calendar.getComponents().getComponents(Component.VTIMEZONE);
        for (Object component : list){
            VTimeZone vtimezone = (VTimeZone) component;
            String curTzid = getPropertyValue(vtimezone, Property.TZID);
            if (tzid.equals(curTzid)){
                return vtimezone;
            }
        }
        return null;
    }

   public static long getDuration(VEvent vevent) {
        Date startDate = vevent.getStartDate().getDate();
        Duration d = (Duration) vevent.getProperties().getProperty(
                Property.DURATION);
        if (d != null) {
            Dur dur = d.getDuration();
            long endTime = dur.getTime(startDate).getTime();
            return endTime - startDate.getTime();
        }
        DtEnd dtEnd = (DtEnd) vevent.getProperties()
                .getProperty(Property.DTEND);
        if (dtEnd != null) {
            Date endDate = dtEnd.getDate();
            return endDate.getTime() - startDate.getTime();
        } else
            return 0;
    }
   
   /**
    * Returns the VTimeZone with the given tzId from the given
    * calendar, or null if none exists.
    * 
    * @param calendar
    * @param tzId
    * @return
    */
   public static VTimeZone getVTimeZone(net.fortuna.ical4j.model.Calendar calendar, String tzId){
      ComponentList cl = calendar.getComponents().getComponents(Component.VTIMEZONE);

      if (cl != null) {
            for (Object o : cl) {
                VTimeZone tz = (VTimeZone) o;
                if (tz.getProperties().getProperty(Property.TZID).getValue().equals(tzId)){
                    return tz;
                }
            }
        }
      
      return null;
   }


   /**
    * This is here because the clone that comes with ical4j is broken
    * if you setTime() on the new object, the old object will be
    * changed!
    *
    * @param date
    */
   public static Date clone(Date date){
       if (date instanceof DateTime){
           DateTime dateTime = (DateTime) date;
           DateTime newDateTime = new DateTime(dateTime.getTime());
           newDateTime.setTimeZone(dateTime.getTimeZone());
           return newDateTime;
       }
       Date newDate = new Date(date.getTime());
       return newDate;
   }

   public static Comparator<VEvent> VEVENT_START_DATE_COMPARATOR = new Comparator<VEvent>(){

    public int compare(VEvent v1, VEvent v2) {
        Date d1 = v1.getStartDate().getDate();
        Date d2 = v2.getStartDate().getDate();
        long t1 = d1.getTime();
        long t2 = d2.getTime();

        if (t1 == t2){
            return 0;
        }

        if (t1 < t2){
            return -1;
        }

        return 1;
    }

   };
}
