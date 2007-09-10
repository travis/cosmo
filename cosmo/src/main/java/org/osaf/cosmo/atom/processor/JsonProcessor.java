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
import java.io.StringReader;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.json.JsonStreamException;
import org.osaf.cosmo.eim.json.JsonStreamReader;
import org.osaf.cosmo.eim.json.JsonValidationException;

/**
 * A class that processes content specified as JSON-serialized EIM
 * recordsets.
 *
 * @see NoteItem
 */
public class JsonProcessor extends BaseEimProcessor {
    private static final Log log = LogFactory.getLog(JsonProcessor.class);

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
            byte[] encoded = IOUtils.toString(content).getBytes("UTF-8");
            String decoded = new String(Base64.decodeBase64(encoded), "UTF-8");

            reader = new JsonStreamReader(new StringReader(decoded));
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
