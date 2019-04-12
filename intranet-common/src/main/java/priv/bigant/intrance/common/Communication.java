package priv.bigant.intrance.common;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class Communication extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Communication.class);

    protected byte[] bytes = new byte[1024];
    protected Socket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;

    public Communication(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public Communication() {
    }

    public abstract void connect();

    public void close() {
        try {
            if (socket != null)
                socket.close();
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        } catch (IOException e) {
            LOGGER.error("communication close error ", e);
        }
        socket = null;
    }


    public byte[] read() {
        int readNum = 0;
        try {
            readNum = inputStream.read(bytes);
        } catch (IOException e) {
            LOGGER.error("communication read error", e);
            connect();
            return null;
        }
        return ArrayUtils.subarray(bytes, 0, readNum);
    }

    public CommunicationRequest readRequest() throws Exception {
        return CommunicationRequest.createCommunicationRequest(read());
    }

    public CommunicationResponse readResponse() {
        return CommunicationResponse.createCommunicationResponse(read());
    }

    public void write(CommunicationReturn communicationReturn) {
        try {
            outputStream.write(communicationReturn.toByte());
        } catch (IOException e) {
            LOGGER.error("communication write error", e);
            connect();
        }
    }
}
