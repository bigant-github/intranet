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
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.coyote.AbstractProcessor;
import priv.bigant.intrance.common.coyote.HttpResponseStatus;
import priv.bigant.intrance.common.log.LogUtil;
import priv.bigant.intrance.common.util.ExceptionUtils;
import priv.bigant.intrance.common.util.buf.Ascii;
import priv.bigant.intrance.common.util.buf.ByteChunk;
import priv.bigant.intrance.common.util.http.FastHttpDateFormat;
import priv.bigant.intrance.common.util.http.MimeHeaders;
import priv.bigant.intrance.common.util.http.parser.HttpParser;
import priv.bigant.intrance.common.util.net.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.arraycopy;

public abstract class Http11Processor extends AbstractProcessor {

    private static final Logger LOG = LogUtil.getLog();

    /**
     * 接收端
     */
    private SocketBean receiver;

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


    private static final Config config = Config.getConfig();

    private final int maxHttpHeaderSize;

    public Http11Processor(int maxHttpHeaderSize, String relaxedPathChars, String relaxedQueryChars) {
        super();
        HttpParser httpParser = new HttpParser(relaxedPathChars, relaxedQueryChars);

        inputBuffer = new Http11InputBuffer(request, maxHttpHeaderSize, httpParser);
        request.setInputBuffer(inputBuffer);

        responseInputBuffer = new Http11ResponseInputBuffer(response, maxHttpHeaderSize, httpParser);
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
    public void service(SocketWrapperBase<?> socketWrapper) throws IOException {

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
                LOG.fine("http keep alive" + (keepCount++));
                responseInputBuffer.nextRequest();
                inputBuffer.nextRequest();
            }

            // Parsing the request header
            try {
                if (!inputBuffer.parseRequestLine(keptAlive)) {//解析http请求第一行
                    if (inputBuffer.getParsingRequestLinePhase() == -1) {
                        //TODO 此处为http协议升级
                    } else if (handleIncompleteRequestLineRead()) {
                        //prepareResponse(HttpResponseStatus.SC_BAD_REQUEST, "解析请求失败");
                        break;
                    }
                }

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
            } catch (SocketTimeoutException e) {
                break;
            } catch (IOException e) {
                LOG.fine("Error parsing HTTP response header" + e);
                break;
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                LOG.fine("Error parsing HTTP response header" + t);
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
                        prepareResponse(HttpResponseStatus.SC_BAD_REQUEST, "解析客户端响应失败");
                        break;
                    }
                }


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

            } catch (SocketTimeoutException e) {
                break;
            } catch (IOException e) {
                LOG.fine("Error parsing HTTP response header"+ e);
                break;
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                LOG.fine("Error parsing HTTP response header"+t);
            }

            try {
                NioChannel socket = (NioChannel) socketWrapper.getSocket();
                mutual(responseSocketWrapper, responseInputBuffer.getByteBuffer(), socket.getIOChannel(), response.isChunked(), response.getContentLength());
            } catch (IOException e) {
                LOG.severe("response mutual error"+e);
                e.printStackTrace();
                break;
            }
        } while (request.isConnection() && response.isConnection() && !isPaused());
        LOG.fine("http 完成");
        close();
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
        if (LOG.isLoggable(Level.FINE)) {
            LOG.finest("write:" + new String(byteBuffer.array(), StandardCharsets.ISO_8859_1));
        }
        if (chunked) {
            byte[] subArray = null;
            do {
                thisBuffer.position(0);
                thisBuffer.limit(thisBuffer.capacity());//展开内存
                int read = socketWrapperBase.read(true, thisBuffer);
                sum += read;
                LOG.fine("sun: " + sum);
                if (read < 2048) {
                    LOG.fine("debug");
                }
                if (read < 0) {
                    LOG.fine("read chunked to -1");
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
            } while (!Arrays.equals(subArray, chunkedEndByte));
        } else {
            while (bodySize < contentLength) {
                thisBuffer.position(0);
                thisBuffer.limit(thisBuffer.capacity());//展开内存
                int read = socketWrapperBase.read(true, thisBuffer);
                bodySize += read;
                thisBuffer.flip();
                socketChannel.write(thisBuffer);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("write:" + new String(thisBuffer.array(), 0, read));
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
                return false;
            } else {
                // Need to keep processor associated with socket
                readComplete = false;
            }
        }
        return true;
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

        if (ArrayUtils.isNotEmpty(body)) {
            outputBuffer.write(body);
        }

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


    public final void recycle() {
        //getAdapter().checkRecycled(request, response);
        request.recycle();
        response.recycle();
        inputBuffer.recycle();
        //TODO outputBuffer.recycle();
        responseInputBuffer.recycle();
        socketWrapper = null;
    }


}
