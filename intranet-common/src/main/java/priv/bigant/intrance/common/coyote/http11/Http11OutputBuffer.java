/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package priv.bigant.intrance.common.coyote.http11;

import priv.bigant.intrance.common.coyote.HttpResponseStatus;
import priv.bigant.intrance.common.util.buf.ByteChunk;
import priv.bigant.intrance.common.util.buf.MessageBytes;
import priv.bigant.intrance.common.util.net.SocketWrapperBase;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides buffering for the HTTP headers (allowing responses to be reset before they have been committed) and the link
 * to the Socket for writing the headers (once committed) and the response body. Note that buffering of the response
 * body happens at a higher level.
 */
public class Http11OutputBuffer implements HttpOutputBuffer {

    /**
     * Associated Coyote response.
     */
    //protected HttpResponse response;


    /**
     * Finished flag.
     */
    protected boolean responseFinished;


    /**
     * The buffer used for header composition.
     */
    protected final ByteBuffer headerBuffer;

    /**
     * Underlying output buffer.
     */
    protected HttpOutputBuffer outputStreamOutputBuffer;


    /**
     * Wrapper for socket where data will be written to.
     */
    protected SocketWrapperBase<?> socketWrapper;


    /**
     * Bytes written to client for the current request
     */
    protected long byteCount = 0;


    protected Http11OutputBuffer(int headerBufferSize) {

        //this.response = response;

        headerBuffer = ByteBuffer.allocate(headerBufferSize);


        responseFinished = false;

        outputStreamOutputBuffer = new SocketOutputBuffer();

        /*if (sendReasonPhrase) {
            // Cause loading of HttpMessages
            // TODO  HttpMessages.getInstance(response.getLocale()).getMessage(200);
        }*/
    }

    // --------------------------------------------------- OutputBuffer Methods

    /**
     * @deprecated Unused. Will be removed in Tomcat 9. Use {@link #doWrite(ByteBuffer)}
     */
    @Deprecated
    @Override
    public int doWrite(ByteChunk chunk) throws IOException {
        return outputStreamOutputBuffer.doWrite(chunk);
    }


    // ----------------------------------------------- HttpOutputBuffer Methods

    /**
     * Flush the response.
     *
     * @throws IOException an underlying I/O error occurred
     */
    @Override
    public void flush() throws IOException {
        outputStreamOutputBuffer.flush();
    }


    @Override
    public void end() throws IOException {
        if (responseFinished) {
            return;
        }

        outputStreamOutputBuffer.end();
        responseFinished = true;
    }


    // --------------------------------------------------------- Public Methods


    public void init(SocketWrapperBase<?> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }


    /**
     * Commit the response.
     *
     * @throws IOException an underlying I/O error occurred
     */
    protected void commit() throws IOException {
        //response.setCommitted(true);
        if (headerBuffer.position() > 0) {
            // Sending the response header buffer
            headerBuffer.flip();
            try {
                socketWrapper.write(isBlocking(), headerBuffer);
            } finally {
                headerBuffer.position(0).limit(headerBuffer.capacity());
            }
        }
    }

    /**
     * Is standard Servlet blocking IO being used for output?
     *
     * @return <code>true</code> if this is blocking IO
     */
    protected final boolean isBlocking() {
        return true;
    }

    /**
     * Send the response status line.
     */
    public void sendStatus(HttpResponseStatus status) {
        // Write protocol name
        write(Constants.HTTP_11_BYTES);
        headerBuffer.put(Constants.SP);

        // Write status code
        //HttpResponseStatus status = response.getStatus();
        write(status.getStatus());

        headerBuffer.put(Constants.SP);

        write(status.getDesc());

        headerBuffer.put(Constants.CR).put(Constants.LF);
    }


    /**
     * Send a header.
     *
     * @param name  Header name
     * @param value Header value
     */
    public void sendHeader(MessageBytes name, MessageBytes value) {
        write(name);
        headerBuffer.put(Constants.COLON).put(Constants.SP);
        write(value);
        headerBuffer.put(Constants.CR).put(Constants.LF);
    }


    /**
     * End the header block.
     */
    public void endHeaders() {
        headerBuffer.put(Constants.CR).put(Constants.LF);
    }


    /**
     * This method will write the contents of the specified message bytes buffer to the output stream, without
     * filtering. This method is meant to be used to write the response header.
     *
     * @param mb data to be written
     */
    private void write(MessageBytes mb) {
        if (mb.getType() != MessageBytes.T_BYTES) {
            mb.toBytes();
            ByteChunk bc = mb.getByteChunk();
            // Need to filter out CTLs excluding TAB. ISO-8859-1 and UTF-8
            // values will be OK. Strings using other encodings may be
            // corrupted.
            byte[] buffer = bc.getBuffer();
            for (int i = bc.getOffset(); i < bc.getLength(); i++) {
                // byte values are signed i.e. -128 to 127
                // The values are used unsigned. 0 to 31 are CTLs so they are
                // filtered (apart from TAB which is 9). 127 is a control (DEL).
                // The values 128 to 255 are all OK. Converting those to signed
                // gives -128 to -1.
                if ((buffer[i] > -1 && buffer[i] <= 31 && buffer[i] != 9) ||
                        buffer[i] == 127) {
                    buffer[i] = ' ';
                }
            }
        }
        write(mb.getByteChunk());
    }


    /**
     * This method will write the contents of the specified byte chunk to the output stream, without filtering. This
     * method is meant to be used to write the response header.
     *
     * @param bc data to be written
     */
    private void write(ByteChunk bc) {
        // Writing the byte chunk to the output buffer
        int length = bc.getLength();
        checkLengthBeforeWrite(length);
        headerBuffer.put(bc.getBytes(), bc.getStart(), length);
    }


    /**
     * This method will write the contents of the specified byte buffer to the output stream, without filtering. This
     * method is meant to be used to write the response header.
     *
     * @param b data to be written
     */
    public void write(byte[] b) {
        checkLengthBeforeWrite(b.length);

        // Writing the byte chunk to the output buffer
        headerBuffer.put(b);
    }


    /**
     * This method will write the contents of the specified String to the output stream, without filtering. This method
     * is meant to be used to write the response header.
     *
     * @param s data to be written
     */
    private void write(String s) {
        if (s == null) {
            return;
        }

        // From the Tomcat 3.3 HTTP/1.0 connector
        int len = s.length();
        checkLengthBeforeWrite(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // Note: This is clearly incorrect for many strings,
            // but is the only consistent approach within the current
            // servlet framework. It must suffice until servlet output
            // streams properly encode their output.
            if (((c <= 31) && (c != 9)) || c == 127 || c > 255) {
                c = ' ';
            }
            headerBuffer.put((byte) c);
        }
    }


    /**
     * This method will write the specified integer to the output stream. This method is meant to be used to write the
     * response header.
     *
     * @param value data to be written
     */
    private void write(int value) {
        // From the Tomcat 3.3 HTTP/1.0 connector
        String s = Integer.toString(value);
        int len = s.length();
        checkLengthBeforeWrite(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            headerBuffer.put((byte) c);
        }
    }


    /**
     * Checks to see if there is enough space in the buffer to write the requested number of bytes.
     */
    private void checkLengthBeforeWrite(int length) {
        // "+ 4": BZ 57509. Reserve space for CR/LF/COLON/SP characters that
        // are put directly into the buffer following this write operation.
        if (headerBuffer.position() + length + 4 > headerBuffer.capacity()) {
            throw new HeadersTooLargeException("An attempt was made to write more data to the response headers than there was room available in the buffer. Increase maxHttpHeaderSize on the connector or write less data into the response headers.");
        }
    }


    // ------------------------------------------ SocketOutputBuffer Inner Class

    /**
     * This class is an output buffer which will write data to a socket.
     */
    protected class SocketOutputBuffer implements HttpOutputBuffer {

        /**
         * Write chunk.
         *
         * @deprecated Unused. Will be removed in Tomcat 9. Use {@link #doWrite(ByteBuffer)}
         */
        @Deprecated
        @Override
        public int doWrite(ByteChunk chunk) throws IOException {
            int len = chunk.getLength();
            int start = chunk.getStart();
            byte[] b = chunk.getBuffer();
            //TODO socketWrapper.write(isBlocking(), b, start, len);
            byteCount += len;
            return len;
        }

        @Override
        public void end() throws IOException {
            socketWrapper.flush(true);
        }

        @Override
        public void flush() throws IOException {
            //TODO socketWrapper.flush(isBlocking());
        }
    }
}
