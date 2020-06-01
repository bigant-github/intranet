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

import priv.bigant.intrance.common.util.res.StringManager;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;


/**
 * NIO based character decoder.
 */
public class B2CConverter {

    private static final StringManager sm = StringManager.getManager(Constants.Package);

    private static final CharsetCache charsetCache = new CharsetCache();


    /**
     * Obtain the Charset for the given encoding
     *
     * @param enc The name of the encoding for the required charset
     * @return The Charset corresponding to the requested encoding
     * @throws UnsupportedEncodingException If the requested Charset is not available
     */
    public static Charset getCharset(String enc)
            throws UnsupportedEncodingException {

        // Encoding names should all be ASCII
        String lowerCaseEnc = enc.toLowerCase(Locale.ENGLISH);

        return getCharsetLower(lowerCaseEnc);
    }


    /**
     * Only to be used when it is known that the encoding name is in lower case.
     *
     * @param lowerCaseEnc The name of the encoding for the required charset in lower case
     * @return The Charset corresponding to the requested encoding
     * @throws UnsupportedEncodingException If the requested Charset is not available
     * @deprecated Will be removed in Tomcat 9.0.x
     */
    @Deprecated
    public static Charset getCharsetLower(String lowerCaseEnc)
            throws UnsupportedEncodingException {

        Charset charset = charsetCache.getCharset(lowerCaseEnc);

        if (charset == null) {
            // Pre-population of the cache means this must be invalid
            throw new UnsupportedEncodingException(
                    sm.getString("b2cConverter.unknownEncoding", lowerCaseEnc));
        }
        return charset;
    }




}
