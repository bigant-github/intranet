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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.coyote.*;
import priv.bigant.intrance.common.coyote.http11.filters.BufferedInputFilter;
import priv.bigant.intrance.common.coyote.http11.filters.SavedRequestInputFilter;
import priv.bigant.intrance.common.util.ExceptionUtils;
import priv.bigant.intrance.common.util.buf.Ascii;
import priv.bigant.intrance.common.util.buf.ByteChunk;
import priv.bigant.intrance.common.util.http.FastHttpDateFormat;
import priv.bigant.intrance.common.util.http.MimeHeaders;
import priv.bigant.intrance.common.util.http.parser.HttpParser;
import priv.bigant.intrance.common.util.log.UserDataHelper;
import priv.bigant.intrance.common.util.net.*;
import priv.bigant.intrance.common.util.net.AbstractEndpoint.Handler.SocketState;
import priv.bigant.intrance.common.util.res.StringManager;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.lang.System.arraycopy;

public abstract class Http11Processor extends AbstractProcessor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);
    private SocketBean receiver;
    /**
     * The string manager for this package.
     */
    private static final StringManager sm = StringManager.getManager(Http11Processor.class);

    /**
     * Input.
     */
    protected final Http11InputBuffer inputBuffer;

    /**
     * Input.
     */
    protected final Http11ResponseInputBuffer responseInputBuffer;

    /**
     * Keep-alive.
     */
    protected volatile boolean keepAlive = true;

    /**
     * Flag used to indicate that the socket should be kept open (e.g. for keep alive or send file.
     */
    protected boolean openSocket = false;

    /**
     * Flag that indicates if the request headers have been completely read.
     */
    protected boolean readComplete = true;

    /**
     * HTTP/1.1 flag.
     */
    protected boolean http11 = true;

    /**
     * HTTP/0.9 flag.
     */
    protected boolean http09 = false;


    /**
     * Maximum timeout on uploads. 5 minutes as in Apache HTTPD server.
     */
    protected int connectionUploadTimeout = 300000;


    /**
     * Flag to disable setting a different time-out on uploads.
     */
    protected boolean disableUploadTimeout = false;


    /**
     * Max saved post size.
     */
    protected int maxSavePostSize = 4 * 1024;


    /**
     * Allow a customized the server header for the tin-foil hat folks.
     */
    private String server = "BigAnt";

    /**
     * Instance of the new protocol to use after the HTTP connection has been upgraded.
     */
    protected UpgradeToken upgradeToken = null;


    /**
     * Sendfile data.
     */
    protected SendfileDataBase sendFileData = null;


    private Config config;

    private int maxHttpHeaderSize;

    public Http11Processor(int maxHttpHeaderSize, boolean rejectIllegalHeaderName, String relaxedPathChars, String relaxedQueryChars) {

        super();
        config = Config.getConfig();
        HttpParser httpParser = new HttpParser(relaxedPathChars, relaxedQueryChars);

        inputBuffer = new Http11InputBuffer(request, maxHttpHeaderSize, rejectIllegalHeaderName, httpParser);
        request.setInputBuffer(inputBuffer);

        responseInputBuffer = new Http11ResponseInputBuffer(response, maxHttpHeaderSize, rejectIllegalHeaderName, httpParser);
        response.setInputBuffer(responseInputBuffer);

        this.maxHttpHeaderSize = maxHttpHeaderSize;
    }


    /**
     * Set the server header name.
     *
     * @param server The new value to use for the server header
     */
    public void setServer(String server) {
        if (StringUtils.isEmpty(server)) {
            this.server = null;
        } else {
            this.server = server;
        }
    }


    /**
     * Specialized utility method: find a sequence of lower case bytes inside a ByteChunk.
     */
    public static int findBytes(ByteChunk bc, byte[] b) {

        byte first = b[0];
        byte[] buff = bc.getBuffer();
        int start = bc.getStart();
        int end = bc.getEnd();

        // Look for first char
        int srcEnd = b.length;

        for (int i = start; i <= (end - srcEnd); i++) {
            if (Ascii.toLower(buff[i]) != first) {
                continue;
            }
            // found first char, now look for a match
            int myPos = i + 1;
            for (int srcPos = 1; srcPos < srcEnd; ) {
                if (Ascii.toLower(buff[myPos++]) != b[srcPos++]) {
                    break;
                }
                if (srcPos == srcEnd) {
                    return i - start; // found it
                }
            }
        }
        return -1;
    }


    @Override
    public AbstractEndpoint.Handler.SocketState service(SocketWrapperBase<?> socketWrapper) throws IOException {
        //RequestInfo rp = request.getRequestProcessor();
        //rp.setStage(priv.bigant.intrance.common.coyote.Constants.STAGE_PARSE);
        // Setting up the I/O
        setSocketWrapper(socketWrapper);
        inputBuffer.init(socketWrapper);
        // Flags
        keepAlive = true;
        openSocket = false;
        readComplete = true;
        boolean keptAlive = false;
        int keepCount = 0;
        SocketWrapperBase<NioChannel> responseSocketWrapper = null;

        do {
            if (request.isConnection() && response.isConnection()) {
                log.debug("http keep alive" + (keepCount++));
                responseInputBuffer.nextRequest();
                inputBuffer.nextRequest();
            }

            // Parsing the request header
            try {
                if (!inputBuffer.parseRequestLine(keptAlive)) {//解析http请求第一行
                    if (inputBuffer.getParsingRequestLinePhase() == -1) {
                        return AbstractEndpoint.Handler.SocketState.UPGRADING;
                    } else if (handleIncompleteRequestLineRead()) {
                        //prepareResponse(HttpResponseStatus.SC_BAD_REQUEST, "解析请求失败");
                        break;
                    }
                }

                if (isPaused()) {
                    // 503 - Service unavailable
                    //TODO response.setStatus(503);
                    setErrorState(ErrorState.CLOSE_CLEAN, null);
                } else {
                    keptAlive = true;
                    // Set this every time in case limit has been changed via JMX
                    request.getMimeHeaders().setLimit(getMaxHeaderCount());
                    if (!inputBuffer.parseHeaders()) {//解析http请求头
                        // We've read part of the request, don't recycle it
                        // instead associate it with the socket
                        openSocket = true;
                        readComplete = false;
                        break;
                    }
                    /*if (!disableUploadTimeout) {
                        socketWrapper.setReadTimeout(connectionUploadTimeout);
                    }*/
                }
            } catch (IOException e) {
                log.debug("Error parsing HTTP request header:", e);
                setErrorState(ErrorState.CLOSE_CONNECTION_NOW, e);
                break;
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                UserDataHelper.Mode logMode = userDataHelper.getNextMode();
                if (logMode != null) {
                    String message = sm.getString("http11processor.header.parse");
                    switch (logMode) {
                        case INFO_THEN_DEBUG:
                            message += sm.getString("http11processor.fallToDebug");
                            //$FALL-THROUGH$
                        case INFO:
                            log.info(message, t);
                            break;
                        case DEBUG:
                            log.debug(message, t);
                    }
                }
                // 400 - Bad Request
                //TODO response.setStatus(400);
                setErrorState(ErrorState.CLOSE_CLEAN, t);
            }


            if (responseSocketWrapper == null) {
                receiver = getSocketBean();
                if (receiver == null) {
                    prepareResponse(HttpResponseStatus.SC_NOT_FOUND, "未找到客户端");
                    break;
                }

                NioChannel nioChannel = new NioChannel(receiver.getSocketChannel(), new SocketBufferHandler(config.getHttpProcessReadBufferSize(), config.getHttpProcessWriteBufferSize(), true));
                responseSocketWrapper = new NioSocketWrapper(nioChannel, getNioSelectorPool());
                nioChannel.setSocketWrapper(responseSocketWrapper);
                responseInputBuffer.init(responseSocketWrapper);
            }

            try {
                mutual(socketWrapper, inputBuffer.getByteBuffer(), receiver.getSocketChannel(), request.isChunked(), request.getContentLength());
            } catch (IOException e) {
                prepareResponse(HttpResponseStatus.SC_BAD_REQUEST, "发送至客户端请求失败");
                break;
            }

            try {
                if (!responseInputBuffer.parseResponseLine(keptAlive)) {//解析http请求第一行
                    if (responseInputBuffer.getParsingRequestLinePhase() == -1) {
                        return AbstractEndpoint.Handler.SocketState.UPGRADING;
                    } else if (handleIncompleteRequestLineRead()) {
                        prepareResponse(HttpResponseStatus.SC_BAD_REQUEST, "解析客户端响应失败");
                        break;
                    }
                }

                if (isPaused()) {
                    // 503 - Service unavailable
                    //TODO response.setStatus(503);
                    setErrorState(ErrorState.CLOSE_CLEAN, null);
                } else {
                    keptAlive = true;
                    // Set this every time in case limit has been changed via JMX
                    response.getMimeHeaders().setLimit(getMaxHeaderCount());
                    if (!responseInputBuffer.parseHeaders()) {//解析http请求头
                        // We've read part of the request, don't recycle it
                        // instead associate it with the socket
                        openSocket = true;
                        readComplete = false;
                        prepareResponse(HttpResponseStatus.SC_BAD_REQUEST, "解析客户端响应头失败");
                        break;
                    }
                    if (!disableUploadTimeout) {
                        socketWrapper.setReadTimeout(connectionUploadTimeout);
                    }
                }

            } catch (SocketTimeoutException e) {
                //log.debug("Error parsing HTTP request header time out", e);
                setErrorState(ErrorState.CLOSE_CONNECTION_NOW, e);
                break;
            } catch (IOException e) {
                log.debug("Error parsing HTTP response header", e);
                setErrorState(ErrorState.CLOSE_CONNECTION_NOW, e);
                break;
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                UserDataHelper.Mode logMode = userDataHelper.getNextMode();
                if (logMode != null) {
                    String message = sm.getString("http11processor.header.parse");
                    switch (logMode) {
                        case INFO_THEN_DEBUG:
                            message += sm.getString("http11processor.fallToDebug");
                            //$FALL-THROUGH$
                        case INFO:
                            log.info(message, t);
                            break;
                        case DEBUG:
                            log.debug(message, t);
                    }
                }
                // 400 - Bad Request
                //TODO response.setStatus(400);
                setErrorState(ErrorState.CLOSE_CLEAN, t);
            }

            log.debug(request.requestURI().getString() + "响应完成");
            try {
                NioChannel socket = (NioChannel) socketWrapper.getSocket();
                mutual(responseSocketWrapper, responseInputBuffer.getByteBuffer(), socket.getIOChannel(), response.isChunked(), response.getContentLength());
            } catch (IOException e) {
                log.error("response mutual error", e);
                break;
            }
        }
        while (!getErrorState().isError() && request.isConnection() && response.isConnection() && upgradeToken == null && !isPaused());
        log.debug("http 完成");
        close();
        return null;
    }

    public abstract SocketBean getSocketBean() throws IOException;

    public abstract int getMaxHeaderCount();

    public abstract boolean isPaused();

    public abstract NioSelectorPool getNioSelectorPool();

    public abstract void close() throws IOException;

    private static final byte[] chunkedEndByte = "0\r\n\r\n".getBytes(StandardCharsets.UTF_8);

    private ByteBuffer thisBuffer = ByteBuffer.allocate(2048);

    //数据传输使用
    private void mutual(SocketWrapperBase socketWrapperBase, ByteBuffer byteBuffer, SocketChannel socketChannel, boolean chunked, int contentLength) throws IOException {
        boolean blocking = socketChannel.isBlocking();
        socketChannel.configureBlocking(true);
        int bodySize = byteBuffer.limit() - byteBuffer.position();
        byteBuffer.position(0);
        int sum = byteBuffer.limit();
        socketChannel.write(byteBuffer);
        if (log.isDebugEnabled()) {
            log.debug("write:" + new String(byteBuffer.array(), StandardCharsets.ISO_8859_1));
        }
        if (chunked) {
            byte[] subArray = null;
            do {
                thisBuffer.position(0);
                thisBuffer.limit(thisBuffer.capacity());//展开内存
                int read = socketWrapperBase.read(true, thisBuffer);
                sum += read;
                log.debug("sun: " + sum);
                if (read < 2048) {
                    log.debug("debug");
                }
                if (read < 0) {
                    log.debug("read chunked to -1");
                    break;
                }
                thisBuffer.flip();
                //thisBuffer.limit(read);
                socketChannel.write(thisBuffer);
                /*if (log.isDebugEnabled()) {
                    log.debug(Arrays.toString(thisBuffer.array()));
                    //log.debug("write:" + new String(thisBuffer.array(), 0, read, StandardCharsets.UTF_8));
                }*/

                //校验是否为最后的分块
                if (thisBuffer.position() > 4) {
                    byte[] array = thisBuffer.array();
                    subArray = ArrayUtils.subarray(array, read - 5, read);
                } else {
                    if (subArray == null)
                        continue;
                    int position = thisBuffer.position();
                    arraycopy(subArray, position, subArray, 0, 5 - position);
                    arraycopy(thisBuffer.array(), 0, subArray, 5 - position, position);
                }
                System.out.println(read);
                System.out.println(Arrays.toString(subArray));
            } while (!Arrays.equals(subArray, chunkedEndByte));
            System.out.println(new String(thisBuffer.array(), "utf-8"));
        } else {
            while (bodySize < contentLength) {
                thisBuffer.position(0);
                thisBuffer.limit(thisBuffer.capacity());//展开内存
                int read = socketWrapperBase.read(true, thisBuffer);
                bodySize += read;
                thisBuffer.flip();
                socketChannel.write(thisBuffer);
                if (log.isDebugEnabled()) {
                    log.debug("write:" + new String(thisBuffer.array(), 0, read));
                }
            }
        }
    }

    private boolean handleIncompleteRequestLineRead() {
        // Haven't finished reading the request so keep the socket
        // open
        openSocket = true;
        // Check to see if we have read any of the request line yet
        if (inputBuffer.getParsingRequestLinePhase() > 1) {
            // Started to read request line.
            if (isPaused()) {
                // Partially processed the request so need to respond
                //TODO response.setStatus(503);
                setErrorState(ErrorState.CLOSE_CLEAN, null);
                return false;
            } else {
                // Need to keep processor associated with socket
                readComplete = false;
            }
        }
        return true;
    }


    private void checkExpectationAndResponseStatus() {
        if (request.hasExpectation()
            //TODO      && (response.getStatus() < 200 || response.getStatus() > 299)
        ) {
            // Client sent Expect: 100-continue but received a
            // non-2xx final response. Disable keep-alive (if enabled)
            // to ensure that the connection is closed. Some clients may
            // still send the body, some may send the next request.
            // No way to differentiate, so close the connection to
            // force the client to send the next request.
            inputBuffer.setSwallowInput(false);
            keepAlive = false;
        }
    }


    /**
     * When committing the response, we have to validate the set of headers
     */
    protected final void prepareResponse(HttpResponseStatus status, String bodyStr) throws IOException {

        byte[] body = null;
        if (StringUtils.isNotEmpty(bodyStr)) {
            body = createBody(status, bodyStr).getBytes(StandardCharsets.UTF_8);
        }
        Http11OutputBuffer outputBuffer = new Http11OutputBuffer(maxHttpHeaderSize);
        outputBuffer.init(socketWrapper);

        if (http09) {
            // HTTP/0.9
            outputBuffer.commit();
            return;
        }

        MimeHeaders headers = new MimeHeaders();
        if (ArrayUtils.isNotEmpty(body)) {
            headers.setValue("Content-Length").setLong(body.length);
        }
        headers.setValue("Content-Type").setString("text/html;charset=UTF-8");
        headers.setValue("Vary").setString("Accept-Encoding");
        headers.addValue("Date").setString(FastHttpDateFormat.getCurrentDate());
        headers.addValue(Constants.CONNECTION).setString(Constants.CLOSE);
        if (StringUtils.isNotEmpty(server)) {
            headers.setValue("Server").setString(server);
        }
        outputBuffer.sendStatus(status);

        int size = headers.size();
        for (int i = 0; i < size; i++) {
            outputBuffer.sendHeader(headers.getName(i), headers.getValue(i));
        }
        outputBuffer.endHeaders();

        if (ArrayUtils.isNotEmpty(body))
            outputBuffer.write(body);

        outputBuffer.commit();

        socketWrapper.flush(true);

    }

    public String bodyTemp = "<div style='text-align:center'>\n" +
            "  <div>\n" +
            "  <h1>BigAnt</h1>\n" +
            "  <hr/>\n" +
            "  </div>\n" +
            "<div>\n" +
            "  <div>\n" +
            "    <span>状态码：%d</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span>内容：%s</span>\n" +
            "  </div>\n" +
            "  <div>\n" +
            "    <h2>\n" +
            "      <a href='http://www.baidu.com'>去官网</a>  \n" +
            "    </h2>\n" +
            "  </div>\n" +
            "</div>\n" +
            "</div>";

    private String createBody(HttpResponseStatus status, String bodyStr) {
        return String.format(bodyTemp, status.getStatus(), bodyStr);
    }


    @Override
    protected boolean flushBufferedWrite() throws IOException {
        /*TODO if (outputBuffer.hasDataToWrite()) {
            if (outputBuffer.flushBuffer(false)) {
                // The buffer wasn't fully flushed so re-register the
                // socket for write. Note this does not go via the
                // Response since the write registration state at
                // that level should remain unchanged. Once the buffer
                // has been emptied then the code below will call
                // Adaptor.asyncDispatch() which will enable the
                // Response to respond to this event.
                outputBuffer.registerWriteInterest();
                return true;
            }
        }*/
        return false;
    }


    @Override
    protected SocketState dispatchEndRequest() {
        if (!keepAlive) {
            return SocketState.CLOSED;
        } else {
            endRequest();
            inputBuffer.nextRequest();
            //TODO outputBuffer.nextRequest();
            if (socketWrapper.isReadPending()) {
                return AbstractEndpoint.Handler.SocketState.LONG;
            } else {
                return AbstractEndpoint.Handler.SocketState.OPEN;
            }
        }
    }


    @Override
    protected Logger getLog() {
        return log;
    }


    /*
     * No more input will be passed to the application. Remaining input will be
     * swallowed or the connection dropped depending on the error and
     * expectation status.
     */
    private void endRequest() {
        if (getErrorState().isError()) {
            // If we know we are closing the connection, don't drain
            // input. This way uploading a 100GB file doesn't tie up the
            // thread if the servlet has rejected it.
            inputBuffer.setSwallowInput(false);
        } else {
            // Need to check this again here in case the response was
            // committed before the error that requires the connection
            // to be closed occurred.
            checkExpectationAndResponseStatus();
        }

        // Finish the handling of the request
        if (getErrorState().isIoAllowed()) {
            try {
                inputBuffer.endRequest();
            } catch (IOException e) {
                setErrorState(ErrorState.CLOSE_CONNECTION_NOW, e);
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                // 500 - Internal Server Error
                // Can't add a 500 to the access log since that has already been
                // written in the Adapter.service method.
                //TODO response.setStatus(500);
                setErrorState(ErrorState.CLOSE_NOW, t);
                log.error(sm.getString("http11processor.request.finish"), t);
            }
        }
        if (getErrorState().isIoAllowed()) {
            try {
                action(ActionCode.COMMIT, null);
                //TODO outputBuffer.end();
            } /*catch (IOException e) {
                setErrorState(ErrorState.CLOSE_CONNECTION_NOW, e);
            }*/ catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                setErrorState(ErrorState.CLOSE_NOW, t);
                log.error(sm.getString("http11processor.response.finish"), t);
            }
        }
    }


    @Override
    protected final int available(boolean doRead) {
        return inputBuffer.available(doRead);
    }


    @Override
    protected final void setRequestBody(ByteChunk body) {
        InputFilter savedBody = new SavedRequestInputFilter(body);
        Http11InputBuffer internalBuffer = (Http11InputBuffer) request.getInputBuffer();
        internalBuffer.addActiveFilter(savedBody);
    }


    @Override
    protected final void disableSwallowRequest() {
        inputBuffer.setSwallowInput(false);
    }


    @Override
    protected final void sslReHandShake() throws IOException {
        if (sslSupport != null) {
            // Consume and buffer the request body, so that it does not
            // interfere with the client's handshake messages
            InputFilter[] inputFilters = inputBuffer.getFilters();
            ((BufferedInputFilter) inputFilters[Constants.BUFFERED_FILTER]).setLimit(maxSavePostSize);
            inputBuffer.addActiveFilter(inputFilters[Constants.BUFFERED_FILTER]);

            /*
             * Outside the try/catch because we want I/O errors during
             * renegotiation to be thrown for the caller to handle since they
             * will be fatal to the connection.
             */
            socketWrapper.doClientAuth(sslSupport);
            try {
                /*
                 * Errors processing the cert chain do not affect the client
                 * connection so they can be logged and swallowed here.
                 */
                Object sslO = sslSupport.getPeerCertificateChain();
                if (sslO != null) {
                    request.setAttribute(SSLSupport.CERTIFICATE_KEY, sslO);
                }
            } catch (IOException ioe) {
                log.warn(sm.getString("http11processor.socket.ssl"), ioe);
            }
        }
    }


    @Override
    protected final boolean isRequestBodyFullyRead() {
        return inputBuffer.isFinished();
    }


    @Override
    protected final void registerReadInterest() {
        socketWrapper.registerReadInterest();
    }


    @Override
    protected final boolean isReadyForWrite() {
        return false;//TODO outputBuffer.isReady();
    }


    @Override
    public UpgradeToken getUpgradeToken() {
        return upgradeToken;
    }


    @Override
    protected final void doHttpUpgrade(UpgradeToken upgradeToken) {
        this.upgradeToken = upgradeToken;
        // Stop further HTTP output
        //TODO outputBuffer.responseFinished = true;
    }


    @Override
    public ByteBuffer getLeftoverInput() {
        return inputBuffer.getLeftover();
    }


    @Override
    public boolean isUpgrade() {
        return upgradeToken != null;
    }


    @Override
    public final void recycle() {
        //getAdapter().checkRecycled(request, response);
        request.recycle();
        response.recycle();
        super.recycle();
        inputBuffer.recycle();
        //TODO outputBuffer.recycle();
        responseInputBuffer.recycle();
        upgradeToken = null;
        socketWrapper = null;
        sendFileData = null;
    }


    @Override
    public void pause() {
        // NOOP for HTTP
    }


}
