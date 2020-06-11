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
package priv.bigant.intrance.common.util.buf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.util.res.StringManager;

import java.io.CharConversionException;
import java.io.IOException;


/**
 * All URL decoding happens here. This way we can reuse, review, optimize without adding complexity to the buffers.
 * <p>
 * The conversion will modify the original buffer.
 *
 * @author Costin Manolache
 */
public final class UDecoder {

    private static final StringManager sm = StringManager.getManager(UDecoder.class);

    private static final Logger log = LoggerFactory.getLogger(UDecoder.class);

    public static final boolean ALLOW_ENCODED_SLASH =
            Boolean.parseBoolean(System.getProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "false"));

    private static class DecodeException extends CharConversionException {
        private static final long serialVersionUID = 1L;

        public DecodeException(String s) {
            super(s);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            // This class does not provide a stack trace
            return this;
        }
    }

    /**
     * Unexpected end of data.
     */
    private static final IOException EXCEPTION_EOF = new DecodeException("EOF");

    /**
     * %xx with not-hex digit
     */
    private static final IOException EXCEPTION_NOT_HEX_DIGIT = new DecodeException(
            "isHexDigit");

    /**
     * %-encoded slash is forbidden in resource path
     */
    private static final IOException EXCEPTION_SLASH = new DecodeException(
            "noSlash");

    public UDecoder() {
    }

    /**
     * URLDecode, will modify the source.
     *
     * @param mb    The URL encoded bytes
     * @param query <code>true</code> if this is a query string
     * @throws IOException Invalid %xx URL encoding
     */
    public void convert(ByteChunk mb, boolean query)
            throws IOException {
        int start = mb.getOffset();
        byte buff[] = mb.getBytes();
        int end = mb.getEnd();

        int idx = ByteChunk.findByte(buff, start, end, (byte) '%');
        int idx2 = -1;
        if (query) {
            idx2 = ByteChunk.findByte(buff, start, (idx >= 0 ? idx : end), (byte) '+');
        }
        if (idx < 0 && idx2 < 0) {
            return;
        }

        // idx will be the smallest positive index ( first % or + )
        if ((idx2 >= 0 && idx2 < idx) || idx < 0) {
            idx = idx2;
        }

        final boolean noSlash = !(ALLOW_ENCODED_SLASH || query);

        for (int j = idx; j < end; j++, idx++) {
            if (buff[j] == '+' && query) {
                buff[idx] = (byte) ' ';
            } else if (buff[j] != '%') {
                buff[idx] = buff[j];
            } else {
                // read next 2 digits
                if (j + 2 >= end) {
                    throw EXCEPTION_EOF;
                }
                byte b1 = buff[j + 1];
                byte b2 = buff[j + 2];
                if (!isHexDigit(b1) || !isHexDigit(b2)) {
                    throw EXCEPTION_NOT_HEX_DIGIT;
                }

                j += 2;
                int res = x2c(b1, b2);
                if (noSlash && (res == '/')) {
                    throw EXCEPTION_SLASH;
                }
                buff[idx] = (byte) res;
            }
        }

        mb.setEnd(idx);

        return;
    }

    // -------------------- Additional methods --------------------
    // XXX What do we do about charset ????







    /**
     * Decode and return the specified URL-encoded byte array.
     *
     * @param bytes   The url-encoded byte array
     * @param enc     The encoding to use; if null, ISO-8859-1 is used. If an unsupported encoding is specified null
     *                will be returned
     * @param isQuery Is this a query string being processed
     * @return the decoded string
     * @throws IllegalArgumentException if a '%' character is not followed by a valid 2-digit hexadecimal number
     * @deprecated This method will be removed in Tomcat 9
     */
    @Deprecated
    public static String URLDecode(byte[] bytes, String enc, boolean isQuery) {
        throw new IllegalArgumentException(sm.getString("udecoder.urlDecode.iae"));
    }


    private static boolean isHexDigit(int c) {
        return ((c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                (c >= 'A' && c <= 'F'));
    }


    private static int x2c(byte b1, byte b2) {
        int digit = (b1 >= 'A') ? ((b1 & 0xDF) - 'A') + 10 :
                (b1 - '0');
        digit *= 16;
        digit += (b2 >= 'A') ? ((b2 & 0xDF) - 'A') + 10 :
                (b2 - '0');
        return digit;
    }


}
