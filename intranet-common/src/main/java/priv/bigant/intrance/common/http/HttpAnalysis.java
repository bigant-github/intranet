/*
package priv.bigant.intrance.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.exception.ServletException;

import java.io.IOException;

public abstract class HttpAnalysis {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpAnalysis.class);

    */
/**
     * 解析
     *
     * @param input
     *//*

    abstract void parse(SocketInputStream input);

    */
/**
     * Parse the incoming HTTP request headers, and set the appropriate request headers.
     *
     * @param input The input stream connected to our socket
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a parsing error occurs
     *//*

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
            */
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
            } else *//*

            if (header.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) {
                int n;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException("httpProcessor.parseHeaders.contentLength");
                }
                contentLength = n;
            } */
/*else if (header.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
                request.setContentType(value);
            }*//*
 else if (header.equals(DefaultHeaders.HOST_NAME)) {
                int n = value.indexOf(':');
                if (n > 0) {
                    this.host = value.substring(0, n).trim();
                } else {
                    this.host = value.trim();
                }
            } else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
                if (header.valueEquals(DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
                    keepAlive = false;
                    //response.setHeader("Connection", "close");
                }
                //request.setConnection(header);
                */
/*
                  if ("keep-alive".equalsIgnoreCase(value)) {
                  keepAlive = true;
                  }
                *//*

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

    abstract void ack();
}
*/
