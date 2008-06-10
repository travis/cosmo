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
package org.osaf.cosmo.atom.generator;

import java.io.StringWriter;
import java.util.HashSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.eimml.EimmlConstants;
import org.osaf.cosmo.eim.eimml.EimmlStreamException;
import org.osaf.cosmo.eim.eimml.EimmlStreamWriter;
import org.osaf.cosmo.eim.json.JsonConstants;
import org.osaf.cosmo.eim.json.JsonStreamException;
import org.osaf.cosmo.eim.json.JsonStreamWriter;
import org.osaf.cosmo.eim.schema.ItemTranslator;
import org.osaf.cosmo.model.NoteItem;

/**
 * A factory that creates content beans for various data formats.
 *
 * @see ContentBean
 */
public class ContentFactory
    implements AtomConstants, EimmlConstants, JsonConstants {
    private static final Log log = LogFactory.getLog(ContentFactory.class);
    private static final HashSet FORMATS = new HashSet();
    static {
        FORMATS.add(FORMAT_EIM_JSON);
        FORMATS.add(FORMAT_EIMML);
        FORMATS.add(FORMAT_HTML);
        FORMATS.add(FORMAT_TEXT);
    }

    /**
     * Creates an instance of <code>Content</code> in the named
     * format.
     * <p>
     * The following data formats are supported:
     * <dl>
     * <dt>{@link AtomConstants#FORMAT_EIM_JSON}</dt>
     * <dd>{@link JsonConstants#MEDIA_TYPE_EIM_JSON}</dd>
     * <dt>{@link AtomConstants#FORMAT_EIMML}</dt>
     * <dd>{@link EimmlConstants#MEDIA_TYPE_EIMML}</dd>
     * </dl>
     * <p>
     * If no format is specified, the EIM-JSON format is used.
     *
     * @param format the format name
     * @return the content, or null if the named format is not
     * supported
     */
    public ContentBean createContent(String format,
                                     NoteItem item)
        throws UnsupportedFormatException {
        if (format == null)
            throw new IllegalArgumentException("null format");
        if (format.equals(FORMAT_EIM_JSON))
            return createEimJsonContent(item);
        if (format.equals(FORMAT_EIMML))
            return createEimmlContent(item);
        if (format.equals(FORMAT_HTML))
            return createHtmlContent(item);
        if (format.equals(FORMAT_TEXT))
            return createTextContent(item);
        throw new UnsupportedFormatException(format);
    }

    public boolean supports(String format) {
        return (format != null && FORMATS.contains(format));
    }

    private ContentBean createEimJsonContent(NoteItem item) {
        try {
            StringWriter out = new StringWriter();
            JsonStreamWriter writer = new JsonStreamWriter(out);
            writeJson(writer, item);
            writer.close();

            ContentBean content = new ContentBean();
            content.setValue(out.toString());
            content.setMediaType(MEDIA_TYPE_EIM_JSON);

            return content;
        } catch (Exception e) {
            throw new RuntimeException("Can't convert item to JSON", e);
        }
    }

    private void writeJson(JsonStreamWriter writer,
                           NoteItem item)
        throws JsonStreamException {
        ItemTranslator translator = new ItemTranslator(item);
        EimRecordSet recordset = translator.generateRecords();
        writer.writeRecordSet(recordset);
    }

    private ContentBean createEimmlContent(NoteItem item) {
        try {
            StringWriter out = new StringWriter();
            EimmlStreamWriter writer = new EimmlStreamWriter(out);
            writer.writeCollection(item.getUid(), null, null);
            writeEimml(writer, item);
            writer.close();

            ContentBean content = new ContentBean();
            content.setValue(out.toString());
            content.setMediaType(MEDIA_TYPE_EIMML);

            return content;
        } catch (Exception e) {
            throw new RuntimeException("Can't convert item to EIMML", e);
        }
    }

    private void writeEimml(EimmlStreamWriter writer,
                            NoteItem item)
        throws EimmlStreamException {
        ItemTranslator translator = new ItemTranslator(item);
        EimRecordSet recordset = translator.generateRecords();
        writer.writeRecordSet(recordset);
    }

    private ContentBean createHtmlContent(NoteItem item) {
        ContentBean content = new ContentBean();
        content.setMediaType(MEDIA_TYPE_HTML);

        String value = new EventEntryFormatter(item).formatHtmlContent();
        if (value == null)
            value = StringEscapeUtils.escapeHtml(item.getBody());
        if (value == null)
            value = StringEscapeUtils.escapeHtml(item.getDisplayName());
        if (value == null)
            value = "";

        content.setValue(value);
        return content;
    }

    private ContentBean createTextContent(NoteItem item) {
        ContentBean content = new ContentBean();
        content.setMediaType(MEDIA_TYPE_TEXT);

        String value = new EventEntryFormatter(item).formatTextSummary();
        if (value == null)
            value = item.getDisplayName();
        if (value == null)
            value = "";

        content.setValue(value);
        return content;
    }
}
