package priv.bigant.intrance.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.exception.ServletException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * request 核心解析
 */
public class RequestProcessor implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestProcessor.class);
    private String uri;

    private List<HttpHeader> httpHeaders = new ArrayList<>();

    /**
     * Keep alive indicator.
     */
    private boolean keepAlive = false;

    /**
     * True if the client has asked to recieve a request acknoledgement. If so the server will send a preliminary 100
     * Continue response just after it has successfully parsed the request headers, and before starting reading the
     * request entity body.
     */
    private boolean sendAck = false;
    private boolean chunked = false;
    private Config config;
    private HttpRequestLine requestLine = new HttpRequestLine();
    private SocketInputStream input;

    private int contentLength;
    private String host;
    private String protocol;
    private static final byte[] ack = ("HTTP/1.1 100 Continue\r\n\r\n").getBytes();
    private SocketBean socketBean;

    public RequestProcessor(SocketBean socketBean, Config config) {
        this.socketBean = socketBean;
        this.config = config;
    }

    protected void process() throws IOException, ServletException {
        // Construct and initialize the objects we will need
        input = new SocketInputStream(socketBean.getIs(), config.getBufferSize());
        // Parse the incoming request
        parseRequest(input);
        if (!protocol.startsWith("HTTP/0")) {
            //TODO http version lg 1
        }
        parseHeaders(input);
        // TODO ack
        ackRequest(socketBean.getOs());

    }

    /**
     * Send a confirmation that a request has been processed when pipelining. HTTP/1.1 100 Continue is sent back to the
     * client.
     *
     * @param output Socket output stream
     */
    private void ackRequest(OutputStream output) throws IOException {
        if (sendAck)
            output.write(ack);
    }


    /**
     * Parse the incoming HTTP request headers, and set the appropriate request headers.
     *
     * @param input The input stream connected to our socket
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a parsing error occurs
     */
    private void parseHeaders(SocketInputStream input) throws IOException, ServletException {

        while (true) {
            HttpHeader header = new HttpHeader();

            // Read the next header
            input.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException("httpProcessor.parseHeaders.colon");
                }
            }

            String value = new String(header.value, 0, header.valueEnd);
            LOGGER.debug(" Header " + new String(header.name, 0, header.nameEnd) + " = " + value);
            if (header.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) {
                int n;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException("httpProcessor.parseHeaders.contentLength");
                }
                contentLength = n;
            } /*else if (header.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
                request.setContentType(value);
            }*/ else if (header.equals(DefaultHeaders.HOST_NAME)) {
                int n = value.indexOf(':');
                if (n > 0) {
                    this.host = value.substring(0, n).trim();
                } else {
                    this.host = value.trim();
                }
            } else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
                if (header.valueEquals(DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
                    keepAlive = false;
                } else if ("keep-alive".equalsIgnoreCase(value)) {
                    keepAlive = true;
                }
            } else if (header.equals(DefaultHeaders.EXPECT_NAME)) {
                if (header.valueEquals(DefaultHeaders.EXPECT_100_VALUE))
                    sendAck = true;
                else
                    throw new ServletException("httpProcessor.parseHeaders.unknownExpectation");
            } else if (header.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) {//分块传输
                chunked = true;
            }

            this.httpHeaders.add(header);

        }

    }

    public boolean isChunked() {
        return chunked;
    }


    /**
     * Parse the incoming HTTP request and set the corresponding HTTP request properties.
     *
     * @param input The input stream attached to our socket
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a parsing error occurs
     */
    private void parseRequest(SocketInputStream input) throws SocketTimeoutException, IOException {

        // Parse the incoming request line
        input.readRequestLine(requestLine);

        protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);
        uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        //System.out.println(" Method:" + method + "_ Uri:" + uri
        //                   + "_ Protocol:" + protocol);

        if (protocol.length() == 0)
            protocol = "HTTP/0.9";

        LOGGER.debug("protocol:" + protocol + "      uri:" + uri);

    }

    @Override
    public void run() {
        try {
            process();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return host;
    }

    public SocketInputStream getInput() {
        return input;
    }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public boolean isSendAck() {
        return sendAck;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setInput(SocketInputStream input) {
        this.input = input;
    }

    public String getUri() {
        return uri;
    }

}
