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
import org.osaf.cosmo.eim.eimml.EimmlStreamException;
import org.osaf.cosmo.eim.eimml.EimmlStreamReader;
import org.osaf.cosmo.eim.eimml.EimmlValidationException;
import org.osaf.cosmo.model.EntityFactory;

/**
 * A class that processes content specified as EIMML-serialized EIM
 * recordsets.
 *
 * @see NoteItem
 */
public class EimmlProcessor extends BaseEimProcessor {
    private static final Log log = LogFactory.getLog(EimmlProcessor.class);

    public EimmlProcessor(EntityFactory entityFactory) {
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
        EimmlStreamReader reader = null;
        try {
            reader = new EimmlStreamReader(content);
            EimRecordSet recordset = reader.nextRecordSet();
            if (recordset == null)
                throw new ValidationException("No recordset read from stream");
            return recordset;
        } catch (IOException e) {
            throw new ProcessorException("Unable to read stream", e);
        } catch (EimmlValidationException e) {
            throw new ValidationException("Invalid EIMML document", e);
        } catch (EimmlStreamException e) {
            throw new ProcessorException("Unable to read parse recordset", e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                log.warn("Unable to close eimml reader", e);
            }
        }
    }
}
