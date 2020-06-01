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
package priv.bigant.intrance.common.coyote;

import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.util.buf.B2CConverter;
import priv.bigant.intrance.common.util.buf.ByteChunk;
import priv.bigant.intrance.common.util.buf.MessageBytes;
import priv.bigant.intrance.common.util.buf.UDecoder;
import priv.bigant.intrance.common.util.http.MimeHeaders;
import priv.bigant.intrance.common.util.http.Parameters;
import priv.bigant.intrance.common.util.http.ServerCookies;
import priv.bigant.intrance.common.util.net.ApplicationBufferHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Response object.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author Hans Bergsten [hans@gefionsoftware.com]
 * @author Remy Maucherat
 */
public final class Response {


    // Expected maximum typical number of cookies per request.
    private static final int INITIAL_COOKIE_SIZE = 4;

    // ----------------------------------------------------------- Constructors

    public Response() {
        parameters.setURLDecoder(urlDecoder);
    }


    // ----------------------------------------------------- Instance Variables

    private int serverPort = -1;
    private final MessageBytes serverNameMB = MessageBytes.newInstance();

    private int remotePort;
    private int localPort;

    private final MessageBytes statusMB = MessageBytes.newInstance();
    private final MessageBytes protoMB = MessageBytes.newInstance();
    private final MessageBytes descriptionMB = MessageBytes.newInstance();

    private final MimeHeaders headers = new MimeHeaders();


    /**
     * Path parameters
     */
    private final Map<String, String> pathParameters = new HashMap<>();

    /**
     * Notes.
     */
    private final Object notes[] = new Object[Constants.MAX_NOTES];


    /**
     * Associated input buffer.
     */
    private InputBuffer inputBuffer = null;


    /**
     * URL decoder.
     */
    private final UDecoder urlDecoder = new UDecoder();


    /**
     * HTTP specific fields. (remove them ?)
     */
    private long contentLength = -1;
    private MessageBytes contentTypeMB = null;
    private Charset charset = null;
    // Retain the original, user specified character encoding so it can be
    // returned even if it is invalid
    private String characterEncoding = null;

    /**
     * Is there an expectation ?
     */
    private boolean expectation = false;

    private final ServerCookies serverCookies = new ServerCookies(INITIAL_COOKIE_SIZE);
    private final Parameters parameters = new Parameters();

    private final MessageBytes remoteUser = MessageBytes.newInstance();
    private boolean remoteUserNeedsAuthorization = false;
    private final MessageBytes authType = MessageBytes.newInstance();
    private final HashMap<String, Object> attributes = new HashMap<>();

    private Response response;
    private volatile ActionHook hook;

    private long bytesRead = 0;
    // Time of the request - useful to avoid repeated calls to System.currentTime
    private long startTime = -1;
    private int available = 0;

    private boolean sendfile = true;

    private final AtomicBoolean allDataReadEventSent = new AtomicBoolean(false);


    // ------------------------------------------------------------- Properties

    public MimeHeaders getMimeHeaders() {
        return headers;
    }


    // -------------------- Request data --------------------


    public MessageBytes protocol() {
        return protoMB;
    }

    public MessageBytes status() {
        return statusMB;
    }

    public MessageBytes description() {
        return descriptionMB;
    }


    // -------------------- encoding/type --------------------


    /**
     * @param enc The new encoding
     * @throws UnsupportedEncodingException If the encoding is invalid
     * @deprecated This method will be removed in Tomcat 9.0.x
     */
    @Deprecated
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
        setCharset(B2CConverter.getCharset(enc));
    }


    public void setCharset(Charset charset) {
        this.charset = charset;
        this.characterEncoding = charset.name();
    }


    public int getContentLength() {
        long length = getContentLengthLong();

        if (length < Integer.MAX_VALUE) {
            return (int) length;
        }
        return -1;
    }

    public long getContentLengthLong() {
        if (contentLength > -1) {
            return contentLength;
        }

        MessageBytes clB = headers.getUniqueValue("content-length");

        contentLength = (clB == null || clB.isNull()) ? -1 : clB.getLong();

        return contentLength;
    }

    public String getContentType() {
        contentType();
        if ((contentTypeMB == null) || contentTypeMB.isNull()) {
            return null;
        }
        return contentTypeMB.toString();
    }


    public MessageBytes contentType() {
        if (contentTypeMB == null) {
            contentTypeMB = headers.getValue("content-type");
        }
        return contentTypeMB;
    }


    // -------------------- Associated response --------------------

    protected void setHook(ActionHook hook) {
        this.hook = hook;
    }


    // -------------------- Cookies --------------------


    // -------------------- Parameters --------------------


    // -------------------- Other attributes --------------------
    // We can use notes for most - need to discuss what is of general interest


    // -------------------- Input Buffer --------------------


    public void setInputBuffer(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }


    /**
     * Read data from the input buffer and put it into a byte chunk.
     * <p>
     * The buffer is owned by the protocol implementation - it will be reused on the next read. The Adapter must either
     * process the data in place or copy it to a separate buffer if it needs to hold it. In most cases this is done
     * during byte-&gt;char conversions or via InputStream. Unlike InputStream, this interface allows the app to process
     * data in place, without copy.
     *
     * @param chunk The destination to which to copy the data
     * @return The number of bytes copied
     * @throws IOException If an I/O error occurs during the copy
     */
    @Deprecated
    public int doRead(ByteChunk chunk) throws IOException {
        int n = inputBuffer.doRead(chunk);
        if (n > 0) {
            bytesRead += n;
        }
        return n;
    }


    public boolean isConnection() {
        // Check connection header
        MessageBytes connectionValueMB = headers.getValue(priv.bigant.intrance.common.coyote.http11.Constants.CONNECTION);
        if (connectionValueMB == null)
            connectionValueMB = headers.getValue(priv.bigant.intrance.common.coyote.http11.Constants.PROXY_CONNECTION);
        if (connectionValueMB != null) {
            ByteChunk connectionValueBC = connectionValueMB.getByteChunk();
            if (Http11Processor.findBytes(connectionValueBC, priv.bigant.intrance.common.coyote.http11.Constants.CLOSE_BYTES) != -1) {
                return false;
            } else if (Http11Processor.findBytes(connectionValueBC, priv.bigant.intrance.common.coyote.http11.Constants.KEEPALIVE_BYTES) != -1) {
                return true;
            }
        }
        return false;
    }

    public boolean isChunked() {
        // Parse transfer-encoding header
        MessageBytes transferEncodingValueMB = headers.getValue("transfer-encoding");
        if (transferEncodingValueMB != null) {
            String transferEncodingValue = transferEncodingValueMB.toString();
            return transferEncodingValue != null && transferEncodingValue.contains("chunked");
        }
        return false;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    // -------------------- Per-Request "notes" --------------------


    // -------------------- Recycling --------------------


    public void recycle() {
        bytesRead = 0;

        contentLength = -1;
        contentTypeMB = null;
        charset = null;
        characterEncoding = null;
        expectation = false;
        headers.recycle();
        serverNameMB.recycle();
        serverPort = -1;
        localPort = -1;
        remotePort = -1;
        available = 0;
        sendfile = true;

        serverCookies.recycle();
        parameters.recycle();
        pathParameters.clear();

        protoMB.recycle();


        remoteUser.recycle();
        remoteUserNeedsAuthorization = false;
        authType.recycle();
        attributes.clear();

        allDataReadEventSent.set(false);

        startTime = -1;
    }


    /*public boolean isProcessing() {
        return reqProcessorMX.getStage() == org.apache.coyote.Constants.STAGE_SERVICE;
    }*/

    /**
     * Parse the character encoding from the specified content type header. If the content type is null, or there is no
     * explicit character encoding,
     * <code>null</code> is returned.
     *
     * @param contentType a content type header
     */
    private static String getCharsetFromContentType(String contentType) {

        if (contentType == null) {
            return null;
        }
        int start = contentType.indexOf("charset=");
        if (start < 0) {
            return null;
        }
        String encoding = contentType.substring(start + 8);
        int end = encoding.indexOf(';');
        if (end >= 0) {
            encoding = encoding.substring(0, end);
        }
        encoding = encoding.trim();
        if ((encoding.length() > 2) && (encoding.startsWith("\"")) && (encoding.endsWith("\""))) {
            encoding = encoding.substring(1, encoding.length() - 1);
        }

        return encoding.trim();
    }
}
