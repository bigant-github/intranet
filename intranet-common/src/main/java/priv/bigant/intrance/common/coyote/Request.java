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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is a low-level, efficient representation of a server request. Most fields are GC-free, expensive operations are
 * delayed until the  user code needs the information.
 * <p>
 * Processing is delegated to modules, using a hook mechanism.
 * <p>
 * This class is not intended for user code - it is used internally by tomcat for processing the request in the most
 * efficient way. Users ( servlets ) can access the information using a facade, which provides the high-level view of
 * the request.
 * <p>
 * Tomcat defines a number of attributes:
 * <ul>
 * <li>"org.apache.tomcat.request" - allows access to the low-level
 * request object in trusted applications
 * </ul>
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author Alex Cruikshank [alex@epitonic.com]
 * @author Hans Bergsten [hans@gefionsoftware.com]
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public final class Request {

    // Expected maximum typical number of cookies per request.
    private static final int INITIAL_COOKIE_SIZE = 4;

    // ----------------------------------------------------------- Constructors

    public Request() {
        parameters.setQuery(queryMB);
        parameters.setURLDecoder(urlDecoder);
    }


    // ----------------------------------------------------- Instance Variables

    private int serverPort = -1;
    private final MessageBytes serverNameMB = MessageBytes.newInstance();

    private int remotePort;
    private int localPort;

    private final MessageBytes schemeMB = MessageBytes.newInstance();

    private final MessageBytes methodMB = MessageBytes.newInstance();
    private final MessageBytes uriMB = MessageBytes.newInstance();
    private final MessageBytes decodedUriMB = MessageBytes.newInstance();
    private final MessageBytes queryMB = MessageBytes.newInstance();
    private final MessageBytes protoMB = MessageBytes.newInstance();

    // remote address/host
    private final MessageBytes remoteAddrMB = MessageBytes.newInstance();
    private final MessageBytes localNameMB = MessageBytes.newInstance();
    private final MessageBytes remoteHostMB = MessageBytes.newInstance();
    private final MessageBytes localAddrMB = MessageBytes.newInstance();

    private final MimeHeaders headers = new MimeHeaders();


    /**
     * Path parameters
     */
    private final Map<String, String> pathParameters = new HashMap<>();


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

    private final RequestInfo reqProcessorMX = new RequestInfo(this);

    private boolean sendfile = true;

    private final AtomicBoolean allDataReadEventSent = new AtomicBoolean(false);


    // ------------------------------------------------------------- Properties

    public MimeHeaders getMimeHeaders() {
        return headers;
    }


    // -------------------- Request data --------------------


    public MessageBytes method() {
        return methodMB;
    }

    public MessageBytes requestURI() {
        return uriMB;
    }

    public MessageBytes queryString() {
        return queryMB;
    }

    public MessageBytes protocol() {
        return protoMB;
    }

    public MessageBytes remoteAddr() {
        return remoteAddrMB;
    }

    public MessageBytes remoteHost() {
        return remoteHostMB;
    }

    public MessageBytes localName() {
        return localNameMB;
    }

    public MessageBytes localAddr() {
        return localAddrMB;
    }

    public void setRemotePort(int port) {
        this.remotePort = port;
    }

    public void setLocalPort(int port) {
        this.localPort = port;
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


    public void setContentLength(long len) {
        this.contentLength = len;
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

    /**
     * 获取Host请求头 过滤端口号
     */
    public String getHost() {
        try {
            MessageBytes hostValueMB = headers.getUniqueValue("host");
            String host = hostValueMB.getByteChunk().toString();
            if (host == null)
                return null;

            int n = host.indexOf(':');//过滤端口号
            if (n > 0) {
                host = host.substring(0, n).trim();
            } else {
                host = host.trim();
            }
            return host;
        } catch (IllegalArgumentException iae) {
            return null;
        }
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


    public boolean hasExpectation() {
        return expectation;
    }


    // -------------------- Associated response --------------------

    public void setResponse(Response response) {
        this.response = response;
        //TODO response.setRequest(this);
    }

    protected void setHook(ActionHook hook) {
        this.hook = hook;
    }


    // -------------------- Cookies --------------------


    // -------------------- Parameters --------------------


    // -------------------- Other attributes --------------------
    // We can use notes for most - need to discuss what is of general interest

    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    public void setAvailable(int available) {
        this.available = available;
    }


    // -------------------- Input Buffer --------------------

    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }


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


    // -------------------- debug --------------------

    @Override
    public String toString() {
        return "R( " + requestURI().toString() + ")";
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
        localAddrMB.recycle();
        localNameMB.recycle();
        localPort = -1;
        remoteAddrMB.recycle();
        remoteHostMB.recycle();
        remotePort = -1;
        available = 0;
        sendfile = true;

        serverCookies.recycle();
        parameters.recycle();
        pathParameters.clear();

        uriMB.recycle();
        decodedUriMB.recycle();
        queryMB.recycle();
        methodMB.recycle();
        protoMB.recycle();

        schemeMB.recycle();

        remoteUser.recycle();
        remoteUserNeedsAuthorization = false;
        authType.recycle();
        attributes.clear();

        allDataReadEventSent.set(false);

        startTime = -1;
    }

    // -------------------- Info  --------------------
    public void updateCounters() {
        reqProcessorMX.updateCounters();
    }

    public RequestInfo getRequestProcessor() {
        return reqProcessorMX;
    }

    /*public boolean isProcessing() {
        return reqProcessorMX.getStage() == org.apache.coyote.Constants.STAGE_SERVICE;
    }*/

}
