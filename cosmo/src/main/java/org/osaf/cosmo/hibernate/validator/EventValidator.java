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
package org.osaf.cosmo.hibernate.validator;

import java.io.IOException;
import java.io.Serializable;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.Validator;
import org.osaf.cosmo.calendar.util.CalendarUtils;

/**
 * Check if a Calendar object contains a valid VEvent
 * @author randy
 *
 */
public class EventValidator implements Validator<Event>, Serializable {

    private static final Log log = LogFactory.getLog(EventValidator.class);
    
    public boolean isValid(Object value) {
        
        Calendar calendar = null;
        try {
            calendar = (Calendar) value;
            
            // validate entire icalendar object
            calendar.validate(true);
            
            // additional check to prevent bad .ics
            CalendarUtils.parseCalendar(calendar.toString());
            
            // make sure we have a VEVENT
            ComponentList comps = calendar.getComponents();
            if(comps==null) {
                log.warn("error validating event: " + calendar.toString());
                return false;
            }
            
            comps = comps.getComponents(Component.VEVENT);
            if(comps==null || comps.size()==0) {
                log.warn("error validating event: " + calendar.toString());
                return false;
            }
            
            VEvent event = (VEvent) comps.get(0);
            if(event ==null) {
                log.warn("error validating event: " + calendar.toString());
                return false;
            }
                
            return true;
            
        } catch(ValidationException ve) {
            log.warn("event validation error", ve);
            if(calendar!=null) {
                log.warn("error validating event: " + calendar.toString() );
            }
            return false;
        } catch (RuntimeException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch(ParserException e) {
            log.warn("parse error", e);
            if(calendar!=null) {
                log.warn("error parsing event: " + calendar.toString() );
            }
            return false;
        }
    }

    public void initialize(Event parameters) {
    }
}

