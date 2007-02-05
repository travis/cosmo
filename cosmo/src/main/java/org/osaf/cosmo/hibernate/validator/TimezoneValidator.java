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

import java.io.Serializable;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.hibernate.validator.Validator;

/**
 * Check if a Calendar object contains a valid VTIMEZONE
 * @author randy
 *
 */
public class TimezoneValidator implements Validator<Timezone>, Serializable {

    public boolean isValid(Object value) {
        
        if(value==null)
            return true;
        
        try {
            Calendar calendar = (Calendar) value;
            
            // validate entire icalendar object
            calendar.validate(true);
            
            // make sure we have a VTIMEZONE
            VTimeZone timezone = (VTimeZone) calendar.getComponents()
                    .getComponents(Component.VTIMEZONE).get(0);
            return (timezone != null);
        } catch(ValidationException ve) {
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void initialize(Timezone parameters) {
    }
}

