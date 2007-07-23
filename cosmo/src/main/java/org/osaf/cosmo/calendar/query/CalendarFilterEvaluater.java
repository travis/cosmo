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
package org.osaf.cosmo.calendar.query;

import java.util.ArrayList;
import java.util.Iterator;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.DateProperty;

import org.osaf.cosmo.calendar.InstanceList;


/**
 * Contains methods for determining if a Calendar matches
 * a CalendarFilter.
 */
public class CalendarFilterEvaluater {
    
    private static final String COMP_VCALENDAR = "VCALENDAR";
    
    public CalendarFilterEvaluater() {}
    
    /**
     * Evaulate CalendarFilter against a Calendar.
     * @param calendar calendar to evaluate against
     * @param filter filter to apply
     * @return true if the filter
     * @throws UnsupportedQueryException if filter represents a query
     *                              that the server does not support
     */
    public boolean evaluate(Calendar calendar, CalendarFilter filter) {
        ComponentFilter rootFilter = filter.getFilter();
        
        // root filter must be "VCALENDAR"
        if(!COMP_VCALENDAR.equalsIgnoreCase(rootFilter.getName()))
            return false;
        
        // evaluate all component filters
        for(Iterator it = rootFilter.getComponentFilters().iterator(); it.hasNext();) {
            ComponentFilter compFilter = (ComponentFilter) it.next();
            
            // If any component filter fails to match, then the calendar filter
            // does not match
            if(!evaluateComps(calendar.getComponents(), compFilter))
                return false;
        }
        
        return true;
    }
    
    private boolean evaluate(ComponentList comps, ComponentFilter filter) {
        for(Iterator<Component> it=comps.iterator();it.hasNext();) {
            if(evaluateComps(getSubComponents(it.next()),filter)==false)
                return false;
        }
        return true;
    }
    
    private boolean evaluate(ComponentList comps, PropertyFilter filter) {
        for(Iterator<Component> it=comps.iterator();it.hasNext();) {
            if(evaluate(it.next(),filter)==false)
                return false;
        }
        return true;
    }
    
    private boolean evaluateComps(ComponentList components, ComponentFilter filter) {
        
        /*The CALDAV:comp-filter XML element is empty and the
        calendar component type specified by the "name"
        attribute exists in the current scope;*/
        if(filter.getComponentFilters().size()==0 && filter.getPropFilters().size()==0 && filter.getTimeRangeFilter()==null && filter.getIsNotDefinedFilter()==null) {
            ComponentList comps = components.getComponents(filter.getName().toUpperCase());
            return comps.size()>0;
        }
        
        /* The CALDAV:comp-filter XML element contains a CALDAV:is-not-
        defined XML element and the calendar object or calendar
        component type specified by the "name" attribute does not exist
        in the current scope;*/
        if(filter.getIsNotDefinedFilter()!=null) {
            ComponentList comps = components.getComponents(filter.getName().toUpperCase());
            return comps.size()==0;
        }
        
        // Match the component
        ComponentList comps = components.getComponents(filter.getName().toUpperCase());
        if(comps.size()==0)
            return false;
        
        /*The CALDAV:comp-filter XML element contains a CALDAV:time-range
        XML element and at least one recurrence instance in the
        targeted calendar component is scheduled to overlap the
        specified time range, and all specified CALDAV:prop-filter and
        CALDAV:comp-filter child XML elements also match the targeted
        calendar component;*/
        
        // Evaulate time-range filter
        if(filter.getTimeRangeFilter()!=null) {
            if(evaluate(comps, filter.getTimeRangeFilter())==false)
                return false;
        }
        
        for(Iterator<ComponentFilter> it = filter.getComponentFilters().iterator(); it.hasNext();) {
            if(evaluate(comps, it.next())==false)
                return false;
        }
        
        for(Iterator<PropertyFilter> it = filter.getPropFilters().iterator(); it.hasNext();) {
            if(evaluate(comps, it.next())==false)
                return false;
        }
        
        return true;
    }
    
    private boolean evaluate(Component component, PropertyFilter filter) {
        
        /*The CALDAV:prop-filter XML element is empty and a property of
        the type specified by the "name" attribute exists in the
        enclosing calendar component;*/
        if(filter.getParamFilters().size()==0 && filter.getTimeRangeFilter()==null && filter.getIsNotDefinedFilter()==null && filter.getTextMatchFilter()==null) {
            PropertyList props = component.getProperties(filter.getName());
            return props.size()>0;
        }
        
        /*The CALDAV:prop-filter XML element contains a CALDAV:is-not-
        defined XML element and no property of the type specified by
        the "name" attribute exists in the enclosing calendar
        component;*/
        if(filter.getIsNotDefinedFilter()!=null) {
            PropertyList props = component.getProperties(filter.getName());
            return props.size()==0;
        }
        
        // Match the property
        PropertyList props = component.getProperties(filter.getName());
        if(props.size()==0)
            return false;
        
        /*The CALDAV:prop-filter XML element contains a CALDAV:time-range
        XML element and the property value overlaps the specified time
        range, and all specified CALDAV:param-filter child XML elements
        also match the targeted property;*/
        
        // Evaulate time-range filter
        if(filter.getTimeRangeFilter()!=null) {
            if(evaluate(props, filter.getTimeRangeFilter())==false)
                return false;
        }
        
        if(filter.getTextMatchFilter()!=null) {
            props = evaluate(props, filter.getTextMatchFilter());
            if(props.size()==0)
                return false;
        }
        
        for(Iterator<ParamFilter> it = filter.getParamFilters().iterator(); it.hasNext();) {
            if(evaluate(props, it.next())==false)
                    return false;
        }
        
        return true;
    }
    
    private boolean evaluate(PropertyList props, ParamFilter filter) {
        for(Iterator<Property> it=props.iterator();it.hasNext();) {
            if(evaulate(it.next(),filter)==false)
                return false;
        }
        return true;
    }
    
    private boolean evaulate(Property property, ParamFilter filter) {
        
        /*The CALDAV:param-filter XML element is empty and a parameter of
        the type specified by the "name" attribute exists on the
        calendar property being examined;*/
        if(filter.getIsNotDefinedFilter()==null && filter.getTextMatchFilter()==null) {
            ParameterList params = property.getParameters(filter.getName());
            return params.size()>0;
        }
        
       /* The CALDAV:param-filter XML element contains a CALDAV:is-not-
        defined XML element and no parameter of the type specified by
        the "name" attribute exists on the calendar property being
        examined;*/
        if(filter.getIsNotDefinedFilter()!=null) {
            ParameterList params = property.getParameters(filter.getName());
            return params.size()==0;
        }
        
        // Match the parameter
        ParameterList params = property.getParameters(filter.getName());
        if(params.size()==0)
            return false;
        
        // Match the TextMatchFilter
        if(evaluate(params, filter.getTextMatchFilter())==false)
            return false;
        
        return true;
    }
    
    private PropertyList evaluate(PropertyList props, TextMatchFilter filter) {
        PropertyList results = new PropertyList();
        for(Iterator<Property> it = props.iterator(); it.hasNext();) {
            Property prop = it.next();
            if(evaluate(prop,filter)==true)
                results.add(prop);
        }
        return results;
    }
    
    private boolean evaluate(ParameterList params, TextMatchFilter filter) {
       
        for(Iterator<Parameter> it = params.iterator(); it.hasNext();) {
            Parameter param = it.next();
            if(evaluate(param,filter)==false)
                return false;
        }
        return true;
    }
    
    private boolean evaluate(Property property, TextMatchFilter filter) {
        boolean matched = false;
        if(filter.isCaseless())
            matched = property.getValue().toLowerCase().contains(filter.getValue().toLowerCase());
        else
            matched = property.getValue().contains(filter.getValue());
        
        if(filter.isNegateCondition())
            return !matched;
        else
            return matched;
    }
    
    private boolean evaluate(Parameter param, TextMatchFilter filter) {
        boolean matched = false;
        if(filter.isCaseless())
            matched = param.getValue().toLowerCase().contains(filter.getValue().toLowerCase());
        else
            matched = param.getValue().contains(filter.getValue());
        
        if(filter.isNegateCondition())
            return !matched;
        else
            return matched;
    }
    
    private boolean evaluate(ComponentList comps, TimeRangeFilter filter) {
        
        Component comp = (Component) comps.get(0);
       
        InstanceList instances = new InstanceList();
        if(filter.getTimezone()!=null)
            instances.setTimezone(new TimeZone(filter.getTimezone()));
        ArrayList<Component> mods = new ArrayList<Component>();
        
        for(Iterator<Component> it=comps.iterator();it.hasNext();) {
            comp = it.next();
            // Add master first
            if(comp.getProperty(Property.RECURRENCE_ID)==null)
                instances.addComponent(comp, filter.getPeriod().getStart(), filter.getPeriod().getEnd());
        }
        
        // Add overides after master has been added
        for(Component mod : mods)
            instances.addOverride(mod, filter.getPeriod().getStart(), filter.getPeriod().getEnd());
        
        if(instances.size()>0)
            return true;
        
        return false;
    }
    
    private boolean evaluate(PropertyList props, TimeRangeFilter filter) {
        for(Iterator<Property> it = props.iterator(); it.hasNext();) {
            if(evaluate(it.next(),filter)==false)
                return false;
        }
        return true;
    }
    
    private boolean evaluate(Property property, TimeRangeFilter filter) {
        if(!(property instanceof DateProperty) )
            return false;
        
        DateProperty dateProp = (DateProperty) property;
        Date date = dateProp.getDate();
        
        return (  (date.before(filter.getPeriod().getEnd()) &&
              date.after(filter.getPeriod().getStart())) ||
              date.equals(filter.getPeriod().getStart()) );
    }
    
    private ComponentList getSubComponents(Component component) {
        if(component instanceof VEvent)
            return ((VEvent) component).getAlarms();
        else if(component instanceof VTimeZone)
            return ((VTimeZone) component).getObservances();
        else if(component instanceof VToDo)
            return ((VToDo) component).getAlarms();
        
        return new ComponentList();
    }
}
