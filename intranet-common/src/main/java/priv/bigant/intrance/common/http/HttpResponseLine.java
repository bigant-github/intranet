package priv.bigant.intrance.common.http;


/**
 * HTTP request line enum type.
 */
final class HttpResponseLine {
    // ----------------------------------------------------- Instance Variables
    public char[] code;
    public int codeEnd;
    public char[] status;
    public int statusEnd;
    public char[] protocol;
    public int protocolEnd;

    // -------------------------------------------------------------- Constants


    public static final int INITIAL_METHOD_SIZE = 8;
    public static final int INITIAL_URI_SIZE = 64;
    public static final int INITIAL_PROTOCOL_SIZE = 8;
    public static final int MAX_METHOD_SIZE = 2048;
    public static final int MAX_URI_SIZE = 2048;
    public static final int MAX_PROTOCOL_SIZE = 2048;


    // ----------------------------------------------------------- Constructors


    public HttpResponseLine() {
        this(new char[INITIAL_METHOD_SIZE], 0, new char[INITIAL_URI_SIZE], 0, new char[INITIAL_PROTOCOL_SIZE], 0);
    }


    public HttpResponseLine(char[] code, int codeEnd, char[] status, int statusEnd, char[] protocol, int protocolEnd) {

        this.code = code;
        this.codeEnd = codeEnd;
        this.status = status;
        this.statusEnd = statusEnd;
        this.protocol = protocol;
        this.protocolEnd = protocolEnd;

    }


    // ------------------------------------------------------------- Properties


    // --------------------------------------------------------- Public Methods


    /**
     * Release all object references, and initialize instance variables, in preparation for reuse of this object.
     */
    public void recycle() {

        codeEnd = 0;
        statusEnd = 0;
        protocolEnd = 0;

    }

    // --------------------------------------------------------- Object Methods
    public int hashCode() {
        // FIXME
        return 0;
    }


    public boolean equals(Object obj) {
        return false;
    }

}
