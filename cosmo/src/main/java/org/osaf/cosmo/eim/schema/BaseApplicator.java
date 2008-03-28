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
package org.osaf.cosmo.eim.schema;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.BlobField;
import org.osaf.cosmo.eim.BytesField;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.DateTimeField;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.BinaryAttribute;
import org.osaf.cosmo.model.CalendarAttribute;
import org.osaf.cosmo.model.DecimalAttribute;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.IntegerAttribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.TextAttribute;

/**
 * Base class for classes that apply EIM records to items and stamps.
 *
 * An applicator uses a fixed record type schema (event, task, note)
 * to validate EIM record data and map it to item or stamp properties
 * and attributes.
 */
public abstract class BaseApplicator implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(BaseApplicator.class);

    private String prefix;
    private String namespace;
    private Item item;

    /**
     * This class should not be instantiated directly.
     */
    protected BaseApplicator(String prefix,
                             String namespace,
                             Item item) {
        this.prefix = prefix;
        this.namespace = namespace;
        this.item = item;
    }

    /**
     * Copies the field into an item attribute in the record's
     * namespace.
     *
     * @throws EimSchemaException if the field is improperly
     * cannot be applied to the item 
     */
    protected void applyUnknownField(EimRecordField field)
        throws EimSchemaException {
        QName qn =
            item.getFactory().createQName(field.getRecord().getNamespace(), field.getName());
        if (log.isDebugEnabled())
            log.debug("applying unknown field " + qn);
        
        // get existing attribute
        Attribute attribute = item.getAttribute(qn);

        if (field instanceof BlobField) {
            InputStream value = ((BlobField)field).getBlob();
            if(attribute!=null)
                attribute.setValue(value);
            else
                item.addAttribute(item.getFactory().createBinaryAttribute(qn, value));
        } else if (field instanceof BytesField) {
            byte[] value = ((BytesField)field).getBytes();
            if(attribute!=null)
                attribute.setValue(value);
            else
                item.addAttribute(item.getFactory().createBinaryAttribute(qn, value));
        } else if (field instanceof ClobField) {
            Reader value = ((ClobField)field).getClob();
            if(attribute!=null)
                attribute.setValue(value);
            else
                item.addAttribute(item.getFactory().createTextAttribute(qn, value));
        } else if (field instanceof DateTimeField) {
            Calendar value = ((DateTimeField)field).getCalendar();
            if(attribute!=null)
                attribute.setValue(value);
            else
                item.addAttribute(item.getFactory().createCalendarAttribute(qn, value));
        } else if (field instanceof DecimalField) {
            BigDecimal value = ((DecimalField)field).getDecimal();
            if(attribute!=null)
                attribute.setValue(value);
            else
                item.addAttribute(item.getFactory().createDecimalAttribute(qn, value));
        } else if (field instanceof IntegerField) {
            Integer value = ((IntegerField)field).getInteger();
            if(attribute!=null)
                attribute.setValue(value);
            else
                item.addAttribute(item.getFactory().createIntegerAttribute(qn, new Long(value.longValue())));
        } else if (field instanceof TextField) {
            String value = ((TextField)field).getText();
            if(attribute!=null)
                attribute.setValue(value);
            else
                item.addAttribute(item.getFactory().createStringAttribute(qn, value));
        } else {
            throw new EimSchemaException("Field " + field.getName() + " is of unknown type " + field.getClass().getName());
        }
    }


    /** */
    public String getPrefix() {
        return prefix;
    }

    /** */
    public String getNamespace() {
        return namespace;
    }

    /** */
    public Item getItem() {
        return item;
    }
    
    /**
     * Determine if current item is a NoteItem that modifies another NoteItem
     * 
     * @return true if item is a NoteItem and modifies another NoteItem
     */
    protected boolean isModification() {
        if(getItem() instanceof NoteItem) {
            NoteItem note = (NoteItem) getItem();
            if(note.getModifies()!=null)
                return true;
        }
        
        return false;
    }
    
    /**
     * Verify that current item is a modification item.  If not, 
     * throw EimSchemaException.
     * @throws EimSchemaException
     */
    protected void checkIsModification() throws EimSchemaException {
        if (!isModification())
            throw new EimSchemaException(
                    "missing attributes not support on non-modification items");
    }
    
    /**
     * Handle a missing attribute for a modification by setting
     * the value to null.
     * 
     * @param attribute attribute to copy
     * @param modification object to copy attribute to
     */
    protected void handleMissingAttribute(String attribute,
            Object modification) {
        try {
            PropertyUtils.setProperty(modification, attribute, null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("error copying attribute " + attribute);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("error copying attribute " + attribute);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("error copying attribute " + attribute);
        }
    }
    
    /**
     * Handle missing attribute for a NoteItem.  This involves setting the 
     * attribute value to null if the NoteItem is a modification.
     * 
     * @param attribute atttribute to copy
     * @throws EimSchemaException
     */
    protected void handleMissingAttribute(String attribute)
            throws EimSchemaException {

        checkIsModification();

        NoteItem modification = (NoteItem) getItem();
        
        handleMissingAttribute(attribute, modification);
    }
}
