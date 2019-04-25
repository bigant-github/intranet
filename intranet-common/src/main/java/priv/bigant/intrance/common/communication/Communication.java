package priv.bigant.intrance.common.communication;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

    public synchronized void close() {
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
        inputStream = null;
        outputStream = null;

    }


    public synchronized byte[] read() {
        int readNum = 0;
        try {
            readNum = inputStream.read(bytes);
        } catch (IOException e) {
            LOGGER.error("communication read error", e);
            return null;
        }
        byte[] subarray = ArrayUtils.subarray(bytes, 0, readNum);
        LOGGER.debug("read :" + new String(subarray, StandardCharsets.UTF_8));
        return subarray;
    }

    public synchronized CommunicationRequest readRequest() {
        return CommunicationRequest.createCommunicationRequest(read());
    }

    public synchronized CommunicationResponse readResponse() {
        return CommunicationResponse.createCommunicationResponse(read());
    }

    public synchronized void write(CommunicationReturn communicationReturn) throws IOException {
        byte[] bytes = communicationReturn.toByte();
        LOGGER.debug("write :" + new String(bytes, StandardCharsets.UTF_8));
        outputStream.write(bytes);
    }

    /**
     * 判断是否断开连接，断开返回true,没有返回false
     */
    public Boolean sendUrgentData() {
        try {
            socket.sendUrgentData(0xFF);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            return false;
        } catch (Exception se) {
            return true;
        }
    }
}
