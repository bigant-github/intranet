package priv.bigant.intrance.common.exception;

public class ServletException extends Exception {
    private Throwable rootCause;

    public ServletException() {
    }

    public ServletException(String message) {
        super(message);
    }

    public ServletException(String message, Throwable rootCause) {
        super(message);
        this.rootCause = rootCause;
    }

    public ServletException(Throwable rootCause) {
        super(rootCause.getLocalizedMessage());
        this.rootCause = rootCause;
    }

    public Throwable getRootCause() {
        return this.rootCause;
    }
}

