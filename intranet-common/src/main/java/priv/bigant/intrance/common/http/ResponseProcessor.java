package priv.bigant.intrance.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.exception.ServletException;
import priv.bigant.intrance.common.thread.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ResponseProcessor implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseProcessor.class);

    private List<HttpHeader> httpHeaders = new ArrayList<>();

    /**
     * Keep alive indicator.
     */
    private boolean keepAlive = false;

    private Container container;

    /**
     * HTTP/1.1 client.
     */
    private boolean http11 = true;


    /**
     * True if the client has asked to recieve a request acknoledgement. If so the server will send a preliminary 100
     * Continue response just after it has successfully parsed the request headers, and before starting reading the
     * request entity body.
     */
    private boolean sendAck = false;
    private Socket socket;
    private Config config;
    private HttpResponseLine responseLine = new HttpResponseLine();
    private SocketInputStream input;
    private OutputStream output;

    protected int contentLength;
    String host;

    private String protocol;

    public ResponseProcessor(Socket socket, Config config) {
        this.socket = socket;
        this.config = config;
    }

    public ResponseProcessor(Container container, Socket socket, Config config) {
        this.container = container;
        this.socket = socket;
        this.config = config;
    }

    public ResponseProcessor(Socket socket) {
        this.socket = socket;
    }

    protected void process() {
        // Construct and initialize the objects we will need
        try {
            input = new SocketInputStream(socket.getInputStream(), config.getBufferSize());
        } catch (Exception e) {
            LOGGER.error("process.create", e);
        }

        try {
            parseRequest(input);
            if (!protocol.startsWith("HTTP/0"))
                parseHeaders(input);
            if (http11) {
                // Sending a request acknowledge back to the client if
                // TODO
                ackRequest(output);
                // If the protocol is HTTP/1.1, chunking is allowed.
                    /*if (connector.isChunkingAllowed())
                        response.setAllowChunking(true);*/
            }
            //container.invoke(this, input);
        } catch (Exception e) {
            LOGGER.error("", e);
                /*try {
                    ((HttpServletResponse) response.getResponse()).sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }*/
        }
    }

    public void shutdown() throws IOException {
        try {
            int available = input.available();
            // skip any unread (bogus) bytes
            if (available > 0) {
                input.skip(available);
            }
        } catch (Throwable e) {
            ;
        }
        socket.close();
    }

    /**
     * Send a confirmation that a request has been processed when pipelining. HTTP/1.1 100 Continue is sent back to the
     * client.
     *
     * @param output Socket output stream
     */
    private void ackRequest(OutputStream output) throws IOException {
        if (sendAck)
            output.write(new byte[1]);
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
            // Set the corresponding request headers
            /*if (header.equals(DefaultHeaders.AUTHORIZATION_NAME)) {
                request.setAuthorization(value);
            } else if (header.equals(DefaultHeaders.ACCEPT_LANGUAGE_NAME)) {
                parseAcceptLanguage(value);
            } else if (header.equals(DefaultHeaders.COOKIE_NAME)) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals(Globals.SESSION_COOKIE_NAME)) {
                        // Override anything requested in the URL
                        if (!request.isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie
                            request.setRequestedSessionId(cookies[i].getValue());
                            request.setRequestedSessionCookie(true);
                            request.setRequestedSessionURL(false);
                            if (debug >= 1)
                                log(" Requested cookie session id is " + ((HttpServletRequest) request.getRequest()).getRequestedSessionId());
                        }
                    }
                    if (debug >= 1)
                        log(" Adding cookie " + cookies[i].getName() + "=" + cookies[i].getValue());
                    request.addCookie(cookies[i]);
                }
            } else */
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
                this.host = value.substring(0, n).trim();
            } else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
                if (header.valueEquals(DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
                    keepAlive = false;
                    //response.setHeader("Connection", "close");
                }
                //request.setConnection(header);
                /*
                  if ("keep-alive".equalsIgnoreCase(value)) {
                  keepAlive = true;
                  }
                */
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

    private boolean chunked = false;

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
    private void parseRequest(SocketInputStream input) throws IOException {

        // Parse the incoming request line
        input.readResponseLine(responseLine);

        protocol = new String(responseLine.protocol, 0, responseLine.protocolEnd);

        //System.out.println(" Method:" + method + "_ Uri:" + uri
        //                   + "_ Protocol:" + protocol);

        if (protocol.length() == 0)
            protocol = "HTTP/0.9";

        // Now check if the connection should be kept alive after parsing the
        // request.
        if (protocol.equals("HTTP/1.1")) {
            http11 = true;
            sendAck = false;
        } else {
            http11 = false;
            sendAck = false;
            // For HTTP/1.0, connection are not persistent by default,
            // unless specified with a Connection: Keep-Alive header.
            keepAlive = false;
        }

    }

    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public void run() {
        process();
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
}
