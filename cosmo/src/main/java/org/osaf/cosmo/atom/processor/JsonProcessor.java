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
package org.osaf.cosmo.atom.processor;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.json.JsonStreamException;
import org.osaf.cosmo.eim.json.JsonStreamReader;
import org.osaf.cosmo.eim.json.JsonValidationException;
import org.osaf.cosmo.model.EntityFactory;

/**
 * A class that processes content specified as JSON-serialized EIM
 * recordsets.
 *
 * @see NoteItem
 */
public class JsonProcessor extends BaseEimProcessor {
    private static final Log log = LogFactory.getLog(JsonProcessor.class);

    public JsonProcessor(EntityFactory entityFactory) {
        super(entityFactory);
    }
    
    /**
     * Converts the EIMML content body into a valid EIM record set.
     *
     * @throws ValidationException if the content does not represent a
     * valid EIM record set
     * @throws ProcessorException
     */
    protected EimRecordSet readRecordSet(Reader content)
        throws ValidationException, ProcessorException {
        JsonStreamReader reader = null;
        try {
            reader = new JsonStreamReader(content);
            EimRecordSet recordset = reader.nextRecordSet();
            if (recordset == null)
                throw new ValidationException("No recordset read from stream");
            return recordset;
        } catch (IOException e) {
            throw new ProcessorException("Unable to read stream", e);
        } catch (JsonValidationException e) {
            throw new ValidationException("Invalid json packet", e);
        } catch (JsonStreamException e) {
            throw new ProcessorException("Unable to parse recordset", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception e) {
                log.warn("Unable to close json reader", e);
            }
        }
    }
}
