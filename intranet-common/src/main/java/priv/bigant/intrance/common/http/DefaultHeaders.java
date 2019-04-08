package priv.bigant.intrance.common.http;


/**
 * HTTP default headers and header names.
 */
public final class DefaultHeaders {


    // -------------------------------------------------------------- Constants


    public static final char[] AUTHORIZATION_NAME = "authorization".toCharArray();
    public static final char[] ACCEPT_LANGUAGE_NAME = "accept-language".toCharArray();
    public static final char[] COOKIE_NAME = "cookie".toCharArray();
    public static final char[] CONTENT_LENGTH_NAME = "content-length".toCharArray();
    public static final char[] CONTENT_TYPE_NAME = "content-type".toCharArray();
    public static final char[] HOST_NAME = "host".toCharArray();
    public static final char[] CONNECTION_NAME = "connection".toCharArray();
    public static final char[] CONNECTION_CLOSE_VALUE = "close".toCharArray();
    public static final char[] EXPECT_NAME = "expect".toCharArray();
    public static final char[] EXPECT_100_VALUE = "100-continue".toCharArray();
    public static final char[] TRANSFER_ENCODING_NAME = "transfer-encoding".toCharArray();


    public static final HttpHeader CONNECTION_CLOSE = new HttpHeader("connection", "close");
    public static final HttpHeader EXPECT_CONTINUE = new HttpHeader("expect", "100-continue");
    public static final HttpHeader TRANSFER_ENCODING_CHUNKED = new HttpHeader("transfer-encoding", "chunked");


    // ----------------------------------------------------------- Constructors


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Properties


    // --------------------------------------------------------- Public Methods


    // --------------------------------------------------------- Object Methods


}
