package priv.bigant.intrance.common.communication;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 客户端与服务端通信的工具
 */
public abstract class Communication extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Communication.class);

    protected byte[] bytes = new byte[1024];
    protected ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    protected Socket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected SocketChannel socketChannel;


    public Communication(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public Communication(SocketChannel socketChannel) throws IOException {
        this(socketChannel.socket());
        this.socketChannel = socketChannel;
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

    public synchronized byte[] readN() throws IOException {
        byteBuffer.clear();
        int readNum = socketChannel.read(byteBuffer);
        byte[] subArray = ArrayUtils.subarray(byteBuffer.array(), 0, readNum);
        byteBuffer.flip();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("读取到数据 :" + new String(subArray, StandardCharsets.UTF_8));

        }

        return subArray;
    }

    /**
     * 发送数据 将CommunicationReturn 转换为JSON数组发送
     *
     * @param communicationReturn
     * @throws IOException
     */
    public synchronized void writeN(CommunicationReturn communicationReturn) throws IOException {
        byteBuffer.clear();
        byteBuffer.put(communicationReturn.toByte());
        byteBuffer.flip();
        //if (LOGGER.isDebugEnabled())
        //    LOGGER.debug("write :" + Charset.forName("UTF-8").decode(byteBuffer).toString());
        socketChannel.write(byteBuffer);
    }


    public synchronized byte[] read() throws IOException {
        int readNum = 0;
        readNum = inputStream.read(bytes);
        byte[] subArray = ArrayUtils.subarray(bytes, 0, readNum);
        LOGGER.debug("read :" + new String(subArray, StandardCharsets.UTF_8));
        return subArray;
    }


    public synchronized void write(CommunicationReturn communicationReturn) throws IOException {
        byte[] bytes = communicationReturn.toByte();
        LOGGER.debug("write :" + new String(bytes, StandardCharsets.UTF_8));
        outputStream.write(bytes);
    }

    /**
     * 读取请求 自动封装为 CommunicationRequest 对象
     *
     * @throws IOException
     */
    public synchronized CommunicationRequest readRequest() throws IOException {
        return CommunicationRequest.createCommunicationRequest(readN());
    }

    public synchronized CommunicationResponse readResponse() throws IOException {
        return CommunicationResponse.createCommunicationResponse(readN());
    }


    /**
     * 判断是否断开连接，断开返回true,没有返回false
     */
    public Boolean sendUrgentData() {
        try {
            socketChannel.socket().sendUrgentData(0xFF);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            return false;
        } catch (Exception se) {
            return true;
        }
    }
}
