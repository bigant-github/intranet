package priv.bigant.intrance.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.exception.ServletException;
import priv.bigant.intrance.common.Config;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * response 核心解析
 */
public class ResponseProcessor implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseProcessor.class);

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
    private Config config;
    private HttpResponseLine responseLine = new HttpResponseLine();
    private SocketInputStream input;

    private int contentLength;

    private SocketBean socketBean;

    public ResponseProcessor(SocketBean socketBean, Config config) {
        this.socketBean = socketBean;
        this.config = config;
    }

    void process() {
        // Construct and initialize the objects we will need
        input = new SocketInputStream(socketBean.getIs(), config.getBufferSize());

        try {
            parseRequest(input);
            parseHeaders(input);
            // Sending a request acknowledge back to the client if
            // TODO
            ackRequest(socketBean.getOs());
            // If the protocol is HTTP/1.1, chunking is allowed.
        } catch (Exception e) {
            LOGGER.error("", e);
                /*try {
                    ((HttpServletResponse) response.getResponse()).sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }*/
        }
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
            }*/ else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
                if (header.valueEquals(DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
                    keepAlive = false;
                } else if ("keep-alive".equalsIgnoreCase(value)) {
                    keepAlive = true;
                }
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
        try {
            input.readResponseLine(responseLine);
            //String protocol = new String(responseLine.protocol, 0, responseLine.protocolEnd);
        } catch (EOFException e) {
            LOGGER.error("客户端已断开");
        }

    }

    @Override
    public void run() {
        process();
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
