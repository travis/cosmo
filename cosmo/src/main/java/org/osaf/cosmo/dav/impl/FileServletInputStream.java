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
package org.osaf.cosmo.dav.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;

/**
 * ServletInputStream implementation that relies on a 
 * File to stream content.  Useful when
 * needing to consume the servlet input stream multiple
 * times.
 */
public class FileServletInputStream extends ServletInputStream {

    private FileInputStream fis = null;
    private File file = null;
    
    public FileServletInputStream(File file) throws IOException {
        this.file = file;
        reset();
    }

    @Override
    public int available() throws IOException {
        return fis.available();
    }

    @Override
    public void close() throws IOException {
        fis.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        fis.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return fis.markSupported();
    }

    @Override
    public int read() throws IOException {
        return fis.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return fis.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return fis.read(b);
    }

    @Override
    public synchronized void reset() throws IOException {
        if(fis!=null)
            fis.close();
        fis = new FileInputStream(file);
    }

    @Override
    public long skip(long n) throws IOException {
        return fis.skip(n);
    }
    
}
