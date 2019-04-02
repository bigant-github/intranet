package priv.bigant.intrance.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.exception.ServletException;
import priv.bigant.intrance.common.thread.Config;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HttpProcessor1 implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpProcessor1.class);

    private List httpHeaders = new ArrayList<HttpHeader>();

    /**
     * Keep alive indicator.
     */
    private boolean keepAlive = false;


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
    private HttpRequestLine requestLine = new HttpRequestLine();


    private int contentLength;

    private String protocol;

    public HttpProcessor1(Socket socket, Config config) {
        this.socket = socket;
        this.config = config;
    }

    private void process(Socket socket) {
        boolean ok = true;
        boolean finishResponse = true;
        SocketInputStream input = null;
        OutputStream output = null;

        // Construct and initialize the objects we will need
        try {
            input = new SocketInputStream(socket.getInputStream(), config.getBufferSize());
        } catch (Exception e) {
            LOGGER.error("process.create", e);
            ok = false;
        }

        keepAlive = true;

        while (ok && keepAlive) {

            finishResponse = true;

            /*try {
                request.setStream(input);
                request.setResponse(response);
                output = socket.getOutputStream();
                response.setStream(output);
                response.setRequest(request);
            } catch (Exception e) {
                LOGGER.error("process.create", e);
                ok = false;
            }*/

            // Parse the incoming request
            try {
                if (ok) {
                    parseRequest(input, output);
                    if (protocol.startsWith("HTTP/0"))
                        parseHeaders(input);
                    if (http11) {
                        // Sending a request acknowledge back to the client if
                        // requested.
                        ackRequest(output);
                        // If the protocol is HTTP/1.1, chunking is allowed.
                        /*if (connector.isChunkingAllowed())
                            response.setAllowChunking(true);*/
                    }
                }
            } catch (EOFException e) {
                // It's very likely to be a socket disconnect on either the
                // client or the server
                ok = false;
                finishResponse = false;
            } catch (ServletException e) {
                LOGGER.error("", e);
                ok = false;
                /*try {
                    ((HttpServletResponse) response.getResponse()).sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }*/
            } catch (InterruptedIOException e) {
                LOGGER.error("", e);
                /*try {
                    ((HttpServletResponse) response.getResponse()).sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }*/
                ok = false;
            } catch (Exception e) {
                LOGGER.error("", e);
                /*try {
                    ((HttpServletResponse) response.getResponse()).sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }*/
                ok = false;
            }

            // Ask our Container to process this request
            try {
                ((HttpServletResponse) response).setHeader("Date", FastHttpDateFormat.getCurrentDate());
                if (ok) {
                    connector.getContainer().invoke(request, response);
                }
            } catch (ServletException e) {
                log("process.invoke", e);
                try {
                    ((HttpServletResponse) response.getResponse()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            } catch (InterruptedIOException e) {
                ok = false;
            } catch (Throwable e) {
                log("process.invoke", e);
                try {
                    ((HttpServletResponse) response.getResponse()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            }

            // Finish up the handling of the reques t
            if (finishResponse) {
                try {
                    response.finishResponse();
                } catch (IOException e) {
                    ok = false;
                } catch (Throwable e) {
                    log("process.invoke", e);
                    ok = false;
                }
                try {
                    request.finishRequest();
                } catch (IOException e) {
                    ok = false;
                } catch (Throwable e) {
                    LOGGER.error("process.invoke", e);
                    ok = false;
                }
                try {
                    if (output != null)
                        output.flush();
                } catch (IOException e) {
                    ok = false;
                }
            }

            // Recycling the request and the response objects
            request.recycle();
            response.recycle();

        }

        try {
            shutdownInput(input);
            socket.close();
        } catch (Throwable e) {
            LOGGER.error("process.invoke", e);
        }

        socket = null;
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
            }*/ /*else if (header.equals(DefaultHeaders.HOST_NAME)) {
                int n = value.indexOf(':');
                if (n < 0) {
                    if (connector.getScheme().equals("http")) {
                        request.setServerPort(80);
                    } else if (connector.getScheme().equals("https")) {
                        request.setServerPort(443);
                    }
                    if (proxyName != null)
                        request.setServerName(proxyName);
                    else
                        request.setServerName(value);
                } else {
                    if (proxyName != null)
                        request.setServerName(proxyName);
                    else
                        request.setServerName(value.substring(0, n).trim());
                    if (proxyPort != 0)
                        request.setServerPort(proxyPort);
                    else {
                        int port = 80;
                        try {
                            port = Integer.parseInt(value.substring(n + 1).trim());
                        } catch (Exception e) {
                            throw new ServletException(sm.getString("httpProcessor.parseHeaders.portNumber"));
                        }
                        request.setServerPort(port);
                    }
                }
            }*/ else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
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
            } else if (header.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) {
                //request.setTransferEncoding(header);
            }

            this.httpHeaders.add(header);

        }

    }

    /**
     * Parse the incoming HTTP request and set the corresponding HTTP request properties.
     *
     * @param input  The input stream attached to our socket
     * @param output The output stream of the socket
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a parsing error occurs
     */
    private void parseRequest(SocketInputStream input, OutputStream output) throws IOException, ServletException {

        // Parse the incoming request line
        input.readRequestLine(requestLine);

        protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

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

    @Override
    public void run() {

    }
}
