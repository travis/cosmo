/**
 * 
 */
package org.osaf.cosmo.calendar.util;

import net.fortuna.ical4j.data.CalendarBuilder;

/**
 * Utility for managing a per-thread singleton for
 * CalendarBuilder.  Constructing a CalendarBuilder every
 * time one is needed can be expensive if a lot of 
 * time zones are involved in the calendar data.
 */
public class CalendarBuilderDispenser {
    
    private static class ThreadLocalCalendarBuilder extends ThreadLocal {
        public Object initialValue() {
            return new CalendarBuilder();
        }
        
        public CalendarBuilder getBuilder() {
            return (CalendarBuilder) super.get();
        }
    }

    private static ThreadLocalCalendarBuilder builder = new ThreadLocalCalendarBuilder();

    /**
     * Return the CalendarBuilder singelton for the current thread
     * @return
     */
    public static CalendarBuilder getCalendarBuilder() {
        return builder.getBuilder();
    }
}
