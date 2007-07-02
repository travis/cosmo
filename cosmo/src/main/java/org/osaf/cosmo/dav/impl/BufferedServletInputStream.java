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
package org.osaf.cosmo.dav.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ServletInputStream implementation that relies on an
 * in-memory buffer or file (for larger requests) to stream content.
 * Useful when needing to consume the servlet input stream multiple
 * times.
 */
public class BufferedServletInputStream extends ServletInputStream {

    private InputStream is = null;
    private File file = null;
    private byte[] buffer = null;
    private int maxMemoryBuffer = 1024*500;
    private static final int BUFFER_SIZE = 4096;
    
    private static final Log log = LogFactory.getLog(BufferedServletInputStream.class);
    
    public BufferedServletInputStream(InputStream is) throws IOException {
       createBuffer(is);
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        is.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    @Override
    public synchronized void reset() throws IOException {
        is.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return is.skip(n);
    }
    
    /**
     * Reset the input stream, allowing it to be re-processed.
     * @throws IOException
     */
    public synchronized void resetToBeginning() throws IOException {
        is.close();
        if(file != null)
            is = new FileInputStream(file);
        else
            is = new ByteArrayInputStream(buffer);
    }
    
    private void createBuffer(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        int read = is.read(buf);
        while(read > 0) {
            bos.write(buf, 0, read);
            // If the request size is bigger than maxMemoryBuffer, then
            // buffer to file instead so we don't run out of memory
            if(bos.size()>maxMemoryBuffer) {
                createFileBuffer(bos.toByteArray(), buf, is);
                return;
            }
            read = is.read(buf);
        }
        buffer = bos.toByteArray();
        this.is = new ByteArrayInputStream(buffer);
    }
    
    private void createFileBuffer(byte[] start, byte[] buf, InputStream is) throws IOException{
        file = File.createTempFile("cosmo", "tmp");
        FileOutputStream fos = new FileOutputStream(file);
        ByteArrayInputStream bis = new ByteArrayInputStream(start);
        IOUtils.copy(bis, fos);
        IOUtils.copy(is, fos);
        fos.close();
        this.is = new FileInputStream(file);
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            if(file!=null)
                file.delete();
        } catch(Exception e) {
            log.error("error deleting temp file: "
                    + file.getAbsolutePath(), e);
        }
    }
    
}
